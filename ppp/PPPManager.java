package ppp;
import java.io.File;
import java.util.Random;
/*
 * 	Author:	Hao Wei
 * 	Time:	05/06/2013
 * 	Purpose: To manage a large number of PPPs, and do the evolutionary algorithm
 * 	Note:	1. For tournament selection, the seven chromosomes are randomly selected
 * 			2. Two point crossover has finished and tested.
 * 			3. tourOcc is a very important function!
 * 	Next:	tournament selection.
 */
public class PPPManager {
	public PPP[] population;	// the array used to store the PPPs population
	private PPP[] tournament;	// the array used to store the PPPs for the tournament
	private PPP[] P1C;			// the array used for comparing Parent 1 with its most similar child in Crowding
	private PPP[] P2C;			// the array used for comparing Parent 2 with its most similar child in Crowding
	private short sizePopu;		// the size of population
	private short sizeTour;		// the size of tournament
	private short sizePPP;		// the size of the PPP
	private short maxObs;		// the maximum number of obstructions allowed in a single PPP
	private short nDes;			// the number of descriptors
	private short[] selected;	// Index into Population of PPPs selected for tournament
	private short[] tourOcc;	// if selected, set to 1, prevent one PPP to be selected twice
	private float avgTurn = 0;	// average number of turns in the population - updates every 100 mating events
	private int mutChance = 12; // Chance of Mutation - updates every 100 mating events
	/*
	 * 	The constructor for PPPManager
	 */
	public PPPManager(short sizePPP, short nDes, short maxObs){
		this.sizePPP = sizePPP;
		this.nDes = nDes;
		this.maxObs = maxObs;
		sizePopu = 60;				// PPPs operated on the population of size 60
		sizeTour = 7;				// steady-state size-seven tournament
		population = new PPP[sizePopu];	
		iniPopulation();
	}
	/*
	 * 	Initialize the population
	 */
	private void iniPopulation(){
		System.out.printf("Initializing tournament population of size %d\n", this.sizePopu);
		for(short i=0; i<sizePopu; i++){
			population[i] = new PPP(sizePPP, nDes, maxObs);
		}
		System.out.println("Done");
	}
	
	/*
	 * 	Select seven random chromosomes from the population
	 */
	private void selectChromosones(short n){ //Creates a list of PPPs of length n
		//n= sizeTour
		//System.out.println("select tour");
		Random generator = new Random();	// for generating random numbers
		tournament = new PPP[n];			// array of PPPs for storing copies of the tournament selected PPPs
		selected = new short[n];			// array of population indexes for the PPPs entered into the tournament
		tourOcc = new short[n];				// array of 0s for all PPPs to avoid multiple selection
		for(short i=0; i<n; i++){
			short random;					//initialise random number variable
			do{
				random = (short)generator.nextInt(sizePopu);  	// Pick a number smaller than the size of the population
			}while(selectedChromo(random, i));					// Check if the chromosome has been collected before
			selected[i] = random;								// record index of randomly selected PPP
			tournament[i] = new PPP(population[random]);		// add PPP to the tournament
			tourOcc[i] = (short)0;								// signify that the PPP hasn't been selected as most or least fit
		}
	}
	/*
	 * 	This is a testing class
	 * 	show the selected chromosomes on console
	 */
	public void showChromo(){
		for(short i = 0; i<7; i++){
			System.out.print(selected[i]+" ");
		}
		System.out.println();
	}
	/*
	 * 	check if a particular chromosome has been already selected
	 */
	private boolean selectedChromo(short n, short index){
		//System.out.println("selected chromo");
		boolean result = false;
		if(index==0)
			return result;
		for(short i=0; i<index; i++){
			if(selected[i]==n)
				result = true;
		}
		return result;
	}
	/*
		 * 	Two point crossover, from 0 to length-1
	 */
	private PairPPP twoCrossover(PPP parent1, PPP parent2){
		boolean both_reachable = false;
		PPP child1 = new PPP(parent1);
		PPP child2 = new PPP(parent2);
		Random generator = new Random();
		short p1, p2;
		int count = 0;
		while (!both_reachable){				// While at least one isn't possible
			count ++;

			p2 = (short)generator.nextInt(nDes);		// Pick the Second Point of Crossover
			p1 = (short)generator.nextInt(p2+1);	// Pick the First Point of Crossover so that it has an index lower than the second one
			for(short i=p1;i<=p2;i++){					// Swap each gene between crossover points (inclusive at both ends)
				Descriptor temp = child1.getDescriptor(i);
				child1.setDescriptor(child2.getDescriptor(i), i);
				child2.setDescriptor(temp, i);
			}
			child1.updatePPP();							// Update the occupancy array to reflect crossover
			child2.updatePPP();
			if (count > 100){
				System.err.println("\nFailing Crossover"); // If you can't have two valid children within 100 tries, give up
				return new PairPPP(parent1, parent2);
			}
			if((child1.checkAvailable()) && (child2.checkAvailable())){
				both_reachable = true;
			}
		}
		return new PairPPP(child1, child2);
	}
	
	/*
	 *  Find the PPP which has the highest turns from the tournament selection
	 */
	private short maxTurns(){
		short tempTurn = 0;	// current max turn
		short index = 0;		// the index for current max turn
		short turn;			// current turn
		for(short i=0; i<sizeTour; i++){
			turn = population[selected[i]].getTurn();
			if(tourOcc[i]==0){
				if(tempTurn<turn){
					index = i;
					tempTurn = turn;
				}
			}
		}
		tourOcc[index] = 1;
		return index;
	}

	/*
	 * 	Find the PPP which has the lowest turns from the Tournament selection
	 */
	private short minTurns(){
		short tempTurn = 1000;	// current max turn
		short index = 0;		// the index for current max turn
		short turn;			// current turn
		for(short i=0; i<sizeTour; i++){
			turn = population[selected[i]].getTurn();
			if(tourOcc[i]==0){
				if(tempTurn>turn){
					index = i;
					tempTurn = turn;
				}
			}
		}
		tourOcc[index] = 1;
		return index;	
	}
	
	private short minGoalVisibility(){
		double min = 999999;
		int index = 0;
		for(int i = 0; i<this.sizeTour; i++){
			if(tourOcc[i]==0){
				double vis = population[selected[i]].getGoalVisibility();
				if (vis < min) {
					index = i;
					min = vis;
				}
			}
		}
		tourOcc[index] = 1;
		return (short) index;
	}
	
	private short maxGoalVisibility(){
		double max = -999999;
		int index = 0;
		for(int i = 0; i<this.sizeTour; i++){
			if(tourOcc[i]==0){
				double vis = population[selected[i]].getGoalVisibility();
				if (vis > max) {
					index = i;
					max = vis;
				}
			}
		}
		tourOcc[index] = 1;
		return (short) index;
	}
	
	private short maxVisibilityMagnitude(){
		double max = -999999;
		int index = 0;
		for(int i = 0; i<this.sizeTour; i++){
			if(tourOcc[i]==0){
				double vis = population[selected[i]].getVisibilityMangitude();
				if (vis > max) {
					index = i;
					max = vis;
				}
			}
		}
		tourOcc[index] = 1;
		return (short) index;
	}
	
	private short minVisibilityMagnitude(){
		double min = 999999;
		int index = 0;
		for(int i = 0; i<this.sizeTour; i++){
			if(tourOcc[i]==0){
				double vis = population[selected[i]].getVisibilityMangitude();
				if (vis < min) {
					index = i;
					min = vis;
				}
			}
		}
		tourOcc[index] = 1;
		return (short) index;
	}
	
	private short minVisibilityWeightedSum(){
		double min = 999999;
		int index = 0;
		for(int i = 0; i<this.sizeTour; i++){
			if(tourOcc[i]==0){
				double vis = population[selected[i]].getVisibilityWeightedSum();
				if (vis < min) {
					index = i;
					min = vis;
				}
			}
		}
		tourOcc[index] = 1;
		return (short) index;
	}
	
	private short maxVisibilityWeightedSum(){
		double max = -999999;
		int index = 0;
		for(int i = 0; i<this.sizeTour; i++){
			if(tourOcc[i]==0){
				double vis = population[selected[i]].getVisibilityWeightedSum();
				if (vis > max) {
					index = i;
					max = vis;
				}
			}
		}
		tourOcc[index] = 1;
		return (short) index;
	}

	private short maxFlushWalls(){
		short tempWalls = 0;	// current max flush walls
		short index = 0;		// the index for current max flush walls
		short walls;			// current flush walls
		for(short i=0; i<sizeTour; i++){
			walls = population[selected[i]].getFlushWalls();
			if(tourOcc[i]==0){
				if(tempWalls<walls){
					index = i;
					tempWalls = walls;
				}
			}
		}
		tourOcc[index] = 1;
		return index;
	}

	private short minFlushWalls(){
		short tempWalls = 999;	// current min flush walls
		short index = 0;		// the index for current min flush walls
		short walls;			// current flush walls
		for(short i=0; i<sizeTour; i++){
			walls = population[selected[i]].getFlushWalls();
			if(tourOcc[i]==0){
				if(tempWalls>walls){
					index = i;
					tempWalls = walls;
				}
			}
		}
		tourOcc[index] = 1;
		return index;
	}
	
	public void evaluatePPPs(){
		for(int i = 0; i < this.sizePopu; i++){
			this.population[i].evaluateDifficulty();
		}
	}
	
	/*
	 * Two most fit PPPs in Tournament
	 */
	private short[] mostFit(){
		short most1, most2;
		//most1 = this.maxTurns();
		//most2 = this.maxTurns();
		most1 = this.minVisibilityWeightedSum();
		most2 = this.minVisibilityWeightedSum();
		return new short[] {most1, most2};
	}
	
	/*
	 * Two least fit PPPs in Tournament
	 */
	private short[] leastFit() {
		short least1, least2;
		//least1 = this.minTurns();
		//least2 = this.minTurns();
		least1 = this.maxVisibilityWeightedSum();
		least2 = this.maxVisibilityWeightedSum();
		return new short[]{least1, least2};
	}

	private short[] newLeastFit(){
		short least1, least2;
		short temp;
		least1 = this.maxFlushWalls();
		if(least1 == this.minGoalVisibility()){
			temp = least1;
			least1 = this.maxGoalVisibility();
			tourOcc[temp] = 0;
		}
		if (least1 == this.minVisibilityMagnitude()){
			temp = least1;
			least1 = this.maxVisibilityMagnitude();
			tourOcc[temp] = 0;
		}
		if (least1 == this.maxTurns()){
			temp = least1;
			least1 = this.minTurns();
			tourOcc[temp] = 0;
		}
		least2 = this.maxFlushWalls();
		if(least2 == this.minGoalVisibility()){
			temp = least2;
			least1 = this.maxGoalVisibility();
			tourOcc[temp] = 0;
		}
		if (least2 == this.minVisibilityMagnitude()){
			temp = least2;
			least2 = this.maxVisibilityMagnitude();
			tourOcc[temp] = 0;
		}
		if (least2 == this.maxTurns()){
			temp = least2;
			least2 = this.minTurns();
			tourOcc[temp] = 0;
		}
		return new short[]{least1, least2};
	}

	private short[] newMostFit(){
		short most1, most2;
		short temp;
		most1 = this.minFlushWalls();
		if(most1 == this.maxGoalVisibility()){
			temp = most1;
			most1 = this.minGoalVisibility();
			tourOcc[temp] = 0;
		}
		if (most1 == this.maxVisibilityMagnitude()){
			temp = most1;
			most1 = this.minVisibilityMagnitude();
			tourOcc[temp] = 0;
		}
		if (most1 == this.minTurns()){
			temp = most1;
			most1 = this.maxTurns();
			tourOcc[temp] = 0;
		}
		most2 = this.minFlushWalls();
		if(most2 == this.maxGoalVisibility()){
			temp = most2;
			most2 = this.minGoalVisibility();
			tourOcc[temp] = 0;
		}
		if (most2 == this.maxVisibilityMagnitude()){
			temp = most2;
			most2 = this.minVisibilityMagnitude();
			tourOcc[temp] = 0;
		}
		if (most2 == this.minTurns()){
			temp = most2;
			most2 = this.maxTurns();
			tourOcc[temp] = 0;
		}
		return new short[]{most1, most2};
	}



	public void checkPopReachable(){
		for (int j = 0; j < this.population.length; j++){
			if (!this.population[j].checkAvailable()){
				System.err.println("Unreachable PPP!");
				this.population[j].drawMap();
				this.population[j].displayMap();
			}
		}
	}
	/*
	 * 	evolution on the seven tournament
	 */
	private void matingEvent(short i){
		this.checkPopReachable();
		short[] most = new short[0];
		short[] least;
		int similarity = 1;
		int count = 0;
		double threshold = 0.5;
		while(similarity>threshold){
			count++;
			if (count > 49) {
				threshold += 0.02;
				count = 0;
			}
			selectChromosones(sizeTour);
			most = this.mostFit();
			similarity = getSimilarity(population[selected[most[0]]],population[selected[most[1]]]);
		}
		PairPPP temp = twoCrossover(tournament[most[0]],tournament[most[1]]);
		/** To avoid premature convergence, the children are compared to see which parent they are most similar to,
		 * each child is then compared to their similar parent, and the stronger of the parent or child survives
		 **/
		if(temp.getP1().getCrossover() < temp.getP1().getnDes()/2){
			if(temp.getP1().getTurn() > tournament[most[0]].getTurn()){
				population[selected[most[0]]] = new PPP(temp.getP1());
			}
			if(temp.getP2().getTurn() > tournament[most[1]].getTurn()) {
				population[selected[most[1]]] = new PPP(temp.getP2());
			}
		} else {
			if(temp.getP2().getTurn() > tournament[most[0]].getTurn()){
				population[selected[most[0]]] = new PPP(temp.getP2());
			}
			if(temp.getP1().getTurn() > tournament[most[1]].getTurn()) {
				population[selected[most[1]]] = new PPP(temp.getP1());
			}
		}
		//population[selected[least[0]]] = new PPP(temp.getP1());
		//population[selected[least[1]]] = new PPP(temp.getP2());
		/** To avoid premature convergence, you can replace one or both mutations with a mutation of a random PPP in the tournament
		 This is currently commented out**/
		//Random generator = new Random();
		//int mutateIndex = 0;
		//while(mutateIndex == 0){
		//	mutateIndex = generator.nextInt(sizeTour - 1);
		//}
		least = this.leastFit();
		Random generator = new Random();
		int mutate = generator.nextInt(mutChance);
		if (mutate == 0){
			population[selected[least[0]]] = population[selected[least[0]]].mutatePPP();
			population[selected[least[1]]] = population[selected[least[1]]].mutatePPP();
		}
		//population[selected[mutateIndex]] = population[selected[mutateIndex]].mutatePPP();
		this.checkPopReachable();
	}              //Mating event with both Incest Prevention and Crowding Methods
	private void matingEventCrowding(short i){
		this.checkPopReachable();
		short[] most;
		short[] least;
		selectChromosones(sizeTour);
		most = this.mostFit();
		PairPPP temp = twoCrossover(tournament[most[0]],tournament[most[1]]);
		/** To avoid premature convergence, the children are compared to see which parent they are most similar to,
		 * each child is then compared to their similar parent, and the stronger of the parent or child survives
		 **/
		if(temp.getP1().getCrossover() < temp.getP1().getnDes()/2){
			if(temp.getP1().getTurn() > tournament[most[0]].getTurn()){
				population[selected[most[0]]] = new PPP(temp.getP1());
			}
			if(temp.getP2().getTurn() > tournament[most[1]].getTurn()) {
				population[selected[most[1]]] = new PPP(temp.getP2());
			}
		} else {
			if(temp.getP2().getTurn() > tournament[most[0]].getTurn()){
				population[selected[most[0]]] = new PPP(temp.getP2());
			}
			if(temp.getP1().getTurn() > tournament[most[1]].getTurn()) {
				population[selected[most[1]]] = new PPP(temp.getP1());
			}
		}
		least = this.leastFit();
		Random generator = new Random();
		int mutate = generator.nextInt(mutChance);
		if (mutate == 0){
			population[selected[least[0]]] = population[selected[least[0]]].mutatePPP();
			population[selected[least[1]]] = population[selected[least[1]]].mutatePPP();
		}
		this.checkPopReachable();
	}      //Mating event with Crowding Method
	private void matingEventIncest(short i){
		this.checkPopReachable();
		short[] most = new short[0];
		short[] least;
		/** To avoid premature convergence, before mating the similarity of the parents is checked by going through each cell in the PPP.
		 If the parents are found to be too similar, select new parents. If suitable parents cannot be found in reasonable time,
		 the constraint is relaxed and the process is repeated. **/
		int similarity = 1;
		double threshold = 0.5;
		int count = 0;
		while(similarity>threshold){
			count++;
			if (count > 49) {
				threshold += 0.02;
				count = 0;
			}
			selectChromosones(sizeTour);
			most = this.mostFit();
			similarity = getSimilarity(population[selected[most[0]]],population[selected[most[1]]]);
		}
		PairPPP temp = twoCrossover(tournament[most[0]],tournament[most[1]]);
		least = this.leastFit();
		population[selected[least[0]]] = new PPP(temp.getP1());
		population[selected[least[1]]] = new PPP(temp.getP2());
		least = this.leastFit();
		Random generator = new Random();
		int mutate = generator.nextInt(mutChance);
		if (mutate == 0){
			population[selected[least[0]]] = population[selected[least[0]]].mutatePPP();
			population[selected[least[1]]] = population[selected[least[1]]].mutatePPP();
		}
		this.checkPopReachable();
	}		//Mating event with Incest Prevention
	private void classicMatingEvent(short i){
		this.checkPopReachable();
		short[] most;
		short[] least;
		selectChromosones(sizeTour);
		most = this.mostFit();
		least = this.leastFit();

		PairPPP temp = twoCrossover(tournament[most[0]],tournament[most[1]]);
		population[selected[least[0]]] = new PPP(temp.getP1());
		population[selected[least[1]]] = new PPP(temp.getP2());

		least = this.leastFit();
		Random generator = new Random();
		int mutate = generator.nextInt(mutChance);
		if (mutate == 0){
			population[selected[least[0]]] = population[selected[least[0]]].mutatePPP();
			population[selected[least[1]]] = population[selected[least[1]]].mutatePPP();
		}
	}		//Mating event with no convergence measures
	
	/*
	 * 	100 mating event
	 */
	public void hundredME(){
		System.out.print("   ");
		for(short i=0; i<100;i++){
			matingEventIncest(i);
//			System.out.print(".");
		}
		System.out.println();
		avgTurn = this.averageTurnNum();
		mutChance = 8 + (int)Math.floor(avgTurn);
		this.averageTurn();
	}
	
	/*
	 * 	10 times 100 mating event
	 */
	public void thousandME(){
		for(short i=0; i<1;i++){
			System.out.printf("\n  Mating Event Set %d - %d \n", i*100, (i+1)*100);
			hundredME();
		}
		this.averageTurn();
	}
	
	/*
	 * 	one run contains 10,000 mating events
	 */
	public void fullRun(){
		for (int i=0; i<1; i++)
		{
			System.out.printf("\n Mating Event Set %d - %d", i*1000,(i+1)*1000);
			thousandME();
		}
		this.averageTurn();
	}
	
	/*
	 *  Display first 4 chromosomes
	 */
	public void displayFirstFour(){
		for(short i=0; i<4; i++){
			population[i].drawMap();
			population[i].displayPPP();
			population[i].displayDes();
		}
	}
	
	private void describePPP(int index){
		int turns = this.population[index].getTurn();
		int adv = this.population[index].getAdvance();
		double vis = this.population[index].getGoalVisibility();
		double mag = this.population[index].getVisibilityMangitude();
		double weight = this.population[index].getVisibilityWeightedSum();
		this.population[index].drawMap();
		this.population[index].displayMap();
		System.out.printf("PPP%d :: Turns: %d; Adv: %d, Vis: %.2f, Mag: %.2f, VisWeighted: %.2f\n", index, turns, adv, vis, mag, weight);
	}
	
	public void describePopulation(){
		for (int i = 0; i<this.sizePopu; i++){
			this.describePPP(i);
		}
		this.printFitnessMeasurements();
	}
	
	public void writePopulation(String folder){
		File f = new File(folder);
		f.mkdir();
		for (int i =0; i<population.length; i++){
				population[i].writePPP(folder, i);
		}
		System.out.println("Population saved to /"+ folder);
	}
	/*
	 * 	average turn for population
	 */
	public void averageTurn(){
		float result = 0;
		float tempTurn;
		for(short i=0; i<60; i++){
			tempTurn = (float)population[i].getTurn();
			result += tempTurn;
		}
		result = result/60;
		System.out.println("average turn is "+result);
	}

	public float averageTurnNum(){
		float result = 0;
		float tempTurn;
		for(short i=0; i<60; i++){
			tempTurn = (float)population[i].getTurn();
			result += tempTurn;
		}
		result = result/60;
		return result;
	}
	
	public void printFitnessMeasurements(){
		int iTurns = 0;
		int iAdv   = 0;
		int iVis   = 0;
		int iMag   = 0;
		int iWeightMax = 0;
		int iWeightMin = 0;
		int maxTurns = -9999;
		int maxAdv = -9999;
		double minVis = 9999;
		double minMag = 9999;
		double minWeight = 999;
		double maxWeight = -999;
		for (int i = 0; i < this.population.length; i++){
			int t = this.population[i].getTurn();
			int a = this.population[i].getAdvance();
			double v = this.population[i].getGoalVisibility();
			double m = this.population[i].getVisibilityMangitude();
			double weight = this.population[i].getVisibilityWeightedSum();
			if (t > maxTurns){
				iTurns = i;
				maxTurns = t;
			}
			if (a > maxAdv){
				iAdv = i;
				maxAdv = a;
			}
			if (v < minVis){
				iVis = i;
				minVis = v;
			}
			if (m < minMag){
				iMag = i;
				minMag = m;
			}
			if (weight > maxWeight){
				iWeightMax = i;
				maxWeight = weight;
			}
			if (weight < minWeight){
				iWeightMin = i;
				minWeight = weight;
			}
		}
		System.out.printf("\nPPP%d : Max Turns, %d\n",  iTurns, maxTurns);
		System.out.printf("PPP%d : Max Adv,   %d\n",  iAdv, maxAdv);
		System.out.printf("PPP%d : Min Vis,   %.2f\n", iVis, minVis);
		System.out.printf("PPP%d : Min Vis Magnitude,   %.2f\n", iMag, minMag);
		System.out.printf("PPP%d : Min Vis Weighted, %.2f\n", iWeightMin, minWeight);
		System.out.printf("PPP%d : Max Vis Weighted, %.2f\n", iWeightMax, maxWeight);
	}


	//Find the number of identical cells in two PPPs as a percentage
	public int getSimilarity(PPP Parent1, PPP Parent2) {
		int similarity = 0;
		int gridSize = 0;
		for (short i=0; i<42; i++){
			for (short j=0; j<22; j++){
				if (Parent1.getOccCell(i,j) == Parent2.getOccCell(i,j)){
					similarity+= 1;
				}
				gridSize += 1;
			}
		}
		similarity = similarity/gridSize;
		return similarity;
	}
	
	/*
	 * 	*******two point crossover test
	 */
	public void testTPC(){
		for(int i=0;i<10000;i++){
			System.out.println(i);
			PairPPP temp = twoCrossover(population[0],population[1]);
			population[0] = new PPP(temp.getP1());
			population[1] = new PPP(temp.getP2());
			PPP temp1 = new PPP(population[0]);
			PPP temp2 = new PPP(population[1]);
			if(!temp1.checkAvailable()){
				System.out.println("false temp1");
				System.exit(0);
			}
			if(!temp2.checkAvailable()){
				System.out.println("false temp2");
				System.exit(0);
			}
		}
	}
}
