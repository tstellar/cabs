import java.io.IOException;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;

public class RemoteEngine extends Engine {

	Socket socket;
	DataInputStream in;
	DataOutputStream out;
	LocalEngine localEngine;

	public RemoteEngine(Socket socket){
		this.socket = socket;
		try{
			this.out = new DataOutputStream(socket.getOutputStream());
			this.in = new DataInputStream(socket.getInputStream());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public RemoteEngine(Socket socket, LocalEngine localEngine) {
		this(socket);
		this.localEngine = localEngine;
	}

	public void setEngine(LocalEngine engine){
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
		Message message = new Message(localEngine.turn, true);
		message.sendAgent(out, newCell.x, newCell.y, agent);
	}
}
