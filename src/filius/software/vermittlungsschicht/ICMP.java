/*
** This file is part of Filius, a network construction and simulation software.
** 
** Originally created at the University of Siegen, Institute "Didactics of
** Informatics and E-Learning" by a students' project group:
**     members (2006-2007): 
**         André Asschoff, Johannes Bade, Carsten Dittich, Thomas Gerding,
**         Nadja Haßler, Ernst Johannes Klebert, Michell Weyer
**     supervisors:
**         Stefan Freischlad (maintainer until 2009), Peer Stechert
** Project is maintained since 2010 by Christian Eibl <filius@c.fameibl.de>
 **         and Stefan Freischlad
** Filius is free software: you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation, either version 2 of the License, or
** (at your option) version 3.
** 
** Filius is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied
** warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
** PURPOSE. See the GNU General Public License for more details.
** 
** You should have received a copy of the GNU General Public License
** along with Filius.  If not, see <http://www.gnu.org/licenses/>.
*/
package filius.software.vermittlungsschicht;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map.Entry;

import filius.Main;
import filius.exception.VerbindungsException;
import filius.hardware.NetzwerkInterface;
import filius.hardware.Verbindung;
import filius.hardware.knoten.InternetKnoten;
import filius.software.netzzugangsschicht.EthernetFrame;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.system.SystemSoftware;
import filius.rahmenprogramm.I18n;

/**
 * This class implements the ICMP protocol -- at least for echo request/response.
 */
public class ICMP extends VermittlungsProtokoll implements I18n {

	/**
	 * 
	 */
	private ICMPThread thread;

	/**
	 * Standard-Konstruktor zur Initialisierung der zugehoerigen Systemsoftware
	 *
	 * @param systemAnwendung
	 */
	public ICMP(SystemSoftware systemAnwendung) {
		super(systemAnwendung);
		Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+" (ICMP), constr: ICMP("+systemAnwendung+")");
	}

	public void starten() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (ICMP), starten()");
		thread = new ICMPThread(this);
		thread.starten();
	}

	public void beenden() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (ICMP), beenden()");
		if (thread != null) thread.beenden();
	}

	private void placeLocalICMPPacket(IcmpPaket icmpPacket) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (ICMP), placeLocalICMPPacket("+icmpPacket.toString()+")");
		LinkedList<IcmpPaket> icmpPakete = ((InternetKnotenBetriebssystem) holeSystemSoftware()).holeEthernet().holeICMPPuffer(); 
		synchronized(icmpPakete) {
			icmpPakete.add(icmpPacket);
//			Main.debug.println("DEBUG ("+this.hashCode()+") "+getClass()+" (Ethernet), placeLocalICMPPacket, ICMP packet="+(icmpPakete.getFirst().toString()));
			icmpPakete.notify();
		}
	}
	

	/** Hilfsmethode zum Versenden eines ICMP Echo Requests */
	public IcmpPaket sendEchoRequest(String destIp, int seqNr) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (ICMP), sendEchoRequest("+destIp+","+seqNr+")");
		ListIterator it;
		IcmpPaket icmpPaket;
		String ipAdresse, netzmaske, mac;
		NetzwerkInterface nic;

		icmpPaket = new IcmpPaket();
		icmpPaket.setProtokollTyp(EthernetFrame.IP);    // ICMP type is equal to IP, since ICMP actually is part of IP!
		icmpPaket.setQuellIp(((InternetKnotenBetriebssystem) holeSystemSoftware()).holeIPAdresse());
		icmpPaket.setQuellMacAdresse(((InternetKnotenBetriebssystem) holeSystemSoftware()).holeMACAdresse());
		icmpPaket.setZielIp(destIp);
		icmpPaket.setIcmpType(8);
		icmpPaket.setIcmpCode(0);
		icmpPaket.setTtl(64);
		icmpPaket.setSeqNr(seqNr);

		it = ((InternetKnoten) holeSystemSoftware().getKnoten())
				.getNetzwerkInterfaces().listIterator();

		if(destIp.equals(IP.LOCALHOST)) {
			//Main.debug.println("DEBUG: sendEchoRequest to localhost IP");
			placeLocalICMPPacket(icmpPaket);
			return icmpPaket;
		}
		else {
			InternetKnotenBetriebssystem bs = (InternetKnotenBetriebssystem) holeSystemSoftware();
			String[] routingEintrag = bs.getWeiterleitungstabelle().holeWeiterleitungsZiele(icmpPaket.getZielIp());

			if (routingEintrag != null) {
				String gateway = routingEintrag[0];
				String schnittstelle = routingEintrag[1];

				// Damit Pakete nicht in Zyklen gesendet werden
				// wird hier die TTL ueberprueft.
				// TTL wird dekrementiert beim Empfang eines Pakets
				if (icmpPaket.getTtl() > 0) {
					try {
						return sendeUnicast(icmpPaket, gateway, schnittstelle);
					}
					catch (VerbindungsException e) {
						e.printStackTrace(Main.debug);
					}
				}
			}
			else {
				bs.benachrichtigeBeobacher(messages.getString("sw_ip_msg4")
						+ " \"" + bs.getKnoten().getName() + "\"!\n"
						+ messages.getString("sw_ip_msg5") + " "
						+ icmpPaket.getZielIp() + " "
						+ messages.getString("sw_ip_msg6"));
			}
		}
		return null;
	}

	/** Hilfsmethode zum Versenden eines ICMP Echo Reply */
	public void sendEchoReply(IcmpPaket rcvPacket) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (ICMP), sendEchoReply("+rcvPacket.toString()+")");
		ListIterator it;
		IcmpPaket icmpPaket;
		String ipAdresse, netzmaske, mac;
		NetzwerkInterface nic;

		icmpPaket = new IcmpPaket();
		icmpPaket.setProtokollTyp(EthernetFrame.IP);    // ICMP type is equal to IP, since ICMP actually is part of IP!
		icmpPaket.setQuellIp(rcvPacket.getZielIp());
		icmpPaket.setQuellMacAdresse(rcvPacket.getZielMacAdresse());
		icmpPaket.setZielIp(rcvPacket.getQuellIp());
		icmpPaket.setIcmpType(0);
		icmpPaket.setIcmpCode(0);
		icmpPaket.setTtl(64);
		icmpPaket.setSeqNr(rcvPacket.getSeqNr());

		it = ((InternetKnoten) holeSystemSoftware().getKnoten())
				.getNetzwerkInterfaces().listIterator();

		if(rcvPacket.getZielIp().equals(IP.LOCALHOST)) {
			//Main.debug.println("DEBUG: sendEchoReply to localhost IP");
			placeLocalICMPPacket(icmpPaket);
		}
		else {
			//Main.debug.println("DEBUG: sendEchoReply, non-local destination IP");
			ipAdresse = icmpPaket.getQuellIp();

					InternetKnotenBetriebssystem bs = (InternetKnotenBetriebssystem) holeSystemSoftware();
					String[] routingEintrag = bs.getWeiterleitungstabelle().holeWeiterleitungsZiele(icmpPaket.getZielIp());

					if (routingEintrag != null) {
						String gateway = routingEintrag[0];
						String schnittstelle = routingEintrag[1];

						// Damit Pakete nicht in Zyklen gesendet werden
						// wird hier die TTL ueberprueft.
						// TTL wird dekrementiert beim Empfang eines Pakets
						if (icmpPaket.getTtl() > 0) {
							try {
								sendeUnicast(icmpPaket, gateway, schnittstelle);
							}
							catch (VerbindungsException e) {
								e.printStackTrace(Main.debug);
							}
						}
					}
					else {
						bs.benachrichtigeBeobacher(messages.getString("sw_ip_msg4")
								+ " \"" + bs.getKnoten().getName() + "\"!\n"
								+ messages.getString("sw_ip_msg5") + " "
								+ icmpPaket.getZielIp() + " "
								+ messages.getString("sw_ip_msg6"));
					}
		}
	}
	
	/**
	 * Hilfsmethode zum versenden eines Unicast-Pakets. Hier wird unterschieden,
	 * ob sich die Ziel-IP-Adresse im lokalen Rechnernetz befindet oder ueber
	 * das Gateway verschickt werden muss.
	 *
	 * @param paket
	 *            das zu versendende IP-Paket
	 * @param gateway
	 *            das Gateway ueber das, das Paket gegebenenfalls verschickt
	 *            werden muss
	 * @param schnittstelle
	 *            die Schnittstelle, ueber die das IP-Paket verschickt werden
	 *            muss
	 * @throws VerbindungsException
	 */
	private IcmpPaket sendeUnicast(IcmpPaket paket, String gateway,
			String schnittstelle) throws VerbindungsException {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (ICMP), sendeUnicast("+paket.toString()+","+gateway+","+schnittstelle+")");
		InternetKnoten knoten;
		NetzwerkInterface nic;
		InternetKnotenBetriebssystem bs;
		String netzmaske;
		String zielMacAdresse;

		knoten = (InternetKnoten) holeSystemSoftware().getKnoten();
		bs = (InternetKnotenBetriebssystem) holeSystemSoftware();

		nic = (NetzwerkInterface) knoten
				.getNetzwerkInterfaceByIp(schnittstelle);
		netzmaske = nic.getSubnetzMaske();

		// adressierter Knoten befindet sich im lokalen Rechnernetz
		if (gleichesRechnernetz(paket.getZielIp(), schnittstelle, netzmaske)) {
			//Main.debug
					//.println("Vermittlung: Ziel-Adresse im lokalen Rechnernetz");
			zielMacAdresse = bs.holeARP().holeARPTabellenEintrag(
					paket.getZielIp());
		}
		// adressierter Knoten ist ueber Gateway zu erreichen
		else {
			//Main.debug.println("Vermittlung: Ziel-Adresse ueber Gateway zu erreichen");
			zielMacAdresse = bs.holeARP().holeARPTabellenEintrag(gateway);
		}
		paket.setZielMacAdresse(zielMacAdresse);

		//Main.debug.println("Vermittlung: ZielMacAdresse = " + zielMacAdresse);

		// MAC-Adresse konnte bestimmt werden
		if (zielMacAdresse != null) {
			bs.holeEthernet().senden(paket, nic.getMac(), zielMacAdresse,
					EthernetFrame.IP);
		}
		// Es konnte keine MAC-Adresse fuer den Zielknoten
		// bzw. fuer das Gateway bestimmt werden
		else {
			throw new VerbindungsException(messages.getString("sw_ip_msg1")
					+ " " + paket.getZielIp() + " " + messages.getString("sw_ip_msg2")
					+ " " + zielMacAdresse + " " + messages.getString("sw_ip_msg3")
					+ " " + gateway);
		}
		return paket;
	}
	
	/**
	 * Methode zum Weiterleiten eines ICMP-Pakets. Zunaechst wird geprueft, ob das
	 * Feld Time-to-Live-Feld (TTL) noch nicht abgelaufen ist (d. h. TTL
	 * groesser 0). Wenn diese Bedingung erfuellt ist, wird zunaechst geprueft,
	 * ob es sich um einen Broadcast handelt. Ein solches Paket wird nicht
	 * weitergeleitet sondern nur an die Transportschicht weitergegeben. Sonst
	 * wird die Weiterleitungstabelle nach einem passenden Eintrag abgefragt.
	 * Anhand des zurueckgegebenen Eintrags wird geprueft, ob das Paket fuer den
	 * eigenen Rechner ist oder ob ein Unicast-Paket verschickt werden muss.
	 *
	 * @param icmpPaket
	 *            das zu versendende ICMP-Paket
	 */
	public void weiterleitenPaket(IcmpPaket icmpPaket) throws VerbindungsException {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (ICMP), weiterleitenPaket("+icmpPaket.toString()+")");
		String gateway;
		String schnittstelle;
		String[] routingEintrag;
		InternetKnotenBetriebssystem bs;

		bs = (InternetKnotenBetriebssystem) holeSystemSoftware();

		routingEintrag = bs.getWeiterleitungstabelle().holeWeiterleitungsZiele(icmpPaket.getZielIp());

		if (routingEintrag != null) {
			gateway = routingEintrag[0];
			schnittstelle = routingEintrag[1];

			// Wenn das Paket fuer diesen Rechner ist, wird das Paket
			// weiterverarbeitet
			if (schnittstelle.equals(IP.LOCALHOST)) {
				// Antwort senden
				sendEchoReply(icmpPaket);
			}
			// Damit Pakete nicht in Zyklen gesendet werden
			// wird hier die TTL ueberprueft.
			// TTL wird dekrementiert beim Empfang eines Pakets
			else if (icmpPaket.getTtl() > 0) {
				sendeUnicast(icmpPaket, gateway, schnittstelle);
			}
		}
		else {
			bs.benachrichtigeBeobacher(messages.getString("sw_ip_msg4")
					+ " \"" + bs.getKnoten().getName() + "\"!\n"
					+ messages.getString("sw_ip_msg5") + " "
					+ icmpPaket.getZielIp() + " "
					+ messages.getString("sw_ip_msg6"));
		}
	}
	
	public int startSinglePing(String destIp, int seqNr) throws java.util.concurrent.TimeoutException {
		return thread.startSinglePing(destIp, seqNr);
	}
	
	public ICMPThread getICMPThread() {
		return thread;
	}

}
