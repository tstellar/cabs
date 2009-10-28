
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
}
