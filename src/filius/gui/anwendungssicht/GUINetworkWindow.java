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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.Host;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.I18n;
import filius.software.system.Betriebssystem;

/**
 * Die Klasse stellt einen Dialog dar, in dem der Nutzer die Netzwerkeinstellungen des aktuellen
 * Rechners betrachten und editieren kann, waehrend er im Anwendungsmodus ist.
 *
 * @author Thomas Gerding
 *
 */
public class GUINetworkWindow  extends JInternalFrame implements I18n {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private GUIDesktopPanel dp;
	private JLabel ipLabel, dnsLabel, gatewayLabel, netmaskLabel, macLabel;
	private JTextField ipField, dnsField, gatewayField, netmaskField, macField;
	private JButton changeButton;
	private Betriebssystem bs;
	private JPanel backPanel;
	private boolean istGueltig=true;

	public GUINetworkWindow(final GUIDesktopPanel dp)
	{
		super();

		NetzwerkInterface nic;

		this.dp=dp;

		backPanel = new JPanel(new BorderLayout());

		ipLabel = new JLabel(messages.getString("network_msg1"));
		ipLabel.setSize(new Dimension(100,15));
		ipLabel.setPreferredSize(new Dimension(100,15));
		dnsLabel = new JLabel(messages.getString("network_msg2"));
		dnsLabel.setSize(new Dimension(100,15));
		dnsLabel.setPreferredSize(new Dimension(100,15));
		gatewayLabel = new JLabel(messages.getString("network_msg3"));
		gatewayLabel.setSize(new Dimension(100,15));
		gatewayLabel.setPreferredSize(new Dimension(100,15));
		netmaskLabel = new JLabel(messages.getString("network_msg4"));
		netmaskLabel.setSize(new Dimension(100,15));
		netmaskLabel.setPreferredSize(new Dimension(100,15));
		macLabel = new JLabel(messages.getString("network_msg9"));
		macLabel.setSize(new Dimension(100,15));
		macLabel.setPreferredSize(new Dimension(100,15));

		bs = this.dp.getBetriebssystem();
		nic = (NetzwerkInterface) ((Host)bs.getKnoten()).getNetzwerkInterfaces().getFirst();

		ipField = new JTextField(nic.getIp());
		ipField.setEditable(false);
		ipField.setSize(new Dimension(100,15));
		ipField.setPreferredSize(new Dimension(100,15));
		ipField.addKeyListener(new KeyAdapter()
		{
			public void keyReleased(KeyEvent e)
			{
				ueberpruefen(EingabenUeberpruefung.musterIpAdresse,ipField);
			}

		});
		dnsField = new JTextField(bs.getDNSServer());
		dnsField.setEditable(false);
		dnsField.setSize(new Dimension(100,15));
		dnsField.setPreferredSize(new Dimension(100,15));
		dnsField.addKeyListener(new KeyAdapter()
		{
			public void keyReleased(KeyEvent e)
			{
				ueberpruefen(EingabenUeberpruefung.musterIpAdresse,dnsField);
			}

		});
		gatewayField = new JTextField(bs.getStandardGateway());
		gatewayField.setEditable(false);
		gatewayField.setSize(new Dimension(100,15));
		gatewayField.setPreferredSize(new Dimension(100,15));
		gatewayField.addKeyListener(new KeyAdapter()
		{
			public void keyReleased(KeyEvent e)
			{
				ueberpruefen(EingabenUeberpruefung.musterIpAdresse,gatewayField);
			}

		});
		netmaskField = new JTextField(nic.getSubnetzMaske());
		netmaskField.setEditable(false);
		netmaskField.setSize(new Dimension(100,15));
		netmaskField.setPreferredSize(new Dimension(100,15));
		netmaskField.addKeyListener(new KeyAdapter()
		{
			public void keyReleased(KeyEvent e)
			{
				ueberpruefen(EingabenUeberpruefung.musterIpAdresse,netmaskField);
			}

		});

		macField = new JTextField(nic.getMac());
		macField.setEditable(false);
		macField.setSize(new Dimension(100,15));
		macField.setPreferredSize(new Dimension(100,15));

		changeButton = new JButton(messages.getString("network_msg5"));
		changeButton.setToolTipText(messages.getString("network_msg6"));
		changeButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						istGueltig=true; //Um den nächsten Testdurchlauf zu ermöglichen

						if(!EingabenUeberpruefung.isGueltig(ipField.getText(), EingabenUeberpruefung.musterIpAdresse) || istGueltig == false) istGueltig=false;
						if(!EingabenUeberpruefung.isGueltig(netmaskField.getText(), EingabenUeberpruefung.musterIpAdresse) || istGueltig == false) istGueltig=false;
						if(!EingabenUeberpruefung.isGueltig(dnsField.getText(), EingabenUeberpruefung.musterIpAdresse) || istGueltig == false) istGueltig=false;
						if(!EingabenUeberpruefung.isGueltig(gatewayField.getText(), EingabenUeberpruefung.musterIpAdresse) || istGueltig == false) istGueltig=false;

						if(istGueltig == true)
						{
							bs.setzeIPAdresse(ipField.getText());
							bs.setzeNetzmaske(netmaskField.getText());
							bs.setDNSServer(dnsField.getText());
							bs.setStandardGateway(gatewayField.getText());
						}
						else
						{
							JOptionPane.showMessageDialog(dp, messages.getString("network_msg7"));
						}

					}

				}
				);


		Box backBox = Box.createVerticalBox();

		Box ipBox = Box.createHorizontalBox();
		ipBox.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		ipBox.add(ipLabel);
		ipBox.add(Box.createHorizontalStrut(5));
		ipBox.add(ipField);

		Box maskBox = Box.createHorizontalBox();
		maskBox.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		maskBox.add(netmaskLabel);
		maskBox.add(Box.createHorizontalStrut(5));
		maskBox.add(netmaskField);

		Box dnsBox = Box.createHorizontalBox();
		dnsBox.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		dnsBox.add(dnsLabel);
		dnsBox.add(Box.createHorizontalStrut(5));
		dnsBox.add(dnsField);

		Box gateBox = Box.createHorizontalBox();
		gateBox.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		gateBox.add(gatewayLabel);
		gateBox.add(Box.createHorizontalStrut(5));
		gateBox.add(gatewayField);

		Box macBox = Box.createHorizontalBox();
		macBox.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		macBox.add(macLabel);
		macBox.add(Box.createHorizontalStrut(5));
		macBox.add(macField);

		Box buttonBox = Box.createHorizontalBox();
		buttonBox.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		buttonBox.add(changeButton);

		backBox.setBorder(BorderFactory.createBevelBorder(2));
		backBox.add(ipBox);
		backBox.add(Box.createHorizontalStrut(5));
		backBox.add(maskBox);
		backBox.add(Box.createHorizontalStrut(5));
		backBox.add(gateBox);
		backBox.add(Box.createHorizontalStrut(5));
		backBox.add(dnsBox);
		backBox.add(Box.createHorizontalStrut(10));
		backBox.add(macBox);
		backBox.add(Box.createHorizontalStrut(5));
		//backBox.add(buttonBox);

		backPanel.add(backBox, BorderLayout.CENTER);
		this.getContentPane().add(backPanel);

		this.setClosable(true);
		this.setMaximizable(false);

		this.setResizable(false);
		this.setBounds(0,80,320,240);
		this.setTitle(messages.getString("network_msg8"));
		this.setVisible(false);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.setAnwendungsIcon("gfx/desktop/netzwek_aus.png");
		dp.getDesktopPane().add(this);
	}

	public void setAnwendungsIcon(String datei)
	{
		ImageIcon image =new ImageIcon(getClass().getResource("/"+datei));
		image.setImage(image.getImage().getScaledInstance(16, 16, Image.SCALE_AREA_AVERAGING));
	    this.setFrameIcon(image);
	}

	/**
	 * Funktion die waehrend der Eingabe ueberprueft ob die bisherige Eingabe einen
	 * korrekten Wert darstellt.
	 *
	 * @author Johannes Bade & Thomas Gerding
	 * @param pruefRegel
	 * @param feld
	 */
	public void ueberpruefen(Pattern pruefRegel, JTextField feld)
	{
		if (EingabenUeberpruefung.isGueltig(feld.getText(), pruefRegel))
		{
			feld.setForeground(EingabenUeberpruefung.farbeRichtig);
			JTextField test = new JTextField();
			feld.setBorder(test.getBorder());
		}
		else
		{
			feld.setForeground(EingabenUeberpruefung.farbeFalsch);

			feld.setForeground(EingabenUeberpruefung.farbeFalsch);
			feld.setBorder(BorderFactory.createLineBorder(EingabenUeberpruefung.farbeFalsch, 1));
		}

	}

	public void setVisible(boolean b) {
		if (b) {
			// bring data up-to-date:
			bs = this.dp.getBetriebssystem();
			NetzwerkInterface nic = (NetzwerkInterface) ((Host)bs.getKnoten()).getNetzwerkInterfaces().getFirst();

			ipField.setText(nic.getIp());
			dnsField.setText(bs.getDNSServer());
			gatewayField.setText(bs.getStandardGateway());
			netmaskField.setText(nic.getSubnetzMaske());
		}
		super.setVisible(b);
	}

}
