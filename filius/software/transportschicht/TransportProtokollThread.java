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
package filius.software.transportschicht;

import filius.Main;
import filius.exception.SocketException;
import filius.software.ProtokollThread;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.vermittlungsschicht.IpPaket;

public class TransportProtokollThread extends ProtokollThread {

	private TransportProtokoll protokoll;

	public TransportProtokollThread(TransportProtokoll protokoll) {
		super(((InternetKnotenBetriebssystem) protokoll.holeSystemSoftware())
				.holeIP().holePaketListe(protokoll.holeTyp()));
		Main.debug.println("INVOKED-2 ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (TransportProtokollThread), constr: TransportProtokollThread("+protokoll+")");
		this.protokoll = protokoll;
	}

	protected void verarbeiteDatenEinheit(Object datenEinheit) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (TransportProtokollThread), verarbeiteDatenEinheit("+datenEinheit.toString()+")");
		SocketSchnittstelle socket;
		IpPaket paket;
		Segment segment;

		paket = (IpPaket) datenEinheit;
		segment = (Segment) paket.getSegment();

//		Main.debug.println(getClass()
//				+ "\n\tverarbeiteDatenEinheit() aufgerufen" + "\n\tZielport: "
//				+ segment.getZielPort());

		try {
			socket = protokoll.holeSocket(segment.getZielPort());
			socket.hinzufuegen(paket.getSender(), segment.getQuellPort(),
					segment);
		}
		catch (SocketException e) {
			if (!paket.getEmpfaenger().equals("255.255.255.255")
					&& !paket.getEmpfaenger().equals("0.0.0.0"))
				e.printStackTrace(Main.debug);
		}
	}
}
