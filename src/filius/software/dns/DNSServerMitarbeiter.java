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
package filius.software.dns;

import filius.Main;
import filius.software.clientserver.ServerMitarbeiter;
import filius.software.transportschicht.Socket;
import filius.software.transportschicht.UDPSocket;

public class DNSServerMitarbeiter extends ServerMitarbeiter {

	public DNSServerMitarbeiter(DNSServer server, Socket socket) {
		super(server, socket);
	}


	protected void verarbeiteNachricht(String dateneinheit) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DNSServerMitarbeiter), verarbeiteNachricht("+dateneinheit+")");
		DNSNachricht nachricht, antwort;
		ResourceRecord record;

		nachricht = new DNSNachricht(dateneinheit);
		antwort = new DNSNachricht(DNSNachricht.RESPONSE);
		antwort.setId(nachricht.getId());

		for (Query query : nachricht.holeQueries()) {
			server.benachrichtigeBeobachter(messages.getString("sw_dnsservermitarbeiter_msg1")+query);

			record = ((DNSServer)server).holeRecord(query.holeDomainname(), query.holeTyp());
			if (record == null) {
				record = ((DNSServer)server).holeRecord(query.holeDomainname());
			}
			
			if (record != null) {
				if (record.getType().equals(ResourceRecord.ADDRESS)) {
					antwort.hinzuAntwortResourceRecord(record.toString());
				}
				else if (record.getType().equals(ResourceRecord.NAME_SERVER)) {
					antwort.hinzuAntwortResourceRecord(record.toString());
					record = ((DNSServer)server).holeRecord(record.getRdata(), ResourceRecord.ADDRESS);
					if (record != null) {
						antwort.hinzuAntwortResourceRecord(record.toString());
					}
				}
				else if (record.getType().equals(ResourceRecord.MAIL_EXCHANGE)) {
					antwort.hinzuAntwortResourceRecord(record.toString());
					record = ((DNSServer)server).holeRecord(record.getRdata(), ResourceRecord.ADDRESS);
					if (record != null) {
						antwort.hinzuAntwortResourceRecord(record.toString());
					}
				}
			}
		}

		if (socket != null) {
			((UDPSocket)socket).senden(antwort.toString());

			server.benachrichtigeBeobachter(messages.getString("sw_dnsservermitarbeiter_msg2")+"\n>>>>\n"+antwort.toString()+"<<<<");
		}

	}

}
