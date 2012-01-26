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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import filius.Main;
import filius.software.www.WebServer;

public class GUIApplicationWebServerWindow extends GUIApplicationWindow {
	
	private static final int  VHOST_NUMBER=5;
	
	private static final long serialVersionUID = 1L;

	private JPanel backPanel;

	private JTextArea logArea;

	private JButton buttonStart;
	
	private JCheckBox showVHosts;
	private JTableEditable vHostTable;
	private Box vHostBox = null, logBox = null;

	public GUIApplicationWebServerWindow(final GUIDesktopPanel desktop,
			String appName) {
		super(desktop, appName);
		Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+", constr: GUIApplicationWebServerWindow("+desktop+","+appName+")");
		this.setResizable(false);
		this.setMaximizable(false);

		backPanel = new JPanel(new BorderLayout());

		buttonStart = new JButton(messages.getString("webserver_msg1"));
		buttonStart.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (buttonStart.getText().equals(
						messages.getString("webserver_msg1"))) {
					((WebServer) holeAnwendung()).setAktiv(true);
				}
				else {
					((WebServer) holeAnwendung()).setAktiv(false);
				}
			}
		});
		Box upperBox = Box.createHorizontalBox();
		upperBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		upperBox.add(buttonStart);
		upperBox.add(Box.createHorizontalStrut(100));
		
		showVHosts = new JCheckBox();
		showVHosts.setSelected(this.isVHostAvailable());
		showVHosts.setText(messages.getString("webserver_msg6"));
		showVHosts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
	            JCheckBox checkBox = (JCheckBox) evt.getSource();
	            GUIApplicationWebServerWindow.this.setVHostTableVisible(checkBox.isSelected());
	            if (!checkBox.isSelected()) {
	            	((WebServer) holeAnwendung()).resetVHosts();
	            }
            }});
		upperBox.add(showVHosts);
		
		backPanel.add(upperBox, BorderLayout.NORTH);

		vHostBox = Box.createVerticalBox();
		vHostBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JLabel label = new JLabel(messages.getString("webserver_msg3"));
		Box vHostLabel = Box.createHorizontalBox();
		vHostLabel.add(label);
		
		DefaultTableModel tablemodel = new DefaultTableModel(VHOST_NUMBER,2);
		vHostTable = new JTableEditable(tablemodel, true, "WWW");
		vHostTable.setParentGUI(this);
		JScrollPane tableScrollPane = new JScrollPane(vHostTable); // necessary for table headers...
		Main.debug.println("DEBUG message webserver_msg4='"+messages.getString("webserver_msg4")+"'");
		vHostTable.getColumnModel().getColumn(0).setHeaderValue(messages.getString("webserver_msg4"));
		vHostTable.getColumnModel().getColumn(1).setHeaderValue(messages.getString("webserver_msg5"));
		Main.debug.println("DEBUG header value: '"+
		   vHostTable.getColumnModel().getColumn(0).getHeaderValue()+"'");
		vHostTable.setIntercellSpacing(new java.awt.Dimension(5,5));
		vHostTable.setRowHeight(28);
		vHostTable.setShowGrid(true);
		vHostTable.setFillsViewportHeight(true);
        vHostTable.setBackground(java.awt.Color.WHITE);
        vHostTable.setShowHorizontalLines(true);
		vHostBox.add(vHostLabel);
		vHostBox.add(Box.createVerticalStrut(5));
		vHostBox.add(tableScrollPane);
				
		logArea = new JTextArea();
		logArea.setEditable(false);
		JScrollPane sPane = new JScrollPane(logArea);

		logBox = Box.createHorizontalBox();
		logBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		logBox.add(sPane);
		
		backPanel.add(logBox, BorderLayout.SOUTH);

		this.getContentPane().add(backPanel);
		this.setClosable(true);

		this.setVHostTableVisible(this.isVHostAvailable());

		aktualisieren();
	}
	
	private void setVHostTableVisible(boolean tableVisible) {
		if (tableVisible) {
			backPanel.add(vHostBox, BorderLayout.CENTER);
			logBox.setPreferredSize(new java.awt.Dimension(300,150));
		}
		else {
			backPanel.remove(vHostBox);
			logBox.setPreferredSize(new java.awt.Dimension(300,350));
		}
		pack();
	}
	
	private boolean isVHostAvailable() {
		boolean vHostsAvailable = false;
		String[][] vHosts = ((WebServer) holeAnwendung()).getVHostArray();
		for (String[] vHost : vHosts) {
			if (vHost[0] != null && !vHost[0].equals("") || vHost[1] != null && !vHost[1].equals("")) {
				vHostsAvailable = true;
			}
		}
		return vHostsAvailable;
	}

	public void updateTable() {
		Main.debug.println("DEBUG GUIApplicationWebServerWindow, updateTable; vHostArray:\n"+((WebServer)holeAnwendung()).printVHostTable());
		String[][] vhosts = ((WebServer) holeAnwendung()).getVHostArray();
		DefaultTableModel tablemodel = (DefaultTableModel) vHostTable.getModel();
		tablemodel.setRowCount(0);
		
		for(int i=0; i<vhosts.length; i++) {
			Vector<Comparable<String>> v = new Vector<Comparable<String>>();
			if(vhosts[i][0]!=null) v.add(vhosts[i][0]);
			else v.add("");
			if(vhosts[i][1]!=null) v.add(vhosts[i][1]);
			else v.add("");
			tablemodel.addRow(v);
		}
		for (int i=vhosts.length; i<VHOST_NUMBER; i++) {
			Vector<Comparable<String>> v = new Vector<Comparable<String>>();
			v.add("");
			v.add("");
			tablemodel.addRow(v);
		}
	}
	
	private void aktualisieren() {
		if (((WebServer) holeAnwendung()).isAktiv()) {
			buttonStart.setText(messages.getString("webserver_msg2"));
		}
		else {
			buttonStart.setText(messages.getString("webserver_msg1"));
		}
		buttonStart.setEnabled(true);
		updateTable();
	}

	public void update(Observable arg0, Object arg1) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (GUIApplicationWebServerWindow), update("+arg0+","+arg1+")");
		if (logArea != null && arg1 != null) {
			logArea.append(arg1.toString() + "\n");
		}

		try {
			aktualisieren();
		}
		catch (Exception e) {
			Main.debug.println("GUIApplicationWebServerWindow: update() Exception: " + e.getMessage());
		}

		Main.debug
				.println("GUIApplicationWebServerWindow: update() aufgerufen.");
	}
}
