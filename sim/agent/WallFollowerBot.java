package sim.agent;

import java.util.ArrayList;

import sim.agent.represenation.Memory;
import sim.agent.represenation.Node;

/**
 * Bot which tries to follow a wall to it's left or right
 * @author slw546
 */
public class WallFollowerBot extends Bot{
	private final String BOT_NAME = "WallFollower";

	private char wallFollowSide;
	
	/**
	 * Constructor
	 * @param mem - empty memory, this bot should explore
	 * @param sensor_range - range of sensor
	 * @param wallFollowSide - one of l or r - side to keep against a wall
	 */
	public WallFollowerBot(Memory mem, int sensor_range, char wallFollowSide) {
		super(mem, sensor_range);
		this.wallFollowSide = wallFollowSide;
		this.name_suffixes.add(Character.toString(wallFollowSide));
		
		//char initialDir = wallFollowSide == 'l' ? 'r' : 'l';
		//this.state = new AgentState((short)1, (short)1, initialDir);
		//zero the count of moves made so far
		//this.state.setStateValue((short)0, (short)0, (short) 0);
	}
	
	@Override
	// No prior plan, as no information known by this bot without exploration.
	public void aprioriPlan(short goalX, short goalY) {}
	
	@Override
	public void plan() {
		short[] currentPos = this.getPos();
		
		Node parent = null;
		int parent_to_reach = 0;
		char parent_heading = 'r';
		if (this.route_taken.size() != 0){
			parent = this.route_taken.get(this.route_taken.size()-1);
			parent_to_reach = parent.getCostToReach();
			parent_heading  = parent.getHeading();
		}
		ArrayList<Node> successors = this.getSuccessors(parent, currentPos[0], currentPos[1], currentMem);
		
		for (Node s: successors){
			int to_reach = parent_to_reach+s.turnCost(parent_heading)+1;
			s.setCost(to_reach, this.evaluatePosition(s));
		};
		
		Node cheapest = this.getCheapestSuccessor(successors);
			
		this.planned_route.add(cheapest);
	}
	
	/*
	 * Discouraging cycles -
	 * Random tiebreaker between options
	 * penalise cell cost by  weight of no. of visits
	 * Set default cost of cell to cost of about turn to encourage turning
	 */
	
	private int evaluatePosition(Node n) {
		//Subsidise nodes which keep the wall on the correct side
		char wallSide = this.keepWallOnSide(n.getHeading());
		//Cost == Cost of about turn - higher encourages cycles
		//Too low encourages movement away from the wall if a turn is required
		
		int cost = 10;		
		switch (wallSide){
			case 'u':
				if (this.currentMem.occupied(n.getX(), n.getY()-1)){
					cost = 0;
				}
				break;
			case 'd':
				if (this.currentMem.occupied(n.getX(), n.getY()+1)){
					cost = 0;
				}
				break;
			case 'l':
				if (this.currentMem.occupied(n.getX()-1, n.getY())){
					cost = 0;
				}
				break;
			case 'r':
				if (this.currentMem.occupied(n.getX()+1, n.getY())){
					cost = 0;
				}
				break;
			default:
				break;
		}
		
		if (n.isPos(this.getX(), this.getY())){
			//Discourage not moving
			cost = 100;
		}
		
		if (this.currentMem.isGoal(n.getX(), n.getY())){
			//subsidise the goal position
			cost = 0;
		}
		
		//Intended fault: This bot will happily be trapped in a cycle.
//		if (this.route_taken.contains(n)){
//			//Previously been at this position
//			Node p = this.route_taken.get(this.route_taken.indexOf(n));
//			p.incVisits();
//			n.setVisits(p.getVisits());
//		}
		int c = cost;//+(2*n.getVisits());
		return c;
	}
	
	private char keepWallOnSide(char currentHeading){
		//Given a heading, return side which the wall should be on
		switch(currentHeading){
		case 'r':
			switch(this.wallFollowSide){
			case 'l':
				return 'u';
			case 'r':
				return 'd';
			default:
				return 'u';
			}
		case 'l':
			switch(this.wallFollowSide){
			case 'l':
				return 'd';
			case 'r':
				return 'u';
			default:
				return 'd';
			}
		case 'u':
			switch(this.wallFollowSide){
			case 'l':
				return 'l';
			case 'r':
				return 'r';
			default:
				return 'l';
			}
		case 'd':
			switch(this.wallFollowSide){
			case 'l':
				return 'r';
			case 'r':
				return 'l';
			default:
				return 'r';
			}
		default:
			return 'l';
		}
	}
	
	@Override
	public String getName(){
		return this.BOT_NAME + super.getSuffix();
	}

}
