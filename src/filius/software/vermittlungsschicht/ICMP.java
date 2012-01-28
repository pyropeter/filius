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
			icmpPakete.notify();
		}
	}
	

	/** Hilfsmethode zum Versenden eines ICMP Echo Requests */
	public void sendEchoRequest(String destIp, int seqNr) {
		sendeICMP(8, 0, seqNr, destIp);
	}

	/** Hilfsmethode zum Versenden eines ICMP Echo Reply */
	public void sendEchoReply(IcmpPaket rcvPacket) {
		sendeICMP(0, 0, rcvPacket.getSeqNr(), rcvPacket.getQuellIp());
	}

	public void sendeICMP(int typ, int code, String zielIP) {
		sendeICMP(typ, code, 0, zielIP);
	}

	public void sendeICMP(int typ, int code, int seqNr, String zielIP) {
		sendeICMP(typ, code, 64, seqNr, zielIP);
	}

	public void sendeICMP(int typ, int code, int ttl, int seqNr, String zielIP) {
		IcmpPaket icmpPaket = new IcmpPaket();
		icmpPaket.setProtokollTyp(EthernetFrame.IP);
		icmpPaket.setQuellIp(((InternetKnotenBetriebssystem)
					holeSystemSoftware()).holeIPAdresse());
		icmpPaket.setQuellMacAdresse(((InternetKnotenBetriebssystem)
					holeSystemSoftware()).holeMACAdresse());
		icmpPaket.setZielIp(zielIP);
		icmpPaket.setIcmpType(typ);
		icmpPaket.setIcmpCode(code);
		icmpPaket.setSeqNr(seqNr);
		icmpPaket.setTtl(ttl);

		if (zielIP.equals(IP.LOCALHOST)) {
			placeLocalICMPPacket(icmpPaket);
		} else {
			InternetKnotenBetriebssystem bs = (InternetKnotenBetriebssystem)
					holeSystemSoftware();
			String[] route = bs.getWeiterleitungstabelle()
					.holeWeiterleitungsZiele(zielIP);

			if (route != null) {
				try {
					sendeUnicast(icmpPaket, route[0], route[1]);
				} catch (VerbindungsException e) {
					// ICMP ist optional...
				}
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
	private void sendeUnicast(IcmpPaket paket, String gateway,
			String schnittstelle) throws VerbindungsException {
		InternetKnoten knoten = (InternetKnoten)holeSystemSoftware().getKnoten();
		NetzwerkInterface nic = knoten.getNetzwerkInterfaceByIp(schnittstelle);
		String netzmaske = nic.getSubnetzMaske();

		if (gleichesRechnernetz(paket.getZielIp(), schnittstelle, netzmaske)) {
			// adressierter Knoten befindet sich im lokalen Rechnernetz
			sendeUnicastLokal(paket, paket.getZielIp(), nic);
		} else {
			// adressierter Knoten ist ueber Gateway zu erreichen
			sendeUnicastLokal(paket, gateway, nic);
		}
	}

	/**
	 * Hilfsmethode zum Versenden eines Unicast-Pakets im lokalen Rechnernetz.
	 *
	 * @param paket
	 *            das zu versendende IP-Paket
	 * @param ziel
	 *            die Ziel-IP
	 * @param schnittstelle
	 *            die Schnittstelle, ueber die das IP-Paket verschickt werden
	 *            muss
	 * @throws VerbindungsException
	 */
	private void sendeUnicastLokal(IcmpPaket paket, String ziel,
			NetzwerkInterface nic) throws VerbindungsException {
		InternetKnotenBetriebssystem bs =
				(InternetKnotenBetriebssystem)holeSystemSoftware();
		String zielMacAdresse = bs.holeARP().holeARPTabellenEintrag(ziel);

		if (zielMacAdresse != null) {
			// MAC-Adresse konnte bestimmt werden
			paket.setZielMacAdresse(zielMacAdresse);
			bs.holeEthernet().senden(paket, nic.getMac(), zielMacAdresse,
					EthernetFrame.IP);
		} else {
			// Es konnte keine MAC-Adresse fuer den Zielknoten
			// bzw. fuer das Gateway bestimmt werden
			throw new VerbindungsException(messages.getString("sw_ip_msg1")
					+ " " + paket.getZielIp() + " " + messages.getString("sw_ip_msg2")
					+ " " + zielMacAdresse + " " + messages.getString("sw_ip_msg3")
					+ " " + ziel);
		}
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
			if (schnittstelle.equals(IP.LOCALHOST)
					&& icmpPaket.getIcmpType() == 8) {
				// Antwort senden
				sendEchoReply(icmpPaket);
			}
			// Damit Pakete nicht in Zyklen gesendet werden
			// wird hier die TTL ueberprueft.
			// TTL wird dekrementiert beim Empfang eines Pakets
			else if (icmpPaket.getTtl() > 0) {
				sendeUnicast(icmpPaket, gateway, schnittstelle);
			}
			else {
				// TTL abgelaufen.
				// ICMP 11/0 Timeout Expired In Transit zuruecksenden
				sendeICMP(11, 0, icmpPaket.getSeqNr(), icmpPaket.getQuellIp());
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

	public IcmpPaket sendProbe(String destIp, int ttl, int seqNr) {
		return thread.sendProbe(destIp, ttl, seqNr);
	}

	public ICMPThread getICMPThread() {
		return thread;
	}

}
