package world;

public abstract class Cell {

	public int x;
	public int y;

	public Cell(int x, int y) {
		this.setX(x);
		this.setY(y);
	}

	abstract public void move(Agent agent, int x, int y);

	abstract public void add(Agent agent);

	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return y;
	}

}
