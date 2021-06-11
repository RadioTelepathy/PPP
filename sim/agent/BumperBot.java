package sim.agent;

import java.util.Random;

import sim.agent.represenation.Memory;
import sim.agent.represenation.Node;

public class BumperBot extends Bot {
	private final String BOT_NAME = "Bumper";

	public BumperBot(Memory currentMem, int sensor_range) {
		super(currentMem, sensor_range);
	}

	@Override
	public void aprioriPlan(short goalX, short goalY) {}
	
	protected char randomHeading(){
		char newHeading = 'u';
		Random rand = new Random();
		int r = rand.nextInt(4);
		switch(r){
		case 0:
			newHeading = 'u';
			break;
		case 1:
			newHeading = 'l';
			break;
		case 2:
			newHeading = 'd';
			break;
		case 3:
			newHeading = 'r';
			break;
		}
		return newHeading;
	}
	
	protected char oppositeDirection(char h){
		switch (h){
		case 'u':
			return 'd';
		case 'd':
			return 'u';
		case 'l':
			return 'r';
		case 'r':
			return 'l';
		default:
			return 'u';
		}
	}
	
	protected Node getNext(int xPos, int yPos, char currentH){
		int[][] neighbours = this.currentMem.getNeighboursOfPos(xPos, yPos);
		int[] ahead;
		switch(currentH){
		case 'u':
			ahead = neighbours[0];
			break;
		case 'l':
			ahead = neighbours[1];
			break;
		case 'd':
			ahead = neighbours[2];
			break;
		case 'r':
			ahead = neighbours[3];
			break;
		default:
			ahead = neighbours[0];
			break;
		}
		return new Node(ahead[0], ahead[1], currentH);
	}
	
	@Override
	public void plan() {
		short[] pos = this.getPos();
		char heading = this.getHeading();
		Node ahead = this.getNext(pos[0], pos[1], heading);
		
		String tried = "";
		while(this.currentMem.occupied(ahead.getX(), ahead.getY())){
			//Must change direction
			char newHeading = heading;
			while ((newHeading == heading) && (tried.indexOf(newHeading) == -1)){
				newHeading = this.randomHeading();
				if (newHeading == this.oppositeDirection(heading)){
					newHeading = this.randomHeading();
				}
			}
			ahead = this.getNext(pos[0], pos[1], newHeading);
			tried += newHeading;
		}
		
		this.planned_route.add(ahead);
	}
	
	@Override
	public String getName(){
		return this.BOT_NAME + super.getSuffix();
	}

}
