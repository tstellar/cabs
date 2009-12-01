package world;

import java.io.Serializable;

public abstract class Agent implements Serializable {
	
	private static final long serialVersionUID = 1L;
	transient LocalCell cell;
	int turn = 0;
	public boolean hasMoved = false;
	
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
		if (!hasMoved) {
			hasMoved = true;
			this.go();
		}
	}
	
}
