package filius.software.rip;

import filius.software.vermittlungsschicht.IP;

public class RIPMessageRoute {
	public String ip;
	public String mask;
	public int hops;

	public RIPMessageRoute(String ip, String mask, int hops) {
		this.ip = ip;
		this.mask = mask;
		this.hops = hops;
	}

	public RIPMessageRoute(String msg) throws IllegalArgumentException {
		String[] fields = msg.split(" ");
		try {
			ip = IP.ipCheck(fields[0]);
			mask = IP.ipCheck(fields[1]);
			hops = Integer.parseInt(fields[2]);
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException();
		}

		if (ip == null || mask == null || hops < 0) {
			throw new IllegalArgumentException();
		}
	}

	public String toString() {
		return ip + " " + mask + " " + hops;
	}
}

