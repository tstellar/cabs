
public abstract class Agent{
	
	Cell cell;
	int turn = 0;
	
	public abstract void go(int turn);
	public abstract void move(int x, int y);
	public abstract void setCell(Cell cell);
/*	public void look();
	public void reproduce();
	public void die();
	public void set();
	public void read();
	public void send();
	public void recv();
*/
}
