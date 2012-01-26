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

/** <b> Resource record format </b> <br />
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
public class ResourceRecord {

	public static final String ADDRESS="A", MAIL_EXCHANGE="MX", NAME_SERVER="NS";

	/** NAME a domain name to which this resource record pertains. */
	private String domainname = null;

	/** TYPE two octets containing one of the RR type codes. This field
 * specifies the meaning of the data in the RDATA field.
 *  Zur Belegung dieses Attributs sollten die entsprechenden Konstanten
	 * verwendet werden*/
	private String type = null;

	/** TTL a 32 bit unsigned integer that specifies the time interval (in
 * seconds) that the resource record may be cached before it should be
 * discarded. */
	private int ttl = 3600;

	/** RDATA a string that describes the resource.
 * The format of this information varies according to the TYPE and CLASS of the
 * resource record. For example, the if the TYPE is A and the CLASS is IN, the
 * RDATA field is a 4 octet ARPA Internet address. */
	private String rdata = null;

	public ResourceRecord(String domainname, String typ, String rdata) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (ResourceRecord), constr: ResourceRecord("+domainname+","+typ+","+rdata+")");
		this.domainname = domainname;
		this.type = typ;
		this.rdata = rdata;
	}

	/** Ein Konstruktor, der aus einem String, der durch den Aufruf der
	 * Methode toString() erzeugt wurde, wieder einen Resource-Record erstellt.
	 *
	 * @param nachricht ein String, der durch die Methode toString()
	 *   erstellt wurde
	 */
	public ResourceRecord(String rr) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (ResourceRecord), constr: ResourceRecord("+rr+")");
		StringTokenizer tokenizer;
		String token;

		tokenizer = new StringTokenizer(rr, " ");

		if (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken().trim();
			domainname = token;
		}

		if (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken().trim();
			type = token;
		}

		if (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken().trim();
			try {
				ttl = Integer.parseInt(token);
			}
			catch(java.lang.NumberFormatException e) {
				Main.debug.println("EXCEPTION: NumberFormatException, TTL field: '"+token+"'");
				e.printStackTrace(Main.debug);
			}
		}

		if (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken().trim();
			rdata = token;
		}
	}

	/**
	 * Diese Methode liefert einen Resource Record als String zurueck.
	 * Methode zum Abruf eines Resource Record. Das Rueckgabeformat ist
	 * NAME TYPE CLASS TTL RDATA (Bsp. web.de. A IN 3600 217.72.195.42)
	 */
	public String toString() {
		return domainname + " " + type + " " + ttl + " " + rdata;
	}

	/**
	 * @return the domainname
	 */
	public String getDomainname() {
		return domainname;
	}

	/**
	 * @param domainname the domainname to set
	 */
	public void setDomainname(String domainname) {
		this.domainname = domainname;
	}

	/**
	 * @return the rdata
	 */
	public String getRdata() {
		return rdata;
	}

	/**
	 * @param rdata the rdata to set
	 */
	public void setRdata(String rdata) {
		this.rdata = rdata;
	}

	/**
	 * @return the ttl
	 */
	public int getTtl() {
		return ttl;
	}

	/**
	 * @param ttl the ttl to set
	 */
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
}
