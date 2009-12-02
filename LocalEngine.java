import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.nio.channels.SocketChannel;

public class LocalEngine extends Engine {

	LocalCell[][] cells;
	ArrayList<RemoteEngine> peerList;
	int globalWidth;
	int globalHeight;
	int turn = 0;
	boolean rollback = false;
	HashMap<Integer, ArrayList<byte[]>> states;
	CellGrid gui;

	public LocalEngine(int tlx, int tly, int width, int height, int globalWidth, int globalHeight) {
		super(tlx, tly, width, height);
		this.states = new HashMap<Integer, ArrayList<byte[]>>();
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
	
	private void saveState(){
		
		ArrayList<byte[]> newState = new ArrayList<byte[]>();
		for(int i=0; i< height; i++){
			for(int j=0; j< width; j++){
				LocalCell cell = cells[i][j];
				newState.add(cell.serialize());
			}
		}
		states.put(turn, newState);
	}
	
	private void rollback(int turn){
		//TODO: Send off anti-message queue.
		rollback = true;
		ArrayList<byte[]> state = states.get(turn);
		for( byte[] b : state){
			//System.err.println("The byte array is of length " + b.length);
			ByteArrayInputStream s = new ByteArrayInputStream(b);
			try {
				ObjectInputStream ois = new ObjectInputStream(s);
				int x = ois.readInt();
				int y = ois.readInt();
				int count = ois.readInt();
				/*System.err.println(MessageFormat.format(
						"Rolling back cell ({0}, {1}); {2} agents.", x,
						y, count));*/
				LocalCell cell = getCell(x,y);
				cell.agents.clear();
				while(count-- != 0){
					cell.add((Agent)ois.readObject());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.turn = turn;
	}

	public void go() {
		
		while(turn < 20){
			//TODO: Remove this only for testing
			if(turn == 10){
				rollback(3);
			}
			
			if(!rollback){
				turn++;
				saveState();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Starting turn " + turn);
			for(LocalCell[] cell : cells) {
				for(LocalCell element: cell){
					element.resetAgents();
				}
			}
			
			for (LocalCell[] cell : cells) {
				for (LocalCell element : cell) {
					element.go(turn);
				}
			}
			rollback = false;
			for(int j=0;j<peerList.size();j++){
				Protocol.endTurn(peerList.get(j).out, turn);
			}
			handleMessages();
			print();
		}
	}

	public void moveAgent(Agent agent, LocalCell oldCell, int x, int y) {
		Cell newCell = findCell(oldCell.x + x, oldCell.y + y);
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
		if (hasCell(x, y)) {
			return getCell(x ,y);
		} else {
			return findRemoteCell(x, y);
		}
	}

	public LocalCell getCell(int x, int y){
		return cells[y - tly][x - tlx];
	}

	public void placeAgent(int x, int y, Agent agent) {
		LocalCell cell = getCell(x, y);
		cell.add(agent);
	}

	public void placeAgents(int agents) {
		for (int i = 0; i < agents; i++) {
			LocalCell cell = getCell(0,i);
			cell.add(new Rabbit());
		}
	}

	public void print() {
		for(int i=0; i < height; i++){
			for(int j=0; j < width; j++){
				LocalCell cell = cells[i][j];
				if (cell.agents.size() > 0) {
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

	private void handleMessages(){
		for(int i=0; i< peerList.size(); i++){
			int messageType = 0;
			try{
				ObjectInputStream in = (ObjectInputStream)peerList.get(i).in; 
				while(messageType != -1){
					messageType = in.read();
					switch(messageType){
					case Protocol.SENDAGENT:
						ReceivedAgent newAgent = Protocol.sendAgent(in);
						this.placeAgent(newAgent.x, newAgent.y, newAgent.agent);
						break;
					case Protocol.ENDTURN:
						int turn = Protocol.endTurn(in);
						messageType = -1;	
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private void sendCells(RemoteEngine remote){
		//TODO:  Send agents along with cells.
		int rWidth = this.width / 2;
		int rHeight = this.height;
		int rTlx = this.width - rWidth;
		int rTly = 0;
		Protocol.offerHelpResp(remote.out, rTlx, rTly, rWidth, rHeight, globalWidth, globalHeight);
		for(int i= rTlx; i < rWidth; i++){
			for(int j = rTly; j < rHeight; j++){
				LocalCell cell = getCell(i, j);
				for(Agent a : cell.agents){
					Protocol.sendAgent(remote.out, cell.x, cell.y, a);
				}
			}		
		}
		remote.setCoordinates(rTlx,rTly,rWidth,rHeight);
		this.peerList.add(remote);
		//TODO: Actually change the size of the data structure that
		//holds the cells.
		this.width = this.width - rWidth;
		gui.dispose();
		gui = new CellGrid(height, width, tlx, tly);
		
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
				//Use multicast instead.
				InetAddress other = InetAddress.getByName(args[0]);
				Socket socket = new Socket(other, port);
				RemoteEngine server = new RemoteEngine(socket);
				Protocol.offerHelpReq(server.out);
				OfferHelpResponse r = Protocol.offerHelpResp(server.in);
				engine = new LocalEngine(r.tlx, r.tly, r.width, r.height, r.globalWidth,
						r.globalHeight);
				server.setEngine(engine);
				engine.peerList.add(server);
				server.setCoordinates(0, 0, 5, 10);
				//TODO: Get agents from server.
			}

			// Server case
			else {
				// TODO: Don't hard code everything.
				engine = new LocalEngine(0, 0, globalWidth, globalHeight, globalWidth, globalHeight);
				ServerSocket serverSocket = new ServerSocket(port);
				Socket clientSocket = serverSocket.accept();
				RemoteEngine client = new RemoteEngine(clientSocket, engine);
				//This is to read the offerHelpReq message.  This
				//should be in a method.
				if(client.in.read() != Protocol.OFFERHELP){
					throw new Exception("Expected offer help request.");
				}
				// TODO: Use a smart algorithm to figure out what
				// coordinates to assign the other node.
				engine.sendCells(client);
	
				// We probably need some kind of ACK here.

				engine.placeAgents(5);

			}
			engine.print();
			engine.go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
