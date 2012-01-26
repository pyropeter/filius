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
package filius.software.vermittlungsschicht;

import filius.Main;
import filius.exception.VerbindungsException;
import filius.software.ProtokollThread;
import filius.software.system.InternetKnotenBetriebssystem;

/**
 * Thread zur Ueberwachung des IP-Puffers, in den von der Ethernet-Schicht
 * eingehende IP-Pakete geschrieben werden
 */
public class IPThread extends ProtokollThread {

	/** Die IPsschicht */
	private IP vermittlung;

	/**
	 * Konstruktor zur Initialisierung des zu ueberwachenden Puffers und der
	 * IPsschicht.
	 */
	public IPThread(IP vermittlung) {
		super(((InternetKnotenBetriebssystem) vermittlung.holeSystemSoftware())
				.holeEthernet().holeIPPuffer());
		Main.debug.println("INVOKED-2 ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (IPThread), constr: IPThread("+vermittlung+")");

		this.vermittlung = vermittlung;
	}

	/**
	 * In dieser Methode wird zunaechst das Feld Time-to-Live (TTL)
	 * des eingehenden Pakets dekrementiert. Anschliessend wird es an
	 * die Methode weiterleitenPaket() des IP uebergeben. Dort werden
	 * Pakete, die fuer diesen Rechner bestimmt sind, an die
	 * Transportschicht weiter gegeben und Pakete an andere Rechner
	 * weitergeleitet.
	 */
	protected void verarbeiteDatenEinheit(Object datenEinheit) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (IPThread), verarbeiteDatenEinheit("+datenEinheit.toString()+")");
		// String zielIPAdresse;
		IpPaket ipPaket = (IpPaket) datenEinheit;

		ipPaket.setTtl(ipPaket.getTtl()-1);
		try {
			vermittlung.weiterleitenPaket(ipPaket);
		}
		catch (VerbindungsException e) {
			e.printStackTrace(Main.debug);
		}
	}
}
