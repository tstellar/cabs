package engine;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;

import net.Message;
import net.Message.LookRequest;
import net.Message.OfferHelpResponse;
import net.Message.ReceivedAgent;
import ui.CellGrid;
import world.Agent;
import world.Cell;
import world.LocalCell;
import world.impl.Rabbit;

public class LocalEngine extends Engine {

	LocalCell[][] cells;
	ArrayList<RemoteEngine> peerList;
	int globalWidth;
	int globalHeight;
	public int turn = 0;
	boolean rollback = false;
	HashMap<Integer, ArrayList<byte[]>> states;
	public PriorityQueue<Message> recvdMessages;
	LinkedList<Message> processedMessages;
	PriorityQueue<Message> unackMessages;
	PriorityQueue<Message> antiMessages;
	HashMap<Integer, Message.LookResponse> lookResponses;

	CellGrid gui;

	Random random = new Random();

	public LocalEngine(int tlx, int tly, int width, int height, int globalWidth, int globalHeight) {
		super(tlx, tly, width, height);
		this.states = new HashMap<Integer, ArrayList<byte[]>>();
		this.recvdMessages = new PriorityQueue<Message>(8, Message.sendTurnComparator);
		this.antiMessages = new PriorityQueue<Message>(8, Message.reverseSendTurnComparator);
		this.unackMessages = new PriorityQueue<Message>(8, Message.sendTurnComparator);
		this.processedMessages = new LinkedList<Message>();
		this.lookResponses = new HashMap<Integer, Message.LookResponse>();
		this.globalWidth = globalWidth;
		this.globalHeight = globalHeight;
		peerList = new ArrayList<RemoteEngine>();
		cells = new LocalCell[height][width];
		gui = new CellGrid(this.height, this.width, tlx, tly);
		for (int i = 0; i < this.height; i++) {
			for (int j = 0; j < this.width; j++) {
				cells[i][j] = new LocalCell(tlx + j, tly + i, this);
			}
		}
	}

	private void saveState() {

		ArrayList<byte[]> newState = new ArrayList<byte[]>();
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				LocalCell cell = cells[i][j];
				newState.add(cell.serialize());
			}
		}
		states.put(turn, newState);
	}

	private void rollback(int turn) {
		System.err.println("Rolling back from turn " + this.turn + " to turn " + turn);
		rollback = true;
		ArrayList<byte[]> state = states.get(turn);
		for (byte[] b : state) {
			// System.err.println("The byte array is of length " + b.length);
			ByteArrayInputStream s = new ByteArrayInputStream(b);
			try {
				DataInputStream dis = new DataInputStream(s);
				int x = dis.readInt();
				int y = dis.readInt();
				int count = dis.readInt();
				/*
				 * System.err.println(MessageFormat.format(
				 * "Rolling back cell ({0}, {1}); {2} agents.", x, y, count));
				 */
				LocalCell cell = getCell(x, y);

				cell.agents.clear();
				while (count-- != 0) {
					cell.add(Agent.read(dis));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Put rolled-back events back onto the incoming queue
		for (Message m : processedMessages) {
			if (m.sendTurn >= turn) {
				if (!recvdMessages.remove(m)) {
					recvdMessages.offer(m);
				} else {
					System.out.println("Previously processed message annihilated");
				}
			}
		}

		// Send antimessages
		while (!this.antiMessages.isEmpty() && antiMessages.peek().sendTurn >= turn) {
			Message msg = antiMessages.poll();
			RemoteEngine remote = getPeer(msg.id);
			System.out.println("Trying to send antimessage to: " + msg.id);
			msg.print();
			storeUnack(msg);
			if (remote == null) {
				System.out.println("Can't find who to send antimessage to: " + msg.id);
			} else {
				msg.sendMessage(remote.out);
			}
		}

		this.turn = turn;
	}

	public void sendMessage(Message message, OutputStream o) {
		this.storeUnack(message);
		message.sendMessage(o);
		this.storeAntimessage(message);
	}

	public RemoteEngine getPeer(String id) {
		System.out.println("I am " + getID() + " Getting peer: " + id);
		for (RemoteEngine re : peerList) {
			if (re.getID().equals(id)) {
				System.out.println("Found it: " + re);
				return re;
			} else {
				System.out.println("It's not: " + re + " which has id " + re.getID());
			}
		}
		System.out.println("Failed to find peer " + id);
		return null;
	}

	private void fossilCollect() {
		int minTurn = minLocalTime();
		for (RemoteEngine re : peerList) {
			System.out.println("Remote turn is " + re.turn);
			if (re.turn < minTurn) {
				minTurn = re.turn;
			}
		}
		System.out.printf("Min turn= %d\n", minTurn);
		// Remove old states.
		System.out.printf("Current states %d\n", states.size());
		// TODO Is this right?
		int i = minTurn - 1;
		while (i >= 0 && states.remove(i--) != null) {
			;
		}
		System.out.printf("New states %d\n", states.size());

	}

	public void go() {

		while (true) {
			while (turn < 50) {
				if (!rollback) {
					turn++;
					saveState();
				}

				/*
				 * try { Thread.sleep(25); } catch (InterruptedException e) {
				 * e.printStackTrace(); }
				 */
				System.out.println("Starting turn " + turn);
				for (LocalCell[] cell : cells) {
					for (LocalCell element : cell) {
						element.resetAgents();
					}
				}

				for (LocalCell[] cell : cells) {
					for (LocalCell element : cell) {
						element.go(turn);
					}
				}
				rollback = false;
				if (turn % 5 == 0) {
					for (int j = 0; j < peerList.size(); j++) {
						System.out.println("ENDTURN to " + peerList.get(j).getID());
						Message.sendEndTurn(peerList.get(j).out, minLocalTime());
					}
				}
				handleMessages();
				fossilCollect();
				System.out.println("At the end of turn  " + turn + " the grid is:");
				print();
			}
			handleMessages();
		}
	}

	public void moveAgent(Agent agent, LocalCell oldCell, int x, int y) {
		Cell newCell = findCell(oldCell.getX() + x, oldCell.getY() + y);
		newCell.add(agent);
		oldCell.remove(agent);
	}

	private Cell findRemoteCell(int x, int y) {
		for (int i = 0; i < peerList.size(); i++) {
			if (peerList.get(i).hasCell(x, y))
				return peerList.get(i).findCell(x, y);
		}
		System.err.println("Didn't find remote cell: " + x + ", " + y);
		return null;
	}

	@Override
	public Cell findCell(int x, int y) {
		if (y >= globalHeight) {
			y = y % globalHeight;
		}
		if (x >= globalWidth) {
			x = x % globalWidth;
		}
		if (y < 0) {
			y = (y % globalHeight) + globalHeight;
		}
		if (x < 0) {
			x = (x % globalWidth) + globalWidth;
		}
		if (hasCell(x, y))
			return getCell(x, y);
		else
			return findRemoteCell(x, y);
	}

	public LocalCell getCell(int x, int y) {
		return cells[y - tly][x - tlx];
	}

	public void placeAgent(int x, int y, Agent agent) {
		LocalCell cell = getCell(x, y);
		cell.add(agent);
	}

	public void placeAgents(int agents) {
		for (int i = 0; i < agents; i++) {
			LocalCell cell = getCell(0, i);
			cell.add(new Rabbit());
		}
	}

	public void print() {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				LocalCell cell = cells[i][j];
				if (cell.getAgents().size() > 0) {
					System.out.print("* ");
					gui.setColor(j, i, CellGrid.agent1);
				} else {
					System.out.print("- ");
					gui.setColor(j, i, CellGrid.empty);
				}
			}
			System.out.println();
		}
	}

	private void handleMessages() {

		try {
			// It is OK to check if recvdMessages is empty without
			// synchronizing,
			// because this has no effect on the process adding things to it.
			// System.out.println("Queue size =" + recvdMessages.size());
			while (!recvdMessages.isEmpty()) {
				Message message = null;
				Boolean needRollback = false;
				synchronized (recvdMessages) {
					message = recvdMessages.peek();
					if (message.sendTurn > this.turn) {
						break;
					}
					if (message.sendTurn < this.turn) {
						needRollback = true;
					} else {
						message = recvdMessages.poll();
						if (message.sign == false) {
							processedMessages.add(message);
							continue;
						}
					}
				}
				if (needRollback) {
					rollback(message.sendTurn);
					return;
				}
				switch (message.messageType) {
				case Message.SENDAGENT:
					ReceivedAgent newAgent = message.recvAgent();
					this.placeAgent(newAgent.x, newAgent.y, newAgent.agent);
					this.processedMessages.add(message);
					break;
				case Message.LOOK:
					LookRequest lreq = message.recvLookRequest();
					Collection<? extends Agent> agents = getCell(lreq.x, lreq.y).listAgents();
					Message resp = new Message(turn, true, message.source.getID());
					resp.lookResponse(lreq.id, agents);
					this.sendMessage(resp, message.source.out);
					this.processedMessages.add(message);
					break;
				case Message.ENDTURN:
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendCells(RemoteEngine remote) {
		// TODO: Send agents along with cells.
		int rWidth = this.width / 2;
		int rHeight = this.height;
		int rTlx = this.width - rWidth;
		int rTly = 0;

		this.width = this.width - rWidth;
		Message.sendOfferHelpResp(remote.out, rTlx, rTly, rWidth, rHeight, globalWidth,
				globalHeight, tlx, tly, width, height);
		for (int i = rTlx; i < rWidth; i++) {
			for (int j = rTly; j < rHeight; j++) {
				LocalCell cell = getCell(i, j);
				for (Agent a : cell.agents) {
					Message message = new Message(this.turn, true, remote.getID());
					message.sendAgent(cell.getX(), cell.getY(), a);
					message.sendMessage(remote.out);
				}
			}
		}
		remote.setCoordinates(rTlx, rTly, rWidth, rHeight);
		this.peerList.add(remote);
		// TODO: Actually change the size of the data structure that
		// holds the cells.
		gui.dispose();
		gui = new CellGrid(height, width, tlx, tly);

	}

	public int minLocalTime() {
		final int unprocessedTime = recvdMessages.isEmpty() ? turn : recvdMessages.peek().sendTurn;
		final int unackTime = unackMessages.isEmpty() ? turn : unackMessages.peek().sendTurn;
		System.out.println("Unprocessed time: " + unprocessedTime + "; unack time: " + unackTime);
		return Math.min(Math.min(unprocessedTime, unackTime), turn);
	}

	public void storeAntimessage(Message message) {
		message.sign = false;
		synchronized (antiMessages) {
			antiMessages.offer(message);
		}
	}

	public void storeUnack(Message message) {
		Message m = (Message) message.clone();
		m.sign = !m.sign;
		synchronized (unackMessages) {
			unackMessages.offer(m);
		}
	}

	public Collection<? extends Agent> look(int x, int y) {
		Cell c = this.findCell(x, y);
		return c.listAgents();
	}

	public static void main(String[] args) {

		int globalWidth = 10;
		int globalHeight = 10;
		int port = 1234;
		LocalEngine engine = null;
		boolean isClient = false;
		try {

			// Client case
			if (args.length == 1) {
				isClient = true;
				// Use multicast instead.
				InetAddress other = InetAddress.getByName(args[0]);
				Socket socket = new Socket(other, port);
				// TODO Remove magic number.
				RemoteEngine server = new RemoteEngine(socket);
				Message.sendOfferHelpReq(server.out);
				OfferHelpResponse r = Message.recvOfferHelpResp(server.in);
				engine = new LocalEngine(r.getTlx(), r.getTly(), r.getWidth(), r.getHeight(), r
						.getGlobalWidth(), r.getGlobalHeight());
				server.setEngine(engine);
				engine.peerList.add(server);
				server.setCoordinates(r.sendertlx, r.sendertly, r.senderw, r.senderh);
				server.listen();
				// TODO: Get agents from server.
			}

			// Server case
			else {
				// TODO: Don't hard code everything.
				engine = new LocalEngine(0, 0, globalWidth, globalHeight, globalWidth, globalHeight);
				ServerSocket serverSocket = new ServerSocket(port);
				Socket clientSocket = serverSocket.accept();
				// TODO Remove magic number.
				RemoteEngine client = new RemoteEngine(clientSocket, engine);
				// This is to read the offerHelpReq message. This
				// should be in a method.
				if (client.in.read() != Message.OFFERHELP)
					throw new Exception("Expected offer help request.");
				client.listen();
				// TODO: Use a smart algorithm to figure out what
				// coordinates to assign the other node.
				engine.sendCells(client);

				// We probably need some kind of ACK here.

				engine.placeAgents(10);

			}
			engine.print();
			engine.go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
