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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import filius.Main;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;

public class PanelVerwaltung extends JPanel implements I18n {

	private static final long serialVersionUID = 1L;

	private ButtonGroup buttonGroupModus, buttonGroupAnwendungstyp;

	private JButton buttonLoeschen, buttonSpeichern;

	private JTable tabelle;

	private JTextField anwendungsName, klassenName;

	public PanelVerwaltung() {
		super();

		setPreferredSize(new Dimension(700, 460));
		setMaximumSize(new Dimension(700, 460));

		initKomponenten();
	}

	public String[] holeAnwendung() {
		String[] array;

		array = new String[2];

		if (buttonGroupModus.getSelection().getActionCommand().equals("neu")) {
			array[0] = anwendungsName.getText().trim();
			array[1] = klassenName.getText().trim();

			if (!array[0].equals("") && !array[1].equals("")) {
				return array;
			}
			else {
				return null;
			}
		}
		else {
			if (tabelle.getSelectedRow() == -1) {
				return null;
			}
			else {
				array[0] = (String) tabelle.getValueAt(
						tabelle.getSelectedRow(), 0);
				array[1] = (String) tabelle.getValueAt(
						tabelle.getSelectedRow(), 1);
			}

			if (!array[0].equals("") && !array[1].equals("")) {
				array[1] = array[1].substring(array[1].lastIndexOf(".")+1);
				return array;
			}
			else {
				return null;
			}
		}
	}

	public String[] initQuelltextDateien() {
		String quelltext = "";
		String[] dateien = null;
		String klasse;
		File file;
		String fs;

		fs = System.getProperty("file.separator");

		if (buttonGroupModus.getSelection().getActionCommand().equals("neu")) {
			klasse = klassenName.getText();

			if (buttonGroupAnwendungstyp.getSelection().getActionCommand()
					.equals("client")) {
				dateien = new String[2];

				dateien[0] = Information.getInformation()
				.getAnwendungenPfad()
				+ "filius"+fs+"software"+fs+"clientserver"+fs+""
				+ klasse + ".java";
				quelltext = ladeQuelltext(Information.getInformation().getProgrammPfad()
						+ "config"+fs+"quelltext_vorlagen"+fs+"client.txt");
				quelltext = quelltext.replaceAll("ClientBaustein", klasse);
				speicherQuelltext(quelltext, dateien[0]);

				dateien[1] = Information.getInformation()
				.getAnwendungenPfad()
				+ "filius"+fs+"gui"+fs+"anwendungssicht"+fs+"GUIApplication"
				+ klasse + "Window.java";
				quelltext = ladeQuelltext(Information.getInformation().getProgrammPfad()
						+ "config"+fs+"quelltext_vorlagen"+fs+"client_gui.txt");
				quelltext = quelltext.replaceAll("ClientBaustein", klasse);
				speicherQuelltext(quelltext, dateien[1]);
			}
			else {
				dateien = new String[3];

				dateien[0] = Information.getInformation()
				.getAnwendungenPfad()
				+ "filius"+fs+"software"+fs+"clientserver"+fs+""
				+ klasse + "Mitarbeiter.java";
				quelltext = ladeQuelltext(Information.getInformation().getProgrammPfad()
						+ "config"+fs+"quelltext_vorlagen"+fs+"server_mitarbeiter.txt");
				quelltext = quelltext.replaceAll("ServerBaustein", klasse);
				speicherQuelltext(quelltext, dateien[0]);

				dateien[1] = Information.getInformation()
				.getAnwendungenPfad()
				+ "filius"+fs+"software"+fs+"clientserver"+fs+""
				+ klasse + ".java";
				quelltext = ladeQuelltext(Information.getInformation().getProgrammPfad()
						+ "config"+fs+"quelltext_vorlagen"+fs+"server.txt");
				quelltext = quelltext.replaceAll("ServerBaustein", klasse);
				speicherQuelltext(quelltext, dateien[1]);

				dateien[2] = Information.getInformation()
				.getAnwendungenPfad()
				+ "filius"+fs+"gui"+fs+"anwendungssicht"+fs+"GUIApplication"
				+ klasse + "Window.java";
				quelltext = ladeQuelltext(Information.getInformation().getProgrammPfad()
						+ "config"+fs+"quelltext_vorlagen"+fs+"server_gui.txt");
				quelltext = quelltext.replaceAll("ServerBaustein", klasse);
				speicherQuelltext(quelltext, dateien[2]);
			}
		}
		else if (tabelle.getSelectedRow() != -1) {
			klasse = (String) tabelle.getValueAt(
					tabelle.getSelectedRow(), 1);
		klasse = klasse.substring(klasse.lastIndexOf(".")+1);


			file = new File(Information.getInformation()
				.getAnwendungenPfad()
				+ "filius"+fs+"software"+fs+"clientserver"+fs+""
				+ klasse + "Mitarbeiter.java");

			if (file.exists()) {
				dateien = new String[3];
				dateien[0] = Information.getInformation()
				.getAnwendungenPfad()
				+ "filius"+fs+"software"+fs+"clientserver"+fs+""
				+ klasse + "Mitarbeiter.java";

				dateien[1] = Information.getInformation()
				.getAnwendungenPfad()
				+ "filius"+fs+"software"+fs+"clientserver"+fs+""
				+ klasse + ".java";

				dateien[2] = Information.getInformation()
				.getAnwendungenPfad()
				+ "filius"+fs+"gui"+fs+"anwendungssicht"+fs+"GUIApplication"
				+ klasse + "Window.java";
			}
			else {
				dateien = new String[2];

				dateien[0] = Information.getInformation()
				.getAnwendungenPfad()
				+ "filius"+fs+"software"+fs+"clientserver"+fs+""
				+ klasse + ".java";

				dateien[1] = Information.getInformation()
				.getAnwendungenPfad()
				+ "filius"+fs+"gui"+fs+"anwendungssicht"+fs+"GUIApplication"
				+ klasse + "Window.java";
			}
		}

		return dateien;
	}

	private String ladeQuelltext(String datei) {
		String quellText = "";
		RandomAccessFile quellDatei = null;

		try {
			quellDatei = new RandomAccessFile(datei, "r");
			for (String line; (line = quellDatei.readLine()) != null;) {
				quellText += line + "\n";
			}
		}
		catch (Exception e1) {
			e1.printStackTrace(Main.debug);
		}
		finally {
			try {
				quellDatei.close();
			}
			catch (Exception e) {
				e.printStackTrace(Main.debug);
			}
		}
		return quellText;
	}

	/**
	 * Methode zum Speichern aller bearbeiteten bzw. neu erstellten Quelltexte
	 */
	private void speicherQuelltext(String quelltext, String datei) {
		FileWriter writer;
		File file;
		String verzeichnis;

		verzeichnis = datei.substring(0, datei.lastIndexOf(System.getProperty("file.separator")));
		file = new File(verzeichnis);
		file.mkdirs();

		try {
			writer = new FileWriter(datei, false);
			writer.write(quelltext);
			writer.close();
		}
		catch (IOException e2) {
			e2.printStackTrace(Main.debug);
		}
	}

	private void initKomponenten() {
		JRadioButton radioButton;
		JLabel label;
		JScrollPane scrollPane;
		GridBagConstraints gbc;
		GridBagLayout gridBag;
		JPanel panel, tmp;
		JTextField text;

		this.setLayout(new BorderLayout());

		panel = new JPanel();
		panel.setPreferredSize(new Dimension(700, 160));
		panel.setMaximumSize(new Dimension(700, 160));
		panel.setMinimumSize(new Dimension(700, 160));
		gridBag = new GridBagLayout();
		panel.setLayout(gridBag);
		panel.setBorder(new EtchedBorder());

		buttonGroupModus = new ButtonGroup();
		buttonGroupAnwendungstyp = new ButtonGroup();

		radioButton = new JRadioButton();
		radioButton.setText(messages.getString("panelverwaltung_msg1"));
		radioButton.setActionCommand("neu");
		radioButton.setSelected(true);
		radioButton.setAlignmentX(0f);
		buttonGroupModus.add(radioButton);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(5,5,5,5);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gridBag.setConstraints(radioButton, gbc);
		panel.add(radioButton);

		label = new JLabel();
		label.setPreferredSize(new Dimension(10,0));
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(5,5,5,5);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gridBag.setConstraints(label, gbc);
		panel.add(label);

		radioButton = new JRadioButton();
		radioButton.setText(messages.getString("panelverwaltung_msg2"));
		radioButton.setActionCommand("client");
		radioButton.setSelected(true);
		buttonGroupAnwendungstyp.add(radioButton);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.insets = new Insets(5,5,5,5);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gridBag.setConstraints(radioButton, gbc);
		panel.add(radioButton);

		radioButton = new JRadioButton();
		radioButton.setText(messages.getString("panelverwaltung_msg3"));
		radioButton.setActionCommand("server");
		radioButton.setSelected(false);
		buttonGroupAnwendungstyp.add(radioButton);

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridwidth = 2;
		gbc.gridy = 1;
		gbc.insets = new Insets(5,5,5,5);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gridBag.setConstraints(radioButton, gbc);
		panel.add(radioButton);

		label = new JLabel(messages.getString("panelverwaltung_msg4"));
		label.setPreferredSize(new Dimension(150, 20));

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.insets = new Insets(5,5,5,5);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gridBag.setConstraints(label, gbc);
		panel.add(label);

		anwendungsName = new JTextField();
		anwendungsName.setPreferredSize(new Dimension(200, 20));

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridwidth = 2;
		gbc.gridy = 2;
		gbc.insets = new Insets(5,5,5,5);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gridBag.setConstraints(anwendungsName, gbc);
		panel.add(anwendungsName);

		label = new JLabel(messages.getString("panelverwaltung_msg5"));
		label.setPreferredSize(new Dimension(150, 20));

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.insets = new Insets(5,5,5,5);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gridBag.setConstraints(label, gbc);
		panel.add(label);

		text = new JTextField("filius.software.clientserver.");
		text.setEditable(false);
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 3;
		gbc.insets = new Insets(5,5,5,0);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gridBag.setConstraints(text, gbc);
		panel.add(text);

		klassenName = new JTextField();
		klassenName.setPreferredSize(new Dimension(200, 20));

		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 3;
		gbc.insets = new Insets(5,5,0,5);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gridBag.setConstraints(klassenName, gbc);
		panel.add(klassenName);

		this.add(panel, BorderLayout.NORTH);

		label = new JLabel();
		label.setPreferredSize(new Dimension(680,0));
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.gridwidth = 5;

		gridBag.setConstraints(label, gbc);
		panel.add(label);

		panel = new JPanel();
		panel.setPreferredSize(new Dimension(700, 300));
		panel.setMaximumSize(new Dimension(700, 300));
		panel.setMinimumSize(new Dimension(700, 300));
		gridBag = new GridBagLayout();
		panel.setLayout(gridBag);
		panel.setBorder(new EtchedBorder());

		radioButton = new JRadioButton();
		radioButton.setText(messages.getString("panelverwaltung_msg7"));
		radioButton.setActionCommand("modifizieren");
		radioButton.setSelected(false);
		radioButton.setAlignmentX(0f);
		buttonGroupModus.add(radioButton);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.gridy = 4;
		gbc.insets = new Insets(5,5,5,5);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gridBag.setConstraints(radioButton, gbc);
		panel.add(radioButton);

		label = new JLabel();
		label.setPreferredSize(new Dimension(10,0));
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(5,5,5,5);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gridBag.setConstraints(label, gbc);
		panel.add(label);

		initTabelle();

		scrollPane = new JScrollPane(tabelle);
		scrollPane.setPreferredSize(new Dimension(620, 180));

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridwidth = 3;
		gbc.gridy = 5;
		gbc.insets = new Insets(5,5,5,5);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gridBag.setConstraints(scrollPane, gbc);
		panel.add(scrollPane);

		tmp = new JPanel();
		tmp.setLayout(new FlowLayout());

		buttonLoeschen = new JButton(messages.getString("panelverwaltung_msg8"));
		buttonLoeschen.setPreferredSize(new Dimension(200, 30));
		buttonLoeschen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (tabelle.getSelectedRow() != -1) {
					((DefaultTableModel) tabelle.getModel()).removeRow(tabelle
							.getSelectedRow());
					tabelleSpeichern();
					updateTabelle();
				}
			}
		});
		tmp.add(buttonLoeschen);

		buttonSpeichern = new JButton(messages.getString("panelverwaltung_msg9"));
		buttonSpeichern.setPreferredSize(new Dimension(200, 30));
		buttonSpeichern.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				tabelleSpeichern();
				updateTabelle();
			}
		});
		tmp.add(buttonSpeichern);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridwidth = 4;
		gbc.gridy = 6;
		gbc.insets = new Insets(5,5,5,5);
		gbc.anchor = GridBagConstraints.CENTER;

		gridBag.setConstraints(tmp, gbc);
		panel.add(tmp);

		label = new JLabel();
		label.setPreferredSize(new Dimension(680,0));
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.gridwidth = 6;

		gridBag.setConstraints(label, gbc);
		panel.add(label);

		this.add(panel, BorderLayout.SOUTH);

		updateTabelle();
	}

	private void tabelleSpeichern() {
		FileWriter fw = null;
		String[] zeile;
		DefaultTableModel dtm;
		String dateiname;

		dtm = (DefaultTableModel) tabelle.getModel();

		dateiname = Information.getInformation().getAnwendungenPfad()
				+ "EigeneAnwendungen.txt";

		try {
			fw = new FileWriter(dateiname, false);

			for (int i = 0; i < dtm.getRowCount(); i++) {
				zeile = new String[4];
				for (int j = 0; j < zeile.length; j++) {
					zeile[j] = (String) dtm.getValueAt(i, j);
				}

				fw.write(zeile[0] + ";" + zeile[1] + ";" + zeile[2] + ";"
						+ zeile[3] + "\n");
			}

		}
		catch (IOException e) {
			e.printStackTrace(Main.debug);
		}
		finally {
			if (fw != null) try {
				fw.close();
			}
			catch (IOException e) {}
		}
	}

	private void initTabelle() {
		DefaultTableModel model;
		TableColumnModel tcm;

		model = new DefaultTableModel(0, 4);
		tabelle = new AnwendungsTabelle(model);
		tabelle.setDragEnabled(false);
		tabelle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabelle.setIntercellSpacing(new Dimension(10, 10));
		tabelle.setRowHeight(30);
		tabelle.setShowGrid(false);
		tabelle.setFillsViewportHeight(true);
		tabelle.setBackground(Color.WHITE);
		tabelle.setShowHorizontalLines(true);

		tcm = tabelle.getColumnModel();
		tcm.getColumn(0).setHeaderValue(messages.getString("panelverwaltung_msg10"));
		tcm.getColumn(0).setPreferredWidth(100);
		tcm.getColumn(1).setHeaderValue(messages.getString("panelverwaltung_msg11"));
		tcm.getColumn(1).setPreferredWidth(100);
		tcm.getColumn(2).setHeaderValue(messages.getString("panelverwaltung_msg12"));
		tcm.getColumn(2).setPreferredWidth(100);
		tcm.getColumn(3).setHeaderValue(messages.getString("panelverwaltung_msg13"));
		tcm.getColumn(3).setPreferredWidth(100);
	}

	private void updateTabelle() {
		HashMap map;
		ListIterator it;
		Vector<String> zeile;
		DefaultTableModel dtm;

		dtm = (DefaultTableModel) tabelle.getModel();
		dtm.setRowCount(0);
		try {
			it = Information.getInformation().ladePersoenlicheProgrammListe().listIterator();

			while (it.hasNext()) {
				map = (HashMap) it.next();

				zeile = new Vector<String>();
				zeile.addElement((String)map.get("Anwendung"));
				zeile.addElement((String)map.get("Klasse"));
				zeile.addElement((String)map.get("GUI-Klasse"));
				zeile.addElement((String)map.get("gfxFile"));

				dtm.addRow(zeile);
			}
		}
		catch (IOException e) {
			e.printStackTrace(Main.debug);
		}

		tabelle.updateUI();
	}
}
