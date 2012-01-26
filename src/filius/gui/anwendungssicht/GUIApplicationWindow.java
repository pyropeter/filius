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

import java.awt.Dimension;
import java.awt.Image;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Observer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import filius.Main;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.software.Anwendung;



/**
 * Diese Klasse dient als Oberklasse für alle Anwendungsfenster
 *
 */
public abstract class GUIApplicationWindow extends JInternalFrame implements I18n, Observer {

	private static final long serialVersionUID = 1L;

	private GUIDesktopPanel desktop;
	private Anwendung anwendung;

	public GUIApplicationWindow(GUIDesktopPanel desktop, String appKlasse) {
		super();
		this.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);

		this.desktop=desktop;
		this.desktop.getDesktopPane().add(this);

		this.anwendung = desktop.getBetriebssystem().holeSoftware(appKlasse);
		this.anwendung.hinzuBeobachter(this);

		this.setPreferredSize(new Dimension(550,420));
		this.setClosable(true);
		this.setMaximizable(true);
		this.setIconifiable(false);
		this.setResizable(true);

		this.setTitle(anwendung.holeAnwendungsName());
		this.initIcon();
	}

	private void initIcon() {
		ListIterator it;
		HashMap tmpMap;
		String awName;
		ImageIcon image;
		boolean fertig = false;

		try {
			it = Information.getInformation().ladeProgrammListe().listIterator();

		while (it.hasNext() && !fertig) {
			tmpMap = (HashMap) it.next();
			awName = (String) tmpMap.get("Anwendung");

			if (awName.equals(anwendung.holeAnwendungsName())) {
				image = new ImageIcon(getClass().getResource("/"+((String) tmpMap.get("gfxFile"))));
				image.setImage(image.getImage().getScaledInstance(16, 16, Image.SCALE_AREA_AVERAGING));
			    setFrameIcon(image);
			    fertig = true;
			}
		}
		}
		catch (Exception e) {
			e.printStackTrace(Main.debug);
		}
	}

	public Anwendung holeAnwendung() {
		return anwendung;
	}

	public void showMessageDialog(String msg) {
		JOptionPane.showMessageDialog(desktop, msg);
	}

	public int showOptionDialog(Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue) {
		return JOptionPane.showOptionDialog(desktop, message, title, optionType, messageType, icon, options, initialValue);
	}

	public int showConfirmDialog(String msg) {
		return JOptionPane.showConfirmDialog(desktop, msg);
	}
	public void addFrame(JInternalFrame frame) {
		desktop.getDesktopPane().add(frame);
	}

	public void removeFrame(JInternalFrame frame) {
		desktop.getDesktopPane().remove(frame);
	}

	public void starteExterneAnwendung(String softwareName) {
		desktop.starteAnwendung(softwareName);
	}

	public void starteExterneAnwendung(String softwareName, String[] param) {
		desktop.starteAnwendung(softwareName, param);
	}

	public String[] holeParameter() {
		return desktop.getParameter();
	}

	public void zeigePopupMenu(JPopupMenu menu, int x, int y) {
		menu.show(desktop, x, y);
	}

	public void starten(String [] param) {	}
}
