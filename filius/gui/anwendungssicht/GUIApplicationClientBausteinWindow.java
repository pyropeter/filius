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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import filius.software.clientserver.ClientBaustein;

/** <p> Diese Klasse stellt die Benutzungsoberflaeche fuer das Client-Programm
 * einer einfachen Client-Server-Anwendung zur Verfuegung. </p>
 * <p> Nachrichten von der Anwendung werden nach dem Beobachtermuster durch
 * die Benachrichtigung der Beobachter angenommen und verarbeitet. </p>
 */
public class GUIApplicationClientBausteinWindow extends GUIApplicationWindow {

	private static final long serialVersionUID = 1L;

	/**
	 * Textfeld fuer die Ausgabe gesendeter und empfangener Nachrichten sowie
	 * fuer Fehlermeldungen
	 */
	private JTextArea taAusgabe;

	/**
	 * Textfeld fuer die Adresse des Servers, zu dem die Verbindung hergestellt
	 * werden soll
	 */
	private JTextField tfServerAdresse;

	/**
	 * Textfeld zur Angabe des TCP-Ports, auf dem der Server auf eingehende
	 * Nachrichten wartet
	 */
	private JTextField tfServerPort;

	/** Textbereich zur Eingabe der Nachrichten */
	private JTextArea taSenden;

	/** Schaltflaeche zum initiieren des Verbindungsaufbaus */
	private JButton btVerbinden;

	/**
	 * Schaltflaeche zum Senden einer zuvor eingegebenen Nachricht
	 */
	private JButton btSenden;

	/**
	 * Standard-Konstruktor, der automatisch zur Erzeugung der graphischen
	 * Benutzungsoberflaeche fuer diese Anwendung aufgerufen wird.
	 */
	public GUIApplicationClientBausteinWindow(GUIDesktopPanel desktop,
			String appName) {
		super(desktop, appName);
		initialisiereKomponenten();
	}

	/** Methode zur Initialisierung der graphischen Komponenten */
	private void initialisiereKomponenten() {
		JPanel hauptPanel;
		JScrollPane scrollPane;
		Box hauptBox;
		Box hBox;
		JLabel label;

		hauptPanel = new JPanel(new BorderLayout());

		hauptBox = Box.createVerticalBox();
		hauptBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		hauptBox.add(Box.createVerticalStrut(5));

		hBox = Box.createHorizontalBox();
		label = new JLabel(messages.getString("clientbaustein_msg1"));
		label.setPreferredSize(new Dimension(100, label.getHeight()));
		hBox.add(label);

		tfServerAdresse = new JTextField();
		tfServerAdresse.setPreferredSize(new Dimension(100, 20));
		hBox.add(tfServerAdresse);
		hauptBox.add(hBox);
		hauptBox.add(Box.createVerticalStrut(5));

		hBox = Box.createHorizontalBox();
		label = new JLabel(messages.getString("clientbaustein_msg2"));
		label.setPreferredSize(new Dimension(100, label.getHeight()));
		hBox.add(label);

		tfServerPort = new JTextField();
		tfServerPort.setPreferredSize(new Dimension(100, 20));
		hBox.add(tfServerPort);
		hauptBox.add(hBox);
		hauptBox.add(Box.createVerticalStrut(5));

		btVerbinden = new JButton(messages.getString("clientbaustein_msg3"));
		btVerbinden.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("verbinden")) {
					((ClientBaustein) holeAnwendung())
							.setZielIPAdresse(tfServerAdresse.getText());
					((ClientBaustein) holeAnwendung()).setZielPort(Integer
							.parseInt(tfServerPort.getText()));
					((ClientBaustein) holeAnwendung()).verbinden();
				}
				else {
					((ClientBaustein) holeAnwendung()).trennen();
				}
				aktualisieren();
			}
		});
		hauptBox.add(btVerbinden);
		hauptBox.add(Box.createVerticalStrut(5));

		label = new JLabel(messages.getString("clientbaustein_msg4"));
		label.setPreferredSize(new Dimension(100, 20));
		hauptBox.add(label);

		taSenden = new JTextArea();
		scrollPane = new JScrollPane(taSenden);
		scrollPane.setPreferredSize(new Dimension(400, 50));
		hauptBox.add(scrollPane);

		btSenden = new JButton(messages.getString("clientbaustein_msg5"));
		btSenden.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((ClientBaustein) holeAnwendung()).senden(taSenden.getText());
				taSenden.setText("");
			}
		});
		hauptBox.add(btSenden);
		hauptBox.add(Box.createVerticalStrut(5));

		taAusgabe = new JTextArea();
		taAusgabe.setEditable(false);
		scrollPane = new JScrollPane(taAusgabe);
		scrollPane.setPreferredSize(new Dimension(400, 200));
		hauptBox.add(scrollPane);
		hauptBox.add(Box.createVerticalStrut(5));

		hauptPanel.add(hauptBox, BorderLayout.CENTER);

		getContentPane().add(hauptPanel);
		pack();

		aktualisieren();
	}

	/**
	 * Methode zum aktualisieren der Komponenten der graphischen
	 * Benutzungsoberflaeche in Abhaengigkeit vom Zustand der Anwendung
	 */
	private void aktualisieren() {
		ClientBaustein client;

		client = (ClientBaustein) holeAnwendung();

		tfServerAdresse.setText(client.getZielIPAdresse());
		tfServerPort.setText("" + client.getZielPort());
		if (client.istVerbunden()) {
			btVerbinden.setText(messages.getString("clientbaustein_msg6"));
			btVerbinden.setActionCommand("trennen");
			btSenden.setEnabled(true);

			tfServerAdresse.setEditable(false);
			tfServerPort.setEditable(false);
		}
		else {
			btVerbinden.setText(messages.getString("clientbaustein_msg3"));
			btVerbinden.setActionCommand("verbinden");
			btSenden.setEnabled(false);

			tfServerAdresse.setEditable(true);
			tfServerPort.setEditable(true);
		}
	}

	/**
	 * Diese Methode wird automatisch ausgefuehrt, wenn eine Nachricht an
	 * den Beobachter der Anwendung gesendet wird. Der Parameter arg enthaelt
	 * die Nachricht, die von der Anwendung verschickt wurde.
	 */
	public void update(Observable o, Object arg) {
		if (arg != null) {
			this.taAusgabe.append(arg.toString() + "\n");
		}

		aktualisieren();
	}
}
