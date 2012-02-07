package filius.software.rip;

import filius.software.vermittlungsschicht.IP;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;

import java.util.LinkedList;
import java.util.ListIterator;

public class RIPTable {
	public static final int INFINITY = 16;
	public static final int INTERVAL = 1000 * 5;
	public static final int TIMEOUT = INTERVAL * 5 / 2;

	public LinkedList<RIPRoute> routes;

	public long nextBeacon;

	public RIPTable() {
		reset();
	}

	public void reset() {
		this.routes = new LinkedList<RIPRoute>();
		this.nextBeacon = 0;
	}

	public void addRoute(RIPRoute route) {
		routes.add(route);
	}

	public void addLocalRoutes(InternetKnoten knoten) {
		NetzwerkInterface nic;
		long netMask, netAddr;

		ListIterator it = knoten.getNetzwerkInterfaces().listIterator();
		while (it.hasNext()) {
			nic = (NetzwerkInterface) it.next();
			netMask = IP.inetAton(nic.getSubnetzMaske());
			netAddr = IP.inetAton(nic.getIp()) & netMask;

			addRoute(new RIPRoute(0, IP.inetNtoa(netAddr), IP.inetNtoa(netMask),
						nic.getIp(), nic.getIp(), 0));
		}
	}

	public RIPRoute search(String net, String mask) {
		for (RIPRoute route : routes) {
			if (route.netAddr.equals(net) && route.netMask.equals(mask)) {
				return route;
			}
		}
		return null;
	}

	public void check() {
		for (RIPRoute route : routes) {
			if (route.isExpired()) {
				route.hops = INFINITY;
			}
		}
	}
}

