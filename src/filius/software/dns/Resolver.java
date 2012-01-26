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
	private DNSNachricht holeResourceRecord(String typ, String domainname, String dnsServer) throws java.util.concurrent.TimeoutException {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Resolver), holeResourceRecord("+typ+","+domainname+")");
		DNSNachricht anfrage, antwort = null;
		String tmp;
		UDPSocket socket = null;

		if (dnsServer != null && !dnsServer.equals("")) {
			if (socket == null) {
				try {
					socket = new UDPSocket(getSystemSoftware(), dnsServer, 53);

					anfrage = new DNSNachricht(DNSNachricht.QUERY);
					anfrage.hinzuQuery(domainname + " " + typ + " IN");

					socket.verbinden();
					socket.senden(anfrage.toString());
					tmp = socket.empfangen(Verbindung.holeRTT());
					if (tmp == null) {
						Main.debug.println("ERROR (" + this.hashCode() + "): keine Antwort auf Query empfangen");
						throw new TimeoutException(); // inform calling function
													  // about Timeout
					}

					antwort = new DNSNachricht(tmp);
					if (antwort.getId() != anfrage.getId()) {
						return null;
					}
					socket.schliessen();
					socket = null;
				} catch (VerbindungsException e) {
					e.printStackTrace(Main.debug);
					return null;
				}
			}
		} else {
			return null;
		}

		return antwort;
	}

	/**
	 * Methode zur Aufloesung eines Domainnamens zu einer IP-Adresse.
	 *
	 * @param domainname
	 * @return
	 */
	public String holeIPAdresse(String domainname) throws TimeoutException {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Resolver), holeIPAdresse("+domainname+")");
		DNSNachricht antwort;
		String adresse, dnsServerDomain;
		String dnsServer = getSystemSoftware().getDNSServer();

		if (domainname.equalsIgnoreCase("localhost")) {
			return "127.0.0.1";
		}
		
		adresse = IP.ipCheck(domainname); 
		if (adresse != null) {		// is domainname already a valid IPv4 address? 
			                        // then return its content without leading zeros
			return adresse;
		}

		while (dnsServer != null) {
			antwort = holeResourceRecord(ResourceRecord.ADDRESS, domainname,
					dnsServer);
			if (antwort == null) {
				return null;
			}

			adresse = durchsucheRecordListe(ResourceRecord.ADDRESS, domainname,
					antwort.holeAntwortResourceRecords());
			if (adresse != null)
				return adresse;

			adresse = durchsucheRecordListe(ResourceRecord.ADDRESS, domainname,
					antwort.holeAuthoritativeResourceRecords());
			if (adresse != null)
				return adresse;

			adresse = durchsucheRecordListe(ResourceRecord.ADDRESS, domainname,
					antwort.holeZusatzResourceRecords());
			if (adresse != null)
				return adresse;

			dnsServerDomain = durchsucheRecordListe(ResourceRecord.NAME_SERVER,
					antwort.holeAntwortResourceRecords());
			dnsServer = durchsucheRecordListe(ResourceRecord.ADDRESS,
					dnsServerDomain, antwort.holeAntwortResourceRecords());
		}

		return null;
	}

	public String holeIPAdresseMailServer(String domainname) throws TimeoutException {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Resolver), holeIPAdressMailServer("+domainname+")");
		DNSNachricht antwort=null;
		String mailserver=null, adresse, dnsServerDomain;
		String dnsServer = getSystemSoftware().getDNSServer();

		while (dnsServer != null && mailserver == null) {
			antwort = holeResourceRecord(ResourceRecord.MAIL_EXCHANGE,
					domainname, dnsServer);
			if (antwort == null) {
				return null;
			}

			mailserver = durchsucheRecordListe(ResourceRecord.MAIL_EXCHANGE,
					domainname, antwort.holeAntwortResourceRecords());
			if (mailserver == null) {
				mailserver = durchsucheRecordListe(
						ResourceRecord.MAIL_EXCHANGE, domainname,
						antwort.holeAntwortResourceRecords());
			}
			if (mailserver == null) {
				mailserver = durchsucheRecordListe(
						ResourceRecord.MAIL_EXCHANGE, domainname,
						antwort.holeAntwortResourceRecords());
			}
			if (mailserver == null) {
				dnsServerDomain = durchsucheRecordListe(
						ResourceRecord.NAME_SERVER,
						antwort.holeAntwortResourceRecords());
				dnsServer = durchsucheRecordListe(ResourceRecord.ADDRESS,
						dnsServerDomain, antwort.holeAntwortResourceRecords());
			}
		}
		if (mailserver == null)
			return null;

		adresse = durchsucheRecordListe(ResourceRecord.ADDRESS, mailserver,
				antwort.holeAntwortResourceRecords());
		if (adresse != null) return adresse;

		adresse = durchsucheRecordListe(ResourceRecord.ADDRESS, mailserver,
				antwort.holeAuthoritativeResourceRecords());
		if (adresse != null) return adresse;

		adresse = durchsucheRecordListe(ResourceRecord.ADDRESS, mailserver,
				antwort.holeZusatzResourceRecords());
		if (adresse != null) return adresse;

		else {
			return holeIPAdresse(mailserver);
		}
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
