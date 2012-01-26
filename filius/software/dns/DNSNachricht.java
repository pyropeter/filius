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
import java.util.ListIterator;
import java.util.StringTokenizer;

import filius.Main;

/**
 * Das Format einer DNS-Nachricht wird in RFC 1035 beschrieben. <br />
 * The header contains the following fields: <br />
 * <ol>
 * <li> ID </li>
 * <li> QR </li>
 * <li> OPCODE </li>
 * <li> AA </li>
 * <li> TC </li>
 * <li> RD </li>
 * <li> RA </li>
 * <li> Z </li>
 * <li> RCODE </li>
 * <li> QDCOUNT </li>
 * <li> ANCOUNT </li>
 * <li> NSCOUNT </li>
 * <li> ARCOUNT </li>
 * </ol>
 * 
 * <b> Resource record format </b> <br />
 * The answer, authority, and additional sections all share the same format
 */
public class DNSNachricht {

	/**
	 * Konstanten fuer das Bit im Kopfteil (Header), das indiziert, ob es sich
	 * um eine Anfrage (QUERY) oder eine Antwort (RESPONSE) handelt.
	 */
	public static final int QUERY = 0, RESPONSE = 1;

	/**
	 * Konstanten fuer den Response-Code: The values have the following
	 * interpretation:
	 * <ul>
	 * <li> 0 - No error condition </li>
	 * <li> 1 - Format error - The name server was unable to interpret the
	 * query. </li>
	 * <li> 2 - Server failure - The name server was unable to process this
	 * query due to a problem with the name server. </li>
	 * <li> 3 - Name Error - Meaningful only for responses from an authoritative
	 * name server, this code signifies that the domain name referenced in the
	 * query does not exist. </li>
	 * <li> 4 - Not Implemented - The name server does not support the requested
	 * kind of query. </li>
	 * <li> 5 - Refused - The name server refuses to perform the specified
	 * operation for policy reasons. For example, a name server may not wish to
	 * provide the information to the particular requester, or a name server may
	 * not wish to perform a particular operation (e.g., zone transfer) for
	 * particular data. </li>
	 * </ul>
	 */
	public static final int NO_ERROR = 0, FORMAT_ERROR = 1, SERVER_FAILURE = 2,
			NAME_ERROR = 3, NOT_IMPLEMENTED = 4, REFUSED = 5;

	/**
	 * ID A 16 bit identifier assigned by the program that generates any kind of
	 * query. This identifier is copied the corresponding reply and can be used
	 * by the requester to match up replies to outstanding queries.
	 */
	private int id = (int) (Math.random() * 65536);

	/**
	 * QR A one bit field that specifies whether this message is a query (0), or
	 * a response (1).
	 */
	private int queryResponse = QUERY;

	/**
	 * OPCODE A four bit field that specifies kind of query in this message.
	 * This value is set by the originator of a query and copied into the
	 * response. The values are:
	 * <ul>
	 * <li> 0 - a standard query (QUERY) </li>
	 * <li> 1 - an inverse query (IQUERY) </li>
	 * <li> 2 - a server status request (STATUS) </li>
	 * </ul>
	 */
	private int opcode = 0;

	/**
	 * AA Authoritative Answer - this bit is valid in responses, and specifies
	 * that the responding name server is an authority for the domain name in
	 * question section. Note that the contents of the answer section may have
	 * multiple owner names because of aliases. The AA bit corresponds to the
	 * name which matches the query name, or the first owner name in the answer
	 * section.
	 */
	private boolean authoritativeAnswer = false;

	/**
	 * TC TrunCation - specifies that this message was truncated due to length
	 * greater than that permitted on the transmission channel.
	 */
	private boolean truncated = false;

	/**
	 * RD Recursion Desired - this bit may be set in a query and is copied into
	 * the response. If RD is set, it directs the name server to pursue the
	 * query recursively. Recursive query support is optional.
	 */
	private boolean recursionDesired = true;

	/**
	 * RA Recursion Available - this be is set or cleared in a response, and
	 * denotes whether recursive query support is available in the name server.
	 */
	private boolean recursionAvailable = true;

	/**
	 * RCODE Response code - this 4 bit field is set as part of responses.
	 * <br />
	 * Dazu werden in dieser Klasse Konstanten zur Verfuegung gestellt.
	 */
	private int responseCode = NO_ERROR;

	/**
	 * QDCOUNT an unsigned 16 bit integer specifying the number of entries in
	 * the question section.
	 */
	private int queryCount = 0;

	/**
	 * ANCOUNT an unsigned 16 bit integer specifying the number of resource
	 * records in the answer section.
	 */
	private int answerCount = 0;

	/**
	 * NSCOUNT an unsigned 16 bit integer specifying the number of name server
	 * resource records in the authority records section.
	 */
	private int nameServerCount = 0;

	/** ARCOUNT Anzahl der Resource Records in der 'additional section' */
	private int additionalCount = 0;

	/**
	 * Anfragen (query) in der Nachricht
	 */
	private LinkedList<Query> queries = new LinkedList<Query>();

	/** Resource Records in der 'answer section' */
	private LinkedList<ResourceRecord> answerRecords = new LinkedList<ResourceRecord>();

	/** Resource Records in der 'authority section' */
	private LinkedList<ResourceRecord> authoratativeRecords = new LinkedList<ResourceRecord>();

	/** Resource Records in der 'additional section' */
	private LinkedList<ResourceRecord> additionalRecords = new LinkedList<ResourceRecord>();

	/**
	 * Konstruktor zur Erzeugung einer DNS-Nachricht mit Standardwerten.
	 * Lediglich ob es sich um eine Anfrage oder eine Antwort handelt, muss als
	 * Parameter uebergeben werden. Dazu werden die Konstanten QUERY und
	 * RESPONSE verwendet.
	 */
	public DNSNachricht(int queryResponse) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", DNSNachricht("+queryResponse+")");
		this.queryResponse = queryResponse;
	}

	/**
	 * Ein Konstruktor, der aus einem String, der durch den Aufruf der Methode
	 * toString() erzeugt wurde, wieder eine DNSNachricht erstellt.
	 * 
	 * @param nachricht
	 *            ein String, der durch die Methode toString() erstellt wurde
	 */
	public DNSNachricht(String nachricht) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DNSNachricht), constr: DNSNachricht("+nachricht+")");
		StringTokenizer lineTokenizer, tokenizer;
		String line, token;

		if (nachricht != null) {
			lineTokenizer = new StringTokenizer(nachricht, "\n");
			line = lineTokenizer.nextToken();

			tokenizer = new StringTokenizer(line, " ");
			while (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken().trim();

				if (token.startsWith("ID")) {
					id = Integer.parseInt(token.substring(3));
				}
				else if (token.startsWith("QR")) {
					queryResponse = Integer.parseInt(token.substring(3));
				}
				else if (token.startsWith("RCODE")) {
					responseCode = Integer.parseInt(token.substring(6));
				}
				else if (token.startsWith("QDCOUNT")) {
					queryCount = Integer.parseInt(token.substring(8));
				}
				else if (token.startsWith("ANCOUNT")) {
					answerCount = Integer.parseInt(token.substring(8));
				}
				else if (token.startsWith("NSCOUNT")) {
					nameServerCount = Integer.parseInt(token.substring(8));
				}
				else if (token.startsWith("ARCOUNT")) {
					additionalCount = Integer.parseInt(token.substring(8));
				}
			}

			for (int i = 0; i < queryCount && lineTokenizer.hasMoreTokens(); i++) {
				line = lineTokenizer.nextToken();
				queries.add(new Query(line));
			}
			for (int i = 0; i < answerCount && lineTokenizer.hasMoreTokens(); i++) {
				line = lineTokenizer.nextToken();
				answerRecords.add(new ResourceRecord(line));
			}
			for (int i = 0; i < nameServerCount
					&& lineTokenizer.hasMoreTokens(); i++) {
				line = lineTokenizer.nextToken();
				authoratativeRecords.add(new ResourceRecord(line));
			}
			for (int i = 0; i < additionalCount
					&& lineTokenizer.hasMoreTokens(); i++) {
				line = lineTokenizer.nextToken();
				additionalRecords.add(new ResourceRecord(line));
			}
		}

	}

	/**
	 * Diese Methode liefert die Nachricht mit ausgewaehlten Attributen des
	 * Kopfteils in der Form &lt;Attribut&gt;=&lt;Wert&gt; (z. B.
	 * &quot;ID=42&quot;), die durch Leerzeichen getrennt sind, und den
	 * Datenteil, der durch einen Zeilenumbruch vom Kopfteil abgetrennt ist.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		ListIterator<?> it;

		buffer.append("ID=" + id + " ");
		buffer.append("QR=" + queryResponse + " ");
		buffer.append("RCODE=" + responseCode + " ");
		buffer.append("QDCOUNT=" + queryCount + " ");
		buffer.append("ANCOUNT=" + answerCount + " ");
		buffer.append("NSCOUNT=" + nameServerCount + " ");
		buffer.append("ARCOUNT=" + additionalCount + " ");
		buffer.append("\n");

		it = queries.listIterator();
		while (it.hasNext()) {
			buffer.append(it.next().toString() + "\n");
		}

		it = answerRecords.listIterator();
		while (it.hasNext()) {
			buffer.append(it.next().toString() + "\n");
		}

		it = authoratativeRecords.listIterator();
		while (it.hasNext()) {
			buffer.append(it.next().toString() + "\n");
		}

		it = additionalRecords.listIterator();
		while (it.hasNext()) {
			buffer.append(it.next().toString() + "\n");
		}

		return buffer.toString();
	}

	/**
	 * Methode zum hinzufuegen einer Anfrage (query). Das Format muss
	 * folgendermassen sein: NAME TYPE CLASS (Bsp.: web.de. A IN)
	 */
	public void hinzuQuery(String anfrage) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DNSNachricht), hinzuQuery("+anfrage+")");
		queries.add(new Query(anfrage));
		queryCount++;
	}

	/** Methode fuer den Zugriff auf die Anfragen (queries) */
	public LinkedList<Query> holeQueries() {
		return queries;
	}

	/**
	 * Methode fuer den Zugriff auf die Resource-Records in der 'answer
	 * section'.
	 */
	public LinkedList<ResourceRecord> holeAntwortResourceRecords() {
		return answerRecords;
	}

	/**
	 * Methode fuer den Zugriff auf die Resource-Records in der 'authority
	 * section'.
	 */
	public LinkedList<ResourceRecord> holeAuthoritativeResourceRecords() {
		return authoratativeRecords;
	}

	/**
	 * Methode fuer den Zugriff auf die Resource-Records in der 'additional
	 * section'.
	 */
	public LinkedList<ResourceRecord> holeZusatzResourceRecords() {
		return additionalRecords;
	}

	/**
	 * Diese Methode fuegt der DNS-Nachricht einen Resource Record hinzu. Das
	 * Format muss folgendermassen aussehen: NAME TYPE CLASS TTL RDATA (Bsp.
	 * web.de. A IN 3600 217.72.195.42)
	 */
	public void hinzuAntwortResourceRecord(String record) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DNSNachricht), hinzuAntwortResourceRecord("+record+")");
		answerRecords.add(new ResourceRecord(record));
		answerCount++;
	}

	/**
	 * Diese Methode fuegt der DNS-Nachricht einen Resource Record hinzu. Das
	 * Format muss folgendermassen aussehen: NAME TYPE CLASS TTL RDATA (Bsp.
	 * web.de. A IN 3600 217.72.195.42)
	 */
	public void hinzuAuthoritativeResourceRecord(String record) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DNSNachricht), hinzuAuthoritativeResourceRecord("+record+")");
		Main.debug.println("INVOKED: "+getClass()+", hinzuAuthoritativeResourceRecord("+record+")");
		authoratativeRecords.add(new ResourceRecord(record));
		nameServerCount++;

	}

	/**
	 * @return the authoritativeAnswer
	 */
	public boolean isAuthoritativeAnswer() {
		return authoritativeAnswer;
	}

	/**
	 * @param authoritativeAnswer
	 *            the authoritativeAnswer to set
	 */
	public void setAuthoritativeAnswer(boolean authoritativeAnswer) {
		this.authoritativeAnswer = authoritativeAnswer;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the opcode
	 */
	public int getOpcode() {
		return opcode;
	}

	/**
	 * @param opcode
	 *            the opcode to set
	 */
	public void setOpcode(int opcode) {
		this.opcode = opcode;
	}

	/**
	 * @return the queryResponse
	 */
	public int getQueryResponse() {
		return queryResponse;
	}

	/**
	 * @param queryResponse
	 *            the queryResponse to set
	 */
	public void setQueryResponse(int queryResponse) {
		this.queryResponse = queryResponse;
	}

	/**
	 * @return the recursionAvailable
	 */
	public boolean isRecursionAvailable() {
		return recursionAvailable;
	}

	/**
	 * @param recursionAvailable
	 *            the recursionAvailable to set
	 */
	public void setRecursionAvailable(boolean recursionAvailable) {
		this.recursionAvailable = recursionAvailable;
	}

	/**
	 * @return the recursionDesired
	 */
	public boolean isRecursionDesired() {
		return recursionDesired;
	}

	/**
	 * @param recursionDesired
	 *            the recursionDesired to set
	 */
	public void setRecursionDesired(boolean recursionDesired) {
		this.recursionDesired = recursionDesired;
	}

	/**
	 * @return the responseCode
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * @param responseCode
	 *            the responseCode to set
	 */
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * @return the truncated
	 */
	public boolean isTruncated() {
		return truncated;
	}

	/**
	 * @param truncated
	 *            the truncated to set
	 */
	public void setTruncated(boolean truncated) {
		this.truncated = truncated;
	}

}
