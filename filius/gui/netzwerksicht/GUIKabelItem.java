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
package filius.gui.netzwerksicht;

import java.io.Serializable;

import filius.hardware.Kabel;
/**
 *
 * Die Klasse GUIKabelItem ist die Verbindung von Grafikdarstellung und "realem" Kabel.
 * Sie enthält Jeweils ein JCablePanel und ein Kabel.
 * Dazu stellt es die getter und setter Methoden bereit.
 *
 * @author Thomas Gerding & Johannes Bade
 *
 */
public class GUIKabelItem implements Serializable{

	private static final long serialVersionUID = 1L;
	private JCablePanel kabelpanel;
	private Kabel dasKabel;

	public GUIKabelItem()
	{
		kabelpanel = new JCablePanel();
	}

	public Kabel getDasKabel() {

		return dasKabel;
	}

	public void setDasKabel(Kabel dasKabel) {
		this.dasKabel = dasKabel;
		dasKabel.addObserver(kabelpanel);
	}

	public JCablePanel getKabelpanel() {
		return kabelpanel;
	}

	public void setKabelpanel(JCablePanel kabelpanel) {
		this.kabelpanel = kabelpanel;
	}
	
	public String toString() {
		String result = "[";
		if(dasKabel!=null) result += "dasKabel (id)="+dasKabel.hashCode()+", ";
		else result += "dasKabel=<null>, ";
		if(kabelpanel!=null) {
			result += "kabelpanel (id)="+kabelpanel.hashCode()+", ";
			if(kabelpanel.getZiel1() != null) {
				result += "kabelpanel.ziel1 (id)"+kabelpanel.getZiel1().hashCode()+", ";
				if(kabelpanel.getZiel1().getKnoten()!=null) result+= "kabelpanel.ziel1.knoten (name)"+kabelpanel.getZiel1().getKnoten().getName()+", ";
				else result += "kabelpanel.ziel1.knoten=<null>, ";
			}
			else result+= "kabelpanel.ziel1=<null>, ";
			if(kabelpanel.getZiel2() != null) {
				result += "kabelpanel.ziel2 (id)"+kabelpanel.getZiel2().hashCode()+", ";
				if(kabelpanel.getZiel2().getKnoten()!=null) result+= "kabelpanel.ziel2.knoten (name)"+kabelpanel.getZiel2().getKnoten().getName()+", ";
				else result += "kabelpanel.ziel2.knoten=<null>, ";
			}
		}
		else result += "kabelpanel=<null>";
		result += "]";
		return result;
	}
}
