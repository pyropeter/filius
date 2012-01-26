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
package filius.software.email;

import filius.Main;
import filius.software.clientserver.TCPServerAnwendung;
import filius.software.transportschicht.Socket;
import filius.software.transportschicht.TCPSocket;

/*
 * Auch der POP3Server wird neu gemacht, bzw. dem neuen EmailServer angepasst. Das heißt vor
 * allem auch, dass der ganze unnötige Ballast, ich habe mich in dem Programm verlaufen,
 * rausgeworfen wird, wie z.B. 2x einen EmailServer zu implementieren!
 */

public class POP3Server extends TCPServerAnwendung // extends EmailServer
{
	private EmailServer emailServer;

	// Konstruktor(en)

	public POP3Server(int port, // Betriebssystem bs,
			EmailServer emailServer) {
		super();
		Main.debug.println("INVOKED-2 ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Server), constr: POP3Server("+port+","+emailServer+")");

		this.port = port;
		this.emailServer = emailServer;
	}

	public EmailServer holeEmailServer() {
		return emailServer;
	}

	// Methoden

	protected void neuerMitarbeiter(Socket socket) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Server), neuerMitarbeiter("+socket+")");
		POP3Mitarbeiter popMitarbeiter;

		popMitarbeiter = new POP3Mitarbeiter((TCPSocket) socket, this);
		popMitarbeiter.starten();
		mitarbeiter.add(popMitarbeiter);

	}

}