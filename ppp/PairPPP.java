package ppp;
/*
 * 	Author:	Hao Wei
 * 	Time:	07/06/2013
 * 	Purpose: To represent a pair of PPP
 */
public class PairPPP {
	private PPP p1;
	private PPP p2;
	/*
	 * 	Constructor
	 */
	public PairPPP(PPP p1, PPP p2){
		this.p1 = p1;
		this.p2 = p2;
	}
	/*
	 * 	return p1
	 */
	public PPP getP1(){
		return this.p1;
	}
	/*
	 * 	return p2
	 */
	public PPP getP2(){
		return this.p2;
	}
}
