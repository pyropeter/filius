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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.QuadCurve2D;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DebugGraphics;

import filius.Main;
import filius.gui.GUIContainer;
import filius.hardware.NetzwerkInterface;
import filius.hardware.Port;
import filius.hardware.knoten.InternetKnoten;


/**
 *
 * @author Johannes Bade
 */
public class JCablePanel extends javax.swing.JPanel implements Observer {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private GUIKnotenItem ziel1, ziel2;
	private GUIKabelItem kabelItem;

	private boolean kurven = true;
	private QuadCurve2D currCurve=null;

	private Color kabelFarbe = new Color(64, 64, 64);

	private final Color farbeStandard = new Color(64, 64, 64);

	private final Color farbeBlinken = new Color(0, 255, 64);

	public JCablePanel() {
		super();

		this.setOpaque(false);
	}

	public void updateBounds() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JCablePanel), updateBounds()");
		int x1, x2, y1, y2, t1;
//		Main.debug.println("DEBUG updateBounds, ziel1: ("
//		          + ziel1.getImageLabel().getX()
//		          + "/"
//		          + ziel1.getImageLabel().getY()
//		          + ")  [W="
//		          + ziel1.getImageLabel().getWidth()
//		          + "; H="
//		          + ziel1.getImageLabel().getHeight()
//		          + "]");
//		Main.debug.println("DEBUG updateBounds, ziel2: ("
//		        + ziel2.getImageLabel().getX()
//		        + "/"
//		        + ziel2.getImageLabel().getY()
//		        + ")  [W="
//		        + ziel2.getImageLabel().getWidth()
//		        + "; H="
//		        + ziel2.getImageLabel().getHeight()
//		        + "]");

		// Theoretisch korrekte Positionen
		x1 = (int) (ziel1.getImageLabel().getX());
		x2 = (int) (ziel2.getImageLabel().getX());
		y1 = (int) (ziel1.getImageLabel().getY());
		y2 = (int) (ziel2.getImageLabel().getY());

		x1 = (int) (x1 + (0.5 * ziel1.getImageLabel().getWidth()));
		y1 = (int) (y1 + (0.5 * ziel1.getImageLabel().getHeight()));
		x2 = (int) (x2 + (0.5 * ziel2.getImageLabel().getWidth()));
		y2 = (int) (y2 + (0.5 * ziel2.getImageLabel().getHeight()));

		// Absolut korrekte Positionen (also Sidebar und Menu rausgerechnet)
		if (x1 > x2) {
			t1 = x1;
			x1 = x2;
			x2 = t1;
		}
		if (y1 > y2) {
			t1 = y1;
			y1 = y2;
			y2 = t1;
		}
		setBounds(x1-2, y1-2, x2 - x1 +4, y2 - y1+4);   // add 2 for each direction to take care of linewidth
		Main.debug.println("JCablePanel ("+this.hashCode()+"), bounds: "+x1+"/"+y1+", "+x2+"/"+y2+"  (W:"+(x2-x1)+", H:"+(y2-y1)+")");
	}

	protected void paintComponent(Graphics g) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JCablePanel), paintComponent("+g+")");
		super.paintComponent(g);

		int x1, x2, y1, y2;

		// Theoretisch korrekte Positionen
		x1 = (int) (ziel1.getImageLabel().getX() + (0.5 * ziel1.getImageLabel().getWidth()));
		x2 = (int) (ziel2.getImageLabel().getX() + (0.5 * ziel2.getImageLabel().getWidth()));
		y1 = (int) (ziel1.getImageLabel().getY() + (0.5 * ziel1.getImageLabel().getHeight()));
		y2 = (int) (ziel2.getImageLabel().getY() + (0.5 * ziel2.getImageLabel().getHeight()));

		/* Einfaches Zeichnen */
		g.setColor(kabelFarbe);
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(2));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (isKurven()) {
			int kp1 = (x1 - this.getX() + x2 - this.getX()) / 4;
			if( (x1>x2 && y1>y2) ||
			    (x1<x2 && y1<y2)) kp1 = 3*kp1;   // correct X value of control point for falling lines (upper left to lower right corner)
			int kp2 = (y1 - this.getY() + y2 - this.getY()) / 4;
//			Main.debug.println("\trect: ("+x1+"/"+y1+") -> ("+kp1+"/"+kp2+") -> ("+x2+"/"+y2+"), thisXY: ("+this.getX()+"/"+this.getY()+"), color: "+kabelFarbe);

			QuadCurve2D myCurve = new QuadCurve2D.Double(x1 - this.getX(), y1
					- this.getY(), // Punkt 1
					kp1, kp2, // Kontrollpunkt k
					x2 - this.getX(), y2 - this.getY() // Punkt 2
			);

			// Kurve malen

			g2.draw(myCurve);
			this.currCurve = myCurve;
		} else {
			g2.drawLine(x1 - this.getX(), y1 - this.getY(), x2 - this.getX(),
					y2 - this.getY());
		}
		this.setOpaque(false);
	}

	/*
	 * Method to examine whether the mouse was clicked close to a line representing a cable in filius.
	 * ATTENTION: There are several workarounds necessary to cope with Java's strange boundary handling.
	 * 		Thus, the actual bounds are tested prior to actually use them for determining a point to be
	 * 		inside a bound of a curve or line, respectively.
	 */
	public boolean clicked(int x, int y) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JCablePanel), clicked("+x+","+y+")");
		if(this.getHeight()<20 || this.getWidth()<20) return true;   // very slim panel --> always within collision area (incl. tolerance)

		int deltaX=0;
		int deltaY=0;
		int xFactor=1;  // factor for adjusting the deltaX sign according to orientation of line
		double tmpX=0;
		double tmpY=0;
		boolean invers = false;
		
		if(isKurven()) {
			// test bounds placement and behaviour
			tmpX = this.currCurve.getCtrlX();
			tmpY = this.currCurve.getCtrlY() - 2;
			if(!this.currCurve.contains(tmpX, tmpY)) {  // unexpected behaviour: bound is considered to be above curve
				invers = true;
			}
			if(this.getWidth() < this.getHeight()) {
				deltaX=10;
				if(this.currCurve.getY1() > this.currCurve.getY2()) {
					// line is "falling" from upper left to lower left corner; --> invert deltaX to move rightwards
					xFactor = -1;
				}
			}
			else {
				deltaY=10;
			}
			
			// FIXME: still suboptimal; bounds seem to be strange concepts (probably only are of closed curve considered as "bound")
			QuadCurve2D topCurve = new QuadCurve2D.Double(	this.currCurve.getX1() - (xFactor * deltaX),
															this.currCurve.getY1() + deltaY,
															this.currCurve.getCtrlX() - (xFactor * deltaX),
															this.currCurve.getCtrlY() + deltaY,
															this.currCurve.getX2() - (xFactor * deltaX),
															this.currCurve.getY2() + deltaY );
			QuadCurve2D bottomCurve = new QuadCurve2D.Double(	this.currCurve.getX1() + (xFactor * deltaX),
																this.currCurve.getY1() - deltaY,
																this.currCurve.getCtrlX() + (xFactor * deltaX),
																this.currCurve.getCtrlY() - deltaY,
																this.currCurve.getX2() + (xFactor * deltaX),
																this.currCurve.getY2() - deltaY );
	
	//		Main.debug.println("DEBUG ("+this.hashCode()+") "+getClass()+" (JCablePanel), clicked:\n\tclicked within bounds of topCurve "+
	//				topCurve.getBounds()+": "+
	//				topCurve.contains(x-this.getX(), y-this.getY()));
	//		Main.debug.println("DEBUG ("+this.hashCode()+") "+getClass()+" (JCablePanel), clicked:\n\tclicked within bounds of bottomCurve "+
	//				bottomCurve.getBounds()+": "+
	//				bottomCurve.contains(x-this.getX(), y-this.getY()));
			if(!invers) {
//				Main.debug.println("DEBUG ("+this.hashCode()+") "+getClass()+" (JCablePanel), clicked:\n\tclicked between those bounds ["+(x-this.getX())+"/"+(y-this.getY())+"] (final result): "+
//						(topCurve.contains(x-this.getX(), y-this.getY()) &&
//						 !bottomCurve.contains(x-this.getX(), y-this.getY()))
//						);
				return (topCurve.contains(x-this.getX(), y-this.getY()) && !bottomCurve.contains(x-this.getX(), y-this.getY()));
			}
			else {
//				Main.debug.println("DEBUG ("+this.hashCode()+") "+getClass()+" (JCablePanel), clicked:\n\tclicked between those bounds ["+(x-this.getX())+"/"+(y-this.getY())+"] (final result, INVERSE): "+
//						(topCurve.contains(x-this.getX(), y-this.getY()) &&
//						 !bottomCurve.contains(x-this.getX(), y-this.getY()))
//						);
				return (!topCurve.contains(x-this.getX(), y-this.getY()) && bottomCurve.contains(x-this.getX(), y-this.getY()));
			}
		}
		else {  // cables represented by simple lines
			// FIXME
			return true;
		}
	}
	
	public GUIKnotenItem getZiel1() {
		return ziel1;
	}

	public void setZiel1(GUIKnotenItem ziel1) {
		this.ziel1 = ziel1;
	}

	public GUIKnotenItem getZiel2() {
		return ziel2;
	}

	public void setZiel2(GUIKnotenItem ziel2) {
		this.ziel2 = ziel2;
		updateBounds();
	}

	/**
	 * @author Johannes Bade & Thomas Gerding
	 *
	 * Gibt zurück ob die Kabel als Geraden oder Kurven dargestellt werden.
	 *
	 * @return boolean
	 */
	public boolean isKurven() {
		return kurven;
	}

	/**
	 * @author Johannes Bade & Thomas Gerding
	 *
	 * Gibt an ob die Kabel als Geraden oder Kurven dargestellt werden.
	 *
	 *
	 * @param boolean
	 */
	public void setKurven(boolean kurven) {
		this.kurven = kurven;
	}

	/**
	 * @author Johannes Bade
	 *
	 * Wird genutzt um Kabel blinken zu lassen :)
	 */
	public void update(Observable o, Object arg) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JCablePanel), update("+o+","+arg+")");
		
		if (arg.equals(Boolean.TRUE)) {
			kabelFarbe = farbeBlinken;
			this.setLocation(this.getX() - 1, this.getY());
			this.setLocation(this.getX() + 1, this.getY());

		} else {
			kabelFarbe = farbeStandard;
		}

		updateUI();
	}
}
