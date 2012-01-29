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
package filius.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ListIterator;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import filius.Main;
import filius.gui.nachrichtensicht.LauscherDialog;
import filius.gui.netzwerksicht.GUIKnotenItem;
import filius.gui.netzwerksicht.GUIKabelItem;
import filius.gui.netzwerksicht.JVermittlungsrechnerKonfiguration;
import filius.gui.quelltextsicht.FrameSoftwareWizard;
import filius.hardware.Verbindung;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.rahmenprogramm.SzenarioVerwaltung;
import filius.software.system.SystemSoftware;
import filius.software.system.Betriebssystem;
import filius.software.dhcp.DHCPServer;


public class GUIMainMenu implements Serializable, I18n {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static final int MODUS_ENTWURF = 1;

	public static final int MODUS_AKTION = 2;

	public static final int MODUS_FEHLER = 9;

	private JBackgroundPanel menupanel;

	private JSlider verzoegerung;

	private FileFilter filiusFileFilter;

	private JLabel geschwindigkeit;

	private int aktuellerModus = MODUS_ENTWURF;

	private JButton btAktionsmodus, btEntwurfsmodus, btOeffnen, btSpeichern,
			btNeu, btWizard, btHilfe, btInfo, btRTT;
	
	private LinkedList<DHCPServer> listDHCPServers = new LinkedList<DHCPServer>();

	private static final int SLIDER = 6;
	private int sliderToDelay(int val) {
		return (int)Math.pow(val, 2);
	}

	public GUIMainMenu() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (GUIMainMenu), constr: GUIMainMenu()");
		Container c = JMainFrame.getJMainFrame().getContentPane();

		menupanel = new JBackgroundPanel();
		menupanel.setPreferredSize(new Dimension(100, 63));
		menupanel.setBounds(0, 0, c.getWidth(), 65);
		menupanel.setEnabled(false);
		menupanel.setBackgroundImage("gfx/allgemein/menue_hg.png");

		btOeffnen = new JButton();
		btOeffnen.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/oeffnen.png")));
		btOeffnen.setBounds(80, 5, btOeffnen.getIcon().getIconWidth(),
				btOeffnen.getIcon().getIconHeight());
		btOeffnen.setActionCommand("oeffnen");
		btOeffnen.setToolTipText(messages.getString("guimainmemu_msg1"));

		btSpeichern = new JButton();
		btSpeichern.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/speichern.png")));
		btSpeichern.setBounds(150, 5, btSpeichern.getIcon().getIconWidth(),
				btSpeichern.getIcon().getIconHeight());
		btSpeichern.setActionCommand("speichern");
		btSpeichern.setToolTipText(messages.getString("guimainmemu_msg2"));

		btEntwurfsmodus = new JButton();
		btEntwurfsmodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/entwurfsmodus_aktiv.png")));
		btEntwurfsmodus.setBounds(290, 5, btEntwurfsmodus.getIcon()
				.getIconWidth(), btEntwurfsmodus.getIcon().getIconHeight());
		btEntwurfsmodus.setActionCommand("entwurfsmodus");
		btEntwurfsmodus.setToolTipText(messages.getString("guimainmemu_msg3"));

		btAktionsmodus = new JButton();
		btAktionsmodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/aktionsmodus.png")));
		btAktionsmodus.setBounds(360, 5, btAktionsmodus.getIcon().getIconWidth(), btAktionsmodus.getIcon().getIconHeight());
		btAktionsmodus.setActionCommand("aktionsmodus");
		btAktionsmodus.setToolTipText(messages.getString("guimainmemu_msg4"));

		btNeu = new JButton();
		btNeu.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/neu.png")));
		btNeu.setBounds(10, 5, btNeu.getIcon().getIconWidth(), btNeu.getIcon()
				.getIconHeight());
		btNeu.setActionCommand("neu");
		btNeu.setToolTipText(messages.getString("guimainmemu_msg5"));

		btRTT = new JButton();
		btRTT.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/rtt_norm.png")));
		btRTT.setBounds(600, 5, btRTT.getIcon().getIconWidth(), btRTT.getIcon().getIconHeight());
		btRTT.setActionCommand("rtt");
		btRTT.setToolTipText(messages.getString("guimainmenu_msg14"));

		btWizard = new JButton();
		btWizard.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/button_wizard.png")));
		btWizard.setBounds(750, 5, btWizard.getIcon().getIconWidth(), btWizard
				.getIcon().getIconHeight());
		btWizard.setActionCommand("wizard");
		btWizard.setToolTipText(messages.getString("guimainmemu_msg6"));

		btHilfe = new JButton();
		btHilfe.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/hilfe.png")));
		btHilfe.setBounds(840, 5, btHilfe.getIcon().getIconWidth(), btHilfe
				.getIcon().getIconHeight());
		btHilfe.setActionCommand("hilfe");
		btHilfe.setToolTipText(messages.getString("guimainmemu_msg7"));

		btInfo = new JButton();
		btInfo.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/info.png")));
		btInfo.setBounds(910, 5, btInfo.getIcon().getIconWidth(), btInfo
				.getIcon().getIconHeight());
		btInfo.setActionCommand("info");
		btInfo.setToolTipText(messages.getString("guimainmemu_msg8"));

		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int entscheidung = JOptionPane.YES_OPTION;
				boolean erfolg;

				if (e.getActionCommand().equals(btWizard.getActionCommand())) {
					FrameSoftwareWizard gsw = new FrameSoftwareWizard();
					gsw.setVisible(true);
				}

				if (e.getActionCommand().equals(btHilfe.getActionCommand())) {
					GUIHilfe.getGUIHilfe().anzeigen();
				}

				if (e.getActionCommand().equals(btNeu.getActionCommand())) {
					try {
						if (SzenarioVerwaltung.getInstance().istGeaendert()) {
							entscheidung = JOptionPane.showConfirmDialog(
									JMainFrame.getJMainFrame(), messages
											.getString("guimainmemu_msg9"),
									messages.getString("guimainmemu_msg10"),
									JOptionPane.YES_NO_OPTION);
						} else {
							entscheidung = JOptionPane.YES_OPTION;
						}
					} catch (Exception exc) {
						exc.printStackTrace(Main.debug);
					}
					if (entscheidung == JOptionPane.YES_OPTION) {
						GUIContainer.getGUIContainer().clearAllItems();
						GUIContainer.getGUIContainer().setProperty(null);
						Information.getInformation().reset();
						SzenarioVerwaltung.getInstance().reset();
					}
				}

				if (e.getActionCommand().equals(btSpeichern.getActionCommand())) {
					if (GUIContainer.getGUIContainer().getActiveSite() == MODUS_ENTWURF) {
						JFileChooser fcSpeichern = new JFileChooser();
						String path;
						File file;

						fcSpeichern.setFileFilter(filiusFileFilter);
						path = SzenarioVerwaltung.getInstance().holePfad();
						if (path != null) {
							file = new File(path);
							if (file.exists())
								fcSpeichern.setSelectedFile(file);
						}

						if (fcSpeichern.showSaveDialog(JMainFrame
								.getJMainFrame()) == JFileChooser.APPROVE_OPTION) {
							if (fcSpeichern.getSelectedFile() != null) {
								if (fcSpeichern.getSelectedFile().getName()
										.endsWith(".fls")) {
									erfolg = SzenarioVerwaltung
											.getInstance()
											.speichern(
													fcSpeichern
															.getSelectedFile()
															.getPath(),
													GUIContainer
															.getGUIContainer()
															.getGUIKnotenItemList(),
													GUIContainer
															.getGUIContainer()
															.getCablelist());
								} else {
									erfolg = SzenarioVerwaltung
											.getInstance()
											.speichern(
													fcSpeichern
															.getSelectedFile()
															.getPath()
															+ ".fls",
													GUIContainer
															.getGUIContainer()
															.getGUIKnotenItemList(),
													GUIContainer
															.getGUIContainer()
															.getCablelist());
								}
								if (!erfolg) {
									JOptionPane.showMessageDialog(JMainFrame
											.getJMainFrame(), messages
											.getString("guimainmemu_msg11"));
								}
							}
						}
					}
				}

				if (e.getActionCommand().equals(btOeffnen.getActionCommand())) {
					try {
						if (SzenarioVerwaltung.getInstance().istGeaendert()) {
							entscheidung = JOptionPane.showConfirmDialog(
									JMainFrame.getJMainFrame(), messages
											.getString("guimainmemu_msg9"),
									messages.getString("guimainmemu_msg10"),
									JOptionPane.YES_NO_OPTION);
						} else {
							entscheidung = JOptionPane.YES_OPTION;
						}
					} catch (Exception exc) {
						exc.printStackTrace(Main.debug);
					}
					if (entscheidung == JOptionPane.YES_OPTION
							&& GUIContainer.getGUIContainer().getActiveSite() == MODUS_ENTWURF) {
						JFileChooser fcLaden = new JFileChooser();
						String path;
						File file;
						fcLaden.setFileFilter(filiusFileFilter);
						path = SzenarioVerwaltung.getInstance().holePfad();
						if (path != null) {
							file = new File(path);
							if (file.exists())
								fcLaden.setSelectedFile(file);
						}

						if (fcLaden.showOpenDialog(JMainFrame.getJMainFrame()) == JFileChooser.APPROVE_OPTION) {

							if (fcLaden.getSelectedFile() != null) {

								try {
									Information.getInformation().reset();
									SzenarioVerwaltung
											.getInstance()
											.laden(
													fcLaden.getSelectedFile()
															.getPath(),
													GUIContainer
															.getGUIContainer()
															.getGUIKnotenItemList(),
													GUIContainer
															.getGUIContainer()
															.getCablelist());
									GUIContainer.getGUIContainer().setProperty(
											null);
									GUIContainer.getGUIContainer().updateViewport();
									Thread.sleep(10);
									GUIContainer.getGUIContainer().updateCables();
								} catch (FileNotFoundException e1) {
									e1.printStackTrace(Main.debug);
								} catch (Exception e2) {
									e2.printStackTrace(Main.debug);
								}
							}

						}
					}
				}

				if (e.getActionCommand().equals(btRTT.getActionCommand())) {
					rotateRTT();
				}
				
				if (e.getActionCommand().equals(
						btEntwurfsmodus.getActionCommand())) {
					selectMode(MODUS_ENTWURF);
				}

				if (e.getActionCommand().equals(
						btAktionsmodus.getActionCommand())) {
					selectMode(MODUS_AKTION);

				}

				if (e.getActionCommand().equals(btInfo.getActionCommand())) {
					(new InfoDialog(JMainFrame.getJMainFrame()))
							.setVisible(true);
				}
			}
		};

		btNeu.addActionListener(al);
		btOeffnen.addActionListener(al);
		btSpeichern.addActionListener(al);
		btEntwurfsmodus.addActionListener(al);
		btAktionsmodus.addActionListener(al);
		btWizard.addActionListener(al);
		btRTT.addActionListener(al);
		btInfo.addActionListener(al);
		btHilfe.addActionListener(al);

		geschwindigkeit = new JLabel("" + sliderToDelay(SLIDER) + " ms");
		geschwindigkeit.setVisible(true);
		geschwindigkeit.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
		geschwindigkeit.setBounds(552, 10, 120, 44);

		verzoegerung = new JSlider(2, 20);
		verzoegerung.setValue(SLIDER);
		Verbindung.setzeVerzoegerung(sliderToDelay(verzoegerung.getValue()));
		verzoegerung.setBounds(450, 10, 100, 44);
		verzoegerung.setOpaque(false);
		verzoegerung.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				int delay = sliderToDelay(verzoegerung.getValue());
				Verbindung.setzeVerzoegerung(delay);
				geschwindigkeit.setText("" + delay + " ms");
			}
		});

		menupanel.setLayout(null);

		menupanel.add(btEntwurfsmodus);
		menupanel.add(btAktionsmodus);
		menupanel.add(btNeu);
		menupanel.add(btOeffnen);
		menupanel.add(btSpeichern);
		menupanel.add(verzoegerung);
		menupanel.add(geschwindigkeit);
		menupanel.add(btRTT);
		menupanel.add(btWizard);
		menupanel.add(btHilfe);
		menupanel.add(btInfo);

		filiusFileFilter = new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.isDirectory())
					return true;
				return pathname.getName().toLowerCase().endsWith(".fls");
			}

			public String getDescription() {
				return messages.getString("guimainmemu_msg13");
			}
		};
	}

	private void rotateRTT() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (GUIMainMenu), rotateRTT()");
		int currRTT = Verbindung.getRTTfactor();
		Main.debug.println("DEBUG ("+this.hashCode()+") "+getClass()+" (GUIMainMenu), rotateRTT:  currRTT="+currRTT);
		if(currRTT == 1) {
			Verbindung.setRTTfactor(2);
			btRTT.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/rtt_slow2.png")));
		}
		else if(currRTT == 2) {
			Verbindung.setRTTfactor(5);
			btRTT.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/rtt_slow5.png")));
		}
		else {
			Verbindung.setRTTfactor(1);
			btRTT.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/rtt_norm.png")));
		}
	}
	
	public void changeSlider(int diff) {
		verzoegerung.setValue(verzoegerung.getValue() + diff);
	}
	
	public boolean doClick(String button) {   // manually perform click event on a registered button
		if (button.equals("btAktionsmodus"))	   btAktionsmodus.doClick();
		else if (button.equals("btEntwurfsmodus")) btEntwurfsmodus.doClick();
		else if (button.equals("btOeffnen"))       btOeffnen.doClick();
		else if (button.equals("btSpeichern"))     btSpeichern.doClick();
		else if (button.equals("btNeu"))           btNeu.doClick();
		else if (button.equals("btRTT"))           btRTT.doClick();
		else if (button.equals("btWizard"))        btWizard.doClick();
		else if (button.equals("btHilfe"))         btHilfe.doClick();
		else if (button.equals("btInfo"))          btInfo.doClick();
		else return false;
	    return true;
	}
	
	// set/reset cable highlight, i.e., make all cables normal coloured for simulation 
	// and possibly highlight in development view
	private void resetCableHL(int mode) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (GUIMainMenu), resetCableHL("+mode+")");
		if(mode == MODUS_AKTION) {  // change to simulation view: de-highlight all cables
			ListIterator it = GUIContainer.getGUIContainer().getCablelist().listIterator();
			while (it.hasNext()) {
				((GUIKabelItem) it.next()).getDasKabel().setAktiv(false);
			}
		}
		else {  // change to development view: possibly highlight a cable (only for 'Vermittlungsrechner' configuration
			if(GUIContainer.getGUIContainer().getProperty() instanceof JVermittlungsrechnerKonfiguration) {
				((JVermittlungsrechnerKonfiguration) GUIContainer.getGUIContainer().getProperty()).highlightConnCable();
			}
		}
	}
	
	public void selectMode(int mode) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (GUIMainMenu), selectMode("+mode+")");
		ListIterator it;
		GUIKnotenItem tmpGUIItem;

		if (mode == MODUS_ENTWURF && aktuellerModus != MODUS_ENTWURF) {
			//Main.debug.println("\tMode: MODUS_ENTWURF");
			resetCableHL(mode);  // de-highlight cables
			
			btEntwurfsmodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/entwurfsmodus_aktiv.png")));
			btAktionsmodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/aktionsmodus.png")));
			GUIContainer.getGUIContainer().setActiveSite(MODUS_ENTWURF);

			GUIHilfe.getGUIHilfe().laden("entwurfsmodus");

			it = GUIContainer.getGUIContainer().getGUIKnotenItemList()
					.listIterator();
			while (it.hasNext()) {
				tmpGUIItem = (GUIKnotenItem) it.next();

				SystemSoftware system;
				system = tmpGUIItem.getKnoten().getSystemSoftware();
				try {
				system.beenden();
				}
				catch (Exception e) {}
			}

			btOeffnen.setEnabled(true);
			btNeu.setEnabled(true);
			btSpeichern.setEnabled(true);
			btWizard.setEnabled(true);

			LauscherDialog.getLauscherDialog(JMainFrame.getJMainFrame())
					.setVisible(false);

			aktuellerModus = mode;
		}

		else if (mode == MODUS_AKTION && aktuellerModus != MODUS_AKTION) {
			//Main.debug.println("\tMode: MODUS_AKTION");
			resetCableHL(mode);  // de-highlight cables

			btEntwurfsmodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/entwurfsmodus.png")));
			btAktionsmodus.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/aktionsmodus_aktiv.png")));
			GUIContainer.getGUIContainer().setActiveSite(MODUS_AKTION);
			GUIHilfe.getGUIHilfe().laden("simulationsmodus");

			// find all DHCP servers for delaying run-time start until servers are ready  
			it = GUIContainer.getGUIContainer().getGUIKnotenItemList().listIterator();
			SystemSoftware syssoft;
			while (it.hasNext()) {
				syssoft = ((GUIKnotenItem) it.next()).getKnoten().getSystemSoftware(); 
				if (syssoft instanceof Betriebssystem) {
				  if (((Betriebssystem) syssoft).getDHCPServer().isAktiv()) {
					  //Main.debug.println("--DHCP-- found DHCP server in '"+syssoft.getKnoten().getName()+"'");
				      listDHCPServers.add(((Betriebssystem) syssoft).getDHCPServer());
				  }
				}
			}
			
			it = GUIContainer.getGUIContainer().getGUIKnotenItemList().listIterator();
			while (it.hasNext()) {
				tmpGUIItem = (GUIKnotenItem) it.next();

				SystemSoftware system;
				system = tmpGUIItem.getKnoten().getSystemSoftware();
				system.starten();

				GUIContainer.getGUIContainer().addDesktopWindow(tmpGUIItem);
			}

			btOeffnen.setEnabled(false);
			btNeu.setEnabled(false);
			btSpeichern.setEnabled(false);
			btWizard.setEnabled(false);

			geschwindigkeit.setEnabled(true);
			verzoegerung.setEnabled(true);

			aktuellerModus = mode;
		}

	}
	
	public LinkedList<DHCPServer> getDHCPservers() {
		return listDHCPServers;
	}

	public JBackgroundPanel getMenupanel() {
		return menupanel;
	}

	public void setMenupanel(JBackgroundPanel menupanel) {
		this.menupanel = menupanel;
	}
}
