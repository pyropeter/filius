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
package filius.rahmenprogramm;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;

import filius.Main;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Switch;
import filius.hardware.knoten.Vermittlungsrechner;

public class KnotenPersistenceDelegate extends DefaultPersistenceDelegate {

	public void writeObject(Object oldInstance, Encoder out)  {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", writeObject()");
		if (oldInstance instanceof Host) {
			//Main.debug.println("\tObjekt der Klasse Host");
			super.writeObject((Host)oldInstance, out);
		}
		else if (oldInstance instanceof Vermittlungsrechner) {
			//Main.debug.println("\tObjekt der Klasse Vermittlungsrechner");
			super.writeObject((Vermittlungsrechner)oldInstance, out);
		}
		else if (oldInstance instanceof Modem) {
			//Main.debug.println("\tObjekt der Klasse Modem");
			super.writeObject((Modem)oldInstance, out);
		}
		else if (oldInstance instanceof Switch) {
			//Main.debug.println("\tObjekt der Klasse Switch");
			super.writeObject((Switch)oldInstance, out);
		}
		else {
			//Main.debug.println("\tObjekt der Klasse 'unbekannt'");
			super.writeObject(oldInstance, out);
		}
	}
}
