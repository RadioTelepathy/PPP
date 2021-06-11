package sim.agent.represenation;

/**
 * Limited Size memory, providing finite window onto the PPP
 * This class maps functions back onto Memory within the constrained view
 * Also provides rePlot to handle moving the view window when the bot moves
 * @author slw546
 */
public class LimitedMemory extends Memory {

	//Bounds of this memory's viewpoint onto the map
	private int xLeft;
	private int xRight;
	private int yTop;
	private int yBottom;
	private int offset;
	
	/**
	 * Construct a limited memory space
	 * @param limitedW 
	 * @param limitedH - Dimensions
	 * @param sensorRange - to calculate offset for setting cells, etc
	 */
	public LimitedMemory(int limitedW, int limitedH, int sensorRange) {
		super(limitedW, limitedH);
		this.xLeft = 0;
		this.xRight = sensorRange+1;
		this.yTop  = 0;
		this.yBottom = sensorRange+1;
		this.offset = sensorRange;
	}
	
	@Override
	public void rePlot(short[] newPos){
		short[][] newMap = new short[this.mem_height][this.mem_width];
		
		//New spatial bounds
		int newXLeft = newPos[0]-this.offset;
		int xRight   = newPos[0]+this.offset;
		int newYTop  = newPos[1]-this.offset;
		int yBottom  = newPos[1]+this.offset;
		
		for (int y = newYTop; y <= yBottom; y++){
			for (int x = newXLeft; x <= xRight; x++){
				if (this.inSpatialBounds(x, y)){
					//New cell is in old viewpoint
					Cell cell = this.readCell(x, y);
					this.setDirect(x-newXLeft, y-newYTop, cell);
				} else {
					//not in old viewpoint, so is unknown
					this.setUnsensed(x-newXLeft, y-newYTop);
				}
			}
		}
		//Set bounds of new viewpoint
		this.xLeft = newXLeft;
		this.xRight = xRight;
		this.yTop = newYTop;
		this.yBottom = yBottom;
		//this.map = newMap;
	}
	
	protected boolean inSpatialBounds(int x, int y){
		if ((x<this.xLeft) || (x>this.xRight)) {
			return false;
		}
		if ((y<this.yTop) || (y>this.yBottom)){
			return false;
		}
		return true;
	}
	
	//Set directly into the limited memory without offsetting
	private void setDirect(int x, int y, Cell c){
		this.map[y][x].setOccupancy(c.getOccupancy());
		this.map[y][x].setReachable(c.isReachable());
	}
	
	@Override
	public boolean setCell(int x, int y, short val){
		try{
			return super.setCell(x-this.xLeft, y-this.yTop, val);
		} catch (ArrayIndexOutOfBoundsException e){
			System.err.println("LimitedMem.setCell");
			System.err.printf("Set %d,%d --> %d,%d\n", x,y, x-this.xLeft, y-this.yTop);
			System.err.printf("Bounds %d,%d to %d,%d\n" , this.xLeft, this.yTop, this.xRight, this.yBottom);
			System.err.printf("%d,%d size\n", this.mem_width, this.mem_height);
			e.printStackTrace();
			System.exit(1);
		}
		return false;
	}
	
	@Override
	public boolean validPosition(int x, int y){
		return super.validPosition(x-this.xLeft, y-this.yTop);
	}
	
	@Override
	public boolean occupied(int x, int y){
		if (!this.inSpatialBounds(x, y)){
			return false;
		}
		return super.occupied(x-this.xLeft, y-this.yTop);
	}
	
	@Override
	public Cell readCell(int x, int y){
		if (!this.inSpatialBounds(x, y)){
			return null;
		}
		return super.readCell(x-this.xLeft, y-this.yTop);
	}
	
	@Override
	public short readSquare(int x, int y){
		if (!this.inSpatialBounds(x, y)){
			return 99;
		}
		return super.readSquare(x-this.xLeft, y-this.yTop);
	}
	
	@Override
	public boolean isGoal(int x, int y){
		return super.isGoal(x-this.xLeft, y-this.yTop);
	}
	
	@Override
	/*
	 * Override to return neighbours in terms of view point position
	 */
	public int[][] getNeighbours(Cell c){
		int[][] coord = new int[][] {
			{c.getX()-this.xLeft, c.getY()-1-this.yTop},  //up
			{c.getX()-1-this.xLeft, c.getY()-this.yTop},  //left
			{c.getX()-this.xLeft, c.getY()+1-this.yTop},  //down
			{c.getX()+1-this.xLeft, c.getY()-this.yTop}}; //right
					
		return coord;
	}
	
	@Override
	public String toString(){
		return "Limited Memory: " + this.mem_width + "x" + this.mem_height;
	}
}
