
public class Rabbit extends Agent{

	public Rabbit(Cell cell){
		this.cell = cell;
	}
	public void go(){
		move(1,0);
		end();
	}
}
