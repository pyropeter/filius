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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import filius.Main;
import filius.rahmenprogramm.Base64;
import filius.rahmenprogramm.Information;
import filius.software.system.Betriebssystem;
import filius.software.system.Datei;

public class GUIApplicationImageViewerWindow extends GUIApplicationWindow{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JPanel backPanel;
	
	public GUIApplicationImageViewerWindow(final GUIDesktopPanel desktop, String appName)
	{
		super(desktop, appName);

		backPanel = new JPanel(new BorderLayout());

		JMenuBar mb = new JMenuBar();

		JMenu menuDatei = new JMenu(messages.getString("imageviewer_msg1"));

		menuDatei.add(new AbstractAction(messages.getString("imageviewer_msg2")) {
			
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent arg0) {
				oeffnen();
			}
	  });

		mb.add(menuDatei);

		this.setJMenuBar(mb);

		this.getContentPane().add(backPanel);
		pack();

	}

	public void oeffnen()
	{
		DMTNFileChooser fc;
		int rueckgabe;
		Datei aktuelleDatei;
		String path;
		ImageIcon image;
		
		fc = new DMTNFileChooser((Betriebssystem)holeAnwendung().getSystemSoftware());
		rueckgabe = fc.openDialog();

		if (rueckgabe == DMTNFileChooser.OK)
		{
			aktuelleDatei = holeAnwendung().getSystemSoftware().getDateisystem().holeDatei(fc.getAktuellerOrdner(), fc.getAktuellerDateiname());
			if (aktuelleDatei != null)
			{
				this.setTitle(aktuelleDatei.getName());
				Base64.decodeToFile(aktuelleDatei.getDateiInhalt(), Information.getInformation().getTempPfad()
						+ aktuelleDatei.getName());

				path = Information.getInformation().getTempPfad() + aktuelleDatei.getName();
				image = new ImageIcon(path);
				JLabel titelgrafik = new JLabel(image);
        		backPanel.add(titelgrafik, BorderLayout.CENTER);
        		backPanel.updateUI();
			}
			else {
				Main.debug.println("ERROR ("+this.hashCode()+"): Fehler beim oeffnen einer Datei: keine Datei ausgewaehlt");
			}

		}
		else {
			Main.debug.println("ERROR ("+this.hashCode()+"): Fehler beim oeffnen einer Datei");
		}
	}

	public void update(Observable arg0, Object arg1) {

	}
}
