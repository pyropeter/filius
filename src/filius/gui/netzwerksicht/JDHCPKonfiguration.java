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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ItemListener;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import filius.Main;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.I18n;
import filius.software.dhcp.DHCPServer;
import filius.software.system.Betriebssystem;

public class JDHCPKonfiguration extends JDialog implements I18n, ItemListener  {

	private DHCPServer server;
	private Betriebssystem bs;

	private JTextField tfObergrenze;
	private JTextField tfUntergrenze;
	private JTextField tfNetzmaske;
	private JTextField tfGateway;
	private JTextField tfDNSServer;
	private JCheckBox cbAktiv;
	private JCheckBox cbUseInternal;


	public JDHCPKonfiguration (JFrame owner, String titel, Betriebssystem bs) {
		super(owner, titel, true);
		this.bs = bs;
		this.server = bs.getDHCPServer();

		this.setSize(380, 340);
		this.setResizable(false);

		//Main.debug.println(owner.getSize().width+" "+owner.getSize().height);
		//Main.debug.println(owner.getWidth()+" "+owner.getHeight());
		//Main.debug.println((owner.getWidth()/2)-(this.getSize().width)/2);
		//Main.debug.println((owner.getHeight()/2)-(this.getSize().height)/2);

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

		Point location = new Point((screen.width/2)-190,(screen.height/2)-140);

		this.setLocation(location);

		initComponents();
	}

	private void initComponents() {
		JPanel jpDhcp;
		JLabel lbObergrenze;
		JLabel lbUntergrenze;
		JLabel lbNetzmaske;
		JLabel lbGateway;
		JLabel lbDNSServer;
		JButton btUebernehmen;

		JLabel lbAktiv;
		JLabel lbUseInternal;
		final JDialog config = this;

		SpringLayout layout = new SpringLayout();
		jpDhcp = new JPanel(layout);

		lbUntergrenze = new JLabel(messages.getString("jdhcpkonfiguration_msg1"));
		tfUntergrenze = new JTextField(server.getUntergrenze());
		tfUntergrenze.setPreferredSize(new Dimension(150,25));
		tfUntergrenze.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfUntergrenze);
			}
		});

		lbObergrenze = new JLabel(messages.getString("jdhcpkonfiguration_msg2"));
		tfObergrenze = new JTextField(server.getObergrenze());
		tfObergrenze.setPreferredSize(new Dimension(150,25));
		tfObergrenze.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfObergrenze);
			}
		});

		lbNetzmaske = new JLabel(messages.getString("jdhcpkonfiguration_msg3"));
		tfNetzmaske = new JTextField(server.getSubnetzmaske());
		tfNetzmaske.setPreferredSize(new Dimension(150,25));
		tfNetzmaske.setEditable(false);


		lbGateway = new JLabel(messages.getString("jdhcpkonfiguration_msg4"));
		tfGateway = new JTextField(server.getGatewayip());
		tfGateway.setPreferredSize(new Dimension(150,25));
		tfGateway.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfGateway);
			}
		});
		tfGateway.setEditable(server.useInternal());

		lbDNSServer = new JLabel(messages.getString("jdhcpkonfiguration_msg5"));
		tfDNSServer = new JTextField(server.getDnsserverip());
		tfDNSServer.setPreferredSize(new Dimension(150,25));
		tfDNSServer.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfDNSServer);
			}
		});
		tfDNSServer.setEditable(server.useInternal());

		jpDhcp.add(lbUntergrenze);
		jpDhcp.add(lbObergrenze);
		jpDhcp.add(lbNetzmaske);

		JPanel borderPanel = new JPanel();  // Panel used to paint border around gateway/DNS form field
		borderPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
		borderPanel.setOpaque(false);
		jpDhcp.add(borderPanel);
		
		jpDhcp.add(lbGateway);
		jpDhcp.add(lbDNSServer);

		jpDhcp.add(tfUntergrenze);
		jpDhcp.add(tfObergrenze);
		jpDhcp.add(tfNetzmaske);

		jpDhcp.add(tfGateway);
		jpDhcp.add(tfDNSServer);

		btUebernehmen = new JButton(messages.getString("jdhcpkonfiguration_msg7"));
		btUebernehmen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfObergrenze))
					server.setObergrenze(tfObergrenze.getText());

				if (ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfUntergrenze))
					server.setUntergrenze(tfUntergrenze.getText());
				
				if (cbUseInternal.isSelected()) {
					server.setOwnSettings(true);
					if (ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfGateway))
						server.setGatewayip(tfGateway.getText());
					if (ueberpruefen(EingabenUeberpruefung.musterIpAdresse, tfDNSServer))
						server.setDnsserverip(tfDNSServer.getText());
				}
				else {
					server.setOwnSettings(false);
				}

				server.setAktiv(cbAktiv.isSelected());

				update();
				config.setVisible(false);
			}});

		lbAktiv = new JLabel(messages.getString("jdhcpkonfiguration_msg6"));
		lbAktiv.setPreferredSize(new Dimension(200, 15));
		cbAktiv = new JCheckBox();
		cbAktiv.setSelected(server.isAktiv());

		lbUseInternal = new JLabel(messages.getString("jdhcpkonfiguration_msg8"));
		lbUseInternal.setToolTipText(messages.getString("jdhcpkonfiguration_msg9"));
		cbUseInternal = new JCheckBox();
		cbUseInternal.addItemListener(this);
		cbUseInternal.setToolTipText(messages.getString("jdhcpkonfiguration_msg9"));
		cbUseInternal.setSelected(server.useInternal());

		jpDhcp.add(lbUseInternal);
		jpDhcp.add(cbUseInternal);

		jpDhcp.add(lbAktiv);
		jpDhcp.add(cbAktiv);
		jpDhcp.add(btUebernehmen);

		/*Layout. Set positions with Constraints.*/
//		Labels:
		layout.putConstraint(SpringLayout.NORTH, lbUntergrenze, 20, SpringLayout.NORTH, this.getContentPane());
		layout.putConstraint(SpringLayout.WEST, lbUntergrenze, 25, SpringLayout.WEST, this.getContentPane());

		layout.putConstraint(SpringLayout.NORTH, lbObergrenze, 20, SpringLayout.SOUTH, lbUntergrenze);
		layout.putConstraint(SpringLayout.WEST, lbObergrenze, 25, SpringLayout.WEST, this.getContentPane());

		layout.putConstraint(SpringLayout.NORTH, lbNetzmaske, 20, SpringLayout.SOUTH, lbObergrenze);
		layout.putConstraint(SpringLayout.WEST, lbNetzmaske, 25, SpringLayout.WEST, this.getContentPane());

		layout.putConstraint(SpringLayout.NORTH, lbGateway, 30, SpringLayout.SOUTH, lbNetzmaske);
		layout.putConstraint(SpringLayout.WEST, lbGateway, 30, SpringLayout.WEST, this.getContentPane());

		layout.putConstraint(SpringLayout.NORTH, lbDNSServer, 20, SpringLayout.SOUTH, lbGateway);
		layout.putConstraint(SpringLayout.WEST, lbDNSServer, 30, SpringLayout.WEST, this.getContentPane());

//		Textfields:
		layout.putConstraint(SpringLayout.NORTH, tfUntergrenze, 0, SpringLayout.NORTH, lbUntergrenze);
		layout.putConstraint(SpringLayout.WEST, tfUntergrenze, 200, SpringLayout.WEST, this.getContentPane());

		layout.putConstraint(SpringLayout.NORTH, tfObergrenze, 0, SpringLayout.NORTH, lbObergrenze);
		layout.putConstraint(SpringLayout.WEST, tfObergrenze, 200, SpringLayout.WEST, this.getContentPane());

		layout.putConstraint(SpringLayout.NORTH, tfNetzmaske, 0, SpringLayout.NORTH, lbNetzmaske);
		layout.putConstraint(SpringLayout.WEST, tfNetzmaske, 200, SpringLayout.WEST, this.getContentPane());

		layout.putConstraint(SpringLayout.NORTH, tfGateway, 0, SpringLayout.NORTH, lbGateway);
		layout.putConstraint(SpringLayout.WEST, tfGateway, 195, SpringLayout.WEST, this.getContentPane());

		layout.putConstraint(SpringLayout.NORTH, tfDNSServer, 0, SpringLayout.NORTH, lbDNSServer);
		layout.putConstraint(SpringLayout.WEST, tfDNSServer, 195, SpringLayout.WEST, this.getContentPane());

		layout.putConstraint(SpringLayout.EAST, lbUseInternal, 0, SpringLayout.EAST, tfDNSServer);
		layout.putConstraint(SpringLayout.NORTH, lbUseInternal, 10, SpringLayout.SOUTH, tfDNSServer);
		layout.putConstraint(SpringLayout.EAST, cbUseInternal, 0, SpringLayout.WEST, lbUseInternal);
		layout.putConstraint(SpringLayout.SOUTH, cbUseInternal, 4, SpringLayout.SOUTH, lbUseInternal);

		/*Layout*/
		layout.putConstraint(SpringLayout.NORTH, cbAktiv, 30, SpringLayout.SOUTH, lbUseInternal);
		layout.putConstraint(SpringLayout.WEST, cbAktiv, 25, SpringLayout.WEST, this.getContentPane());

		layout.putConstraint(SpringLayout.NORTH, lbAktiv, 4, SpringLayout.NORTH, cbAktiv);
		layout.putConstraint(SpringLayout.WEST, lbAktiv, 4, SpringLayout.EAST, cbAktiv);

		layout.putConstraint(SpringLayout.NORTH, btUebernehmen, 10, SpringLayout.SOUTH, lbAktiv);
		layout.putConstraint(SpringLayout.WEST, btUebernehmen, 25, SpringLayout.WEST, this.getContentPane());

		layout.putConstraint(SpringLayout.NORTH, borderPanel, 10, SpringLayout.SOUTH, tfNetzmaske);
		layout.putConstraint(SpringLayout.WEST, borderPanel, 25, SpringLayout.WEST, this.getContentPane());

		borderPanel.setPreferredSize(new Dimension(325,105));
		getContentPane().add(jpDhcp);

		update();
	}

	/** Listens to the check boxes. */
    public void itemStateChanged(java.awt.event.ItemEvent e) {
    	Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JDHCPKonfiguration) itemStateChanged("+e+"); source="+e.getItemSelectable());
        Object source = e.getItemSelectable();

        if (source == cbUseInternal) {
        	//Main.debug.println("\titemStateChanged; source==cbUseInternal");
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            	server.setOwnSettings(true);
            	tfGateway.setText(server.getGatewayip());
            	tfGateway.setEditable(true);
            	tfDNSServer.setText(server.getDnsserverip());
            	tfDNSServer.setEditable(true);
            }
            else {
            	server.setOwnSettings(false);
            	tfGateway.setText(server.getGatewayip());
            	tfGateway.setEditable(false);
            	tfDNSServer.setText(server.getDnsserverip());
            	tfDNSServer.setEditable(false);
            }
        }
        else {
        	//Main.debug.println("\titemStateChanged; source ("+source+") != cbUseInternal ("+cbUseInternal+")");
        }
    }
	
	private void update() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JDHCPKonfiguration), update()");
		tfObergrenze.setText(server.getObergrenze());
		tfUntergrenze.setText(server.getUntergrenze());
		tfNetzmaske.setText(server.getSubnetzmaske());
		tfGateway.setText(server.getGatewayip());
		tfDNSServer.setText(server.getDnsserverip());
		cbUseInternal.setSelected(server.useInternal());
		cbAktiv.setSelected(server.isAktiv());
	}

	public boolean ueberpruefen(Pattern pruefRegel, JTextField feld) {
		if (EingabenUeberpruefung.isGueltig(feld.getText(), pruefRegel)) {
			feld.setForeground(EingabenUeberpruefung.farbeRichtig);
			JTextField test = new JTextField();
			feld.setBorder(test.getBorder());
			return true;
		} else {
			feld.setForeground(EingabenUeberpruefung.farbeFalsch);

			feld.setForeground(EingabenUeberpruefung.farbeFalsch);
			feld.setBorder(BorderFactory.createLineBorder(
					EingabenUeberpruefung.farbeFalsch, 1));
			return false;
		}

	}

}
