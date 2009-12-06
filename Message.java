import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
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
			if (o1.sendTurn > o2.sendTurn) {
				return 1;
			} else if (o1.sendTurn < o2.sendTurn) {
				return -1;
			} else {
				return 0;
			}

		}

	};

	// This compares things in the opposite order so that messages in the
	// priority queue will be sorted correctly.
	public static Comparator<Message> reverseSendTurnComparator = new Comparator<Message>() {

		public int compare(Message o1, Message o2) {
			if (o1.sendTurn > o2.sendTurn) {
				return -1;
			} else if (o1.sendTurn < o2.sendTurn) {
				return 1;
			} else {
				return 0;
			}

		}

	};

	public void print() {
		System.out.println("sendTurn: " + sendTurn + " messageType: " + messageType + " sign: " + sign + " data: " + data);
	}
	
	@Override
	public boolean equals(Object other) {
		boolean result = false;
		System.err.println("Message.equals called");
		if (other instanceof Message) {
			Message otherMsg = (Message) other;
			this.print();
			otherMsg.print();
			result = ((this.sendTurn == otherMsg.sendTurn)
					&& (this.messageType == otherMsg.messageType)
					&& (this.sign != otherMsg.sign) && Arrays.equals(this.data,
					otherMsg.data));
		}
		System.out.println("Result of equals: " + result);
		return result;
		}

	int sendTurn;
	boolean sign;
	private int recvTurn;
	public int messageType;
	private byte[] data;
	int id;

	public Message(int sendTurn, boolean sign, int id) {
		this.sendTurn = sendTurn;
		this.sign = sign;
		this.id = id;
	}

	public Message(int recvTurn, int messageType) {
		this.recvTurn = recvTurn;
		this.messageType = messageType;
	}

	private void writeMessage(DataOutputStream dos, byte messageType,
			int dataSize) {
		try {
			this.messageType = messageType;
			dos.writeByte(messageType);
			dos.writeInt(sendTurn);
			dos.writeBoolean(sign);
			dos.writeInt(dataSize);
			dos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int readMessage(InputStream is) {
		int dataSize = 0;
		try {
			DataInputStream dis = new DataInputStream(is);
			sendTurn = dis.readInt();
			sign = dis.readBoolean();
			dataSize = dis.readInt();
			System.out.println("Read Message: sendTurn =" + sendTurn + " sign "
					+ sign + " dataSize " + dataSize);
		} catch (Exception e) {
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

	public static void sendOfferHelpResp(OutputStream out, int tlx, int tly,
			int width, int height, int globalWidth, int globalHeight) {
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

	public void sendMessage(OutputStream out) {
		DataOutputStream dos = new DataOutputStream(out);
		writeMessage(dos, (byte) this.messageType, data.length);
		try {
			dos.write(data, 0, data.length);
			dos.flush();
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * sendAgent: +Request: requestType (1 byte) X (4 bytes) Y (4 bytes)
	 * Agent(serialized) (? bytes)
	 */
	public void sendAgent(OutputStream out, int x, int y, Agent agent) {
		try {
			DataOutputStream dos = new DataOutputStream(out);
			byte[] agentBytes = agent.toBytes();
			int messageSize = agentBytes.length + 4 + 4;
			ByteBuffer buffer = ByteBuffer.allocate(messageSize);
			writeMessage(dos, SENDAGENT, messageSize);
			buffer.putInt(x);
			buffer.putInt(y);
			buffer.put(agentBytes);
			byte[] bytes = buffer.array();
			this.data = bytes;
			dos.write(bytes, 0, bytes.length);
			dos.flush();
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ReceivedAgent recvAgent() {
		ReceivedAgent result = null;
		try {
			result = new ReceivedAgent();
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
					data));
			result.x = dis.readInt();
			result.y = dis.readInt();
			result.agent = (Agent) Agent.read(dis);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public void recvAgent(InputStream in) {

		try {
			int dataSize = readMessage(in);
			System.out.println("size:" + dataSize);
			data = new byte[dataSize];
			int bytesRead = 0;
			do {
				bytesRead += in.read(data, bytesRead, dataSize - bytesRead);
				System.out.println("Read " + bytesRead + " of " + dataSize);
			} while (bytesRead < dataSize);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void endTurn(OutputStream out, int turn) {
		try {
			ByteBuffer data = ByteBuffer.allocate(5);
			data.put(ENDTURN);
			data.putInt(turn);
			out.write(data.array());
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int endTurn(InputStream in) {
		int turn = -1;
		try {
			DataInputStream dis = new DataInputStream(in);
			// TODO: Check message type.
			turn = dis.readInt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return turn;
	}
}
