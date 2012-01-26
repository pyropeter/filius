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

import java.util.StringTokenizer;

import filius.Main;

/**  * <b>Question section format </b> <br />
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
 */
public class Query {

	/** QNAME a domain name */
	private String qname = null;

	/** QTYPE a code which specifies the type of the query.
	 * Zur Belegung dieses Attributs sollten die entsprechenden Konstanten
	 * der Klasse DNSNachricht verwendet werden */
	private String qtype = null;

	/** QCLASS a code that specifies the class of the query. For
 * example, the QCLASS field is IN for the Internet. */
	private String qclass = "IN";

	/** Ein Konstruktor, der aus einem String, der durch den Aufruf der
	 * Methode toString() erzeugt wurde, wieder eine Query erstellt.
	 *
	 * @param nachricht ein String, der durch die Methode toString()
	 *   erstellt wurde
	 */
	public Query(String query) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Query), constr: Query("+query+")");
		StringTokenizer tokenizer;
		String token;

		tokenizer = new StringTokenizer(query, " ");

		if (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken().trim();
			qname = token;
		}

		if (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken().trim();
			qtype = token;
		}

		if (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken().trim();
			qclass = token;
		}
	}

	/** Methode zum Abruf einer Anfrage (query). Das Rueckgabeformat ist
	 * NAME TYPE CLASS (Bsp.: web.de. A IN) */
	public String toString() {
		return qname + " " + qtype + " " + qclass;
	}

	public String holeDomainname() {
		return qname;
	}

	public String holeTyp() {
		return qtype;
	}

	public String holeKlasse() {
		return qclass;
	}
}
