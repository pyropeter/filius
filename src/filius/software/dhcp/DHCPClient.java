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

import java.util.StringTokenizer;
import java.util.ListIterator;

import filius.Main;
import filius.exception.VerbindungsException;
import filius.gui.GUIContainer;
import filius.hardware.Verbindung;
import filius.software.clientserver.ClientAnwendung;
import filius.software.transportschicht.UDPSocket;

public class DHCPClient extends ClientAnwendung {

	private int zustand;

	private String dhcpserverIP = "255.255.255.255";

	public static final int IP_ZUGEWIESEN = 99;

	public static final int ABBRUCH = -1, INIT_DHCP = 2, CONFIG = 3;

	public void starten() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DHCPClient), starten()");
		super.starten();

		ausfuehren("konfiguriere", null);
	}

	public void konfiguriere() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DHCPClient), konfiguriere()");
		getSystemSoftware().setzeIPAdresse("0.0.0.0");
		getSystemSoftware().setzeNetzmaske("0.0.0.0");
		getSystemSoftware().setStandardGateway("");
		ListIterator<DHCPServer> it;
		boolean allDHCPserversStarted;

		// es muss gewaehrleistet werden, dass der DHCP-Server bereits
		// gestartet worden ist!  (sofern denn einer als "aktiv" gekennzeichnet ist!)
		try {
			do {
				it = GUIContainer.getGUIContainer().getMenu().getDHCPservers().listIterator();
				allDHCPserversStarted = true;
				DHCPServer server;
				while (it.hasNext()) {
					server = it.next();
					if (!server.isStarted()) {
						allDHCPserversStarted = false;
						Main.debug.println("WARNING ("+this.hashCode()+"): DHCP server on '"+server.getSystemSoftware().getKnoten().getName()+"' has NOT been started --> waiting");
						break;
					}
					else {
						//Main.debug.println("DHCPClient:\tserver on '"+server.getSystemSoftware().getKnoten().getName()+"' has been started");
					}
				}
				Thread.sleep(100);
			} while (!allDHCPserversStarted);
		}
		catch (InterruptedException e) {
			e.printStackTrace(Main.debug);
		}

		//Main.debug.println("DHCP-Client wurde gestartet.");
		starteDatenaustausch();
	}

	private void starteDatenaustausch() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DHCPClient), starteDatenaustausch()");
		String antwort = "";
		String angeboteneIP = "";
		String typ = null;
		String mac;
		boolean erfolg;
		long start;
		int fehlerzaehler = 0;
		StringTokenizer st = null;
		UDPSocket socket = (UDPSocket) this.socket;
		int maxFehler = 5;


		zustand = INIT_DHCP;

		try {
			socket = new UDPSocket(getSystemSoftware(), "255.255.255.255", 67, 68);
			socket.verbinden();

			//Main.debug.println(getClass() + "\n\tkonfiguriere() -> zustand = "
					//+ zustand);

			while (zustand != IP_ZUGEWIESEN && zustand != ABBRUCH
					&& fehlerzaehler < maxFehler) {

				if (zustand == INIT_DHCP) {
					socket.senden("DHCPDISCOVER "
							+ getSystemSoftware().holeIPAdresse() + " "
							+ getSystemSoftware().holeMACAdresse() + " "
							+ dhcpserverIP);

					start = System.currentTimeMillis();
					do {
						mac = null;
						typ = null;
						erfolg = false;

						antwort = socket.empfangen(Verbindung.holeRTT());

						if (antwort != null) {
							st = new StringTokenizer(antwort, " ");

							typ = st.nextToken().trim();
							st.nextToken();
							mac = st.nextToken().trim();

							erfolg = mac.equalsIgnoreCase(getSystemSoftware()
									.holeMACAdresse())
									&& typ.equalsIgnoreCase("DHCPOFFER");
						}
					}
					while (!erfolg && System.currentTimeMillis() - start <= Verbindung.holeRTT());

					//if(!erfolg) { Main.debug.println(getClass()+"\n\tTimeOut! (>"+Verbindung.holeRTT()); }

					if (erfolg && st != null) {
						dhcpserverIP = st.nextToken();
						angeboteneIP = st.nextToken();

						//Main.debug.println(getClass()
								//+ "\n\tangebotene IP-Adresse: " + angeboteneIP);

						zustand = CONFIG;
						//zustand = ABBRUCH;
					}
					else {
						fehlerzaehler++;
						Main.debug.println("ERROR ("+this.hashCode()+"): DHCPOFFER erwartet, erhalten: " + typ);
					}
				}
				else if (zustand == CONFIG) {
					if (socket != null) socket.schliessen();
					socket = new UDPSocket(getSystemSoftware(), dhcpserverIP, 67, 68);
					socket.verbinden();

					socket.senden("DHCPREQUEST "
							+ getSystemSoftware().holeIPAdresse() + " "
							+ getSystemSoftware().holeMACAdresse() + " "
							+ dhcpserverIP + " " + angeboteneIP);

					start = System.currentTimeMillis();
					do {
						mac = null;
						typ = null;
						erfolg = false;

						antwort = socket.empfangen(Verbindung.holeRTT());

						if (antwort != null) {
							//Main.debug.println("DHCPClient, received response from DHCP Server:\n\t"
											//+  "'"+antwort+"'" );							
							st = new StringTokenizer(antwort, " ");

							typ = st.nextToken();
							st.nextToken();
							mac = st.nextToken();

							erfolg = mac.equalsIgnoreCase(getSystemSoftware().holeMACAdresse())
									&& (typ.equalsIgnoreCase("DHCPACK") 
										|| typ.equalsIgnoreCase("DHCPNAK"));
						}
					}
					while (!erfolg && System.currentTimeMillis() - start <= Verbindung.holeRTT());

					if (erfolg && st != null) {
						if (typ.equalsIgnoreCase("DHCPNAK")) {
							zustand = INIT_DHCP;
						}
						else {
							st.nextToken();
							st.nextToken();

							getSystemSoftware().setzeIPAdresse(angeboteneIP);
							if (st.hasMoreTokens()) {    // set netmask
								getSystemSoftware().setzeNetzmaske(st.nextToken());
								if (st.hasMoreTokens()) {    // set DNS server
									getSystemSoftware().setStandardGateway(st.nextToken());   // set Gateway
									if (st.hasMoreTokens()) {    // set DNS server
										getSystemSoftware().setDNSServer(st.nextToken());
									}
								}
							}
							zustand = IP_ZUGEWIESEN;
							//Main.debug.println(getClass()
									//+ "\n\tkonfiguriere(): IP-Adresse = "
									//+ angeboteneIP);
						}
					}
					else {
						fehlerzaehler++;
					}
				}
				if (fehlerzaehler == maxFehler) {
					zustand = ABBRUCH;
					Main.debug.println("ERROR ("+this.hashCode()+"): kein DHCPACK erhalten");

					// set own value 169.254.x.x; resp. delete values
					int block3 = (new java.util.Random()).nextInt(253) + 1;
					int block4 = (new java.util.Random()).nextInt(253) + 1;
					getSystemSoftware().setzeIPAdresse("169.254."+block3+"."+block4);
					getSystemSoftware().setzeNetzmaske("255.255.0.0");
					getSystemSoftware().setStandardGateway("");   // set Gateway
					getSystemSoftware().setDNSServer("");
					///////
					
					fehlerzaehler = 0;
				}
			}

			socket.schliessen();
		}
		catch (VerbindungsException e1) {
			e1.printStackTrace(Main.debug);
		}

		this.getSystemSoftware().benachrichtigeBeobacher(null);

		beenden();
	}
}
