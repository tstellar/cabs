
public class Rabbit implements Agent{

	Cell cell;
	public Rabbit(Cell cell){
		this.cell = cell;
	}
	public void go(){
		move(1,0);
	}

	public void move(int x, int y){
		cell.move(this, x, y);
	}
}
