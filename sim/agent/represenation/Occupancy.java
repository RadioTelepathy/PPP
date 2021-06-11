package sim.agent.represenation;

/**
 * Enum to describe occupancy of a grid cell 
 * and provide pretty printing for each "type" of occupancy
 * @author slw546
 */
public enum Occupancy {
	EMPTY(0,      '.'),
	BOUNDARY(1,   '#'),
	START(2,       '@'),
	OBS_LEFT(3,   '['),
	OBS_RIGHT(4,  ']'),
	MOVE_UP(5,    '^'),
	MOVE_DOWN(6,  'v'),
	MOVE_LEFT(7,  '<'),
	MOVE_RIGHT(8, '>'),
	CLEAR_LEFT(9, '('),
	CLEAR_RIGHT(10, ')'),
	POI(11,        '*'),//Point of Interest, for debugging
	GOAL(12,      'G'),
	BOT(13,       'B'),

	UNKNOWN(99,   '~');
	
	public int code;
	public char symbol;
	private Occupancy(int o, char s){
		this.code = o;
		this.symbol = s;
	}
	
	public String toString(){
		return Character.toString(this.symbol);
	}
	
	public static Occupancy getType(int o){
		switch (o){
		case 0:
			return EMPTY;
		case 1:
			return BOUNDARY;
		case 2:
			return START;
		case 3:
			return OBS_LEFT;
		case 4:
			return OBS_RIGHT;
		case 5:
			return MOVE_UP;
		case 6:
			return MOVE_DOWN;
		case 7:
			return MOVE_LEFT;
		case 8:
			return MOVE_RIGHT;
		case 9:
			return CLEAR_LEFT;
		case 10:
			return CLEAR_RIGHT;
		case 11:
			return POI;
		case 12:
			return GOAL;
		case 13:
			return BOT;
		default:
			return UNKNOWN;
		}
	}
	
	public static Occupancy getType(short o){
		return Occupancy.getType((int)o);
	}
	
	public static boolean isObstacle(Occupancy o){
		return Occupancy.isObstacle((int)o.code);
	}
	
	public static boolean isObstacle(short o){
		return Occupancy.isObstacle((int)o);
	}
	
	public static boolean isObstacle(int o){
		switch(Occupancy.getType(o)){
		case BOUNDARY:
		case OBS_LEFT:
		case OBS_RIGHT:
		case CLEAR_LEFT:
		case CLEAR_RIGHT:
			return true;
		default:
			return false;
		}
	}
	
	public static Occupancy getHeading(char h){
		switch (h){
		case 'u':
			return MOVE_UP;
		case 'd':
			return MOVE_DOWN;
		case 'l':
			return MOVE_LEFT;
		case 'r':
			return MOVE_RIGHT;
		default:
			return UNKNOWN;
		}
	}

}
