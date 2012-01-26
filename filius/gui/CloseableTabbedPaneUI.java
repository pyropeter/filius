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
package filius.gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.LinkedList;

import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class CloseableTabbedPaneUI extends BasicTabbedPaneUI {
	
	public static LinkedList button_positionen = new LinkedList();
	
	private final int button_breite = 24;
	
	public CloseableTabbedPaneUI()
	{
		super();
	}
	
protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
		
		return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + button_breite;
		}

protected int calculateTabHeight(int tabPlacement, int tabIndex,
		int fontHeight) {
	return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + 2;
}
	
	protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect){
		super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
		
		
		button_positionen = new LinkedList();
		
		
				if (tabIndex > 0)
				{
					int posX = rects[tabIndex].x+rects[tabIndex].width-button_breite+5;
					int posY = rects[tabIndex].y+5;
					int breite = 12;
					int hoehe = 12;
					Rectangle rechteck = new Rectangle(posX,posY,breite,hoehe);
					button_positionen.add(rechteck);
					
					Graphics2D g2 = (Graphics2D) g;
					
					g2.setColor(new Color(255,0,0));
					g2.setComposite(AlphaComposite.getInstance( AlphaComposite.SRC_OVER ,0.4f ));
					g2.fillRoundRect(posX, posY, breite, hoehe, 3, 3);
					g2.setComposite(AlphaComposite.getInstance( AlphaComposite.SRC_OVER ,1.0f ));
					g2.setColor(new Color(255,255,255));
					
					g2.setStroke(new BasicStroke(2));
					g2.drawLine(posX+3, posY+3, posX+breite-3, posY+hoehe-3);
					g2.drawLine(posX+breite-3, posY+3, posX+3, posY+hoehe-3);
				}
	
			
	
	}

}
