package sim.agent;

import sim.agent.represenation.Memory;
import sim.agent.represenation.Node;

public class DecisionBumper extends BumperBot {
	private final static String BOT_NAME = "Decision Bumper";
	private String[][] decisions;

	public DecisionBumper(Memory currentMem, int sensor_range) {
		super(currentMem, sensor_range);
		this.decisions = new String[this.currentMem.mem_height][this.currentMem.mem_width];
		this.initDecisions();
	}
	
	private void initDecisions(){
		for (int y = 0; y < this.currentMem.mem_height; y++){
			for (int x = 0; x < this.currentMem.mem_width; x++){
				this.decisions[y][x] = "";
			}
		}
	}
	
	@Override
	public void reset(){
		super.reset();
		this.initDecisions();
	}
	
	@Override
	public void plan(){
		short[] pos = this.getPos();
		char currentHeading = this.getHeading();
		Node ahead = this.getNext(pos[0], pos[1], currentHeading);
		
		//Can't continue in this direction
		while(this.currentMem.occupied(ahead.getX(), ahead.getY())){
			String history = this.decisions[pos[1]][pos[0]];
			//Continuing in this direction not valid, add it to history so it isn't picked
			history += currentHeading;
			
			if (this.allMovesTried(history)){
				//All possible directions tried, revert to random behaviour
				super.plan();
				//Cease further planning
				return;
			} else {
				//Pick untried heading
				char newHeading = this.randomHeading();
				boolean going = true;
				while (going){
					newHeading = this.randomHeading();
					//Reduce possibility of heading back the way we came
					if (newHeading == this.oppositeDirection(currentHeading)){
						newHeading = this.randomHeading();
					}
					//Previously tried this heading
					if (history.indexOf(newHeading) != -1){
						going = true;
					} else {
						going = false;
					}
				}
				ahead = this.getNext(pos[0], pos[1], newHeading);
				history += newHeading;
				//update decisions
				this.decisions[pos[1]][pos[0]] = history;
			}
		}
		this.planned_route.add(ahead);
	}
	
	public boolean allMovesTried(String history){
		if ((history.contains("u")) && (history.contains("d"))){
			if ((history.contains("l")) && (history.contains("r"))){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String getName(){
		return this.BOT_NAME + super.getSuffix();
	}

}
