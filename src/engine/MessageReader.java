package engine;

import java.io.InputStream;
import java.util.PriorityQueue;
import java.net.Socket;

import net.Message;

public class MessageReader implements Runnable {

	private PriorityQueue<Message> recvdMessages;
	private InputStream in;
	private LocalEngine engine;
	private RemoteEngine sender;

	public MessageReader(LocalEngine engine, RemoteEngine sender){
		this.engine = engine;
		this.recvdMessages = engine.recvdMessages;
		this.sender = sender;
		System.out.println("Init message reader.");
	}

	public void run() {
		if(sender.in == null){
			try{
			System.out.println(sender.getID());
			System.out.println(engine.getID());
			Socket recvSocket = engine.listenSocket.accept();
			sender.setRecvSocket(recvSocket);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		this.in = sender.in;

		while (true) {
			try {
				byte messageType = (byte) in.read();
				Message message = null;
				switch (messageType) {
				case Message.SENDAGENT:
					message = new Message(engine.turn, messageType);
					message.recvAgent(in);
					message.ackMessage(sender.out);
					synchronized (recvdMessages) {						
						if(!recvdMessages.remove(message)) {
							recvdMessages.add(message);
						} else {
							System.err.println("Message and antimessage annihilated");
						}
					}
					break;
				case (~Message.SENDAGENT):
					System.out.println("Got an ack of sendagent");
					message = new Message(engine.turn, (byte)~messageType);
					message.recvAgent(in);
					synchronized(engine.unackMessages) {
						if(engine.unackMessages.remove(message)) {
							System.out.println("Removed unack message");
						} else {
							System.out.println("DID NOT remove unack message");
						}
					}
					break;
				case Message.ENDTURN:
					int turn = Message.recvEndTurn(in);
					sender.turn = turn;
					System.out.println("Received end turn: " + turn);
					break;
				default:
					do{
						System.out.println("Unknown Message type ");
						System.out.println(messageType);
						messageType = (byte)in.read();
					}while(messageType != -1);
					System.exit(0);
				}
				if (messageType == -1) {
					break;

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
