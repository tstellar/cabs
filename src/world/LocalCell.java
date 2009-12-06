package world;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;

import engine.LocalEngine;

public class LocalCell extends Cell {
	LocalEngine engine;
	public ArrayList<Agent> agents;
	
	public LocalCell(int x, int y, LocalEngine engine) {
		super(x, y);
		setAgents(new ArrayList<Agent>());
		this.engine = engine;
	}
	
	public void go(int turn) {
		int totalAgents = getAgents().size();
		for (int i = 0; i < totalAgents; i++) {
			getAgents().get(i).start(turn);
			if (getAgents().isEmpty()) {
				break;
			}
			if (getAgents().size() < totalAgents) {
				totalAgents = getAgents().size();
				i -= (totalAgents - getAgents().size());
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
		getAgents().add(agent);
	}
	
	public void remove(Agent agent) {
		getAgents().remove(agent);
		// Handle error.
	}
	
	public byte[] serialize() {
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		DataOutputStream dos;
		try {
			dos = new DataOutputStream(s);
			dos.writeInt(x);
			dos.writeInt(y);
			dos.writeInt(agents.size());
			/*if(agents.size() != 0) {
			System.err.println(MessageFormat.format("Serializing cell ({0}, {1}); {2} agents.",
					x, y, agents.size()));
			}*/
			for( Agent a : agents){
				dos.write(a.toBytes());
			}
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s.toByteArray();
	}
	
	public void resetAgents() {
		for (Agent a : getAgents()) {
			a.hasMoved = false;
		}
	}
	
	public void setAgents(ArrayList<Agent> agents) {
		this.agents = agents;
	}
	
	public ArrayList<Agent> getAgents() {
		return agents;
	}
}
