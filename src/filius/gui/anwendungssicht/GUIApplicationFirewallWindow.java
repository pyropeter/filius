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
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;

import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;

import filius.Main;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.software.firewall.Firewall;
import filius.software.firewall.FirewallWebLog;
import filius.software.www.WebServer;
import filius.software.www.WebServerPlugIn;




/**
 * Applikationsfenster für den Datei-Importer
 *
 * @author Johannes Bade & Thomas Gerding
 *
 */
public class GUIApplicationFirewallWindow extends GUIApplicationWindow {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private JTable tTabellePort;

	private JTextField tfPort;

	private JComboBox cbAlleAbsender;

	private JTextArea log; //Log-Fenster

	private JCheckBox cbEinAus = new JCheckBox(); //Initial an oder ausgehakt?

	private void initKomponenten() {
		Box box;
		JEditorPane text;
		JLabel label;
		JButton button;
		TableColumnModel columnModel;
		JPanel panel;
		JScrollPane scrollPane;
		Box boxFirewall;
		JTabbedPane tp;

		boxFirewall = Box.createVerticalBox();
		boxFirewall.add(Box.createVerticalStrut(10));

		cbEinAus = new JCheckBox(messages.getString("firewall_msg1"));//Bei true nur mit 1 durchlassen
		cbEinAus.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent arg0) {
				((Firewall)holeAnwendung()).setAktiviert(cbEinAus.isSelected());
				updateAttribute();

			}
		});
		boxFirewall.add(cbEinAus);
		boxFirewall.add(Box.createVerticalStrut(10));

		text = new JEditorPane();
		text.setEditable(false);
		text.setText(messages.getString("firewall_msg2"));
		text.setBackground(boxFirewall.getBackground());

		box = Box.createHorizontalBox();
		box.add(Box.createHorizontalStrut(10));

		label = new JLabel(messages.getString("firewall_msg3"));
		box.add(label);
		box.add(Box.createHorizontalStrut(10));

		tfPort = new JTextField();
		tfPort.setPreferredSize(new Dimension(30, 15));
		box.add(tfPort);
		box.add(Box.createHorizontalStrut(10));

		cbAlleAbsender = new JComboBox();
		cbAlleAbsender.addItem(messages.getString("firewall_msg4"));
		cbAlleAbsender.addItem(messages.getString("firewall_msg5"));
		cbAlleAbsender.setSelectedIndex(0);
		box.add(cbAlleAbsender);
		box.add(Box.createHorizontalStrut(10));

		boxFirewall.add(box);
		boxFirewall.add(Box.createVerticalStrut(10));

		box = Box.createHorizontalBox();
		box.add(Box.createHorizontalStrut(10));

		button = new JButton(messages.getString("firewall_msg6"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hinzuRegel();
				updateAttribute();
			}
		});
		box.add(button);
		box.add(Box.createHorizontalStrut(10));

		button = new JButton(messages.getString("firewall_msg7"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loescheRegel();
				updateAttribute();
			}
		});
		box.add(button);
		box.add(Box.createHorizontalStrut(10));

		boxFirewall.add(box);
		boxFirewall.add(Box.createVerticalStrut(10));

		DefaultTableModel tabellenModell = new DefaultTableModel(0, 2);
		tTabellePort = new JTable(tabellenModell);
		tTabellePort.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tTabellePort.setIntercellSpacing(new Dimension(10,5));
		tTabellePort.setRowHeight(30);
		tTabellePort.setShowGrid(false);
		tTabellePort.setFillsViewportHeight(true);
		tTabellePort.setBackground(Color.WHITE);
		tTabellePort.setShowHorizontalLines(true);

		columnModel = tTabellePort.getColumnModel();
		columnModel.getColumn(0).setHeaderValue(messages.getString("firewall_msg8"));
		columnModel.getColumn(0).setWidth(20);
		columnModel.getColumn(1).setHeaderValue(messages.getString("firewall_msg9"));
		columnModel.getColumn(1).setWidth(300);

		scrollPane = new JScrollPane(tTabellePort);
		scrollPane.setPreferredSize(new Dimension(90,250));

		boxFirewall.add(scrollPane);
		boxFirewall.add(Box.createVerticalStrut(10));

		tp = new JTabbedPane();
		tp.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent arg0) {
				updateAttribute();
			}
		});

		panel = new JPanel(new BorderLayout());
		panel.add(boxFirewall, BorderLayout.CENTER);
		tp.add(messages.getString("firewall_msg10"), panel);

		log = new JTextArea();
		scrollPane = new JScrollPane(log);
		tp.add(messages.getString("firewall_msg11"), scrollPane);

		getContentPane().add(tp);
		pack();
	}

	private void hinzuRegel() {
		boolean unterscheideNetzwerk;
		int port;

		try {
			port = Integer.parseInt(tfPort.getText());
			unterscheideNetzwerk = (cbAlleAbsender.getSelectedIndex() == 1);

			((Firewall)holeAnwendung()).eintragHinzufuegenPort(""+port, unterscheideNetzwerk);
			tfPort.setText("");
		}
		catch (Exception e) { }
		updateAttribute();
	}

	private void loescheRegel() {
		if (tTabellePort.getSelectedRow() != -1) {
			((Firewall)holeAnwendung()).entferneRegelPort(tTabellePort.getSelectedRow());
		}
		updateAttribute();
	}

	/*
	 * @author Weyer
	 * Im Konstruktor werden alle Dinge erzeugt, die in der GUI angezeigt werden muessen
	 */
	public GUIApplicationFirewallWindow(final GUIDesktopPanel desktop, String appName){

		super(desktop, appName);

		//setAnwendungsIcon("gfx/desktop/icon_firewall.png");
		initKomponenten();
		updateAttribute();
	}

	/*
	 * @author Weyer
	 * bringt das aktuell angezeigte Fenster immer auf den neuesten Stand mit aktuellen Daten
	 */
	public void updateAttribute() {
		LinkedList portList;
		Vector<String> vector;
		DefaultTableModel model;

		portList = ((Firewall)holeAnwendung()).getPortList();

		model = (DefaultTableModel)tTabellePort.getModel();
		model.setRowCount(0);

//		Main.debug.println("Portliste mit "+portList.size()+" Eintraegen");
		for (int i=0; i<portList.size(); i++) {
			vector = new Vector<String>();
			vector.addElement((String)((Object[])portList.get(i))[0]);
			if ((Boolean)((Object[])portList.get(i))[1]) {
				vector.addElement(messages.getString("firewall_msg12"));
			}
			else {
				vector.addElement(messages.getString("firewall_msg4"));
			}
			model.addRow(vector);
		}

		if(((Firewall)holeAnwendung()).isAktiviert()){
			cbEinAus.setSelected(true);
		}
		else{
			cbEinAus.setSelected(false);
		}
	}

	public void update(Observable arg0, Object nachricht) {
		log.append(nachricht+"\n");
	}




}
