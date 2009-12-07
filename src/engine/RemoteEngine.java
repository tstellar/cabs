package engine;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collection;

import engine.MessageReader;
import net.Message;
import world.Agent;
import world.Cell;
import world.RemoteCell;

public class RemoteEngine extends Engine {

	Socket sendSocket;
	Socket recvSocket;
	InputStream in;
	OutputStream out;
	LocalEngine localEngine;
	MessageReader reader;
	Thread readerThread;
	public InetAddress addr;
	public int port;
	int id;

	/*This constructor should be called during the Engine discovery process
	 * of CABS when each Engine is reading and writing from the same socket*/
	public RemoteEngine(Socket sendSocket, Socket recvSocket) {
		this.setSendSocket(sendSocket);
		this.setRecvSocket(recvSocket);
	}

	public RemoteEngine(LocalEngine localEngine, InetAddress addr, int port) {
		this.localEngine = localEngine;
		this.addr = addr;
		this.port = port;
		this.sendSocket = new Socket();
		this.recvSocket = new Socket();
		this.in = null;
		this.out = null;
	}
	
	public void setSendSocket(Socket sendSocket){
		this.sendSocket = sendSocket;
		try{
			this.out = sendSocket.getOutputStream();
			this.addr = sendSocket.getInetAddress();
			this.port = sendSocket.getPort();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void setRecvSocket(Socket recvSocket){
		this.recvSocket = recvSocket;
		try{
			this.in = this.recvSocket.getInputStream();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public Socket getRecvSocket(){
		return this.recvSocket;
	}
	
	public void setEngine(LocalEngine engine) {
		this.localEngine = engine;
	}

	public void listen() {
		reader = new MessageReader(localEngine, this);
		readerThread = new Thread(reader);
		readerThread.start();
	}

	@Override
	public Cell findCell(int x, int y) {
		// TODO: Send a 'findCell' request to this remote machine using
		// the message protocol.
		return new RemoteCell(x, y, this);
	}

	public void sendAgent(RemoteCell newCell, Agent agent) {
		// TODO: Send a 'sendAgent' request to the remote machine using
		// the message protocol.
		Message message = new Message(localEngine.turn, true, getID());
		message.sendAgent(newCell.getX(), newCell.getY(), agent);
		localEngine.sendMessage(message, out);
	}

	public Collection<? extends Agent> getRemoteAgents(int x, int y) {
		System.out.println("Looking at remote agents");
		Collection<? extends Agent> agents = null;
		int msgId = id++;
		Message message = new Message(localEngine.turn, true, getID());
		message.lookRequest(x, y, msgId);
		localEngine.sendMessage(message, out);
		while (!localEngine.lookResponses.containsKey(msgId)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		synchronized (localEngine.lookResponses) {
			agents = localEngine.lookResponses.remove(msgId).agents;
		}
		return agents;
	}
}
