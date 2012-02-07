package filius.software.rip;

import filius.software.vermittlungsschicht.IP;

import java.util.LinkedList;
import java.util.ListIterator;

public class RIPMessage {
	public String ip;
	public int infinity;
	public int timeout;

	public LinkedList<RIPMessageRoute> routes;

	public RIPMessage(String ip, int infinity, int timeout) {
		this.ip = ip;
		this.infinity = infinity;
		this.timeout = timeout;

		routes = new LinkedList<RIPMessageRoute>();
	}

	public RIPMessage(String msg) throws IllegalArgumentException {
		String[] lines = msg.split("\n");

		try {
			ip = IP.ipCheck(lines[0]);
			infinity = Integer.parseInt(lines[1]);
			timeout = Integer.parseInt(lines[2]);
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException();
		}

		if (ip == null || timeout <= 0 || infinity <= 0) {
			throw new IllegalArgumentException();
		}

		routes = new LinkedList<RIPMessageRoute>();

		for (int i = 3; i < lines.length; i++) {
			routes.add(new RIPMessageRoute(lines[i]));
		}
	}

	public String toString() {
		String res = "";
		res += ip + "\n";
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

