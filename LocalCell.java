import java.util.ArrayList;

public class LocalCell extends Cell {
	LocalEngine engine;
	ArrayList<Agent> agents;

	public LocalCell(int x, int y, LocalEngine engine) {
		super(x, y);
		agents = new ArrayList<Agent>();
		this.engine = engine;
	}

	public void go(int turn) {
		int totalAgents = agents.size();
		for (int i = 0; i < totalAgents; i++) {
			agents.get(i).start(turn);
			if(agents.isEmpty()){
				break;
			}
			if(agents.size() < totalAgents){
				totalAgents = agents.size();
				i-= (totalAgents - agents.size());
			}
		}
	}

	@Override
	public void move(Agent agent, int x, int y) {
		engine.moveAgent(agent, this, x, y);
	}

	@Override
	public void add(Agent agent) {
		agent.setCell(this);
		agents.add(agent);
	}

	public void remove(Agent agent) {
		agents.remove(agent);
		// Handle error.
	}
}
