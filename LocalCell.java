import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
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

	public byte[] serialize() {
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(s);
			oos.writeInt(x);
			oos.writeInt(y);
			oos.writeInt(agents.size());
			/*if(agents.size() != 0) {
			System.err.println(MessageFormat.format("Serializing cell ({0}, {1}); {2} agents.",
					x, y, agents.size()));
			}*/
			for( Agent a : agents){
				oos.writeObject(a);
			}
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s.toByteArray();
	}
	
	public void resetAgents(){
		for(Agent a : agents){
			a.hasMoved = false;
		}
	}
}

