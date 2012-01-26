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

import java.awt.Color;
import java.awt.Component;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

// import sun.misc.JavaLangAccess;
/**
 * 
 * Dient dazu in einer JList Ordner und Dateien mit Symbolen darzustellen
 * 
 * @author Hannes
 *
 */
public class OrdnerInhaltListRenderer extends JLabel implements ListCellRenderer{
	
	private ImageIcon dateiIcon, ordnerIcon;
	
	public OrdnerInhaltListRenderer()
	{
		super();
		this.dateiIcon = new ImageIcon(getClass().getResource("/gfx/desktop/datei.png"));
		this.ordnerIcon = new ImageIcon(getClass().getResource("/gfx/desktop/ordner.png"));

	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		// TODO Auto-generated method stub
		
		String [] teile = value.toString().split(";");
		
		String text = value.toString();
		if (teile.length > 0)
		{
			if (teile[0].equals("Datei"))
			{
				setIcon(dateiIcon);
			}
			else
			{
				setIcon(ordnerIcon);
			}
			setText(teile[1]);
		}
		/* Wenn Selektiert: Farbe ändern */
		if (isSelected)
			{
				setBackground(new Color(128,200,255));
				setOpaque(true);
			}
		else
		{
			setOpaque(false);
		}
		/* Wenn Fokus: Border setzen*/
		if (cellHasFocus)
		{
			setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
		}
		else
		{
			setBorder(null);
		}
		/* experimentell: Feste ZellenHoehe basierend auf JLabel Hoehe */
		
		return this;
	}

}
