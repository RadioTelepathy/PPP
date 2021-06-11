package sim.agent;

import java.util.ArrayList;

import state.AgentState;
import state.StateValue;
import ppp.PPP;
import sim.agent.represenation.LimitedMemory;
import sim.agent.represenation.Memory;
import sim.agent.represenation.Node;
import sim.agent.represenation.PathPlanner;
import sim.agent.represenation.Sensor;

class InvalidMoveError extends Exception {
	private static final long serialVersionUID = 9203487744481552202L;

	public InvalidMoveError() {}
	
	public InvalidMoveError(String msg) { super(msg); }
	
}

/**
 * Abstract class providing interface for Sim Agents
 * and methods useful to all agent subclasses
 * @author slw546
 *
 */
public abstract class Bot {
	private final String BOT_NAME = "Bot";
	private final int STEP_TIME = 250;

	protected ArrayList<String> name_suffixes;
	protected AgentState state;
	protected ArrayList<Node> planned_route;
	protected ArrayList<Node> route_taken;
	protected Sensor sensor;
	public int unknown_cells;
	private int invalidMoves;
	private int successes;
	private int fails;
	protected int testRuns;
	private int totalMoves;
	private int maxMoves;
	private int minMoves;
	private int minInvalidMoves;
	private int maxInvalidMoves;
	private int totalStationaryMoves;
	private int totalTurns;
	private int totalAdv;
	private int totalUnknownCells;
	private int totalInvalidMoves;
	private float avgMoves;
	private float avgTurns;
	private float avgAdv;
	private float avgUnknownCells;
	private float avgInvalidMoves;
	private double sensorNoise;
	
	//Memory - (Partial) views on the PPP, developed via exploration and movement.
	// A priori map information
	public Memory apriori;
	// Current knowledge of map
	public Memory currentMem;
	public Boolean goal_found;
	public short[] goal_pos;
	public int sensorRange = 1;
	
	/*
	 * Constructors
	 */
	// No A Priori Knowledge
	/**
	 * Constructor without A priori map
	 * @param mem Memory space for exploration
	 * @param sensor_range Range of sensor sweep
	 */
	public Bot(Memory currentMem, int sensor_range){
		this(null, currentMem, sensor_range);
	}
	
	// A priori Knowledge
	/**
	 * Constructor with A priori map
	 * @param apriori A priori knowledge of the map, partial or complete
	 * @param mem Memory space for exploration
	 * @param sensor_range Range of sensor sweep
	 */
	public Bot(Memory apriori, Memory currentMem, int sensor_range){
		this.name_suffixes = new ArrayList<String>();
		this.apriori     = apriori;
		this.currentMem  = currentMem;
		if (this.currentMem instanceof LimitedMemory){
			this.name_suffixes.add(this.currentMem.toString());
		}
		this.sensor = new Sensor(sensor_range, 0.0);
		this.sensorRange = sensor_range;
		this.planned_route = new ArrayList<Node>();
		this.route_taken = new ArrayList<Node>();
		this.successes   = 0;
		this.fails       = 0;
		this.totalMoves  = 0;
		this.maxMoves    = 0;
		this.minMoves    = 99999;
		this.totalStationaryMoves = 0;
		this.totalTurns  = 0;
		this.totalAdv    = 0;
		this.avgMoves    = 0;
		this.avgTurns    = 0;
		this.avgAdv      = 0;
		this.testRuns    = 0;
		this.sensorNoise = 0.0;
		this.setUpBot();
	}
	
	public void setUpBot(){
		this.goal_found  = false;
		this.goal_pos    = null;
		this.currentMem.setAllUnsensed();
		//init state
		//zero the count of moves made so far
		this.state = new AgentState((short)1, (short)1,'r');
		this.state.setStateValue((short)0, (short)0, (short) 0);
		//int apriori_unknown = this.apriori == null? 0 : this.apriori.unknownCells();
		this.unknown_cells = this.currentMem == null ? 0: this.currentMem.unknownCells();
		this.planned_route.clear();
		this.route_taken.clear();
	}
	
	public void reset(){
		this.setUpBot();
	}
	
	public void setSensorNoise(double noise){
		this.sensorNoise = noise;
		this.name_suffixes.add(String.format("Noise: %.2f", noise));
		this.sensor.setNoise(noise);
	}
		
	/*
	 * Sense
	 */
	public void sense(PPP ppp){
		this.sensor.sense(ppp, this);
	}
	
	/*
	 * Plan
	 */
	abstract public void aprioriPlan(short goalX, short goalY);
	abstract public void plan();
		
	/*
	 * Execution
	 */
	public void run(PPP ppp, int maxMoves){
		this.run(ppp, maxMoves, false);
	}

	public void run(PPP ppp, int maxMoves, boolean verbose){
		this.run(ppp, maxMoves, verbose, false);
	}
	
	public void run(PPP ppp, int maxMoves, boolean verbose, boolean showSteps){
		short[] pos = this.getPos();
		if ((pos[0] > (ppp.size*2)) || (pos[1] > ppp.size)) {
			System.err.println("Bot started on an invalid Position!");
			System.err.printf("Position: %d,%d :: PPP is %dx%d, ignoring the boundary wall\n", pos[0], pos[1], 2*ppp.size, ppp.size);
			System.exit(1);
		}
		short goalX = (short)(ppp.size*2);
		short goalY = ppp.size;
		int moves = this.route_taken.size();
		this.aprioriPlan(goalX, goalY);
		
		// If we have a limited memory, we won't be able to see the route taken across the whole map
		// So create an overview map (which won't be used by the bot) to print the route on later
		Memory routeMap = null;
		if (this.currentMem instanceof LimitedMemory){
			routeMap = new Memory(ppp);
		}
		
		if (verbose){
			if (this.planned_route.size() > 0){
				if (this.apriori != null) {
					System.out.println("\nPlanned route via A Priori knowledge");
					this.apriori.prettyPrintRoute(this.planned_route);
					//this.printPlannedRoute();
				}
			}
			System.out.println();
		}
		
		while (!this.state.isPos(goalX, goalY)){
			if (this.currentMem instanceof LimitedMemory){
				this.currentMem.rePlot(this.getPos());
			}
			this.sense(ppp);
			//this.currentMem.prettyPrintRoute(route_taken);
			try {
				this.plan();
			} catch (Exception e){
				System.out.println("Route Taken");
				this.currentMem.prettyPrintRoute(this.route_taken);
				System.out.println("\n\nRoute Planned");
				this.currentMem.prettyPrintRoute(this.planned_route);
				System.out.println("\n\n Reachability");
				this.currentMem.prettyPrintRoute(this.route_taken, true);
				System.err.print(e);
				e.printStackTrace();
				System.exit(1);
			}
			try {
				//this.move(ppp);
				if(showSteps){
					this.currentMem.prettyPrintRoute(this.route_taken);
					System.out.printf("Step %d\n\n", moves);
					Thread.sleep(this.STEP_TIME);
				}
				this.move(ppp);
			} catch (InvalidMoveError e) {
				if (verbose) {
					System.err.println(e+" -- replanning\n");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			moves++;
			if (moves >= maxMoves) { break; }
		}
		//Sense around goal pos to tidy up the map
		if (this.currentMem instanceof LimitedMemory){
			this.currentMem.rePlot(this.getPos());
		}
		this.sense(ppp);
		
		boolean success = false;
		if(this.state.isPos(goalX, goalY)){
			if (verbose) {
				System.out.printf("Reached Goal %d,%d in %d moves\n", goalX, goalY, moves);
			}
			success = true;
		} else {
			if (verbose) {
				System.out.printf("Failed to reach goal in %d moves\n", moves);
			}
		}
		
		if(verbose){
			if (routeMap == null) {
				routeMap = this.currentMem;
			}
			System.out.println("\nRoute Taken");
			routeMap.prettyPrintRoute(route_taken);
			System.out.println("\nReachability Map");
			routeMap.prettyPrintRoute(null, true);
			//this.printTakenRoute();
		}
		this.finished(moves, success);
	}
	
	protected void finished(int movesMade, boolean success){
		if (success){
			this.successes++;
		} else {
			this.fails++;
		}
		this.testRuns++;
		this.totalMoves += this.state.getStateValue().getMove();
		this.totalTurns += this.state.getStateValue().getTurn();
		this.totalAdv   += this.state.getStateValue().getAdvance();
		this.totalUnknownCells += this.unknown_cells;
		this.totalInvalidMoves += this.invalidMoves;
		
		this.maxMoves = movesMade > this.maxMoves ? movesMade : this.maxMoves;
		this.minMoves = movesMade < this.minMoves ? movesMade : this.minMoves;
		this.maxInvalidMoves = this.invalidMoves > this.maxInvalidMoves ? this.invalidMoves : this.maxInvalidMoves;
		this.minInvalidMoves = this.invalidMoves < this.minInvalidMoves ? this.invalidMoves : this.minInvalidMoves;
		
		this.avgMoves = (float)this.totalMoves / (float)this.testRuns;
		this.avgTurns = (float)this.totalTurns / (float)this.testRuns;
		this.avgAdv   = (float)this.totalAdv   / (float)this.testRuns;
		this.avgUnknownCells = (float)this.totalUnknownCells / (float)this.testRuns;
		this.avgInvalidMoves = (float)this.totalInvalidMoves / (float)this.testRuns;
		this.reset();
	}
	
	public void move(PPP ppp) throws InvalidMoveError{
		Node move_to = this.planned_route.remove(0);
		if ((move_to.getX() == this.getX()) &&(move_to.getY() == this.getY())){
			this.totalStationaryMoves++;
		}
		
		boolean invalid = false;
		if (ppp.isOccupied(move_to.getX(), move_to.getY(), true)){
			// Invalid move made!
			invalid = true;
			this.invalidMoves++;
			throw new InvalidMoveError("Invalid move - " + move_to.toString()+" is Occupied\n");
		}
		if (invalid) {
			System.out.println("invalid move beyond throw");
		}
		
		move_to.incVisits();
		if (this.route_taken.contains(move_to)){
			this.route_taken.get(this.route_taken.indexOf(move_to)).incVisits();
		}
		this.route_taken.add(move_to);
		
		short turns = (short)(this.state.getStateValue().getTurn()+move_to.turnCost(this.getHeading()));
		short adv = (short)(this.state.getStateValue().getAdvance()+1);
		short moves = (short)(turns+adv);
		
		this.state.setStateValue(turns, adv, moves);
		this.state.setAgentState(move_to.getX(), move_to.getY(), move_to.getHeading());
	}
	
	/*
	 * Positioning
	 */
	public short[] getPos(){
		short[] ret = {this.state.getX(), this.state.getY()};
		return ret;
	}
	
	public short getX() {
		return this.state.getX();
	}
	
	public short getY(){
		return this.state.getY();
	}
	
	public char getHeading(){
		return this.state.getDirection();
	}
	
	public ArrayList<Node> getSuccessors(Node n, Memory mem){
		return this.getSuccessors(n, n.getX(), n.getY(), mem);
	}
	
	public ArrayList<Node> getSuccessors(Node parent, int nX, int nY, Memory mem){
		ArrayList<Node> successors = new ArrayList<Node>();
		
		//up
		if (mem.validPosition(nX, nY-1)){
			successors.add(new Node(parent, nX, nY-1, 'u'));
		}
		//down
		if (mem.validPosition(nX, nY+1)){
			successors.add(new Node(parent, nX, nY+1, 'd'));
		}
		//left
		if (mem.validPosition(nX-1, nY)){
			successors.add(new Node(parent, nX-1, nY, 'l'));
		}
		//right
		if (mem.validPosition(nX+1, nY)){
			successors.add(new Node(parent, nX+1, nY, 'r'));
		}
		//Stay still
		successors.add(new Node(parent, nX, nY, this.getHeading()));
		
		return successors;
	}
	
	public Node getCheapestSuccessor(ArrayList<Node> succ){
		return this.getCheapestSuccessor(succ, true);
	}
	
	public Node getCheapestSuccessor(ArrayList<Node> succ, Boolean tiebreak){
		int cheapest_cost = 99999;
		Node cheapest = null;
		for (Node s: succ){
			if (s.getCost() < cheapest_cost){
				cheapest_cost = s.getCost();
				cheapest = s;
			} else if ((s.getCost() == cheapest_cost) && (tiebreak)){
				//Tie breaker
				cheapest = PathPlanner.tiebreaker(s, cheapest);
				cheapest_cost = cheapest.getCost();
			}
		}
		return cheapest;
	}
	
	/*
	 * Movement
	 */
	public void turnRight(){
		this.state.turnRight();
	}
	
	public void turnLeft(){
		this.state.turnLeft();
	}
	
	public void advance(){
		this.state.advance();
	}
	
	/*
	 * Results 
	 */
	
	public StateValue getStateValue(){
		return this.state.getStateValue();
	}
	
	public boolean atGoalPos(short goalX, short goalY){
		if (goalX == this.getX() && (goalY == this.getY())) return true;
		return false;
	}
	
	public void printPlannedRoute(){
		System.out.printf("Route planned in %d steps\n", this.planned_route.size());
		this.printRoute(this.planned_route);
	}
	
	public void printTakenRoute(){
		System.out.printf("Route of %d steps\n", this.route_taken.size());
		this.printRoute(this.route_taken);
	}
	
	private void printRoute(ArrayList<Node> route){
		System.out.printf("Start: %s :: End: %s\n", route.get(0).toString(), 
				route.get(route.size()-1).toString());
		for (Node n: route){
			System.out.println(n.toString());
		}
		System.out.print("\n");
	}
	
	public void printTestResults(){
		System.out.printf("Bot: %s\n", this.getName());
		System.out.printf("    Successes: %d Fails: %d\n", this.successes, this.fails);
		System.out.printf("    Success Rate: %.2f%%\n", 100*((float)this.successes / (float)this.testRuns));
		System.out.printf("    Total Moves: %d, Avg Moves: %.2f\n",  this.totalMoves, this.avgMoves);
		System.out.printf("    Max Moves: %d, Min Moves: %d\n",      this.maxMoves, this.minMoves);
		System.out.printf("    Avg Advances: %.2f, Avg Turns: %.2f\n", this.avgAdv, this.avgTurns);
		System.out.printf("    Avg Unknown Cells: %.2f\n", this.avgUnknownCells);
		
		if (this.sensorNoise != 0.0){
			System.out.printf("    Sensor Noise: %.2f\n", this.sensorNoise);
			System.out.printf("    Total invalid moves: %d, Avg invalid moves: %.2f\n", this.totalInvalidMoves, this.avgInvalidMoves);
		}
		
		if (this.totalStationaryMoves != 0){
			System.out.printf("    Remained stationary %d times over all runs\n", this.totalStationaryMoves);
		}
	}
	
	public String getTestResults(){
		double passRate = 100*((float)this.successes / (float)this.testRuns);
		return String.format("%.2f", passRate);
	}
	
	public ArrayList<Node> getPlannedRoute(){
		return this.planned_route;
	}
	
	public void setPlannedRoute(ArrayList<Node> plan){
		this.planned_route = plan;
	}
	
	public ArrayList<Node> getRouteTaken(){
		return this.route_taken;
	}
	
	public String getName(){
		return this.BOT_NAME;
	}
	
	public String getSuffix(){
		if (this.name_suffixes.size() > 0) {
			return " " + this.name_suffixes.toString();
		}
		return "";
	}
	
}
