package tax;
/*
 * 	Author: Hao Wei
 * 	Time:	25/05/2013
 * 	Purpose: To represent a (x,y)
 */
public class Pair {
	private short x;
	private short y;
	/*
	 * 	Constructor for Pair
	 */
	public Pair(short x, short y){
		this.x = x;
		this.y = y;
	}
	/*
	 * 	return the value of x
	 */
	public short getX(){
		return x;
	}
	/*
	 * 	return the value of y
	 */
	public short getY(){
		return y;
	}
	/*
	 * 	return true if x is the same as the given number
	 */
	public boolean contain(short n){
		return n==x;
	}
	/*
	 * 	toString 
	 */
	public String toString(){
		String result;
		result = "(" + x + ", " + y + ")";
		return result;
	}
}
