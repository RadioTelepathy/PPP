package sim.agent.represenation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

import sim.agent.Bot;
import sim.agent.ExplorerBot;

/**
 * Static method handler for path planning, e.g. A*
 * @author slw546
 *
 */
public class PathPlanner {
	
	public static void aStar(Bot bot, Memory memory, int goalX, int goalY){
		/*
		 * Plan via A* a complete route from a priori position to the goal specified.
		 * OPEN = priority queue containing START
			CLOSED = empty set
			while lowest rank in OPEN is not the GOAL:
			  current = remove lowest rank item from OPEN
			  add current to CLOSED
			  for neighbors of current:
			    cost = g(current) + movementcost(current, neighbor)
			    if neighbor in OPEN and cost less than g(neighbor):
			      remove neighbor from OPEN, because new path is better
			    if neighbor in CLOSED and cost less than g(neighbor): ⁽²⁾
			      remove neighbor from CLOSED
			    if neighbor not in OPEN and neighbor not in CLOSED:
			      set g(neighbor) to cost
			      add neighbor to OPEN
			      set priority queue rank to g(neighbor) + h(neighbor)
			      set neighbor's parent to current
			
			reconstruct reverse path from goal to start
			by following parent pointers
			http://theory.stanford.edu/~amitp/GameProgramming/ImplementationNotes.html
		 */
		
		//Pos stores current location and cost so far
		ArrayList<Node> open   = new ArrayList<Node>();
		ArrayList<Node> closed = new ArrayList<Node>();
		
		open.add(new Node(bot.getX(), bot.getY(), bot.getHeading()));
		Comparator<Node> comparator = new Comparator<Node>(){
			@Override
			public int compare(Node o1, Node o2) {
				if (o1.getCost() < o2.getCost()){
					return -1;
				} else if (o1.getCost() == o2.getCost()) {
					if (o1.getCostToGoal() < o2.getCostToGoal()){
						return -1;
					} else if (o1.getCostToGoal() > o2.getCostToGoal()){
						return 1;
					}
					return 0;
				} else {
					return 1;
				}
			}
		};
		
		Node goal = null;
		while (!open.isEmpty()){
			Node current = open.remove(0);
			closed.add(current);
			if (current.isPos(goalX, goalY)){
				goal = current;
				break;
			}
			
			//generate successors of best node
			ArrayList<Node> successors = bot.getSuccessors(current, memory);
			for (Node s: successors){
				//cost so far + turn cost + advance cost
				int to_reach = current.getCostToReach()+s.turnCost(current.getHeading())+1;
				//int tentative_cost = to_reach + this.evaluatePosition(s.getX(), s.getY(), goalX, goalY);
				
				s.setCost(to_reach, PathPlanner.cartesianDistance(s.getX(), s.getY(), goalX, goalY));

				if (open.contains(s)){
					Iterator<Node> iter = open.iterator();
					while (iter.hasNext()){
						Node o = iter.next();
						if (o.equalPos(s)){
							if (s.getCost() < o.getCost()) {
								//s is a better path than currently possible to this node
								//removes current item
								iter.remove();
							}
						}
					}
				}
				
				//For if we have an inadmissible heuristic:
				if (closed.contains(s)){
					Iterator<Node> iter = closed.iterator();
					while (iter.hasNext()){
						Node o = iter.next();
						if (o.equalPos(s)){
							if (s.getCost() < o.getCost()) {
								//s is better path than previously taken to this node
								//removes current item
								iter.remove();
							}
						}
					}
				}
				
				//Checking for equal pos. nodes in open and closed via node.equals
				if ((!open.contains(s)) && (!closed.contains(s))){
					open.add(s);
				}
				Collections.sort(open, comparator);
			}
		}
		ArrayList<Node> route = new ArrayList<Node>();
		route.add(0, goal);
		//Start on 1,1 so we actually want to cut off the plan
		//the node ahead of that spot; ie. our plan is to move off 1,1
		//not move to 1,1 and move off (since we start there)
		while (goal.getParent() != null){
			Node p = goal.getParent();
			if (!(p.isPos(bot.getX(), bot.getY()))){
				route.add(0, goal.getParent());
			}
			goal = goal.getParent();
		}
		bot.setPlannedRoute(route);
	}
	
	public static Node localExploration(ExplorerBot bot) {
		short[] currentPos = bot.getPos();
		ArrayList<Node> route_taken = bot.getRouteTaken();
		Node parent = null;
		int parent_to_reach = 0;
		char parent_heading = 'r';
		
		if (route_taken.size() != 0){
			parent = bot.getRouteTaken().get(route_taken.size()-1);
			parent_to_reach = parent.getCostToReach();
			parent_heading  = parent.getHeading();
		}
		ArrayList<Node> successors = bot.getSuccessors(parent, currentPos[0], currentPos[1], bot.currentMem);
		
		int cheapest_cost = 99999;
		Node cheapest = null;
		Random rand = new Random();
		ArrayList<short[]> cheapest_LoS = new ArrayList<short[]>();
		for(Node s: successors) {
			int to_reach = parent_to_reach+s.turnCost(parent_heading)+1;
			//discourage staying stationairy
			if (s.isPos(bot.getX(), bot.getY())){
				to_reach += 10;
			}
			ArrayList<short[]> visible = bot.getVisibleCells(s);
			
			if (route_taken.contains(s)){
				//Previously been at this position
				Node p = route_taken.get(route_taken.indexOf(s));
				s.setVisits(p.getVisits());
			}
			
			if (bot.goal_found) {
				s.setCost(to_reach, bot.evaluatePositionDistance(s, bot.goal_pos));//+s.getVisits());
			} else {
				int reveals = bot.evaluatePositionReveals(visible);
				int visits = s.getVisits();
				//subsidise cost by revealed squares, encouraging move to unexplored area
				s.setCost(to_reach, visits-reveals);
			}
			
			if (s.getCost() < cheapest_cost){
				cheapest_cost = s.getCost();
				cheapest = s;
				cheapest_LoS = visible;
			} else if (s.getCost() == cheapest_cost){
				//Tie breaker
				int  n = rand.nextInt(10);
				if (n <= 4){
					cheapest_cost = s.getCost();
					cheapest = s;
					cheapest_LoS = visible;
				}
			}
		}
		cheapest.setLoS(cheapest_LoS);
		return cheapest;
	}
	
	public static int cartesianDistance(int x1, int y1, int x2, int y2){		
		int dist_x = Math.abs(x1 - x2);
		int dist_y = Math.abs(y1 - y2);
		return (dist_x+dist_y);
	}
	
	public static Node tiebreaker(Node n1, Node n2){
		int  n = new Random().nextInt(10);
		if (n <= 4){
			return n1;
		}
		return n2;
	}

}
