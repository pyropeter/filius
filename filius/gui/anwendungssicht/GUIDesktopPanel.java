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
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

import filius.Main;
import filius.gui.JBackgroundPanel;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.FiliusClassLoader;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.software.system.Betriebssystem;

public class GUIDesktopPanel extends JBackgroundPanel implements I18n, Observer {

	private static final long serialVersionUID = 1L;

	private filius.software.system.Betriebssystem betriebssystem;

	private HashMap<String, GUIApplicationWindow> laufendeAnwendung = new HashMap<String, GUIApplicationWindow>();

	private JBackgroundDesktopPane desktopPane = null;

	private JPanel iconPanel = null;

	private JPanel taskLeiste;

	private JLabel lbNetzwerk;

	private GUIInstallationsDialog installationsDialog = null;
	private GUINetworkWindow gnw;

	private String[] parameter = { "", "", "" };

	public GUIDesktopPanel(Betriebssystem betriebssystem) {
		super();
		this.setLayout(null);
		this.setPreferredSize(new Dimension(640, 480));
		this.setBounds(0, 0, 640, 480);
		this.setBackgroundImage("gfx/desktop/hintergrundbild.png");
		this.setVisible(true);
		this.setLayout(new BorderLayout());

		this.betriebssystem = betriebssystem;
		betriebssystem.addObserver(this);

		desktopPane = new JBackgroundDesktopPane();
		desktopPane.setBackgroundImage("gfx/desktop/hintergrundbild.png");

		this.add(this.desktopPane, BorderLayout.CENTER);

		iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		iconPanel.setBounds(0, 0, 640, 432);
		iconPanel.setOpaque(false);

		taskLeiste = new JPanel();

		taskLeiste.setBorder(BorderFactory.createEmptyBorder());
		Box boxTaskLeiste = Box.createHorizontalBox();
		boxTaskLeiste.setBorder(BorderFactory.createEmptyBorder());
		boxTaskLeiste.add(Box.createHorizontalStrut(600));
		taskLeiste.setBounds(0, 420, 640, 36);

		gnw = new GUINetworkWindow(this);

		lbNetzwerk = new JLabel(new ImageIcon(getClass().getResource("/gfx/desktop/netzwek_aus.png")));
		lbNetzwerk.addMouseListener(new MouseInputAdapter() {
		
			public void mousePressed(MouseEvent e) {
				{
					gnw.setVisible(true);
					try {
						gnw.setSelected(true);
						gnw.toFront();
					}
					catch (PropertyVetoException e1) {
						e1.printStackTrace(Main.debug);
					}
				}

			}
		});

		boxTaskLeiste.add(lbNetzwerk);

		taskLeiste.add(boxTaskLeiste);

		desktopPane.add(taskLeiste);
		desktopPane.add(iconPanel);
		desktopPane.validate();
		this.updateAnwendungen();
	}

	public void updateAnwendungen() {
		GUIDesktopIcon tmpLabel;
		LinkedList<?> softwareList = null;
		String softwareKlasse, guiKlassenName;
		HashMap<?, ?> tmpMap;
		Class<?> cl = null;
		GUIApplicationWindow tempWindow;

		try {
			softwareList = Information.getInformation().ladeProgrammListe();
		}
		catch (IOException e) {
			e.printStackTrace(Main.debug);
		}
		this.iconPanel.removeAll();

		tmpLabel = new GUIDesktopIcon(new ImageIcon(getClass().getResource("/gfx/desktop/icon_softwareinstallation.png")));
		tmpLabel.setAnwendungsName(messages.getString("desktoppanel_msg1"));
		tmpLabel.setInvokeName("Software-Installation");
		tmpLabel.setToolTipText(tmpLabel.getAnwendungsName());
		tmpLabel.setText(tmpLabel.getAnwendungsName());
		tmpLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		tmpLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		tmpLabel.setForeground(new Color(255, 255, 255));
		tmpLabel.setPreferredSize(new Dimension(120, 96));
		this.iconPanel.add(tmpLabel);

		ListIterator<?> it = ((softwareList != null) ? softwareList.listIterator() : null);
		while (it!=null && it.hasNext()) {

			tmpMap = (HashMap<?, ?>) it.next();
			softwareKlasse = (String) tmpMap.get("Klasse");
			if ((betriebssystem.holeSoftware(softwareKlasse) != null)) {
				if (softwareKlasse.equals((String) tmpMap.get("Klasse"))) {
					guiKlassenName = (String) tmpMap.get("GUI-Klasse");

					try {
						cl = Class.forName(guiKlassenName, true,
								FiliusClassLoader.getInstance(Thread
										.currentThread()
										.getContextClassLoader()));
					}
					catch (ClassNotFoundException e) {
						e.printStackTrace(Main.debug);
					}

					try {

						if (cl != null) {
							tempWindow = (GUIApplicationWindow) cl
									.getConstructor(GUIDesktopPanel.class,
											String.class).newInstance(this,
											softwareKlasse);

							tempWindow.setVisible(false);

							addLaufendeAnwendung(softwareKlasse, tempWindow);

							tmpLabel = new GUIDesktopIcon(new ImageIcon(getClass().getResource("/"+((String) tmpMap.get("gfxFile")))));

							tmpLabel.setAnwendungsName((String) tmpMap
									.get("Anwendung"));
							tmpLabel.setInvokeName((String) tmpMap
									.get("Klasse"));
							tmpLabel.setToolTipText(tmpLabel
									.getAnwendungsName());
							tmpLabel.setText(tmpLabel.getAnwendungsName());
							tmpLabel
									.setVerticalTextPosition(SwingConstants.BOTTOM);
							tmpLabel
									.setHorizontalTextPosition(SwingConstants.CENTER);
							tmpLabel.setForeground(new Color(255, 255, 255));
							tmpLabel.setPreferredSize(new Dimension(120, 96));
							this.iconPanel.add(tmpLabel);
						}

					}
					catch (Exception e) {
						e.printStackTrace(Main.debug);
					}
				}

			}
		}
		this.iconPanel.updateUI();

		NetzwerkInterface nic = (NetzwerkInterface) ((InternetKnoten) betriebssystem
				.getKnoten()).getNetzwerkInterfaces().getFirst();
		if (nic != null) {
			if (nic.getPort() != null) {
				if (nic.getPort().getVerbindung() != null) {
					nic.getPort().getVerbindung().addObserver(this);
					lbNetzwerk.setToolTipText("" + nic.getIp());
				}
			}
		}
		if (this.getParent() != null) {
			taskLeiste.setBounds(0, 424, 640,
					32 + this.getParent().getInsets().top);
		}
	}

	public GUIApplicationWindow starteAnwendung(String softwareKlasse,
			String[] param) {
		setParameter(param);
		return starteAnwendung(softwareKlasse);
	}

	public GUIApplicationWindow starteAnwendung(String softwareKlasse) {
		GUIApplicationWindow tempWindow = null;

		if (softwareKlasse.equals("Software-Installation")) {
			this.installationsDialog = new GUIInstallationsDialog(this);
			getDesktopPane().add(this.installationsDialog, 3);

			try {
				this.installationsDialog.setSelected(true);

			}
			catch (PropertyVetoException e) {
				e.printStackTrace(Main.debug);
			}
		}

		else if (getLaufendeAnwendungByName(softwareKlasse) != null) {
			tempWindow = getLaufendeAnwendungByName(softwareKlasse);

			tempWindow.updateUI();
			tempWindow.starten(parameter);
			tempWindow.show();

		}

		return tempWindow;
	}

	/**
	 * Fuegt der Hashmap laufendeAnwendung das Fenster der laufenden Anwendung
	 * hinzu, damit Fenster geschlossen und wieder geoeffnet werden koennen,
	 * ohne die Anwendung dafuer neu starten zu muessen.
	 *
	 * @author Thomas Gerding & Johannes Bade
	 * @param fenster
	 *            Das GUIApplicationWindow der Anwendung
	 * @param anwendungsName
	 *            Name der Anwendung
	 */
	private void addLaufendeAnwendung(String anwendungsName,
			GUIApplicationWindow fenster) {
		this.laufendeAnwendung.put(anwendungsName, fenster);
	}

	/**
	 * Gibt das GUIApplicationWindow einer Anwendung aus der HashMap
	 * laufendeAnwendung zurueck.
	 *
	 * @param anwendungsName
	 * @return Das GUIApplicationWindow der angeforderten Anwendung
	 */
	private GUIApplicationWindow getLaufendeAnwendungByName(
			String anwendungsName) {
		GUIApplicationWindow tmpFenster = null;

		tmpFenster = (GUIApplicationWindow) this.laufendeAnwendung
				.get(anwendungsName);

		return tmpFenster;
	}

	/*
	 * public LinkedList getIconListe() { return iconListe; }
	 */

	public filius.software.system.Betriebssystem getBetriebssystem() {
		return betriebssystem;
	}

	public JDesktopPane getDesktopPane() {
		return desktopPane;
	}

	public String[] getParameter() {
		return parameter;
	}

	public void setParameter(String[] parameter) {
		this.parameter = parameter;
	}

	public void update(Observable o, Object arg) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (GUIDesktopPanel), update("+o+","+arg+")");
		if (arg == null) {
			updateAnwendungen();
		}
		else if (arg.equals(Boolean.TRUE)) {
			lbNetzwerk.setIcon(new ImageIcon(getClass().getResource("/gfx/desktop/netzwek_c.png")));
		}
		else {
			lbNetzwerk.setIcon(new ImageIcon(getClass().getResource("/gfx/desktop/netzwek_aus.png")));
		}
	}
}
