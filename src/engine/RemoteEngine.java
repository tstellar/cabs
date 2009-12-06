package engine;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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
	int id;

	public RemoteEngine(Socket socket, int id) {
		this.socket = socket;
		this.id = id;
		try {
			this.out = socket.getOutputStream();
			this.in = socket.getInputStream();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public RemoteEngine(Socket socket, LocalEngine localEngine, int id) {
		this(socket, id);
		this.localEngine = localEngine;
	}

	public void setEngine(LocalEngine engine) {
		this.localEngine = engine;
	}

	public void listen() {
		reader = new MessageReader(localEngine, in);
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
		Message message = new Message(localEngine.turn, true, id);
		message.sendAgent(out, newCell.getX(), newCell.getY(), agent);
		localEngine.storeAntimessage(message);
	}
}
