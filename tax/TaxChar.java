package tax;

import java.util.ArrayList;

import state.StateValue;

/*
 * 	Author:	Hao Wei
 * 	Time:	24/05/2013
 * 	Purpose: To represent the four taxonomic characters by a single vector
 */
public class TaxChar {
	private float advance;			// the number of advances
	private float turn;			// the number of turns
	private float move;		 	// the number of moves
	private float obstruction;		// the number of obstructions
	private float goalVis;
	private float startVis;
	private float centreVis;
	private float topRightVis;
	private float bottomLeftVis;
	private float obsUsage;
	private float reachableCellRatio;
	private float avgHorizontalOpenness;
	private float avgVerticalOpenness;
	private float norm;
	private boolean extra_chars = false;
	//private float difficulty;
	
	
	private boolean wasted = false;	// whether this TaxChar is wasted.
	private short sAdvance;			// advance for toString
	private short sTurn;				// turn for toString
	private short sObs;				// obstruction for toString
	private String name;			// name for the TaxChar
	private boolean isMerge = false;
	private boolean isRoot = false;
	private int id=0;
	private String parent;
	public ArrayList<String> children;
	/*
	 * 	The constructor for TaxChar by passing each values in int
	 * 	Notes: move = advance + turn
	 */
	public TaxChar(short advance, short turn, short obstruction){
		this.sAdvance = advance;
		this.sTurn = turn;
		this.sObs = obstruction;
		this.advance = (float) advance;
		this.turn = (float) turn;
		this.move = (float)(advance + turn);
		this.obstruction = (float) obstruction;
		this.children = new ArrayList<String>();
		this.goalVis = 0;
		this.startVis = 0;
		this.centreVis = 0;
		this.topRightVis = 0;
		this.bottomLeftVis = 0;
		this.obsUsage = 0;
		this.reachableCellRatio = 0;
		this.avgHorizontalOpenness = 0;
		this.avgVerticalOpenness = 0;
		//normalizeTC();
	}
	/*
	 * 	The constructor for TaxChar by passing each values int double
	 * 	Notes: move = advance + turn
	 */
	public TaxChar(float advance, float turn, float obstruction){
		this.sAdvance = (short)advance;
		this.sTurn = (short)turn;
		this.sObs = (short)obstruction;
		this.advance = advance;
		this.turn = turn;
		this.move = advance + turn;
		this.obstruction = obstruction;
		this.children = new ArrayList<String>();
		this.goalVis = 0;
		this.startVis = 0;
		this.centreVis = 0;
		this.topRightVis = 0;
		this.bottomLeftVis = 0;
		this.obsUsage = 0;
		this.reachableCellRatio = 0;
		this.avgHorizontalOpenness = 0;
		this.avgVerticalOpenness = 0;
		//normalizeTC();
	}
	/*
	 * 	The constructor for TaxChar by passing the StateValue
	 * 	and the number of obstructions in int
	 */
	public TaxChar(StateValue sv, short obstruction){
		this.sAdvance = sv.getAdvance();
		this.sTurn = sv.getTurn();
		this.sObs = obstruction;
		this.advance = (float) sv.getAdvance();
		this.turn = (float) sv.getTurn();
		this.move = (float) sv.getMove();
		this.obstruction = (float) obstruction;
		this.children = new ArrayList<String>();
		this.goalVis = 0;
		this.startVis = 0;
		this.centreVis = 0;
		this.topRightVis = 0;
		this.bottomLeftVis = 0;
		this.obsUsage = 0;
		this.reachableCellRatio = 0;
		this.avgHorizontalOpenness = 0;
		this.avgVerticalOpenness = 0;
		//normalizeTC();
	}
	
	public void addExtraCharacters(double goalVis, double startVis, double centreVis, double topRightVis, 
			double bottomLeftVis, double obsUsage, double reachableRatio, double openH, double openV){
		this.addExtraCharacters((float)goalVis, (float)startVis, (float)centreVis, (float)topRightVis, (float)bottomLeftVis,
				(float)obsUsage, (float)reachableRatio, (float)openH, (float)openV);
	}
	
	public void addExtraCharacters(float goalVis, float startVis, float centreVis, float topRightVis, 
			float bottomLeftVis, float obsUsage, float reachableRatio, float openH, float openV){
		this.goalVis = goalVis;
		this.startVis = startVis;
		this.centreVis = centreVis;
		this.topRightVis = topRightVis;
		this.bottomLeftVis = bottomLeftVis;
		if (obsUsage > 1) { obsUsage = 1;}
		this.obsUsage = obsUsage;
		this.reachableCellRatio = reachableRatio;
		this.avgHorizontalOpenness = openH;
		this.avgVerticalOpenness = openV;
		this.extra_chars = true;
	}
	
	//Renormalise after adding extra chars
	public void normalizeTC(){
		float[] arr = {advance, turn, move, obstruction};
		if (this.extra_chars){
			arr = new float[]{0, turn, move, obstruction, goalVis, startVis, centreVis, topRightVis, bottomLeftVis,
					obsUsage, reachableCellRatio, avgHorizontalOpenness, avgVerticalOpenness};
		}
		float total = 0;
		for(float f : arr){
			total += (float) Math.pow(f, 2);
		}
		this.norm = (float) Math.sqrt(total);
		
		advance = advance / norm;
		turn = turn / norm;
		move = move / norm;
		obstruction = obstruction / norm;
		
		goalVis = goalVis / norm;
		startVis = startVis / norm;
		centreVis = centreVis / norm;
		topRightVis = topRightVis / norm;
		bottomLeftVis = bottomLeftVis / norm;
		obsUsage = obsUsage / norm;
		reachableCellRatio = reachableCellRatio / norm;
		avgHorizontalOpenness = avgHorizontalOpenness / norm;
		avgVerticalOpenness = avgVerticalOpenness / norm;
	}

	/*
	 * 	Calculate the Euclidian Distance between two TaxChar
	 */
	public float EuclidianDistance(TaxChar tc){
		float q1 = 0;//advance - tc.getAdvance();
		float q2 = turn - tc.getTurn();
		float q3 = move - tc.getMove();
		float q4 = obstruction - tc.getObs();
		float q5 = goalVis - tc.getGoalVis();
		float q6 = startVis - tc.getStartVis();
		float q7 = centreVis - tc.getCentreVis();
		float q13 = topRightVis - tc.getTopRVis();
		float q8 = bottomLeftVis - tc.getBottomLeftVis();
		float q9 = obsUsage - tc.getObsUsage();
		float q10 = reachableCellRatio - tc.getReachableCellRatio();
		float q11 = avgHorizontalOpenness - tc.getHOpen();
		float q12 = avgVerticalOpenness - tc.getVOpen();
		float[] arr = {q1, q2, q3, q4};
		if (this.extra_chars){
			arr = new float[]{q1, q2, q3, q4, q5, q6, q7, q8, q9, q10, q11, q12, q13};
		}
		float total = 0;
		for (float f : arr){
			total += Math.pow(f, 2);
		}
		return (float)Math.sqrt(total);
	}
	/*
	 * 	return advance
	 */
	public float getAdvance(){
		return advance;
	}
	/*
	 * 	return turn
	 */
	public float getTurn(){
		return turn;
	}
	/*
	 * 	return move ****** this method is optional
	 */
	public float getMove(){
		return move;
	}
	/*
	 * 	return obstruction
	 */
	public float getObs(){
		return obstruction;
	}
	
	public float getReachableCellRatio(){return this.reachableCellRatio;}
	public float getObsUsage(){return this.obsUsage;}
	public float getBottomLeftVis(){return this.bottomLeftVis;}
	public float getTopRVis(){return this.topRightVis;}
	public float getGoalVis(){ return this.goalVis;}
	public float getStartVis(){ return this.startVis;}
	public float getCentreVis(){ return this.centreVis;}
	public float getVOpen(){ return this.avgVerticalOpenness;}
	public float getHOpen(){ return this.avgHorizontalOpenness;}
	public float getNorm(){ return this.norm;}
	public boolean hasExtraChars(){return this.extra_chars;}
	
	/*
	 * 	Determine whether this TaxChar is wasted
	 * 	return true if it is wasted
	 */
	public boolean wasted(){
		return wasted;
	}
	/*
	 * 	Set this TaxChar to be wasted
	 */
	public void setWaste(){
		wasted = true;
	}
	/*
	 * 	Set name for this TaxChar
	 */
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setMerged(){
		this.isMerge = true;
	}
	
	public boolean isMerged(){
		return this.isMerge;
	}
	
	public void setRoot(){
		this.isRoot = true;
	}
	
	public boolean isRoot(){
		return this.isRoot;
	}
	
	/*
	 * 	toString
	 */
	public String toString(){
		String result;
		result = name+ "("+sAdvance+"-"+sTurn+"-"+sObs+")";
		return result;
	}
	
	public void printDebugInfo(){
		System.out.printf("adv %.2f turn %.2f obs %.2f\n", advance, this.turn, this.obstruction);
		if (this.extra_chars){
			System.out.printf("gv %.2f cv %.2f sv %.2f\n", this.goalVis, this.centreVis, this.startVis);
			System.out.printf("trv %.2f blv %.2f oU %.2f\n", this.topRightVis, this.bottomLeftVis, this.obsUsage);
			System.out.printf("RC %.2f hO %.2f vO %.2f\n", this.reachableCellRatio, this.getHOpen(), this.getVOpen());
		}
		System.out.printf("normaliser %.2f extra chars %s\n", this.norm, this.hasExtraChars());
	}
	
	public String toForceJson(){
		String nodeType = this.isMerge ? "merge" : "leaf";
		return String.format("{\"name\":\"%s\", \"node\":\"%s\"}", this.name, nodeType);
	}
	
	public String toTreeJson(){
		String parent = this.parent == null ? "null" : this.parent;
		return String.format("{\"name\": \"%s\", \"parent\":\"%s\"", this.name, parent);
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return this.id;
	}
	
	public void setParent(String p){
		this.parent = p;
	}
	
	public String getParent(){
		return this.parent;
	}
	
	public void addChild(String c){
		this.children.add(c);
	}
	
	public ArrayList<String> getChildren(){
		return this.children;
	}
}
