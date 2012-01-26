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

import java.util.StringTokenizer;

import filius.Main;

/**
 * @author Nadja Haßler
 */
public class PingPaket extends PeerToPeerPaket {

	private String ip;

	public PingPaket() {
		super();
		Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+" (PingPaket), constr: PingPaket()");
		setPayload("0x00");
		this.setPayloadLength(0);
		this.setIp("");
	}

	/**
	 * wandelt einen String (wenn möglich) in ein PingPaket um
	 */
	public PingPaket(String string){
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (PingPaket), constr: PingPaket("+string+")");
//		String wird nach "//" getrennt
		StringTokenizer tk=new StringTokenizer(string,"//");
//		Absichern der Informationen
		guid=Integer.parseInt(tk.nextToken());
		payload=tk.nextToken();
		hops=Integer.parseInt(tk.nextToken());
		ttl=Integer.parseInt(tk.nextToken());
		payloadLength=Integer.parseInt(tk.nextToken());
		ip=tk.nextToken();
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * wandelt ein PingPaket in einen String um
	 * @return der String das PingPaket verpackt als String
	 */
	public String toString() {
		return getGuid()+"//"+getPayload()+"//"+getHops()
		+"//"+getTtl()+"//"+getPayloadLength()+"//"+getIp();
	}

}
