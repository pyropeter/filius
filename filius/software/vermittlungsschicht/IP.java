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

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;

import filius.Main;
import filius.exception.VerbindungsException;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.I18n;
import filius.software.netzzugangsschicht.EthernetFrame;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.transportschicht.TcpSegment;
import filius.software.transportschicht.UdpSegment;

/**
 * Diese Klasse implementiert die Funktionalitaet des Internet-Protokolls. Das
 * heisst, dass Pakete richtig weitergeleitet werden und eingehende Segmente an
 * die Transportschicht weitergeleitet werden.
 */
public class IP extends VermittlungsProtokoll implements I18n {

	private static final long serialVersionUID = 1L;

	/** String-Konstante fuer die IP-Adresse Localhost (127.0.0.1) */
	public static final String LOCALHOST = "127.0.0.1";

	/** Puffer fuer eingehende IP-Pakete fuer TCP */
	private LinkedList<IpPaket> ipPaketListeTCP = new LinkedList<IpPaket>();

	/** Puffer fuer eingehende IP-Pakete fuer UDP */
	private LinkedList<IpPaket> ipPaketListeUDP = new LinkedList<IpPaket>();

	/**
	 * Der Thread zur Ueberwachung des IP-Pakete-Puffers der Ethernet-Schicht
	 */
	private IPThread thread;

	private LinkedList<IpPaket> paketListe = new LinkedList<IpPaket>();

	/**
	 * Konstruktor zur Initialisierung der Systemsoftware
	 *
	 * @param systemsoftware
	 */
	public IP(InternetKnotenBetriebssystem systemsoftware) {
		super(systemsoftware);
		Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+" (IP), constr: IP("+systemsoftware+")");
	}

	public static String ipCheck(String ip) {
		Main.debug.println("INVOKED (static) filius.software.vermittlungsschicht.IP, ipCheck("+ip+")");
		String neueIp = "";
		int a;
		StringTokenizer st;

		st = new StringTokenizer(ip, ".");
		try {
			for (int i = 0; i < 4; i++) {
				a = Integer.parseInt(st.nextToken());
				if ((a < 0) || (a > 255)) {
					return null;
				}
				else {
					neueIp += "" + a;
					if (i < 3) {
						neueIp += ".";
					}
				}
			}
			return neueIp;
		}
		catch (Exception e) {
		  	Main.debug.println("INFO: ipCheck: keine gültige IP-Adresse: '"+ip+"'");
			return null;
		}
	}
	
	/** Hilfsmethode zum Versenden eines Broadcast-Pakets */
	private void sendeBroadcast(IpPaket ipPaket) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (IP), sendeBroadcast("+ipPaket.toString()+")");
		InternetKnoten knoten;
		NetzwerkInterface nic;
		ListIterator it;
		InternetKnotenBetriebssystem bs;

		// Damit Broadcast-Pakete nicht in Zyklen gesendet werden,
		// wird das Feld Time-to-Live (TTL) auf 1 gesetzt. Damit
		// wird es von keinem Knoten weitergeschickt
		ipPaket.setTtl(1);

		knoten = (InternetKnoten) holeSystemSoftware().getKnoten();
		bs = (InternetKnotenBetriebssystem) holeSystemSoftware();

		it = knoten.getNetzwerkInterfaces().listIterator();
		while (it.hasNext()) {
			nic = (NetzwerkInterface) it.next();

			// Broadcast-Nachrichten werden nur im lokalen Rechnernetz
			// verschickt
			if (gleichesRechnernetz(ipPaket.getSender(), nic.getIp(), nic
					.getSubnetzMaske())) {
				bs.holeEthernet().senden(ipPaket, nic.getMac(),
						"FF:FF:FF:FF:FF:FF", EthernetFrame.IP);
			}
		}
	}

	/**
	 * Hilfsmethode fuer Pakete, die fuer den eigenen Knoten bestimmt sind
	 *
	 * @param segment
	 *            das zu verarbeitende Segment
	 */
	private void benachrichtigeTransportschicht(IpPaket paket) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (IP), benachrichtigeTransportschicht("+paket.toString()+")");
		if (paket.getSegment() instanceof TcpSegment) {
			synchronized (ipPaketListeTCP) {
				ipPaketListeTCP.add(paket);
				ipPaketListeTCP.notify();
			}
		}
		else if (paket.getSegment() instanceof UdpSegment) {
			synchronized (ipPaketListeUDP) {
				ipPaketListeUDP.add(paket);
				ipPaketListeUDP.notify();
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
	private void sendeUnicast(IpPaket paket, String gateway,
			String schnittstelle) throws VerbindungsException {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (IP), sendeUnicast("+paket.toString()+","+gateway+","+schnittstelle+")");
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
		if (gleichesRechnernetz(paket.getEmpfaenger(), schnittstelle, netzmaske)) {
			//Main.debug
			//		.println("Vermittlung: Ziel-Adresse im lokalen Rechnernetz");
			zielMacAdresse = bs.holeARP().holeARPTabellenEintrag(
					paket.getEmpfaenger());
		}
		// adressierter Knoten ist ueber Gateway zu erreichen
		else {
			//Main.debug
					//.println("Vermittlung: Ziel-Adresse ueber Gateway zu erreichen");
			zielMacAdresse = bs.holeARP().holeARPTabellenEintrag(gateway);
		}

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
					+ " " + paket.getEmpfaenger() + " " + messages.getString("sw_ip_msg2")
					+ " " + zielMacAdresse + " " + messages.getString("sw_ip_msg3")
					+ " " + gateway);
		}
	}

	/**
	 * Methode zum Versenden eines IP-Pakets. Zunaechst wird die
	 * Sender-IP-Adresse an Hand der Weiterleitungstabelle bestimmt (die
	 * Schnittstelle, ueber die das Paket versendet wird). Dann wird das Paket
	 * erzeugt und schliesslich an die Methode weiterleitenPaket() uebergeben.
	 *
	 * @param zielIp
	 *            Gibt die Ziel-IP-Adresse an
	 * @param protokoll
	 *            Der Parameter Protokoll gibt die Protokollnummer an. Dabei
	 *            steht die Nummer 6 fuer das Protokoll TCP
	 * @param segment -
	 *            Enthaellt das erzeugte Segment mit den Nutzdaten.
	 * @throws VerbindungsException
	 */
	public void senden(String zielIp, int protokoll, int ttl, Object segment)
			throws VerbindungsException {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (IP), senden("+zielIp+","+protokoll+","+ttl+","+segment+")");
		IpPaket ipPaket;
		String quellIp;
		InternetKnotenBetriebssystem bs;
		String[] tabellenEintrag;

		//Main.debug.println(getClass().toString()
				//+ "\n\tsenden() wurde aufgerufen" + "\n\tZiel-Adresse: "
				//+ zielIp + "\n\tDaten: " + segment.toString());

		bs = (InternetKnotenBetriebssystem) holeSystemSoftware();

		ipPaket = new IpPaket();
		ipPaket.setEmpfaenger(zielIp);
		ipPaket.setProtocol(protokoll);
		ipPaket.setTtl(ttl);
		ipPaket.setSegment(segment);

		// Broadcast-Nachricht
		if (ipPaket.getEmpfaenger().equals("255.255.255.255")) {
			ipPaket.setSender(bs.holeIPAdresse());
			sendeBroadcast(ipPaket);
		}
		else {
			tabellenEintrag = bs.getWeiterleitungstabelle()
					.holeWeiterleitungsZiele(zielIp);
			if (tabellenEintrag != null) {
				ipPaket.setSender(tabellenEintrag[1]);
				weiterleitenPaket(ipPaket);
			}
			else {
				bs.benachrichtigeBeobacher(messages.getString("sw_ip_msg4")
						+ " \"" + bs.getKnoten().getName() + "\"!\n"
						+ messages.getString("sw_ip_msg5") + " " + zielIp + " "
						+ messages.getString("sw_ip_msg6"));
			}
		}
	}

	/**
	 * Methode zum Weiterleiten eines IP-Pakets. Zunaechst wird geprueft, ob das
	 * Feld Time-to-Live-Feld (TTL) noch nicht abgelaufen ist (d. h. TTL
	 * groesser 0). Wenn diese Bedingung erfuellt ist, wird zunaechst geprueft,
	 * ob es sich um einen Broadcast handelt. Ein solches Paket wird nicht
	 * weitergeleitet sondern nur an die Transportschicht weitergegeben. Sonst
	 * wird die Weiterleitungstabelle nach einem passenden Eintrag abgefragt.
	 * Anhand des zurueckgegebenen Eintrags wird geprueft, ob das Paket fuer den
	 * eigenen Rechner ist oder ob ein Unicast-Paket verschickt werden muss.
	 *
	 * @param ipPaket
	 *            das zu versendende IP-Paket
	 */
	public void weiterleitenPaket(IpPaket ipPaket) throws VerbindungsException {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (IP), weiterleitenPaket("+ipPaket.toString()+")");
		String gateway;
		String schnittstelle;
		String[] routingEintrag;
		InternetKnotenBetriebssystem bs;

		bs = (InternetKnotenBetriebssystem) holeSystemSoftware();

		// Broadcast-Nachricht
		if (ipPaket.getEmpfaenger().equals("255.255.255.255")) {
			benachrichtigeTransportschicht(ipPaket);
		}
		else {
			routingEintrag = bs.getWeiterleitungstabelle().holeWeiterleitungsZiele(ipPaket.getEmpfaenger());

			if (routingEintrag != null) {
				gateway = routingEintrag[0];
				schnittstelle = routingEintrag[1];

				// Wenn das Paket fuer diesen Rechner ist, wird das Paket
				// an die Transportschicht weitergegeben
				if (schnittstelle.equals(LOCALHOST)) {
					benachrichtigeTransportschicht(ipPaket);
				}

				// Damit Pakete nicht in Zyklen gesendet werden
				// wird hier die TTL ueberprueft.
				// TTL wird dekrementiert beim Empfang eines Pakets
				else if (ipPaket.getTtl() > 0) {
					sendeUnicast(ipPaket, gateway, schnittstelle);
				}
			}
			else {
				bs.benachrichtigeBeobacher(messages.getString("sw_ip_msg4")
						+ " \"" + bs.getKnoten().getName() + "\"!\n"
						+ messages.getString("sw_ip_msg5") + " "
						+ ipPaket.getEmpfaenger() + " "
						+ messages.getString("sw_ip_msg6"));
			}
		}
	}

	/**
	 * Methode fuer den Zugriff auf den Puffer fuer eingehende Segmente
	 *
	 * @param protokollTyp
	 *            der Protokolltyp UDP oder TCP (Konstanten der Klasse
	 *            TransportProtokoll)
	 * @return die Liste mit Segmenten fuer UDP- oder TCP-Segmente
	 */
	public LinkedList<IpPaket> holePaketListe(int protokollTyp) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (IP), holePaketListe("+protokollTyp+")");
		if (protokollTyp == IpPaket.TCP)
			return ipPaketListeTCP;
		else if (protokollTyp == IpPaket.UDP)
			return ipPaketListeUDP;
		else
			return null;
	}

	/**
	 * Hier wird der Thread zur Ueberwachung des Puffers fuer eingehende
	 * IP-Pakete der Netzzugangsschicht
	 */
	public void starten() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (IP), starten()");

		thread = new IPThread(this);
		thread.starten();
	}

	/** Der Thread zur Ueberwachung des IP-Pakete-Puffers */
	public void beenden() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (IP), beenden()");
		thread.beenden();
	}
	
	public IPThread getIPThread() {
		return thread;
	}

}
