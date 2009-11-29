package net;

import javax.swing.event.EventListenerList;

public abstract class PeerFinder {
	
	protected EventListenerList listeners = new EventListenerList();
	
	public void addPeerListener(PeerListener p) {
		listeners.add(PeerListener.class, p);
	}
	
	public void removePeerListener(PeerListener p) {
		listeners.remove(PeerListener.class, p);
	}
	
	public void firePeerFound(Peer p) {
		for (PeerListener l : listeners.getListeners(PeerListener.class)) {
			l.foundPeer(p);
		}
	}
	
	public void firePeerLost(Peer p) {
		for (PeerListener l : listeners.getListeners(PeerListener.class)) {
			l.lostPeer(p);
		}
	}
	
	public abstract void startSearching();
	
	public abstract void stopSearching();
	
	public abstract void register();
	
	public abstract void unregister();
	
}
