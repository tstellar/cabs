import java.util.*;
import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;

public class LocalEngine extends Engine{


	Cell[][] cells;
	ArrayList<RemoteEngine> peerList;
	int globalWidth;
	int globalHeight;

	public LocalEngine(int tlx, int tly, int width, int height, int globalWidth, int globalHeight){
		super(tlx, tly, width, height);
		this.globalWidth = globalWidth;
		this.globalHeight = globalHeight;
		peerList = new ArrayList<RemoteEngine>();
		cells = new Cell[height][width];
		for(int i=0; i< cells.length; i++){
			for(int j=0; j< cells[i].length; j++){
				cells[i][j] = new Cell(j,i,this);
			}
		}
	}

	public void go(int turn){
		for(int i=0; i< cells.length; i++){
			for(int j=0; j< cells[i].length; j++){
				cells[i][j].go(turn);
			}
		}
	}
	
	public void moveAgent(Agent agent, Cell oldCell, int x, int y){
		Cell newCell = findCell(oldCell.x + x, oldCell.y + y);
		newCell.add(agent);
		oldCell.remove(agent);
	}
	
	private Cell findRemoteCell(int x, int y){
		for(int i=0; i< peerList.size(); i++){
			if(peerList.get(i).hasCell(x, y)){
				return peerList.get(i).findCell(x, y);
			}
		}
		return null;
	}
	
	public Cell findCell(int x, int y){
		if(y >= globalHeight){
			y = y % globalHeight;
		}
		if(x >= globalWidth){
			x = x % globalWidth;
		}
		if(y < 0){
			y = (y % globalHeight) + globalHeight;
		}
		if(x < 0){
			x = (x % globalWidth) + globalWidth;
		}
		if(hasCell(x, y)){
			return cells[y][x];
		}
		else{
			return findRemoteCell(x, y);
		}
	}

	public void placeAgents(int agents){
		for(int i=0; i< agents; i++){
			cells[i][0].add(new Rabbit());
		}
	}

	public void print(){
		for(int i=0; i< cells.length; i++){
			for(int j=0; j< cells[i].length; j++){
				if(cells[i][j].agents.size() > 0){
					System.out.print("* ");
				}
				else{
					System.out.print("- ");
				}
			}
			System.out.println();
		}
	}	
	
	public static void main(String[] args){
		
		int globalWidth = 10;
		int globalHeight = 10;
		int port = 1234;
		LocalEngine engine = null;
		try{
		if(args.length == 1){
			byte[] x = new byte[25];
			InetAddress other = InetAddress.getByName(args[0]);
			Socket socket = new Socket(other, port);
			OutputStream out  = socket.getOutputStream();
			InputStream in = socket.getInputStream();
			Protocol.offerHelpReq(out);
			OfferHelpResponse r = Protocol.offerHelpResp(in);
			engine = new LocalEngine(r.tlx, r.tly, r.width, r.height,
						r.globalWidth, r.globalHeight);
		}
		else{
			engine = new LocalEngine(0, 0, 5, 5, 10, 10);
			byte[] r = new byte[1];
			ServerSocket serverSocket = new ServerSocket(port);
			Socket clientSocket = serverSocket.accept();
			InputStream in = clientSocket.getInputStream();
			OutputStream out = clientSocket.getOutputStream();
			in.read(r);
			System.out.println(r[0]);
			Protocol.offerHelpResp(out, 5, 0, 5, 5, 10, 10);
			engine.placeAgents(5);
			engine.print();
			for(int i=0; i< 8; i++){
				engine.go(i);
				engine.print();
			}

		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
