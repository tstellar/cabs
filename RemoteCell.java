
public class RemoteCell extends Cell {

	RemoteEngine engine;

	public RemoteCell(int x, int y, RemoteEngine engine) {
		super(x, y);
		this.engine = engine;
	}

	@Override
	public void add(Agent agent) {
		engine.sendAgent(this, agent);
	}

	@Override
	public void move(Agent agent, int x, int y) {
		// TODO Auto-generated method stub

	}

}
