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
import filius.software.Anwendung;
import filius.software.transportschicht.Socket;

/**
 * Die Klasse ClientAnwendung enthaelt die fuer Clients spezifischen Methoden.
 */
public abstract class ClientAnwendung extends Anwendung {

	/**
	 * der Socket, der zum Nachrichtenaustausch mit dem Server genutzt wird
	 */
	protected Socket socket = null;

	/** Methode zur Abfrage, ob der Socket mit einem Server-Socket
	 * verbunden ist. <br />
	 * Diese Methode ist <b>nicht blockierend</b>.
	 */
	public boolean istVerbunden() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ClientAnwendung), istVerbunden()");
		if (socket != null) return socket.istVerbunden();
		else return false;
	}


	/** Diese Methode ruft beenden() der Oberklasse auf und loescht
	 * die Referenz auf den Socket. Der Socket wird also einfach
	 * verworfen und nicht geschlossen. <br />
	 * Diese Methode ist <b>nicht blockierend</b>.
	 */
	public void beenden() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ClientAnwendung), beenden()");
		super.beenden();

		if (socket != null) socket.beenden();
	}
}
