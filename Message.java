import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Comparator;

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
	
	public static Comparator<Message> sendTurnComparator = new Comparator<Message>() {

		public int compare(Message o1, Message o2) {
			if(o1.sendTurn > o2.sendTurn){
				return 1;
			}
			else if(o1.sendTurn < o2.sendTurn){
				return -1;
			}
			else{
				return 0;
			}

		}
		
	};
	
	public static Comparator<Message> recvTurnComparator = new Comparator<Message>() {

		public int compare(Message o1, Message o2) {
			if(o1.recvTurn > o2.recvTurn){
				return 1;
			}
			else if(o1.recvTurn < o2.recvTurn){
				return -1;
			}
			else{
				return 0;
			}

		}
		
	};

	
	private int sendTurn;
	boolean sign;
	private int recvTurn;
	private byte[] data;

	public Message(int sendTurn, boolean sign){
		this.sendTurn = sendTurn;
		this.sign = sign;
	}

	public Message(int recvTurn){
		this.recvTurn = recvTurn;
	}

	private void writeMessage(DataOutputStream dos, byte messageType, int dataSize){
		try{
			dos.writeByte(messageType);
			dos.writeInt(sendTurn);
			dos.writeBoolean(sign);
			dos.writeInt(dataSize);
			dos.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private int readMessage(InputStream is){
		int dataSize = 0;
		try{
			DataInputStream dis = new DataInputStream(is);
			sendTurn = dis.readInt();
			sign = dis.readBoolean();
			dataSize = dis.readInt();
			System.out.println("Read Message: sendTurn =" + sendTurn + " sign " + sign + " dataSize " + dataSize);
		}catch(Exception e){
			e.printStackTrace();
		}
		return dataSize;
	}

	public static void sendOfferHelpReq(OutputStream out) {
		try {
			out.write(OFFERHELP);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void sendOfferHelpResp(OutputStream out, int tlx, int tly, int width, int height,
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

	public static OfferHelpResponse recvOfferHelpResp(InputStream in) {
		OfferHelpResponse r = new OfferHelpResponse();
		try {
			// TODO verify message type
			DataInputStream dis = new DataInputStream(in);
			dis.read();
			r.tlx = dis.readInt();
			r.tly = dis.readInt();
			r.width = dis.readInt();
			r.height = dis.readInt();
			r.globalWidth = dis.readInt();
			r.globalHeight = dis.readInt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	/*
	 * sendAgent: +Request: requestType (1 byte) X (4 bytes) Y (4 bytes)
	 * Agent(serialized) (? bytes)
	 */
	public void sendAgent(OutputStream out, int x, int y, Agent agent){
		try {
			DataOutputStream dos = new DataOutputStream(out);
			byte[] bytes = agent.toBytes();
			System.out.write(bytes);
			System.out.println();
			int messageSize = bytes.length + 4 + 4;
			writeMessage(dos, SENDAGENT, messageSize);
			dos.writeInt(x);
			dos.writeInt(y);
			dos.write(bytes, 0, bytes.length);
			dos.flush();
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ReceivedAgent recvAgent(InputStream in) {
		ReceivedAgent result = null;

		try {
			int dataSize = readMessage(in);
			System.out.println("size:" + dataSize);
			byte[] data = new byte[dataSize];
			int bytesRead = 0;
			do{
				bytesRead += in.read(data, bytesRead, dataSize - bytesRead);
				System.out.println("Read " + bytesRead + " of " + dataSize);
			}while(bytesRead < dataSize);
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
			result = new ReceivedAgent();
			result.x = dis.readInt();
			result.y = dis.readInt();
			result.agent = (Agent) Agent.read(dis);
			this.data = data;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public static void endTurn(OutputStream out, int turn){
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

	public static int endTurn(InputStream in){
		int turn = -1;
		try{
			DataInputStream dis = new DataInputStream(in);
			//TODO: Check message type.
			turn = dis.readInt();
		}catch(Exception e){
			e.printStackTrace();
		}
		return turn;
	}
}
