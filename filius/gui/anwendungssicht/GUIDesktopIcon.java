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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;

import filius.Main;


public class GUIDesktopIcon extends JLabel implements MouseListener {

	private String anwendungsName;
	private String invokeName;
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;



	public GUIDesktopIcon() {
		super();
		// TODO Auto-generated constructor stub

	}

	public GUIDesktopIcon(Icon arg0, int arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public GUIDesktopIcon(Icon arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
		this.addMouseListener(this);
	}

	public GUIDesktopIcon(String arg0, Icon arg1, int arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public GUIDesktopIcon(String arg0, int arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public GUIDesktopIcon(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public String getAnwendungsName() {
		return anwendungsName;
	}

	public void setAnwendungsName(String anwendungsName) {
		this.anwendungsName = anwendungsName;
	}

	public String getInvokeName() {
		return invokeName;
	}

	public void setInvokeName(String invokeName) {
		this.invokeName = invokeName;
	}

	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent arg0) {

		// TODO Auto-generated method stub
		// FIXME Hardcoded Trash!! (getParent.getParent  & Anwendunginstallieren abfrage)

		GUIDesktopPanel dp = (GUIDesktopPanel) this.getParent().getParent().getParent();

		//Main.debug.println("GUIDesktopIcon: "+getAnwendungsName()+" soll gestartet werden.");

			GUIApplicationWindow tempWindow = dp.starteAnwendung(this.getInvokeName());
			if (tempWindow != null)
			{
				try {
					tempWindow.setSelected(true);
					tempWindow.setVisible(true);
				} catch (PropertyVetoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace(Main.debug);
				}
			}
	}

	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}







}
