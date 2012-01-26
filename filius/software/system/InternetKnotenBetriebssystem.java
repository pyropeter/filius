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
package filius.software.system;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map.Entry;

import filius.Main;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.FiliusClassLoader;
import filius.rahmenprogramm.Information;
import filius.software.Anwendung;
import filius.software.dns.Resolver;
import filius.software.netzzugangsschicht.Ethernet;
import filius.software.transportschicht.TCP;
import filius.software.transportschicht.UDP;
import filius.software.vermittlungsschicht.ARP;
import filius.software.vermittlungsschicht.ICMP;
import filius.software.vermittlungsschicht.IP;
import filius.software.vermittlungsschicht.Weiterleitungstabelle;

/**
 * Diese Klasse implementiert die Funktionalitaet eines Betriebssystems für
 * Internetknoten. Dass heisst, das Betriebssystem unterstuetzt den gesamten
 * Protokollstapel, der fuer den Betrieb von Internetanwendungen benoetigt wird.
 * <br />
 * Ausserdem stellt diese Klasse eine Schnittstelle fuer den Zugriff auf
 * <ol>
 * <li> die erste Netzwerkkarte, </li>
 * <li> den DNS-Client (Resolver) </li>
 * </ol>
 * zur Verfuegung. (als Entwurfsmuster Fassade)
 */
public abstract class InternetKnotenBetriebssystem extends SystemSoftware {

	private static final long serialVersionUID = 1L;

	/** Das lokale Dateisystem eines Rechners */
	private Dateisystem dateisystem;

	/**
	 * Die installierten Anwendungen. Sie werden mit dem Anwendungsnamen als
	 * Schluessel in einer HashMap gespeichert.
	 */
	private HashMap<String, Anwendung> installierteAnwendung;

	/**
	 * Mit Hilfe des DNS-Client werden Rechneradressen, die als Domainname
	 * uebergeben werden aufgeloest. Ausserdem wird er benutzt, um jegliche
	 * Anfragen an den DNS-Server zu stellen.
	 */
	private Resolver dnsclient;

	/** Die Transportschicht wird durch TCP und UDP implementiert. */
	private TCP tcp;

	/** Die Transportschicht wird durch TCP und UDP implementiert. */
	private UDP udp;

	/**
	 * Die Vermittlungsschicht wird durch das Address Resolution Protocol (ARP)
	 * und das Internetprotokoll implementiert. Dafür stehen die Klassen ARP und
	 * Vermittlung.
	 */
	private ARP arpVermittlung;

	/**
	 * Die Vermittlungsschicht wird durch das Address Resolution Protocol (ARP)
	 * und das Internetprotokoll implementiert. Dafür stehen die Klassen ARP und
	 * Vermittlung.
	 */
	private IP vermittlung;
	private ICMP icmpVermittlung;
	
	/**
	 * Die Weiterleitungstabelle enthaelt neben Standardeintraegen ggfs. auch
	 * durch den Anwender hinzugefuegte Eintraege. Diese zusaetzliche
	 * Funktionalitaet wird derzeit nur durch den Vermittlungsrechner genutzt.
	 * Generell wird die Entscheidung, ueber welche Netzwerkkarte Daten
	 * versendet werden, auf Grundlage der Weiterleitungstabelle getroffen. Sie
	 * kann nicht der Vermittlungsschicht zugeordnet werden, weil sie mit einem
	 * Projekt persistent gespeichert werden muss.
	 */
	private Weiterleitungstabelle weiterleitungstabelle;

	/**
	 * Die Netzzugangsschicht wird durch das Ethernet-Protokoll implementiert.
	 * Die zugehoerigen Threads werden vom Betriebssystem gestartet und beendet.
	 */
	private Ethernet ethernet;

	/**
	 * Konstruktor fuer das Betriebssystem eines Internetknotens. Hier werden
	 * <ul>
	 * <li> die Schichten initialisiert, </li>
	 * <li> die installierten Anwendungen zurueck gesetzt, </li>
	 * <li> das Dateisystem initialisiert, </li>
	 * <li> der DNS-Client erzeugt. </li>
	 * </ul>
	 */
	public InternetKnotenBetriebssystem() {
		super();
		Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), constr: InternetKnotenBetriebssystem()");

		installierteAnwendung = new HashMap<String, Anwendung>();

		weiterleitungstabelle = new Weiterleitungstabelle();
		weiterleitungstabelle.setInternetKnotenBetriebssystem(this);

		arpVermittlung = new ARP(this);
		vermittlung = new IP(this);
		icmpVermittlung = new ICMP(this);
		ethernet = new Ethernet(this);

		tcp = new TCP(this);
		udp = new UDP(this);

		this.dateisystem = new Dateisystem();

		dnsclient = new Resolver();
		dnsclient.setSystemSoftware(this);
		
		// print IDs for all network layers and the according node --> for providing debug support in log file
		Main.debug.println("DEBUG: InternetKnotenBetriebssystem ("+this.hashCode()+")\n"
//						 + "\tKnoten: "+this.getKnoten().hashCode()+" ("+this.getKnoten().getName()+", "+this.getKnoten().holeHardwareTyp()+")\n"
						 + "\tEthernet: "+ethernet.hashCode()+"\n"
						 + "\tARP: "+arpVermittlung.hashCode()+"\n"
						 + "\tIP: "+vermittlung.hashCode()+"\n"
						 + "\tICMP: "+icmpVermittlung.hashCode()+"\n"
						 + "\tTCP: "+tcp.hashCode()+"\n"
						 + "\tUDP: "+udp.hashCode()
						 );
	}

	/**
	 * Zum beenden der Protokoll-Threads und der Anwendungs-Threads.
	 *
	 * @see filius.software.system.SystemSoftware.beenden()
	 */
	public void beenden() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), beenden()");
		Iterator it;

		// Die einzelnen Protokoll-Threads werden beginnend
		// mit der untersten Schicht beendet.

		// Netzzugangsschicht
		ethernet.beenden();

		// Vermittlungsschicht
		arpVermittlung.beenden();
		vermittlung.beenden();
		icmpVermittlung.beenden();

		// Transportschicht
		tcp.beenden();
		udp.beenden();

		dnsclient.beenden();

		it = installierteAnwendung.entrySet().iterator();
		while (it.hasNext()) {
			((Anwendung) ((Entry) it.next()).getValue()).beenden();
		}
	}

	private void printDebugInfo() {
		Main.debug.println("DEBUG ("+this.hashCode()+"): start InternetKnotenBetriebssystem");
		if (this.getKnoten()!=null) {
			Main.debug.println("DEBUG ("+this.hashCode()+") - Hostname = "+this.getKnoten().getName());
			Main.debug.print("DEBUG ("+this.hashCode()+") - Hardwaretyp = '");
			if (getKnoten() instanceof filius.hardware.knoten.Notebook) { 
				Main.debug.println("Notebook'");
			}
			else if (getKnoten() instanceof filius.hardware.knoten.Rechner) { 
				Main.debug.println("Rechner'"); 
			}
			else if (getKnoten() instanceof filius.hardware.knoten.Vermittlungsrechner) { 
				Main.debug.println("Vermittlungsrechner'"); 
			}
			else { Main.debug.println("<unknown>'"); }
		}
		else {
			Main.debug.println("DEBUG ("+this.hashCode()+") - Hostname = <unknown>");
			Main.debug.println("DEBUG ("+this.hashCode()+") - Hardwaretyp = <unknown>");
		}
		Main.debug.println("DEBUG ("+this.hashCode()+") - ETHER = "+ethernet.hashCode());
		LinkedList threads = ethernet.getEthernetThreads();
		if (threads!=null)
			for(int i=0; i<threads.size(); i++)
				Main.debug.println("DEBUG ("+this.hashCode()+")      - ETHER T-"+i+" = "+((filius.software.netzzugangsschicht.EthernetThread) threads.get(i)).hashCode());
		Main.debug.println("DEBUG ("+this.hashCode()+") - ARP = "+arpVermittlung.hashCode());
		filius.software.vermittlungsschicht.ARPThread thread = arpVermittlung.getARPThread();
		if (thread!=null)
			Main.debug.println("DEBUG ("+this.hashCode()+")      - ARP T = "+thread.hashCode());
		Main.debug.println("DEBUG ("+this.hashCode()+") - IP = "+vermittlung.hashCode());
		filius.software.vermittlungsschicht.IPThread IPthread = vermittlung.getIPThread();
		if (IPthread!=null)
			Main.debug.println("DEBUG ("+this.hashCode()+")      - IP T = "+IPthread.hashCode());
		Main.debug.println("DEBUG ("+this.hashCode()+") - ICMP = "+icmpVermittlung.hashCode());
		filius.software.vermittlungsschicht.ICMPThread ICMPthread = icmpVermittlung.getICMPThread();
		if (IPthread!=null)
			Main.debug.println("DEBUG ("+this.hashCode()+")      - ICMP T = "+ICMPthread.hashCode());
		Main.debug.println("DEBUG ("+this.hashCode()+") - TCP = "+tcp.hashCode());
		Main.debug.println("DEBUG ("+this.hashCode()+") - UDP = "+udp.hashCode());
		if (this.getKnoten()!=null) {
			if (getKnoten() instanceof filius.hardware.knoten.Notebook) { 
				NetzwerkInterface nic = ((NetzwerkInterface) ((filius.hardware.knoten.Notebook) getKnoten()).getNetzwerkInterfaces().getFirst());
				Main.debug.println("DEBUG ("+this.hashCode()+") - NIC: {IP="+nic.getIp()+"/"+nic.getSubnetzMaske()+", MAC="+nic.getMac()+", DNS="+nic.getDns()+", GW="+nic.getGateway()+"}");
			}
			else if (getKnoten() instanceof filius.hardware.knoten.Rechner) { 
				NetzwerkInterface nic = ((NetzwerkInterface) ((filius.hardware.knoten.Rechner) getKnoten()).getNetzwerkInterfaces().getFirst());
				Main.debug.println("DEBUG ("+this.hashCode()+") - NIC: {IP="+nic.getIp()+"/"+nic.getSubnetzMaske()+", MAC="+nic.getMac()+", DNS="+nic.getDns()+", GW="+nic.getGateway()+"}");
			}
			else if (getKnoten() instanceof filius.hardware.knoten.Vermittlungsrechner) { 
				Iterator it = (((filius.hardware.knoten.Vermittlungsrechner) getKnoten()).getNetzwerkInterfaces().iterator());
				int nicNr=0;
				while(it.hasNext()) {
					NetzwerkInterface nic = (NetzwerkInterface) it.next();
					Main.debug.println("DEBUG ("+this.hashCode()+") - NIC"+nicNr+": {IP="+nic.getIp()+"/"+nic.getSubnetzMaske()+", MAC="+nic.getMac()+", DNS="+nic.getDns()+", GW="+nic.getGateway()+"}");
					nicNr++;
				}
			}
		}
		else {
			Main.debug.println("DEBUG ("+this.hashCode()+") - NIC=<unknown>");
		}
		getWeiterleitungstabelle().printTabelle(Integer.toString(this.hashCode()));
	}
	
	/**
	 * Methode zum starten der Protokoll-Threads und der Anwendungen.
	 *
	 * @see filius.software.system.SystemSoftware.starten()
	 */
	public void starten() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), starten()");
		Iterator it;
		Entry entry;

		// Die Protokoll-Threads der einzelnen Schichten werden
		// beginnend mit der untersten Schicht gestartet.
		ethernet.starten();
		arpVermittlung.starten();
		vermittlung.starten();
		icmpVermittlung.starten();
		tcp.starten();
		udp.starten();

		printDebugInfo();   // print all relevant debug information in log file to follow these data

		it = installierteAnwendung.entrySet().iterator();
		while (it.hasNext()) {
			entry = (Entry) it.next();
			if (entry.getValue() != null)
				((Anwendung) entry.getValue()).starten();
		}
	}

	/** Methode fuer den Zugriff auf den DNS-Resolver */
	public Resolver holeDNSClient() {
		return dnsclient;
	}

	/**
	 * Methode fuer den Zugriff auf das Transport Control Protocol (TCP).
	 */
	public TCP holeTcp() {
		return tcp;
	}

	/**
	 * Methode fuer den Zugriff auf das User Datagram Protocol (UDP).
	 */
	public UDP holeUdp() {
		return udp;
	}

	/**
	 * Methode fuer den Zugriff auf das Address Resolution Protocol (ARP).
	 */
	public ARP holeARP() {
		return arpVermittlung;
	}

	/**
	 * Methode fuer den Zugriff auf das Internet Control Message Protocol (ICMP).
	 */
	public ICMP holeICMP() {
		return icmpVermittlung;
	}
	
	/**
	 * Methode fuer den Zugriff auf das Internet Protocol (IP).
	 */
	public IP holeIP() {
		return vermittlung;
	}

	/** Methode fuer den Zugriff auf das Ethernet-Protokoll */
	public Ethernet holeEthernet() {
		return ethernet;
	}

	/**
	 * Methode fuer den Zugriff auf das Dateisystem Diese Methode wird fuer das
	 * Speichern des Dateisystems in einer Projektdatei benoetigt. (JavaBean-
	 * konformer Zugriff erforderlich)
	 */
	public Dateisystem getDateisystem() {
		return dateisystem;
	}

	/**
	 * Methode, um das Dateisystem zu setzen. Diese Methode wird fuer das
	 * Speichern des Dateisystems in einer Projektdatei benoetigt. (JavaBean-
	 * konformer Zugriff erforderlich)
	 *
	 * @param dateisystem
	 */
	public void setDateisystem(Dateisystem dateisystem) {
		this.dateisystem = dateisystem;
	}

	/**
	 * Methode fuer den Zugriff auf die Hash-Map zur Verwaltung der
	 * installierten Anwendungen. Diese Methode wird benoetigt, um den
	 * Anforderungen an JavaBeans gerecht zu werden.
	 */
	public void setInstallierteAnwendungen(
			HashMap<String, Anwendung> anwendungen) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), setInstallierteAnwendungen()");
		this.installierteAnwendung = anwendungen;
//		printInstallierteAnwendungen();
	}

	/**
	 * Methode zur Ausgabe auf der aktuell installierten Anwendungen auf der
	 * Standardausgabe
	 */
	private void printInstallierteAnwendungen() {
		Iterator it = installierteAnwendung.entrySet().iterator();

		Main.debug.println("\tInternetKnotenBetriebssystem: installierte Anwendungen:");
		while (it.hasNext()) {
			Main.debug.println("\t  - " + ((Entry) it.next()).getKey().toString());
		}
		Main.debug.println("\t  ges: " + installierteAnwendung.toString());
	}

	/**
	 * Methode fuer den Zugriff auf die Hash-Map zur Verwaltung der
	 * installierten Anwendungen. Diese Methode wird benoetigt, um den
	 * Anforderungen an JavaBeans gerecht zu werden.
	 */
	public HashMap<String, Anwendung> getInstallierteAnwendungen() {
		return installierteAnwendung;
	}

	/**
	 * Methode fuer den Zugriff auf eine bereits installierte Anwendung.
	 *
	 * @param anwendungsklasse
	 *            Klasse der Anwendung
	 * @return das Programm / die Anwendung
	 */
	public Anwendung holeSoftware(String anwendungsklasse) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), holeSoftware("+anwendungsklasse+")");
		Anwendung anwendung;

		if (anwendungsklasse == null)
			return null;
		anwendung = (Anwendung) installierteAnwendung.get(anwendungsklasse);
		if (anwendung == null) {
			return null;
		} else {
			return anwendung;
		}

	}


	/**
	 * Methode zum Entfernen einer installierten Anwendung.
	 *
	 * @param awKlasse
	 *            Klasse der zu entfernenden Anwendung
	 * @return ob eine Anwendung entfernt wurde
	 */
	public boolean entferneSoftware(String awKlasse) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), entferneSoftware("+awKlasse+")");
		printInstallierteAnwendungen();  // DEBUG
		boolean entfernt = false;
		Iterator it = installierteAnwendung.entrySet().iterator();

		while (it.hasNext() && !entfernt) {
			if (awKlasse.equals((String) ((Entry) it.next()).getKey())) {
				it.remove();
				entfernt = true;
			}
		}
		return entfernt;
	}

	public boolean installiereSoftware(String klassenname) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), installiereSoftware("+klassenname+")");
		printInstallierteAnwendungen(); // DEBUG
		Anwendung neueAnwendung = null;
		boolean erfolg = false;
		LinkedList liste = null;
		HashMap tmpMap;
		Class<?> cl;
		ListIterator it;

		if (holeSoftware(klassenname) != null) {
//			Main.debug.println(klassenname + " ist bereits installiert!");
			return false;
		} else {
			try {
				liste = Information.getInformation().ladeProgrammListe();
			} catch (Exception e) {
				e.printStackTrace(Main.debug);
				return false;
			}

			it = liste.listIterator();
			while (it.hasNext() && !erfolg) {
				tmpMap = (HashMap) it.next();
				if (klassenname.equals((String) tmpMap.get("Klasse"))) {

					try {
						cl = Class.forName(klassenname, true, FiliusClassLoader
								.getInstance(Thread.currentThread()
										.getContextClassLoader()));
						try {
							neueAnwendung = (Anwendung) cl.getConstructor()
									.newInstance();
							neueAnwendung.setSystemSoftware(this);
						} catch (Exception e) {
							e.printStackTrace(Main.debug);
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace(Main.debug);
					}
					if (neueAnwendung != null) {
						installierteAnwendung.put(klassenname, neueAnwendung);
						erfolg = true;
					}
					//Main.debug.println("Installiert: " + klassenname);
					//Main.debug
							//.println(" SIZE: " + installierteAnwendung.size());
					//Main.debug.println("Anwendung--->"
							//+ installierteAnwendung.toString());
				}
			}
		}
		return erfolg;
	}

	public boolean deinstalliereAnwendung(String anwendungsName) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), deinstalliereAnwendung("+anwendungsName+")");
		printInstallierteAnwendungen();  // DEBUG
		Anwendung anwendung;

		if (anwendungsName == null)
			return false;
		anwendung = (Anwendung) installierteAnwendung.get(anwendungsName);
		if (anwendung == null) {
			return false;
		} else {
			installierteAnwendung.remove(anwendung.holeAnwendungsName());
			return true;
		}
	}

	/**
	 * Methode zur Abfrage aller aktuell installierter Anwendungen
	 *
	 * @return ein Array der Anwendungsnamen
	 */
	public Anwendung[] holeArrayInstallierteSoftware() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), holeArrayInstallierteSoftware()");
		Anwendung[] anwendungen;
		Iterator it = installierteAnwendung.entrySet().iterator();

		anwendungen = new Anwendung[installierteAnwendung.size()];
		for (int i = 0; it.hasNext() && i < anwendungen.length; i++) {
			anwendungen[i] = (Anwendung) ((Entry) it.next()).getValue();
		}

//		printInstallierteAnwendungen();

		return anwendungen;
	}

	/**
	 * Methode fuer den JavaBean-konformen Zugriff auf die
	 * Weiterleitungstabelle. Diese Methode wird aber ausserdem von der
	 * Implementierung der Vermittlungsschicht verwendet.
	 */
	public Weiterleitungstabelle getWeiterleitungstabelle() {
		return weiterleitungstabelle;
	}

	/**
	 * Methode fuer den JavaBean-konformen Zugriff auf die
	 * Weiterleitungstabelle.
	 */
	public void setWeiterleitungstabelle(Weiterleitungstabelle tabelle) {
		this.weiterleitungstabelle = tabelle;
	}

	/**
	 * Methode fuer den Zugriff auf die IP-Adresse des Standard-Gateways, aller
	 * Netzwerkkarten.
	 *
	 * @return IP-Adresse der einzigen Netzwerkkarte als String
	 */
	public String getStandardGateway() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), getStandardGateway()");
		InternetKnoten knoten;
		NetzwerkInterface nic;

		if (getKnoten() instanceof InternetKnoten) {
			knoten = (InternetKnoten) getKnoten();

			if (knoten.getNetzwerkInterfaces().size() > 0) {
				nic = (NetzwerkInterface) knoten.getNetzwerkInterfaces().getFirst();
				return nic.getGateway();
			}
		}
		return null;
	}

	/**
	 * Methode zum Einstellen des Standard-Gateways fuer die Netwerkkarten. Das
	 * ist eine Methode des Entwurfsmusters Fassade.
	 *
	 * @param gateway
	 *            IP-Adresse der Netzwerkkarten als String
	 */
	public void setStandardGateway(String gateway) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), setStandardGateway("+gateway+")");
		InternetKnoten knoten;
		NetzwerkInterface nic;
		Iterator<?> it;
		gateway = (gateway != null && gateway.trim().equals("")) ? gateway.trim() : IP.ipCheck(gateway);

		if (gateway != null
				&& EingabenUeberpruefung.isGueltig(gateway,EingabenUeberpruefung.musterIpAdresseAuchLeer)
				&& getKnoten() instanceof InternetKnoten) {
			knoten = (InternetKnoten) getKnoten();
			it = knoten.getNetzwerkInterfaces().listIterator();
			while (it.hasNext()) {
				nic = (NetzwerkInterface) it.next();
				nic.setGateway(gateway);
			}
		}
	}

	/**
	 * Methode zum Einstellen der IP-Adresse fuer die einzige Netwerkkarte. Das
	 * ist eine Methode des Entwurfsmusters Fassade
	 */
	public void setzeIPAdresse(String ip) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), setzeIPAdresse("+ip+")");
		InternetKnoten knoten;
		ip = IP.ipCheck(ip);
		if (ip != null
				&& EingabenUeberpruefung.isGueltig(ip,EingabenUeberpruefung.musterIpAdresse)
				&& getKnoten() instanceof InternetKnoten) {
			knoten = (InternetKnoten) getKnoten();
			((NetzwerkInterface) knoten.getNetzwerkInterfaces().getFirst()).setIp(ip);
			//Main.debug.println("\t"
					//+ ((NetzwerkInterface) knoten.getNetzwerkInterfaces()
							//.getFirst()).getIp());
		}
	}

	/**
	 * Methode fuer den Zugriff auf die IP-Adresse der einzigen Netwerkkarte.
	 * Das ist eine Methode des Entwurfsmusters Fassade
	 */
	public String holeIPAdresse() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), holeIPAdresse()");
		InternetKnoten knoten;

		if (getKnoten() instanceof InternetKnoten) {
			knoten = (InternetKnoten) getKnoten();

			if (knoten.getNetzwerkInterfaces().size() > 0) {
				NetzwerkInterface nic = (NetzwerkInterface) knoten
						.getNetzwerkInterfaces().getFirst();
				return nic.getIp();
			}
		}

		return null;
	}

	/**
	 * Methode fuer den Zugriff auf die MAC-Adresse der einzigen Netwerkkarte.
	 * Das ist eine Methode des Entwurfsmusters Fassade
	 */
	public String holeMACAdresse() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), holeMACAdresse()");
		InternetKnoten knoten;

		if (getKnoten() instanceof InternetKnoten) {
			knoten = (InternetKnoten) getKnoten();

			if (knoten.getNetzwerkInterfaces().size() > 0) {
				NetzwerkInterface nic = (NetzwerkInterface) knoten
						.getNetzwerkInterfaces().getFirst();
				return nic.getMac();
			}
		}

		return null;
	}

	/**
	 * Methode fuer den Zugriff auf die IP-Adresse des DNS-Servers der aller
	 * Netzwerkkarten. Das ist eine Methode des Entwurfsmusters Fassade
	 */
	public String getDNSServer() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), getDNSServer()");
		InternetKnoten knoten;

		if (getKnoten() instanceof InternetKnoten) {
			knoten = (InternetKnoten) getKnoten();

			if (knoten.getNetzwerkInterfaces().size() > 0) {
				NetzwerkInterface nic = (NetzwerkInterface) knoten
						.getNetzwerkInterfaces().getFirst();
				return nic.getDns();
			}
		}
		return null;
	}

	/**
	 * Methode fuer den Zugriff auf die IP-Adresse des DNS-Servers der aller
	 * Netzwerkkarten. Das ist eine Methode des Entwurfsmusters Fassade
	 */
	public void setDNSServer(String dns) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), setDNSServer("+dns+")");
		InternetKnoten knoten;
		NetzwerkInterface nic;
		Iterator<?> it;
		dns = (dns != null && dns.trim().equals("")) ? dns.trim() : IP.ipCheck(dns);

		if (dns != null
				&& EingabenUeberpruefung.isGueltig(dns,EingabenUeberpruefung.musterIpAdresseAuchLeer)
				&& getKnoten() instanceof InternetKnoten) {
			knoten = (InternetKnoten) getKnoten();
			it = knoten.getNetzwerkInterfaces().listIterator();
			while (it.hasNext()) {
				nic = (NetzwerkInterface) it.next();
				nic.setDns(dns);
			}
		}
	}

	/**
	 * Methode fuer den Zugriff auf die Netzmaske der einzigen Netzwerkkarte.
	 * Das ist eine Methode des Entwurfsmusters Fassade
	 */
	public void setzeNetzmaske(String mask) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), setzeNetzmaske("+mask+")");
		InternetKnoten knoten;
		mask = IP.ipCheck(mask);

		if (mask != null
				&& EingabenUeberpruefung.isGueltig(mask,
						EingabenUeberpruefung.musterSubNetz)
				&& getKnoten() instanceof InternetKnoten) {
			knoten = (InternetKnoten) getKnoten();
			((NetzwerkInterface) knoten.getNetzwerkInterfaces().getFirst())
					.setSubnetzMaske(mask);
			//Main.debug.println("\t"
					//+ ((NetzwerkInterface) knoten.getNetzwerkInterfaces()
							//.getFirst()).getSubnetzMaske());
		}
	}

	/**
	 * Methode fuer den Zugriff auf die Netzmaske der einzigen Netzwerkkarte.
	 * Das ist eine Methode des Entwurfsmusters Fassade
	 */
	public String holeNetzmaske() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (InternetKnotenBetriebssystem), holeNetzmaske()");
		InternetKnoten knoten;

		if (getKnoten() instanceof InternetKnoten) {
			knoten = (InternetKnoten) getKnoten();
			return ((NetzwerkInterface) knoten.getNetzwerkInterfaces()
					.getFirst()).getSubnetzMaske();
		}
		return null;

	}
}
