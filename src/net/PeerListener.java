package net;

import java.util.EventListener;

public interface PeerListener extends EventListener {

	public void foundPeer(Peer p);

	public void lostPeer(Peer p);
}
