public abstract class Cell {

	int x;
	int y;

	public Cell(int x, int y) {
		this.x = x;
		this.y = y;
	}

	abstract public void move(Agent agent, int x, int y);

	abstract public void add(Agent agent);

}
