package tax;
/*
 *	Author:	Hao Wei
 *	Time:	24/05/2013
 *	Purpose: to record the replacement in the array.
 */
public class Replace {
	private short a;	// the replace number
	private short b;	// the first number being replaced
	private short c;	// the second number being replaced
	public Replace(short a, short b, short c){
		this.a = a;
		this.b = b;
		this.c = c;
	}
	/*
	 *  return a - in order to find the particular replacement
	 */
	public short getRep(){
		return a;
	}
	/*
	 * 	return b
	 */
	public short getFirst(){
		return b;
	}
	/*
	 * 	return c
	 */
	public short getSec(){
		return c;
	}
}
