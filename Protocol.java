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
	private static byte SENDAGENT = 0x2;

	public static void offerHelpReq(OutputStream out) {
		try {
			out.write(OFFERHELP);
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

	public static OfferHelpResponse offerHelpResp(InputStream in) {
		OfferHelpResponse r = new OfferHelpResponse();
		try {
			// TODO verify message type
			in.read();
			DataInputStream data = new DataInputStream(in);
			r.tlx = data.readInt();
			r.tly = data.readInt();
			r.width = data.readInt();
			r.height = data.readInt();
			r.globalWidth = data.readInt();
			r.globalHeight = data.readInt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	/*
	 * sendAgent: +Request: requestType (1 byte) X (4 bytes) Y (4 bytes)
	 * Agent(serialized) (? bytes)
	 */
	public static void sendAgent(OutputStream out, int x, int y, Agent agent) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeByte(SENDAGENT);
			oos.writeInt(x);
			oos.writeInt(y);
			oos.writeObject(agent);
			oos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ReceivedAgent sendAgent(InputStream in) {
		ReceivedAgent result = null;

		try {
			// in.read();
			ObjectInputStream oin = new ObjectInputStream(in);
			oin.readByte();
			result = new ReceivedAgent();
			result.x = oin.readInt();
			result.y = oin.readInt();
			result.agent = (Agent) oin.readObject();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
}
