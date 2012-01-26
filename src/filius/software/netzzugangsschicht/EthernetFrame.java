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
package filius.software.netzzugangsschicht;

import java.io.Serializable;

import filius.Main;

/**
 * Diese Klasse implementiert einen Frame auf der Netzzugangsschicht.
 */
public class EthernetFrame implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Protokolltypen der Vermittlungsschicht */
	public static final String IP = "0x800", ARP = "0x806";

	/** Die Ziel-Adresse des Frames */
	private String zielMacAdresse;

	/** Die MAC-Adresse, von dem sendenden Rechner */
	private String quellMacAdresse;

	/** Typ des uebergeordneten Protokolls (ARP oder IP) */
	private String typ;
	private boolean icmpFlag;

	/** die Nutzdaten */
	private Object daten;

	/** Konstruktor zur Initialisierung der Attribute des Frames */
	public EthernetFrame(Object daten, String quellMacAdresse,
			String zielMacAdresse, String typ) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (EthernetFrame), constr: EthernetFrame("+daten+","+quellMacAdresse+","+zielMacAdresse+","+typ+")");
		this.zielMacAdresse = zielMacAdresse;
		this.quellMacAdresse = quellMacAdresse;
		this.typ = typ;
		this.daten = daten;
		this.icmpFlag=false;
	}
	/** Konstruktor zur Initialisierung der Attribute des Frames */
	public EthernetFrame(Object daten, String quellMacAdresse,
			String zielMacAdresse, String typ, boolean icmp) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (EthernetFrame), constr: EthernetFrame("+daten+","+quellMacAdresse+","+zielMacAdresse+","+typ+","+icmp+")");
		this.zielMacAdresse = zielMacAdresse;
		this.quellMacAdresse = quellMacAdresse;
		this.typ = typ;
		this.daten = daten;
		this.icmpFlag = icmp;
	}

	public boolean isICMP() {
		return (this.typ.equals(IP) && this.icmpFlag);
	}
	
	/** Zugriff auf die Daten, die mit dem Frame verschickt werden */
	public Object getDaten() {
		return daten;
	}

	/** Zugriff auf die Absender-MAC-Adresse */
	public String getQuellMacAdresse() {
		return quellMacAdresse;
	}

	/** Zugriff auf den Protokolltyp. Zulaessig sind ARP und IP */
	public String getTyp() {
		return typ;
	}

	/** Methode fuer den Zugriff auf die Ziel-MAC-Adresse */
	public String getZielMacAdresse() {
		return zielMacAdresse;
	}
	
	public String toString() {
		return "["
			 + "quellMAC="+quellMacAdresse+", "
			 + "zielMAC="+zielMacAdresse+", "
			 + "typ="+typ+", "
			 + "icmpFlag="+icmpFlag
			 + "]";
	}
}
