import java.util.*;

public class Cell{
	int x;
	int y;
	Engine engine;
	ArrayList<Agent> agents;

	public Cell(int x, int y, Engine engine){
		this.x = x;
		this.y = y;
		agents = new ArrayList<Agent>();
		this.engine = engine;
	}


	public void go(int turn){
		int totalAgents = agents.size();
		for(int i=0; i< totalAgents; i++){
			agents.get(i).go(turn);
		}
	}
	
	public void move(Agent agent, int x, int y){
		engine.moveAgent(agent, this, x, y);
	}

	public void add(Agent agent){
		agent.setCell(this);
		agents.add(agent);
	}

	public void remove(Agent agent){
		agents.remove(agent);
		//Handle error.
	}
}
