
public class Rabbit extends Agent{

	public Rabbit(Cell cell){
		this.cell = cell;
	}
	public void go(int turn){
		if(this.turn == turn){
			move(1,0);
			this.turn++;
		}
	}

	public void move(int x, int y){
		cell.move(this, x, y);
	}
	
	public void setCell(Cell cell){
		this.cell = cell;
	}
}
