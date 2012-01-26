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

public class DNSVerknuepfung {

	private String wert; // kann eine IP oder eine MailDomain sein
	private String domain;
	private long ttl; // Lebensdauer
	private String typ; // kann a, mx oder ns(zur Zeit nicht implementiert) sein
	
	/**
	 * Ist die DNSVerknuepfung vom typ mx, dann 
	 * enthält wert keine IP sondern eine domain.
	 * Beispiel:
	 * typ: mx
	 * domain: gmx.de
	 * wert: mail.gmx.de
	 * 
	 * In dem Fall MUSS es dann noch eine weitere DNSVerknüpfung geben 
	 * 
	 * typ: a
	 * domain: mail.gmx.de
	 * wert: 81.25.21.123
	 * 
	 * 
	 * @return
	 */
	
	
	
	public long getTtl() {
		return ttl;
	}
	public void setTtl(long ttl) {
		this.ttl = ttl;
	}
	public String getTyp() {
		return typ;
	}
	public void setTyp(String typ) {
		this.typ = typ;
	}
	public String getWert() {
		return wert;
	}
	public void setWert(String wert) {
		this.wert = wert;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String toString()
	{
		String str = wert + "   " + ttl + "   " + typ + "   " + domain;
		return str;
	}
	
	
	
}
