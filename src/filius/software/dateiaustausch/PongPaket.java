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
public class PongPaket extends PeerToPeerPaket {

	private String ipAdresse; // Die IP-Adresse des antwortenden Rechners
								// (der, der Pong schickt)

	private int port; // Portnummer, an welcher der antwortende Rechner
						// eingehende Verbindungen annehmen kann

	private int anzahlZurVerfuegungStehenderDateien; // selbsterklaerend

	private long anzahlZurVerfuegungStehenderKBs; // selbsterklaerend

	public PongPaket(String ip, int port,
			int anzahlZurVerfuegungStehenderDateien,
			long anzahlZurVerfuegungStehenderKBs) {
		super();
		Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+" (PongPaket), constr: PongPaket("+ip+","+port+","+anzahlZurVerfuegungStehenderDateien+","+anzahlZurVerfuegungStehenderKBs+")");
		setPayload("0x01");
		this.ipAdresse = ip;
		this.port = port;
		this.anzahlZurVerfuegungStehenderDateien = anzahlZurVerfuegungStehenderDateien;
		this.anzahlZurVerfuegungStehenderKBs = anzahlZurVerfuegungStehenderKBs;
		this.setPayloadLength(this.payloadLengthBerechnen());
	}

	/**
	 * wandelt einen String (wenn möglich) in ein PongPaket um
	 *
	 * @param string
	 *            der umzuwandelnde String
	 */
	public PongPaket(String string) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (PongPaket), constr: PongPaket("+string+")");
		// String wird nach "//" getrennt
		StringTokenizer tk = new StringTokenizer(string, "//");
		// Absichern der Informationen
		guid = Integer.parseInt(tk.nextToken());
		payload = tk.nextToken();
		hops = Integer.parseInt(tk.nextToken());
		ttl = Integer.parseInt(tk.nextToken());
		payloadLength = Integer.parseInt(tk.nextToken());
		ipAdresse = tk.nextToken();
		port = Integer.parseInt(tk.nextToken());
		anzahlZurVerfuegungStehenderDateien = Integer.parseInt(tk.nextToken());
		anzahlZurVerfuegungStehenderKBs = Integer.parseInt(tk.nextToken());
	}

	public long payloadLengthBerechnen() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (PongPaket), payloadLengthBerechnen()");
		long bits = 0;
		bits = ipAdresse.length() * 8 + anzahlBenoetigterBits(port)
				+ anzahlBenoetigterBits(anzahlZurVerfuegungStehenderDateien)
				+ anzahlBenoetigterBits(anzahlZurVerfuegungStehenderKBs);
		return bits;
	}

	public int getAnzahlZurVerfuegungStehenderDateien() {
		return anzahlZurVerfuegungStehenderDateien;
	}

	public void setAnzahlZurVerfuegungStehenderDateien(
			int anzahlZurVerfuegungStehenderDateien) {
		this.anzahlZurVerfuegungStehenderDateien = anzahlZurVerfuegungStehenderDateien;
	}

	public long getAnzahlZurVerfuegungStehenderKBs() {
		return anzahlZurVerfuegungStehenderKBs;
	}

	public void setAnzahlZurVerfuegungStehenderKBs(
			int anzahlZurVerfuegungStehenderKBs) {
		this.anzahlZurVerfuegungStehenderKBs = anzahlZurVerfuegungStehenderKBs;
	}

	public String getIpAdresse() {
		return ipAdresse;
	}

	public void setIpAdresse(String ipAdresse) {
		this.ipAdresse = ipAdresse;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * wandelt ein PongPaket in einen String um
	 *
	 * @param pongPaket
	 *            das umzuwandelnde PongPaket
	 * @return der String das PongPaket verpackt als String
	 */
	public String toString() {
		return getGuid() + "//" + getPayload() + "//" + getHops() + "//"
				+ getTtl() + "//" + getPayloadLength() + "//" + getIpAdresse()
				+ "//" + getPort() + "//"
				+ getAnzahlZurVerfuegungStehenderDateien() + "//"
				+ getAnzahlZurVerfuegungStehenderKBs();
	}

}
