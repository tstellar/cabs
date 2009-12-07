package engine;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;

import net.Message;
import net.Message.LookRequest;
import net.Message.OfferHelpResponse;
import net.Message.ReceivedAgent;
import ui.CellGrid;
import world.Agent;
import world.Cell;
import world.LocalCell;
import world.impl.*;

public class LocalEngine extends Engine {

	static String PLACEFILE = "engine/agents.txt";
	LocalCell[][] cells;
	ArrayList<RemoteEngine> peerList;
	int globalWidth;
	int globalHeight;
	public int turn = 0;
	boolean rollback = false;
	boolean enableGUI = false;
	HashMap<Integer, ArrayList<byte[]>> states;
	public PriorityQueue<Message> recvdMessages;
	LinkedList<Message> processedMessages;
	PriorityQueue<Message> unackMessages;
	PriorityQueue<Message> antiMessages;
	ServerSocket listenSocket;
	HashMap<Integer, Message.LookResponse> lookResponses;

	CellGrid gui;

	Random random = new Random();

	public LocalEngine(int tlx, int tly, int width, int height,
			int globalWidth, int globalHeight) {
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
		if(enableGUI){
			gui = new CellGrid(this.height, this.width, tlx, tly);
		}
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
					System.out
							.println("Previously processed message annihilated");
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
			System.out.println(re.getID() + " Remote turn is " + re.turn);
			if (re.turn < minTurn) {
				minTurn = re.turn;
			}
		}
		System.out.printf("Min turn= %d\n", minTurn);
		// Remove old states.
		System.out.printf("Current states %d\n", states.size());
		// TODO Is this right?
		int i = minTurn - 1;
		while (i >= 0 && states.remove(i--) != null);
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
				if (turn % 1 == 0) {
					for (int j = 0; j < peerList.size(); j++) {
						System.out.println("ENDTURN to "
								+ peerList.get(j).getID());
						Message
								.sendEndTurn(peerList.get(j).out,
										minLocalTime());
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

	public void placeAgents() {
		Random x = new Random();
		Random y = new Random();
		try{
			if(System.in.available() == 0){
				for(int i=0; i< height; i++){
					placeAgent(0,i,new Rabbit());
				}
				return;
			}
//			File f = new File(LocalEngine.PLACEFILE);
			Scanner s = new Scanner(System.in);
			while(s.hasNext()){
				String className = s.next();
				int number = s.nextInt();
				while(number-- > 0){
					Class agentClass = Class.forName("world.impl." + className);
					Object newAgent = agentClass.newInstance();
					placeAgent(x.nextInt(width), y.nextInt(height),(Agent)newAgent);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void print() {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				LocalCell cell = cells[i][j];
				if (cell.getAgents().size() > 0) {
					System.out.print(cell.getAgents().size() + " ");
					if(enableGUI){
						gui.setColor(j, i, CellGrid.agent1);
					}
				} else {
					System.out.print("- ");
					if(enableGUI){
						gui.setColor(j, i, CellGrid.empty);
					}
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
							System.out.println("Skipping antimessage in queue");
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
					resp.sendMessage(message.source.out);
					break;
				case Message.ENDTURN:
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendCells() {
		/*The Server will be have more cells than the rest of the Engines,
		 * but that is OK.
		 */
		int totalEngines = peerList.size() + 1;
		int rWidth = this.width / totalEngines;
		int rHeight = this.height;
		int rTly = 0;
		/*Calculate the coordinates for all the RemoteEngines.*/
		for(RemoteEngine re : peerList){
			int rTlx = this.width - rWidth;
			this.width -= rWidth;
			re.setCoordinates(rTlx, rTly, rWidth, rHeight);
		}
		/*Send the cells to the RemoteEngines.  The reason we aren't
		 * doing this in the same loop where we calculate the 
		 * coordinates, is because we need to know the server
		 * coordinates to send the OfferHelpResp message.  I am aware
		 * we could calculate it ahead of time, but this way is less
		 * prone to bugs.
		 */
		for(RemoteEngine re: peerList){
			/*Tell the RemoteEngine its coordinates.*/
			Message.sendOfferHelpResp(re.out, re.tlx, re.tly,
			re.width, re.height, globalWidth, globalHeight, tlx, 
							tly, width, height);
			try{System.out.println("send resp");Thread.sleep(2000);}catch(Exception e){}
			/*Send the RemoteEngine its new cells.*/
			for (int i = re.tlx; i < re.width; i++) {
				for (int j = re.tly; j < re.height; j++) {
					LocalCell cell = getCell(i, j);
					for (Agent a : cell.agents) {
						Message message = new Message(
						this.turn, true, re.getID());
						message.sendAgent(cell.getX(), cell.getY(), a);
						message.sendMessage(re.out);
					}
				}
			}
			/*Send the coordinates of the other RemoteEngines.*/
			System.out.println("Sending connections.");
			Message.sendConnections(re.out, peerList);
			System.out.println("done.");
			try{System.out.println("send resp");Thread.sleep(2000);}catch(Exception e){}

		}
		/* Redraw the GUI. */
		if(enableGUI){
			gui.dispose();
			gui = new CellGrid(height, width, tlx, tly);
		}

	}

	public int minLocalTime() {
		final int unprocessedTime = recvdMessages.isEmpty() ? turn
				: recvdMessages.peek().sendTurn;
		final int unackTime = unackMessages.isEmpty() ? turn : unackMessages
				.peek().sendTurn;
		System.out.println("Unprocessed time: " + unprocessedTime
				+ "; unack time: " + unackTime);
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
		int waitTime = 5000;
		LocalEngine engine = null;
		boolean isClient = false;
		try {

			// Client case
			if (args.length == 1) {
				
				isClient = true;
				InetAddress other = InetAddress.getByName(args[0]);
				/*Connect to the server.*/
				Socket serverSend = new Socket(other, port);
				/*Create a socket to listen for messages sent
				 * by the server.*/
				ServerSocket listenSocket = new ServerSocket(0);
				/*Alert the server of our presence.*/
				System.out.println("Listening on " + listenSocket.getInetAddress().getHostAddress());
				Message.sendOfferHelpReq(serverSend.getOutputStream(), listenSocket.getInetAddress(), listenSocket.getLocalPort());
				/*Listen for the server's response.*/
				System.out.println("Waiting for server.");
				Socket serverRecv = listenSocket.accept();
				System.out.println("Found Server.");
				RemoteEngine server = new RemoteEngine(serverSend, serverRecv);
				System.out.println("Waiting for server to accept help.");
				OfferHelpResponse r = Message.recvOfferHelpResp(server.in);
				engine = new LocalEngine(r.getTlx(), r.getTly(), r.getWidth(),
						r.getHeight(), r.getGlobalWidth(), r.getGlobalHeight());
				engine.listenSocket = listenSocket;
				server.setEngine(engine);
				engine.peerList.add(server);
				server.setCoordinates(r.sendertlx, r.sendertly, r.senderw,
						r.senderh);
				System.out.println("Waiting for server to send connections.");
				ArrayList<Message.ConnectInfo> connections = Message.recvConnections(server.in);
				
				/*Create RemoteEngine objects and start listening for messages.*/
				for(Message.ConnectInfo c : connections){
					if(c.tlx == engine.tlx && c.tly == engine.tly){
						System.out.printf("Skipping %d,%d\n",engine.tlx, engine.tly);
						continue;
					}
					RemoteEngine re = new RemoteEngine(engine,  c.addr, c.port);
					re.setCoordinates(c.tlx, c.tly, c.width, c.height);
					engine.peerList.add(re);
					System.out.println("Calling listen.");
					re.listen();
					Socket peerSocket = new Socket(c.addr, c.port);
					re.setSendSocket(peerSocket);
				}
				System.out.println("Calling listen.");
				/*Listen for messages from the server.*/
				server.listen();
			}

			// Server case
			else {
				engine = new LocalEngine(0, 0, globalWidth, globalHeight,
						globalWidth, globalHeight);
				engine.placeAgents();
				engine.listenSocket = new ServerSocket(port);
				System.out.println("Listening on " + engine.listenSocket.getInetAddress().getHostAddress());
				engine.listenSocket.setSoTimeout(waitTime);
				while (true) {
					Socket clientRecv;
					try {
						clientRecv = engine.listenSocket.accept();
						System.out.println("Heard a socket.");
					} catch (SocketTimeoutException e) {
						break;
					}
					/*Wait for an offerHelp Request*/
					if (clientRecv.getInputStream().read() != Message.OFFERHELP){
						System.err.println("Bad message, disconnecting socket.");
						clientRecv.close();
						break;
					}
					Message.OfferHelpReq help = Message.recvOfferHelpReq(clientRecv.getInputStream());
					System.out.println("Connecting to " + help.addr.getHostAddress());
					Socket clientSend = new Socket(help.addr.getHostAddress(), help.port);
					RemoteEngine client = new RemoteEngine(clientSend, clientRecv);
					client.setEngine(engine);
					engine.peerList.add(client);
					client.listen();
				}
				// TODO: Use a smart algorithm to figure out what
				// coordinates to assign the other node.
				engine.sendCells();

				// We probably need some kind of ACK here.

			}
			engine.print();
			engine.go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
