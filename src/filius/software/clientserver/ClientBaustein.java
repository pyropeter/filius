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

import filius.Main;
import filius.rahmenprogramm.I18n;
import filius.software.transportschicht.TCPSocket;

/**
 * <p>
 * In dieser Klasse wird das Client-Programm einer einfachen
 * Client-Server-Anwendung implementiert. Nachrichten an die graphische
 * Benutzungsoberflaeche werden durch den Aufruf
 * banachrichtigeBeobachter(Object) versendet.
 * </p>
 * <p>
 * Aufrufe folgender Methoden des Sockets blockieren:
 * </p>
 * <ul>
 * <li> verbinden() </li>
 * <li> senden() </li>
 * <li> empfangen() </li>
 * <li> schliessen() </li>
 * </ul>
 * <p>
 * Deshalb muessen Methoden dieser Klasse, die Aufrufe dieser Methoden
 * enthalten, ueber die Methode <code>ausfuehren(String, Object[])</code>
 * aufgerufen werden. Das bewirkt, dass diese Methoden in einem eigenen Thread
 * ausgefuehrt werden und damit der aufrufende Thread nicht blockiert. Das ist
 * wichtig, wenn die Aufrufe von der graphischen Benutzungsoberflaeche
 * ausgeloest werden.
 * </p>
 * <p>
 * Ein Beispiel fuer die Verwendung von <code>ausfuehren()</code> ist
 * folgendes: <br />
 * <code> public void verbinden(String zielAdresse, Integer port) { <br />
 * &nbsp;&nbsp; Object[] args; <br /> <br />
 * &nbsp;&nbsp; args = new Object[2]; <br />
 * &nbsp;&nbsp; args[0] = zielAdresse; <br />
 * &nbsp;&nbsp;	args[1] = port; <br /> <br />
 * &nbsp;&nbsp; ausfuehren("initialisiereSocket", args); <br />
 * } </p>
 * <p> Dabei wird als erstes Argument der auszufuehrenden blockierenden
 * Methode uebergeben (hier: <code> initialisiereSocket</code>) und dann in
 * einem Array die zu uebergebenden Parameter (hier: <code>zielAdresse</code>
 * und <code>port</code>). Der Aufruf der Methode <code>verbinden(zielAdresse, port)
 * </code> bewirkt also das Ausfuehren der Methode
 * <code>initialisiereSocket(zielAdresse, port)</code> in einem anderen Thread.
 * Damit blockiert die Methode <code>verbinden</code> nicht. </p>
 * <p> <b> Achtung:</b> Die indirekt aufgerufene Methode (d. h. ueber <code>
 * ausfuehren(String, Object[])</code>) muss als <code>public</code> deklariert
 * sein! </p>
 */
public class ClientBaustein extends ClientAnwendung implements I18n {

	/** Port-Nummer des Servers, an dem Verbindungsanfragen angenommen werden */
	private int zielPort = 55555;

	/**
	 * Adresse des Rechners, auf dem der Server laeuft als Domainname oder
	 * IP-Adresse.
	 */
	private String zielIPAdresse;

	/** Methode zum Verbindungsaufbau zu einem Server. Hier wird der
	 * Client-Socket als TCP/IP-Socket erzeugt. Wenn UDP verwendet werden soll,
	 * muss diese Methode ueberschrieben werden. <br />
	 * Diese Methode ist <b>nicht blockierend</b>. Diese Methode veranlasst den
	 * Aufruf von <code>initialisiereSocket</code> in einem anderen Thread. */
	public void verbinden() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ClientBaustein), verbinden()");
		Object[] args;

		args = new Object[2];
		args[0] = zielIPAdresse;
		args[1] = new Integer(zielPort);

		ausfuehren("initialisiereSocket", args);
		
		ausfuehren("empfangeNachricht", null);
	}

	/** Methode zum Verbindungsaufbau zu einem Server. Hier wird der
	 * Client-Socket als TCP/IP-Socket erzeugt. Wenn UDP verwendet werden soll,
	 * muss diese Methode ueberschrieben werden. <br />
	 * Diese Methode ist <b>nicht blockierend</b>. Diese Methode veranlasst den
	 * Aufruf von <code>initialisiereSocket</code> in einem anderen Thread.
	 * 
	 * @param zielAdresse
	 * @param port
	 */
	/*private void verbinden(String zielAdresse, Integer port) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ClientBaustein), verbinden("+zielAdresse+","+port+")");
		Object[] args;

		args = new Object[2];
		args[0] = zielAdresse;
		args[1] = port;

		ausfuehren("initialisiereSocket", args);
	}*/

	/**
	 * Nicht-blockierende Methode zum Versenden einer Nachricht an den Server.
	 * Diese Methode veranlasst den Aufruf von <code>versendeNachricht</code>
	 * in einem anderen Thread.
	 *
	 * @param nachricht
	 *            die zu versendende Nachricht
	 */
	/*public void senden(String nachricht) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ClientBaustein), senden("+nachricht+")");
		Object[] args;

		args = new Object[1];
		args[0] = nachricht;

		ausfuehren("versendeNachricht", args);
	}*/

	/*public void empfangen() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ClientBaustein), empfangen()");
		ausfuehren("empfangeNachricht", null);
	}*/

	/**
	 * Methode zum Aufbau einer Verbindung mit einem TCP-Socket. Diese Methode
	 * ist blockierend und sollte nicht direkt von der GUI aufgerufen werden.
	 */
	public void initialisiereSocket(String zielAdresse, Integer port) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ClientBaustein), initialisiereSocket("+zielAdresse+","+port+")");
		try {
			socket = new TCPSocket(getSystemSoftware(), zielAdresse, port);
			socket.verbinden();

			benachrichtigeBeobachter(messages.getString("sw_clientbaustein_msg2"));
		}
		catch (Exception e) {
			e.printStackTrace(Main.debug);
			socket = null;
			benachrichtigeBeobachter(messages.getString("sw_clientbaustein_msg1")
					+ e.getMessage());
		}
	}

	/**
	 * Methode zum trennen einer Verbindung. Der Socket wird durch den Aufruf
	 * der Methode schliessen() geschlossen und und der Socket fuer diese
	 * Anwendung auf null gesetzt. <br />
	 * Diese Methode ist <b> blockierend</b>. 
	 */
	public void trennen() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ClientBaustein), trennen()");
		if (socket != null) {
			socket.schliessen();
			socket = null;
			benachrichtigeBeobachter(messages.getString("sw_clientbaustein_msg3"));
		}
	}

	/**
	 * Diese Methode ist <b>blockierend</b> und sollte nicht direkt von der GUI
	 * aufgerufen werden.
	 */
	/*public void schliesseSocket() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ClientBaustein), schliesseSocket()");
		if (socket != null) {
			socket.schliessen();
			socket = null;
			benachrichtigeBeobachter(messages.getString("sw_clientbaustein_msg3"));
		}
	}*/

	/**
	 * Diese Methode ist <b>blockierend</b> und sollte nicht direkt von der GUI
	 * aufgerufen werden.
	 */
	public void senden(String nachricht) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ClientBaustein), versendeNachricht("+nachricht+")");

		if (socket != null && socket.istVerbunden()) {
			try {
				socket.senden(nachricht);
				benachrichtigeBeobachter("<<" + nachricht);
			}
			catch (Exception e) {
				benachrichtigeBeobachter(e.getMessage());
				e.printStackTrace(Main.debug);
			}
		}
		else {
			benachrichtigeBeobachter(messages.getString("sw_clientbaustein_msg4"));
		}
	}

	/**
	 * Methode zum Empfang einer Nachricht vom Socket. Die empfangene Nachricht
	 * wird mit <code>benachrichtigeBeobachter</code> an die GUI weiter
	 * gegeben. Diese Methode ist blockierend und sollte nicht direkt von der
	 * GUI aufgerufen werden.
	 */
	public void empfangeNachricht() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ClientBaustein), empfangeNachricht()");
		String nachricht;

		if (socket != null && socket.istVerbunden()) {
			try {
				while (this.istVerbunden()) {
					nachricht = socket.empfangen();
					if (nachricht != null) {
						benachrichtigeBeobachter(">>" + nachricht);
					}
					else {
						benachrichtigeBeobachter(messages.getString("sw_clientbaustein_msg5") + " "
								+ socket.holeZielIPAdresse() + ":"
								+ socket.holeZielPort() + " " + messages.getString("sw_clientbaustein_msg6"));
	
						socket.schliessen();
					}
				}
			}
			catch (Exception e) {
				benachrichtigeBeobachter(e.getMessage());
				e.printStackTrace(Main.debug);
			}
		}
		else {
			benachrichtigeBeobachter(messages.getString("sw_clientbaustein_msg4"));
		}
	}

	/** Methode fuer den Zugriff auf die Server-Adresse */
	public String getZielIPAdresse() {
		return zielIPAdresse;
	}

	/** Methode fuer den Zugriff auf die Server-Adresse */
	public void setZielIPAdresse(String zielIPAdresse) {
		this.zielIPAdresse = zielIPAdresse;
	}

	/**
	 * Methode fuer den Zugriff auf die Port-Nummer, an dem der Server zu
	 * erreichen ist.
	 */
	public int getZielPort() {
		return zielPort;
	}

	/**
	 * Methode fuer den Zugriff auf die Port-Nummer, an dem der Server zu
	 * erreichen ist.
	 */
	public void setZielPort(int zielPort) {
		this.zielPort = zielPort;
	}
}
