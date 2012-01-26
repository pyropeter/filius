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

/**
 *
 * @author Andre Asschoff
 *
 */
public class SMTPServer extends TCPServerAnwendung
{
//	Attribute
	private EmailServer emailServer;

//	Konstruktor(en)

	public SMTPServer(//Betriebssystem bs,
			int port, EmailServer emailServer)
	{
		super();
		Main.debug.println("INVOKED-2 ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPServer), constr: SMTPServer("+port+","+emailServer+")");

		this.port = port;
		this.emailServer = emailServer;
	}


//	GET & SET-METHODEN

	public EmailServer getEmailServer() {
		return emailServer;
	}

	public void setEmailServer(EmailServer emailServer) {
		this.emailServer = emailServer;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public EmailServer holeEmailServer() {
		return emailServer;
	}

	@Override
	protected void neuerMitarbeiter(Socket socket) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPServer), neuerMitarbeiter("+socket+")");
		SMTPMitarbeiter smtpMitarbeiter;

		smtpMitarbeiter = new SMTPMitarbeiter((TCPSocket)socket, this);
		smtpMitarbeiter.starten();
		mitarbeiter.add(smtpMitarbeiter);
	}
}
