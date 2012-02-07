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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.QuadCurve2D;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import filius.Main;
import filius.exception.VerbindungsException;
import filius.gui.GUIContainer;
import filius.gui.JMainFrame;
import filius.hardware.Hardware;
import filius.hardware.NetzwerkInterface;
import filius.hardware.Port;
import filius.hardware.Verbindung;
import filius.hardware.Kabel;
import filius.hardware.knoten.InternetKnoten;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.LokalerKnoten;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Switch;
import filius.hardware.knoten.Vermittlungsrechner;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.I18n;
import filius.software.firewall.Firewall;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.system.SwitchFirmware;
import filius.software.system.VermittlungsrechnerBetriebssystem;

public class JVermittlungsrechnerKonfiguration extends JKonfiguration implements
		I18n {

	private static final long serialVersionUID = 1L;

	private JDialog changeBasicSettingsDialog;
	
	private JTextField name; // Name,Name,20,String,editable,Vermittlungsrechner,null

	private JTextField[] ipAdressen;
	private JTextField[] netzmasken;
	private JTextField[] macAdressen;

	private JTextField gateway;
	private JCheckBox rip;

	private JLabel[] verbundeneKomponente;

	private JWeiterleitungsTabelle weiterleitungstabelle;

	private JCheckBox alleEintraegeAnzeigen;

	private JButton[] btnLocal = new JButton[8];
	private JLabel[] lblLocal = new JLabel[8];
	JButton btnAddInterface;

	// highlighted cable in development view
	private Kabel highlightedCable = null;
	
	// cables in BasicSettingsDialog
	JPanel cablePanel;
	class LinePanel extends JPanel {
		Point lineStart = new Point(0, 0);
		Point lineEnd = new Point(0, 0);
		Color lineColor = new Color(64,64,64);

		//width of line
		LinePanel()
		{
		    super();
			this.setOpaque(false);
//			this.setBackground(Color.RED);
			//Main.debug.println("DEBUG: JVermittlungsrechnerkonfiguration, showBasicSettingsDialog, new LinePanel created ("+hashCode()+")");
		}
		
		public void setStartPoint(int x, int y) {
			lineStart = new Point(x,y);
		}
		public void setEndPoint(int x, int y) {
			lineEnd = new Point(x,y);
		}
		public void setColor(Color col) {
			lineColor = col;
		}
		
		public String toString() {
			return "["
			      +"name='"+getName()+"', "
			      +"start=("+lineStart.x+"/"+lineStart.y+"), "
			      +"end=("+lineEnd.x+"/"+lineEnd.y+"), "
			      +"color="+lineColor.toString()+", "
			      +"bounds="+getBounds()
			      +"]";
		}
		//draw and delete line
		public void paintComponent(Graphics g)
		{
			//Main.debug.println("DEBUG: JVermittlungsrechnerkonfiguration, showBasicSettingsDialog, paintComponent LinePanel ("+hashCode()+"); ("+lineStart.x+"/"+lineStart.y+")-("+lineEnd.x+"/"+lineEnd.y+"), color="+lineColor);
			super.paintComponent(g);
			/* Einfaches Zeichnen */
			g.setColor(lineColor);
			//Main.debug.println("DEBUG:  graphics object: "+g.toString());
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(2));
//			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.drawLine(lineStart.x, lineStart.y,
					    lineEnd.x, lineEnd.y);
		}
	}
	private LinkedList<LinePanel> cables;

	protected JVermittlungsrechnerKonfiguration(Hardware hardware) {
		super(hardware);
	}

	public void aenderungenAnnehmen() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JVermittlungsrechnerKonfiguration), aenderungenAnnehmen()");
		ListIterator it;
		Vermittlungsrechner vRechner;
		NetzwerkInterface nic;
		VermittlungsrechnerBetriebssystem bs;

		vRechner = (Vermittlungsrechner) holeHardware();
		bs = (VermittlungsrechnerBetriebssystem) vRechner.getSystemSoftware();

		vRechner.setName(name.getText());
		bs.setStandardGateway(gateway.getText());
		bs.setRip(rip.isSelected());

		it = vRechner.getNetzwerkInterfaces().listIterator();
		for (int i = 0; it.hasNext(); i++) {
			nic = (NetzwerkInterface) it.next();

			if (ueberpruefen(EingabenUeberpruefung.musterIpAdresse,
					ipAdressen[i]))
				nic.setIp(ipAdressen[i].getText());
			else Main.debug.println("ERROR ("+this.hashCode()+"): IP-Adresse ungueltig "
							+ ipAdressen[i].getText());

			if (ueberpruefen(EingabenUeberpruefung.musterSubNetz, netzmasken[i]))
				nic.setSubnetzMaske(netzmasken[i].getText());
			else Main.debug
					.println("ERROR ("+this.hashCode()+"): Netzmaske ungueltig "
							+ netzmasken[i].getText());
		}

		GUIContainer.getGUIContainer().updateViewport();
		updateAttribute();
	}

	/*
	 * wird von der Schaltflaeche "Firewall einrichten" in der GUI aufgerufen
	 */
	private void firewallDialogAnzeigen() {
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
		        + " (JVermittlungsrechnerKonfiguration), firewallDialogAnzeigen()");

		Firewall firewall = ((VermittlungsrechnerBetriebssystem) ((Vermittlungsrechner) holeHardware())
		        .getSystemSoftware()).holeFirewall();

		JFirewallDialog firewallDialog = new JFirewallDialog(firewall, JMainFrame.getJMainFrame());
		firewallDialog.setBounds(100, 100, 520, 340);
		firewallDialog.setName(messages.getString("jvermittlungsrechnerkonfiguration_msg1"));

		firewallDialog.updateAttribute(); 	// muss hier passieren, damit beim
		                                  	// oeffnen immer die aktuellen Werte
											// vorhanden sind!
		firewallDialog.setVisible(true);
	}

	protected void initAttributEingabeBox(Box box) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JVermittlungsrechnerKonfiguration), initAttributEingabeBox("+box+")");
		Vermittlungsrechner vRechner;
		NetzwerkInterface tempNic;
		Knoten tempKnoten;
		LinkedList nicListe;
		ListIterator it;
		Box boxNetzwerkKarten;
		JTabbedPane tpNetzwerkKarten;
		Box vBox;
		Box boxNic;
		Box nicWithButton;
		Box boxIpAdresse;
		Box boxSubnetz;
		Box boxMacAdresse;
		Box boxKomponente;
		KeyAdapter ipAdresseKeyAdapter;
		KeyAdapter netzmaskeKeyAdapter;
		FocusListener focusListener;
		ActionListener actionListener;
		JButton btFirewall;
		JButton btNeuerEintrag;
		JButton btEintragLoeschen;
		Box boxWeiterleitung;
		JButton btTabellenDialog;
		JButton changeBasicSettingsButton;

		JLabel tempLabel;
		Box tempBox;

		actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				aenderungenAnnehmen();
			}
		};

		focusListener = new FocusListener() {

			public void focusGained(FocusEvent arg0) {}

			public void focusLost(FocusEvent arg0) {
				aenderungenAnnehmen();
			}

		};

		this.addFocusListener(focusListener);

		boxNetzwerkKarten = Box.createVerticalBox();
		boxNetzwerkKarten.setPreferredSize(new Dimension(440, 150));
		boxNetzwerkKarten.setAlignmentX(JComponent.LEFT_ALIGNMENT);

		tpNetzwerkKarten = new JTabbedPane();
		boxNetzwerkKarten.add(tpNetzwerkKarten);

		ipAdresseKeyAdapter = new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				JTextField tfQuelle = (JTextField) e.getSource();
				ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfQuelle);
			}
		};

		netzmaskeKeyAdapter = new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				JTextField tfQuelle = (JTextField) e.getSource();
				ueberpruefen(EingabenUeberpruefung.musterSubNetz, tfQuelle);
			}
		};

		vBox = Box.createVerticalBox();

		//	 Attribut Name
		tempBox = Box.createHorizontalBox();
		tempBox.setMaximumSize(new Dimension(400, 40));

		tempLabel = new JLabel(messages
				.getString("jvermittlungsrechnerkonfiguration_msg2"));
		tempLabel.setPreferredSize(new Dimension(140, 10));
		tempLabel.setVisible(true);
		tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		tempBox.add(tempLabel);

		name = new JTextField(messages
				.getString("jvermittlungsrechnerkonfiguration_msg3"));
		name.setPreferredSize(new Dimension(160, 20));
		name.addActionListener(actionListener);
		name.addFocusListener(focusListener);
		tempBox.add(name);

		vBox.add(tempBox);
		vBox.add(Box.createVerticalStrut(5));


//		 Attribut Gateway
		tempBox = Box.createHorizontalBox();
		tempBox.setMaximumSize(new Dimension(400, 40));

		tempLabel = new JLabel(messages
				.getString("jvermittlungsrechnerkonfiguration_msg9"));
		tempLabel.setPreferredSize(new Dimension(140, 10));
		tempLabel.setVisible(true);
		tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		tempBox.add(tempLabel);

		gateway = new JTextField();
		gateway.setPreferredSize(new Dimension(160, 20));
		gateway.addActionListener(actionListener);
		gateway.addFocusListener(focusListener);
		gateway.addKeyListener(ipAdresseKeyAdapter);
		tempBox.add(gateway);

		vBox.add(tempBox);
		vBox.add(Box.createVerticalStrut(5));

		// Attribut rip
		tempBox = Box.createHorizontalBox();
		tempBox.setMaximumSize(new Dimension(400, 20));

		tempLabel = new JLabel("Enable RIP?");
		tempLabel.setPreferredSize(new Dimension(140, 10));
		tempLabel.setVisible(true);
		tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		tempBox.add(tempLabel);
		tempBox.add(Box.createHorizontalStrut(10));

		rip = new JCheckBox();
		rip.addActionListener(actionListener);
		rip.addFocusListener(focusListener);
		tempBox.add(rip);

		vBox.add(tempBox);
		vBox.add(Box.createVerticalStrut(5));

		btFirewall = new JButton(messages
				.getString("jvermittlungsrechnerkonfiguration_msg4"));

		btFirewall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				firewallDialogAnzeigen();
			}
		});
		vBox.add(btFirewall);
		vBox.add(Box.createVerticalStrut(5));

		changeBasicSettingsButton = new JButton(messages
				.getString("jvermittlungsrechnerkonfiguration_msg23"));
		changeBasicSettingsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showBasicSettingsDialog();
			}
		});
		vBox.add(changeBasicSettingsButton);


		// NIC tabs
		tpNetzwerkKarten.addTab(messages.getString("jvermittlungsrechnerkonfiguration_msg17"),
				vBox);

		vRechner = (Vermittlungsrechner) holeHardware();
		nicListe = vRechner.getNetzwerkInterfaces();
		ipAdressen = new JTextField[nicListe.size()];
		netzmasken = new JTextField[nicListe.size()];
		macAdressen = new JTextField[nicListe.size()];
		verbundeneKomponente = new JLabel[nicListe.size()];

		it = nicListe.listIterator();

		for (int i = 0; it.hasNext(); i++) {
			tempNic = (NetzwerkInterface) it.next();

			boxNic = Box.createVerticalBox();

			boxKomponente = Box.createHorizontalBox();
			boxKomponente.setMaximumSize(new Dimension(400, 40));

			tempKnoten = holeVerbundeneKomponente(tempNic);
			if (tempKnoten == null) verbundeneKomponente[i] = new JLabel(
					messages
							.getString("jvermittlungsrechnerkonfiguration_msg5"));
			else verbundeneKomponente[i] = new JLabel(messages
					.getString("jvermittlungsrechnerkonfiguration_msg6")
					+ " " + tempKnoten.getDisplayName().replace("\n",", "));
			verbundeneKomponente[i].setPreferredSize(new Dimension(400, 10));
			boxKomponente.add(verbundeneKomponente[i]);

			// show IP address (editable)
			boxIpAdresse = Box.createHorizontalBox();
			boxIpAdresse.setMaximumSize(new Dimension(400, 40));
			tempLabel = new JLabel(messages
					.getString("jvermittlungsrechnerkonfiguration_msg7"));
			tempLabel.setPreferredSize(new Dimension(120, 10));
			boxIpAdresse.add(tempLabel);

			ipAdressen[i] = new JTextField(tempNic.getIp());
			boxIpAdresse.add(ipAdressen[i]);

			// show netmask (editable)
			boxSubnetz = Box.createHorizontalBox();
			boxSubnetz.setMaximumSize(new Dimension(400, 40));
			tempLabel = new JLabel(messages
					.getString("jvermittlungsrechnerkonfiguration_msg8"));
			tempLabel.setPreferredSize(new Dimension(120, 10));
			boxSubnetz.add(tempLabel);

			netzmasken[i] = new JTextField(tempNic.getSubnetzMaske());
			boxSubnetz.add(netzmasken[i]);

			// show MAC address (not editable)
			boxMacAdresse = Box.createHorizontalBox();
			boxMacAdresse.setMaximumSize(new Dimension(400, 40));
			tempLabel = new JLabel(messages
					.getString("jvermittlungsrechnerkonfiguration_msg18"));
			tempLabel.setPreferredSize(new Dimension(120, 10));
			boxMacAdresse.add(tempLabel);

			macAdressen[i] = new JTextField(tempNic.getMac());
			macAdressen[i].setEnabled(false);
			boxMacAdresse.add(macAdressen[i]);

			boxNic.add(boxKomponente);
			boxNic.add(Box.createVerticalStrut(5));
			boxNic.add(boxIpAdresse);
			boxNic.add(Box.createVerticalStrut(5));
			boxNic.add(boxSubnetz);
			boxNic.add(Box.createVerticalStrut(5));
			boxNic.add(boxMacAdresse);
			
			if(tempKnoten == null) {
				tpNetzwerkKarten.addTab(
						messages.getString("jvermittlungsrechnerkonfiguration_msg10") + (i + 1),
						new ImageIcon(getClass().getResource("/gfx/allgemein/conn_fail.png")),
						boxNic);
			}
			else {
				tpNetzwerkKarten.addTab(
						tempKnoten.getDisplayName().replace("\n",", "),
						new ImageIcon(getClass().getResource("/gfx/allgemein/conn_ok.png")),
						boxNic);
			}
		}

		for (int i = 0; i < ipAdressen.length; i++) {
			ipAdressen[i].addKeyListener(ipAdresseKeyAdapter);
			ipAdressen[i].addActionListener(actionListener);
			ipAdressen[i].addFocusListener(focusListener);

			netzmasken[i].addKeyListener(netzmaskeKeyAdapter);
			netzmasken[i].addActionListener(actionListener);
			netzmasken[i].addFocusListener(focusListener);
		}
		tpNetzwerkKarten.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent arg0) {
				//Main.debug.println("JVermittlungsrechnerKonfiguration, ChangeListener, stateChanged("+arg0+")");
				JTabbedPane pane = (JTabbedPane)arg0.getSource();
		        // Get current tab
		        int sel = pane.getSelectedIndex();
//		        Main.debug.println("\tsource: "+pane+", index="+sel+", getComponentCount="+pane.getComponentCount());
		        if(highlightedCable != null) { highlightedCable.setAktiv(false); }
		        if(sel > 0 && sel < pane.getComponentCount()-1) {
					Verbindung conn = ((NetzwerkInterface) ((Vermittlungsrechner) holeHardware()).getNetzwerkInterfaces().get(sel-1)).getPort().getVerbindung();
			        if(conn != null) { conn.setAktiv(true); highlightedCable = (Kabel) conn; }
		        }

		        weiterleitungstabelle.aenderungenAnnehmen();
			}

		});

		/* Weiterleitungs-Tabelle Router */
		weiterleitungstabelle = new JWeiterleitungsTabelle(this);

		JScrollPane spWeiterleitung = new JScrollPane(weiterleitungstabelle);
		spWeiterleitung.setPreferredSize(new Dimension(300, 120));
		spWeiterleitung.addFocusListener(focusListener);

		tempBox = Box.createHorizontalBox();
		tempBox.setOpaque(false);
		tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		alleEintraegeAnzeigen = new JCheckBox();
		alleEintraegeAnzeigen.setSelected(true);
		alleEintraegeAnzeigen.setText(messages
				.getString("jvermittlungsrechnerkonfiguration_msg11"));
		alleEintraegeAnzeigen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				weiterleitungstabelle
						.setzeAlleEintraegeAnzeigen(alleEintraegeAnzeigen
								.isSelected());
				weiterleitungstabelle.updateAttribute();
			}
		});
		tempBox.add(alleEintraegeAnzeigen, BorderLayout.NORTH);

		btNeuerEintrag = new JButton(messages
				.getString("jvermittlungsrechnerkonfiguration_msg12"));
		btNeuerEintrag.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				weiterleitungstabelle.neuerEintrag();
			}
		});
		tempBox.add(Box.createHorizontalStrut(50));
		tempBox.add(btNeuerEintrag);

		btEintragLoeschen = new JButton(messages
				.getString("jvermittlungsrechnerkonfiguration_msg13"));
		btEintragLoeschen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				weiterleitungstabelle.markiertenEintragLoeschen();
			}
		});
		tempBox.add(Box.createHorizontalStrut(5));
		tempBox.add(btEintragLoeschen);

		btTabellenDialog = new JButton(messages
				.getString("jvermittlungsrechnerkonfiguration_msg14"));
		btTabellenDialog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JDialog tabellenDialog;
				JScrollPane scrollPane;
				JWeiterleitungsTabelle tabelle;

				tabellenDialog = new JDialog(filius.gui.JMainFrame
						.getJMainFrame(), true);
				tabellenDialog.setTitle(messages
						.getString("jvermittlungsrechnerkonfiguration_msg15"));
				tabellenDialog.setSize(600, 400);

				Dimension screenSize = Toolkit.getDefaultToolkit()
						.getScreenSize();
				tabellenDialog.setLocation(screenSize.width / 2 - 300,
						screenSize.height / 2 - 200);
				tabelle = new JWeiterleitungsTabelle(getKonfiguration());
				tabelle.updateAttribute();
				scrollPane = new JScrollPane(tabelle);
				tabellenDialog.getContentPane().add(scrollPane);

				tabellenDialog.setVisible(true);
				weiterleitungstabelle.updateAttribute();
			}
		});
		tempBox.add(Box.createHorizontalStrut(50));
		tempBox.add(btTabellenDialog);

		boxWeiterleitung = Box.createVerticalBox();
		boxWeiterleitung.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		boxWeiterleitung.add(tempBox);
		boxWeiterleitung.add(spWeiterleitung);

		tpNetzwerkKarten.addTab(messages
				.getString("jvermittlungsrechnerkonfiguration_msg15"),
				boxWeiterleitung);

		box.add(boxNetzwerkKarten);

		updateAttribute();

	}

	private void showBasicSettingsDialog() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JVermittlungsrechnerKonfiguration), showBasicSettingsDialog()");
		GUIContainer.getGUIContainer().getProperty().minimieren();
		GUIContainer.getGUIContainer().setProperty(null);
		JScrollPane scrollPane;
		JPanel foreignPanel, localPanel;
		JPanel upperCompound, noteCompound, buttonCompound;
		Box scrollPaneContent;
		
		JButton[] btnRemote = new JButton[8];
		JLabel[] lblRemote = new JLabel[8];
		JLabel lblLocalTitle, lblRemoteTitle;
		
		cables = new LinkedList();
		
		// temporary components for list entries in left and right column
		Box tmpBox;
		JButton tmpButton;
		JLabel tmpLabel;
		
		JButton btnClose;
		JTextArea usageNote;
		Dimension screenSize;
		
		// basic dialog creation and settings
		changeBasicSettingsDialog = new JDialog(filius.gui.JMainFrame.getJMainFrame(), true);
		changeBasicSettingsDialog.setTitle(messages.getString("jvermittlungsrechnerkonfiguration_msg23"));

		// positioning and size
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		changeBasicSettingsDialog.setLocation(screenSize.width / 2 - 300,
				screenSize.height / 2 - 200);
		
		/////////////////////////////////////////////
		// contents
		/////////////////////////////////////////////
		
		// - create assignment area
		upperCompound = new JPanel();
//		upperCompound.setBackground(Color.GREEN);
		upperCompound.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		upperCompound.setLayout(new BoxLayout(upperCompound, BoxLayout.X_AXIS));
		upperCompound.setPreferredSize(new Dimension(700,360));
		
		// -- create left column; connected foreign components
		SpringLayout layoutRemote = new SpringLayout();
		foreignPanel = new JPanel(layoutRemote);
		foreignPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY,2));
		lblRemoteTitle = new JLabel(messages.getString("jvermittlungsrechnerkonfiguration_msg21"));
		foreignPanel.add(lblRemoteTitle);
		foreignPanel.setPreferredSize(new Dimension(230,700));
		foreignPanel.setMaximumSize(foreignPanel.getPreferredSize());
	
		// -- create right column; local NICs and connections
		SpringLayout layoutLocal = new SpringLayout();
		localPanel = new JPanel(layoutLocal);
		localPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY,2));
		lblLocalTitle = new JLabel(messages.getString("jvermittlungsrechnerkonfiguration_msg22"));
		localPanel.add(lblLocalTitle);
		localPanel.setPreferredSize(new Dimension(230,700));
		localPanel.setMaximumSize(localPanel.getPreferredSize());

		btnAddInterface = new JButton(messages.getString("jvermittlungsrechnerkonfiguration_msg24"));

		// --- create connectors in both columns
		ListIterator it = ((Vermittlungsrechner) holeHardware()).getNetzwerkInterfaces().listIterator();
		int nicNr=0;
		NetzwerkInterface nic;
		Knoten node;
		while (it.hasNext()) {
			nicNr++;
			nic = (NetzwerkInterface) it.next();
			btnLocal[nicNr-1] = new JButton(new ImageIcon(getClass().getResource("/gfx/hardware/rj45.png")));
			btnLocal[nicNr-1].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					interfaceButtonClicked(arg0);
				}});

			lblLocal[nicNr-1] = new JLabel("NIC "+nicNr+": "+nic.getIp());
			localPanel.add(btnLocal[nicNr-1]);
			localPanel.add(lblLocal[nicNr-1]);

			// if NIC is connected, then extract connected foreign component
			node = holeVerbundeneKomponente(nic);
			if(node != null) {
				String remoteAddress = "";
				Verbindung connection = this.getConnectedCable(nic);
				Port[] ports = connection.getAnschluesse();
				for (Port port : ports) {
					if (port.getNIC() != null && port.getNIC() != nic) {
						remoteAddress = port.getNIC().getIp();
					}
				}
				btnLocal[nicNr-1].setBackground(Color.GREEN);
				btnLocal[nicNr-1].setEnabled(true);
				btnRemote[nicNr-1] = new JButton(new ImageIcon(getClass().getResource("/gfx/hardware/rj45.png")));
				btnRemote[nicNr-1].setEnabled(false);
				lblRemote[nicNr-1] = new JLabel();
				if(node instanceof filius.hardware.knoten.InternetKnoten) {
					lblRemote[nicNr-1].setText("<html>"+node.getDisplayName().replaceFirst("\n.*",", ...")+"<br>("+remoteAddress+")</html>");
				}
				else {
					lblRemote[nicNr-1].setText(node.getDisplayName().replaceFirst("\n.*",", ..."));
				}
				foreignPanel.add(btnRemote[nicNr-1]);
				foreignPanel.add(lblRemote[nicNr-1]);
				cables.add(new LinePanel());
				cables.getLast().setName( (nicNr-1)+"-"+(nicNr-1) );  // encode index information in name field
			}
			else {
				btnLocal[nicNr-1].setBackground(Color.RED);
				btnLocal[nicNr-1].setEnabled(true);
				btnRemote[nicNr-1] = new JButton(new ImageIcon(getClass().getResource("/gfx/hardware/rj45.png")));
				btnRemote[nicNr-1].setEnabled(false);
				btnRemote[nicNr-1].setVisible(false);
				lblRemote[nicNr-1] = new JLabel();
				lblRemote[nicNr-1].setText("");
				foreignPanel.add(btnRemote[nicNr-1]);
				foreignPanel.add(lblRemote[nicNr-1]);
			}
			if(nicNr==8) {
				btnAddInterface.setEnabled(false);
			}
		}
		for(int i=nicNr; i<8; i++) {
			btnLocal[i] = new JButton(new ImageIcon(getClass().getResource("/gfx/hardware/rj45.png")));
			btnLocal[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					interfaceButtonClicked(arg0);
				}});
			btnLocal[i].setEnabled(false);
			lblLocal[i] = new JLabel();
			lblLocal[i].setText("");
			localPanel.add(btnLocal[i]);
			localPanel.add(lblLocal[i]);

			btnRemote[i] = new JButton(new ImageIcon(getClass().getResource("/gfx/hardware/rj45.png")));
			btnRemote[i].setEnabled(false);
			btnRemote[i].setVisible(false);
			lblRemote[i] = new JLabel();
			lblRemote[i].setText("");
			foreignPanel.add(btnRemote[i]);
			foreignPanel.add(lblRemote[i]);
		}
		btnAddInterface.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// neues Interface einfügen / sichtbar machen
				int newIF = -1;
				for(int i=0; i<8; i++) {
					if(btnLocal[i].isEnabled()) {
						newIF=i+1;
					}
				}
				if(newIF>=8) {
					JOptionPane.showMessageDialog(getKonfiguration(), 
							messages.getString("jvermittlungsrechnerkonfiguration_msg25"),
							messages.getString("jvermittlungsrechnerkonfiguration_msg24"),
							JOptionPane.ERROR_MESSAGE);
					btnAddInterface.setEnabled(false);
					return;
				}
				((Vermittlungsrechner) getKonfiguration().holeHardware()).hinzuAnschluss();
				btnLocal[newIF].setEnabled(true);
				btnLocal[newIF].setBackground(Color.RED);
				lblLocal[newIF].setText("NIC "+(newIF+1)+": "+((NetzwerkInterface) ((Vermittlungsrechner) getKonfiguration().holeHardware()).getNetzwerkInterfaces().get(newIF)).getIp());
				if(newIF==7) {
					btnAddInterface.setEnabled(false);
				}
			}});

		localPanel.add(btnAddInterface);
		
		// -- create visual cable connections (middle area)
		cablePanel = new JPanel();
		cablePanel.setPreferredSize(new Dimension(280,700));
		// directly drawn on background canvas, no components to lay out!
		
		
		// --- bring those areas together
		upperCompound.add(foreignPanel);
		upperCompound.add(cablePanel);
//		upperCompound.add(Box.createHorizontalGlue());
//		upperCompound.add(Box.createHorizontalStrut(100));
		upperCompound.add(localPanel);
		foreignPanel.setSize(400, foreignPanel.getHeight());
		localPanel.setSize(400, localPanel.getHeight());
		
		// - create note area
		noteCompound = new JPanel();
//		noteCompound.setBackground(Color.BLUE);
		noteCompound.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		usageNote = new JTextArea(messages.getString("jvermittlungsrechnerkonfiguration_msg19"));
		usageNote.setOpaque(false);
		usageNote.setEditable(false);
//		usageNote.setBorder(BorderFactory.createLineBorder(Color.GRAY,2));
		noteCompound.add(Box.createVerticalGlue());
		noteCompound.add(usageNote,BorderLayout.CENTER);
		noteCompound.setMinimumSize(new Dimension(700,200));
		
		// - create main button area
		buttonCompound = new JPanel();
//		buttonCompound.setBackground(Color.RED);
		buttonCompound.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		btnClose = new JButton(messages.getString("jvermittlungsrechnerkonfiguration_msg20"));
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Container c = (Container) arg0.getSource();
				do {
					c = c.getParent();
				} while (! (c instanceof JDialog ));
				c.setVisible(false);
				GUIContainer.getGUIContainer().setPropertyConf(getKonfiguration());
				GUIContainer.getGUIContainer().getProperty().reInit();
				GUIContainer.getGUIContainer().getProperty().maximieren();
			}});
		buttonCompound.add(btnClose);
		buttonCompound.setMinimumSize(new Dimension(200,50));
		
//			changeBasicSettingsDialog.getContentPane().add(scrollPane,BorderLayout.CENTER);
		changeBasicSettingsDialog.getContentPane().add(upperCompound,BorderLayout.NORTH);
		changeBasicSettingsDialog.getContentPane().add(noteCompound,BorderLayout.CENTER);
		changeBasicSettingsDialog.getContentPane().add(buttonCompound,BorderLayout.SOUTH);
		
		/////////////////////////////////////////////

		//### Layout (Foreign Components)
		layoutRemote.putConstraint(SpringLayout.NORTH, lblRemoteTitle, 5, SpringLayout.NORTH, foreignPanel);
		layoutRemote.putConstraint(SpringLayout.HORIZONTAL_CENTER, lblRemoteTitle, 0, SpringLayout.HORIZONTAL_CENTER, foreignPanel);

		layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[0], 25, SpringLayout.NORTH, foreignPanel);
		layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[0], 0, SpringLayout.EAST, foreignPanel);
		layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[1], 0, SpringLayout.SOUTH, btnRemote[0]);
		layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[1], 0, SpringLayout.EAST, foreignPanel);
		layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[2], 0, SpringLayout.SOUTH, btnRemote[1]);
		layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[2], 0, SpringLayout.EAST, foreignPanel);
		layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[3], 0, SpringLayout.SOUTH, btnRemote[2]);
		layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[3], 0, SpringLayout.EAST, foreignPanel);
		layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[4], 0, SpringLayout.SOUTH, btnRemote[3]);
		layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[4], 0, SpringLayout.EAST, foreignPanel);
		layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[5], 0, SpringLayout.SOUTH, btnRemote[4]);
		layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[5], 0, SpringLayout.EAST, foreignPanel);
		layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[6], 0, SpringLayout.SOUTH, btnRemote[5]);
		layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[6], 0, SpringLayout.EAST, foreignPanel);
		layoutRemote.putConstraint(SpringLayout.NORTH, btnRemote[7], 0, SpringLayout.SOUTH, btnRemote[6]);
		layoutRemote.putConstraint(SpringLayout.EAST, btnRemote[7], 0, SpringLayout.EAST, foreignPanel);

		layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[0], 0, SpringLayout.VERTICAL_CENTER, btnRemote[0]);
		layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[0], -10, SpringLayout.WEST, btnRemote[0]);
		layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[1], 0, SpringLayout.VERTICAL_CENTER, btnRemote[1]);
		layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[1], -10, SpringLayout.WEST, btnRemote[1]);
		layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[2], 0, SpringLayout.VERTICAL_CENTER, btnRemote[2]);
		layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[2], -10, SpringLayout.WEST, btnRemote[2]);
		layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[3], 0, SpringLayout.VERTICAL_CENTER, btnRemote[3]);
		layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[3], -10, SpringLayout.WEST, btnRemote[3]);
		layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[4], 0, SpringLayout.VERTICAL_CENTER, btnRemote[4]);
		layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[4], -10, SpringLayout.WEST, btnRemote[4]);
		layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[5], 0, SpringLayout.VERTICAL_CENTER, btnRemote[5]);
		layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[5], -10, SpringLayout.WEST, btnRemote[5]);
		layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[6], 0, SpringLayout.VERTICAL_CENTER, btnRemote[6]);
		layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[6], -10, SpringLayout.WEST, btnRemote[6]);
		layoutRemote.putConstraint(SpringLayout.VERTICAL_CENTER, lblRemote[7], 0, SpringLayout.VERTICAL_CENTER, btnRemote[7]);
		layoutRemote.putConstraint(SpringLayout.EAST, lblRemote[7], -10, SpringLayout.WEST, btnRemote[7]);
		//##########

		//### Layout (Local interfaces)
		layoutLocal.putConstraint(SpringLayout.NORTH, lblLocalTitle, 5, SpringLayout.NORTH, localPanel);
		layoutLocal.putConstraint(SpringLayout.HORIZONTAL_CENTER, lblLocalTitle, 0, SpringLayout.HORIZONTAL_CENTER, localPanel);

		layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[0], 25, SpringLayout.NORTH, localPanel);
		layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[0], 0, SpringLayout.WEST, localPanel);
		layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[1], 0, SpringLayout.SOUTH, btnLocal[0]);
		layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[1], 0, SpringLayout.WEST, localPanel);
		layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[2], 0, SpringLayout.SOUTH, btnLocal[1]);
		layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[2], 0, SpringLayout.WEST, localPanel);
		layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[3], 0, SpringLayout.SOUTH, btnLocal[2]);
		layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[3], 0, SpringLayout.WEST, localPanel);
		layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[4], 0, SpringLayout.SOUTH, btnLocal[3]);
		layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[4], 0, SpringLayout.WEST, localPanel);
		layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[5], 0, SpringLayout.SOUTH, btnLocal[4]);
		layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[5], 0, SpringLayout.WEST, localPanel);
		layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[6], 0, SpringLayout.SOUTH, btnLocal[5]);
		layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[6], 0, SpringLayout.WEST, localPanel);
		layoutLocal.putConstraint(SpringLayout.NORTH, btnLocal[7], 0, SpringLayout.SOUTH, btnLocal[6]);
		layoutLocal.putConstraint(SpringLayout.WEST, btnLocal[7], 0, SpringLayout.WEST, localPanel);

		layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[0], 0, SpringLayout.VERTICAL_CENTER, btnLocal[0]);
		layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[0], 10, SpringLayout.EAST, btnLocal[0]);
		layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[1], 0, SpringLayout.VERTICAL_CENTER, btnLocal[1]);
		layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[1], 10, SpringLayout.EAST, btnLocal[1]);
		layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[2], 0, SpringLayout.VERTICAL_CENTER, btnLocal[2]);
		layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[2], 10, SpringLayout.EAST, btnLocal[2]);
		layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[3], 0, SpringLayout.VERTICAL_CENTER, btnLocal[3]);
		layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[3], 10, SpringLayout.EAST, btnLocal[3]);
		layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[4], 0, SpringLayout.VERTICAL_CENTER, btnLocal[4]);
		layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[4], 10, SpringLayout.EAST, btnLocal[4]);
		layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[5], 0, SpringLayout.VERTICAL_CENTER, btnLocal[5]);
		layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[5], 10, SpringLayout.EAST, btnLocal[5]);
		layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[6], 0, SpringLayout.VERTICAL_CENTER, btnLocal[6]);
		layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[6], 10, SpringLayout.EAST, btnLocal[6]);
		layoutLocal.putConstraint(SpringLayout.VERTICAL_CENTER, lblLocal[7], 0, SpringLayout.VERTICAL_CENTER, btnLocal[7]);
		layoutLocal.putConstraint(SpringLayout.WEST, lblLocal[7], 10, SpringLayout.EAST, btnLocal[7]);

		layoutLocal.putConstraint(SpringLayout.NORTH, btnAddInterface, 5, SpringLayout.SOUTH, btnLocal[7]);
		layoutLocal.putConstraint(SpringLayout.HORIZONTAL_CENTER, btnAddInterface, 0, SpringLayout.HORIZONTAL_CENTER, foreignPanel);
		//##########

		//-- draw cable connections
		Main.debug.println("DEBUG: JVermittlungsrechnerkonfiguration, showBasicSettingsDialog, cables.size()="+cables.size());
		SpringLayout cableLayout = new SpringLayout();
		cablePanel.setLayout(cableLayout);
		for (int i=0; i<cables.size(); i++) {
			int l,r;  // indices for foreign component (l; left area) and local component (r; right area)
			LinePanel tmp = cables.get(i);
			Main.debug.println("DEBUG: JVermittlungsrechnerkonfiguration, showBasicSettingsDialog, tmp LinePanel: ("+tmp.hashCode()+")");
			String idxStr = tmp.getName();
			l = Integer.parseInt(idxStr.substring(0, 1));
			r = Integer.parseInt(idxStr.substring(2));
			changeBasicSettingsDialog.pack();
			tmp.setStartPoint(-2,
				              btnRemote[l].getY()+(btnRemote[l].getPreferredSize().height / 2));
			tmp.setEndPoint(282,
				            btnLocal[r].getY()+(btnLocal[r].getPreferredSize().height / 2));
			cablePanel.add(tmp);
			cableLayout.putConstraint(SpringLayout.WEST, tmp, 0, SpringLayout.WEST, cablePanel);
			cableLayout.putConstraint(SpringLayout.NORTH, tmp, 0, SpringLayout.NORTH, cablePanel);
			tmp.setPreferredSize(new Dimension(280,700));
		}
		//-------------------------
		
		changeBasicSettingsDialog.setSize(750, 530);
		changeBasicSettingsDialog.setResizable(false);

		changeBasicSettingsDialog.setVisible(true);
	}
	
	// update settings for all tabs (esp. icons) and specifically for currently open tab
	// it is assumed, that this configuration panel is visible! otherwise no update would be necessary until setting visible
	// (when setting visible, an update will be done automatically!)
//	public void updateSettings() {
//	}
	
	public void doUnselectAction() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JVermittlungsrechnerKonfiguration), doUnselectAction()");
		if(highlightedCable != null) {
			highlightedCable.setAktiv(false);
			highlightedCable = null;
		}
	}
	
	// method to highlight marked cable; called from GUIMainMenu in case of switching back to development view
	public void highlightConnCable() {
		if(highlightedCable != null) {
			highlightedCable.setAktiv(true);
		}
	}
	
	public JVermittlungsrechnerKonfiguration getKonfiguration() {
		return this;
	}

	// simply exchange NIC data and position (!) of NICs in NIC list of router
	// this is done due to problems in changing cable connections (not functioning properly; don't know why...)
	// parameters are direct indices for NICs in NIC list
	private void exchangeNICdata(int idx1, int idx2) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JVermittlungsrechnerKonfiguration), exchangeNICdata("+idx1+","+idx2+")");
		String mac;
		String ip;
		String subnetzMaske;
		String gateway;
		String dns;
		NetzwerkInterface nic1,nic2;
		LinkedList<NetzwerkInterface> nicList = ((Vermittlungsrechner) holeHardware()).getNetzwerkInterfaces();
		
		// get NIC copies
		if(idx1==idx2) return;
		if(idx1 > idx2) {
			int tmpIdx = idx1;
			idx1 = idx2;
			idx2 = tmpIdx;
		}
//		Main.debug.println("DEBUG ("+this.hashCode()+") exchangeNICdata:  idx1="+idx1+", idx2="+idx2+"; NICliste.size="+nicList.size() );
		
		nic2 = nicList.remove(idx2);
		nic1 = nicList.remove(idx1);

//		Main.debug.println("DEBUG ("+this.hashCode()+") exchangeNICdata:  NICliste.size="+nicList.size()+"; "
//	        + "nic1.id="+nic1.hashCode()+", nic1.ip="+nic1.getIp()+", nic1.mac="+nic1.getMac()+"; "
//	        + "nic2.id="+nic2.hashCode()+", nic2.ip="+nic2.getIp()+", nic2.mac="+nic2.getMac() );

		// exchange settings, i.e., store old ones in temp variables first
		mac = nic1.getMac();
		ip = nic1.getIp();
		subnetzMaske = nic1.getSubnetzMaske();
		gateway = nic1.getGateway();
		dns = nic1.getDns();
		nic1.setMac(nic2.getMac());
		nic1.setIp(nic2.getIp());
		nic1.setSubnetzMaske(nic2.getSubnetzMaske());
		nic1.setGateway(nic2.getGateway());
		nic1.setDns(nic2.getDns());
		nic2.setMac(mac);
		nic2.setIp(ip);
		nic2.setSubnetzMaske(subnetzMaske);
		nic2.setGateway(gateway);
		nic2.setDns(dns);
	
		// exchange position in list
		nicList.add(idx1, nic2);
		nicList.add(idx2, nic1);
//		Main.debug.println("DEBUG ("+this.hashCode()+") exchangeNICdata:  idx1="+idx1+", idx2="+idx2+"; NICliste.size="+nicList.size()+"; " 
//	        + "nic1.id="+nicList.get(idx1).hashCode()+", nic1.ip="+nicList.get(idx1).getIp()+", nic1.mac="+nicList.get(idx1).getMac()+"; "
//	        + "nic2.id="+nicList.get(idx2).hashCode()+", nic2.ip="+nicList.get(idx2).getIp()+", nic2.mac="+nicList.get(idx2).getMac() );
	}
	
	// remove cable and return the remotely connected item for further processing
	private GUIKnotenItem removeCable(Verbindung cableConn) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JVermittlungsrechnerKonfiguration), removeCable("+cableConn+")");
		ListIterator cit = GUIContainer.getGUIContainer().getCablelist().listIterator();
		GUIKabelItem cittemp = new GUIKabelItem();
		GUIKnotenItem remoteNode = null;
		LinkedList<GUIKabelItem> loeschListe = new LinkedList<GUIKabelItem>();
		
		// Zu löschende Elemente werden in eine temporäre Liste gepackt
		while (cit.hasNext()) {
			cittemp = (GUIKabelItem) cit.next();
			if (cittemp.getDasKabel() == cableConn) {
				loeschListe.add(cittemp);
				if(cittemp.getKabelpanel().getZiel1().getKnoten().equals(((Vermittlungsrechner) holeHardware()).getSystemSoftware().getKnoten())) {
					remoteNode = cittemp.getKabelpanel().getZiel2();
				}
				else if(cittemp.getKabelpanel().getZiel2().getKnoten().equals(((Vermittlungsrechner) holeHardware()).getSystemSoftware().getKnoten())) {
					remoteNode = cittemp.getKabelpanel().getZiel1();
				}
				
				try {
					cittemp.getDasKabel().anschluesseTrennen();
				} catch (VerbindungsException e) {
					e.printStackTrace(Main.debug);
				}
			}
		}
		
		ListIterator ctt = loeschListe.listIterator();
		while (ctt.hasNext()) {
			cittemp = (GUIKabelItem) ctt.next();
			GUIContainer.getGUIContainer().getCablelist().remove(cittemp);
			GUIContainer.getGUIContainer().getDraftpanel().remove(cittemp.getKabelpanel());
		}
		
		GUIContainer.getGUIContainer().updateViewport();
		return remoteNode;
	}

	// add new cable
	private boolean addCable(GUIKnotenItem remoteNode, Port localPort, Port remotePort) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JVermittlungsrechnerKonfiguration), addCable("+remoteNode+","+localPort+","+remotePort+")");
		GUIContainer c = GUIContainer.getGUIContainer();
		GUIDraftPanel draftpanel = c.getDraftpanel();
		LinkedList<GUIKabelItem> cablelist = c.getCablelist();
		NetzwerkInterface nic2;
		Port anschluss1 = null;
		Port anschluss2 = null;

		GUIKabelItem tmpCable = new GUIKabelItem();
		ListIterator nit = GUIContainer.getGUIContainer().getGUIKnotenItemList().listIterator();
		GUIKnotenItem tmpNode;
		tmpCable.getKabelpanel().setZiel1(null);
		while (nit.hasNext()) {
			tmpNode = (GUIKnotenItem) nit.next();
//			Main.debug.println("DEBUG:  ("+this.hashCode()+") addCable:\n"
//							+  "\ttmpNode: "+tmpNode.getKnoten().getName()+" - "+tmpNode.hashCode()
//							+  "\t<intern>: "+((Vermittlungsrechner) holeHardware()).getSystemSoftware().getKnoten().getName()+" - "+((Vermittlungsrechner) holeHardware()).getSystemSoftware().getKnoten().hashCode());
			if (tmpNode.getKnoten() == ((Vermittlungsrechner) holeHardware()).getSystemSoftware().getKnoten()) {
				tmpCable.getKabelpanel().setZiel1(tmpNode);
			}
		}
		//Main.debug.println("DEBUG:  ("+this.hashCode()+") addCable:   --> tmpCable Ziel1 null? "+(tmpCable.getKabelpanel().getZiel1() == null));
		if (tmpCable.getKabelpanel().getZiel1() == null) return false;  // an error occurred:  current node not identified

		tmpCable.getKabelpanel().setZiel2(remoteNode);

		//Main.debug.println("DEBUG:  ("+this.hashCode()+") addCable:   remoteNode null? "+(remoteNode == null));

		if(remoteNode != null) {
			//Main.debug.println("DEBUG:  ("+this.hashCode()+") addCable:   ziel1 ("+tmpCable.getKabelpanel().getZiel1().hashCode()+"), ziel2 ("+tmpCable.getKabelpanel().getZiel2().hashCode()+")");
		}

		if (tmpCable.getKabelpanel().getZiel2().getKnoten() instanceof Modem) {
			Modem vrOut = (Modem) tmpCable.getKabelpanel().getZiel2().getKnoten();
			anschluss2 = vrOut.getErstenAnschluss();
		}
		else if (tmpCable.getKabelpanel().getZiel2().getKnoten() instanceof Vermittlungsrechner) {
			anschluss2 = remotePort;   // only in this case use pre-determined port; otherwise use internal methods
		}
		else if (tmpCable.getKabelpanel().getZiel2().getKnoten() instanceof Switch) {
			Switch sw = (Switch) tmpCable.getKabelpanel().getZiel2().getKnoten();
			anschluss2 = ((SwitchFirmware) sw.getSystemSoftware()).getKnoten().holeFreienPort();
		}
		else if (tmpCable.getKabelpanel().getZiel2().getKnoten() instanceof InternetKnoten){
			nic2 = (NetzwerkInterface) ((InternetKnoten) tmpCable.getKabelpanel().getZiel2().getKnoten()).getNetzwerkInterfaces().getFirst();
			anschluss2 = nic2.getPort();
		}

		anschluss1 = localPort;
		tmpCable.setDasKabel(new Kabel());
		tmpCable.getDasKabel().setAnschluesse(new Port[]{anschluss1, anschluss2});

		//Main.debug.println("DEBUG:  ("+this.hashCode()+") addCable:   jetzt hinzufügen von Kabel...\n\t"
//				+  tmpCable.toString());

		draftpanel.add(tmpCable.getKabelpanel());
		tmpCable.getKabelpanel().updateBounds();
		draftpanel.updateUI();
		cablelist.add(tmpCable);

		tmpCable = null;
		c.setCablelist(cablelist);
		return true;
	}
	
	// react to clicked interface button in localPanel (BasicSettingsDialog)
	private void interfaceButtonClicked(ActionEvent e) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JVermittlungsrechnerKonfiguration), interfaceButtonClicked("+e+")");
		JButton source = (JButton) e.getSource();
		LinePanel formerLine, currLine;
		LinePanel tmpLP = null;
		int currIdx = -1;
		int formerIdx = -1;
		
		// search for clicked button in button array
		for(int i=0; i<8; i++) {
			if(btnLocal[i]!=null) {
				if(btnLocal[i]==source) 									// found current source button
					currIdx = i;
				if(btnLocal[i].getBackground().equals(Color.YELLOW))		// some button marked yellow 
					formerIdx = i;
			}
		}
		//Main.debug.println("DEBUG: interfaceButtonClicked:  formerIdx="+formerIdx+", currIdx="+currIdx);
		// is some button already highlighted?
		if(formerIdx >= 0) {
			// swap cables
			if(formerIdx!=currIdx) {
				btnLocal[formerIdx].setBackground(Color.RED);
				formerLine = null;
				currLine = null;
				for (int i=0; i<cables.size(); i++) {
					if(cables.get(i).getName().substring(2).equals(String.valueOf(formerIdx))) {  // found corresponding cable (i.e., it exists!)
						formerLine = cables.get(i);
						//Main.debug.println("DEBUG: interfaceButtonClicked;  formerLine="+formerLine.toString()+", index="+i);
					}
					if(cables.get(i).getName().substring(2).equals(String.valueOf(currIdx))) {  // found corresponding cable (i.e., it exists!)
						currLine = cables.get(i);
						//Main.debug.println("DEBUG: interfaceButtonClicked;  currLine="+currLine.toString()+", index="+i);
					}
				}
				if(formerLine != null) {  // found corresponding cable (i.e., it exists!)
					formerLine.lineColor = new Color(64,64,64);
					formerLine.lineEnd = new Point(282,source.getY()+(source.getHeight() / 2));
					formerLine.setName(formerLine.getName().substring(0,2)+currIdx);
					source.setBackground(Color.GREEN);
					//Main.debug.println("DEBUG: interfaceButtonClicked;  formerLine != null ("+formerLine.toString()+")");
				}
				if(currLine != null) {  // found corresponding cable (i.e., it exists!)
					currLine.lineEnd = new Point(282,btnLocal[formerIdx].getY()+(btnLocal[formerIdx].getHeight() / 2));
					currLine.setName(currLine.getName().substring(0,2)+formerIdx);
					btnLocal[formerIdx].setBackground(Color.GREEN);
					//Main.debug.println("DEBUG: interfaceButtonClicked;  currLine != null ("+currLine.toString()+")");
				}
				
				exchangeNICdata(formerIdx,currIdx);
				//
				// actually change connected cables here:
				//
//				try {
//					Port localFormer = ((NetzwerkInterface) ((Vermittlungsrechner) holeHardware()).getNetzwerkInterfaces().get(formerIdx)).getPort();
//					Verbindung formerConn = ((NetzwerkInterface) ((Vermittlungsrechner) holeHardware()).getNetzwerkInterfaces().get(formerIdx)).getPort().getVerbindung();
//					Port[] formerConnPorts = ((NetzwerkInterface) ((Vermittlungsrechner) holeHardware()).getNetzwerkInterfaces().get(formerIdx)).getPort().getVerbindung().getAnschluesse();
//					
//					Port remoteFormer = null; 
//					for(int i=0; i<formerConnPorts.length; i++) {
//						if(formerConnPorts[i] != localFormer) {    // usually only two ports are in this array!
//							remoteFormer = formerConnPorts[i];     // hence, the first port unequal to the local one is assumed to be the remote port
//						}
//					}
//					GUIKnotenItem formerRemNode = removeCable(formerConn);
//					Port[] newFormerPortArray = new Port[2];
//					Port[] newCurrPortArray = new Port[2];
//					if(currLine != null) {
//						Port localCurr = ((NetzwerkInterface) ((Vermittlungsrechner) holeHardware()).getNetzwerkInterfaces().get(currIdx)).getPort();
//						Verbindung currConn = ((NetzwerkInterface) ((Vermittlungsrechner) holeHardware()).getNetzwerkInterfaces().get(currIdx)).getPort().getVerbindung();
//						Port[] currConnPorts = ((NetzwerkInterface) ((Vermittlungsrechner) holeHardware()).getNetzwerkInterfaces().get(currIdx)).getPort().getVerbindung().getAnschluesse();
//						Port remoteCurr = null;
//						for(int i=0; i<currConnPorts.length; i++) {
//							if(currConnPorts[i] != localCurr) {    // usually only two ports are in this array!
//								remoteCurr = currConnPorts[i];     // hence, the first port unequal to the local one is assumed to be the remote port
//							}
//						}
//						// set new ports to former NIC locally and current remote one
//						newCurrPortArray[0] = ((NetzwerkInterface) ((Vermittlungsrechner) holeHardware()).getNetzwerkInterfaces().get(formerIdx)).getPort();
//						newCurrPortArray[1] = remoteCurr;
//						GUIKnotenItem currRemNode = null;
//						currRemNode = removeCable(currConn);
//						if(currRemNode != null) 
//							addCable(currRemNode,newCurrPortArray[0],newCurrPortArray[1]);
//					}
//					// set new ports to current NIC locally and former remote one
//					newFormerPortArray[0] = ((NetzwerkInterface) ((Vermittlungsrechner) holeHardware()).getNetzwerkInterfaces().get(currIdx)).getPort();
//					newFormerPortArray[1] = remoteFormer;
//					addCable(formerRemNode,newFormerPortArray[0],newFormerPortArray[1]);
//				}
//				catch (Exception ge) {
//					ge.printStackTrace(Main.debug);
//				}
				/////////
			}
			else {
				for (int i=0; i<cables.size(); i++) {
					if(cables.get(i).getName().substring(2).equals(String.valueOf(formerIdx))) {  // found corresponding cable (i.e., it exists!)
						//Main.debug.println("DEBUG: interfaceButtonClicked;  formerIdx=currIdx (cables="+cables.get(i).toString()+", index="+i+")");
						cables.get(i).lineColor = new Color(64,64,64);
						source.setBackground(Color.GREEN);
					}
				}
			}
		}
		else {  // else mark current source button
			for (int i=0; i<cables.size(); i++) {
				if(cables.get(i).getName().substring(2).equals(String.valueOf(currIdx))) {  // found corresponding cable (i.e., it exists!)
					source.setBackground(Color.YELLOW);
					cables.get(i).lineColor = Color.MAGENTA;
				}
			}
		}
		changeBasicSettingsDialog.repaint();
	}
	
	public void updateAttribute() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JVermittlungsrechnerKonfiguration), updateAttribute()");
		ListIterator it;
		Vermittlungsrechner vRechner;
		VermittlungsrechnerBetriebssystem bs;
		NetzwerkInterface nic;
		Knoten tempKnoten;

		vRechner = (Vermittlungsrechner) holeHardware();
		bs = (VermittlungsrechnerBetriebssystem) vRechner.getSystemSoftware();

		name.setText(vRechner.getName());
		gateway.setText(bs.getStandardGateway());
		rip.setSelected(bs.getRip());

		it = vRechner.getNetzwerkInterfaces().listIterator();
		for (int i = 0; it.hasNext() && i < ipAdressen.length; i++) {
			nic = (NetzwerkInterface) it.next();
			ipAdressen[i].setText(nic.getIp());
			netzmasken[i].setText(nic.getSubnetzMaske());

			tempKnoten = holeVerbundeneKomponente(nic);
			if (tempKnoten == null) verbundeneKomponente[i].setText(messages
					.getString("jvermittlungsrechnerkonfiguration_msg16"));
			else verbundeneKomponente[i].setText(messages
					.getString("jvermittlungsrechnerkonfiguration_msg6")
					+ " " + tempKnoten.getDisplayName().replace("\n",", "));

		}

		weiterleitungstabelle.updateAttribute();
	}

	private Knoten holeVerbundeneKomponente(NetzwerkInterface nic) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JVermittlungsrechnerKonfiguration), holeVerbundeneKomponente("+nic+")");
		Port lokalerAnschluss, entfernterAnschluss;
		Port[] ports;
		ListIterator it1, it2;
		Knoten knoten;

		if (nic.getPort().getVerbindung() == null) return null;

		lokalerAnschluss = nic.getPort();
		ports = lokalerAnschluss.getVerbindung().getAnschluesse();
		if (ports[0] == lokalerAnschluss) entfernterAnschluss = ports[1];
		else entfernterAnschluss = ports[0];

		it1 = GUIContainer.getGUIContainer().getGUIKnotenItemList()
				.listIterator();
		while (it1.hasNext()) {
			knoten = ((GUIKnotenItem) it1.next()).getKnoten();
			if (knoten instanceof LokalerKnoten) {
				it2 = ((LokalerKnoten) knoten).getAnschluesse().listIterator();
				while (it2.hasNext()) {
					if (it2.next() == entfernterAnschluss) return knoten;
				}
			}
			else if (knoten instanceof InternetKnoten) {
				it2 = ((InternetKnoten) knoten).getNetzwerkInterfaces()
						.listIterator();
				while (it2.hasNext()) {
					if (((NetzwerkInterface) it2.next()).getPort() == entfernterAnschluss)
						return knoten;
				}
			}
			else {
				Main.debug.println("ERROR ("+this.hashCode()+"): Knotentyp unbekannt.");
			}
		}

		return null;
	}
	
	private Kabel getConnectedCable(NetzwerkInterface nic) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JVermittlungsrechnerKonfiguration), getConnectedCable("+nic+")");
		ListIterator<GUIKabelItem> it;
		Verbindung nicConn, tmpConn;
		GUIKabelItem cable;

		nicConn = nic.getPort().getVerbindung(); 
		if (nicConn == null) return null;

		it = GUIContainer.getGUIContainer().getCablelist().listIterator();
		
		while (it.hasNext()) {
			cable = ((GUIKabelItem) it.next());
			tmpConn = cable.getDasKabel();
			if (nicConn == tmpConn) {
				//Main.debug.println("JVermittlungsrechnetKonfiguration, getnicConnectedCable:   ("+nicConn+") == ("+tmpConn+")");
				return (Kabel) tmpConn;
			}
			else {
				//Main.debug.println("JVermittlungsrechnetKonfiguration, getnicConnectedCable:   ("+nicConn+") != ("+tmpConn+")");
			}
		}
		return null;  // nothing found
	}

}
