package net;

import java.text.MessageFormat;

import com.apple.dnssd.TXTRecord;

public class Peer {
	private final String name;
	private final String address;
	private final int port;
	private final TXTRecord txtRecord;

	public Peer(final String name, final String address, final int port, final TXTRecord txtRecord) {
		this.name = name;
		this.address = address;
		this.port = port;
		this.txtRecord = txtRecord;
	}

	public Peer(final String name) {
		this(name, "", -1, new TXTRecord());
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

	public TXTRecord getTxtRecord() {
		return txtRecord;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(MessageFormat.format("Name: {0}, Address: {1}, Port: {2}", name, address, port));
		for (int i = 0; i < txtRecord.size(); ++i) {
			sb.append(MessageFormat.format("<{0}, {1}>", txtRecord.getKey(i), txtRecord
					.getValueAsString(i)));
		}
		return sb.toString();
	}

}
