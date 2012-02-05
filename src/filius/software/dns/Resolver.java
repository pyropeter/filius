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

import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

import filius.Main;
import filius.exception.VerbindungsException;
import filius.hardware.Verbindung;
import filius.software.clientserver.ClientAnwendung;
import filius.software.transportschicht.UDPSocket;
import filius.software.vermittlungsschicht.IP;

/**
 * Ausschnitt aus dem RFC 1035: <br />
 * <b>Question section format </b> <br />
 * The question section is used to carry the "question" in most queries, i.e.,
 * the parameters that define what is being asked. The section contains QDCOUNT
 * (usually 1) entries, each of the following format: ...
 * <ol>
 * <li> QNAME a domain name represented as a sequence of labels, where each
 * label consists of a length octet followed by that number of octets. The
 * domain name terminates with the zero length octet for the null label of the
 * root. Note that this field may be an odd number of octets; no padding is
 * used. </li>
 * <li> QTYPE a two octet code which specifies the type of the query. The values
 * for this field include all codes valid for a TYPE field, together with some
 * more general codes which can match more than one type of RR. </li>
 * <li> QCLASS a two octet code that specifies the class of the query. For
 * example, the QCLASS field is IN for the Internet. </li>
 * </ol>
 *
 * <b> Resource record format </b> <br />
 * The answer, authority, and additional sections all share the same format: a
 * variable number of resource records, where the number of records is specified
 * in the corresponding count field in the header. Each resource record has the
 * following format: ...
 * <ol>
 * <li> NAME a domain name to which this resource record pertains. </li>
 * <li> TYPE two octets containing one of the RR type codes. This field
 * specifies the meaning of the data in the RDATA field. </li>
 * <li> CLASS two octets which specify the class of the data in the RDATA field.
 * </li>
 * <li> TTL a 32 bit unsigned integer that specifies the time interval (in
 * seconds) that the resource record may be cached before it should be
 * discarded. Zero values are interpreted to mean that the RR can only be used
 * for the transaction in progress, and should not be cached. </li>
 * <li> RDLENGTH an unsigned 16 bit integer that specifies the length in octets
 * of the RDATA field. </li>
 * <li> RDATA a variable length string of octets that describes the resource.
 * The format of this information varies according to the TYPE and CLASS of the
 * resource record. For example, the if the TYPE is A and the CLASS is IN, the
 * RDATA field is a 4 octet ARPA Internet address. </li>
 * </ol>
 */

public class Resolver extends ClientAnwendung {

	/**
	 * Methode zum Abruf eines Resource Record. Das Rueckgabeformat ist NAME
	 * TYPE CLASS TTL RDATA (Bsp. web.de. A IN 3600 217.72.195.42)
	 *
	 * @param typ
	 * @param domainname
	 * @return einen Resource Record oder 'null', wenn ein Fehler bei der
	 *         Verbindung auftritt
	 */

	/**
	 * Methode zum Versenden einer Anfrage und zur Rueckgabe der
	 * DNS-Server-Antwort als DNSNachricht
	 */
	private DNSNachricht holeResourceRecord(String typ, String domainname, String dnsServer) {
		if (dnsServer == null || dnsServer.trim().equals("")) {
			return null;
		}

		DNSNachricht anfrage = new DNSNachricht(DNSNachricht.QUERY);
		anfrage.hinzuQuery(domainname + " " + typ + " IN");

		String antwortStr;
		try {
			UDPSocket socket = new UDPSocket(getSystemSoftware(), dnsServer, 53);
			socket.verbinden();
			socket.senden(anfrage.toString());
			antwortStr = socket.empfangen(Verbindung.holeRTT());
			socket.schliessen();
		} catch (VerbindungsException e) {
			return null;
		}

		if (antwortStr == null) {
			return null;
		}
		DNSNachricht antwort = new DNSNachricht(antwortStr);

		if (antwort.getId() != anfrage.getId()) {
			return null;
		}

		return antwort;
	}

	public String holeIterativ(String typ, String domain) {
		String server = getSystemSoftware().getDNSServer();
		if (server == null || server.trim().equals("")) {
			return null;
		}

		DNSNachricht antwort;
		String res;
		for (int i = 0; i < 10; i++) {
			antwort = holeResourceRecord(typ, domain, server);
			if (antwort == null) {
				return null;
			}

			LinkedList<ResourceRecord> records = new LinkedList<ResourceRecord>();
			records.addAll(antwort.holeAntwortResourceRecords());
			records.addAll(antwort.holeAuthoritativeResourceRecords());
			records.addAll(antwort.holeZusatzResourceRecords());

			res = durchsucheRecordListe(typ, domain, records);
			if (res != null) {
				return res;
			}
			res = durchsucheRecordListe(ResourceRecord.ADDRESS, records);
			if (res == null || res == server) {
				return null;
			}
			server = res;
		}
		return null;
	}

	/**
	 * Methode zur Aufloesung eines Domainnamens zu einer IP-Adresse.
	 *
	 * @param domainname
	 * @return
	 */
	public String holeIPAdresse(String domain) {
		if (domain.equalsIgnoreCase("localhost")) {
			return "127.0.0.1";
		}

		String adresse = IP.ipCheck(domain);
		if (adresse != null) {
			return adresse;
		}

		return holeIterativ(ResourceRecord.ADDRESS, domain);
	}

	public String holeIPAdresseMailServer(String domain) {
		String mxdomain = holeIterativ(ResourceRecord.MAIL_EXCHANGE, domain);
		if (mxdomain == null) {
			return null;
		}

		return holeIterativ(ResourceRecord.ADDRESS, mxdomain);
	}

	private String durchsucheRecordListe(String typ, LinkedList<ResourceRecord> liste) {
		for (ResourceRecord rr : liste) {
			if (rr.getType().equals(typ)) {
				return rr.getRdata();
			}
		}
		return null;
	}

	private String durchsucheRecordListe(String typ, String domainname, LinkedList<ResourceRecord> liste) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Resolver), durchsucheRecordListe("+typ+","+domainname+","+liste+")");
		
		for (ResourceRecord rr : liste) {
			if (rr.getDomainname().equalsIgnoreCase(domainname)
					&& rr.getType().equals(typ)) {
				return rr.getRdata();
			}
		}
		return null;
	}
}
