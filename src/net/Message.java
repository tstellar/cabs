package net;
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

import world.Agent;

public class Message {
	
	public static class OfferHelpResponse {
		private int tlx;
		private int tly;
		private int width;
		private int height;
		private int globalWidth;
		private int globalHeight;
		public int sendertlx;
		public int sendertly;
		public int senderw;
		public int senderh;
		public void setTlx(int tlx) {
			this.tlx = tlx;
		}
		public int getTlx() {
			return tlx;
		}
		public void setTly(int tly) {
			this.tly = tly;
		}
		public int getTly() {
			return tly;
		}
		public void setWidth(int width) {
			this.width = width;
		}
		public int getWidth() {
			return width;
		}
		public void setHeight(int height) {
			this.height = height;
		}
		public int getHeight() {
			return height;
		}
		public void setGlobalWidth(int globalWidth) {
			this.globalWidth = globalWidth;
		}
		public int getGlobalWidth() {
			return globalWidth;
		}
		public void setGlobalHeight(int globalHeight) {
			this.globalHeight = globalHeight;
		}
		public int getGlobalHeight() {
			return globalHeight;
		}
	}
	
	public static class ReceivedAgent {
		private int x;
		private int y;
		private Agent agent;
		
		@Override
		public String toString() {
			return getX() + ", " + getY() + getAgent().toString();
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getX() {
			return x;
		}

		public void setY(int y) {
			this.y = y;
		}

		public int getY() {
			return y;
		}

		public void setAgent(Agent agent) {
			this.agent = agent;
		}

		public Agent getAgent() {
			return agent;
		}
	}
	
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

	public int sendTurn;
	public boolean sign;
	private int recvTurn;
	public int messageType;
	private byte[] data;
	public int id;

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
			int width, int height, int globalWidth, int globalHeight,
			int sendertlx, int sendertly, int senderw, int senderh) {
		try {
			DataOutputStream dos = new DataOutputStream(out);
			dos.write(OFFERHELP);
			dos.writeInt(tlx);
			dos.writeInt(tly);
			dos.writeInt(width);
			dos.writeInt(height);
			dos.writeInt(globalWidth);
			dos.writeInt(globalHeight);
			dos.writeInt(sendertlx);
			dos.writeInt(sendertly);
			dos.writeInt(senderw);
			dos.writeInt(senderh);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static OfferHelpResponse recvOfferHelpResp(InputStream in) {
		OfferHelpResponse r = new OfferHelpResponse();
		try {
			// TODO verify message type
			in.read();
			DataInputStream dis = new DataInputStream(in);
			r.setTlx(dis.readInt());
			r.setTly(dis.readInt());
			r.setWidth(dis.readInt());
			r.setHeight(dis.readInt());
			r.setGlobalWidth(dis.readInt());
			r.setGlobalHeight(dis.readInt());
			r.sendertlx = dis.readInt();
			r.sendertly = dis.readInt();
			r.senderw = dis.readInt();
			r.senderh = dis.readInt();
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
			result.setX(dis.readInt());
			result.setY(dis.readInt());
			result.setAgent(Agent.read(dis));
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
