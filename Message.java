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

public class Message {

	public static final byte OFFERHELP = 0x1;
	public static final byte SENDAGENT = 0x2;
	public static final byte ENDTURN = 0x3;

	private int sendTurn;
	boolean sign;
	private int recvTurn;

	public Message(int sendTurn, boolean sign){
		this.sendTurn = sendTurn;
		this.sign = sign;
	}

	public Message(int recvTurn){
		this.recvTurn = recvTurn;
	}
	
	private void writeMessage(ObjectOutputStream oos, byte messageType){
		try{
			oos.writeByte(messageType);
			oos.writeInt(sendTurn);
			oos.writeBoolean(sign);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void readMessage(ObjectInputStream ois){
		try{
			sendTurn = ois.readInt();
			sign = ois.readBoolean();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void sendOfferHelpReq(ObjectOutputStream out) {
		try {
			out.write(OFFERHELP);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void sendOfferHelpResp(ObjectOutputStream out, int tlx, int tly, int width, int height,
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

	public static OfferHelpResponse recvOfferHelpResp(ObjectInputStream in) {
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
	public void sendAgent(ObjectOutputStream out, int x, int y, Agent agent) {
		try {
			writeMessage(out, SENDAGENT);
			out.writeInt(x);
			out.writeInt(y);
			out.writeObject(agent);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ReceivedAgent recvAgent(ObjectInputStream in) {
		ReceivedAgent result = null;

		try {
			readMessage(in);
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

	public static void endTurn(ObjectOutputStream out, int turn){
		try{
			ByteBuffer data = ByteBuffer.allocate(5);
			data.put(ENDTURN);
			data.putInt(turn);
			out.write(data.array());
			out.flush();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public static int endTurn(ObjectInputStream in){
		int turn = -1;
		try{
			//TODO: Check message type.
			turn = in.readInt();
		}catch(Exception e){
			e.printStackTrace();
		}
		return turn;
	}
}
