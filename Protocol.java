import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

class OfferHelpResponse {
	int tlx;
	int tly;
	int width;
	int height;
	int globalWidth;
	int globalHeight;
}

class ReceivedAgent {
	int x;
	int y;
	Agent agent;

	@Override
	public String toString() {
		return x + ", " + y + agent.toString();
	}
}

public class Protocol {

	private static byte OFFERHELP = 0x1;
	public static final byte SENDAGENT = 0x2;
	private static byte STARTTURN = 0x3;

	public static void offerHelpReq(OutputStream out) {
		try {
			out.write(OFFERHELP);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void offerHelpResp(OutputStream out, int tlx, int tly, int width, int height,
			int globalWidth, int globalHeight) {
		try {
			ByteBuffer data = ByteBuffer.allocate(25);
			data.put(OFFERHELP);
			data.putInt(tlx);
			data.putInt(tly);
			data.putInt(width);
			data.putInt(height);
			data.putInt(globalWidth);
			data.putInt(globalHeight);
			out.write(data.array());
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static OfferHelpResponse offerHelpResp(ObjectInputStream in) {
		OfferHelpResponse r = new OfferHelpResponse();
		try {
			// TODO verify message type
			in.read();
			r.tlx = in.readInt();
			r.tly = in.readInt();
			r.width = in.readInt();
			r.height = in.readInt();
			r.globalWidth = in.readInt();
			r.globalHeight = in.readInt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	/*
	 * sendAgent: +Request: requestType (1 byte) X (4 bytes) Y (4 bytes)
	 * Agent(serialized) (? bytes)
	 */
	public static void sendAgent(ObjectOutputStream out, int x, int y, Agent agent) {
		try {
			ObjectOutputStream oos = out;
			oos.writeByte(SENDAGENT);
			oos.writeInt(x);
			oos.writeInt(y);
			oos.writeObject(agent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ReceivedAgent sendAgent(ObjectInputStream in) {
		ReceivedAgent result = null;

		try {
			result = new ReceivedAgent();
			result.x = in.readInt();
			result.y = in.readInt();
			result.agent = (Agent) in.readObject();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public static void startTurn(OutputStream out, int turn){
		try{
			ByteBuffer data = ByteBuffer.allocate(5);
			data.put(STARTTURN);
			data.putInt(turn);
			out.write(data.array());

		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public static int startTurn(InputStream in){
		int turn = -1;
		try{
			//TODO: Check message type.
			in.read();
			DataInputStream data = new DataInputStream(in);
			turn = data.readInt();
		}catch(Exception e){
			e.printStackTrace();
		}
		return turn;
	}
}
