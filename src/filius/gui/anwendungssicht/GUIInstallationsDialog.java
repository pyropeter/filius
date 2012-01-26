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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import filius.Main;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.software.Anwendung;
import filius.software.system.InternetKnotenBetriebssystem;

public class GUIInstallationsDialog extends JInternalFrame implements I18n {

	private static final long serialVersionUID = 1L;

	private Container c;

	private JList softwareInstalliert, softwareVerfuegbar;

	private JButton removeButton, addButton, confirmButton;

	private JLabel titleInstalled, titleAvailable;

	private DefaultListModel lmVerfuegbar, lmInstalliert;

	private GUIDesktopPanel dp;

	private LinkedList programme = null;

	public GUIInstallationsDialog(GUIDesktopPanel dp) {
		super();
		c = this.getContentPane();
		this.dp = dp;

		try {
			programme = Information.getInformation().ladeProgrammListe();
		} catch (IOException e) {
			e.printStackTrace(Main.debug);
		}

		initListen();
		initButtons();

		/* Title above lists */
		titleInstalled = new JLabel(messages.getString("installationsdialog_msg3"));
		titleAvailable = new JLabel(messages.getString("installationsdialog_msg4"));

		/* Komponenten dem Panel hinzufügen */
		Box gesamtBox = Box.createVerticalBox();

		Box wrapperInstBox = Box.createVerticalBox();
		Box wrapperAvailBox = Box.createVerticalBox();

		wrapperInstBox.add(titleInstalled);
		wrapperInstBox.add(Box.createVerticalStrut(10));

		Box listenBox = Box.createHorizontalBox();
		listenBox.add(Box.createHorizontalStrut(10));

		JScrollPane ScrollAnwendungInstallieren = new JScrollPane(
				softwareInstalliert);
		ScrollAnwendungInstallieren.setPreferredSize(new Dimension(150, 200));
		wrapperInstBox.add(ScrollAnwendungInstallieren);

		listenBox.add(wrapperInstBox);

		listenBox.add(Box.createHorizontalGlue());

		wrapperAvailBox.add(titleAvailable);
		wrapperAvailBox.add(Box.createVerticalStrut(10));

		JScrollPane scrollAnwendungVerfuegbar = new JScrollPane(
				softwareVerfuegbar);
		ScrollAnwendungInstallieren.setPreferredSize(new Dimension(150, 200));
		wrapperAvailBox.add(scrollAnwendungVerfuegbar);
		listenBox.add(wrapperAvailBox);

		listenBox.add(Box.createHorizontalStrut(10));

		gesamtBox.add(Box.createVerticalStrut(10));
		gesamtBox.add(listenBox);
		gesamtBox.add(Box.createVerticalStrut(10));

		Box topButtonBox = Box.createHorizontalBox();
		topButtonBox.add(addButton);
		topButtonBox.add(Box.createHorizontalStrut(32));
		topButtonBox.add(removeButton);
		gesamtBox.add(topButtonBox);
		gesamtBox.add(Box.createVerticalStrut(10));

		Box bottomButtonBox = Box.createVerticalBox();

		bottomButtonBox.add(confirmButton);
		confirmButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);

		gesamtBox.add(bottomButtonBox);
		gesamtBox.add(Box.createVerticalStrut(10));

		c.add(gesamtBox, BorderLayout.CENTER);
		this.setClosable(true);
		this.setMaximizable(true);
		this.setResizable(true);
		this.setBounds(0, 80, 400, 360);
		this.setTitle(messages.getString("installationsdialog_msg1"));
		this.setVisible(true);
		this.setAnwendungsIcon("gfx/desktop/icon_softwareinstallation.png");
	}

	private GUIDesktopPanel getDesktopPanel() {
		return dp;
	}

	private void hinzufuegen() {
		Vector vLoeschen = new Vector();
		int[] selektiertIndices = softwareVerfuegbar.getSelectedIndices();

		for (int i : selektiertIndices) {
			lmInstalliert.addElement(lmVerfuegbar.get(i));
			vLoeschen.add(lmVerfuegbar.get(i));
		}

		// umständlich, aber wegen der Möglichkeit von Mehrfachselektion lassen sich nicht einzelne Anwendungen sofort entfernen
		for (Enumeration e = vLoeschen.elements(); e.hasMoreElements();) {
			Object oZuLoeschen = e.nextElement();
			lmVerfuegbar.removeElement(oZuLoeschen);
		}
	}

	private void entfernen() {
		int[] selektiertIndices = softwareInstalliert.getSelectedIndices();
		Vector hinzu = new Vector();

		for (int i : selektiertIndices) {
			lmVerfuegbar.addElement(lmInstalliert.getElementAt(i));
			hinzu.add(lmInstalliert.getElementAt(i));
		}

		// umständlich, aber wegen der Möglichkeit von Mehrfachselektion lassen sich nicht einzelne Anwendungen sofort entfernen
		for (Enumeration e = hinzu.elements(); e.hasMoreElements();) {
			Object hinzuObjekt = e.nextElement();
			lmInstalliert.removeElement(hinzuObjekt);
		}
	}

	private void aenderungenSpeichern() {
		InternetKnotenBetriebssystem bs = getDesktopPanel().getBetriebssystem();
		Anwendung anwendung;
		HashMap map;
		ListIterator it;

		it = programme.listIterator();
		while (it.hasNext()) {
			map = (HashMap) it.next();
			for (int i = 0; i < lmInstalliert.getSize(); i++) {
				if (lmInstalliert.getElementAt(i).equals(map.get("Anwendung"))
						&& bs.holeSoftware(map.get("Klasse").toString()) == null) {
					bs.installiereSoftware(map.get("Klasse").toString());

					anwendung = bs.holeSoftware(map.get("Klasse").toString());
					anwendung.starten();
				}
			}

			for (int i = 0; i < lmVerfuegbar.getSize(); i++) {
				if (lmVerfuegbar.getElementAt(i).equals(map.get("Anwendung"))) {
					anwendung = bs.holeSoftware(map.get("Klasse").toString());
					if (anwendung != null) {
						anwendung.beenden();
						bs.entferneSoftware(map.get("Klasse").toString());
					}
				}
			}
		}

		dp.updateAnwendungen();
	}

	private void initButtons() {
		/* ActionListener */
		ActionListener al = new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (arg0.getActionCommand()
						.equals(addButton.getActionCommand())) {
					hinzufuegen();
				} else if (arg0.getActionCommand().equals(
						removeButton.getActionCommand())) {
					entfernen();
				} else if (arg0.getActionCommand() == confirmButton.getText()) {
					aenderungenSpeichern();
					setVisible(false);
				}

			}
		};

		/* Buttons */
		removeButton = new JButton(new ImageIcon(getClass().getResource("/gfx/allgemein/pfeil_rechts.png")));
		removeButton.setActionCommand("remove");
		removeButton.addActionListener(al);

		addButton = new JButton(new ImageIcon(getClass().getResource("/gfx/allgemein/pfeil_links.png")));
		addButton.setActionCommand("add");
		addButton.addActionListener(al);

		confirmButton = new JButton(messages
				.getString("installationsdialog_msg2"));
		confirmButton.addActionListener(al);
	}

	private void initListen() {
		Anwendung[] anwendungen;
		HashMap tmpMap;
		String awKlasse;
		InternetKnotenBetriebssystem bs;

		lmInstalliert = new DefaultListModel();
		lmVerfuegbar = new DefaultListModel();

		bs = dp.getBetriebssystem();

		/* Installierte Anwendung auslesen */
		anwendungen = bs.holeArrayInstallierteSoftware();

		for (int i = 0; i < anwendungen.length; i++) {
			if (anwendungen[i] != null)
				lmInstalliert.addElement(anwendungen[i].holeAnwendungsName());
		}

		if (programme != null) {
			ListIterator it = programme.listIterator();

			while (it.hasNext()) {
				tmpMap = (HashMap) it.next();
				awKlasse = (String) tmpMap.get("Klasse");

				if (dp.getBetriebssystem().holeSoftware(awKlasse) == null) {
					lmVerfuegbar.addElement(tmpMap.get("Anwendung"));
				}
			}
		}

		/* Listen */
		softwareInstalliert = new JList(lmInstalliert);
		softwareVerfuegbar = new JList(lmVerfuegbar);
	}

	public void setAnwendungsIcon(String datei) {
		ImageIcon image = new ImageIcon(getClass().getResource("/"+datei));
		image.setImage(image.getImage().getScaledInstance(16, 16,
				Image.SCALE_AREA_AVERAGING));
		this.setFrameIcon(image);
	}
}
