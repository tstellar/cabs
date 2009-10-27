import java.util.*;


public class Engine{


	Cell[][] cells;

	public Engine(int height, int width){
		cells = new Cell[height][width];
		for(int i=0; i< cells.length; i++){
			for(int j=0; j< cells[i].length; j++){
				cells[i][j] = new Cell(j,i,this);
			}
		}
	}

	public void go(int turn){
		for(int i=0; i< cells.length; i++){
			for(int j=0; j< cells[i].length; j++){
				cells[i][j].go(turn);
			}
		}
	}
	
	public void moveAgent(Agent agent, Cell oldCell, int x, int y){
		Cell newCell = findCell(oldCell.x + x, oldCell.y + y);
		newCell.add(agent);
		oldCell.remove(agent);
	}

	private Cell findCell(int x, int y){
		if(y >= cells.length){
			y = y % cells.length;
		}
		if(x >= cells[y].length){
			x = x % cells.length;
		}
		if(y < 0){
			y = (y % cells.length) + cells.length;
		}
		if(x < 0){
			x = (x % cells.length) + cells[y].length;
		}
		return cells[y][x];
	}

	public void placeAgents(int agents){
		for(int i=0; i< agents; i++){
			cells[i][0].add(new Rabbit(cells[i][0]));
		}
	}

	public void print(){
		for(int i=0; i< cells.length; i++){
			for(int j=0; j< cells[i].length; j++){
				if(cells[i][j].agents.size() > 0){
					System.out.print("* ");
				}
				else{
					System.out.print("- ");
				}
			}
			System.out.println();
		}
	}	
	
	public static void main(String[] args){
		
		Engine engine = new Engine(5, 5);
		engine.placeAgents(5);
		engine.print();
		for(int i=0; i< 8; i++){
			engine.go(i);
			engine.print();
		}
	}
}
