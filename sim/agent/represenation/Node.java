package sim.agent.represenation;

import java.util.ArrayList;

/**
 * Class representing a (potential) point on a bot's route
 * @author slw546
 */
public class Node {
	private Node parent;
	private int pos_x;
	private int pos_y;
	private int cost;
	private int cost_to_reach;
	private int cost_to_goal;
	private int visits;
	private ArrayList<short[]> lineOfSight;
	
	//Heading of the robot when it enters this node
	//Influences cost of leaving it (robot may have to turn)
	private char heading;
	
	public Node(int x, int y, char heading){
		// Root Node
		this(null, x, y, heading);
	}
	
	public Node(Node parent, int x, int y, char heading){
		// Unevaluated Node
		this(parent, x, y, heading, 0, 0);
	}
	
	public Node(Node parent, int x, int y, char heading, int cost_to_reach, int cost_to_goal){
		// Fully evaluated Node
		this.parent = parent;
		this.pos_x  = x;
		this.pos_y  = y;
		this.heading = heading;
		this.cost_to_reach = cost_to_reach;
		this.cost_to_goal  = cost_to_goal;
		this.cost = cost_to_reach + cost_to_goal;
		this.visits = 0;
	}
	
	private char oppositeHeading(char h){
		switch(h){
			case 'u':
				return 'd';
			case 'd':
				return 'u';
			case 'l':
				return 'r';
			default:
				return 'l';
		}			
	}
	
	public boolean isPos(int x, int y){
		if ((this.pos_x == x) && this.pos_y == y) return true;
		return false;
	}
	
	public Node getParent(){
		return this.parent;
	}
	
	public void setCostToReach(int cost){
		this.cost_to_reach = cost;
	}
	
	public void setCostToGoal(int cost){
		this.cost_to_goal = cost;
	}
	
	public void setCost(int to_reach, int to_goal){
		this.cost_to_reach = to_reach;
		this.cost_to_goal = to_goal;
		this.cost = this.cost_to_reach + this.cost_to_goal;
	}
	
	public int getCost(){
		this.cost = this.cost_to_reach + this.cost_to_goal;
		return this.cost;
	}
	
	public int getCostToReach(){
		return this.cost_to_reach;
	}
	
	public int getCostToGoal(){
		return this.cost_to_goal;
	}
	
	public int getX(){
		return this.pos_x;
	}
	
	public int getY(){
		return this.pos_y;
	}
	
	public char getHeading(){
		return this.heading;
	}
	
	public boolean equalPos(Node n){
		return this.isPos(n.getX(), n.getY());
	}
		
	public int turnCost(char heading){
		//no turn
		if (heading == this.heading){
			return 0;
		}
		//about turn
		if (heading == this.oppositeHeading(this.heading)){
			return 2;
		}
		//one turn
		return 1;
	}
	
	public String toString(){
		return String.format("Node: %d,%d, heading:%s, cost: %d", this.pos_x, this.pos_y, this.heading, this.cost);
	}
	
	@Override
	public boolean equals(Object n){
		if ((n != null) && n instanceof Node){
			return this.equalPos((Node)n);
		}
		return false;
	}
	
	public void incVisits(){
		this.visits ++;
	}
	
	public void setVisits(int v){
		this.visits = v;
	}
	
	public int getVisits(){
		return this.visits;
	}
	
	public void setLoS(ArrayList<short[]> los){
		this.lineOfSight = los;
	}
	
	public ArrayList<short[]> getLoS(){
		return this.lineOfSight;
	}
}
