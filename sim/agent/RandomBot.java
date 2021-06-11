package sim.agent;

import java.util.ArrayList;
import java.util.Random;

import sim.agent.represenation.Memory;
import sim.agent.represenation.Node;
import sim.agent.represenation.PathPlanner;

/**
 * Bot which moves randomly
 * @author slw546
 */
public class RandomBot extends Bot{
	private final String BOT_NAME = "Random Bot";

	private Random random;
	public RandomBot(Memory currentMem, int sensor_range) {
		super(currentMem, sensor_range);
		this.random = new Random();
	}

	@Override
	public void aprioriPlan(short goalX, short goalY) {	
	}

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
			if (this.goal_found){
				s.setCost(to_reach, this.evaluatePositionDistance(s));
			} else {
				s.setCost(to_reach, this.evaluatePosition(s));
			}
		};
		
		Node cheapest = this.getCheapestSuccessor(successors);
			
		this.planned_route.add(cheapest);
	}
	
	/*
	 * Evaluate cell via distance to goal
	 * Use once the goal is found to path the bot towards it.
	 */
	private int evaluatePositionDistance(Node n){
		return PathPlanner.cartesianDistance(n.getX(), n.getY(), this.goal_pos[0], this.goal_pos[1]);
	}
	
	public int evaluatePosition(Node s){
		int ret = this.random.nextInt(4);
		if (s.isPos(this.getX(), this.getY())){
			ret += this.random.nextInt(4);
		}
		return ret;
	}
	
	@Override
	public String getName(){
		return this.BOT_NAME + super.getSuffix();
	}
}
