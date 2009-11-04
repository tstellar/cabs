public class Engine {

	Cell[][] cells;
	int globalHeight;
	int globalWidth;
	int tlx;
	int tly;
	int height;
	int width;

	public Engine(int tlx, int tly, int height, int width, int globalHeight, int globalWidth) {
		this.tlx = tlx;
		this.tly = tly;
		this.globalHeight = globalHeight;
		this.globalWidth = globalWidth;

		cells = new Cell[height][width];
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cells[i][j] = new Cell(j, i, this);
			}
		}
	}

	public void go(int turn) {
		for (Cell[] cell : cells) {
			for (Cell element : cell) {
				element.go(turn);
			}
		}
	}

	public void moveAgent(Agent agent, Cell oldCell, int x, int y) {
		Cell newCell = findCell(oldCell.x + x, oldCell.y + y);
		newCell.add(agent);
		oldCell.remove(agent);
	}

	/*
	 * private String getCellOwner(int x, int y){ }
	 * 
	 * private void discoverEngines(){ }
	 * 
	 * private Cell findCell(String ip, int x, int y){ }
	 */
	private Cell findCell(int x, int y) {
		if (y >= globalHeight) {
			y = y % globalHeight;
		}
		if (x >= globalWidth) {
			x = x % globalWidth;
		}
		if (y < 0) {
			y = (y % globalHeight) + globalHeight;
		}
		if (x < 0) {
			x = (x % globalWidth) + globalWidth;
		}
		return cells[y][x];
	}

	public void placeAgents(int agents) {
		for (int i = 0; i < agents; i++) {
			cells[i][0].add(new Rabbit());
		}
	}

	public void print() {
		for (Cell[] cell : cells) {
			for (Cell element : cell) {
				if (element.agents.size() > 0) {
					System.out.print("* ");
				} else {
					System.out.print("- ");
				}
			}
			System.out.println();
		}
	}

	public static void main(String[] args) {

		Engine engine = new Engine(0, 0, 5, 5, 5, 5);
		engine.placeAgents(5);
		engine.print();
		for (int i = 0; i < 8; i++) {
			engine.go(i);
			engine.print();
		}
	}
}
