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
package filius.software.dhcp;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import filius.Main;
import filius.hardware.Verbindung;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.software.clientserver.UDPServerAnwendung;
import filius.software.transportschicht.Socket;

/**
 * In dieser Klasse und in DHCPServerMitarbeiter wird ein Sever fuer Dynamic
 * Host Configuration Protocol implementiert. <br />
 * In dieser Klasse werden die Einstellungen des Server verwaltet. Das Protokoll
 * zur Vereinbarung einer IP-Adresse wird durch DHCPServerMitarbeiter
 * realisiert. D. h. zu jeder eingehenden Anfrage wird ein neuer Mitarbeiter
 * erzeugt.
 */

public class DHCPServer extends UDPServerAnwendung {

	/** niedrigste IP-Adresse, die durch diesen DHCP-Server vergeben wird */
	private String untergrenze;

	/** hoechste IP-Adresse, die durch diesen DHCP-Server vergeben wird */
	private String obergrenze;
	
	/** settings for DHCP server; not necessarily equal to operating system settings **/ 
	private String dhcpGateway = "0.0.0.0";
	private String dhcpDNS = "0.0.0.0";
	private boolean ownSettings = false;

	/**
	 * wie lange ein DHCP-Eintrag gueltig sein soll bzw. die IP-Adresse einer
	 * MAC zugewiesen bleibt (Standardwert)
	 */
	private long ttlvoneinemEintrag = 86400000;

	/** Die zuletzt vergebene IP-Adresse */
	private String letztevergebeneIPm = null;

	// has server already been started (entirely), i.e., can it be contacted by a client
	private boolean started = false;
	
	/** Liste mit vergebenen IP-Adressen mit zugehoeriger MAC-Adresse */
	private LinkedList<IPEintrag> ipListe = new LinkedList<IPEintrag>();

	/** Konstruktor, in dem der UDP-Port 67 gesetzt wird. */
	public DHCPServer() {
		super();

		port = 67;
	}

	/**
	 * Zur Pruefung, ob eine bestimmte IP-Adresse bereits vergeben ist. <br />
	 * Zunaechst wird dazu die Liste vergebener IP-Adressen aufgeraeumt (d.h.
	 * abgelaufene Eintraege entfernt) mit Aufruf von cleanUp()
	 */
	private boolean istIPAdresseFrei(String ip) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DHCPServer), istIPAdresseFrei("+ip+")");
		cleanUp();

		synchronized (ipListe) {
			for (int i = 0; i < ipListe.size(); i++) {
				IPEintrag ipE = (IPEintrag) ipListe.get(i);
				if (ipE.getIp().equalsIgnoreCase(ip)) {
					return false;
				}
			}
		}

		return true;
	}

	public boolean useInternal() {
		return ownSettings;
	}
	
	public void setOwnSettings(boolean val) {
		this.ownSettings = val;
	}
	
	/**
	 * Ein eventuell vorhandener Eintrag zur uebergebenen MAC-Adresse wird
	 * geloescht.
	 */
	public boolean gibMACFrei(String macAdresse) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DHCPServer), gibMACFrei("+macAdresse+")");
		synchronized (ipListe) {
			for (int i = 0; i < ipListe.size(); i++) {
				IPEintrag ipE = (IPEintrag) ipListe.get(i);
				if (ipE.getMAC().equalsIgnoreCase(macAdresse)) {
					ipListe.remove(i);
					i--;
				}
			}
		}

		return true;
	}

	/**
	 * Methode, um die naechste freie IP-Adresse zu erhalten.
	 * <ol>
	 * <li> Beginnend mit der Untergrenze werden alle IP-Adressen im Intervall
	 * von Untergrenze bis Obergrenze geprueft, ob sie noch zu vergeben sind.
	 * </li>
	 * <li> die gefundene IP-Adresse wird zunaechst mit einer kurzen TTL
	 * reserviert, d.h. in die Liste vergebener IP-Adressen eingetragen (TTL = 4
	 * sek. mal Verzoegerungsfaktor der Verbindungsleitungen)</li>
	 * <li> Rueckgabe der reservierten IP-Adresse </li>
	 * </ol>
	 *
	 */
	synchronized String reserviereFreieIP(String maddr) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DHCPServer), reserviereFreiIP("+maddr+")");
		String freieIP;

		synchronized (ipListe) {
			freieIP = letztevergebeneIPm;

			while (freieIP != null && !istIPAdresseFrei(freieIP)) {
				freieIP = naechsteIPAdresse(freieIP);
			}
		}

		if (freieIP != null) {
			reserviereIPAdresse(maddr, freieIP, System.currentTimeMillis()
					+ (long) (Verbindung.holeRTT() * 20));
			letztevergebeneIPm = freieIP;
		}

		return freieIP;
	}

	/**
	 * Weisst einer mac-Adresse eine IP für die angegebene ttl zu. Diese
	 * Funktion wird normalerweise nur von den DHCPServerMitarbeitern
	 * ausgeführt, kann aber auch "manuell" aufgerufen werden. Das richtige
	 * Setzen der ttl ist hierbei zu beachten. Sobald System.currentMills() >=
	 * ttl ist, ist die time to life abgelaufen. die ttl muss also entsprechend
	 * hoch gesetzt werden. <br />
	 * <b>Achtung! </b> Wenn die angeforderte Adresse bereits vergeben ist,
	 * erfolgt keine Zuweisung!
	 *
	 * @param mac
	 * @param ip
	 * @param ttl
	 *            wenn 0, dann wird die Standard-TTL verwendet
	 * @return
	 */
	synchronized boolean reserviereIPAdresse(String mac, String ip, long ttl) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DHCPServer), reserviereIPAdresse("+mac+","+ip+","+ttl+")");
		if (istIPAdresseFrei(ip)) {
			synchronized (ipListe) {
				IPEintrag ipE = new IPEintrag();
				ipE.setMAC(mac);
				if (ttl == 0)
					ipE.setTtl(System.currentTimeMillis() + ttlvoneinemEintrag);
				else
					ipE.setTtl(ttl); // LEASETIME BEACHTEN!
				ipE.setIp(ip);
				ipListe.add(ipE);
			}
			return true;
		} else
			return false;
	}

	/**
	 * Entfernt abgelaufene Eintraege. Wird automatisch vor jeder Suche nach der
	 * naechsten freien IP-Adresse ausgefuehrt.
	 */
	private void cleanUp() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DHCPServer), cleanUp()");
		IPEintrag ipE;
		Iterator it;
		LinkedList<IPEintrag> abgelaufeneEintraege;

		abgelaufeneEintraege = new LinkedList<IPEintrag>();
		synchronized (ipListe) {
			it = ipListe.listIterator();
			while (it.hasNext()) {
				ipE = (IPEintrag) it.next();
				if (ipE.getTtl() < System.currentTimeMillis()) {
					abgelaufeneEintraege.add(ipE);
				}
			}
			it = abgelaufeneEintraege.listIterator();
			while (it.hasNext()) {
				ipListe.remove(it.next());
			}
		}
	}

	/**
	 * Berechnet die folgende IP-Adresse an Hand der Netzmaske. Wenn die
	 * Obergrenze erreicht ist, wird <b>nicht</b> wieder bei der Untergrenze
	 * begonnen. <br />
	 * Dazu wird zunaechst geprueft, ob die Netzkennung der uebergebenen
	 * IP-Adresse identisch zu denen der Unter- und Obergrenze ist. <br />
	 * Dann wird geprueft, ob die Rechnerkennung der uebergebenen IP-Adresse
	 * zwischen Unter- und Obergrenze liegt. <br />
	 * Schliesslich wird die naechste Rechnerkennung zurueck gegeben.
	 *
	 * @param ip
	 *            Die IP-Adresse, zu der die folgende Adresse gesucht wird.
	 * @return die Folgeadresse oder null, wenn es keine Adresse im Intervall
	 *         zwischen Unter- und Obergrenze gibt, die auf die uebergebene
	 *         Adresse folgt.
	 */
	private String naechsteIPAdresse(String ip) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DHCPServer), naechsteIPAdresse("+ip+")");
		// die Netzmaske als eine Zahl
		long maske = 0;
		// zu jeder IP-Adresse
		// [0] die vollstaendige Adresse,
		// [1] die Netzkennung und
		// [2] die Rechnerkennung
		// jeweils als 4-Tupel von Zahlen
		long[] adresse, untergrenze, obergrenze;
		StringTokenizer tokenizer;
		long tmp;
		String naechsteAdresse;

		// --------------------------------------------------------
		// Initialisierung der Arrays
		tokenizer = new StringTokenizer(getSubnetzmaske(), ".");
		for (int i = 0; i < 4; i++) {
			maske = maske * 256 + Integer.parseInt(tokenizer.nextToken());
		}

		tokenizer = new StringTokenizer(ip, ".");
		adresse = new long[3];
		adresse[0] = 0;
		for (int i = 0; i < 4; i++) {
			adresse[0] = adresse[0] * 256
					+ Integer.parseInt(tokenizer.nextToken());
		}
		adresse[1] = adresse[0] & maske;
		adresse[2] = adresse[0] & (4294967295l - maske);

		tokenizer = new StringTokenizer(getUntergrenze(), ".");
		untergrenze = new long[3];
		untergrenze[0] = 0;
		for (int i = 0; i < 4; i++) {
			untergrenze[0] = untergrenze[0] * 256
					+ Integer.parseInt(tokenizer.nextToken());
		}
		untergrenze[1] = untergrenze[0] & maske;
		untergrenze[2] = untergrenze[0] & (4294967295l - maske);

		tokenizer = new StringTokenizer(getObergrenze(), ".");
		obergrenze = new long[3];
		obergrenze[0] = 0;
		for (int i = 0; i < 4; i++) {
			obergrenze[0] = obergrenze[0] * 256
					+ Integer.parseInt(tokenizer.nextToken());
		}
		obergrenze[1] = obergrenze[0] & maske;
		obergrenze[2] = obergrenze[0] & (4294967295l - maske);

		// --------------------------------------------------------
		// Pruefung der Netzkennung und der Rechnerkennung der uebergebenen
		// IP-Adresse:
		// im gueltigen Intervall zwischen Unter- und Obergrenze?
		if ((adresse[1] != untergrenze[1]) || (adresse[1] != obergrenze[1])
				|| (adresse[2] < untergrenze[2])
				|| (adresse[2] >= obergrenze[2])
				|| untergrenze[2] == obergrenze[2]) {
			//Main.debug.println(getClass() + " naechsteIPAdresse():"
					//+ "\n\tkeine weitere Adresse im Intervall verfuegbar"
					//+ "\n\t " + untergrenze[0] + " (" + getUntergrenze()
					//+ ") - " + obergrenze[0] + " (" + getObergrenze() + ")");
			return null;
		}

		// --------------------------------------------------------
		// naechste IP-Adresse im Intervall berechnen und Rueckgabe als String
		tmp = adresse[0] + 1;

		naechsteAdresse = (tmp / 16777216) + "." + ((tmp % 16777216) / 65536)
				+ "." + ((tmp % 65536) / 256) + "." + (tmp % 256);

		return naechsteAdresse;
	}

	public String getObergrenze() {
		return obergrenze;
	}

	public void setObergrenze(String obergrenze) {
		this.obergrenze = obergrenze;
	}

	public String getUntergrenze() {
		return untergrenze;
	}

	public void setUntergrenze(String untergrenze) {
		this.untergrenze = untergrenze;
	}

	public String getDnsserverip() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DHCPServer), getDnsserverip()");
		if(ownSettings) {
			return dhcpDNS;
		}
		else {
			String dns;
	
			dns = getSystemSoftware().getDNSServer();
			if (dns == null || dns.equals("")) {
				dns = "0.0.0.0";
			}
	
			return dns;
		}
	}
	
	public void setDnsserverip(String ip) {
		this.dhcpDNS = ip;
	}

	public String getGatewayip() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DHCPServer), getGatewayip()");
		if(ownSettings) {
			return dhcpGateway;
		}
		else {
			String gateway;
	
			gateway = getSystemSoftware().getStandardGateway();
			if (gateway == null || gateway.equals("")) {
				gateway = "0.0.0.0";
			}
	
			return gateway;
		}
	}
	
	public void setGatewayip(String ip) {
		this.dhcpGateway = ip;
	}

	public String getSubnetzmaske() {
		return getSystemSoftware().holeNetzmaske();
	}

	public void starten() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DHCPServer), starten()");
			started = false;
			ipListe = new LinkedList<IPEintrag>();
			letztevergebeneIPm = untergrenze;
	
			super.starten();
			
			/*
			do {
				started = (socket != null); // klären, ob Socket bereits fertig...
				if (!started) { 
					try { Thread.sleep(100); }
					catch (InterruptedException e) {}
				}
			} while (!started);
			Main.debug.println("DHCP server started...");
			*/
	}
	
	/**
	 * Diese Methode wird bei der ersten eingehenden DHCP-Anfrage aufgerufen.
	 * Der DHCP-Server verfuegt naemlich nur ueber einen Mitarbeiter, weil es
	 * nur einen Port mit der Gegenstelle "0.0.0.0:68" gibt.
	 */
	protected void neuerMitarbeiter(Socket socket) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DHCPServer), neuerMitarbeiter("+socket+")");
		DHCPServerMitarbeiter dhcpMitarbeiter;

		dhcpMitarbeiter = new DHCPServerMitarbeiter(this, socket);
		dhcpMitarbeiter.starten();
		mitarbeiter.add(dhcpMitarbeiter);

		//Main.debug.println("\tSocket-Zieladresse: " + socket.holeZielIPAdresse() + ":"
				//+ socket.holeZielPort());
	}

}
