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
import filius.software.transportschicht.TCPSocket;

/**
 * <p>
 * In dieser Klasse wird das Server-Programm einer einfachen
 * Client-Server-Anwendung implementiert. Nachrichten an die graphische
 * Benutzungsoberflaeche werden durch den Aufruf
 * banachrichtigeBeobachter(Object) versendet.
 * </p>
 * <p>
 * In dieser Klasse wird mit einem eigenen Thread der Server-Socket auf
 * eingehende Verbindungen gewartet. Die Verarbeitung der eingehenden
 * Nachrichten erfolgt durch einen Mitarbeiter, der in der Methode
 * <code>neuerMitarbeiter(Socket)</code> erzeugt wird.
 * </p>
 * <p>
 * Die Verarbeitung eingehender Nachrichten wird von einem Mitarbeiter
 * uebernommen, der erzeugt wird, sobald eine neue Verbindungsanfrage vom
 * Server-Socket empfangen wurde.
 * </p>
 * <p>
 * Einstellungen, die in einer Projektdatei gespeichert werden sollen, muessen
 * in dieser Klasse als Attribute verwaltet werden und mit Getter- und
 * Setter-Methoden zugaenglich sein (z. B. fuer das Attribut
 * <code>int bspAttr</code> mit <code>
 * public int getBspAttr()</code> und
 * <code>public void setBspAttr(int)
 * </code>). Attribute, die in der
 * Mitarbeiterklasse vorhanden sind, werden nicht gespeichert!
 * </p>
 * <p>
 * Das Server-Programm wird durch Aufruf der Methode der Oberklasse
 * <code>setAktiv(boolean)</code> aktiviert bzw. deaktiviert (d. h. die
 * Verbindungsannahme wird aktiviert bzw. deaktiviert).
 * </p>
 */
public class ServerBaustein extends TCPServerAnwendung {

	/**
	 * In dieser Methode wird ein neuer Mitarbeiter zur Verarbeitung von
	 * eingehenden Nachrichten erzeugt und der Liste der zu verwaltenden
	 * Mitarbeiter hinzugefuegt.
	 */
	protected void neuerMitarbeiter(Socket socket) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ServerBaustein), neuerMitarbeiter("+socket+")");
		ServerMitarbeiter mitarbeiter;

		if (socket instanceof TCPSocket) {
			mitarbeiter = new ServerBausteinMitarbeiter(this,
					(TCPSocket) socket);
			this.mitarbeiter.add(mitarbeiter);
			mitarbeiter.starten();
		}
	}
}
