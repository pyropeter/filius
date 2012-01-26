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
package filius.hardware;

import java.io.Serializable;
import java.util.LinkedList;

import filius.Main;
import filius.exception.VerbindungsException;
import filius.software.netzzugangsschicht.EthernetFrame;

public class Port implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private LinkedList<EthernetFrame> eingangsPuffer = new LinkedList<EthernetFrame>();
	private LinkedList<EthernetFrame> ausgangsPuffer = new LinkedList<EthernetFrame>();
	private Verbindung verbindung = null;
	private NetzwerkInterface nic = null;
	
	// constructor with parameter for all other nodes with explicit NIC for each port
	public Port(NetzwerkInterface nic) {
		this.nic = nic;
	}

	// constructor without parameters for switches
	public Port() {
	}
	
	public NetzwerkInterface getNIC() {
		return nic;
	}
	
	public boolean isPortFrei(){
		return (verbindung == null);
	}

	public boolean setVerbindung(Verbindung verbindung) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Port), setVerbindung("+verbindung+")");
		if(isPortFrei()){
			this.verbindung = verbindung;
			return true;
		}
		else{
			//Main.debug.println("\tPort: Dieser Port ist schon belegt. Port "+this);

			return false;
		}
	}

	public void entferneVerbindung() throws VerbindungsException{
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Port), entferneVerbindung()");
		this.verbindung = null;
	}

	public LinkedList<EthernetFrame> holeAusgangsPuffer() {
		return ausgangsPuffer;
	}

	public LinkedList<EthernetFrame> holeEingangsPuffer() {
		return eingangsPuffer;
	}
	
	public void setzeEingangsPuffer(LinkedList<EthernetFrame> puffer) {
		this.eingangsPuffer = puffer;
	}

	public Verbindung getVerbindung() {
		return verbindung;
	}

}
