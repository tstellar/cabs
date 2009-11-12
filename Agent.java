import java.io.Serializable;

public abstract class Agent implements Serializable {

	transient LocalCell cell;
	int turn = 0;
	
	public abstract void go(); //abstract instead of interface so we can assume variables.
/*	public void look();
	public void reproduce();
	public void die();
	public void set();
	public void read();
	public void send();
	public void recv();
*/
	public void move(int x, int y){

	public abstract void go();

	/*
	 * public void look(); public void reproduce(); public void die(); public
	 * void set(); public void read(); public void send(); public void recv();
	 */
	public void move(int x, int y) {
		cell.move(this, x, y);
	}

	public void setCell(LocalCell cell) {
		this.cell = cell;
	}

	public void start(int turn) {
		if (this.turn == turn) {
			this.go();
		}
	}

	public void end() {
		this.turn++;
	}

}
