package filius.software.rip;

import filius.software.vermittlungsschicht.IP;

import java.util.LinkedList;
import java.util.ListIterator;

public class RIPMessage {
	public String ip;
	public String publicIp;
	public int infinity;
	public int timeout;

	public LinkedList<RIPMessageRoute> routes;

	public RIPMessage(String ip, String publicIp, int infinity, int timeout) {
		this.ip = ip;
		this.publicIp = publicIp;
		this.infinity = infinity;
		this.timeout = timeout;

		routes = new LinkedList<RIPMessageRoute>();
	}

	public RIPMessage(String msg) throws IllegalArgumentException {
		String[] lines = msg.split("\n");

		try {
			ip = IP.ipCheck(lines[0]);
			publicIp = IP.ipCheck(lines[1]);
			infinity = Integer.parseInt(lines[2]);
			timeout = Integer.parseInt(lines[3]);
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException();
		}

		if (ip == null || publicIp == null || timeout <= 0
				|| infinity <= 0) {
			throw new IllegalArgumentException();
		}

		routes = new LinkedList<RIPMessageRoute>();

		for (int i = 4; i < lines.length; i++) {
			routes.add(new RIPMessageRoute(lines[i]));
		}
	}

	public String toString() {
		String res = "";
		res += ip + "\n";
		res += publicIp + "\n";
		res += infinity + "\n";
		res += timeout;

		for (RIPMessageRoute route : routes) {
			res += "\n" + route.toString();
		}

		return res;
	}

	public void addRoute(RIPMessageRoute newRoute) {
		for (RIPMessageRoute route : routes) {
			if (route.ip.equals(newRoute.ip)
			&& route.mask.equals(newRoute.mask)) {
				if (newRoute.hops < route.hops) {
					route.hops = newRoute.hops;
				}
				return;
			}
		}
		routes.add(newRoute);
	}
}

