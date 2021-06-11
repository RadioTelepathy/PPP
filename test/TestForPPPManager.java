package test;

import ppp.PPPManager;

/*
 * 	Test class for PPPManager
 */
public class TestForPPPManager {
	public static void main(String[] args){
		PPPManager ppp = new PPPManager((short)20,(short)8,(short)50);
		/*ppp.averageTurn();
		ppp.oneRun();*/
		ppp.averageTurn();
		for(int i=0; i<40;i++){
			//System.out.println(i+"00");
			ppp.describePopulation();
			System.out.println();
			ppp.hundredME();
			ppp.averageTurn();
		}
	}
}
