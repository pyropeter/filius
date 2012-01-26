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

import java.io.Serializable;

public class Segment implements Serializable {

	private int quellPort;
	private int zielPort;
	private int pruefSumme;
	private String daten;

	public String getDaten() {
		return daten;
	}

	public void setDaten(String daten) {
		this.daten = daten;
	}

	public int getPruefSumme() {
		return pruefSumme;
	}

	public void setPruefSumme(int pruefSumme) {
		this.pruefSumme = pruefSumme;
	}

	public int getQuellPort() {
		return quellPort;
	}

	public void setQuellPort(int quellPort) {
		this.quellPort = quellPort;
	}

	public int getZielPort() {
		return zielPort;
	}

	public void setZielPort(int zielPort) {
		this.zielPort = zielPort;
	}
}
