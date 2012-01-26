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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import filius.Main;
import filius.exception.CreateAccountException;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.software.email.EmailKonto;
import filius.software.email.EmailServer;

/**
 * Diese Klasse stellt das Konfigurationsfenster fr den E-Mail Server dar. Damit
 * knnen Konten auf dem Server angelegt und entfernt werden.
 *
 * @author Thomas Gerding & Johannes Bade
 *
 */
public class GUIApplicationEmailServerWindow extends GUIApplicationWindow {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private JPanel listPanel, formPanel, backPanel, logPanel;

	private JScrollPane scrolly, sPane;

	private JTabbedPane tabby;

	private JButton addButton, deleteButton, startStopButton;

	private JTextField benutzernameField, domainField;

	private JLabel benutzernameLabel, nameLabel, passwortLabel, domainLabel;

	private JTable kontenListenTabelle;

	private int markierteZeile;

	private JTextArea logArea;

	private JPasswordField passwortField;

	public GUIApplicationEmailServerWindow(final GUIDesktopPanel desktop,
			String appName) {
		super(desktop, appName);
		((EmailServer) holeAnwendung()).kontenLaden();

		initialisiereKomponenten();
	}

	private void initialisiereKomponenten() {
		startStopButton = new JButton(messages.getString("emailserver_msg1"));
		startStopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (startStopButton.getText().equals(messages.getString("emailserver_msg1"))) {
					((EmailServer) holeAnwendung()).setAktiv(true);
					updateLog(messages.getString("emailserver_msg2"));
				}
				else {
					((EmailServer) holeAnwendung()).setAktiv(false);
					updateLog(messages.getString("emailserver_msg3"));
				}
				aktualisiere();
			}
		});

		domainLabel = new JLabel(messages.getString("emailserver_msg4"));
		domainField = new JTextField();
		domainField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				((EmailServer) holeAnwendung()).setMailDomain(domainField
						.getText());
				aktualisiere();
			}
		});
		domainField.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent arg0) {
				((EmailServer) holeAnwendung()).setMailDomain(domainField
						.getText());
				aktualisiere();
			}

			public void focusGained(FocusEvent arg0) {}
		});


		Box obenBox = Box.createHorizontalBox();
		obenBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		obenBox.add(startStopButton);
		obenBox.add(Box.createHorizontalStrut(10));
		obenBox.add(domainLabel);
		obenBox.add(Box.createHorizontalStrut(3));
		obenBox.add(domainField);
		obenBox.add(Box.createHorizontalStrut(3));

		tabby = new JTabbedPane();
		tabby.setTabPlacement(JTabbedPane.LEFT);
		/*
		 * Dieser Button entfernt ein bestehendes Konto, wenn in der
		 * Sicherheitsabfrage YES ausgewaehlt wurde Danach wird die Tabelle neu
		 * erstellt, damit die Aenderung auch sichtbar wird.
		 */
		deleteButton = new JButton(messages.getString("emailserver_msg8"));
		deleteButton.addMouseListener(new MouseInputAdapter() {

			public void mousePressed(MouseEvent e) {
				{

					int Auswahl = showOptionDialog(messages.getString("emailserver_msg11")
							+ ((EmailKonto) ((EmailServer) holeAnwendung())
									.getListeBenutzerkonten().get(
											markierteZeile)).getBenutzername()
									.toString() + messages.getString("emailserver_msg12"),
							messages.getString("emailserver_msg13"),
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, null,
							null, null);

					if (Auswahl == JOptionPane.YES_OPTION) {
						((EmailServer) holeAnwendung())
								.getListeBenutzerkonten()
								.remove(markierteZeile);
						updatekontenListenTabelle();
					}
				}
			}
		});

		DefaultTableModel kontenListenTabelleModell = new DefaultTableModel(0,
				2);
		kontenListenTabelle = new JTableEditable(kontenListenTabelleModell, false);
		kontenListenTabelle.setDragEnabled(false);
		kontenListenTabelle.setIntercellSpacing(new Dimension(5,5));
		kontenListenTabelle.setRowHeight(30);
		kontenListenTabelle.setShowGrid(false);
		kontenListenTabelle.setFillsViewportHeight(true);
		kontenListenTabelle.setBackground(Color.WHITE);
		kontenListenTabelle.setShowHorizontalLines(true);


		TableColumnModel tcm = kontenListenTabelle.getColumnModel();
		tcm = kontenListenTabelle.getColumnModel();
		tcm.getColumn(0).setHeaderValue(messages.getString("emailserver_msg14"));
		tcm.getColumn(1).setHeaderValue(messages.getString("emailserver_msg15"));

		scrolly = new JScrollPane(kontenListenTabelle);
		scrolly.setPreferredSize(new Dimension(200, 300));

		Box listenBox = Box.createHorizontalBox();
		listenBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		listenBox.add(scrolly);

		Box buttonBox = Box.createHorizontalBox();
		buttonBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonBox.add(deleteButton);

		listPanel = new JPanel(new BorderLayout());
		listPanel.add(listenBox, BorderLayout.CENTER);
		listPanel.add(buttonBox, BorderLayout.SOUTH);

		benutzernameLabel = new JLabel(messages.getString("emailserver_msg16"));
		benutzernameLabel.setPreferredSize(new Dimension(120, 25));
		nameLabel = new JLabel(messages.getString("emailserver_msg17"));
		nameLabel.setPreferredSize(new Dimension(120, 25));
		passwortLabel = new JLabel(messages.getString("emailserver_msg18"));
		passwortLabel.setPreferredSize(new Dimension(120, 25));

		benutzernameField = new JTextField();
		benutzernameField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				gueltigkeitPruefen(benutzernameField,
						EingabenUeberpruefung.musterEmailBenutzername);
			}

		});
		passwortField = new JPasswordField();

		Box benutzerBox = Box.createHorizontalBox();
		benutzerBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		benutzerBox.add(benutzernameLabel);
		benutzerBox.add(Box.createHorizontalStrut(2));
		benutzerBox.add(benutzernameField);
		benutzerBox.setPreferredSize(new Dimension(250, 20));

		Box passwortBox = Box.createHorizontalBox();
		passwortBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		passwortBox.add(passwortLabel);
		passwortBox.add(Box.createHorizontalStrut(2));
		passwortBox.add(passwortField);
		passwortBox.setPreferredSize(new Dimension(250, 20));

		Box addButtonBox = Box.createHorizontalBox();
		addButton = new JButton(messages.getString("emailserver_msg19"));
		addButton.setPreferredSize(new Dimension(120, 25));
		addButton.addMouseListener(new MouseInputAdapter() {
			public void mousePressed(MouseEvent e) {
				{

					if (!benutzernameField.getText().equals("")
							&& !(new String(passwortField.getPassword()))
									.equals("")) {
						if (EingabenUeberpruefung.isGueltig(benutzernameField
								.getText(),
								EingabenUeberpruefung.musterKeineLeerzeichen)) {
							try {
								((EmailServer) holeAnwendung())
										.benutzerHinzufuegen(benutzernameField
												.getText(), (new String(
												passwortField.getPassword())),
												messages.getString("emailserver_msg20"), messages.getString("emailserver_msg21"));
								showMessageDialog(messages.getString("emailserver_msg22")
										+" "+ benutzernameField.getText()
										+" "+ messages.getString("emailserver_msg23"));
								benutzernameField.setText("");
								passwortField.setText("");
							}
							catch (CreateAccountException e1) {
								e1.printStackTrace(Main.debug);
							}
						}
						else {
							showMessageDialog(messages.getString("emailserver_msg24"));
						}

					}
					else {
						showMessageDialog(messages.getString("emailserver_msg25"));
					}
				}
			}
		});
		addButtonBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		addButtonBox.add(addButton);

		Box formBox = Box.createVerticalBox();
		formBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		formBox.add(benutzerBox);

		formBox.add(passwortBox);
		formBox.add(addButtonBox);

		Box dummyBox = Box.createHorizontalBox();
		dummyBox.setPreferredSize(new Dimension(1, 100));
		formBox.add(dummyBox);

		formPanel = new JPanel(new BorderLayout());
		formPanel.add(formBox, BorderLayout.NORTH);

		logArea = new JTextArea();
		sPane = new JScrollPane(logArea);

		Box logBox = Box.createHorizontalBox();
		logBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		logBox.add(sPane);

		logPanel = new JPanel(new BorderLayout());
		logPanel.add(logBox, BorderLayout.CENTER);

		backPanel = new JPanel(new BorderLayout());

		tabby.addTab(messages.getString("emailserver_msg26"), formPanel);
		tabby.addTab(messages.getString("emailserver_msg27"), listPanel);
		tabby.addTab(messages.getString("emailserver_msg28"), logPanel);
		backPanel.add(obenBox, BorderLayout.NORTH);
		backPanel.add(tabby, BorderLayout.CENTER);
		this.getContentPane().add(backPanel);

		pack();
		aktualisiere();
	}

	private void aktualisiere() {
		EmailServer server;

		server = (EmailServer) holeAnwendung();
		if (server.isAktiv()) {
			startStopButton.setText(messages.getString("emailserver_msg29"));
			domainField.setEnabled(false);
		}
		else {
			startStopButton.setText(messages.getString("emailserver_msg1"));
			domainField.setEnabled(true);
		}

		domainField.setText(server.getMailDomain());

		updatekontenListenTabelle();
	}

	/**
	 * Durch Beobachterprinzip ausgeloeste Funktion, die die Tabelle mit den
	 * Email Konten des Servers aktualisiert.
	 *
	 * @author Thomas Gerding & Johannes Bade
	 *
	 */
	private void updatekontenListenTabelle() {

		DefaultTableModel tabellenModell = (DefaultTableModel) kontenListenTabelle
				.getModel();
		tabellenModell.setRowCount(0);

		LinkedList tempListe = ((EmailServer) holeAnwendung())
				.getListeBenutzerkonten();

		ListIterator it = tempListe.listIterator();
		while (it.hasNext()) {
			EmailKonto tmpKonto = (EmailKonto) it.next();
			Vector<Comparable> v = new Vector<Comparable>();
			v.add(tmpKonto.getBenutzername() + "@" + ((EmailServer)holeAnwendung()).getMailDomain());
			v.add(tmpKonto.getNachrichten().size());
			tabellenModell.addRow(v);
		}
		kontenListenTabelle.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent lse) {
						markierteZeile = kontenListenTabelle.getSelectedRow();

					}
				});
	}

	/**
	 * Ueberprueft Eingabefelder auf Richtigkeit
	 *
	 * @author Johannes Bade & Thomas Gerding
	 * @param pruefRegel
	 * @param feld
	 */
	private void gueltigkeitPruefen(JTextField feld, Pattern pruefRegel) {
		if (EingabenUeberpruefung.isGueltig(feld.getText(), pruefRegel)) {
			feld.setForeground(EingabenUeberpruefung.farbeRichtig);
			JTextField temp = new JTextField();
			feld.setBorder(temp.getBorder());
		}
		else {
			feld.setForeground(EingabenUeberpruefung.farbeFalsch);
			feld.setBorder(BorderFactory.createLineBorder(
					EingabenUeberpruefung.farbeFalsch, 1));
		}
	}

	public void update(Observable arg0, Object arg1) {
		updatekontenListenTabelle();

		if (arg1 instanceof String) updateLog(arg1);
	}

	private void updateLog(Object arg1) {
		this.logArea.append(arg1.toString() + "\n");

		Main.debug
				.println("GUIApplicationWebServerWindow: update() aufgerufen.");
		logArea.updateUI();
	}
}
