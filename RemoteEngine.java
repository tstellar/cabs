import java.io.IOException;
import java.net.Socket;

public class RemoteEngine extends Engine {

	Socket socket;
	LocalEngine localEngine;

	public RemoteEngine(Socket socket, LocalEngine localEngine, int tlx, int tly, int width,
			int height) {
		super(tlx, tly, width, height);
		this.socket = socket;
		this.localEngine = localEngine;
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
		try {
			Protocol.sendAgent(socket.getOutputStream(), newCell.x, newCell.y, agent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
