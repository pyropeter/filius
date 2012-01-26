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

import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;

public class JBackgroundDesktopPane extends JDesktopPane {

	private static final long serialVersionUID = 1L;
	private ImageIcon backgroundImage = new ImageIcon(getClass().getResource("/gfx/allgemein/menue_hg.png"));
	private boolean repeatBG = true;

	/**
	 * Setzt ein neues Hintergrundbild
	 *
	 * @author Johannes Bade
	 * @param dateiname
	 *            String
	 */
	public void setBackgroundImage(String dateiname) {
		backgroundImage = new ImageIcon(getClass().getResource("/"+dateiname));
	}

	/**
	 * Immer wenn die Komponente gezeichnet wird, malt diese Methode auf der
	 * Komponentenfläche ein vorher bestimmtes Hintergrundbild
	 *
	 * @author Johannes Bade
	 * @param g
	 *            Graphics
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Insets ins = getInsets();
		if (repeatBG) {
			for (int a = 0; a <= this.getHeight()
					/ backgroundImage.getIconHeight(); a++) {
				for (int i = 0; i <= this.getWidth()
						/ backgroundImage.getIconWidth(); i++) {
					backgroundImage.paintIcon(this, g, ins.left + i
							* backgroundImage.getIconWidth(), ins.top + a
							* backgroundImage.getIconHeight());
				}
			}
		}
		else {
			backgroundImage.paintIcon(this, g, ins.left, ins.top);
		}
	}
}
