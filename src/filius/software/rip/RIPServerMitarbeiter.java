package filius.software.rip;

import filius.software.clientserver.ServerMitarbeiter;
import filius.software.vermittlungsschicht.IP;
import filius.software.transportschicht.Socket;
import filius.software.transportschicht.UDPSocket;
import filius.software.system.VermittlungsrechnerBetriebssystem;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;

import java.util.ListIterator;

public class RIPServerMitarbeiter extends ServerMitarbeiter {
	private RIPTable table;
	private VermittlungsrechnerBetriebssystem bs;
	private InternetKnoten knoten;

	public RIPServerMitarbeiter(RIPServer server, Socket socket) {
		super(server, socket);
		bs = (VermittlungsrechnerBetriebssystem) server.getSystemSoftware();
		knoten = (InternetKnoten) bs.getKnoten();
		table = bs.getRIPTable();
	}

	protected void verarbeiteNachricht(String nachricht) {
		RIPMessage msg;
		try {
			msg = new RIPMessage(nachricht);
		} catch (IllegalArgumentException e) {
			return;
		}

		// find the interface that (probably) received the message
		String nicIp = findeInterfaceIp(msg.ip);
		if (nicIp == null || nicIp.equals(msg.ip)) {
			return;
		}

		RIPRoute route;
		int hops;
		for (RIPMessageRoute entry : msg.routes) {
			if (entry.hops >= msg.infinity || entry.hops + 1 >= RIPTable.INFINITY) {
				hops = RIPTable.INFINITY;
			} else {
				hops = entry.hops + 1;
			}

			synchronized (table) {
				route = table.search(entry.ip, entry.mask);
				if (route != null) {
					// route exists, just update
					if (!route.nextHop.equals(msg.ip) && route.hops <= hops) {
						continue;
					}
					if (route.hops > hops) {
						// found a shorter route
						route.nextHop = msg.ip;
						route.hopPublicIp = msg.publicIp;
						route.nic = nicIp;
					} else if (route.hops < hops) {
						// the old route just got worse. this has to be
						// flushed to other routers immediately
						table.nextBeacon = 0;
					}
					route.hops = hops;
					route.refresh(msg.timeout);
				} else {
					// route is unknown, create it
					if (hops < RIPTable.INFINITY) {
						route = new RIPRoute(msg.timeout, entry.ip, entry.mask,
								msg.ip, msg.publicIp, nicIp, hops);
						table.addRoute(route);
					}
				}
			}
		}

		synchronized (table) {
			// table.nextBeacon was changed, notify
			// the beacon:
			table.notifyAll();
		}
	}

	String findeInterfaceIp(String ipStr) {
		long ip = IP.inetAton(ipStr);
		ListIterator it = knoten.getNetzwerkInterfaces().listIterator();

		while (it.hasNext()) {
			NetzwerkInterface nic = (NetzwerkInterface) it.next();
			long netMask = IP.inetAton(nic.getSubnetzMaske());
			long netAddr = IP.inetAton(nic.getIp()) & netMask;

			if ((ip & netMask) == netAddr) {
				return nic.getIp();
			}
		}
		return null;
	}
}
