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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import filius.Main;
import filius.gui.JMainFrame;
import filius.hardware.knoten.*;

public class JSidebarButton extends JLabel implements Observer {

	private static final long serialVersionUID = 1L;

	private String hardwareTyp;

	private boolean selektiert = false;

	private boolean modemVerbunden = false;

	public boolean isSelektiert() {
		return selektiert;
	}

	public void setSelektiert(boolean selektiert) {
		this.selektiert = selektiert;
	}

	public String getHardwareTyp() {
		return hardwareTyp;
	}

	public void setHardwareTyp(String hardwareTyp) {
		this.hardwareTyp = hardwareTyp;
	}

	public JSidebarButton() {
		this.setVerticalTextPosition(SwingConstants.BOTTOM);
		this.setHorizontalTextPosition(SwingConstants.CENTER);
	}

	public JSidebarButton(String text, Icon icon, String hardwareTyp) {
		super(text, icon, JLabel.CENTER);

		this.setVerticalTextPosition(SwingConstants.BOTTOM);
		this.setHorizontalTextPosition(SwingConstants.CENTER);
		this.hardwareTyp = hardwareTyp;
	}

	public int getWidth() {
		int width;

		width = this.getFontMetrics(this.getFont()).stringWidth(this.getText());
		width += 15;
		if (this.getIcon() != null && this.getIcon().getIconWidth() > width)
			width = this.getIcon().getIconWidth();

		return width;
	}

	public int getHeight() {
		int height;

		height = this.getFontMetrics(this.getFont()).getHeight();
		if (this.getIcon() != null) {
			height += this.getIcon().getIconHeight();
		}
		else {
//			Main.debug.println("DEBUG ("+this.hashCode()+"), getHeight(): add 80 to height");
//			height+=80;
		}
		height += 10;

//		Main.debug.println("DEBUG ("+this.hashCode()+"), getHeight="+height);
		return height;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;

		if (selektiert) {
			g.setColor(new Color(0, 0, 0));
			Graphics2D g2 = (Graphics2D) g;
			Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_BEVEL, 1, new float[] { 2 }, 0);
			g2.setStroke(stroke);
			g2.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
			g.setColor(new Color(128, 200, 255));
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					0.2f));
			g2.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
		}

		if (modemVerbunden) {
			g2d.setColor(new Color(0, 255, 0));
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.fillOval((this.getWidth() / 2) - 6, (this.getHeight() / 2) - 6,
					12, 12);
		}
	}

	public void update(Observable o, Object arg) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JSidebarButton), update("+o+","+arg+")");

		if (arg != null && arg.equals(Boolean.TRUE)) {
			modemVerbunden = true;
		}
		else if (arg != null && arg.equals(Boolean.FALSE)) {
			modemVerbunden = false;
		}
		else if (arg != null && arg instanceof String) {
			JOptionPane.showMessageDialog(JMainFrame.getJMainFrame(), arg);
		}
		this.updateUI();
	}

}
