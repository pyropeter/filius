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

import filius.software.clientserver.ServerAnwendung;
import filius.software.clientserver.ServerBaustein;

/**
 * <p>
 * Diese Klasse stellt die Benutzungsoberflaeche fuer das Server-Programm einer
 * einfachen Client-Server-Anwendung zur Verfuegung.
 * </p>
 * <p>
 * Nachrichten von der Anwendung werden nach dem Beobachtermuster durch die
 * Benachrichtigung der Beobachter angenommen und verarbeitet.
 * </p>
 */
public class GUIApplicationServerBausteinWindow extends GUIApplicationWindow {

	private static final long serialVersionUID = 1L;

	/**
	 * Textbereich zur Ausgabe von Log-Daten, die vom Server-Programm erzeugt
	 * und durch Benachrichtigung der Beobachter an die graphische
	 * Benutzungsoberflaeche weiter gegeben werden
	 */
	private JTextArea taLog;

	/**
	 * Schaltflaeche zum Starten bzw. Stoppen des Server-Programms, d. h. zum
	 * Starten bzw. Beenden der Verbindungsannahme durch den Server
	 */
	private JButton bStartStop;

	/**
	 * Textfeld zur Eingabe des Ports, an dem der Server eingehende
	 * Verbindungsanfragen entgegen nimmt.
	 */
	private JTextField tfPort;

	/**
	 * Standard-Konstruktor, der automatisch zur Erzeugung der graphischen
	 * Benutzungsoberflaeche fuer diese Anwendung aufgerufen wird.
	 */
	public GUIApplicationServerBausteinWindow(GUIDesktopPanel desktop,
			String appName) {
		super(desktop, appName);
		initialisiereKomponenten();
	}

	/** Methode zur Initialisierung der graphischen Komponenten */
	private void initialisiereKomponenten() {
		JPanel hauptPanel;
		JScrollPane spLogScroller;
		JLabel lbPort;
		Box hBox, vBox;

		hauptPanel = new JPanel(new BorderLayout());
		vBox = Box.createVerticalBox();
		vBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		vBox.add(Box.createVerticalStrut(5));

		hBox = Box.createHorizontalBox();

		bStartStop = new JButton();
		bStartStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("start")) {
					((ServerAnwendung) holeAnwendung()).setPort(Integer
							.parseInt(tfPort.getText()));
					((ServerAnwendung) holeAnwendung()).setAktiv(true);
					((ServerAnwendung) holeAnwendung()).setPort(Integer
							.parseInt(tfPort.getText()));
				}
				else {
					((ServerAnwendung) holeAnwendung()).setAktiv(false);
				}
				aktualisieren();
			}
		});
		hBox.add(bStartStop);
		hBox.add(Box.createHorizontalStrut(5));

		lbPort = new JLabel(messages.getString("serverbaustein_msg1"));
		hBox.add(lbPort);
		tfPort = new JTextField(""
				+ ((ServerAnwendung) holeAnwendung()).getPort());
		hBox.add(tfPort);

		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(5));

		taLog = new JTextArea();
		taLog.setEditable(false);
		spLogScroller = new JScrollPane(taLog);
		spLogScroller.setPreferredSize(new Dimension(400, 400));
		vBox.add(spLogScroller);
		vBox.add(Box.createVerticalStrut(5));

		hauptPanel.add(vBox, BorderLayout.CENTER);

		getContentPane().add(hauptPanel);
		pack();

		aktualisieren();
	}

	/**
	 * Methode zum aktualisieren der Komponenten der graphischen
	 * Benutzungsoberflaeche in Abhaengigkeit vom Zustand der Anwendung
	 */
	public void aktualisieren() {
		ServerBaustein server;

		server = (ServerBaustein) holeAnwendung();
		if (server.isAktiv()) {
			tfPort.setEditable(false);
			bStartStop.setText(messages.getString("serverbaustein_msg2"));
			bStartStop.setActionCommand("stop");
		}
		else {
			bStartStop.setText(messages.getString("serverbaustein_msg3"));
			bStartStop.setActionCommand("start");
			tfPort.setEditable(true);
		}
	}

	/**
	 * Diese Methode wird automatisch ausgefuehrt, wenn der eine Nachricht an
	 * den Beobachter der Anwendung gesendet wird. Der Parameter arg enthaelt
	 * die Nachricht, die von der Anwendung verschickt wurde.
	 */
	public void update(Observable o, Object arg) {
		taLog.append(arg.toString() + "\n");
		aktualisieren();
	}
}
