package sim.agent;

import sim.agent.represenation.Memory;
import sim.agent.represenation.PathPlanner;

/**
 * Bot which uses perfect A Priori knowledge to find the optimal route
 * @author slw546
 */
public class OmniscientBot extends Bot{
	private final String BOT_NAME = "Omniscient A* Bot";

	/*
	 * A bot which has full knowledge of the map and therefore finds the perfect route.
	 */
	
	/**
	 * Constructor
	 * @param apriori - the full occ grid of the PPP under test
	 * @param sensor_range - any int, this bot does not need to sense.
	 */
	public OmniscientBot(Memory apriori, int sensor_range) {
		super(apriori, new Memory(apriori.mem_width, apriori.mem_height), sensor_range);
	}
	
	@Override
	public void aprioriPlan(short goalX, short goalY){
		PathPlanner.aStar(this, this.apriori, goalX, goalY);
	}

	@Override 
	public void plan() {}
	
	/**
	 * Returns cost to goal from position tested
	 */
	protected int evaluatePosition(short x, short y, short goalX, short goalY){
		return this.evaluatePosition((int)x, (int)y, goalX, goalY);
	}
	
	protected int evaluatePosition(int x, int y, short goalX, short goalY){
		int dist_x = Math.abs(goalX - x);
		int dist_y = Math.abs(goalY - y);
		return (dist_x+dist_y);
	}
	
	@Override
	public String getName(){
		return this.BOT_NAME + super.getSuffix();
	}
}
