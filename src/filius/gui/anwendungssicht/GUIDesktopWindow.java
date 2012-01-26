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
package filius.gui.anwendungssicht;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import filius.Main;
import filius.gui.netzwerksicht.GUISidebar;
import filius.hardware.Hardware;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Rechner;
import filius.software.system.Betriebssystem;

public class GUIDesktopWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	private GUIDesktopPanel desktopPanel;

	public GUIDesktopWindow(Betriebssystem bs) {
		super();

		Hardware hardware;
		String imageFile = null;

		hardware = bs.getKnoten();
		if (hardware instanceof Rechner)
			imageFile = GUISidebar.RECHNER;
		else if (hardware instanceof Notebook)
			imageFile = GUISidebar.NOTEBOOK;

		ImageIcon icon = new ImageIcon(getClass().getResource("/"+imageFile));
		setIconImage(icon.getImage());

		setSize(640,480);
		//setBounds(100, 100, 640, 480);
		setResizable(false);

		desktopPanel = new GUIDesktopPanel(bs);
		getContentPane().add(desktopPanel);
	}

	public void setVisible(boolean flag) {
		super.setVisible(flag);

		String title;

		title = desktopPanel.getBetriebssystem().getKnoten().getName() + " - "
				+ desktopPanel.getBetriebssystem().holeIPAdresse();
		//Main.debug.println("GUIDesktopWindow: Titel = " + title);
		setTitle(title);

		if (flag) {
			toFront();
		}
	}

	public Betriebssystem getBetriebssystem() {
		return desktopPanel.getBetriebssystem();
	}
}
