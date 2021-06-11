package ppp;

import java.util.Random;
/*
 * 	Author:	Hao Wei
 * 	Time:	01/06/2013
 * 	Purpose: to represent a single descriptor as a type in order to apply the evolutionary algorithm 
 */

public class Descriptor {
	private short x;		// x coordinate of the descriptor
	private short y;		// y coordinate of the descriptor
	private short l;		// the length of the descriptor
	private short t;		// the type or pattern of the descriptors
	private short o;		// the type of obstacle the descriptor uses
	Random generator;	// for evolution
	/*
	 * 	Constructor for Descriptor
	 */
	public Descriptor(short x, short y, short l, short t, short o){
		this.x = x;
		this.y = y;
		this.l = l;
		this.t = t;
		this.o = o;
	}
	
	public Descriptor(int x, int y, int l, int t, int o){
		this.x = (short) x;
		this.y = (short) y;
		this.l = (short) l;
		this.t = (short) t;
		this.o = (short) o;
	}
	
	/*
	 * 	Constructor by given a Descriptor
	 */
	public Descriptor(Descriptor d){
		this.x = d.x;
		this.y = d.y;
		this.l = d.l;
		this.t = d.t;
		this.o = d.o;
	}
	/*
	 * 	return x coordinate
	 */
	public short getX(){
		return x;
	}
	/*
	 * 	return y coordinate
	 */
	public short getY(){
		return y;
	}
	/*
	 * 	return the length of the descriptor
	 */
	public short getLength(){
		return l;
	}
	/*
	 * 	return the type
	 */
	public short getType(){
		return t;
	}

	public short getObstacle(){
		return o;
	}
	/*
	 * 	Set the length of the descriptors
	 */
	public void setLength(short l){
		this.l = l;
	}
	/*
	 * 	mutation operator
	 */
	public void mutation(short size){
		generator = new Random();
		int mut = generator.nextInt(6);
		switch(mut){
			case 0: {newX(size);
					break;}
			case 1: {newY(size);
					break;}
			case 2: {lengthenDes();
					break;}
			case 3: {shortenDes();
					break;}
			case 4: {changeType();
					break;}
			case 5: changeObs();
					break;}
		}

	/*
	 * 	generate a new x for the descriptor by given the size of its PPP
	 */
	private void newX(short size){
		x = (short)(generator.nextInt(size)+1);
	}
	/*
	 * 	generate a new y for the descriptor by given the size of its PPP
	 */
	private void newY(short size){
		y = (short)(generator.nextInt(size)*2+1);
	}
	/*
	 * 	lengthen the length of the descriptor by one 
	 */
	private void lengthenDes(){
		l++;
	}
	/*
	 * 	shorten the length of the descriptor by one unless this would
	 * 	make the length zero
	 */
	private void shortenDes(){
		if(l>1) l--;
	}
	/*
	 * 	change the type of the descriptor randomly;
	 */
	private void changeType(){
		short temp = (short)(generator.nextInt(6));
		while(temp == t){
			temp = (short)(generator.nextInt(6));
		}
		t = temp;
	}

	private void changeObs(){
		short temp = (short)(generator.nextInt(2));
		while (temp == o) {
			temp = (short)(generator.nextInt(2));
		}
	}
	/*
	 * 	toString
	 */
	public String toString(){
		return this.write()+ "," + this.printType();
//		String result;
//		result = "("+(y-1)/2+","+(x-1)+","+l+","+t+")";
//		return result;
	}
	
	public String printType(){
		switch(this.t){
		case 0:
			return "right";
		case 1:
			return "left";
		case 2:
			return "up";
		case 3:
			return "down";
		case 4:
			return "left-up";
		case 5:
			return "left-down";
		default:
			return "??";	
		}
	}
	
	public String write(){
		String result = "("+ x + "," + y + "," + l +","+ t + "," + o + ")";
		return result;
	}
}