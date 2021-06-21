package ppp;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import state.AgentState;
import state.StateValue;
import sim.agent.represenation.Sensor;
import sim.agent.represenation.Memory;

/*
 * 	Author: Hao Wei
 * 	Time:	14/05/2013
 * 	Description: PPP(size, lDes, nDes)
 */

public class PPP{
	public short size;				// the size of the PPP (The height and half the width of the PPP, excluding outer border)
	private short nDes;				// the number of descriptors
	private short maxObs;			// the maximum number of obstructions allowed
	private int obsUsed; 			// Number of obstacles used
	private short row;				// the number of chars in one column
	private short col;				// the number of chars in one row
	private char[][] map;			// the char array of the map
	public Descriptor[] arrayDes;	// the array of the descriptors
	//private Descriptor[] tempDes;	// the temp array of the descriptors, for updatePPP
	private short crossover;		// The number of features of a child that come from parent 1
	/*
	 * 	0 presents the current cell is non-occupied and 1 presents occupied
	 * 	2 presents the cell is occupied by the agent or the destination
	 */
	private short[][] occ; 
	/*
	 * 	The state space array of all possible triples (x,y,h) that the agent can occupy
	 */
	private AgentState[] asArray;
	private short asSize;			// the size of the array of agent state
	private boolean availability;	// the availability of the PPP, whether it is reachable or not
	private StateValue bestSV;		// the best StateValue
	private AgentState finalAS; 	// the final AgentState
	
	//Difficulty
	private int totalCells;				// Number of Cells?
	private int goalVisibleCells;		// Cells visible from the goal position
	private int startVisibleCells;		// Cells visible from the start position
	private int centreVisibleCells;		// Cells visible from the middle of the map
	private int topRightVisibleCells;	// Cells visible from the top right position
	private int bottomLeftVisibleCells;	// Cells visible from the bottom left position
	private int reachableCells;			// Number of cells the agent could reach starting from the start position
	private int unreachableCells;		// Number of cells the agent could not reach starting from the start position
	private int maxOpenWidth;			// Largest row of open spaces
	private int maxOpenHeight;			// Largest column of open spaces
	private double avgOpenCellsW;		// Average row of open spaces
	private double avgOpenCellsV;		// Average column of open spaces
	
	private double goalVisiblePercentage;		// % Cells visible from the goal position
	private double startVisiblePercentage;		// % Cells visible from the start position
	private double centreVisiblePercentage;		// % Cells visible from the middle of the map
	private double topRightVisiblePercentage;	// % Cells visible from the top right position
	private double bottomLeftVisiblePercentage;	// % Cells visible from the bottom left position
	private double visibilityMagnitude;			// Square Root of the Sum of the square of each visible percentage (PPP 61-65)
	private double visibilityPercentageSum;		// % visibility sum - Never used
	private double obstaclesUsePercentage;		// % obstacles used
	private double visibilityWeighted;			// ??weighted?? ??visibility??
	private double reachableRatio;				// Ratio of Reachable to non-reachable cells
	private double reachablePercent;			// Percentage of cells that are reachable
	private double avgOpenWidth;				// Average open ??width??
	private double avgOpenHeight;				// Average open ??height??
	private double sumVis;						// Visibility summed
	
	
	/*
	 * 	Initialize the map
	 */
	public PPP(short size, short nDes, short maxObs){
		this.size = size;
		this.nDes = nDes;
		this.maxObs = maxObs;
		this.goalVisibleCells = 0;
		this.obsUsed = 0;
		this.totalCells = (size*2)*size;
		row = (short)(size+2);
		col = (short)(size*2+2);
		map = new char[row][col];
		occ = new short[row][col];
		arrayDes = new Descriptor[nDes]; // ??I still don't understand what descriptors means??
		availability = false; // Initialisation - assume the PPP solution is unreachable
		createPPP();
	}
	/*
	 * 	Initialize the map using a given PPP
	 */
	public PPP(PPP ppp){
		this.size = ppp.size;
		this.nDes = ppp.nDes;
		this.maxObs = ppp.maxObs;
		this.asSize = ppp.asSize; // ??asSize??
		this.goalVisibleCells = 0;
		this.startVisibleCells = 0;
		this.totalCells = (this.size*2)*this.size;
		row = (short)(size+2);
		col = (short)(size*2+2);
		map = new char[row][col];
		occ = new short[row][col];
		arrayDes = new Descriptor[nDes];
		availability = false;
		System.arraycopy(ppp.arrayDes, 0, this.arrayDes, 0, ppp.arrayDes.length); // ??I don't know??
		copyPPP(); // ??copy?? the PPP for ??reasons??
	}
	/*
	 * initialize the occ array
	 */
	private void iniOcc(){ //Initialize the occupancy array
		for (short i = 0; i<row; i++){
			for (short j = 0; j<col; j++){
				occ[i][j] = 0; //Everything starts empty
			}
		}
	}
	/*
	 *  Draw map with the occ
	 */
	public void drawMap(){ //Draw the visual map
		map[1][1] = '-'; // Filled in Space
		map[1][2] = '>'; // The Agent
		map[row-2][col-2] = '*'; // Goal
		map[row-2][col-3] = '*'; // Goal
		for (short i = 0; i<row; i++){
			for (short j = 0; j<col; j++){
				if(occ[i][j]==1){
					map[i][j] = '#'; // ??Outer Wall??
				}
				if(occ[i][j]==0){
					map[i][j] = '.'; // Empty Space
				}
				if(occ[i][j]==3){
					map[i][j] = '['; // Obstacle
				}
				if(occ[i][j]==4){
					map[i][j] = ']'; // Obstacle
				}
				if(occ[i][j]==9) {
					map[i][j] = '('; // Clear Obstacle Left
				}
				if(occ[i][j]==10) {
					map[i][j] = ')'; // Clear Obstacle Right
				}
			}
		}
	}
	/*
	 * 	initialize boundaries for createDescriptors
	 */
	public void iniBoundary(){
		for (short i = 0; i<col; i++){
			occ[0][i] = 1;
		}
		for (short i = 1; i< row-1; i++){
			occ[i][0] = 1;
		}
		for (short i = 1; i<row-1; i++){
			occ[i][col-1] = 1;
		}
		for (short i = 0; i<col; i++){
			occ[row-1][i] = 1;
		}
	} 
	/*
	 * 	initialize agency for createDescriptors 
	 */
	public void iniAgency(){ // Initialise start state for the agent
		occ[1][1] = 2;
		occ[1][2] = 2;
	}
	/*
	 * 	initialize destination for createDescriptors
	 */
	public void iniDestination(){ //Initialise the goal state
		occ[row-2][col-2] = 12;
		occ[row-2][col-3] = 12;
	}
	/*
	 * 	initialize all the descriptors
	 */
	private void iniDescriptors(){
		short obsLeft = maxObs;	// the number obstructions still left for the PPP
		short lDes;				// the number of obstructions allowed in a single descriptor
		arrayDes = new Descriptor[nDes];
		for (short i = 0; i<nDes; i++){
			Random generator = new Random();
			short rRow, rCol;
			short obsType;
			do{
				// the x position of the descriptor
				rRow = (short)(generator.nextInt(size)+1);
				// the y position of the descriptor
				rCol = (short)(generator.nextInt(size)*2+1);


			}while(occ[rRow][rCol]==2||occ[rRow][rCol]==12);
			// the type of the descriptor, totally six.
			short type = (short)generator.nextInt(6);
			// the type of descriptor being created
			obsType = (short)generator.nextInt(2);
			short currentLength = 0;	// the length of the current descriptor
			/*
			 * 	If the remaining obsLeft is greater than one, then create descriptor
			 * 	If the remaining obsLeft is zero, then create zero length descriptor
			 */
			if(obsLeft>0){
				lDes = lengthDes(obsLeft); // the maximum number of obstructions for one descriptor
				if(occ[rRow][rCol]==0 && obsType % 2 == 0){
					occ[rRow][rCol] = 3;
					occ[rRow][rCol+1] = 4;
					currentLength++;
					lDes--;
				} else {
					occ[rRow][rCol] = 9;
					occ[rRow][rCol+1] = 10;
					currentLength++;
					lDes--;
				}
				// 0 presents right
				if (type == 0){
					for (short j=0; j<lDes; j++){
						if(rCol+j*2<col-2) {
							if (occ[rRow][rCol + j * 2] == 0 && obsType % 2 == 0) {
								occ[rRow][rCol + j * 2] = 3;
								occ[rRow][rCol + 1 + j * 2] = 4;
								currentLength++;
							}else if(occ[rRow][rCol + j * 2] == 0 && obsType % 2 == 1){
								occ[rRow][rCol + j * 2] = 9;
								occ[rRow][rCol + 1 + j * 2] = 10;
								currentLength++;
							}else lDes++;
						}else{
							break;
						}
					}
				}
				// 1 presents left
				if (type == 1){
					for (short j=0; j<lDes; j++){
						if(rCol-j*2>0){
							if(occ[rRow][rCol-j*2]==0 && obsType % 2 == 0){
								occ[rRow][rCol-j*2] = 3;
								occ[rRow][rCol+1-j*2] = 4;
								currentLength++;
							}else if(occ[rRow][rCol-j*2]==0 && obsType % 2 == 1){
								occ[rRow][rCol-j*2] = 9;
								occ[rRow][rCol+1-j*2] = 10;
								currentLength++;
							} else lDes++;
						}else{
							break;
						}
					}
				}
				// 2 presents up
				if (type == 2){
					for (short j=0; j<lDes; j++){
						if(rRow-j>0){
							if(occ[rRow-j][rCol]==0 && obsType % 2 == 0){
								occ[rRow-j][rCol] = 3;
								occ[rRow-j][rCol+1] = 4;
								currentLength++;
							}else if(occ[rRow-j][rCol]==0 && obsType % 2 == 1){
								occ[rRow-j][rCol] = 9;
								occ[rRow-j][rCol+1] = 10;
								currentLength++;
							} else lDes++;
						}else{
							break;
						}
					}
				}
				// 3 presents down
				if (type == 3){
					for (short j=0; j<lDes; j++){
						if(rRow+j<row-1){
							if(occ[rRow+j][rCol]==0 && obsType % 2 == 0){
								occ[rRow+j][rCol] = 3;
								occ[rRow+j][rCol+1] = 4;
								currentLength++;
							}else if(occ[rRow+j][rCol]==0 && obsType % 2 == 1){
								occ[rRow+j][rCol] = 9;
								occ[rRow+j][rCol+1] = 10;
								currentLength++;
							} else lDes++;
						}else{
							break;
						}
					}
				}
				// 4 presents left-up
				if (type == 4){
					for (short j=0; j<lDes; j++){
						if(rCol-j*2>0 && rRow-j>0){
							if(occ[rRow-j][rCol-j*2]==0 && obsType % 2 == 0){
								occ[rRow-j][rCol-j*2] = 3;
								occ[rRow-j][rCol+1-j*2] = 4;
								currentLength++;
							}else if(occ[rRow-j][rCol-j*2]==0 && obsType % 2 == 1){
								occ[rRow-j][rCol-j*2] = 9;
								occ[rRow-j][rCol+1-j*2] = 10;
								currentLength++;
							} else lDes++;
						}else{
							break;
						}
					}
				}
				// 5 presents left-down
				if (type == 5){
					for (short j=0; j<lDes; j++){
						if(rCol-j*2>0 && rRow+j<row-1){
							if(occ[rRow+j][rCol-j*2]==0 && obsType % 2 == 0){
								occ[rRow+j][rCol-j*2] = 3;
								occ[rRow+j][rCol+1-j*2] = 4;
								currentLength++;
							}else if(occ[rRow+j][rCol-j*2]==0 && obsType % 2 == 1){
								occ[rRow+j][rCol-j*2] = 9;
								occ[rRow+j][rCol+1-j*2] = 10;
								currentLength++;
							} else lDes++;
						}else{
							break;
						}
					}
				}
			}
			arrayDes[i] = new Descriptor(rRow, rCol, currentLength, type, obsType);
			obsLeft -= currentLength;
			obsUsed += currentLength;
		}
	}
	/*
	 * 	Generate the length of a single descriptor
	 * 	the length of the descriptor is generated by averaging two random numbers
	 */
	private short lengthDes(short obsLeft){
		Random generator = new Random();
		short l = (short)(generator.nextInt(size-1));
		short h = (short)(generator.nextInt(size-1));
		short result = (short)((l+h)/2+1);
		if(result<=obsLeft)
			return result;
		else
			return obsLeft;
	}
	/*
	 * 	Create the array of Descriptors
	 */
	private void iniPPP(){
		crossover = 0;
		iniOcc();
		iniBoundary();
		iniAgency();
		iniDestination();
		iniDescriptors();
	}
	/*
	 * 	copy the array of Descriptors, given a PPP to create a PPP
	 */
	private void copyDescriptors(){
		iniOcc();
		iniBoundary();
		iniAgency();
		iniDestination();
		updateDescriptors();
	}
	/*
	 * 	return the particular descriptor at given index
	 */
	public Descriptor getDescriptor(int n){
		return arrayDes[n];
	}
	/*
	 * 	set the descriptor at a particular index
	 */
	public void setDescriptor(Descriptor des, int n){
		arrayDes[n] = des;
	}
	/*
	 * 	Display the following information on the console:
	 * 	the map and the final result of PPP
	 */
	public void displayPPP(){
		//displayOcc();
		displayMap();
		displayFinal();
	}
	/*
	 * 	Print out the array of descriptors on Console
	 */
	public void displayDes(){
		System.out.println("The descriptors are showing below:");
		for(short i=0; i<nDes; i++){
			System.out.print(arrayDes[i]);
			if(i!=(short)(nDes-1)){
				System.out.print(", ");
			}
		}
		System.out.println();
	}
	/*
	 * 	Display the following information on the console:
	 * 	the map, the space left, the size of the array of the agent state,
	 * 	all the possible state values and the final result of PPP.
	 */
	/*public void displayPPPwithInfo(){
		displayMap();
		System.out.println("Space left is "+nonOcc());
		System.out.println("The number of obstructions are "+ occ());
		System.out.println("The length of the array is "+ asArray.length);
		displayStateValue();
		displayFinal();
	}*/
	/*
	 * 	Display the map
	 */
	public void displayMap(){
		for (short i = 0; i<row; i++){
			for (short j = 0; j<col; j++){
				System.out.print(map[i][j]);
			}
			System.out.println();
		}
		System.out.printf("%d x %d squares \n", this.size*2, this.size);
	}
	
	public boolean isOccupied(int x, int y, boolean move){ //Determine whether a point in the PPP is occupied for moving and sensing purposes
		short o = this.occ[y][x];
		switch (o){
		case 1:
		case 3:
		case 4:
			return true;
		case 9:
		case 10:
			return move;
		default:
			return false;
		}
	}
	
	public short getOccCell(int x, int y){
		return occ[y][x];
	}
	
	public void displayOcc(){
		for (int i = 0; i<row; i++){
			for(int j=0; j<col; j++){
				System.out.print(occ[i][j]);
			}
			System.out.println();
		}
		
	}
	
	/*
	 * 	Get the number of non-occupied cells
	 */
	private short nonOcc(){
		short result = 0;
		for(short i = 0; i<row; i++){
			for(short j = 0; j<col; j++){
				if(occ[i][j]==0||occ[i][j]==2||occ[i][j]==12) result++;
			}
		}
		return (short)(result/2);
	}
	/*
	 * 	Get the number of occupied cells
	 */
	public short occ(){
		return (short)(size*size - nonOcc());
	}
	/*
	 * 	Initialize the AgentState Array
	 * 	Output: After the initialization the asArray contains all possible triples
	 * 			that the agent can occupy, and the initial value for each state
	 * 			is (max, max, max) for all possible states except the agent's starting
	 * 			state, set to (0,0,r), which has an initial values of (0,0,0)
	 * 	Tips: j = j + 2 and (j-1)/2 is because two columns represents one space
	 * 		  ((j-1)/2,i-1) is because the array contains boundaries for the map,
	 * 		  and x is the column and y is the row
	 */
	private void iniAgentState(){
		asSize = (short)(nonOcc()*4);
		//System.out.println(occ());
		//System.out.println(arraySize);
		short n = 0;
		asArray = new AgentState[asSize];
		//for each column
		for(short i = 0; i<size; i++){
			//for each row
			for(short j = 0; j<size; j++){
				//For each free grid sq. in Occ grid:
				if(occ[j+1][i*2+1]==0||occ[j+1][i*2+1]==2||occ[j+1][i*2+1]==12){
					asArray[n*4] = new AgentState((short)i,(short)j,'r');
					asArray[n*4+1] = new AgentState((short)i,(short)j,'u');
					asArray[n*4+2] = new AgentState((short)i,(short)j,'d');
					asArray[n*4+3] = new AgentState((short)i,(short)j,'l');
					n++;
				}
			}
		}
		//System.out.println(n);
		asArray[0].setStateValue((short)0, (short)0, (short)0);
	}

	/*
	 * 	Help function for determine whether the position is a possible position
	 * 	Whether the tested AgentState is in the asArray
	 * 	If the agentState is in the array return the position, if not return -1
	 */
	private short arrayContain(AgentState agentState){
		for(short i = 0; i<asSize; i++){
			//System.out.print("asSize =");
			//System.out.println(asSize);
			//System.out.println(i);
			//System.out.println(asArray[i]);
			//if(asArray[i]== null){
			//	System.out.println(asArray[i+1]);
			//	System.out.println(asArray[i+2]);
			//}
			if(asArray[i].similar(agentState)) return i;
		}
		return -1;
	}
	/*
	 * 	Compare the values of two agent state
	 * 	And choose the smaller one
	 */
	private AgentState agentSelector(AgentState a, AgentState b){
		if(a.getStateValue().compareSVT(b.getStateValue()))  // ??idk??
			return a;
		else return b;
	}
	/*
	 * 	To check whether the two Arrays are the same in values.
	 * 	Notes: When there is no updates for every state value any more.
	 */
	private boolean similarAS(AgentState[] as1, AgentState[] as2){
		boolean result = true;
		for(short i = 0; i<asSize; i++){
			result = result && as1[i].getStateValue().sameSV(as2[i].getStateValue());
		}
		return result;
	}
	/*
	 * 	Display the smallest state value of every positions in the asArray
	 * 	Note: different directions in the same position are considered different.
	 */
	/*private void displayStateValue(){
		for(short i = 0; i<arraySize; i++){
			System.out.print("The position is " + asArray[i] + " and its value is ");
			System.out.println(asArray[i].getStateValue());
		}
	}*/
	/*
	 * 	Display the final position with direction, 
	 * 	and the smallest state value at destination
	 */
	public void displayFinal(){
		if(availability){
			System.out.println("The final agent state is " + finalAgentState());
			System.out.println("The smallest state value is " + bestSV);
			System.out.println("\nVisibility");
			System.out.printf("From Goal: %d/%d = %.2f\n", this.goalVisibleCells, this.totalCells, this.goalVisiblePercentage);
			System.out.printf("From Start: %d/%d = %.2f\n", this.startVisibleCells, this.totalCells, this.startVisiblePercentage);
			System.out.printf("From TopR: %d/%d = %.2f\n", this.topRightVisibleCells, this.totalCells, this.topRightVisiblePercentage);
			System.out.printf("From BLeft: %d/%d = %.2f\n", this.bottomLeftVisibleCells, this.totalCells, this.bottomLeftVisiblePercentage);
			System.out.printf("From Centre: %d/%d = %.2f\n", this.centreVisibleCells, this.totalCells, this.centreVisiblePercentage);
			System.out.printf("Magnitude: %.2f\n", this.visibilityMagnitude);
			System.out.println("\nWeighted Sum");
			//System.out.printf("Magnitude: %.2f\n", this.visibilityMagnitude);
			System.out.printf("Vis Sum: %.2f\n", this.sumVis);
			System.out.printf("Obstacle use: %.2f ==> (1- %.2f)= %.2f\n", this.obstaclesUsePercentage, this.obstaclesUsePercentage, 2*(1-this.obstaclesUsePercentage));
			int turns = this.bestSV.getTurn();
			System.out.printf("Turns: %d\n", turns);
			System.out.printf("Total: %.2f\n", this.visibilityWeighted);
			System.out.printf("Reachable cells %d\n", this.reachableCells);
			System.out.printf("Unreachable Cells %d\n of which obstacles: %d\n", this.unreachableCells, 2*this.obsUsed);
			System.out.printf(" Ratio %.2f:1\n", this.reachableRatio);
			System.out.printf("Max open width: %d, Avg Max Open: %.2f\n", this.maxOpenWidth, this.avgOpenWidth);
			System.out.printf("Max open height: %d, Avg Max Open: %.2f\n", this.maxOpenHeight, this.avgOpenHeight);
			System.out.println("");
		} else
			System.out.println("The destination is unreachable!");
	}
	/*
	 * 	check the availability of this PPP
	 */
	private void checkPPP(){
		bestSV = bestStateValue();
		if(bestSV.validSV()){
			availability = true;
		} else {
			availability = false;
		}
	}
	/*	
	 * 	Get the availability of this PPP
	 */
	public boolean checkAvailable(){
		return availability;
	}
	/*
	 * 	Return the state with the direction of facing for the agent to have the 
	 * 	smallest value at the destination
	 */
	private AgentState finalAgentState(){
		finalAS = compareAgentState(asArray[asSize-1], asArray[asSize-2],
				asArray[asSize-3], asArray[asSize-4]);
		return finalAS;
	}
	/*
	 * 	Return the smallest state value from the start point to the destination
	 */
	private StateValue bestStateValue(){
		return finalAgentState().getStateValue();
	}
	/*
	 * 	Compare four state values and return the smallest one
	 */
	private AgentState compareAgentState(AgentState as1, AgentState as2,
			AgentState as3, AgentState as4){
		AgentState result;
		if(as1.getStateValue().compareSV(as2.getStateValue()))
			result = as1;
		else
			result = as2;
		if(as3.getStateValue().compareSV(result.getStateValue()))
			result = as3;
		if(as4.getStateValue().compareSV(result.getStateValue()))
			result = as4;
		return result;
	}
	/*
	 * 	mutate the arrayDes
	 */
	public PPP mutatePPP(){
		PPP child = new PPP(this);
		Random generator = new Random();
		boolean reachable = false;
		
		while(!reachable){
			child = new PPP(this);
			int random = generator.nextInt(this.nDes);
			child.arrayDes[random].mutation(this.size);
			child.updatePPP();
			reachable = child.checkAvailable();
		}
		if(reachable) {
			return child;
		} else{
			return this;
		}
	}
	/*
	 * 	Update PPP after mutation and two point crossover
	 */
	public void updatePPP(){
		copyDescriptors();
		evaluatePPP();
	}
	
	public void evaluatePPP(){
		iniAgentState();
		dPA();
		checkPPP();
		this.evaluateDifficulty();
	}
	
	/*
	 * 	Update the descriptors after mutation
	 */
	public void updateDescriptors(){
		short obsLeft = maxObs;
		short lDes;
		for (short i = 0; i<nDes; i++){
			// the x position of the descriptor
			short rRow = arrayDes[i].getX();
			// the y position of the descriptor
			short rCol = arrayDes[i].getY();
			// the type of the descriptor, totally six.
			short type = arrayDes[i].getType();
			//the type of obstacle the descriptor is
			int obsType = arrayDes[i].getObstacle();
			short currentLength = 0;	// the length of the current descriptor
			if(obsLeft>0){
				lDes = arrayDes[i].getLength(); // the maximum number of obstructions for one descriptor
				if(occ[rRow][rCol]==0 && obsType == 0){
					occ[rRow][rCol] = 3;
					occ[rRow][rCol+1] = 4;
					currentLength++;
					lDes--;
				}else{
					occ[rRow][rCol] = 9;
					occ[rRow][rCol+1] = 10;
					currentLength++;
					lDes--;
				}
				// 0 presents right
				if (type == 0){ 
					for (short j=0; j<lDes; j++){
						if(rCol+j*2<col-2){
							if(occ[rRow][rCol+j*2]==0 && obsType == 0){
								occ[rRow][rCol+j*2] = 3;
								occ[rRow][rCol+1+j*2] = 4;
								currentLength++;
							}else if(occ[rRow][rCol+j*2]==0 && obsType == 1){
								occ[rRow][rCol+j*2] = 9;
								occ[rRow][rCol+1+j*2] = 10;
								currentLength++;
							} else {lDes++;}
						}else{
							break;
						}
					}
				}
				// 1 presents left
				if (type == 1){
					for (short j=0; j<lDes; j++){
						if(rCol-j*2>0){
							if(occ[rRow][rCol-j*2]==0 && obsType == 0){
								occ[rRow][rCol-j*2] = 3;
								occ[rRow][rCol+1-j*2] = 4;
								currentLength++;
							}else if(occ[rRow][rCol-j*2]==0 && obsType == 1){
								occ[rRow][rCol-j*2] = 9;
								occ[rRow][rCol+1-j*2] = 10;
								currentLength++;
							} else {lDes++;}
						}else{
							break;
						}
					}
				}
				// 2 presents up
				if (type == 2){
					for (short j=0; j<lDes; j++){
						if(rRow-j>0){
							if(occ[rRow-j][rCol]==0 && obsType == 0){
								occ[rRow-j][rCol] = 3;
								occ[rRow-j][rCol+1] = 4;
								currentLength++;
							} 	else if(occ[rRow-j][rCol]==0 && obsType == 1){
								occ[rRow-j][rCol] = 9;
								occ[rRow-j][rCol+1] = 10;
								currentLength++;
							}else {lDes++;}
						}else{
							break;
						}
					}
				}
				// 3 presents down
				if (type == 3){
					for (short j=0; j<lDes; j++){
						if(rRow+j<row-1){
							if(occ[rRow+j][rCol]==0 && obsType == 0){
								occ[rRow+j][rCol] = 3;
								occ[rRow+j][rCol+1] = 4;
								currentLength++;
							}else if(occ[rRow+j][rCol]==0 && obsType == 1){
								occ[rRow+j][rCol] = 9;
								occ[rRow+j][rCol+1] = 10;
								currentLength++;
							}
							else {lDes++;}
						}else{
							break;
						}
					}
				}
				// 4 presents left-up
				if (type == 4){
					for (short j=0; j<lDes; j++){
						if(rCol-j*2>0 && rRow-j>0){
							if(occ[rRow-j][rCol-j*2]==0 && obsType == 0){
								occ[rRow-j][rCol-j*2] = 3;
								occ[rRow-j][rCol+1-j*2] = 4;
								currentLength++;
							}else if(occ[rRow-j][rCol-j*2]==0 && obsType == 1) {
								occ[rRow - j][rCol - j * 2] = 9;
								occ[rRow - j][rCol + 1 - j * 2] = 10;
								currentLength++;
							}else {lDes++;}
						}else{
							break;
						}
					}
				}
				// 5 presents left-down
				if (type == 5){
					for (short j=0; j<lDes; j++){
						if(rCol-j*2>0 && rRow+j<row-1){
							if(occ[rRow+j][rCol-j*2]==0 && obsType == 0){
								occ[rRow+j][rCol-j*2] = 3;
								occ[rRow+j][rCol+1-j*2] = 4;
								currentLength++;
							}else if(occ[rRow+j][rCol-j*2]==0 && obsType == 1) {
								occ[rRow + j][rCol - j * 2] = 9;
								occ[rRow + j][rCol + 1 - j * 2] = 10;
								currentLength++;
							} else {lDes++;}
						}else{
							break;
						}
					}
				}
			}
			arrayDes[i].setLength(currentLength);
			obsLeft -= currentLength;
			obsUsed += currentLength;
		}
	}
	/*
	 * 	This function is used for creating a valid PPP
	 * 	if the destinatino is unreachable, the PPP will redo everything again
	 * 	to generate a reachable PPP
	 */
	private void createPPP(){
		while (!this.availability){
			iniPPP();
			iniAgentState();
			dPA();
			checkPPP();
		}
		evaluateDifficulty();
	}
	/*
	 * 	This function is used for copy the PPP
	 */
	private void copyPPP(){
		copyDescriptors();
		iniAgentState();
		dPA();
		checkPPP();
		evaluateDifficulty();
	}
	/*
	 * 	Get the value of turn for this PPP
	 */
	public short getTurn(){
		if(!bestSV.validSV()){
			return (short) 0;
		}
		return bestSV.getTurn();
	}
	/*
	 * 	Get the value of advance for this PPP
	 */
	public short getAdvance(){
		return bestSV.getAdvance();
	}
	/*
	 * 	Get the value of move for this PPP
	 */
	public short getMove(){
		return bestSV.getMove();
	}
	/*
	 * 	check whether two PPPs are same in value or not
	 */

	public short getFlushWalls(){ //This method counts the number of descriptors that are adjacent to a boundary wall and run parallel to it
		short flushWalls = 0;
		for (short i = 0; i<nDes;i++){
			if(arrayDes[i].getY() == 1 && arrayDes[i].getType() == 2 || arrayDes[i].getY() == 1 && arrayDes[i].getType() == 3){
				flushWalls += (arrayDes[i].getLength())^2;
			} else if (arrayDes[i].getY() == 39 && arrayDes[i].getType() == 2 || arrayDes[i].getY() == 39 && arrayDes[i].getType() == 3){
				flushWalls += (arrayDes[i].getLength())^2;
			}
			if(arrayDes[i].getX() == 1 && arrayDes[i].getType() == 0 || arrayDes[i].getX() == 1 && arrayDes[i].getType() == 1){
				flushWalls += (arrayDes[i].getLength())^2;
			} else if (arrayDes[i].getX() == 20 && arrayDes[i].getType() == 0 || arrayDes[i].getX() == 20 && arrayDes[i].getType() == 1){
				flushWalls += (arrayDes[i].getLength())^2;
			}
		}
		return flushWalls;
	}

	public short getCrossover(){
		return crossover;
	}

	public boolean samePPP(PPP target){
		boolean result = true;
		for (short i = 0; i<row; i++){
			for (short j = 0; j<col; j++){
				if(occ[i][j]!=target.occ[i][j]){
					result = false;
				}
			}
		}
		return result;
	}
	
	/*
	 * 	write the PPP to a file, named PPP
	 */
	public void writePPP(int number){
		this.writePPP("", number);
	}
	
	public void writePPP (String folder, int number) {
		Writer writer = null;

		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(folder+"\\PPP"+number+".ppp"), "utf-8"));
		    writer.write("PPP\n");
		    writer.write("#.\n");
		    writer.write((size+2)+" "+(size+2)+" "+(maxObs)+ "\n");
		    writer.write(bestStateValue().getMove()+"\n");
		    
		    for(short i=0; i<nDes; i++){
				writer.write(this.arrayDes[i].write());
				if(i!=(short)(nDes-1)){
					writer.write(", ");
				}
			}
		    writer.write("\n");
		    
		    for (short i = 0; i<row; i++){
				for (short j = 0; j<col; j++){
					// Boundary wall
					if(occ[i][j]==1){
						writer.write("#");
//						if(j!=0&j!=col-1){
//							j++;
//						}
					}
					//Empty space or goal / initial position
					if(occ[i][j]==0||occ[i][j]==2||occ[i][j]==12){
						writer.write(".");
//						j++;
					}
					// Left side of obstacle
					if(occ[i][j]==3||occ[i][j]==4){
						writer.write("#");
//						j++;
					}
					if(occ[i][j]==9||occ[i][j]==10){
						writer.write("A");
//						j++;
					}
				}
				writer.write("\n");
			}
		} catch (IOException ex){
			System.err.println("Folder :" + folder + ", File " + "/PPP"+number+".ppp");
			ex.printStackTrace();
			System.exit(1);
		} finally {
		   try {writer.close();} catch (Exception ex) {}
		}
	}
	
	public short[][] getOccGrid(){
		return this.occ;
	}
	
	public void setOccGrid(short[][] occ){
		this.occ = occ;
	}
	
	/**
	 * Evaluation
	 */
	
	/*
	 * Dynamic Programming Algorithm
	 */
	private void dPA(){
		singleDPA();
		AgentState[] temp = new AgentState[asSize];
		do{
			System.arraycopy(asArray, 0, temp, 0, asArray.length);
			singleDPA();
		} while (!similarAS(asArray, temp));
	}
	
	/*
	 * 	one loop for the Dynamic Programming Algorithm
	 */
	private void singleDPA(){
		AgentState r, l, a;	// stands for right, left, advance
		short n;			// the current position of the replacement
		for(short i = 0; i<asSize; i++){
			AgentState temp = new AgentState(asArray[i]);
			r = temp.turnRight();
			n = arrayContain(r);
			if(n!=-1){
				asArray[n] = agentSelector(asArray[n], r);
			}
			temp = new AgentState(asArray[i]);
			l = temp.turnLeft();
			n = arrayContain(l);
			if(n!=-1){
				asArray[n] = agentSelector(asArray[n], l);
			}
			temp = new AgentState(asArray[i]);
			a = temp.advance();
			n = arrayContain(a);
			if(n!=-1){
				asArray[n] = agentSelector(asArray[n], a);
			}
		}
	}
	
	public void evaluateDifficulty(){
		this.goalVisibleCells       = this.evaluateVisibilityFromPosition(col-2, row-2, true);
		this.startVisibleCells      = this.evaluateVisibilityFromPosition(1, 1, false);
		this.centreVisibleCells     = this.evaluateVisibilityFromPosition(size, size/2, false);
		this.topRightVisibleCells   = this.evaluateVisibilityFromPosition(col-2, 1, false);
		this.bottomLeftVisibleCells = this.evaluateVisibilityFromPosition(1, row-2, false);
		
		this.goalVisiblePercentage  = this.goalVisibleCells / (double)this.totalCells;
		this.startVisiblePercentage = this.startVisibleCells / (double)this.totalCells;
		this.centreVisiblePercentage =  this.centreVisibleCells / (double)this.totalCells;
		this.topRightVisiblePercentage = this.topRightVisibleCells / (double)this.totalCells;
		this.bottomLeftVisiblePercentage = this.bottomLeftVisibleCells / (double)this.totalCells;
		double[] visLst = {this.goalVisiblePercentage, this.startVisiblePercentage, this.centreVisiblePercentage,
							this.topRightVisiblePercentage, this.bottomLeftVisiblePercentage};
		double sumSq = 0.0;
		for (double d : visLst){
			sumSq += Math.pow(d, 2);
		}
		this.visibilityMagnitude = Math.sqrt(sumSq);
		
		this.obsUsed = 0;
		for (short[] row : this.occ){
			for (short cell : row){
				if ((cell == 3)||(cell == 9)){
					this.obsUsed++;
				}
			}
		}
		
		if (this.maxObs > 0){
			this.obstaclesUsePercentage = (float)this.obsUsed / (float)this.maxObs;
		} else {
			this.obstaclesUsePercentage = 1;
		}
		
		//Prefer to minimise this score
		double visSum = 0.0;
		double visSumInverted = 0.0;
		//Increase important of start, goal by higher weight
		double[] visSumLst = {2.5*this.goalVisiblePercentage, 1.5*this.startVisiblePercentage, this.centreVisiblePercentage,
				this.topRightVisiblePercentage, this.bottomLeftVisiblePercentage};
		for (double d: visSumLst){
			visSum += d;
			visSumInverted += (1-d);
		}
		this.sumVis = visSum;
		this.visibilityWeighted = visSum;
		// Penalise lack of obstacles
		this.visibilityWeighted += 2*(1-this.obstaclesUsePercentage);
		
		Memory mem = new Memory(this);
		this.reachableCells = mem.checkReachability(this.occ);
		this.unreachableCells = this.totalCells-this.reachableCells;
		//Double obsUsed as that is the count of '3' in Occ, ie. the left peice of an obstacle
		//But our reachability is counting occ as double width, so we need to account for the '4' right hand occ.
		double unreachable = this.unreachableCells-(2*this.obsUsed);
		if (unreachable == 0){
			this.reachablePercent = 1;
		} else {
			this.reachablePercent = (float)this.reachableCells/(this.totalCells-(2*this.obsUsed));
		}
		this.reachableRatio = (float)this.reachableCells/(float)(unreachable);
		
		
		//try encouraging visMag around 0.6
//		double goal = 0.6;
//		double visMagDiff = Math.abs(this.visibilityMagnitude-goal);
////		//penalise visMag far from 0.6
////		if (visMagDiff > 0.3){
////			this.visibilityWeighted += 2;
////		}
		//Try encouraging more than 1 turn
		if (this.bestSV.getTurn() == 1){
			this.visibilityWeighted += 2;
		}
		
//		//measure visibility from more points across map
//		int width = this.size*2;
//		int[] xPoints = {width/4, width/2, 3*width/4};
//		int[] yPoints = {size/4,  size/2, 3*size/4};
//		int[] xWeights = {1,1,1};
//		int[] yWeights = {1,1,1};
//		double total = 0.0;
//		for(int yIndex = 0; yIndex < yPoints.length; yIndex++){
//			for(int xIndex=0; xIndex < xPoints.length; xIndex++){
//				int x = xPoints[xIndex];
//				int y = yPoints[yIndex];
//				double cellsVisible = this.evaluateVisibilityFromPosition(x, y, false);
//				double score = cellsVisible / (double)this.totalCells;
//				total += score*xWeights[xIndex]*yWeights[yIndex];
//			}
//		}
//		this.visibilityWeighted += total;
		
		this.maxOpenWidth = 0;
		this.avgOpenWidth = 0.0;
		for (int y = 1; y < this.occ.length-1; y++){
			int open = 0;
			int rowMax = 0;
			for(int x = 1; x < this.occ[0].length; x++){
				short ob = this.occ[y][x];
				if ((ob == 3)||(ob == 1) || (ob==4) || (ob==9) || (ob==10)){
					//obstacle, run broken
					this.maxOpenWidth = this.maxOpenWidth < open ? open : this.maxOpenWidth;
					rowMax = open > rowMax ? open : rowMax;
					open = 0;
				} else {
					open++;
				}
			}
			this.avgOpenWidth += rowMax;
		}
		//avg over rows
		this.avgOpenWidth = this.avgOpenWidth / (this.occ.length-2);
		//Represent as a % of max possible width
		this.avgOpenCellsW = this.avgOpenWidth;
		this.avgOpenWidth = this.avgOpenWidth / (this.size*2);
		this.visibilityWeighted += 2*this.avgOpenWidth;
		
		this.maxOpenHeight = 0;
		this.avgOpenHeight = 0.0;
		for (int x = 1; x < this.occ[0].length-1; x++){
			int open = 0;
			int colMax = 0;
			for (int y = 1; y < this.occ.length; y++){
				short ob = this.occ[y][x];
				if ((ob == 3)||(ob == 1) || (ob==4) || (ob==9) || (ob==10)){
					this.maxOpenHeight = this.maxOpenHeight < open ? open : this.maxOpenHeight;
					colMax = open > colMax ? open : colMax;
					open = 0;
				} else {
					open++;
				}
			}
			this.avgOpenHeight += colMax;
		}
		this.avgOpenCellsV = this.avgOpenHeight;
		this.avgOpenHeight = this.avgOpenHeight / (2*this.occ.length);
		//Represent as a % of max possible height
		this.avgOpenHeight = this.avgOpenHeight / (this.size);
		this.visibilityWeighted += 2*this.avgOpenHeight;
	}
	
	/**
	 * Evaluate the visibility of the goal position assuming a perfect sensor
	 * I.e. no noise and with a range covering the map
	 * We calculate this by looking out from the position specified;
	 * Cells visible from the pos have visibility onto that pos .
	 * This is less intensive than checking visibilty for every possible position.
	 * @return count of cells from which the pos is visible
	 */
	private int evaluateVisibilityFromPosition(int xPos, int yPos, boolean showOnMap){
		//calculate the number of cells from which the goal position is visible
		int range = (this.size*2)+2;
		Sensor sensor = new Sensor(range);
		int[] senseWindow = sensor.boundSenseWindow(xPos, yPos, this.size);
		int x_left   = senseWindow[0];
		int x_right  = senseWindow[1];
		int y_top    = senseWindow[2];
		int y_bottom = senseWindow[3];
		
		HashSet<List<Short>> visibleCells = new HashSet<List<Short>>();
		
		for (int y=y_top; y<=y_bottom; y++){
			int[] endPoints = Sensor.getEndPoints(x_left, x_right);
			for (int x: endPoints){
				//Line from goalPos to cell (endX, Y)
				//Scan along this line to determine number of cells which can see the goal
				ArrayList<short[]> LoS = Sensor.line(xPos, yPos, x, y);
				for (short[] p: LoS){
//					if((p[0]==xPos)&&(p[1]==yPos)){
//						continue;
//					}
					if(this.isOccupied(p[0], p[1], false)){
						//Can't sense from, or from behind, an occupied cell.
						//The rest of the line has it's visibilty blocked by this cell,
						//so skip to next LoS
						break;
					} else {
						visibleCells.add(Arrays.asList(p[0], p[1]));
						if (showOnMap){
							map[p[1]][p[0]] = 'v';
						}
					}
				}
			}
		}
		return visibleCells.size();
	}
	
	public double getGoalVisibility(){
		return this.goalVisiblePercentage;
	}
	public int getGoalVisibleCells(){return this.goalVisibleCells;}
	public int getStartVisibleCells(){return this.startVisibleCells;}
	public int getCentreVisibleCells(){return this.centreVisibleCells;}
	public int getTRVisibleCells(){return this.topRightVisibleCells;}
	public int getBLVisibleCells(){return this.bottomLeftVisibleCells;}
	public double getAvgOpenCellsW(){return this.avgOpenCellsW;}
	public double getAvgOpenCellsV(){return this.avgOpenCellsV;}
	
	public double getCentreVisibility(){
		return this.centreVisiblePercentage;
	}
	
	public double getTopRightVisibility(){
		return this.topRightVisiblePercentage;
	}
	
	public double getBottomLeftVisiblity(){
		return this.bottomLeftVisiblePercentage;
	}
	
	public double getStartVisibility(){
		return this.startVisiblePercentage;
	}
	
	public double getVisibilityMangitude(){
		return this.visibilityMagnitude;
	}
	
	public double getVisibilityWeightedSum(){
		return this.visibilityWeighted;
	}
	
	public double getObstacleUse(){
		return this.obstaclesUsePercentage;
	}

	public short getnDes(){
		return nDes;
	}
	
	public int getUnreachableCells(){
		return this.unreachableCells-(2*this.obsUsed);
	}
	
	public int getOpenW(){
		return this.maxOpenWidth;
	}
	
	public int getOpenH(){
		return this.maxOpenHeight;
	}
	
	public double getAvgOpenW(){
		return this.avgOpenWidth;
	}
	
	public double getReachablePercent(){
		return this.reachablePercent;
	}
	
	public double getAvgOpenH(){
		return this.avgOpenHeight;
	}
	
	public int getObsUsed(){
		return this.obsUsed;
	}

}
