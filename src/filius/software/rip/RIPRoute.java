package filius.software.rip;

import filius.software.rip.RIPUtil;

import java.util.LinkedList;
import java.util.ListIterator;

public class RIPRoute {
	public long expires;
	public long created;
	public int hops;

	public String netAddr;
	public String netMask;
	public String nextHop;
	public String hopPublicIp; // hint for system administrator
	public String nic;

	public RIPRoute(int timeout, String netAddr, String netMask,
			String nextHop, String hopPublicIp,
			String nic, int hops) {
		this.created = RIPUtil.getTime();
		refresh(timeout);

		this.netAddr = netAddr;
		this.netMask = netMask;
		this.nextHop = nextHop;
		this.hopPublicIp = hopPublicIp;
		this.nic = nic;
		this.hops = hops;
	}

	public void refresh(int timeout) {
		if (timeout > 0) {
			this.expires = RIPUtil.getTime() + timeout;
		} else {
			this.expires = 0;
		}
	}

	public boolean isExpired() {
		return (this.expires > 0) && (this.expires < RIPUtil.getTime());
	}
}

