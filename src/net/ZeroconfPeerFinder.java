package net;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import com.apple.dnssd.BrowseListener;
import com.apple.dnssd.DNSSD;
import com.apple.dnssd.DNSSDException;
import com.apple.dnssd.DNSSDRegistration;
import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.RegisterListener;
import com.apple.dnssd.ResolveListener;
import com.apple.dnssd.TXTRecord;

public class ZeroconfPeerFinder extends PeerFinder implements RegisterListener,
		BrowseListener, ResolveListener {
	
	private final Logger log = Logger.getLogger(this.getClass());
	
	protected DNSSDRegistration localRegistration = null;
	protected DNSSDService browser = null;
	
	public static final String SERVICE_TYPE = "_cabs._tcp";
	
	@Override
	public void register() {
		try {
			localRegistration = DNSSD.register(null, SERVICE_TYPE, 1234, this);
		} catch (DNSSDException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void startSearching() {
		if (browser == null) {
			try {
				browser = DNSSD.browse(SERVICE_TYPE, this);
			} catch (DNSSDException e) {
				log.error("Unable to start searching for peers", e);
			}
		} else {
			log
					.info("Got request to start searching for peers, but was already searching.");
		}
	}
	
	@Override
	public void stopSearching() {
		if (browser != null) {
			browser.stop();
			browser = null;
		} else {
			log
					.info("Got request to stop searching for peers, but wasn't searching.");
		}
	}
	
	@Override
	public void unregister() {
		if (localRegistration != null) {
			localRegistration.stop();
			localRegistration = null;
		} else {
			log
					.info("Got request to unregister, but wasn't registered to begin with.");
		}
	}
	
	@Override
	public void serviceRegistered(DNSSDRegistration registration, int flags,
			java.lang.String serviceName, java.lang.String regType,
			java.lang.String domain) {
		log.debug("Registered service: " + serviceName + " " + domain);
	}
	
	@Override
	public void operationFailed(DNSSDService arg0, int arg1) {
		log.error(MessageFormat.format("mDNS Error: {0}; Error code: {1}", arg0
				.toString(), arg1));
	}
	
	@Override
	public void serviceFound(DNSSDService browser, int flags, int ifIndex,
			java.lang.String serviceName, java.lang.String regType,
			java.lang.String domain) {
		
		log.debug(MessageFormat.format("Found a service: {0}; {1}; {2}",
				serviceName, regType, domain));
		try {
			DNSSD.resolve(flags, ifIndex, serviceName, regType, domain, this);
		} catch (DNSSDException e) {
			log.error("Error resolving found service", e);
		}
	}
	
	@Override
	public void serviceLost(DNSSDService browser, int flags, int ifIndex,
			java.lang.String serviceName, java.lang.String regType,
			java.lang.String domain) {
		log.debug(MessageFormat.format("Lost a service: {0}", serviceName));
		firePeerLost(new Peer(serviceName));
	}
	
	@Override
	public void serviceResolved(DNSSDService resolver, int flags, int ifIndex,
			java.lang.String fullName, java.lang.String hostName, int port,
			TXTRecord txtRecord) {
		log.debug(MessageFormat.format("Resolved a service: {0}; {1}; {2}",
				fullName, hostName, port));
		firePeerFound(new Peer(fullName, hostName, port));
	}
}
