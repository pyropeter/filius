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

import java.util.Hashtable;
import java.util.StringTokenizer;

import filius.Main;
import filius.software.clientserver.ServerMitarbeiter;
import filius.software.transportschicht.Socket;
import filius.software.transportschicht.UDPSocket;

/**
 * <p> Der DHCPServerMitarbeiter hat eine andere Funktion als dies bei
 * sonstigen ServerMitarbeiter-Objekten der Fall ist. Dieser Mitarbeiter
 * verarbeitet alle eingehenden UDP-Segmente, weil alle DHCP-Clients mit
 * der Absender-Adresse 0.0.0.0 und dem UDP-Port 68 Nachrichten verschicken.
 * Daher funktioniert das Demultiplexen auf der Transportschicht nicht. </p>
 *
 * <p> Dieser Mitarbeiter verwaltet daher fuer jede MAC-Adresse, zu der eine
 * Verbindung besteht, einen eigenen Zustand in einer Hash-Tabelle, der ueber das
 * folgende Verhalten entscheidet. Der Socket wird nicht geschlossen, weil immer
 * auf weitere eingehende Verbindungsanfragen gewartet wird. </p>
 *
 * Die verwendeten DHCP-Nachrichten sind stark vereinfacht, weil sie
 * beobachtet werden können. Der Aufbau einer DHCP-Nachricht besteht aus
 * folgenden Komponenten:
 * <ul>
 * <li> Nachrichtentyp (s.u.) </li>
 * <li> Client-IP-Adresse: caddr </li>
 * <li> Client-MAC-Adresse: maddr </li>
 * <li> Server-IP-Adresse: saddr </li>
 * </ul>
 *
 * DHCP kennt folgende Nachrichtentypen (Quelle: de.wikipedia.org):
 * <ul>
 * <li> DHCPDISCOVER: Ein Client ohne IP-Adresse sendet eine
 * Broadcast-Anfrage nach Adress-Angeboten an den/die DHCP-Server im lokalen
 * Netz. </li>
 * <li> DHCPOFFER: Der/die DHCP-Server antworten mit entsprechenden Werten
 * auf eine DHCPDISCOVER-Anfrage. </li>
 * <li> DHCPREQUEST: Der Client fordert (eine der angebotenen)
 * IP-Adresse(n), weitere Daten sowie Verlaengerung der Lease-Zeit von einem
 * der antwortenden DHCP-Server. </li>
 * <li> DHCPACK: Bestaetigung des DHCP-Servers zu einer
 * DHCPREQUEST-Anforderung </li>
 * <li> DHCPNAK: Ablehnung einer DHCPREQUEST-Anforderung durch den
 * DHCP-Server </li>
 * <li> DHCPDECLINE: Ablehnung durch den Client, da die IP-Adresse schon
 * verwendet wird.
 * <li>
 * <li> DHCPRELEASE: Der Client gibt die eigene Konfiguration frei, damit
 * die Parameter wieder für andere Clients zur Verfuegung stehen. </li>
 * <li> DHCPINFORM: Anfrage eines Clients nach Daten ohne IP-Adresse, z. B.
 * weil der Client eine statische IP-Adresse besitzt. </li>
 * </ul>
 * Durch diesen DHCP-Server werden nicht alle Befehle unterstuetzt.
 */
public class DHCPServerMitarbeiter extends ServerMitarbeiter {

	private String angeboteneAdresse = "";

	Hashtable<String,String> macIPAdresse = new Hashtable<String,String>();

	public DHCPServerMitarbeiter(DHCPServer server, Socket socket) {
		super(server, socket);
	}



	/**
	 * Ablauf einer DHCP-Anfrage (Quelle: de.wikipedia.org): <br />
	 * <p>
	 * Wenn ein Client erstmalig eine IP-Adresse benötigt, schickt er eine
	 * DHCPDISCOVER-Nachricht (mit seiner MAC-Adresse) als Netzwerk-Broadcast an
	 * die verfuegbaren DHCP-Server (es kann durchaus mehrere davon im gleichen
	 * Subnetz geben). Dieser Broadcast hat als Absender-IP-Adresse 0.0.0.0 und
	 * als Zieladresse 255.255.255.255, da der Absender noch keine IP-Adresse
	 * besitzt und seine Anfrage "an alle" richtet. Dabei ist der UDP-Quellport
	 * 68 und der UDP-Zielport 67. Die DHCP-Server antworten mit DHCPOFFER und
	 * machen Vorschlaege für eine IP-Adresse. Dies geschieht ebenfalls mit einem
	 * Broadcast an die Adresse 255.255.255.255 mit UDP-Quellport 67 und
	 * UDP-Zielport 68. </p>
	 *
	 * <p>
	 * Der Client darf nun unter den eingetroffenen Angeboten (DHCP-Offers)
	 * waehlen. Wenn er sich für eines entschieden hat (z. B. wegen laengster
	 * Lease-Zeit oder wegen Ablehnung eines speziellen, evtl. falsch
	 * konfigurierten DHCP-Servers, oder einfach für die erste Antwort),
	 * kontaktiert er per Broadcast und einem im Paket enthaltenen
	 * Serveridentifier den entsprechenden Server mit der Nachricht DHCPREQUEST.
	 * Alle eventuellen weiteren DHCP-Server werten dies als Absage fuer ihre
	 * Angebote. Der vom Client ausgewaehlte Server bestaetigt in einer
	 * DHCPACK-Nachricht (DHCP-Acknowledged) die IP-Adresse mit den weiteren
	 * relevanten Daten, oder er zieht sein Angebot zurück (DHCPNAK, siehe auch
	 * sonstiges).
	 * </p>
	 *
	 * <p>
	 * Bevor der Client sein Netzwerkinterface mit der zugewiesenen Adresse
	 * konfiguriert, sollte er noch pruefen, ob nicht versehentlich noch ein
	 * anderer Rechner die Adresse verwendet. Dies geschieht ueblicherweise durch
	 * einen ARP-Request mit der soeben zugeteilten IP-Adresse. Antwortet ein
	 * anderer Host im Netz auf diesen Request, so wird der Client die
	 * vorgeschlagene Adresse mit einer DHCPDECLINE-Nachricht zurueckweisen.
	 * </p>
	 */
	protected void verarbeiteNachricht(String nachricht) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DHCPServerMitarbeiter), verarbeiteNachricht("+nachricht+")");
		String body;
		StringTokenizer st;
		String caddr;
		String maddr;
		String saddr;
		String befehl;
		DHCPServer dhcpServer;

		dhcpServer = (DHCPServer) server;

		st = new StringTokenizer(nachricht, " ");

		befehl = st.nextToken();
		caddr = st.nextToken();
		maddr = st.nextToken();
		saddr = st.nextToken();

		// der Client sucht einen DHCP-Server
		if (befehl.equalsIgnoreCase("DHCPDISCOVER")) {
			angeboteneAdresse = dhcpServer.reserviereFreieIP(maddr);
			//if (angeboteneAdresse == null) Main.debug.println("Adresse null");
			//else if (maddr == null) Main.debug.println("MAC null");
			macIPAdresse.put(maddr, angeboteneAdresse);

			((UDPSocket) socket).sendeBroadcast("DHCPOFFER " + caddr + " "
					+ maddr + " " + saddr + " " + angeboteneAdresse);
		}
		// der Client fordert die angebotene IP-Adresse an
		else if ((befehl.equalsIgnoreCase("DHCPREQUEST"))) {
			angeboteneAdresse = macIPAdresse.get(maddr);
			body = st.nextToken();
			if (angeboteneAdresse != null && body.trim().equalsIgnoreCase(angeboteneAdresse)) {
				dhcpServer.gibMACFrei(maddr);
					dhcpServer.reserviereIPAdresse(maddr, body, 0);
						((UDPSocket) socket).sendeBroadcast("DHCPACK " + caddr
								+ " " + maddr + " " + saddr + " " + body + " "
								+ dhcpServer.getSubnetzmaske() + " "
								+ dhcpServer.getGatewayip() + " "
								+ dhcpServer.getDnsserverip());
				}
				else {
					((UDPSocket) socket).sendeBroadcast("DHCPNAK " + caddr
							+ " " + maddr + " " + saddr + " " + body);
				}
			macIPAdresse.remove(maddr);
		}
		// Die zugewiesene IP-Adresse eines Rechner wird wieder freigegeben
		else if (befehl.equalsIgnoreCase("DHCPRELEASE")) {
			dhcpServer.gibMACFrei(maddr);
		}
		// Abfrage der Standardeinstellungen (ohne neue IP-Adresse)
		else if (befehl.equalsIgnoreCase("DHCPINFORM")) {
			((UDPSocket) socket).sendeBroadcast(caddr + " " + maddr + " "
					+ saddr + " " + dhcpServer.getDnsserverip() + " "
					+ dhcpServer.getGatewayip() + " "
					+ dhcpServer.getSubnetzmaske());
		}
		// Client hat IP-Adresse abgelehnt
		else if (befehl.equalsIgnoreCase("DHCPDECLINE")) {
			dhcpServer.gibMACFrei(maddr);
			macIPAdresse.remove(maddr);
		}
		else {
			Main.debug.println("ERROR ("+this.hashCode()+"): unbekannten DHCP-Nachrichtentyp empfangen: "+befehl);
		}
	}
}
