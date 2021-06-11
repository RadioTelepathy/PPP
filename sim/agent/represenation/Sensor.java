package sim.agent.represenation;

import java.util.ArrayList;
import java.util.Random;

import ppp.PPP;
import sim.agent.Bot;

public class Sensor {
	
	private int sensorRange;
	private double sensorNoise;
	
	public Sensor(int range){
		this(range,  0.0);
	}
	
	public Sensor(int range, double noise){
		this.sensorRange = range;
		this.sensorNoise = noise;
	}
	
	public int getRange(){
		return this.sensorRange;
	}
	
	public double getNoise(){
		return this.sensorNoise;
	}
	
	public void setNoise(double noise){
		this.sensorNoise = noise;
	}
	
	/*
	 * Line of Sight
	 */
	public static double lerp(int start, int end, double dist){
		return Math.round(start+dist *(end-start));
	}
	
	public static int diag_dist(int x1, int y1, int x2, int y2){
		int distX = x1-x2;
		int distY = y1-y2;
		return Math.max(Math.abs(distX), Math.abs(distY));
	}
	
	public static ArrayList<short[]> line(int x1, int y1, int x2, int y2){
		ArrayList<short[]> points = new ArrayList<short[]>();
		int nPoints = Sensor.diag_dist(x1, y1, x2, y2);
		for (int step = 0; step<=nPoints; step++){
			double t = nPoints == 0? 0.0 : (double)step/(double)nPoints;
			short x = (short)Sensor.lerp(x1, x2, t);
			short y = (short)Sensor.lerp(y1, y2, t);
			short[] point = {x,y};
			points.add(point);
		}
		return points;
	}
	
	public static int[] getEndPoints(int xLeft, int xRight){
		int[] endPoints = new int[xRight-xLeft+1];
		for(int i=0; i<xRight-xLeft+1; i++){
			endPoints[i]=xLeft+i;
		}
		return endPoints;
	}
	
	public int[] boundSenseWindow(int centreX, int centreY, int pppSize){
		int x_left   = centreX - this.sensorRange;
		int x_right  = centreX + this.sensorRange;
		int y_top    = centreY - this.sensorRange;
		int y_bottom = centreY + this.sensorRange;
		
		if(y_top < 0)              { y_top  = 0;}
		if(x_left < 0)             { x_left = 0;}
		if(x_right > 2*pppSize+1) {x_right  = 2*pppSize+1;}
		if(y_bottom > pppSize+1)  {y_bottom = pppSize+1;}
		
		return new int[] {x_left, x_right, y_top, y_bottom};
	}
	
	/*
	 * Sense
	 */
	public void sense(PPP ppp, Bot bot){
		short[] centre_pos = bot.getPos();
		int[] senseWindow = this.boundSenseWindow(centre_pos[0], centre_pos[1], ppp.size);
		// Sense along lines from robot to fringe of sight, given by sensorRange
		
		int x_left   = senseWindow[0];
		int x_right  = senseWindow[1];
		int y_top    = senseWindow[2];
		int y_bottom = senseWindow[3];
		
		if(y_top < 0)              { y_top = 0;}
		if(x_left < 0)             { x_left = 0;}
		if(x_right > 2*ppp.size+1) {x_right = 2*ppp.size+1;}
		if(y_bottom > ppp.size+1)  {y_bottom=ppp.size+1;}
		
		Random rand = new Random();
		for(int y=y_top; y<=y_bottom; y++){
			//End X coords of the end of a line to each cell between (xleft, x_right)
			//along the current y coord
			int[] endPoints = Sensor.getEndPoints(x_left, x_right);
			
			for(int x: endPoints){
				ArrayList<short[]> LoS = Sensor.line(centre_pos[0], centre_pos[1], x, y);
				//Sense along the LoS
				for (short[] p : LoS){
					boolean true_reading = true;
					boolean is_goal = false;
					short cell_value = 0;
					if(bot.currentMem.isGoal(p[0], p[1])){
						bot.goal_found = true;
						bot.goal_pos = p;
						is_goal = true;
					}
					
					if ((this.sensorNoise != 0.0) && (!is_goal)) {
						//Never incorrectly sense the goal position
						double n = rand.nextDouble();
						if (n < this.sensorNoise){
							//sensor returns true occupancy
							true_reading = false;
						}
					}

					if (true_reading) {
						if (Occupancy.getType(bot.currentMem.readSquare(p[0], p[1])) == Occupancy.UNKNOWN){
							bot.unknown_cells--;
						}
						cell_value = ppp.getOccCell(p[0], p[1]);
					} else {
						//sensor returns noisy value
						int n = rand.nextInt(2);
						switch (n) {
						case 0:
							cell_value = (short)Occupancy.BOUNDARY.code;
							break;
						case 1:
							cell_value = (short)Occupancy.EMPTY.code;
							break;
						case 2:
							cell_value = (short)Occupancy.UNKNOWN.code;
							break;
						}
					}
					bot.currentMem.setCell(p[0], p[1],  cell_value);
					//Can't see through walls, rest of the line ignored.
					if (ppp.isOccupied(p[0], p[1], false)){
						break;
					}
				}
			}
		}
	}

}
