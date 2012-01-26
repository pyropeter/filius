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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class JMarkerPanel extends JBackgroundPanel {
	   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void paintComponent(Graphics g)
	   {
	    super.paintComponent(g);   
	    g.setColor(new Color(0,0,0));
		Graphics2D g2 = (Graphics2D) g;
		Stroke stroke = new BasicStroke( 1,
				  BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
				  1, new float[]{ 2 }, 0 );
		g2.setStroke(stroke);
	    g2.drawRect(0, 0, this.getWidth()-1, this.getHeight()-1);
	   }
	   


}
 