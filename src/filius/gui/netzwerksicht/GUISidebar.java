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

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

import filius.gui.GUIContainer;
import filius.gui.JBackgroundPanel;
import filius.hardware.Kabel;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Rechner;
import filius.hardware.knoten.Switch;
import filius.hardware.knoten.Vermittlungsrechner;

/**
 * Klasse für das linke Panel in der Entwurfsansicht. Darin werden alle
 * nutzbaren Elemente für den Netzwerkentwurf angezeigt und können per Drag&Drop
 * in den Entwurfsbildschirm gezogen werden.
 *
 * @author Johannes Bade & Thomas Gerding
 *
 */
public class GUISidebar implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String KABEL = "gfx/hardware/kabel.png";

	public static final String RECHNER = "gfx/hardware/server.png";

	public static final String SWITCH = "gfx/hardware/switch.png";
	public static final String SWITCH_CLOUD = "gfx/hardware/cloud.png";

	public static final String VERMITTLUNGSRECHNER = "gfx/hardware/router.png";

	public static final String NOTEBOOK = "gfx/hardware/laptop.png";

	public static final String MODEM = "gfx/hardware/vermittlungsrechner-out.png";

	private JBackgroundPanel leistenpanel;

	private List<Serializable> buttonList; // , configItems;

	private JLabel kabel_neu, kabelvorschau;

	private static GUISidebar sidebar;

	/**
	 * @author Johannes Bade & Thomas Gerding
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private GUISidebar() {

		buttonList = new LinkedList<Serializable>();

		leistenpanel = new JBackgroundPanel();

		leistenpanel.setBackgroundImage("gfx/allgemein/leisten_hg.png");
		leistenpanel.setEnabled(false);
		kabelvorschau = GUIContainer.getGUIContainer().getKabelvorschau();

		addCableItemToSidebar();

		addItemsToSidebar();

	}

	public static GUISidebar getGUISidebar() {
		if (sidebar == null) {
			sidebar = new GUISidebar();
		}

		return sidebar;
	}

	public void addCableItemToSidebar() {
		kabel_neu = new JLabel(new ImageIcon(getClass().getResource("/"+KABEL)));
		kabel_neu.setText(Kabel.holeHardwareTyp());
		kabel_neu.setVerticalTextPosition(SwingConstants.BOTTOM);
		kabel_neu.setHorizontalTextPosition(SwingConstants.CENTER);
		kabel_neu.setBounds(5, 5, kabel_neu.getIcon().getIconWidth(), kabel_neu
				.getIcon().getIconHeight()
				+ kabel_neu.getFontMetrics(kabel_neu.getFont()).getHeight());

		kabel_neu.setVerticalTextPosition(SwingConstants.BOTTOM);
		kabel_neu.setHorizontalTextPosition(SwingConstants.CENTER);

		leistenpanel.add(kabel_neu);

		kabel_neu.addMouseListener(new MouseInputAdapter() {
			public void mousePressed(MouseEvent e) {
				/* Wechselt bla */
				kabelvorschau.setBounds(
						e.getX() - kabelvorschau.getWidth() / 2, e.getY()
								- kabelvorschau.getHeight()
								/ 2
								+ GUIContainer.getGUIContainer().getMenu()
										.getMenupanel().getHeight(),
						kabelvorschau.getWidth(), kabelvorschau.getHeight());
				kabelvorschau.setVisible(true);

			}
		});
	}

	/**
	 * Füllt das Sidebar Panel mit Items fuer die Knoten.
	 *
	 * @author Johannes Bade & Thomas Gerding
	 * @param llist
	 */
	public void addItemsToSidebar() {
		String[] bildDateien;
		String[] hardwareTypen;
		JSidebarButton newLabel;
		ImageIcon icon;

		bildDateien = new String[5];
		hardwareTypen = new String[5];
		bildDateien[0] = RECHNER;
		hardwareTypen[0] = Rechner.holeHardwareTyp();
		bildDateien[1] = NOTEBOOK;
		hardwareTypen[1] = Notebook.holeHardwareTyp();
		bildDateien[2] = SWITCH;
		hardwareTypen[2] = Switch.holeHardwareTyp();
		bildDateien[3] = VERMITTLUNGSRECHNER;
		hardwareTypen[3] = Vermittlungsrechner.holeHardwareTyp();
		bildDateien[4] = MODEM;
		hardwareTypen[4] = Modem.holeHardwareTyp();

		int hoehe = kabel_neu.getIcon().getIconHeight();

		for (int i = 0; i < bildDateien.length && i < hardwareTypen.length; i++) {
			icon = new ImageIcon(getClass().getResource("/"+bildDateien[i]));
			newLabel = new JSidebarButton(hardwareTypen[i], icon,
					hardwareTypen[i]);
			newLabel.setBounds(5, 5, icon.getIconWidth(), icon.getIconHeight());

			/* Label wird liste und Leiste hinzugefuegt */
			buttonList.add(newLabel);
			leistenpanel.add(newLabel);
			hoehe += newLabel.getHeight()
					+ newLabel.getFontMetrics(newLabel.getFont()).getHeight()
					+ newLabel.getFontMetrics(newLabel.getFont()).getDescent();
		}

		leistenpanel.setPreferredSize(new Dimension(127, hoehe));

	}

	public JBackgroundPanel getLeistenpanel() {
		return leistenpanel;
	}

	public JSidebarButton aufButton(int x, int y) {
		JSidebarButton tmpLbl = null;
		JSidebarButton klickLabel = null;
		ListIterator it = buttonList.listIterator();
		y += GUIContainer.getGUIContainer().getSidebarScrollpane()
				.getVerticalScrollBar().getValue();
		while (it.hasNext()) {
			tmpLbl = (JSidebarButton) it.next();
			if (x >= tmpLbl.getX() && y >= tmpLbl.getY()
					&& x <= tmpLbl.getX() + tmpLbl.getWidth()
					&& y <= tmpLbl.getY() + tmpLbl.getHeight()) {
				klickLabel = tmpLbl;
			}

		}
		return klickLabel;
	}
}
