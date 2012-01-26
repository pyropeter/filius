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
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import filius.Main;
import filius.gui.GUIContainer;
import filius.hardware.Hardware;
import filius.hardware.knoten.Modem;
import filius.rahmenprogramm.I18n;
import filius.software.system.ModemFirmware;

public class JModemKonfiguration extends JKonfiguration implements I18n, Observer {

	private static final long serialVersionUID = 1L;

	private JTextField name; // Name,Name,20,String,editable,Neuer

	private JCheckBox cbServerModus;

	private JTextField tfIpAdresse;
	private JTextField tfPort;

	private JButton btStartStop;

	protected JModemKonfiguration(Hardware hardware) {
		super(hardware);

		((Modem)holeHardware()).getSystemSoftware().addObserver(this);
	}

	public void aenderungenAnnehmen() {
	  	Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JModemKonfiguration), aenderungenAnnehmen()");
		Modem modem;
		ModemFirmware firmware;

		modem = (Modem)holeHardware();
		firmware = (ModemFirmware)modem.getSystemSoftware();

		modem.setName(name.getText());
		firmware.setIpAdresse(tfIpAdresse.getText());
		try {
		firmware.setPort(Integer.parseInt(tfPort.getText()));
		}
		catch (Exception e) {}

		if (cbServerModus.isSelected()) {
			((ModemFirmware)modem.getSystemSoftware()).setMode(ModemFirmware.SERVER);
		}
		else {
			((ModemFirmware)modem.getSystemSoftware()).setMode(ModemFirmware.CLIENT);
		}

	}

	protected void initAttributEingabeBox(Box box) {
	  	Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JModemKonfiguration), initAttributEingabeBox("+box+")");
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

		tempLabel = new JLabel(messages.getString("jmodemkonfiguration_msg1"));
		tempLabel.setPreferredSize(new Dimension(140, 10));
		tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		name = new JTextField("");
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

		tempBox = Box.createHorizontalBox();
		tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		tempBox.setMaximumSize(new Dimension(400, 40));
		tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		cbServerModus = new JCheckBox();
		cbServerModus.setText(messages.getString("jmodemkonfiguration_msg2"));
		tempBox.add(cbServerModus);

		box.add(tempBox);

		tempBox = Box.createHorizontalBox();
		tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		tempBox.setMaximumSize(new Dimension(400, 40));
		tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		tempLabel = new JLabel(messages.getString("jmodemkonfiguration_msg3"));
		tempLabel.setPreferredSize(new Dimension(140, 10));
		tempBox.add(tempLabel);

		tfIpAdresse = new JTextField("192.168.0.21");
		tfIpAdresse.setEnabled(false);
		tfIpAdresse.setPreferredSize(new Dimension(100,18));
		tfIpAdresse.setText(((ModemFirmware)((Modem)holeHardware()).getSystemSoftware()).getIpAdresse());
		tempBox.add(tfIpAdresse);

		box.add(tempBox);

		tempBox = Box.createHorizontalBox();
		tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		tempBox.setMaximumSize(new Dimension(400, 40));
		tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		tempLabel = new JLabel(messages.getString("jmodemkonfiguration_msg4"));
		tempLabel.setPreferredSize(new Dimension(140, 10));
		tempBox.add(tempLabel);

		tfPort = new JTextField("1234");
		tfPort.setPreferredSize(new Dimension(100,18));
		tfPort.setText(""+((ModemFirmware)((Modem)holeHardware()).getSystemSoftware()).getPort());
		tempBox.add(tfPort);

		box.add(tempBox);


		tempBox = Box.createHorizontalBox();
		tempBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		tempBox.setMaximumSize(new Dimension(400, 40));
		tempBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		btStartStop = new JButton(messages.getString("jmodemkonfiguration_msg2"));
		btStartStop.setPreferredSize(new Dimension(300, 30));
		btStartStop.setActionCommand("ServerStarten");
		btStartStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ModemFirmware firmware = (ModemFirmware)((Modem)holeHardware()).getSystemSoftware();

				aenderungenAnnehmen();

				if (e.getActionCommand().equals("ClientStarten")) {
					firmware.starteClient();
				}
				else if (e.getActionCommand().equals("ServerStarten"))	{
					firmware.starteServer();
				}
				else if (e.getActionCommand().equals("Trennen")) {
					firmware.trennen();
				}

				updateAttribute();
			}
		});
		tempBox.add(btStartStop);

		box.add(tempBox);

		ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					aenderungenAnnehmen();
					updateAttribute();
				}
		};

		cbServerModus.addActionListener(al);
		name.addActionListener(al);
		tfIpAdresse.addActionListener(al);
		tfPort.addActionListener(al);

		updateAttribute();
	}

	public void updateAttribute() {
	  	Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JModemKonfiguration), updateAttribute()");
		Modem modem;
		ModemFirmware firmware;
		boolean aktiv;

		modem = (Modem)holeHardware();
		firmware = (ModemFirmware)modem.getSystemSoftware();
		name.setText(modem.getName());

		tfIpAdresse.setText(firmware.getIpAdresse());
		tfPort.setText(""+firmware.getPort());


		if (firmware.getMode() == ModemFirmware.CLIENT)
		{
			cbServerModus.setSelected(false);
			tfIpAdresse.setEnabled(true);
			btStartStop.setText(messages.getString("jmodemkonfiguration_msg5"));
			btStartStop.setPreferredSize(new Dimension(300, 30));
			btStartStop.setActionCommand("ClientStarten");
			aktiv = modem.istVerbindungAktiv();
		}
		else
		{
			cbServerModus.setSelected(true);
			tfIpAdresse.setEnabled(false);
			btStartStop.setText(messages.getString("jmodemkonfiguration_msg2"));
			btStartStop.setPreferredSize(new Dimension(300, 30));
			btStartStop.setActionCommand("ServerStarten");
			aktiv = firmware.istServerBereit();
		}

		if (aktiv) {
			btStartStop.setText(messages.getString("jmodemkonfiguration_msg6"));
			btStartStop.setActionCommand("Trennen");
			tfIpAdresse.setEnabled(false);
			tfPort.setEnabled(false);
			cbServerModus.setEnabled(false);
		}
		else {
			tfPort.setEnabled(true);
			cbServerModus.setEnabled(true);
		}
		GUIContainer.getGUIContainer().updateViewport();
	}

	public void update(Observable arg0, Object arg1) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (JModemKonfiguration), update("+arg0+","+arg1+")");
		updateAttribute();

//		if (arg1 != null) {
//			JOptionPane.showMessageDialog(this, arg1);
//		}
	}
}
