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
import filius.software.transportschicht.Socket;

/**
 * <p>
 * In dieser Klasse erfolgt die Verarbeitung von eingehenden Nachrichten an
 * einen Server.
 * </p>
 * <p>
 * Die Oberklasse <code>ServerMitarbeiter</code> erbt von der Klasse Thread.
 * In der <code>run()</code>-Methode der Oberklasse wird der Socket auf
 * eingehende Nachrichten ueberwacht. Sobald eine Nachricht eintrifft, wird
 * diese an die Methode <code>verarbeiteNachricht(String)</code> zur weiteren
 * Verarbeitung weiter gegeben. Ausserdem wird dort der Socket automatisch
 * geschlossen, wenn das Client-Programm den Verbindungsabbau initiiert.
 * </p>
 * <p>
 * In dieser Klasse sollte nur die Methode <code>senden(String)</code> des
 * Sockets verwendet werden!
 * </p>
 */
public class ServerBausteinMitarbeiter extends ServerMitarbeiter {

	/** Standard-Konstruktor. Wenn der Server auf einem bestimmten Port
	 * auf eingehende Verbindungen warten soll, muss die Port-Nummer hier mit
	 * <code>setPort(int)</code> initialisiert werden! */
	public ServerBausteinMitarbeiter(ServerAnwendung server, Socket socket) {
		super(server, socket);
	}

	/**
	 * Methode, die automatisch aufgerufen wird, wenn eine neue Nachricht
	 * eintrifft. Hier erfolgt die Verarbeitung der eingehenden Nachricht.
	 */
	protected void verarbeiteNachricht(String nachricht) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ServerBausteinMitarbeiter), verarbeiteNachricht("+nachricht+")");
		try {
			socket.senden(nachricht);
			server.benachrichtigeBeobachter("<<" + nachricht);
		}
		catch (Exception e) {
			e.printStackTrace(Main.debug);
			server.benachrichtigeBeobachter(e.getMessage());
		}
	}
}
