
public abstract class Agent{
	
	Cell cell;
	int turn = 0;
	
	public abstract void go(int turn);
/*	public void look();
	public void reproduce();
	public void die();
	public void set();
	public void read();
	public void send();
	public void recv();
*/
	public void move(int x, int y){
		cell.move(this, x, y);
	}
	
	public void setCell(Cell cell){
		this.cell = cell;
	}

}
