package net;

public class Peer {
	private final String name;
	private final String address;
	private final int port;

	public Peer(final String name, final String address, final int port) {
		this.name = name;
		this.address = address;
		this.port = port;
	}

	public Peer(final String name) {
		this.name = name;
		this.address = "";
		this.port = -1;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}
}
