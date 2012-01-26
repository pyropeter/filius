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

import java.io.Serializable;

/** Diese Klasse umfasst die Attribute eines ARP-Pakets */
public class ArpPaket implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Protokoll-Typ der Vermittlungsschicht */
	private String protokollTyp;

	/** MAC-Adresse des sendenden Knotens */
	private String quellMacAdresse;

	/** IP-Adresse des sendenden Knotens */
	private String quellIp;

	/** MAC-Adresse des Zielknotens (i.d.R. Broadcast) */
	private String zielMacAdresse;

	/**
	 * Ziel-IP-Adresse bzw. die Adresse des Knotens, zu dem die MAC-Adresse
	 * gesucht wird
	 */
	private String zielIp;

	public String getProtokollTyp() {
		return protokollTyp;
	}

	public void setProtokollTyp(String protokollTyp) {
		this.protokollTyp = protokollTyp;
	}

	public String getQuellIp() {
		return quellIp;
	}

	public void setQuellIp(String quellIp) {
		this.quellIp = quellIp;
	}

	public String getQuellMacAdresse() {
		return quellMacAdresse;
	}

	public void setQuellMacAdresse(String quellMacAdresse) {
		this.quellMacAdresse = quellMacAdresse;
	}

	public String getZielIp() {
		return zielIp;
	}

	public void setZielIp(String zielIp) {
		this.zielIp = zielIp;
	}

	public String getZielMacAdresse() {
		return zielMacAdresse;
	}

	public void setZielMacAdresse(String zielMacAdresse) {
		this.zielMacAdresse = zielMacAdresse;
	}
	
	public String toString() {
		return "["
		     + "protokollTyp="+protokollTyp+", "
		     + "quellMacAdresse="+quellMacAdresse+", "
		     + "quellIp="+quellIp+", "
		     + "zielMacAdresse="+zielMacAdresse+", "
		     + "zielIp="+zielIp
		     + "]";
	}
}
