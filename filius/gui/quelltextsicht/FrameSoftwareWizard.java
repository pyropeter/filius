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
package filius.gui.quelltextsicht;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import filius.gui.JMainFrame;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.SzenarioVerwaltung;

public class FrameSoftwareWizard extends JDialog implements I18n {

	private static final long serialVersionUID = 1L;

	public static final int VERWALTUNG=1, QUELLTEXT=2, COMPILER=3;

	private int zustand;

	private PanelCompiler pCompiler = null;

	private PanelVerwaltung pVerwaltung = null;

	private PanelQuelltext pQuelltext = null;

	private JLabel untertitel;

	private JButton vorButton, zurueckButton;

	private String anwendungsName, klassenName;

	private String[] quelltextDateien;

	public FrameSoftwareWizard () {
		super(JMainFrame.getJMainFrame(), messages.getString("framesoftwarewizard_msg1"), true);

		Dimension screenSize =
            Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(720, 560);
		this.setLocation(screenSize.width/2 - (this.getWidth()/2),
                 screenSize.height/2 - (this.getHeight()/2));

		initKomponenten();

		SzenarioVerwaltung.getInstance().setzeGeaendert();

		setzeVerwaltung();
	}

	private void initKomponenten() {
		JPanel panel;

		getContentPane().removeAll();

		untertitel = new JLabel();
		untertitel.setBackground(Color.WHITE);
		untertitel.setOpaque(true);
		untertitel.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
		untertitel.setHorizontalAlignment(JLabel.CENTER);
		getContentPane().add(untertitel, BorderLayout.NORTH);

		panel = new JPanel();
		panel.setLayout(new FlowLayout());

		zurueckButton = new JButton(messages.getString("framesoftwarewizard_msg2"));
		zurueckButton.setPreferredSize(new Dimension(150, 30));
		zurueckButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				zurueck();
			}
		});
		panel.add(zurueckButton);

		vorButton = new JButton(messages.getString("framesoftwarewizard_msg3"));
		vorButton.setPreferredSize(new Dimension(150, 30));
		vorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				weiter();
			}
		});
		panel.add(vorButton);

		getContentPane().add(panel, BorderLayout.SOUTH);
	}

	private void setzeVerwaltung() {
		JScrollPane scrollPane;

		if (pVerwaltung == null) {
			pVerwaltung = new PanelVerwaltung();
		}

		scrollPane = new JScrollPane(pVerwaltung);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		this.validate();

		untertitel.setText(messages.getString("framesoftwarewizard_msg4"));
		zurueckButton.setText(messages.getString("framesoftwarewizard_msg5"));
		vorButton.setText(messages.getString("framesoftwarewizard_msg3"));

		zustand = VERWALTUNG;
	}

	private void setzeEditor() {
		if (pQuelltext == null) {
			pQuelltext = new PanelQuelltext();
			if (quelltextDateien.length == 2) {
			pQuelltext.hinzuEditor(klassenName, quelltextDateien[0]);
			pQuelltext.hinzuEditor("GUIApplication"+klassenName+"Window", quelltextDateien[1]);
			}
			else {
				pQuelltext.hinzuEditor(klassenName+"Mitarbeiter", quelltextDateien[0]);
				pQuelltext.hinzuEditor(klassenName, quelltextDateien[1]);
				pQuelltext.hinzuEditor("GUIApplication"+klassenName+"Window", quelltextDateien[2]);

			}

		}

		getContentPane().add(pQuelltext, BorderLayout.CENTER);
		this.validate();

		untertitel.setText(messages.getString("framesoftwarewizard_msg6"));
		zurueckButton.setText(messages.getString("framesoftwarewizard_msg2"));
		vorButton.setText(messages.getString("framesoftwarewizard_msg3"));

		zustand = QUELLTEXT;
	}

	private void setzeCompiler() {
		JScrollPane scrollPane;

		if (pCompiler == null) {
			pCompiler = new PanelCompiler(quelltextDateien, anwendungsName, klassenName);
		}

		scrollPane = new JScrollPane(pCompiler);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		this.validate();

		untertitel.setText(messages.getString("framesoftwarewizard_msg7"));
		zurueckButton.setText(messages.getString("framesoftwarewizard_msg2"));
		vorButton.setText(messages.getString("framesoftwarewizard_msg8"));

		(new Thread(pCompiler)).start();

		zustand = COMPILER;
	}

	private void weiter() {
		String[] array;

		switch (zustand) {
		case VERWALTUNG:
			array = pVerwaltung.holeAnwendung();
			if (array != null) {
				anwendungsName = array[0];
				klassenName = array[1];
				quelltextDateien = pVerwaltung.initQuelltextDateien();

				initKomponenten();
				pQuelltext = null;
				setzeEditor();
			}
			break;
		case QUELLTEXT:
			pQuelltext.speicherQuelltexte();

			initKomponenten();
			pCompiler = null;
			setzeCompiler();
			break;
		case COMPILER:
			pCompiler.speichern();
			setVisible(false);

			break;
		}
	}

	private void zurueck() {

		switch (zustand) {
		case VERWALTUNG:
			setVisible(false);
			break;
		case QUELLTEXT:
			initKomponenten();
			setzeVerwaltung();
			break;
		case COMPILER:
			initKomponenten();
			setzeEditor();
			break;
		}
	}
}
