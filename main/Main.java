package main;

import ppp.PPP;
import ppp.PPPManager;
import sim.agent.Bot;
import sim.agent.LongTermExplorer;
import sim.agent.represenation.Memory;
import sim.main.Sim;

public class Main {
	private final static int size = 20; //Width and Height of the PPP
	private final static int descriptors = 10; //Number of different things that can be in a PPP
	private final static int obstacles = 50; //Maximum number of obstacles
	public final static int sensorRange = 3;

	public static void main(String[] args){
		tournament(); //Run the Tournament Selection process for PPPs Main.34
		//singlePPP(); //Generate one PPP Main.20
		//PPP map = Sim.loadPPP("C:\\Users\\burro\\Documents\\ppp-master\\PPP\\new\\PPP62.ppp", false);
		//map.drawMap();
		//map.displayMap();
		//map = Sim.loadPPP("C:\\Users\\burro\\Documents\\ppp-master\\PPP\\new\\PPP23.ppp", false);
		//map.drawMap();
		//map.displayMap();
		//Bot lte = new LongTermExplorer(new Memory(2+(map.size*2), 2+map.size), sensorRange);
		//Sim.singleTest(map , lte, true, true);
		//Sim.testMapsInFolder("C:\\Users\\burro\\Documents\\ppp-master\\PPP\\new", false); //Test maps in given folder
		//Sim.runUPGMA("C:\\Users\\burro\\Documents\\ppp-master\\PPP\\new", true); //Run evaluation on maps in given folder

	}
	
	public static void singlePPP() {
		System.out.println("Initialising a new PPP");
		PPP ppp = new PPP((short)size, (short)descriptors, (short)obstacles); // PPP(size, nDes, maxObs) //Create PPP using specified parameters
		//System.out.println("\n Mutating the PPP");
		ppp = ppp.mutatePPP(); //??Mutate?? the PPP
		ppp.drawMap(); // ??Draw?? the PPP
		ppp.displayPPP(); // ??Display?? the PPP
		ppp.displayDes(); // ??Display?? the descriptors
		//ppp.displayOcc(); // No idea
		ppp.writePPP("C:\\Users\\burro\\Documents\\ppp-master\\PPP\\new", 62); // ??Write?? one of the PPPs
		System.out.println("Done: Exiting");
	}
	
	public static void tournament() {
		PPPManager manager = new PPPManager((short) size, (short)descriptors, (short) obstacles); // Create a new population
		manager.checkPopReachable(); // Check that all PPPs in the population are reachable
		System.out.println("Running tournament");
		for(int i = 0; i < 1; i++){ // Counts out the 10,000s for generation
			manager.fullRun(); // ?? Counts out the 1000s
			System.out.println("This is the big number TOM");
			System.out.print(i);
		}
		manager.describePopulation(); // Runs the Evaluation methods on the population
		manager.writePopulation("C:\\Users\\burro\\Documents\\ppp-master\\PPP\\new"); // Write Population to files
		Sim.newTestPopulation(manager, "C:\\Users\\burro\\Documents\\ppp-master\\PPP\\new" );
		Sim.newUPGMA(manager, "C:\\Users\\burro\\Documents\\ppp-master\\PPP\\new");
	}

}
