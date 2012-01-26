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
public class QueryHitPaket extends PeerToPeerPaket {

	private int anzahlHits;

	private int port;

	private String ipAdresse;

	private String geschwindigkeit; // (in kb/sek)

	private String ergebnis;// =new LinkedList();

	private String serventIdentifizierung;

	public QueryHitPaket(int anzahlHips, int port, String ip, String speed,
			String ergebnisListe, String si) {
		super();
		Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+" (QueryHitPaket), constr: QueryHitPaket("+anzahlHips+","+port+","+ip+","+speed+","+ergebnisListe+","+si+")");
		setPayload("0x81");
		this.anzahlHits = anzahlHips;
		this.port = port;
		this.ipAdresse = ip;
		this.geschwindigkeit = speed;
		this.ergebnis = ergebnisListe;
		this.serventIdentifizierung = si;
		this.setPayloadLength(payloadLengthBerechnen());
	}

	/**
	 * wandelt einen String (wenn möglich) in ein QueryHitPaket um
	 *
	 * @param string
	 *            der umzuwandelnde String
	 */
	public QueryHitPaket(String string) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (QueryHitPaket), constr: QueryHitPaket("+string+")");
		// String wird nach "//" getrennt
		StringTokenizer tk = new StringTokenizer(string, "//");
		// Absichern der Informationen
		guid = Integer.parseInt(tk.nextToken());
		payload = tk.nextToken();
		hops = Integer.parseInt(tk.nextToken());
		ttl = Integer.parseInt(tk.nextToken());
		payloadLength = Integer.parseInt(tk.nextToken());
		anzahlHits = Integer.parseInt(tk.nextToken());
		port = Integer.parseInt(tk.nextToken());
		ipAdresse = tk.nextToken();
		geschwindigkeit = tk.nextToken();
		ergebnis = tk.nextToken();
		serventIdentifizierung = tk.nextToken();
	}

	public long payloadLengthBerechnen() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (QueryHitPaket), payloadLengthBerechnen()");
		long ergebnisZahl = 0;
		ergebnisZahl = ergebnisZahl + anzahlBenoetigterBits(anzahlHits)
				+ anzahlBenoetigterBits(port) + ipAdresse.length() * 8
				+ (geschwindigkeit.length() * 8) + ergebnis.length() * 8
				+ serventIdentifizierung.length() * 8;
		//Main.debug.println(" hits: " + anzahlBenoetigterBits(anzahlHits)
				//+ " port: " + anzahlBenoetigterBits(port) + " ip: "
				//+ ipAdresse.length() * 8 + " geschw: "
				//+ (geschwindigkeit.length() * 8) + " ergebnis: "
				//+ ergebnis.length() * 8 + " serverID: "
				//+ serventIdentifizierung.length() * 8);

		return ergebnisZahl;
	}

	public int getAnzahlHits() {
		return anzahlHits;
	}

	public void setAnzahlHits(int anzahlHips) {
		this.anzahlHits = anzahlHips;
	}

	public String getErgebnis() {
		return ergebnis;
	}

	public void setErgebnis(String ergebnisListe) {
		this.ergebnis = ergebnisListe;
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

	public String getServentIdentifizierung() {
		return serventIdentifizierung;
	}

	public void setServentIdentifizierung(String serventIdentifizierung) {
		this.serventIdentifizierung = serventIdentifizierung;
	}

	public String getGeschwindigkeit() {
		return geschwindigkeit;
	}

	public void setGeschwindigkeit(String speed) {
		this.geschwindigkeit = speed;
	}

	/**
	 * wandelt ein QueryHitPaket in einen String um
	 *
	 * @return der String das QueryHitPaket verpackt als String
	 */
	public String toString() {
		if (getErgebnis().equals("")) {
			setErgebnis(" ");
		}
		if (getServentIdentifizierung().equals("")) {
			setServentIdentifizierung(" ");
		}
		return getGuid() + "//" + getPayload() + "//" + getHops() + "//"
				+ getTtl() + "//" + getPayloadLength() + "//" + getAnzahlHits()
				+ "//" + getPort() + "//" + getIpAdresse() + "//"
				+ getGeschwindigkeit() + "//" + getErgebnis() + "//"
				+ getServentIdentifizierung();
	}
}
