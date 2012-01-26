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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.MouseInputAdapter;

import filius.Main;
import filius.gui.anwendungssicht.GUIDesktopWindow;
import filius.gui.nachrichtensicht.LauscherDialog;
import filius.gui.netzwerksicht.GUIDraftPanel;
import filius.gui.netzwerksicht.GUIKabelItem;
import filius.gui.netzwerksicht.GUIKnotenItem;
import filius.gui.netzwerksicht.GUISidebar;
import filius.gui.netzwerksicht.GUISimulationPanel;
import filius.gui.netzwerksicht.JCablePanel;
import filius.gui.netzwerksicht.JHostKonfiguration;
import filius.gui.netzwerksicht.JKonfiguration;
import filius.gui.netzwerksicht.JModemKonfiguration;
import filius.gui.netzwerksicht.JSidebarButton;
import filius.gui.netzwerksicht.JSwitchKonfiguration;
import filius.gui.netzwerksicht.JVermittlungsrechnerKonfiguration;
import filius.hardware.Kabel;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Rechner;
import filius.hardware.knoten.Switch;
import filius.hardware.knoten.Vermittlungsrechner;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.SzenarioVerwaltung;
import filius.software.system.Betriebssystem;

public class GUIContainer implements Serializable, I18n {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static GUIContainer ref;

	private GUIMainMenu menu;

	private JKonfiguration property;

	private LinkedList<GUIDesktopWindow> desktopWindowList = new LinkedList<GUIDesktopWindow>();

	private GUISidebar sidebar;

	private GUIDraftPanel draftpanel;

	private GUISimulationPanel simpanel;

	private JSidebarButton dragVorschau, kabelvorschau, testlabel;

	private JScrollPane scrollPane, sidebarScrollpane;

	private JSidebarButton ziel2Label;

	private JCablePanel kabelPanelVorschau;

	/** covered area during mouse pressed; visual representation of this area */
	private static JMarkerPanel auswahl;

	/** actual area containing selected objects */
	private static JMarkerPanel markierung;

	private int activeSite = 1; // enthält einen Integerwert dafür welche
	// Ansicht gerade aktiv ist

	public static final int FLAECHE_BREITE = 2000;

	public static final int FLAECHE_HOEHE = 1500;

	int abstandy, abstandx;

	private LinkedList<Object> itemlist;
	private LinkedList<GUIKabelItem> cablelist;

	/* HashMap in der die Hardware gespeichert wird */
	/*
	 * private HashMap hardwarelist;
	 * 
	 * public HashMap getHardwarelist() { return hardwarelist; } public void
	 * setHardwarelist(HashMap hardwarelist) { this.hardwarelist = hardwarelist;
	 * }
	 */

	public LinkedList<Object> getGUIKnotenItemList() {
		return itemlist;
	}

	public JSidebarButton getZiel2Label() {
		return ziel2Label;
	}

	public void setZiel2Label(JSidebarButton ziel2Label) {
		this.ziel2Label = ziel2Label;
	}

	public void nachrichtenDialogAnzeigen() {
		LauscherDialog.getLauscherDialog(JMainFrame.getJMainFrame())
				.setVisible(true);
	}

	/**
	 * 
	 * 
	 * Die Prozedur wird erst aufgerufen wenn der Container c zugewiesen wurde.
	 * Sie Initialisiert die einzelnen Panels und weist die ActionListener zu.
	 * 
	 * 
	 * FIXME -> Zuviel geschieht in dieser Prozedur? FIXME -> ActionListener
	 * irgendwie auslagern?
	 * 
	 * @author Johannes Bade & Thomas Gerding
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * 
	 */
	public void initialisieren() // throws FileNotFoundException, IOException
	{
		Container c;
		JLayeredPane layeredpane;

		c = JMainFrame.getJMainFrame().getContentPane();
		layeredpane = JMainFrame.getJMainFrame().getRootPane().getLayeredPane();

		/* auswahl: area covered during mouse pressed, i.e., area with components to be selected */
		auswahl = new JMarkerPanel();
		auswahl.setBounds(0, 0, 0, 0);
		auswahl.setBackgroundImage("gfx/allgemein/auswahl.png");
		auswahl.setOpaque(false);
		auswahl.setVisible(true);

		layeredpane.add(auswahl);

		/* markierung: actual area covering selected objects */
		markierung = new JMarkerPanel();
		markierung.setBounds(0, 0, 0, 0);
		markierung.setBackgroundImage("gfx/allgemein/markierung.png");
		markierung.setOpaque(false);
		markierung.setVisible(false);
		markierung.setCursor(new Cursor(Cursor.MOVE_CURSOR));

		/*
		 * Kabelvorschau wird erstellt und dem Container hinzugefuegt. Wird
		 * Anfangs auf Invisible gestellt, und nur bei Verwendung sichtbar
		 * gemacht.
		 */
		kabelvorschau = new JSidebarButton("", new ImageIcon(getClass().getResource("/gfx/allgemein/ziel1.png")), null);
		kabelvorschau.setVisible(false);
		layeredpane.add(kabelvorschau);

		cablelist = new LinkedList<GUIKabelItem>();
		/*
		 * Hauptmenü wird erstellt und dem Container hinzugefügt
		 */
		menu = new GUIMainMenu();
		c.setLayout(new BorderLayout(0, 0));
		c.add(menu.getMenupanel(), BorderLayout.NORTH);

		setProperty(null);

		/* sidebar wird erstellt und anschliessend dem Container c zugefüt */
		try {
			sidebar = GUISidebar.getGUISidebar();
		} finally {
		}

		/* draftpanel wird erstellt */
		draftpanel = new GUIDraftPanel();

		c.add(draftpanel, BorderLayout.CENTER);

		/* simulationspanel wird erstellt */
		simpanel = new GUISimulationPanel();
		c.add(simpanel, BorderLayout.CENTER);
		simpanel.setVisible(false);

		itemlist = new LinkedList<Object>();

		/*
		 * Abstand verursacht durch Titelleiste muss ueber getInsets ausgelesen
		 * werden
		 */
		abstandy = c.getInsets().top;
		abstandx = c.getInsets().left;

		/*
		 * Die Vorschau für das drag&drop, die das aktuelle Element anzeigt wird
		 * initialisiert und dem layeredpane hinzugefügt. Die Visibility wird
		 * jedoch auf false gestellt, da ja Anfangs kein drag&drop vorliegt.
		 */

		dragVorschau = new JSidebarButton("", null, null);
		dragVorschau.setVisible(false);
		layeredpane.add(dragVorschau);

		markierung.setVisible(false);
		draftpanel.add(markierung);

		/* scrollpane für das Mittlere Panel */
		scrollPane = new JScrollPane(draftpanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);

		/* scrollpane für das Linke Panel (sidebar) */
		sidebarScrollpane = new JScrollPane(sidebar.getLeistenpanel());
		sidebarScrollpane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sidebarScrollpane.getVerticalScrollBar().setUnitIncrement(10);

		c.add(sidebarScrollpane, BorderLayout.WEST);
		c.add(scrollPane, BorderLayout.CENTER);

		/*
		 * Wird auf ein Item der Sidebar geklickt, so wird ein neues
		 * Vorschau-Label mit dem entsprechenden Icon erstellt.
		 */
		sidebarScrollpane.addMouseListener(new MouseInputAdapter() {
			public void mousePressed(MouseEvent e) {
				testlabel = sidebar.aufButton(e.getX(), e.getY());
				if (testlabel != null) {
					//Main.debug.println("GUIContainer: Maustaste wurde ueber "
							//+ testlabel.getHardwareTyp() + " betaetigt.");
					neueVorschau(testlabel.getHardwareTyp(), e.getX(), e.getY());
				}

			}
		});
		/*
		 * Wird die Maus auf dem Entwurfspanel losgelassen, während ein Item
		 * gedragged wird, so wird eine neue Komponente erstellt.
		 */
		sidebarScrollpane.addMouseListener(new MouseInputAdapter() {
			public void mouseReleased(MouseEvent e) {
				int x, y;

				if (dragVorschau.isVisible()
						&& aufObjekt(draftpanel, e.getX()
								- sidebar.getLeistenpanel().getWidth(), e
								.getY())) {

					x = e.getX()
							+ scrollPane.getHorizontalScrollBar().getValue();
					y = e.getY() + scrollPane.getVerticalScrollBar().getValue();
					neuerKnoten(x, y, dragVorschau);
				}
				dragVorschau.setVisible(false);

			}
		});

		/*
		 * 
		 * Sofern die Drag & Drop Vorschau sichtbar ist, wird beim draggen der
		 * Maus die entsprechende Vorschau auf die Mausposition verschoben.
		 */
		sidebarScrollpane.addMouseMotionListener(new MouseInputAdapter() {
			public void mouseDragged(MouseEvent e) {
				if (dragVorschau.isVisible()) {
					dragVorschau.setBounds(e.getX()
							- (dragVorschau.getWidth() / 2), e.getY()
							- (dragVorschau.getHeight() / 2)
							+ menu.getMenupanel().getHeight(), dragVorschau
							.getWidth(), dragVorschau.getHeight());

				}
			}
		});

		// sidebarScrollpane.addKeyListener(kl);
		// scrollPane.addKeyListener(kl);

		/*
		 * Erzeugen und transformieren des Auswahlrahmens, und der sich darin
		 * befindenden Objekte.
		 */
		scrollPane.addMouseMotionListener(new MouseInputAdapter() {
			public void mouseDragged(MouseEvent e) {
				if (getActiveSite() == 1) {
					GUIEvents.getGUIEvents().mausDragged(e);
				}
			}
		});

		scrollPane.addMouseListener(new MouseInputAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (getActiveSite() == 1) {
					GUIEvents.getGUIEvents().mausReleased();
				}
			}
		});

		scrollPane.addMouseListener(new MouseInputAdapter() {
			public void mousePressed(MouseEvent e) {
				GUIEvents.getGUIEvents().mausPressed(e);

			}
		});

		scrollPane.addMouseMotionListener(new MouseInputAdapter() {
			public void mouseMoved(MouseEvent e) {
				if (kabelvorschau.isVisible()) {
					kabelvorschau.setBounds(e.getX()
							+ sidebar.getLeistenpanel().getWidth()
							- (kabelvorschau.getWidth() / 2), e.getY()
							+ menu.getMenupanel().getHeight()
							- (kabelvorschau.getHeight() / 2), kabelvorschau
							.getWidth(), kabelvorschau.getHeight());
					if (ziel2Label != null)
						ziel2Label.setLocation(e.getX()
								+ scrollPane.getHorizontalScrollBar()
										.getValue(), e.getY()
								+ scrollPane.getVerticalScrollBar().getValue());
					if (kabelPanelVorschau != null) {
						kabelPanelVorschau.updateBounds();
					}

				}
			}
		});

		JMainFrame.getJMainFrame().setVisible(true);

		setActiveSite(GUIMainMenu.MODUS_ENTWURF);
	}

	private GUIContainer() {
		Image image;

		image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/gfx/hardware/kabel.png"));
		JMainFrame.getJMainFrame().setIconImage(image);
	}

	/**
	 * Erstellt ein neues Item. Der Dateiname des Icons wird über den String
	 * "komponente" angegeben, die Position über x und y. Das Item wird
	 * anschließend dem Entwurfspanel hinzugefügt, und das Entwurfspanel wird
	 * aktualisiert.
	 * 
	 * FIXME -> Gibt immer TRUE zurück! FIXME -> Vielleicht neu bezeichnen
	 * "neuesItem" o.ä.
	 * 
	 * @author Johannes Bade & Thomas Gerding
	 * 
	 * @param komponente
	 * @param x
	 * @param y
	 * @return boolean
	 */
	private boolean neuerKnoten(int x, int y, JSidebarButton label) {
		Knoten neuerKnoten = null;
		GUIKnotenItem item;
		JSidebarButton templabel;
		ImageIcon tempIcon = null;

		SzenarioVerwaltung.getInstance().setzeGeaendert();

		ListIterator<Object> it = itemlist.listIterator();
		while (it.hasNext()) {
			item = (GUIKnotenItem) it.next();
			item.getImageLabel().setSelektiert(false);
		}

		if (label.getHardwareTyp().equals(Switch.holeHardwareTyp())) {
			neuerKnoten = new Switch();
			tempIcon = new ImageIcon(getClass().getResource("/"+GUISidebar.SWITCH));

		} else if (label.getHardwareTyp().equals(Rechner.holeHardwareTyp())) {
			neuerKnoten = new Rechner();
			tempIcon = new ImageIcon(getClass().getResource("/"+GUISidebar.RECHNER));
		} else if (label.getHardwareTyp().equals(Notebook.holeHardwareTyp())) {
			neuerKnoten = new Notebook();
			tempIcon = new ImageIcon(getClass().getResource("/"+GUISidebar.NOTEBOOK));
		} else if (label.getHardwareTyp().equals(
				Vermittlungsrechner.holeHardwareTyp())) {
			neuerKnoten = new Vermittlungsrechner();
			tempIcon = new ImageIcon(getClass().getResource("/"+GUISidebar.VERMITTLUNGSRECHNER));

			Object[] possibleValues = { "2", "3", "4", "5", "6", "7", "8" };
			Object selectedValue = JOptionPane.showInputDialog(JMainFrame
					.getJMainFrame(), messages.getString("guicontainer_msg1"),
					messages.getString("guicontainer_msg2"),
					JOptionPane.INFORMATION_MESSAGE, null, possibleValues,
					possibleValues[0]);
			if (selectedValue != null) {
				((Vermittlungsrechner) neuerKnoten)
						.setzeAnzahlAnschluesse(Integer
								.parseInt((String) selectedValue));
			}
		} else if (label.getHardwareTyp().equals(Modem.holeHardwareTyp())) {
			neuerKnoten = new Modem();
			tempIcon = new ImageIcon(getClass().getResource("/"+GUISidebar.MODEM));
		} else {
			Main.debug.println("ERROR ("+this.hashCode()+"): "
					+ "unbekannter Hardwaretyp " + label.getHardwareTyp()
					+ " konnte nicht erzeugt werden.");
		}

		if (tempIcon != null && neuerKnoten != null) {
			templabel = new JSidebarButton(neuerKnoten.getName(), tempIcon,
					neuerKnoten.holeHardwareTyp());
			templabel.setBounds(x - templabel.getWidth() / 2
					- sidebar.getLeistenpanel().getWidth(), y
					- templabel.getHeight() / 2, templabel.getWidth(),
					templabel.getHeight());

			item = new GUIKnotenItem();
			item.setKnoten(neuerKnoten);
			item.setImageLabel(templabel);

			setProperty(item);
			item.getImageLabel().setSelektiert(true);
			itemlist.add(item);

			draftpanel.add(templabel);
			draftpanel.repaint();

			//Main.debug.println("GUIContainer: neue Hardware erzeugt ("
					//+ neuerKnoten.getName() + ")");
			GUIEvents.getGUIEvents().setNewItemActive(item);   // set active for further processing (to be evaluated by other methods)
			
			return true;
		} else {
			//Main.debug.println("GUIContainer: Keine neue Hardware erzeugt!");
			return false;
		}

	}

	/**
	 * 
	 * Entwurfsmuster: Singleton
	 * 
	 * Da die GUI nur einmal erstellt werden darf, und aus verschiedenen Klassen
	 * auf sie zugegriffen wird, ist diese als Singleton realisiert.
	 * 
	 * getGUIContainer() ruft den private Konstruktor auf, falls dies noch nicht
	 * geschehen ist, ansonsten wird die Referenz auf die Klasse zurückgegeben.
	 * 
	 * @author Johannes Bade & Thomas Gerding
	 * 
	 * @return ref
	 */
	public static GUIContainer getGUIContainer() {
		if (ref == null) {
			ref = new GUIContainer();
			if (ref == null)
				Main.debug.println("ERROR (static) getGUIContainer(): Fehler!!! ref==null");
		}

		return ref;
	}

	/**
	 * 
	 * 
	 * 
	 * Ermöglicht den zugriff auf den Container, um z.B. diesem Objekte
	 * hinzuzufügen.
	 * 
	 * @author Johannes Bade & Thomas Gerding
	 * @return c
	 */
	/*
	 * public Container getC() { return c; }
	 */

	/**
	 * 
	 * 
	 * Weist der Klasse den Container zu, daraufhin wird die GUI initialisiert.
	 * 
	 * FIXME -> Vielleicht sollte das initialisieren extra aufgerufen werden?
	 * 
	 * @author Johannes Bade & Thomas Gerding
	 * 
	 * @param c
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	/*
	 * public void setC(Container c) throws FileNotFoundException, IOException {
	 * this.c = c; if (c != null) initialisieren(); }
	 */

	/**
	 * 
	 * @author Johannes Bade & Thomas Gerding
	 * 
	 *         Das Layeredpane muss in jedem Fall zugewiesen werden, es dient
	 *         dazu die Drag & Drop Objekte von einer Ebene auf die andere
	 *         anzuzeigen. Dadurch, dass das Layerpane über allen anderen panes
	 *         liegt ist der übergang von einem pane zum anderen möglich.
	 * 
	 * 
	 */
	/*
	 * public void setLayeredpane(JLayeredPane layeredpane) { this.layeredpane =
	 * layeredpane; }
	 */

	/**
	 * 
	 * Setzt das Label für die Drag&Drop-Vorschau auf die durch x und y
	 * angegebene Position und weist ihr das durch den Parameter bild angegebene
	 * Bild zu. Die Größe des Labels wird dem Bild angepasst und die
	 * Sichtbarkeit auf true gestellt.
	 * 
	 * @author Johannes Bade
	 * @param bild
	 *            String
	 * @param x
	 *            int
	 * @param y
	 *            int
	 * 
	 * @return boolean
	 */
	private boolean neueVorschau(String hardwareTyp, int x, int y) {
		String tmp;

		Main.debug
				.println("GUIContainer: die Komponenten-Vorschau wird erstellt.");
		if (hardwareTyp.equals(Kabel.holeHardwareTyp())) {
			tmp = GUISidebar.KABEL;
		} else if (hardwareTyp.equals(Switch.holeHardwareTyp())) {
			tmp = GUISidebar.SWITCH;
		} else if (hardwareTyp.equals(Rechner.holeHardwareTyp())) {
			tmp = GUISidebar.RECHNER;
		} else if (hardwareTyp.equals(Notebook.holeHardwareTyp())) {
			tmp = GUISidebar.NOTEBOOK;
		} else if (hardwareTyp.equals(Vermittlungsrechner.holeHardwareTyp())) {
			tmp = GUISidebar.VERMITTLUNGSRECHNER;
		} else if (hardwareTyp.equals(Modem.holeHardwareTyp())) {
			tmp = GUISidebar.MODEM;
		} else {
			tmp = null;
			Main.debug
					.println("GUIContainer: ausgewaehlte Hardware-Komponente unbekannt!");
		}

		dragVorschau.setHardwareTyp(hardwareTyp);
		dragVorschau.setIcon(new ImageIcon(getClass().getResource("/"+tmp)));
		//Main.debug.println("GUIContainer: dragVorschau width = "
				//+ dragVorschau.getWidth() + "  height = "
				//+ dragVorschau.getHeight());
		//Main.debug.println("\t\tdragVorschau.getIcon() width = "
//				+ dragVorschau.getIcon().getIconWidth() + "  height = "
//				+ dragVorschau.getIcon().getIconHeight());
		dragVorschau.setBounds(x - (dragVorschau.getWidth() / 2), y
				- (dragVorschau.getHeight() / 2)
				+ menu.getMenupanel().getHeight(), dragVorschau.getWidth(),
				dragVorschau.getHeight());
		dragVorschau.setVisible(true);
		return true;
	}

	/**
	 * 
	 * Prüft ob ein Punkt (definiert durch die Parameter x & y) auf einem Objekt
	 * (definiert durch den Parameter komp) befindet.
	 * 
	 * @author Johannes Bade
	 * @param komp
	 *            Component
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public boolean aufObjekt(Component komp, int x, int y) {
		if (x > komp.getX() && x < komp.getX() + komp.getWidth()
				&& y - abstandy > komp.getY()
				&& y - abstandy < komp.getY() + komp.getHeight()) {
			return true;
		} else
			return false;
	}

	/**
	 * Löscht alle Elemente der Item- und Kabelliste und frischt den Viewport
	 * auf. Dies dient dem Reset vor dem Laden oder beim Erstellen eines neuen
	 * Projekts.
	 * 
	 * @author Johannes Bade & Thomas Gerding
	 */
	public void clearAllItems() {
		itemlist.clear();
		cablelist.clear();
		updateViewport();
	}

	public void updateViewport() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (GUIContainer), updateViewport()");
		if (draftpanel.isVisible())
			draftpanel.updateViewport(itemlist, cablelist);
		else if (simpanel.isVisible())
			simpanel.updateViewport(itemlist, cablelist);
	}

	/**
	 * 
	 * Geht die Liste der Kabel durch und ruft bei diesen updateBounds() auf. So
	 * werden die Kabel neu gezeichnet.
	 * 
	 * @author Thomas Gerding & Johannes Bade
	 * 
	 */
	public void updateCables() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (GUIContainer), updateCables()");
		ListIterator<GUIKabelItem> it = cablelist.listIterator();
		while (it.hasNext()) {
			GUIKabelItem tempCable = (GUIKabelItem) it.next();
			tempCable.getKabelpanel().updateBounds();
		}
	}

	public JSidebarButton getKabelvorschau() {
		return kabelvorschau;
	}

	public void setKabelvorschau(JSidebarButton kabelvorschau) {
		this.kabelvorschau = kabelvorschau;
	}

	public int getActiveSite() {
		return activeSite;
	}

	public void setActiveSite(int activeSite) {
		this.activeSite = activeSite;

		if (activeSite == GUIMainMenu.MODUS_ENTWURF) {
			getSimpanel().setVisible(false);
			getDraftpanel().setVisible(true);

			closeDesktops();

			JMainFrame.getJMainFrame().getContentPane().add(
					getSidebarScrollpane(), BorderLayout.WEST);
			JMainFrame.getJMainFrame().invalidate();
			JMainFrame.getJMainFrame().validate();

			getScrollPane().setViewportView(getDraftpanel());
			getSidebarScrollpane().setVisible(true);
			getSidebarScrollpane().updateUI();
			GUIContainer.getGUIContainer().getProperty().setVisible(true);

		} else if (activeSite == GUIMainMenu.MODUS_AKTION) {
			getDraftpanel().setVisible(false);
			getSimpanel().setVisible(true);

			JMainFrame.getJMainFrame().getContentPane().remove(
					GUIContainer.getGUIContainer().getSidebarScrollpane());
			JMainFrame.getJMainFrame().invalidate();
			JMainFrame.getJMainFrame().validate();

			getScrollPane().setViewportView(getSimpanel());
			getSidebarScrollpane().setVisible(false);
			getSidebarScrollpane().updateUI();
			getProperty().setVisible(false);

			GUIContainer.getGUIContainer().getKabelvorschau().setVisible(false);
			GUIContainer.getGUIContainer().getKabelvorschau().setIcon(
					new ImageIcon(getClass().getResource("/gfx/allgemein/ziel1.png")));

		}
		updateViewport();
	}

	public LinkedList<GUIKabelItem> getCablelist() {
		return cablelist;
	}

	public void setCablelist(LinkedList<GUIKabelItem> cablelist) {
		this.cablelist = cablelist;
	}

	public int getAbstandLinks() {
		int abstand = 0;
		abstand = GUIContainer.getGUIContainer().getSidebar().getLeistenpanel()
				.getWidth();
		return abstand;
	}

	public int getAbstandOben() {
		int abstand = 0;
		abstand = GUIContainer.getGUIContainer().getMenu().getMenupanel()
				.getHeight();
		return abstand;
	}

	public static JMarkerPanel getMarkierung() {
		return markierung;
	}

	public static JMarkerPanel getAuswahl() {
		return auswahl;
	}

	public JSidebarButton getDragVorschau() {
		return dragVorschau;
	}

	public JKonfiguration getProperty() {
		return property;
	}

	public void showDesktop(GUIKnotenItem hardwareItem) {
		ListIterator<GUIDesktopWindow> it;
		Betriebssystem bs;
		GUIDesktopWindow tmpDesktop = null;
		boolean fertig = false;

		if (hardwareItem != null && hardwareItem.getKnoten() instanceof Host) {
			bs = (Betriebssystem) ((Host) hardwareItem.getKnoten())
					.getSystemSoftware();

			it = desktopWindowList.listIterator();
			while (!fertig && it.hasNext()) {
				tmpDesktop = it.next();
				if (bs == tmpDesktop.getBetriebssystem()) {
					tmpDesktop.setVisible(true);
					fertig = true;
				}
			}

			if (!fertig) {
				tmpDesktop = new GUIDesktopWindow(bs);
				desktopWindowList.add(tmpDesktop);
				tmpDesktop.setVisible(true);
				tmpDesktop.toFront();

				fertig = true;
			}

			if (tmpDesktop != null)
				this.desktopWindowList.add(tmpDesktop);
		}
	}

	public void addDesktopWindow(GUIKnotenItem hardwareItem) {
		GUIDesktopWindow tmpDesktop = null;
		Betriebssystem bs;

		if (hardwareItem != null && hardwareItem.getKnoten() instanceof Host) {
			bs = (Betriebssystem) ((Host) hardwareItem.getKnoten())
					.getSystemSoftware();
			tmpDesktop = new GUIDesktopWindow(bs);
			desktopWindowList.add(tmpDesktop);
		}
	}

	public void closeDesktops() {
		ListIterator<GUIDesktopWindow> it;

		it = desktopWindowList.listIterator();
		while (it.hasNext()) {
			it.next().setVisible(false);
			it.remove();
		}
	}

	public void setPropertyConf(JKonfiguration conf) {
		boolean maximieren = false;

		if (property != null) {
			property.doUnselectAction();  // do actions required prior to getting unselected (i.e., postprocessing)
			JMainFrame.getJMainFrame().getContentPane().remove(property);
			maximieren = property.isMaximiert();
		}

		property = conf;

		if (property != null) {
			JMainFrame.getJMainFrame().getContentPane().add(property,
					BorderLayout.SOUTH);
			property.updateUI();
			if (!maximieren) {
				property.minimieren();
			} else {
				property.maximieren();
			}
		}
		
	}
	
	public void setProperty(GUIKnotenItem hardwareItem) {
		boolean maximieren = false;

		if (property != null) {
			property.doUnselectAction();  // do actions required prior to getting unselected (i.e., postprocessing)
			JMainFrame.getJMainFrame().getContentPane().remove(property);
			maximieren = property.isMaximiert();
		}

		if (hardwareItem == null) {
			property = JKonfiguration.getInstance(null);
		} else {
			property = JKonfiguration.getInstance(hardwareItem.getKnoten());
		}

		if (property != null) {
		    property.updateAttribute();
			JMainFrame.getJMainFrame().getContentPane().add(property,
					BorderLayout.SOUTH);
			property.updateUI();
			if (hardwareItem == null || !maximieren) {
				property.minimieren();
			} else {
				property.maximieren();
			}
		}
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public JScrollPane getSidebarScrollpane() {
		return sidebarScrollpane;
	}

	public GUIDraftPanel getDraftpanel() {
		return draftpanel;
	}

	public GUISimulationPanel getSimpanel() {
		return simpanel;
	}

	public GUISidebar getSidebar() {
		return sidebar;
	}

	public GUIMainMenu getMenu() {
		return menu;
	}

	public JCablePanel getKabelPanelVorschau() {
		return kabelPanelVorschau;
	}

	public void setKabelPanelVorschau(JCablePanel kabelPanelVorschau) {
		this.kabelPanelVorschau = kabelPanelVorschau;
	}
	
	public JSidebarButton getLabelforKnoten(Knoten node) {
		LinkedList<Object> list = getGUIKnotenItemList();
		for (int i=0; i<list.size(); i++) {
			if (((GUIKnotenItem) list.get(i)).getKnoten().equals(node)) {
				return ((GUIKnotenItem) list.get(i)).getImageLabel();
			}
		}
		return null;
	}

}
