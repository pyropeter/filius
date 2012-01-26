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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;

import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.Main;

import filius.software.email.EmailAnwendung;
import filius.software.email.Email;
import filius.software.email.EmailKonto;
import filius.software.email.SMTPClient;
import filius.software.email.POP3Client;
import filius.software.system.Datei;

/**
 * Applikationsfenster fr den Email-Client
 *
 * @author Thomas Gerding & Johannes Bade
 *
 */
public class GUIApplicationEmailAnwendungWindow extends GUIApplicationWindow {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private JTabbedPane tabbedPane;

	private JPanel gesendetPanel, eingangPanel, backPanel;

	private JScrollPane gesendetScroll, eingangScroll;

	private JEditorPane emailVorschau;

	private JButton buttonMailsAbholen, buttonMailVerfassen,
			buttonMailAntworten, buttonKonten, buttonEmailLoeschen;

	private Email aktuelleMail = null;

	private JProgressBar progressBar;

	private JInternalFrame inFrVerfassen, inFrAbholen, inFrKonten;

	private Box middleBox;

	private DefaultTableModel posteingangModell = new DefaultTableModel(0, 2);

	private DefaultTableModel gesendeteModell = new DefaultTableModel(0, 2);

	private JTable posteingangTable, gesendeteTable;

	private DefaultListModel lmKonten;

	private JList jlKonten;

	private JTextField tfName, tfEmailAdresse, tfPOP3Server, tfPOP3Port,
			tfSMTPServer, tfSMTPPort, tfBenutzername;

	private JPasswordField tfPasswort;

	private JPanel rechtesKontenPanel;

	private int index, zeilenNummer;

	private boolean kontoMailOK, kontoPOPPortOK, kontoSMTPPortOK,
			kontoBenutzerOK;

	private int auswahlfuerloeschen, paa = -1;

	public GUIApplicationEmailAnwendungWindow(GUIDesktopPanel desktop,
			String appName) {
		super(desktop, appName);

		((EmailAnwendung) holeAnwendung()).holePOP3Client().hinzuBeobachter(
				this);
		initialisiereKomponenten();
	}

	private void initialisiereKomponenten() {
		backPanel = new JPanel(new BorderLayout());

		tabbedPane = new JTabbedPane();

		gesendetPanel = new JPanel(new BorderLayout());

		eingangPanel = new JPanel(new BorderLayout());

		Box gesendetBox = Box.createHorizontalBox();
		gesendetBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		Box eingangBox = Box.createHorizontalBox();
		eingangBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		Box vorschauBox = Box.createHorizontalBox();
		vorschauBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		posteingangTable = new JTableEditable(posteingangModell, false);
		TableColumnModel tcm = posteingangTable.getColumnModel();
		tcm.getColumn(0).setHeaderValue(
				messages.getString("emailanwendung_msg1"));
		tcm.getColumn(1).setHeaderValue(
				messages.getString("emailanwendung_msg2"));
		eingangScroll = new JScrollPane(posteingangTable);

		posteingangTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent lse) {
						zeilenNummer = posteingangTable.getSelectedRow();
						auswahlfuerloeschen = zeilenNummer;
						paa = 0;
						if (zeilenNummer != -1) {
							Email tmpEmail = (Email) ((EmailAnwendung) holeAnwendung())
									.getEmpfangeneNachrichten().get(
											zeilenNummer);
							emailVorschau.setContentType("text/plain");
							emailVorschau.setText(tmpEmail.getText());
							aktuelleMail = tmpEmail;
							emailVorschau.updateUI();
						}
					}
				});

		eingangBox.add(eingangScroll);
		eingangPanel.add(eingangBox, BorderLayout.CENTER);

		gesendeteTable = new JTableEditable(gesendeteModell, false);
		TableColumnModel tcmGesendet = gesendeteTable.getColumnModel();
		tcmGesendet.getColumn(0).setHeaderValue(
				messages.getString("emailanwendung_msg3"));
		tcmGesendet.getColumn(1).setHeaderValue(
				messages.getString("emailanwendung_msg2"));

		gesendeteTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent lse) {
						zeilenNummer = gesendeteTable.getSelectedRow();
						auswahlfuerloeschen = zeilenNummer;
						paa = 1;
						if (zeilenNummer != -1) {
							Email tmpEmail = (Email) ((EmailAnwendung) holeAnwendung())
									.getGesendeteNachrichten()
									.get(zeilenNummer);
							emailVorschau.setContentType("text/plain");
							emailVorschau.setText(tmpEmail.getText());
							aktuelleMail = tmpEmail;
							emailVorschau.updateUI();

						}
					}
				});

		gesendetScroll = new JScrollPane(gesendeteTable);
		gesendetBox.add(gesendetScroll);
		gesendetPanel.add(gesendetBox, BorderLayout.CENTER);

		emailVorschau = new JEditorPane();
		emailVorschau.setBackground(new Color(255, 255, 255));
		emailVorschau.setContentType("text/html");
		emailVorschau.setEditable(false);
		emailVorschau
				.setText("<html><head><base href=\"file:bilder\"></head><body>"
						+ "<img src=\"config/email_icon.png\" align=\"top\">"
						+ "<font face=arial>"
						+ messages.getString("emailanwendung_msg4")
						+ "!<br /></font>" + "</body></html>");
		JScrollPane vorschauScrollPane = new JScrollPane(emailVorschau);
		vorschauScrollPane.setPreferredSize(new Dimension(300,200));
		vorschauBox.add(vorschauScrollPane);

		eingangPanel.add(vorschauBox, BorderLayout.SOUTH);

		tabbedPane.addTab(messages.getString("emailanwendung_msg5"),
				new ImageIcon(getClass().getResource("/gfx/desktop/email_ordner_posteingang.png")),
				eingangPanel);
		tabbedPane.addTab(messages.getString("emailanwendung_msg6"),
				new ImageIcon(getClass().getResource("/gfx/desktop/email_ordner_gesendet.png")),
				gesendetPanel);

		tabbedPane.setTabPlacement(JTabbedPane.LEFT);

		Box topBox = Box.createHorizontalBox();
		ImageIcon image = new ImageIcon(getClass().getResource("/gfx/desktop/email_emails_abholen.png"));
		buttonMailsAbholen = new JButton(image);
		image = new ImageIcon(getClass().getResource("/gfx/desktop/email_emails_abholen.gif"));
		buttonMailsAbholen.setRolloverIcon(image);
		buttonMailsAbholen.setFocusPainted(false);
		buttonMailsAbholen.setActionCommand("Abholen");
		buttonMailsAbholen.setToolTipText(messages.getString("emailanwendung_msg7"));

		topBox.add(buttonMailsAbholen);
		topBox.add(Box.createHorizontalStrut(5)); // Platz zw. urlFeld und
		// senden

		image = new ImageIcon(getClass().getResource("/gfx/desktop/email_email_verfassen.png"));
		buttonMailVerfassen = new JButton(image);
		/* Gif Animation fuer Hover Effekt */
		image = new ImageIcon(getClass().getResource("/gfx/desktop/email_email_verfassen.gif"));
		buttonMailVerfassen.setRolloverIcon(image);
		buttonMailVerfassen.setFocusPainted(false);
		buttonMailVerfassen.setActionCommand("Verfassen");
		buttonMailVerfassen.setToolTipText(messages.getString("emailanwendung_msg8"));
		/* ActionListener */
		ActionListener al = new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (arg0.getActionCommand() == buttonMailVerfassen
						.getActionCommand()) {
					emailVerfassen(null);
				}
				if (arg0.getActionCommand() == buttonMailAntworten
						.getActionCommand()) {
					emailVerfassen(aktuelleMail);
				}

				if (arg0.getActionCommand() == buttonMailsAbholen
						.getActionCommand()) {
					emailsAbholen();
				}

				if (arg0.getActionCommand() == buttonKonten.getActionCommand()) {
					kontoVerwalten();
					kontoAktualisieren();
				}
				if (arg0.getActionCommand() == buttonEmailLoeschen
						.getActionCommand()) {
					emailLoeschen(auswahlfuerloeschen, paa);
				}
			}
		};
		buttonMailVerfassen.addActionListener(al);
		buttonMailsAbholen.addActionListener(al);
		topBox.add(buttonMailVerfassen);
		topBox.add(Box.createHorizontalStrut(5));

		image = new ImageIcon(getClass().getResource("/gfx/desktop/email_email_antworten.png"));
		buttonMailAntworten = new JButton(image);
		image = new ImageIcon(getClass().getResource("/gfx/desktop/email_email_antworten.gif"));
		buttonMailAntworten.setRolloverIcon(image);
		buttonMailAntworten.setFocusPainted(false);
		buttonMailAntworten.addActionListener(al);
		buttonMailAntworten.setActionCommand("antworten");
		buttonMailAntworten.setToolTipText(messages
				.getString("emailanwendung_msg9"));
		topBox.add(buttonMailAntworten);
		topBox.add(Box.createHorizontalStrut(5));

		image = new ImageIcon(getClass().getResource("/gfx/desktop/icon_emailloeschen.png"));
		buttonEmailLoeschen = new JButton(messages
				.getString("emailanwendung_msg43"));
		buttonEmailLoeschen.addActionListener(al);
		buttonEmailLoeschen.setActionCommand("loeschen");
		buttonEmailLoeschen.setToolTipText(messages
				.getString("emailanwendung_msg10"));
		topBox.add(buttonEmailLoeschen);
		topBox.add(Box.createHorizontalStrut(5));

		buttonKonten = new JButton(messages.getString("emailanwendung_msg44"));
		buttonKonten.addActionListener(al);
		buttonKonten.setActionCommand("konten");
		buttonKonten.setToolTipText(messages.getString("emailanwendung_msg11"));
		topBox.add(buttonKonten);
		topBox.add(Box.createHorizontalStrut(5));

		topBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		backPanel.add(tabbedPane, BorderLayout.CENTER);
		backPanel.add(vorschauBox, BorderLayout.SOUTH);
		backPanel.add(topBox, BorderLayout.NORTH);
		this.getContentPane().add(backPanel);
		pack();

		laden();
		posteingangAktualisieren();
		gesendeteAktualisieren();

	}

	/**
	 * loescht eine ausgewaehlte und als Parameter uebergebene Email aus dem
	 * Posteingang oder dem Postausgang des Client. Dazu dient der zweite
	 * Parameter als Abfrage.
	 *
	 * @return boolean
	 */
	public void emailLoeschen(int i, int j /* ob Posteingang- oder ausgang */) {
		if (j == 0) // dann loeschen aus dem Posteingang
		{
//			Main.debug
//					.println("================================= GUIAppl. Client Action command: größe der Liste: "
//							+ ((EmailAnwendung) holeAnwendung())
//									.getEmpfangeneNachrichten().size()
//							+ " ===============");
			((EmailAnwendung) holeAnwendung()).getEmpfangeneNachrichten()
					.remove(i);
			posteingangModell.setRowCount(0);
			zeilenNummer = zeilenNummer - 1;
			posteingangAktualisieren();
			emailVorschau.setText(" ");
			emailVorschau.updateUI();
		}
		if (j == 1) // dann loeschen aus dem Postausgang
		{
			((EmailAnwendung) holeAnwendung()).getEmpfangeneNachrichten()
					.remove(i);
			gesendeteModell.setRowCount(0);
			zeilenNummer = zeilenNummer - 1;
			gesendeteAktualisieren();
			emailVorschau.setText(" ");
			emailVorschau.updateUI();
		} else if (j == -1) {
//			Main.debug
//					.println("============================================GuiAppl. Emailloeschen: Email konnte nicht geloescht werden=======================================");
		}

	}

	// provide more sophisticated and 'real' layout for quoted text
	private String replyLayout(String text) {
		return "> "+text.replaceAll("\\n", "\n> ");
	}
	
	private String extractMailAddress(String mailTo) {
		if(mailTo.indexOf("<")<0)   // no "<...>" form, i.e. no name
			return mailTo.trim();
		else {
			return mailTo.substring(mailTo.indexOf("<")+1, mailTo.indexOf(">")).trim();
		}
	}
	
	private void emailVerfassen(Email antwortAuf) {
		ImageIcon image;
		inFrVerfassen = new JInternalFrame(messages.getString("emailanwendung_msg12"));
		inFrVerfassen.setBounds(100, 50, 512, 384);
		inFrVerfassen.setVisible(true);
		inFrVerfassen.setResizable(true);
		inFrVerfassen.setClosable(true);

		image = new ImageIcon(getClass().getResource("/gfx/desktop/email_email_verfassen_icon.png"));
		image.setImage(image.getImage().getScaledInstance(16, 16,
				Image.SCALE_AREA_AVERAGING));
		inFrVerfassen.setFrameIcon(image);

		addFrame(inFrVerfassen);
		try {
			inFrVerfassen.setSelected(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace(Main.debug);
		}

		JPanel verfassenPanel = new JPanel(new BorderLayout());

		/* Obere Box (Sende Button usw.) */
		Box topBox = Box.createHorizontalBox();
		JButton buttonSenden = new JButton(messages.getString("emailanwendung_msg13"));
		buttonSenden.setActionCommand("senden");
		topBox.add(buttonSenden);
		topBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		verfassenPanel.add(topBox, BorderLayout.NORTH);

		/*
		 * Mittlere Box (enthält das Betreffs- & Nachrichten Feld sowie An,CC
		 * und BCC
		 */
		middleBox = Box.createVerticalBox();

		Box absenderBox = Box.createHorizontalBox();

		Iterator it = ((EmailAnwendung) holeAnwendung()).getKontoListe()
				.values().iterator();
		Vector kontenVector = new Vector();
		while (it.hasNext()) {
			EmailKonto aktuellesKonto = (EmailKonto) it.next();
			kontenVector.addElement(aktuellesKonto.getBenutzername());
		}
		final JComboBox cbAbsender = new JComboBox(kontenVector);
		absenderBox.add(cbAbsender);
		absenderBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		middleBox.add(absenderBox);

		/* Box mit An Feld und dazugehörigem Label */
		Box kleineBox = Box.createHorizontalBox();
		JLabel anLabel = new JLabel(messages.getString("emailanwendung_msg14"));
		anLabel.setPreferredSize(new Dimension(50, 20));
		kleineBox.add(anLabel);
		kleineBox.add(Box.createHorizontalStrut(5));
		final JTextField anField = new JTextField();
		anField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				mailPruefen(anField);
			}
		});
		anField.setBorder(null);
		kleineBox.add(anField);
		middleBox.add(kleineBox);
		middleBox.add(Box.createVerticalStrut(3));

		/* Box mit CC Feld und dazugehörigem Label */
		kleineBox = Box.createHorizontalBox();
		JLabel ccLabel = new JLabel(messages.getString("emailanwendung_msg15"));
		ccLabel.setPreferredSize(new Dimension(50, 20));
		kleineBox.add(ccLabel);
		kleineBox.add(Box.createHorizontalStrut(5));
		final JTextField ccField = new JTextField();
		ccField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				mailPruefen(ccField);
			}

		});
		ccField.setBorder(null);
		kleineBox.add(ccField);
		middleBox.add(kleineBox);
		middleBox.add(Box.createVerticalStrut(3));

		/* Box mit CC Feld und dazugehörigem Label */
		kleineBox = Box.createHorizontalBox();
		JLabel bccLabel = new JLabel(messages.getString("emailanwendung_msg16"));
		bccLabel.setPreferredSize(new Dimension(50, 20));
		kleineBox.add(bccLabel);
		kleineBox.add(Box.createHorizontalStrut(5));
		final JTextField bccField = new JTextField();
		bccField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				mailPruefen(bccField);
			}

		});
		bccField.setBorder(null);
		kleineBox.add(bccField);
		middleBox.add(kleineBox);
		middleBox.add(Box.createVerticalStrut(10));

		/* Box mit Betreffszeile und dazugehörigem Label */
		kleineBox = Box.createHorizontalBox();
		JLabel betreffLabel = new JLabel(messages
				.getString("emailanwendung_msg17"));
		betreffLabel.setPreferredSize(new Dimension(50, 20));
		kleineBox.add(betreffLabel);
		kleineBox.add(Box.createHorizontalStrut(5));
		final JTextField betreffszeile = new JTextField();
		betreffszeile.setBorder(null);
		kleineBox.add(betreffszeile);
		middleBox.add(kleineBox);
		middleBox.add(Box.createVerticalStrut(5));

		final JTextArea inhaltField = new JTextArea();
		inhaltField.setPreferredSize(new Dimension(100, 300));
		middleBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JScrollPane inhaltScrollPane = new JScrollPane(inhaltField);
		middleBox.add(inhaltScrollPane);
		verfassenPanel.add(middleBox);

		if (antwortAuf != null) {
			betreffszeile.setText(messages.getString("emailanwendung_msg18")
					+ " " + antwortAuf.getBetreff());
			if(!(antwortAuf.getAbsender().indexOf("<") >= 0)){
				if(antwortAuf.getAbsender().indexOf("@")>=0) {
					inhaltField.setText("\n\n<" + antwortAuf.getAbsender() + "> "
							+ messages.getString("emailanwendung_msg19") + "\n"
							+ replyLayout(antwortAuf.getText()));
				}
				else {
					inhaltField.setText("\n\n" + antwortAuf.getAbsender() + " "
							+ messages.getString("emailanwendung_msg19") + "\n"
							+ replyLayout(antwortAuf.getText()));
				}
				anField.setText(antwortAuf.getAbsender());
			}
			else if(antwortAuf.getAbsender().substring(0,antwortAuf.getAbsender().indexOf("<")).trim().isEmpty()) {
				inhaltField.setText("\n\n" + antwortAuf.getAbsender() + " "
						+ messages.getString("emailanwendung_msg19") + "\n"
						+ replyLayout(antwortAuf.getText()));
				anField.setText(extractMailAddress(antwortAuf.getAbsender()));
			}
			else {
				inhaltField.setText("\n\n" + antwortAuf.getAbsender().substring(0,antwortAuf.getAbsender().indexOf("<")).trim() + " "
						+ messages.getString("emailanwendung_msg19") + "\n"
						+ replyLayout(antwortAuf.getText()));
				anField.setText(antwortAuf.getAbsender());
			}			
		}

		inFrVerfassen.getContentPane().add(verfassenPanel);

		inhaltField.requestFocus();
		inhaltField.grabFocus();
		inhaltField.setCaretPosition(0);
		
		/* ActionListener fuer Senden Button */
		ActionListener al = new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				Email mail;
				String kontoString;
				EmailKonto versendeKonto;
				boolean eingabeFehler = false;
				String[] adressen;

				mail = new Email();

				kontoString = cbAbsender.getSelectedItem().toString();
				versendeKonto = (EmailKonto) ((EmailAnwendung) holeAnwendung())
						.getKontoListe().get(kontoString);

//				Main.debug.println("kontoString = " + kontoString);
				//Main.debug.println(versendeKonto.toString());

				mail.setAbsender(versendeKonto.getVorname() 
						+ (!versendeKonto.getNachname().isEmpty() ? (" " + versendeKonto.getNachname()) : "") 
						+ " <" 
						+ versendeKonto.getEmailAdresse() + ">");

				if (!mailPruefen(anField)) {
//					anField.setText("");
					eingabeFehler = true;
				} else {
					adressen = anField.getText().split(",");
					for (int i = 0; i < adressen.length; i++) {
						if (!adressen[i].trim().equals(""))
							mail.getEmpfaenger().add(extractMailAddress(adressen[i].trim()));
					}
				}

				if (!mailPruefen(ccField)) {
//					ccField.setText("");
					eingabeFehler = true;
				} else {
					adressen = ccField.getText().split(",");
					for (int i = 0; i < adressen.length; i++) {
						if (!adressen[i].trim().equals(""))
							mail.getCc().add(extractMailAddress(adressen[i].trim()));
					}
				}

				if (!mailPruefen(bccField)) {
//					bccField.setText("");
					eingabeFehler = true;
				} else {
					adressen = bccField.getText().split(",");
					for (int i = 0; i < adressen.length; i++) {
						if (!adressen[i].trim().isEmpty())
							mail.getBcc().add(extractMailAddress(adressen[i].trim()));
					}
				}

				if (eingabeFehler) {
					showMessageDialog(messages
							.getString("emailanwendung_msg20"));
				} else if (mail.getEmpfaenger().size() == 0
						&& mail.getCc().size() == 0
						&& mail.getBcc().size() == 0) {
					showMessageDialog(messages
							.getString("emailanwendung_msg21"));
				} else {
					mail.setBetreff(betreffszeile.getText());
					mail.setText(inhaltField.getText());

					progressBar = new JProgressBar(0, 100);
					progressBar.setValue(0);
					progressBar.setIndeterminate(true);
					progressBar.setStringPainted(true);

					middleBox.add(progressBar);
					middleBox.invalidate();
					middleBox.validate();

					progressBar.setString(messages
							.getString("emailanwendung_msg22"));
					((EmailAnwendung) holeAnwendung()).versendeEmail(
							versendeKonto.getSmtpserver(), mail, versendeKonto
									.getEmailAdresse());
					tabbedPane.setSelectedIndex(1);
				}
			}
		};

		buttonSenden.addActionListener(al);

	}

	public void emailsAbholen() {
		inFrAbholen = new JInternalFrame(messages
				.getString("emailanwendung_msg23"));
		inFrAbholen.setBounds(100, 50, 384, 64);
		inFrAbholen.setVisible(true);
		inFrAbholen.setResizable(true);
		inFrAbholen.setClosable(true);
		addFrame(inFrAbholen);
		try {
			inFrAbholen.setSelected(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace(Main.debug);
		}

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);

		inFrAbholen.getContentPane().add(progressBar);
		inFrAbholen.getContentPane().invalidate();
		inFrAbholen.getContentPane().validate();

		Iterator it = ((EmailAnwendung) holeAnwendung()).getKontoListe()
				.values().iterator();
		while (it.hasNext()) {
			EmailKonto aktuellesKonto = (EmailKonto) it.next();
			progressBar.setString(messages.getString("emailanwendung_msg24")
					+ aktuellesKonto.getEmailAdresse() + ")");
			((EmailAnwendung) holeAnwendung()).emailsAbholenEmails(
					aktuellesKonto.getBenutzername(), aktuellesKonto
							.getPasswort(), aktuellesKonto.getPop3port(),
					aktuellesKonto.getPop3server());
			tabbedPane.setSelectedIndex(0);
		}

	}

	private void posteingangAktualisieren() {
		posteingangModell.setRowCount(0);
		ListIterator mailit = ((EmailAnwendung) holeAnwendung())
				.getEmpfangeneNachrichten().listIterator();
		while (mailit.hasNext()) {
			Email neueMail = (Email) mailit.next();
			Vector v = new Vector();
			String absender = neueMail.getAbsender();
			if(absender.indexOf("<")>=0 && absender.substring(0,absender.indexOf("<")).trim().isEmpty()) {
				v.add(absender.substring(absender.indexOf("<")+1, absender.indexOf(">")));
			}
			else {
				v.add(absender);
			}
			v.add(neueMail.getBetreff());

			posteingangModell.addRow(v);
		}
	}

	public void gesendeteAktualisieren() {
		gesendeteModell.setRowCount(0);
		ListIterator mailit = ((EmailAnwendung) holeAnwendung())
				.getGesendeteNachrichten().listIterator();
		while (mailit.hasNext()) {
			Email neueMail = (Email) mailit.next();
			Vector v = new Vector();
			v.add(neueMail.getEmpfaenger());
			v.add(neueMail.getBetreff());

			gesendeteModell.addRow(v);
		}
	}

	private void kontoAktualisieren() {
		Iterator it;
		EmailKonto konto;

		it = ((EmailAnwendung) holeAnwendung()).getKontoListe().values()
				.iterator();
		if (it.hasNext()) {
			konto = (EmailKonto) it.next();

			tfName.setText(konto.getVorname() + " " + konto.getNachname());
			tfEmailAdresse.setText(konto.getEmailAdresse());
			tfPOP3Server.setText(konto.getPop3server());
			tfPOP3Port.setText(konto.getPop3port());
			tfSMTPServer.setText(konto.getSmtpserver());
			tfSMTPPort.setText(konto.getSmtpport());
			tfBenutzername.setText(konto.getBenutzername());
			tfPasswort.setText(konto.getPasswort());
		}
	}

	private void kontoVerwalten() {
		JLabel label;
		JPanel panel;
		Box vBox, hBox;
		JButton button;
		JScrollPane scroller;

		inFrKonten = new JInternalFrame(messages
				.getString("emailanwendung_msg25"));
		inFrKonten.setBounds(100, 50, 400, 350);
		inFrKonten.setVisible(true);
		inFrKonten.setResizable(true);
		inFrKonten.setClosable(true);
		inFrKonten.getContentPane().setLayout(new BorderLayout());

		vBox = Box.createVerticalBox();

		/* Name */
		label = new JLabel(messages.getString("emailanwendung_msg45"));
		label.setPreferredSize(new Dimension(150, 25));

		tfName = new JTextField();
		tfName.setPreferredSize(new Dimension(150, 25));

		hBox = Box.createHorizontalBox();
		hBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		hBox.add(label);
		hBox.add(tfName);
		vBox.add(hBox);

		/* Email-Adresse */
		label = new JLabel(messages.getString("emailanwendung_msg26"));
		label.setPreferredSize(new Dimension(150, 25));

		tfEmailAdresse = new JTextField();
		tfEmailAdresse.setPreferredSize(new Dimension(150, 25));
		tfEmailAdresse.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				gueltigkeitPruefen(tfEmailAdresse,
						EingabenUeberpruefung.musterEmailAdresse);
			}
		});

		hBox = Box.createHorizontalBox();
		hBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		hBox.add(label);
		hBox.add(tfEmailAdresse);
		vBox.add(hBox);

		/* POP3 Server */
		label = new JLabel(messages.getString("emailanwendung_msg27"));
		label.setPreferredSize(new Dimension(150, 25));

		tfPOP3Server = new JTextField();
		tfPOP3Server.setPreferredSize(new Dimension(150, 25));

		hBox = Box.createHorizontalBox();
		hBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		hBox.add(label);
		hBox.add(tfPOP3Server);
		vBox.add(hBox);

		/* POP3 Port */
		label = new JLabel(messages.getString("emailanwendung_msg28"));
		label.setPreferredSize(new Dimension(150, 25));

		tfPOP3Port = new JTextField();
		tfPOP3Port.setPreferredSize(new Dimension(150, 25));
		tfPOP3Port.setText("110");
		tfPOP3Port.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				gueltigkeitPruefen(tfPOP3Port, EingabenUeberpruefung.musterPort);
			}
		});

		hBox = Box.createHorizontalBox();
		hBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		hBox.add(label);
		hBox.add(tfPOP3Port);
		vBox.add(hBox);

		/* SMTP Server */
		label = new JLabel(messages.getString("emailanwendung_msg29"));
		label.setPreferredSize(new Dimension(150, 25));

		tfSMTPServer = new JTextField();
		tfSMTPServer.setPreferredSize(new Dimension(150, 25));

		hBox = Box.createHorizontalBox();
		hBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		hBox.add(label);
		hBox.add(tfSMTPServer);
		vBox.add(hBox);

		/* SMTP Port */
		label = new JLabel(messages.getString("emailanwendung_msg30"));
		label.setPreferredSize(new Dimension(150, 25));

		tfSMTPPort = new JTextField();
		tfSMTPPort.setPreferredSize(new Dimension(150, 25));
		tfSMTPPort.setText("25");
		tfSMTPPort.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				gueltigkeitPruefen(tfSMTPPort, EingabenUeberpruefung.musterPort);
			}
		});

		hBox = Box.createHorizontalBox();
		hBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		hBox.add(label);
		hBox.add(tfSMTPPort);
		vBox.add(hBox);

		/* Benutzername */
		label = new JLabel(messages.getString("emailanwendung_msg31"));
		label.setPreferredSize(new Dimension(150, 25));

		tfBenutzername = new JTextField();
		tfBenutzername.setPreferredSize(new Dimension(150, 25));
		tfBenutzername.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				gueltigkeitPruefen(tfBenutzername,
						EingabenUeberpruefung.musterEmailBenutzername);
			}
		});

		hBox = Box.createHorizontalBox();
		hBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		hBox.add(label);
		hBox.add(tfBenutzername);
		vBox.add(hBox);

		/* Passwort */
		label = new JLabel(messages.getString("emailanwendung_msg32"));
		label.setPreferredSize(new Dimension(150, 25));

		tfPasswort = new JPasswordField();
		tfPasswort.setPreferredSize(new Dimension(150, 25));

		hBox = Box.createHorizontalBox();
		hBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		hBox.add(label);
		hBox.add(tfPasswort);
		vBox.add(hBox);

		hBox = Box.createHorizontalBox();

		/* Erstellen-Button */
		button = new JButton(messages.getString("emailanwendung_msg33"));
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (kontoSpeichern()) {
					inFrKonten.setVisible(false);
				} else {
					showMessageDialog(messages
							.getString("emailanwendung_msg46"));
				}
			}
		});
		hBox.add(button);
		hBox.add(Box.createHorizontalStrut(5));

		button = new JButton(messages.getString("emailanwendung_msg37"));
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				inFrKonten.setVisible(false);
			}

		});

		hBox.add(button);
		vBox.add(Box.createVerticalStrut(10));
		vBox.add(hBox);

		panel = new JPanel();
		panel.add(vBox);

		scroller = new JScrollPane(panel);

		inFrKonten.getContentPane().add(scroller, BorderLayout.CENTER);
		addFrame(inFrKonten);
		try {
			inFrKonten.setSelected(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace(Main.debug);
		}
	}

	private boolean kontoSpeichern() {
		String[] teilStrings;
		String tmp = "";
		EmailKonto neuesKonto = null;
		Iterator it;

		it = ((EmailAnwendung) holeAnwendung()).getKontoListe().values()
				.iterator();
		if (it.hasNext())
			neuesKonto = (EmailKonto) it.next();

		if (EingabenUeberpruefung.isGueltig(tfPOP3Port.getText(),
				EingabenUeberpruefung.musterPort)
				&& EingabenUeberpruefung.isGueltig(tfSMTPPort.getText(),
						EingabenUeberpruefung.musterPort)
				&& EingabenUeberpruefung.isGueltig(tfBenutzername.getText(),
						EingabenUeberpruefung.musterEmailBenutzername)) {

			if (neuesKonto == null) {
				neuesKonto = new EmailKonto();
			}

			if (tfName.getText().trim().equals("")) {
				neuesKonto.setVorname("");
				neuesKonto.setNachname("");
			} else {
				teilStrings = tfName.getText().split(" ");
				if (teilStrings.length == 1) {
					neuesKonto.setVorname(tfName.getText().trim());
				} else if (teilStrings.length >= 2) {
					neuesKonto.setNachname(teilStrings[teilStrings.length - 1]);
					for (int i = 0; i < teilStrings.length - 1; i++)
						tmp += teilStrings[i] + " ";
					neuesKonto.setVorname(tmp.trim());
				}
			}
			neuesKonto.setBenutzername(tfBenutzername.getText());
			neuesKonto.setPasswort(new String(tfPasswort.getPassword()));
			neuesKonto.setPop3port(tfPOP3Port.getText());
			neuesKonto.setPop3server(tfPOP3Server.getText());
			neuesKonto.setSmtpport(tfSMTPPort.getText());
			neuesKonto.setSmtpserver(tfSMTPServer.getText());
			neuesKonto.setEmailAdresse(tfEmailAdresse.getText());

			((EmailAnwendung) holeAnwendung()).getKontoListe().clear();
			((EmailAnwendung) holeAnwendung()).getKontoListe().put(
					neuesKonto.getBenutzername(), neuesKonto);

			speichern();

			return true;
		} else {
			return false;
		}
	}

	private void speichern() {
		((EmailAnwendung) holeAnwendung()).speichern();
	}

	private void laden() {
		((EmailAnwendung) holeAnwendung()).laden();
	}

	/**
	 * Ueberprueft Eingabefelder auf Richtigkeit
	 *
	 * @author Johannes Bade & Thomas Gerding
	 * @param pruefRegel
	 * @param feld
	 */
	public void gueltigkeitPruefen(JTextField feld, Pattern pruefRegel) {
		if (EingabenUeberpruefung.isGueltig(feld.getText(), pruefRegel)) {
			feld.setForeground(EingabenUeberpruefung.farbeRichtig);
			JTextField temp = new JTextField();
			feld.setBorder(temp.getBorder());

		} else {
			feld.setForeground(EingabenUeberpruefung.farbeFalsch);
			feld.setBorder(BorderFactory.createLineBorder(
					EingabenUeberpruefung.farbeFalsch, 1));

		}

	}

	/**
	 * Funktion die während der Eingabe überprüft ob die bisherige Eingabe einen
	 * korrekten Wert darstellt.
	 *
	 * @author Johannes Bade & Thomas Gerding
	 * @param pruefRegel
	 * @param feld
	 */
	private boolean mailPruefen(JTextField feld) {
		String[] adressen;
		boolean fehler = false;

		if (!feld.getText().trim().equals("")) {
			adressen = feld.getText().split(",");

			for (int i = 0; i < adressen.length; i++) {
				if (!EingabenUeberpruefung.isGueltig(adressen[i].trim(),
						EingabenUeberpruefung.musterEmailAdresse)) {
					fehler = true;
				}
			}
		}

		if (!fehler) {
			feld.setForeground(EingabenUeberpruefung.farbeRichtig);
			feld.setBorder(null);
		} else {
			feld.setForeground(EingabenUeberpruefung.farbeFalsch);
			feld.setBorder(BorderFactory.createLineBorder(
					EingabenUeberpruefung.farbeFalsch, 1));
		}

		return !fehler;
	}

	public void update(Observable arg0, Object arg1) {
		posteingangAktualisieren();
		gesendeteAktualisieren();

		if (arg1 instanceof Exception) {
			showMessageDialog(((Exception) arg1).getMessage());
		}

		//Main.debug.println(arg1);

		if (arg1 == null || arg1.equals("") || arg1 instanceof Exception) {
			if (inFrVerfassen != null) {
				inFrVerfassen.setVisible(false);
				inFrVerfassen = null;
			}

			if (inFrAbholen != null) {
				inFrAbholen.getContentPane().remove(progressBar);
				inFrAbholen.setVisible(false);
				inFrAbholen = null;
			}
		}
	}

}
