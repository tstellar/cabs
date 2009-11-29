package engine;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import net.Protocol;
import world.Agent;
import world.Cell;
import world.RemoteCell;

public class RemoteEngine extends Engine {
	
	Socket socket;
	ObjectInputStream in;
	ObjectOutputStream out;
	LocalEngine localEngine;
	
	public RemoteEngine(Socket socket) {
		this.socket = socket;
		try {
			this.out = new ObjectOutputStream(socket.getOutputStream());
			this.in = new ObjectInputStream(socket.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public RemoteEngine(Socket socket, LocalEngine localEngine) {
		this(socket);
		this.localEngine = localEngine;
	}
	
	public void setEngine(LocalEngine engine) {
		this.localEngine = engine;
	}
	
	@Override
	public Cell findCell(int x, int y) {
		// TODO: Send a 'findCell' request to this remote machine using
		// the message protocol.
		return new RemoteCell(x, y, this);
	}
	
	public void sendAgent(RemoteCell newCell, Agent agent) {
		// TODO: Send a 'sendAgent' request to the remote machine using
		// the message protocol.
		Protocol.sendAgent(out, newCell.getX(), newCell.getY(), agent);
	}
}
