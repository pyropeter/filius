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

import filius.Main;
import filius.software.dhcp.DHCPClient;
import filius.software.dhcp.DHCPServer;

/** Diese Klasse stellt die Funktionalitaet eines Betriebssystems
 * fuer Hosts (d. h. Rechner und Notebooks) zur Verfuegung.
 * Spezifisch ist die Moeglichkeit, einen DHCP-Server zu
 * installieren und die Konfiguration der Netzwerkkarten mit DHCP
 * durchzufuehren. Die weitere Funktionalitaet wird von der
 * Oberklasse (InternetKnotenBetriebssystem) zur Verfuegung
 * gestellt.
 *
 */
public class Betriebssystem extends InternetKnotenBetriebssystem {

	private static final long serialVersionUID = 1L;

	/** ob die Konfiguration der Netzwerkkarte mit DHCP
	 * erfolgt
	 */
	private boolean dhcpKonfiguration;
	/** der DHCP-Server, der aktiviert und deaktiviert werden kann */
	private DHCPServer dhcpServer;
	/** der DHCP-Client, der zur Konfiguration der Netzwerkkarte
	 * genutzt wird, wenn die Konfiguration mit DHCP erfolgen soll
	 * @see dhcpKonfiguration
	 */
	private DHCPClient dhcpClient;

	/** Konstruktur, in dem DHCP-Client und -Server initialisiert
	 * werden
	 */
	public Betriebssystem() {
		super();
		Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+" (Betriebssystem), constr: Betriebssystem()");

		dhcpServer = new DHCPServer();
		dhcpServer.setSystemSoftware(this);
	}

	/** Starten der Threads. Der DHCP-Client wird hier gestartet,
	 * wenn die Konfiguration mit DHCP aktiviert ist. Der DHCP-Server
	 * wird hier auch gestartet.
	 */
	public void starten() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Betriebssystem), starten()");
		super.starten();

		dhcpServer.starten();

		if (isDHCPKonfiguration()) {
			dhcpClient = new DHCPClient();
			dhcpClient.setSystemSoftware(this);
			dhcpClient.starten();
		}
	}

	/** Aufruf erfolgt beim Wechsel vom Aktions- zum Entwurfsmodus. Die
	 * entsprechende Methode der Oberklasse wird aufgerufen und der
	 * DHCP-Server und -Client beendet.
	 */
	public void beenden() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Betriebssystem), beenden()");
		super.beenden();

		dhcpServer.beenden();
		if (dhcpClient != null) dhcpClient.beenden();
	}

	/** Methode zum Zugriff auf den DHCP-Server. */
	public DHCPServer getDHCPServer() {
		return dhcpServer;
	}

	/** Methode zum Zugriff auf den DHCP-Server. */
	public void setDHCPServer(DHCPServer dhcpServer) {
		this.dhcpServer = dhcpServer;
	}

	/** Ob die Konfiguration der Netzwerkkarte mit DHCP erfolgt */
	public boolean isDHCPKonfiguration() {
		return dhcpKonfiguration;
	}

	/** Ob die Konfiguration der Netzwerkkarte mit DHCP erfolgt */
	public void setDHCPKonfiguration(boolean dhcp) {
		this.dhcpKonfiguration = dhcp;
		if (dhcp) {
			setzeIPAdresse("0.0.0.0");
			setzeNetzmaske("0.0.0.0");
		}
	}
}
