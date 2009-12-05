package net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import world.Agent;

public class Message {

	public static class OfferHelpResponse {
		private int tlx;
		private int tly;
		private int width;
		private int height;
		private int globalWidth;
		private int globalHeight;

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

	private int sendTurn;
	boolean sign;
	private int recvTurn;

	public Message(int sendTurn, boolean sign) {
		this.sendTurn = sendTurn;
		this.sign = sign;
	}

	public Message(int recvTurn) {
		this.recvTurn = recvTurn;
	}

	private void writeMessage(ObjectOutputStream oos, byte messageType) {
		try {
			oos.writeByte(messageType);
			oos.writeInt(sendTurn);
			oos.writeBoolean(sign);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readMessage(ObjectInputStream ois) {
		try {
			sendTurn = ois.readInt();
			sign = ois.readBoolean();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeMessage(DataOutputStream oos, byte messageType, int dataSize) {
		try {
			oos.writeByte(messageType);
			oos.writeInt(sendTurn);
			oos.writeBoolean(sign);
			oos.writeInt(dataSize);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int readMessage(DataInputStream ois) {
		try {
			sendTurn = ois.readInt();
			sign = ois.readBoolean();
			return ois.readInt();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static void sendOfferHelpReq(DataOutputStream out) {
		try {
			out.write(OFFERHELP);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void sendOfferHelpResp(DataOutputStream out, int tlx, int tly, int width,
			int height, int globalWidth, int globalHeight) {
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

	public static OfferHelpResponse recvOfferHelpResp(DataInputStream in) {
		OfferHelpResponse r = new OfferHelpResponse();
		try {
			// TODO verify message type
			in.read();
			r.setTlx(in.readInt());
			r.setTly(in.readInt());
			r.setWidth(in.readInt());
			r.setHeight(in.readInt());
			r.setGlobalWidth(in.readInt());
			r.setGlobalHeight(in.readInt());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	/*
	 * sendAgent: +Request: requestType (1 byte) X (4 bytes) Y (4 bytes)
	 * Agent(serialized) (? bytes)
	 */
	public void sendAgent(DataOutputStream out, int x, int y, Agent agent) {
		try {
			byte[] bytes = agent.toBytes();
			int messageSize = bytes.length + 4 + 4 + 4;
			writeMessage(out, SENDAGENT, messageSize);
			out.writeInt(x);
			out.writeInt(y);
			out.write(bytes, 0, bytes.length);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ReceivedAgent recvAgent(DataInputStream in) {
		ReceivedAgent result = null;
		
		try {
			int dataSize = readMessage(in);
			byte[] data = in.read
			result = new ReceivedAgent();
			result.x = in.readInt();
			result.y = in.readInt();
			result.agent = Agent.read(in);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}

	public static void endTurn(DataOutputStream out, int turn) {
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

	public static int endTurn(DataInputStream in) {
		int turn = -1;
		try {
			// TODO: Check message type.
			turn = in.readInt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return turn;
	}
}
