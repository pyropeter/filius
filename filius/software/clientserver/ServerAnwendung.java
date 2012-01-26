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
package filius.software.clientserver;

import java.util.LinkedList;
import java.util.ListIterator;

import filius.Main;
import filius.exception.ServerSocketException;
import filius.rahmenprogramm.I18n;
import filius.software.Anwendung;
import filius.software.transportschicht.ServerSocket;
import filius.software.transportschicht.Socket;
import filius.software.transportschicht.SocketSchnittstelle;

/**
 * Diese Klasse ist die Oberklasse fuer Serveranwendungen. Dazu wird ein
 * Server-Socket und Methoden zur Verbindungsherstellung zur Verfuegung
 * gestellt. Ausserdem wird eine Liste von Mitarbeiter-Threads, die die
 * Anwendungslogik implementieren bzw. die Verarbeitung eingehender
 * Verbindungsanfragen und Dienstnforderungen uebernehmen.
 */
public abstract class ServerAnwendung extends Anwendung implements I18n {

	/** Konstante: UDP oder TCP der Klasse TransportProtokoll */
	protected int transportProtokoll;

	/** der Socket zur Annahme eingehender Verbindungsanfragen */
	protected SocketSchnittstelle socket;

	/** Der TCP-Port, der auf eingehende Verbindungsanfragen wartet. */
	protected int port = 55555;

	/**
	 * Ob der Server aktiv ist, d. h. ob auf eingehende Verbindungsanfragen
	 * gewartet wird.
	 */
	protected boolean aktiv = false;

	/**
	 * Liste von Mitarbeitern, die die Bearbeitung der erstellten Verbindungen
	 * vornehmen.
	 */
	protected LinkedList<ServerMitarbeiter> mitarbeiter;

	/** Konstruktor zur Initialisierung des verwendeten TransportProtokolls */
	public ServerAnwendung(int transportProtokoll) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+", constr: ServerAnwendung("+transportProtokoll+")");
		this.transportProtokoll = transportProtokoll;
	}

	/**
	 * Methode fuer den Zugriff auf die Portnummer, auf der eingehende
	 * Verbindungen angenommen werden
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Methode fuer den Zugriff auf die Portnummer, auf der eingehende
	 * Verbindungen angenommen werden
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Methode zur Abfrage, ob der Server-Socket auf eingehende Verbindungen
	 * wartet.
	 */
	public boolean isAktiv() {
		return aktiv;
	}

	/**
	 * Zum aktivieren bzw. deaktivieren des Servers. Wenn der Server aktiv ist,
	 * wartet er auf eingehende Verbindungsanfragen. Sonst ist der Port
	 * geschlossen.
	 * 
	 * @param flag
	 */
	public void setAktiv(boolean flag) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ServerAnwendung), setAktiv("+flag+")");
		aktiv = flag;

		//Main.debug.println(getClass() + "\n\taktiv = " + aktiv);

		if (getState().equals(State.WAITING)) {
			synchronized (this) {
				//Main.debug.println("\taufgeweckt");
				notifyAll();
			}
		}

		if (!flag) {
			if (socket != null)
				socket.schliessen();
			socket = null;
			benachrichtigeBeobachter(messages
					.getString("sw_serveranwendung_msg1"));
		}
		else {
			benachrichtigeBeobachter(messages
					.getString("sw_serveranwendung_msg2"));
		}
	}

	/**
	 * Methode zum Starten des Threads beim Wechsel vom Entwurfs- in den
	 * Aktionsmodus. Hier wird die Liste der Mitarbeiter als leere Liste
	 * erstellt und die starten()-Methode der Oberklasse zum Starten des Threads
	 * aufgerufen.
	 */
	public void starten() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ServerAnwendung), starten()");
		super.starten();
		mitarbeiter = new LinkedList<ServerMitarbeiter>();

		ausfuehren("annehmenVerbindungen", null);
	}

	// return, whether this application can be used already
	public boolean isStarted() {
		return (socket != null);
	}

	/**
	 * Methode zum Anhalten des Threads. Hier wird die beenden()-Methode der
	 * Oberklasse aufgerufen und die Mitarbeiter-Threads sowie die
	 * Socket-Schnittstelle werden beendet.
	 */
	public void beenden() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ServerAnwendung), beenden()");
		ListIterator it;

		super.beenden();

		it = mitarbeiter.listIterator();
		while (it.hasNext()) {
			((ServerMitarbeiter) it.next()).beenden();
		}

		if (socket != null)
			socket.beenden();
		socket = null;
	}

	/**
	 * Methode zum erzeugen eines neuen Mitarbeiters, wenn eine
	 * Verbindungsanfrage eingetroffen ist. <b>Diese Methode muss von
	 * Unterklassen ueberschrieben werden, um den Mitarbeiter mit der richtigen
	 * Anwengungslogik zu erzeugen.</b> <br />
	 * In dieser Methode wird der Mitarbeiter erzeugt und der Liste der
	 * Mitarbeiter hinzugefuegt. Das muss in den Unterklassen implementiert
	 * werden.
	 * 
	 * @param socket
	 */
	protected abstract void neuerMitarbeiter(Socket socket);

	/**
	 * Methode zum entfernen eines Mitarbeiters, dessen Socket geschlossen und
	 * der Thread beendet worden ist. <br />
	 * In dieser Methode wird der Mitarbeiter nur aus der Liste der verwalteten
	 * Threads entfernt.
	 * 
	 * @param thread
	 *            der nicht mehr aktive Mitarbeiter
	 */
	public void entferneMitarbeiter(ServerMitarbeiter thread) {
		mitarbeiter.remove(thread);
	}

	/**
	 * Die Aufgabe des Threads der Server-Anwendung besteht darin, wenn der
	 * Server aktiv ist, auf eingehende Verbindungsanforderungen zu warten. Wenn
	 * eine Anforderung erfolgt, wird ein neuer Mitarbeiter mit der Methode
	 * neuerMitarbeiter() erstellt, der die weitere Verarbeitung uebernimmt.
	 */
	public void annehmenVerbindungen() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ServerAnwendung), annehmenVerbindungen()");
		Socket transportSocket;

		while (running) {
			if (aktiv) {
				if (socket == null) {
					try {
						socket = new ServerSocket(getSystemSoftware(), port,
								transportProtokoll);
					}
					catch (ServerSocketException e) {
						e.printStackTrace(Main.debug);
						benachrichtigeBeobachter(messages.getString("sw_serveranwendung_msg3"));
						setAktiv(false);
						if (socket != null)
							socket.beenden();
						socket = null;
					}
				}

				if (socket != null) {
					try {
						transportSocket = ((ServerSocket) socket).oeffnen();

						if (transportSocket != null
								&& transportSocket.holeZielIPAdresse() != null) {
							neuerMitarbeiter(transportSocket);
							benachrichtigeBeobachter(messages
									.getString("sw_serveranwendung_msg4")
									+ " "
									+ transportSocket.holeZielIPAdresse()
									+ ":"
									+ transportSocket.holeZielPort()
									+ " "
									+ messages
											.getString("sw_serveranwendung_msg5"));
						}
					}
					catch (Exception e) {
						benachrichtigeBeobachter(e.getMessage());
						e.printStackTrace(Main.debug);
					}
				}
			}
			else {
				
				synchronized (this) {
					try {
						wait();
						//Main.debug.println(getClass()
										//+ "\n\tThread fortgesetzt nach Aktivierung der Anwendung");
					}
					catch (InterruptedException e) {
					}
				}
			}
		}

		//Main.debug.println(getClass()
				//+ "aktiven Zustand und damit Verbindungsannahme beendet");
	}
}
