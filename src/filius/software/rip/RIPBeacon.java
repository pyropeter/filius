package filius.software.rip;

import java.util.ListIterator;
import java.util.Random;

import filius.exception.VerbindungsException;
import filius.software.clientserver.ClientAnwendung;
import filius.software.transportschicht.UDPSocket;
import filius.software.system.VermittlungsrechnerBetriebssystem;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;

public class RIPBeacon extends ClientAnwendung {
	private Random rand;

	public void starten() {
		super.starten();

		rand = new Random();

		ausfuehren("announce", null);
	}

	public void announce() {
		VermittlungsrechnerBetriebssystem bs = (VermittlungsrechnerBetriebssystem)
				getSystemSoftware();
		RIPTable table = bs.getRIPTable();

		UDPSocket sock;
		try {
			sock = new UDPSocket(bs, "255.255.255.255", 520, 521);
			sock.verbinden();
		} catch (VerbindungsException e) {
			return;
		}

		long remaining;
		while (running) {
			synchronized (table) {
				remaining = table.nextBeacon - RIPUtil.getTime();
				if (remaining > 0) {
					try {
						table.wait(remaining);
					} catch (InterruptedException e) { }
					continue;
				}

				table.check();
				broadcast(sock, bs, table);

				table.nextBeacon = RIPUtil.getTime()
					+ (int)(RIPTable.INTERVAL
					* (rand.nextFloat()/3 + 0.84));
			}

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {}
		}

		socket.beenden();
	}

	public void broadcast(UDPSocket sock, VermittlungsrechnerBetriebssystem bs, RIPTable table) {
		InternetKnoten knoten = (InternetKnoten) bs.getKnoten();

		RIPMessage msg;
		NetzwerkInterface nic;

		ListIterator it = knoten.getNetzwerkInterfaces().listIterator();
		while (it.hasNext()) {
			nic = (NetzwerkInterface) it.next();

			msg = new RIPMessage(nic.getIp(), bs.holeIPAdresse(),
					RIPTable.INFINITY, RIPTable.TIMEOUT);
			for (RIPRoute route : table.routes) {
				// split horizon:
				if (nic.getIp().equals(route.nic)) {
					continue;
				}
				msg.addRoute(new RIPMessageRoute(
						route.netAddr, route.netMask, route.hops));
			}
			sock.bind(nic.getIp());
			sock.senden(msg.toString());
		}
	}
}
