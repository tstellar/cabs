import java.io.IOException;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectOutputStream;

public class RemoteEngine extends Engine {

	Socket socket;
	InputStream in;
	OutputStream out;
	LocalEngine localEngine;

	public RemoteEngine(Socket socket, InputStream in, OutputStream out, LocalEngine localEngine, int tlx, int tly, int width,
			int height) {
		super(tlx, tly, width, height);
		this.socket = socket;
		this.in = in;
		this.out = out;
		this.localEngine = localEngine;
		System.out.println("Created a new remoteEngine " + tlx + ", " + tly);
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
		Protocol.sendAgent((ObjectOutputStream)out, newCell.x, newCell.y, agent);

	}
}
