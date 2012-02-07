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

//Netzwerkziel, Netzwerkmaske, ZielIp(Gateway), Schnittstelle
import filius.Main;
import filius.rahmenprogramm.Information;
import filius.software.firewall.Firewall;
import filius.software.firewall.FirewallWebKonfig;
import filius.software.firewall.FirewallWebLog;
import filius.software.www.WebServer;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.Vermittlungsrechner;

import filius.software.rip.RIPTable;
import filius.software.rip.RIPBeacon;
import filius.software.rip.RIPServer;
import filius.software.rip.RIPWeb;

/**
 * Diese Klasse stellt die Funktionalitaet eines Betriebssystems fuer
 * Vermittlungsrechner zur Verfuegung. Spezifisch ist die automatische
 * Installation einer Firewall und eines Webservers mit einer Erweiterung zur
 * Konfiguration der Firewall. Die weitere Funktionalitaet wird von der
 * Oberklasse (InternetKnotenBetriebssystem) zur Verfuegung gestellt.
 */
public class VermittlungsrechnerBetriebssystem extends
		InternetKnotenBetriebssystem {

	private static final long serialVersionUID = 1L;

	private boolean ripEnabled;

	private RIPTable riptable;
	private RIPBeacon ripbeacon;
	private RIPServer ripserver;

	/** Konstruktor mit Initialisierung von Firewall und Webserver */
	public VermittlungsrechnerBetriebssystem() {
		super();
		Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+" (VermittlungsrechnerBetriebssystem), constr: VermittlungsrechnerBetriebssystem()");

		initialisiereAnwendungen();
	}
	
	public void setKnoten(Knoten vermittlungsrechner) {
		super.setKnoten(vermittlungsrechner);
	}

	/**
	 * Methode zur initialisierung der Firewall und des Web-Servers mit den
	 * Erweiterungen fuer den Zugriff auf die Firewall ueber eine
	 * Web-Schnittstelle
	 */
	private void initialisiereAnwendungen() {
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
		        + " (VermittlungsrechnerBetriebssystem), initialisiereFirewallUndWebserver()");
		FirewallWebLog weblog;
		FirewallWebKonfig webkonfig;
		WebServer server = null;
		Firewall firewall = null;

		// Installation von Firewall und Webserver
		installiereSoftware("filius.software.firewall.Firewall");
		installiereSoftware("filius.software.www.WebServer");
		// while(firewall==null) {
		firewall = this.holeFirewall();
		// }
		// while(server==null) {
		server = this.holeWebServer();
		// }
		firewall.setModus(Firewall.GATEWAY);

		// Erweiterung des Webservers fuer die Anzeige der
		// Log-Eintraege der Firewall
		weblog = new FirewallWebLog();
		weblog.setFirewall(firewall);
		weblog.setPfad("log.html");
		server.setzePlugIn(weblog);

		// Erweiterung des Webservers fuer die Konfiguration
		// der Firewall
		webkonfig = new FirewallWebKonfig();
		webkonfig.setWebserver(server);
		webkonfig.setFirewall(firewall);
		webkonfig.setPfad("konfig.html");
		server.setzePlugIn(webkonfig);

		server.erzeugeIndexDatei(Information.getInformation().getProgrammPfad() + "config/firewall_index_"+Information.getInformation().getLocale()+".txt");

		// ------------- RIP ------------------
		riptable = new RIPTable();
		ripserver = new RIPServer();
		ripserver.setSystemSoftware(this);
		ripbeacon = new RIPBeacon();
		ripbeacon.setSystemSoftware(this);

		RIPWeb ripweb = new RIPWeb(riptable);
		ripweb.setPfad("routes.html");
		server.setzePlugIn(ripweb);
	}

	/**
	 * Starten des Webservers
	 *
	 * @see filius.software.system.InternetKnotenBetriebssystem.starten()
	 */
	public void starten() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (VermittlungsrechnerBetriebssystem), starten()");

		super.starten();

		// Startet den Web-Server
		holeWebServer().setAktiv(true);

		if (ripEnabled) {
			riptable.reset();
			riptable.addLocalRoutes((InternetKnoten)this.getKnoten());
			ripserver.starten();
			ripserver.setAktiv(true);
			ripbeacon.starten();
		}
	}

	public void beenden() {
		super.beenden();
		if (ripEnabled) {
			ripbeacon.beenden();
			ripserver.beenden();
		}
	}

	public RIPTable getRIPTable() {
		if (ripEnabled) {
			return riptable;
		} else {
			return null;
		}
	}

	public boolean getRip() {
		return ripEnabled;
	}

	public void setRip(boolean state) {
		ripEnabled = state;
	}

	/**
	 * Methode fuer den Zugriff auf die Firewall. Dieser Zugriff ist nicht
	 * JavaBean-konform, weil die Speicherung der Firewall als eine Anwendung
	 * durch die Oberklasse erfolgt.
	 */
	public Firewall holeFirewall() {
		return (Firewall) holeSoftware("filius.software.firewall.Firewall");
	}

	/**
	 * Methode fuer den Zugriff auf den Webserver Dieser Zugriff ist nicht
	 * JavaBean-konform, weil die Speicherung des Webservers als eine Anwendung
	 * durch die Oberklasse erfolgt.
	 */
	public WebServer holeWebServer() {
		return (WebServer) holeSoftware("filius.software.www.WebServer");
	}
}
