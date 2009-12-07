package engine;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;

import net.Message;
import world.Agent;
import world.Cell;
import world.RemoteCell;

public class RemoteEngine extends Engine {

	Socket socket;
	InputStream in;
	OutputStream out;
	LocalEngine localEngine;
	MessageReader reader;
	Thread readerThread;

	int id = 0;

	public RemoteEngine(Socket socket) {
		this.socket = socket;
		try {
			this.out = socket.getOutputStream();
			this.in = socket.getInputStream();

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

	public void listen() {
		reader = new MessageReader(localEngine, this);
		readerThread = new Thread(reader);
		readerThread.start();
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
		Message message = new Message(localEngine.turn, true, getID());
		message.sendAgent(newCell.getX(), newCell.getY(), agent);
		localEngine.sendMessage(message, out);
	}

	public Collection<? extends Agent> getRemoteAgents(int x, int y) {
		System.out.println("Looking at remote agents");
		Collection<? extends Agent> agents = null;
		int msgId = id++;
		Message message = new Message(localEngine.turn, true, getID());
		message.lookRequest(x, y, msgId);
		localEngine.sendMessage(message, out);
		while (!localEngine.lookResponses.containsKey(msgId)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		synchronized (localEngine.lookResponses) {
			agents = localEngine.lookResponses.remove(msgId).agents;
		}
		return agents;
	}
}
