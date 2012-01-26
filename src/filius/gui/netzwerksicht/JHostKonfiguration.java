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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import filius.Main;
import filius.gui.GUIContainer;
import filius.gui.JMainFrame;
import filius.hardware.Hardware;
import filius.hardware.knoten.Host;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.I18n;
import filius.software.system.Betriebssystem;

public class JHostKonfiguration extends JKonfiguration implements I18n {

	private static final long serialVersionUID = 1L;

	private JTextField name; // Name,Name,20,String,editable,Neuer
								// Rechner,null

	private JTextField macAdresse; // MAC-Adresse,15,String,not editable

	private JTextField ipAdresse; // IP-Adresse,IpAdresse,15,String,editable,192.168.0.0,IpAdresse

	private JTextField netzmaske; // Subnetzmaske,Subnetzmaske,15,String,editable,255.255.255.0,subnetz

	private JTextField gateway; // Gateway,Gateway,15,String,editable,192.168.0.99,IpAdresse

	private JTextField dns; // DNS,Dns,15,String,editable,192.168.0.1,IpAdresse

	// Anzahl der Anschlüsse,AnzahlAnschluesse,1,int,neditable,1,null
	private JCheckBox dhcp; // Adresse per DHCP
							// beziehen,Dhcp,1,boolean,editable,false,null
	private JButton btDhcp;


	protected JHostKonfiguration(Hardware hardware) {
		super(hardware);
	}

	/** Diese Methode wird vom JAendernButton aufgerufen */
	public void aenderungenAnnehmen() {
		Host host;
		Betriebssystem bs;

		if (holeHardware() != null) {
			host = (Host) holeHardware();
			host.setName(name.getText());

			bs = (Betriebssystem) host.getSystemSoftware();
				bs.setzeIPAdresse(ipAdresse.getText());
				bs.setzeNetzmaske(netzmaske.getText());
				bs.setStandardGateway(gateway.getText());
				bs.setDNSServer(dns.getText());
			bs.setDHCPKonfiguration(dhcp.isSelected());

			if (dhcp.isSelected()) bs.getDHCPServer().setAktiv(false);
		} else {
			Main.debug.println("GUIRechnerKonfiguration: Aenderungen konnten nicht uebernommen werden.");
		}

		GUIContainer.getGUIContainer().updateViewport();
		updateAttribute();
	}

	protected void initAttributEingabeBox(Box box) {
		JLabel tempLabel;
		Box tempBox;
		FocusListener focusListener;
		ActionListener actionListener;


		actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				aenderungenAnnehmen();
			}
		};
		focusListener = new FocusListener() {

			public void focusGained(FocusEvent arg0) {	}

			public void focusLost(FocusEvent arg0) {
				aenderungenAnnehmen();
			}

		};


		// =======================================================
		// Attribut Name
		tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg1"));
		tempLabel.setPreferredSize(new Dimension(140, 10));
		tempLabel.setVisible(true);
		tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		name = new JTextField(messages.getString("jhostkonfiguration_msg2"));
		name.addActionListener(actionListener);
		name.addFocusListener(focusListener);

		tempBox = Box.createHorizontalBox();
		tempBox.setOpaque(true);
		tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		tempBox.setMaximumSize(new Dimension(400, 40));
		tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tempBox.add(tempLabel);
		tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
		tempBox.add(name);
		box.add(tempBox, BorderLayout.NORTH);

		// =======================================================
		// Attribut MAC-Adresse
		tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg9"));
		tempLabel.setPreferredSize(new Dimension(140, 10));
		tempLabel.setVisible(true);
		tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		macAdresse = new JTextField("");
		macAdresse.setEnabled(false);

		tempBox = Box.createHorizontalBox();
		tempBox.setOpaque(true);
		tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		tempBox.setMaximumSize(new Dimension(400, 40));
		tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tempBox.add(tempLabel);
		tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
		tempBox.add(macAdresse);
		box.add(tempBox, BorderLayout.NORTH);

		// =======================================================
		// Attribut IP-Adresse
		tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg3"));
		tempLabel.setPreferredSize(new Dimension(140, 10));
		tempLabel.setVisible(true);
		tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		ipAdresse = new JTextField("192.168.0.1");
		ipAdresse.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				ueberpruefen(EingabenUeberpruefung.musterIpAdresse, ipAdresse);
			}
		});
		ipAdresse.addActionListener(actionListener);
		ipAdresse.addFocusListener(focusListener);


		tempBox = Box.createHorizontalBox();
		tempBox.setOpaque(true);
		tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		tempBox.setMaximumSize(new Dimension(400, 40));
		tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tempBox.add(tempLabel);
		tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
		tempBox.add(ipAdresse);
		box.add(tempBox, BorderLayout.NORTH);

		// =======================================================
		// Attribut Netzmaske
		tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg4"));
		tempLabel.setPreferredSize(new Dimension(140, 10));
		tempLabel.setVisible(true);
		tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		netzmaske = new JTextField("255.255.255.0");
		netzmaske.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				ueberpruefen(EingabenUeberpruefung.musterSubNetz, netzmaske);
				//setMessage("\u00C4nderungen werden erst durch die Schaltflaeche \u00C4ndern \u00FCbernommen!");
			}
		});
		netzmaske.addActionListener(actionListener);
		netzmaske.addFocusListener(focusListener);


		tempBox = Box.createHorizontalBox();
		tempBox.setOpaque(true);
		tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		tempBox.setMaximumSize(new Dimension(400, 40));
		tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tempBox.add(tempLabel);
		tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
		tempBox.add(netzmaske);
		box.add(tempBox, BorderLayout.NORTH);

		// =======================================================
		// Attribut Gateway-Adresse
		tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg5"));
		tempLabel.setPreferredSize(new Dimension(140, 10));
		tempLabel.setVisible(true);
		tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		gateway = new JTextField("192.168.0.1");
		gateway.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				ueberpruefen(EingabenUeberpruefung.musterIpAdresseAuchLeer, gateway);
			}
		});
		gateway.addActionListener(actionListener);
		gateway.addFocusListener(focusListener);


		tempBox = Box.createHorizontalBox();
		tempBox.setOpaque(true);
		tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		tempBox.setMaximumSize(new Dimension(400, 40));
		tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tempBox.add(tempLabel);
		tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
		tempBox.add(gateway);
		box.add(tempBox, BorderLayout.NORTH);

		// =======================================================
		// Attribut Adresse des Domain Name Server
		tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg6"));
		tempLabel.setPreferredSize(new Dimension(140, 10));
		tempLabel.setVisible(true);
		tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		dns = new JTextField("192.168.0.1");
		dns.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				ueberpruefen(EingabenUeberpruefung.musterIpAdresseAuchLeer, dns);
			}
		});
		dns.addActionListener(actionListener);
		dns.addFocusListener(focusListener);


		tempBox = Box.createHorizontalBox();
		tempBox.setOpaque(true);
		tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		tempBox.setMaximumSize(new Dimension(400, 40));
		tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tempBox.add(tempLabel);
		tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
		tempBox.add(dns);
		box.add(tempBox, BorderLayout.NORTH);

		// =======================================================
		// Attribut Verwendung von DHCP
		tempLabel = new JLabel(messages.getString("jhostkonfiguration_msg7"));
		tempLabel.setPreferredSize(new Dimension(140, 10));
		tempLabel.setVisible(true);
		tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		dhcp = new JCheckBox();
		dhcp.setSelected(false);
		dhcp.addActionListener(actionListener);

		tempBox = Box.createHorizontalBox();
		tempBox.setOpaque(true);
		tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		tempBox.setMaximumSize(new Dimension(400, 40));
		tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tempBox.add(tempLabel);
		tempBox.add(Box.createHorizontalStrut(5)); // Platz zw. tempLabel und
		tempBox.add(dhcp);
		box.add(tempBox, BorderLayout.NORTH);

		// ===================================================
		// DHCP-Server einrichten
		tempBox = Box.createHorizontalBox();
		tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);

		btDhcp = new JButton(messages.getString("jhostkonfiguration_msg8"));
		btDhcp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				JDHCPKonfiguration dhcpKonfig = new JDHCPKonfiguration(JMainFrame.getJMainFrame(), messages.getString("jhostkonfiguration_msg8"), (Betriebssystem)((Host)holeHardware()).getSystemSoftware());
				dhcpKonfig.setVisible(true);
			}});
		tempBox.add(btDhcp);
		box.add(tempBox);

		updateAttribute();
	}

	public void updateAttribute() {
		Betriebssystem bs;
		Host host;

		if (holeHardware() != null) {
			host = (Host) holeHardware();
			name.setText(host.getName());

			bs = (Betriebssystem) host.getSystemSoftware();

			macAdresse.setText(bs.holeMACAdresse());
			ipAdresse.setText(bs.holeIPAdresse());
			netzmaske.setText(bs.holeNetzmaske());
			gateway.setText(bs.getStandardGateway());
			dns.setText(bs.getDNSServer());

			dhcp.setSelected(bs.isDHCPKonfiguration());
			btDhcp.setEnabled(!dhcp.isSelected());

			ipAdresse.setEnabled(!bs.isDHCPKonfiguration());
			netzmaske.setEnabled(!bs.isDHCPKonfiguration());
			gateway.setEnabled(!bs.isDHCPKonfiguration());
			dns.setEnabled(!bs.isDHCPKonfiguration());
		}
		else {
			Main.debug
					.println("GUIRechnerKonfiguration: keine Hardware-Komponente vorhanden");
		}
	}

}
