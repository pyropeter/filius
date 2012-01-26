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

import filius.hardware.Hardware;
import filius.hardware.Port;
import filius.software.system.SystemSoftware;

public abstract class Knoten extends Hardware {

	private String name; // name der Hardware

    private SystemSoftware systemAnwendung; //je nach Art der Hardware ist dies ein Betriebssystem oder eine Firmware

    public abstract Port holeFreienPort();


	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if(systemAnwendung!=null) {
        	filius.Main.debug.println("DEBUG: node with SystemSoftware ("+systemAnwendung.hashCode()+") now has name '"+name+"'");
        }
    }

	public SystemSoftware getSystemSoftware() {
		return systemAnwendung;
	}

	public void setSystemSoftware(SystemSoftware systemAnwendung) {
		this.systemAnwendung = systemAnwendung;
	}
}
