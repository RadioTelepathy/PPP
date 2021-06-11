package state;

/*
 * 	Author: Hao Wei
 * 	Time: 	14/05/2013
 * 	Purpose: This class is used for representing the value of a state of the agent
 */
public class StateValue {
	private short turn;		// the number of turns
	private short advance;	// the number of advances
	private short move;		// the number of total moves
	/*
	 * 	Constructor without parameter
	 * 	The maximum number that short can represent is 32767
	 */
	public StateValue(){
		turn = 5000;
		advance = 5000;
		move = 5000;
	}
	/*
	 * 	Constructor with parameter
	 */
	public StateValue(short turn, short advance, short move){
		this.turn = turn;
		this.advance = advance;
		this.move = move;
	}
	/*
	 * 	Constructor by given a StateValue
	 */
	public StateValue(StateValue sv){
		this.turn = sv.turn;
		this.advance = sv.advance;
		this.move = sv.move;
	}
	/*
	 * 	Return the value of the number of turns
	 */
	public short getTurn(){
		return turn;
	}
	/*
	 * 	Return the value of the number of advances
	 */
	public short getAdvance(){
		return advance;
	}
	/*
	 * 	Return the value of the number of total moves
	 */
	public short getMove(){
		return move;
	}
	/*
	 * 	Add the value of turn and move by 1
	 */
	public void addTurn(){
		turn++;
		move++;
	}
	/*
	 * 	Add the value of advance and move by 1
	 */
	public void addAdvance(){
		advance++;
		move++;
	}
	/*
	 * 	Compare the state values, and return true if this is smaller(parameter is larger)
	 */
	public boolean compareSV(StateValue n){
		if(move < n.getMove()){
			return true;
		} else if(move == n.getMove()){
			if(advance < n.getAdvance()){
				return true;
			} else if(advance == n.getAdvance()){
				if(turn < n.getTurn()){
					return true;
				} else return false;
			} else return false;
		} else return false;
	}
	/*
	 * 	compare the state values by turn
	 */
	public boolean compareSVT(StateValue n){
		if(turn < n.getTurn()){
			return true;
		} else if(turn == n.getTurn()){
			if(move < n.getMove()){
				return true;
			} else if(move == n.getMove()){
				if(advance < n.getAdvance()){
					return true;
				} else return false;
			} else return false;
		} else return false;
	}
	/*
	 * 	Compare the states values, and return true if they are the same
	 */
	public boolean sameSV(StateValue n){
		return move == n.getMove() && advance == n.getAdvance() && turn == n.getTurn();
	}
	/*
	 * 	Check whether the state value is max or not.
	 * 	Return true if the state value is valid.
	 */
	public boolean validSV(){
		return move < 4000;
	}
	/*
	 * 	toString
	 */
	public String toString(){
		return "("+turn+","+advance+","+move+")";
	}
}
