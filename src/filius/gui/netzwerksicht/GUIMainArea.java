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
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.ImageIcon;

import filius.Main;
import filius.gui.GUIContainer;
import filius.gui.JBackgroundPanel;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Rechner;
import filius.hardware.knoten.Switch;
import filius.hardware.knoten.Vermittlungsrechner;

/**
 * Diese Klasse dient als Oberklasse für die verschiedenen Sichten im Haupt-Bereich
 * der GUI.
 */
public class GUIMainArea extends JBackgroundPanel implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Macht ein Update des Panels. Dabei wird der gesamte Inhalt des
	 * Panels geloescht und ganz neu mit den Elementen der itemlist und cablelist
	 * befuellt.
	 *
	 * @author Johannes Bade & Thomas Gerding
	 *
	 */
	public void updateViewport(LinkedList knoten, LinkedList kabel) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (GUIMainArea), updateViewport("+knoten+","+kabel+")");
		GUIKnotenItem tempitem;
		JSidebarButton templabel;
		ListIterator it;
		Knoten tempKnoten;
		GUIKabelItem tempcable;

		removeAll();

		it = knoten.listIterator();

		while (it.hasNext()) {
			tempitem = (GUIKnotenItem) it.next();
			tempKnoten = (Knoten) tempitem.getKnoten();
			templabel = tempitem.getImageLabel();
			
			if(tempKnoten==null || templabel==null) continue;  // continue in case some elements are not yet created correctly

			tempKnoten.addObserver(templabel);
			tempKnoten.getSystemSoftware().addObserver(templabel);

			templabel.setText(tempKnoten.getName());
			templabel.setHardwareTyp(tempKnoten.holeHardwareTyp());
			if (tempitem.getKnoten() instanceof Switch) {
				if(((Switch)tempitem.getKnoten()).isCloud())
					templabel.setIcon(new ImageIcon(getClass().getResource("/"+GUISidebar.SWITCH_CLOUD)));
				else
					templabel.setIcon(new ImageIcon(getClass().getResource("/"+GUISidebar.SWITCH)));
			}
			else if (tempitem.getKnoten() instanceof Vermittlungsrechner) {
				templabel.setIcon(new ImageIcon(getClass().getResource("/"+GUISidebar.VERMITTLUNGSRECHNER)));
			}
			else if (tempitem.getKnoten() instanceof Rechner) {
				templabel.setIcon(new ImageIcon(getClass().getResource("/"+GUISidebar.RECHNER)));
			}
			else if (tempitem.getKnoten() instanceof Notebook) {
				templabel.setIcon(new ImageIcon(getClass().getResource("/"+GUISidebar.NOTEBOOK)));
			}
			else if (tempitem.getKnoten() instanceof Modem) {
				templabel.setIcon(new ImageIcon(getClass().getResource("/"+GUISidebar.MODEM)));
			}
			else {
				templabel = null;
				Main.debug.println("ERROR ("+this.hashCode()+"): Hardware-Komponente "
						+ tempitem.getKnoten().holeHardwareTyp()
						+ " ist nicht bekannt.");
			}

			templabel.setBounds(tempitem.getImageLabel().getBounds());
			add(templabel);
		}

		it = kabel.listIterator();
		while (it.hasNext()) {
			tempcable = (GUIKabelItem) it.next();
			add(tempcable.getKabelpanel());
		}

		add(GUIContainer.getGUIContainer().getMarkierung());

		updateUI();
	}
}
