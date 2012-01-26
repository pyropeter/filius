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
package filius.hardware.knoten;

import java.util.LinkedList;
import java.util.ListIterator;

import filius.Main;
import filius.hardware.NetzwerkInterface;
import filius.hardware.Port;

public abstract class LokalerKnoten extends Knoten {

	private LinkedList<Port> anschluesse = new LinkedList<Port>();

	public Port holeFreienPort(){
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (LokalerKnoten), holeFreienPort()");
		ListIterator iter = anschluesse.listIterator();
		while(iter.hasNext()){
			Port anschluss = (Port) iter.next();
			if(anschluss.isPortFrei()){
				//Main.debug.println("LokalerKnoten: Port "+iter.nextIndex()+" ist noch frei.");
				return anschluss;
			}
		}
		return null;
	}

	public Port getErstenAnschluss() {
		if (anschluesse != null && anschluesse.size()>0) {
			return (Port)anschluesse.getFirst();
		}
		return null;
	}

	public LinkedList<Port> getAnschluesse() {
		return anschluesse;
	}

	public void setAnschluesse(LinkedList<Port> anschluesse) {
		this.anschluesse = anschluesse;
	}


	public int holeAnzahlAnschluesse() {
		return anschluesse.size();
	}

	public void setzeAnzahlAnschluesse(int anzahlAnschluesse) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (LokalerKnoten), setzeAnzahlAnschluesse("+anzahlAnschluesse+")");
        LinkedList<Port> anschluesse;

        anschluesse = new LinkedList<Port>();
        for (int i=0;i<anzahlAnschluesse;i++)
        {
        	anschluesse.add(new Port());
        }
        setAnschluesse(anschluesse);
    }
}
