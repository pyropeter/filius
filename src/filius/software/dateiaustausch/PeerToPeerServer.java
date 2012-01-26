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
package filius.software.dateiaustausch;

import java.util.ListIterator;

import filius.Main;
import filius.software.clientserver.ServerMitarbeiter;
import filius.software.clientserver.TCPServerAnwendung;
import filius.software.transportschicht.Socket;


/**
 * In dieser Klasse wird der Server der Peer-to-Peer-Anwendung
 * implementiert. D. h., dass hier eingehende Anfragen aus dem
 * Peer-to-Peer-Netzwerk empfangen und verarbeitet werden. <br />
 * Zur Verarbeitung der Anfragen wird jeweils ein neuer Mitarbeiter
 * in einem eigenen Thread gestartet.
 */
public class PeerToPeerServer extends TCPServerAnwendung {

	/** die Instanz der Peer-to-Peer-Anwendung, fuer die eingehende
	 * Anfragen verarbeitet werden.
	 */
	private PeerToPeerAnwendung peerToPeerAnwendung;

	/** Konstruktor zur Initialisierung der Peer-to-Peer-Anwendung
	 * und setzen des Ports 6346, bei dem auf eingehende Anfragen
	 * gewartet wird. Außerdem wird der Konstruktor der Oberklasse
	 * aufgerufen.
	 * @param peerToPeerAnwendung
	 */
	PeerToPeerServer(PeerToPeerAnwendung peerToPeerAnwendung){
		super();
		Main.debug.println("INVOKED-2 ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerServer), constr: PeerToPeerServer("+peerToPeerAnwendung+")");
		this.peerToPeerAnwendung=peerToPeerAnwendung;
		setPort(6346);
		setAktiv(true);
	}

	/** Der Thread der Oberklasse ruft diese Methode auf, sobald eine
	 * Verbindungsanfrage eingegangen ist. Hier wird ein Mitarbeiter in
	 * einem neuen Thread gestartet, der fuer die Verarbeitung der
	 * eingehenden Anfrage genutzt wird.
	 */
	protected void neuerMitarbeiter(Socket socket) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerServer), neuerMitarbeiter("+socket+")");
		PeerToPeerServerMitarbeiter neuerMitarbeiter;
		neuerMitarbeiter = new PeerToPeerServerMitarbeiter(this, socket, peerToPeerAnwendung);
		neuerMitarbeiter.starten();
		mitarbeiter.add(neuerMitarbeiter);
	}

	/** Diese Methode wird genutzt, um Antwortpakete ueber einen
	 * Mitarbeiter-Thread zu verschicken. Die Auswahl des Mitarbeiter-Threads
	 * erfolgt an Hand der GUID der Nachricht.
	 * @param paket
	 */
	void sendePaket(PeerToPeerPaket paket) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerServer), sendePaket("+paket+")");
		ListIterator<ServerMitarbeiter> it;
		PeerToPeerServerMitarbeiter m;

		it = mitarbeiter.listIterator();
		while (it.hasNext()) {
			m = (PeerToPeerServerMitarbeiter)it.next();
			if (m.holeGuid() == paket.getGuid()) {
				m.senden(paket.toString());
			}
		}
	}
}
