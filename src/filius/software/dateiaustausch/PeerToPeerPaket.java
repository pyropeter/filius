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

import java.util.Random;
import java.util.StringTokenizer;

import filius.Main;

/**
 * @author Nadja Haßler
 *
 */
public class PeerToPeerPaket {


	/* Attribute ---------------------------------------------------------------------------------------------*/

	protected int guid; //16 Byte lange zufaellige Zahl zur Identifizierung von Paketen, 128 Bit, also Zahlen zwischen 0 und 6.805647338418769*10^38, hier ist die aber nur 2^31-1 gross hoechstens

	protected String payload; // gibt Art des Pakets an: 0x00 (Ping), 0x01 (Pong), 0x80 (Query), 0x81 (Query-Hit) oder 0x40 (Push), Groesse 1 Byte

	protected int ttl; // die Lebensdauer eines Pakets, Groesse 1 Byte

	protected int hops; // die Anzahl der Hops, die das Paket schon zurueckgelegt hat, Groesse 1 Byte

	protected long payloadLength; //Laenge der Nutzdaten in Bits


	/* Konstruktoren ---------------------------------------------------------------------------------------------*/

	/**
	 * einziger benoetiger Konstruktor
	 */
	public PeerToPeerPaket(){
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (PeerToPeerPaket), constr: PeerToPeerPaket()");
		this.guid=guidErstellen();
		this.payload="";
		this.ttl=8;
		this.hops=0;
	}

	/**
	 * wandelt einen String (wenn möglich) in ein PeerToPeerPaket um
	 */
	public PeerToPeerPaket(String string){
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (PeerToPeerPaket), constr: PeerToPeerPaket("+string+")");
//		String wird nach "//" getrennt
		StringTokenizer tk=new StringTokenizer(string,"//");
//		Absichern der Informationen
		guid=Integer.parseInt(tk.nextToken());
		payload=tk.nextToken();
		hops=Integer.parseInt(tk.nextToken());
		ttl=Integer.parseInt(tk.nextToken());
		payloadLength=Integer.parseInt(tk.nextToken());
	}

	/* Operationen ---------------------------------------------------------------------------------------------*/

	protected long anzahlBenoetigterBits(long zahl){
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (PeerToPeerPaket), anzahlBenoetigterBits("+zahl+")");
		int exponent=0;
		while (potenzieren(2,exponent)<zahl){
			exponent++;
		}
		return exponent;
	}

	private int potenzieren(int basis, int exponent){
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (PeerToPeerPaket), potenzieren("+basis+","+exponent+")");
/*
		int ergebnis=1;
		for (int i=0;i<exponent;i++){
			ergebnis=ergebnis*basis;
		}
		return ergebnis;
*/
		return (int) (java.lang.Math.pow((double) basis, (double) exponent));
	}

	/**
	 * erstellt eine zufaellige GUID im int-Bereich
	 * normalerweise sind diese Zahlen um einiges groesser, so jedoch handhabbarer
	 * @return erstellteGuid
	 */
	public int guidErstellen(){
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (PeerToPeerPaket), guidErstellen()");
		Random zufallszahl = new Random();
		int erstellteGuid=-1;
		while(erstellteGuid<0){
			erstellteGuid=zufallszahl.nextInt();
		}
			return erstellteGuid;
	}


	/**
	 * wandelt ein PeerToPeerPaket in einen String um
	 * @return der String das PeerToPeerPaket verpackt als String
	 */
	public String toString(){
		return getGuid()+"//"+getPayload()+"//"+getHops()+"//"
		+getTtl()+"//"+getPayloadLength();
	}

	/* Getter und Setter ---------------------------------------------------------------------------------------------*/

	public int getGuid() {
		return guid;
	}

	public void setGuid(int guid) {
		this.guid = guid;
	}

	public int getHops() {
		return hops;
	}

	public void setHops(int hops) {
		this.hops = hops;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}


	public long getPayloadLength() {
		return payloadLength;
	}

	public void setPayloadLength(long payloadLength) {
		this.payloadLength = payloadLength;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}


}
