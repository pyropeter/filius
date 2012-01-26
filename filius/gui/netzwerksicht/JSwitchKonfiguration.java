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
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import filius.gui.GUIContainer;
import filius.hardware.Hardware;
import filius.hardware.knoten.Switch;
import filius.rahmenprogramm.I18n;

public class JSwitchKonfiguration extends JKonfiguration implements I18n {

	private static final long serialVersionUID = 1L;
	private JTextField name; // Name,Name,20,String,editable,Neuer
	private JCheckBox checkCloud;

	protected JSwitchKonfiguration(Hardware hardware) {
		super(hardware);
	}

	public void aenderungenAnnehmen() {
		((Switch)holeHardware()).setName(name.getText());

		GUIContainer.getGUIContainer().updateViewport();
		updateAttribute();
	}

	public void changeAppearance() {
		filius.Main.debug.println("DEBUG: changeAppearance invoked for Switch");
		if (checkCloud.isSelected()) {
			GUIContainer.getGUIContainer().getLabelforKnoten(((Switch)holeHardware())).setIcon(new ImageIcon(getClass().getResource("/"+GUISidebar.SWITCH_CLOUD)));
			((Switch)holeHardware()).setCloud(true);
		}
		else {
			GUIContainer.getGUIContainer().getLabelforKnoten(((Switch)holeHardware())).setIcon(new ImageIcon(getClass().getResource("/"+GUISidebar.SWITCH)));
			((Switch)holeHardware()).setCloud(false);
		}
	}
	
	@Override
	protected void initAttributEingabeBox(Box box) {
		JLabel tempLabel;
		Box tempBox;
		Box tempBox2;
		FocusListener focusListener;
		ActionListener actionListener;
		ItemListener itemListener;

		actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				aenderungenAnnehmen();
			}
		};
		itemListener = new ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				changeAppearance();
			}
		};
		focusListener = new FocusListener() {

			public void focusGained(FocusEvent arg0) {	}

			public void focusLost(FocusEvent arg0) {
				aenderungenAnnehmen();
			}

		};


		tempLabel = new JLabel(messages.getString("jswitchkonfiguration_msg1"));
		tempLabel.setPreferredSize(new Dimension(140, 10));
		tempLabel.setVisible(true);
		tempLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		checkCloud = new JCheckBox(messages.getString("jswitchkonfiguration_msg3"));
		checkCloud.setPreferredSize(new Dimension(160,10));
		checkCloud.setVisible(true);
//		checkCloud.setAlignmentX(Component.RIGHT_ALIGNMENT);
		checkCloud.addItemListener(itemListener);
		
		name = new JTextField(messages.getString("jswitchkonfiguration_msg2"));
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
		tempBox2 = Box.createVerticalBox();
		tempBox2.add(tempBox);
		tempBox2.add(checkCloud);
		box.add(tempBox2, BorderLayout.NORTH);
	}

	@Override
	public void updateAttribute() {
		name.setText(((Switch)holeHardware()).getName());
		checkCloud.setSelected(((Switch)holeHardware()).isCloud());
	}

}
