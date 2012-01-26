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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;

import filius.Main;
import filius.exception.VerbindungsException;
import filius.gui.nachrichtensicht.LauscherDialog;
import filius.gui.netzwerksicht.GUIDraftPanel;
import filius.gui.netzwerksicht.GUIKabelItem;
import filius.gui.netzwerksicht.GUIKnotenItem;
import filius.gui.netzwerksicht.JCablePanel;
import filius.gui.netzwerksicht.JSidebarButton;
import filius.hardware.Kabel;
import filius.hardware.NetzwerkInterface;
import filius.hardware.Port;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.InternetKnoten;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Rechner;
import filius.hardware.knoten.Switch;
import filius.hardware.knoten.Vermittlungsrechner;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.SzenarioVerwaltung;
import filius.software.system.Betriebssystem;
import filius.software.system.ModemFirmware;
import filius.software.system.SwitchFirmware;
import filius.software.system.VermittlungsrechnerBetriebssystem;

public class GUIEvents implements I18n {

	private int auswahlx, auswahly, auswahlx2, auswahly2, mausposx, mausposy;

	private int mausmarkdifx, mausmarkdify;

	private GUIKabelItem neuesKabel;

	private static GUIEvents ref;

	private JSidebarButton aktiveslabel = null;

	private boolean aufmarkierung = false;

	private LinkedList<Object> markedlist;

	private GUIKnotenItem
			loeschitem,
			aktivesItem, ziel2;

	private JSidebarButton loeschlabel;

	private JCablePanel kabelPanelVorschau;

	private GUIEvents() {
		markedlist = new LinkedList<Object>();
	}

	public static GUIEvents getGUIEvents() {
		if (ref == null) {
			ref = new GUIEvents();
		}

		return ref;
	}

	/*
	 * FIXME: Funktionen sollten vielleicht nochmal in einzelfunktionen
	 * aufgeteilt werden
	 */
	public void mausReleased() {
		GUIContainer c = GUIContainer.getGUIContainer();

		LinkedList itemlist = c.getGUIKnotenItemList();
		JMarkerPanel auswahl = c.getAuswahl();
		JMarkerPanel markierung = c.getMarkierung();
		JScrollPane scrollPane = c.getScrollPane();
		GUIKnotenItem tempitem;

		SzenarioVerwaltung.getInstance().setzeGeaendert();

		if (auswahl.isVisible()) {
			int tx, ty, twidth, theight;
			int minx = 999999, miny = 999999, maxx = 0, maxy = 0;
			boolean markiert = false;
			markedlist = new LinkedList<Object>();
			ListIterator it = itemlist.listIterator();
			while (it.hasNext()) {
				tempitem = (GUIKnotenItem) it.next();
				tx = tempitem.getImageLabel().getX();
				twidth = tempitem.getImageLabel().getWidth();
				ty = tempitem.getImageLabel().getY();
				theight = tempitem.getImageLabel().getHeight();

				if (tx > auswahl.getX() - c.getAbstandLinks()
						+ scrollPane.getHorizontalScrollBar().getValue()
						&& tx + twidth < auswahl.getX()
								+ auswahl.getWidth()
								- c.getAbstandLinks()
								+ scrollPane.getHorizontalScrollBar()
										.getValue()
						&& ty > auswahl.getY() - c.getAbstandOben()
								+ scrollPane.getVerticalScrollBar().getValue()
						&& ty + theight < auswahl.getY() + auswahl.getHeight()
								- c.getAbstandOben()
								+ scrollPane.getVerticalScrollBar().getValue()) {
					if (tx < minx) {
						minx = tx;
					}
					if (tx + twidth > maxx) {
						maxx = tx + twidth;
					}
					if (ty < miny) {
						miny = ty;
					}
					if (ty + theight > maxy) {
						maxy = ty + theight;
					}
					markierung.setBounds(minx, miny, maxx - minx, maxy - miny);

					markedlist.add(tempitem);

					markiert = true;
				}
			}

			if (markiert) {
				markierung.setVisible(true);
			}
			auswahl.setVisible(false);
		}
	}

	public void mausDragged(MouseEvent e) {
		
		// do not allow dragging while cable connector is visible, i.e., during cable assignment
		if (GUIContainer.getGUIContainer().getKabelvorschau().isVisible()) return;
		/////
		
		GUIContainer c = GUIContainer.getGUIContainer();

		JMarkerPanel auswahl = c.getAuswahl();
		JMarkerPanel markierung = c.getMarkierung();
		JScrollPane scrollPane = c.getScrollPane();
		int neuX, neuY, neuWidth, neuHeight;
		int tmpX, tmpY;  // for calculating the actual position (only within working panel)

		JSidebarButton dragVorschau = c.getDragVorschau();

		SzenarioVerwaltung.getInstance().setzeGeaendert();

		// Einzelnes Item verschieben
		if (!markierung.isVisible()) {
			if (aktiveslabel != null && !dragVorschau.isVisible()) {

				tmpX = e.getX()
						+ scrollPane.getHorizontalScrollBar().getValue()
						- (aktiveslabel.getWidth() / 2);
				if (tmpX < -(aktiveslabel.getWidth() / 2)) { neuX = -(aktiveslabel.getWidth() / 2); }
				else if (tmpX > (GUIContainer.FLAECHE_BREITE-(aktiveslabel.getWidth()/2))) { neuX = GUIContainer.FLAECHE_BREITE - (aktiveslabel.getWidth() / 2); }
				     else { neuX = tmpX; }
				tmpY = e.getY() + scrollPane.getVerticalScrollBar().getValue()
						- (aktiveslabel.getHeight() / 2);
				if (tmpY < -(aktiveslabel.getHeight() / 2)) { neuY = -(aktiveslabel.getHeight() / 2); }
				else if (tmpY > (GUIContainer.FLAECHE_HOEHE-(aktiveslabel.getHeight() / 2))) { neuY = (GUIContainer.FLAECHE_HOEHE-(aktiveslabel.getHeight() / 2)); }
				     else { neuY = tmpY; }
				neuWidth = aktiveslabel.getWidth();
				neuHeight = aktiveslabel.getHeight();
				aktiveslabel.setBounds(neuX, neuY, neuWidth, neuHeight);
				c.updateCables();
			} else {
				mausposx = e.getX() + c.getAbstandLinks();
				mausposy = e.getY() + c.getAbstandOben();
				if (!auswahl.isVisible()) {
					auswahlx = mausposx;
					auswahly = mausposy;
					auswahlx2 = auswahlx;
					auswahly2 = auswahly;

					auswahl.setBounds(auswahlx, auswahly, auswahlx2 - auswahlx,
							auswahly2 - auswahly);
					auswahl.setVisible(true);
				} else {
					auswahlx2 = mausposx;
					auswahly2 = mausposy;

					auswahl.setBounds(auswahlx, auswahly, auswahlx2 - auswahlx,
							auswahly2 - auswahly);

					if (mausposx < auswahlx) {
						auswahl.setBounds(auswahlx2, auswahly, auswahlx
								- auswahlx2, auswahly2 - auswahly);
					}
					if (mausposy < auswahly) {
						auswahl.setBounds(auswahlx, auswahly2, auswahlx2
								- auswahlx, auswahly - auswahly2);
					}
					if (mausposy < auswahly && mausposx < auswahlx) {
						auswahl.setBounds(auswahlx2, auswahly2, auswahlx
								- auswahlx2, auswahly - auswahly2);
					}
				}
			}
		}
		// Items im Auswahlrahmen verschieben
		else if (!dragVorschau.isVisible()) {
			/* Verschieben mehrerer ausgewaehlter Objekte */
			if (aufmarkierung && markedlist.size() > 0) {
				/*
				 * int ursprungx = markierung.getX(); int ursprungy =
				 * markierung.getY();
				 *
				 * markierung.setBounds(e.getX() + c.getAbstandOben() -
				 * mausmarkdifx, e.getY() + c.getAbstandOben() - mausmarkdify,
				 * markierung.getWidth(), markierung .getHeight()); int
				 * verschiebungx = ursprungx - markierung.getX(); int
				 * verschiebungy = ursprungy - markierung.getY();
				 */
				int maxMinusX=GUIContainer.FLAECHE_BREITE,
					maxPlusX=GUIContainer.FLAECHE_BREITE,
					maxMinusY=GUIContainer.FLAECHE_HOEHE,
					maxPlusY=GUIContainer.FLAECHE_HOEHE;
				int tempM, tempP;
				ListIterator<Object> it = markedlist.listIterator();
				while (it.hasNext()) {
					JSidebarButton templbl = ((GUIKnotenItem) it.next()).getImageLabel();
					tempM=templbl.getX()+((int) templbl.getWidth()/2);
					tempP=GUIContainer.FLAECHE_BREITE - (templbl.getX()+((int) templbl.getWidth()/2));
					if(tempM < maxMinusX) { maxMinusX = tempM; }
					if(tempP < maxPlusX) { maxPlusX = tempP; }
					tempM=GUIContainer.FLAECHE_HOEHE - (templbl.getY()+((int) templbl.getHeight()/2));
					tempP=templbl.getY()+((int) templbl.getHeight()/2);
					if(tempM < maxMinusY) { maxMinusY = tempM; }
					if(tempP < maxPlusY) { maxPlusY = tempP; }
				}
				// Main.debug.println("Max movement of selection compound (left <--> right; up <--> down):\n\t-"
				//		+ maxMinusX+" < moveX < "+maxPlusX+"\n\t-"
				//		+ maxMinusY+" < moveY < "+maxPlusY);
				
				// movement to right:  negative X value
				// movement upwards: positive Y value
				int verschiebungx = (markierung.getX() + mausmarkdifx)
						- e.getX();
				int verschiebungy = (markierung.getY() + mausmarkdify)
						- e.getY();

				// Main.debug.println("Actual mouse movement:\n\tX: "+verschiebungx+"\n\t Y: "+verschiebungy);
				if( verschiebungx > maxMinusX ) { verschiebungx = maxMinusX; }
				if( verschiebungx < -maxPlusX ) { verschiebungx = -maxPlusX; }
				if( verschiebungy > maxPlusY ) { verschiebungy = maxPlusY; }
				if( verschiebungy < -maxMinusY ) { verschiebungy = -maxMinusY; }
				
				markierung.setBounds(markierung.getX() - verschiebungx,
						markierung.getY() - verschiebungy, markierung
								.getWidth(), markierung.getHeight());

				it = markedlist.listIterator();
				while (it.hasNext()) {
					JSidebarButton templbl = ((GUIKnotenItem) it.next())
							.getImageLabel();
					templbl.setLocation(templbl.getX() - verschiebungx, 
										templbl.getY() - verschiebungy);
				}
				c.updateCables();
			}

		}
	}

	public void mausPressed(MouseEvent e) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", mausPressed("+e+")");

		GUIContainer c = GUIContainer.getGUIContainer();
		JMarkerPanel auswahl = c.getAuswahl();
		JMarkerPanel markierung = c.getMarkierung();
		JScrollPane scrollPane = c.getScrollPane();

		JSidebarButton kabelvorschau = c.getKabelvorschau();
		Port anschluss = null;
		Knoten tempKnoten;

		SzenarioVerwaltung.getInstance().setzeGeaendert();

		if (neuesKabel == null) {
			neuesKabel = new GUIKabelItem();
		}
		updateAktivesItem(e);

		// Wurde Maustaste im Bereich der Markierung betaetigt?
		// -> aufmarkierung = true und initialisierung von der Groesse der
		// Markierung

		if (c.aufObjekt(markierung, e.getX()
				+ scrollPane.getHorizontalScrollBar().getValue(), e.getY()
				+ scrollPane.getVerticalScrollBar().getValue())) {
			if (markierung.isVisible()) {
				aufmarkierung = true;
				mausmarkdifx = e.getX() + 
						-markierung.getX();
				mausmarkdify = e.getY() + 
						-markierung.getY();
			}
		}
		else
		{
			aufmarkierung = false;
			markierung.setVisible(false);
			auswahl.setBounds(0, 0, 0, 0);
		}

		// Wurde die rechte Maustaste betaetigt?
		if (e.getButton() == 3) {
			if(aktivesItem != null && aktiveslabel != null) {
//				Main.debug.println("\tmausPressed: IF-1");
				if (GUIContainer.getGUIContainer().getActiveSite() == GUIMainMenu.MODUS_ENTWURF) {
//					Main.debug.println("\tmausPressed: IF-1.1");
					GUIContainer.getGUIContainer().getProperty().minimieren();  // hide property panel (JKonfiguration)
					GUIContainer.getGUIContainer().setProperty(null);
	
					if (!kabelvorschau.isVisible()) {
//						Main.debug.println("\tmausPressed: IF-1.1.1");
						kontextMenueEntwurfsmodus(aktiveslabel, e);
					} else {
						//Main.debug.println("\tmausPressed: ELSE-1.1.1");
	
						kabelvorschau.setVisible(false);
						neuesKabel = null;
						kabelvorschau.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/ziel1.png")));
	
						if (kabelPanelVorschau != null)
							kabelPanelVorschau.setVisible(false);
						ziel2 = null;
					}
				} else {
					//Main.debug.println("\tmausPressed: ELSE-1.1");
					if (GUIContainer.getGUIContainer().getActiveSite() == GUIMainMenu.MODUS_AKTION) {
						kontextMenueAktionsmodus(aktiveslabel, e);
					}
					else {
						Main.debug.println("ERROR ("+this.hashCode()+"): weder Entwurfs- noch Aktionsmodus sind aktiv!?");
					}
				}
			}
			else {
				GUIKabelItem cableItem = clickedCable(e);
				if((kabelPanelVorschau==null || !kabelPanelVorschau.isVisible()) && cableItem != null) {
					contextMenuCable(cableItem,e);
				}
			}
		}
		// Wurde die linke Maustaste betaetigt?
		else {
			//Main.debug.println("\tmausPressed: ELSE-1");
			if (e.getButton() == 1) {
				//Main.debug.println("\tmausPressed: IF-2");

				// Aktivierung einer Komponente im Aktionsmodus mit Doppelklick
				if (GUIContainer.getGUIContainer().getActiveSite() == GUIMainMenu.MODUS_AKTION
						&& e.getClickCount() == 2 ) 
				{
					//Main.debug.println("\tmausPressed: IF-2.1");
					if (aktivesItem != null && aktiveslabel != null) {
						//Main.debug.println("\tmausPressed: IF-2.1.1");
						if (aktivesItem.getKnoten() instanceof Rechner
								|| aktivesItem.getKnoten() instanceof Notebook) {
							//Main.debug.println("\tmausPressed: IF-2.1.1");
							desktopAnzeigen(aktivesItem);
						}
						else if (aktivesItem.getKnoten() instanceof Switch) {
							//Main.debug.println("\tmausPressed: ELSE-2.1.1");
							satTabelleAnzeigen(aktivesItem);
						}
					}
	
				}

				// Auswahl einer Komponente im Entwurfsmodus
				if (GUIContainer.getGUIContainer().getActiveSite() == GUIMainMenu.MODUS_ENTWURF) {
					//Main.debug.println("\tmausPressed: IF-2.2");
					// eine neue Kabelverbindung erstellen
					if (kabelvorschau.isVisible() && aktivesItem != null
							&& aktiveslabel != null) {
						//Main.debug.println("\tmausPressed: IF-2.2.1");
						GUIContainer.getGUIContainer().getProperty().minimieren();  // hide property panel (JKonfiguration)
						GUIContainer.getGUIContainer().setProperty(null);
	
						if (aktivesItem.getKnoten() instanceof Knoten) {
							//Main.debug.println("\tmausPressed: IF-2.2.1.1");
							tempKnoten = (Knoten) aktivesItem.getKnoten();
							anschluss = tempKnoten.holeFreienPort();
	
						}
	
						if (anschluss != null) {
							//Main.debug.println("\tmausPressed: IF-2.2.1.2");
							// Ausgewaehlte Komponente ist erste
							// Verbindungskomponente
							if (neuesKabel.getKabelpanel().getZiel1() == null) {
								//Main.debug.println("\tmausPressed: IF-2.2.1.2.1");
								neuesKabel.getKabelpanel().setZiel1(aktivesItem);
								kabelvorschau.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/ziel2.png")));
								kabelPanelVorschau = new JCablePanel();
								c.getDraftpanel().add(
										kabelPanelVorschau);
								kabelPanelVorschau.setZiel1(aktivesItem);
								c.setZiel2Label(new JSidebarButton());
								ziel2 = new GUIKnotenItem();
								ziel2.setImageLabel(GUIContainer
										.getGUIContainer().getZiel2Label());
	
								c.getZiel2Label().setBounds(
										e.getX()
												+ c.getScrollPane()
														.getHorizontalScrollBar()
														.getValue(),
										e.getY()
												+ c.getScrollPane()
														.getVerticalScrollBar()
														.getValue(), 8, 8);
								kabelPanelVorschau.setZiel2(ziel2);
								kabelPanelVorschau.setVisible(true);
								c.setKabelPanelVorschau(kabelPanelVorschau);
	
							} else {
								//Main.debug.println("\tmausPressed: ELSE-2.2.1.2.1");
	
								if (neuesKabel.getKabelpanel().getZiel2() == null
									&& neuesKabel.getKabelpanel().getZiel1() != aktivesItem) {
									verbindungErstellen(aktivesItem);
								}
								kabelPanelVorschau = null;  // no longer needed
								c.setKabelPanelVorschau(null);
							}
						} else // Anzahl Angeschlossene > Anzahl Erlaubt
						{
							//Main.debug.println("\tmausPressed: ELSE-2.2.1.2");
	
							GUIErrorHandler
									.getGUIErrorHandler()
									.DisplayError(
											messages.getString("guievents_msg1"));
						}
	
					}
					// einen Knoten zur Bearbeitung der Eigenschaften auswaehlen
					else {
						//Main.debug.println("\tmausPressed: ELSE-2.2.1");
	
						if (kabelvorschau.isVisible()) {
							kabelvorschau.setVisible(false);
							if (kabelPanelVorschau != null) {   // abort cable assignment
								GUIContainer.getGUIContainer().getDraftpanel().remove(kabelPanelVorschau);
								kabelPanelVorschau = null;
								c.setKabelPanelVorschau(null);
								neuesKabel = null;
								kabelvorschau.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/ziel1.png")));
								kabelvorschau.setVisible(false);
							}
						}
						
						c.setProperty(aktivesItem);
	
						// wurde Maus ueber einem Knoten betaetigt?
						// -> Item markieren und Eigenschaften anzeigen
						if (aktivesItem != null && aktiveslabel != null) {
							if (e.getClickCount() == 2) {
								GUIContainer.getGUIContainer().getProperty()
										.maximieren();
							}
							aktiveslabel.setSelektiert(true);
							//auswahl.setVisible(false);
						}
						// wurde Maus ueber leerem Bereich betaetigt?
						// -> Markierung sichtbar machen
						else {
							auswahl.setVisible(false);
						}
					}
				}
			}
		}	

	}

	private GUIKabelItem clickedCable(MouseEvent e) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", clickedCable("+e+")");
		// Falls kein neues Objekt erstellt werden soll
		LinkedList<GUIKabelItem> itemlist = GUIContainer.getGUIContainer().getCablelist();
		ListIterator<GUIKabelItem> it = itemlist.listIterator();
		GUIKabelItem tempitem = null;
		GUIContainer c = GUIContainer.getGUIContainer();
		int mouseX = e.getX() + c.getScrollPane().getHorizontalScrollBar().getValue();
		int mouseY = e.getY() + c.getScrollPane().getVerticalScrollBar().getValue();

		while (it.hasNext()) {
			tempitem = it.next();
			
			if (c.aufObjekt(tempitem.getKabelpanel(), 
					mouseX, mouseY)) {  // item clicked, i.e., mouse pointer within item bounds
//				Main.debug.println("DEBUG ("+this.hashCode()+") "+getClass()+", clickedCable:  mouse pointer hit tempitem area ("+tempitem.hashCode()+")");
				if (tempitem.getKabelpanel().clicked(mouseX, mouseY)) {
					// mouse pointer really close to the drawn line, too
//					Main.debug.println("DEBUG ("+this.hashCode()+") "+getClass()+", clickedCable:  mouse pointer hit line of tempitem ("+tempitem.hashCode()+")");
					return tempitem;
				}
			}
		}
//		Main.debug.println("DEBUG ("+this.hashCode()+") "+getClass()+", clickedCable:  nothing hit, return null");
		return null;
	}
	
	private void updateAktivesItem(MouseEvent e) {
		// Falls kein neues Objekt erstellt werden soll
		LinkedList itemlist = GUIContainer.getGUIContainer().getGUIKnotenItemList();
		ListIterator it = itemlist.listIterator();
		GUIKnotenItem tempitem = null;
		GUIContainer c = GUIContainer.getGUIContainer();
		JSidebarButton templabel;

		aktiveslabel = null;
		aktivesItem = null;

		while (it.hasNext()) {
			tempitem = (GUIKnotenItem) it.next();
			templabel = tempitem.getImageLabel();
			templabel.setSelektiert(false);
			templabel.revalidate();
			templabel.updateUI();

			if (c.aufObjekt(templabel, e.getX()
					+ c.getScrollPane().getHorizontalScrollBar().getValue(), e
					.getY()
					+ c.getScrollPane().getVerticalScrollBar().getValue())) {
				aktivesItem = tempitem;
				aktiveslabel = tempitem.getImageLabel();
			}
		}
	}
	
	public GUIKnotenItem getActiveItem() {
		return aktivesItem;
	}

	/* method called in case of new item creation in GUIContainer, such that this creation
	 * process will be registered and the according item is marked active
	 */
	public void setNewItemActive(GUIKnotenItem item) {
		aktivesItem = item;
	}
	
	private void desktopAnzeigen(GUIKnotenItem aktivesItem) {
		GUIContainer.getGUIContainer().showDesktop(aktivesItem);
	}

	private void verbindungErstellen(GUIKnotenItem tempitem) {
		GUIContainer c = GUIContainer.getGUIContainer();
		GUIDraftPanel draftpanel = c.getDraftpanel();
		LinkedList<GUIKabelItem> cablelist = c.getCablelist();
		JSidebarButton kabelvorschau = c.getKabelvorschau();
		NetzwerkInterface nic1, nic2;
		Port anschluss1 = null;
		Port anschluss2 = null;

		neuesKabel.getKabelpanel().setZiel2(tempitem);
		draftpanel.remove(kabelPanelVorschau);
		ziel2 = null;

		draftpanel.add(neuesKabel.getKabelpanel());
		neuesKabel.getKabelpanel().updateBounds();
		draftpanel.updateUI();
		cablelist.add(neuesKabel);
		if (neuesKabel.getKabelpanel().getZiel1().getKnoten() instanceof Modem) {
			Modem vrOut = (Modem) neuesKabel.getKabelpanel().getZiel1()
					.getKnoten();
			anschluss1 = vrOut.getErstenAnschluss();
		}
		else if (neuesKabel.getKabelpanel().getZiel1().getKnoten() instanceof Vermittlungsrechner) {
			Vermittlungsrechner r = (Vermittlungsrechner) neuesKabel
					.getKabelpanel().getZiel1().getKnoten();
			anschluss1 = r.holeFreienPort();
		}
		else if (neuesKabel.getKabelpanel().getZiel1().getKnoten() instanceof Switch) {
			Switch sw = (Switch) neuesKabel.getKabelpanel().getZiel1()
					.getKnoten();
			anschluss1 = ((SwitchFirmware) sw.getSystemSoftware()).getKnoten()
					.holeFreienPort();
		}
		else if (neuesKabel.getKabelpanel().getZiel1().getKnoten() instanceof InternetKnoten){
			nic1 = (NetzwerkInterface) ((InternetKnoten) neuesKabel
					.getKabelpanel().getZiel1().getKnoten())
					.getNetzwerkInterfaces().getFirst();
			anschluss1 = nic1.getPort();
		}

		if (neuesKabel.getKabelpanel().getZiel2().getKnoten() instanceof Modem) {
			Modem vrOut = (Modem) neuesKabel.getKabelpanel().getZiel2()
					.getKnoten();
			anschluss2 = vrOut.getErstenAnschluss();
		}
		else if (neuesKabel.getKabelpanel().getZiel2().getKnoten() instanceof Vermittlungsrechner) {
			Vermittlungsrechner r = (Vermittlungsrechner) neuesKabel
					.getKabelpanel().getZiel2().getKnoten();
			anschluss2 = r.holeFreienPort();
		}
		else if (neuesKabel.getKabelpanel().getZiel2().getKnoten() instanceof Switch) {
			Switch sw = (Switch) neuesKabel.getKabelpanel().getZiel2()
					.getKnoten();
			anschluss2 = ((SwitchFirmware) sw.getSystemSoftware()).getKnoten()
					.holeFreienPort();
		}
		else if (neuesKabel.getKabelpanel().getZiel2().getKnoten() instanceof InternetKnoten){
			nic2 = (NetzwerkInterface) ((InternetKnoten) neuesKabel
					.getKabelpanel().getZiel2().getKnoten())
					.getNetzwerkInterfaces().getFirst();
			anschluss2 = nic2.getPort();
		}

		neuesKabel.setDasKabel(new Kabel());
		neuesKabel.getDasKabel().setAnschluesse(new Port[]{anschluss1, anschluss2});

		neuesKabel = new GUIKabelItem();

		kabelvorschau.setIcon(new ImageIcon(getClass().getResource("/gfx/allgemein/ziel1.png")));
		kabelvorschau.setVisible(false);
		c.setCablelist(cablelist);
	}

	/**
	 * @author Johannes Bade & Thomas Gerding
	 *
	 * Bei rechter Maustaste auf ein Item (bei Laufendem Aktionsmodus) wird ein
	 * Kontextmenü angezeigt, in dem z.B. der Desktop angezeigt werden kann.
	 *
	 * @param templabel
	 *            Item auf dem das Kontextmenü erscheint
	 * @param e
	 *            MouseEvent (Für Position d. Kontextmenü u.a.)
	 */
	private void kontextMenueAktionsmodus(JSidebarButton templabel, MouseEvent e) {
		updateAktivesItem(e);

		if (aktivesItem != null) {
			if (aktivesItem.getKnoten() instanceof Rechner
					|| aktivesItem.getKnoten() instanceof Notebook) {

				JPopupMenu popmen = new JPopupMenu();

				ActionListener al = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "desktopanzeigen") {
							desktopAnzeigen(aktivesItem);
						}


						if (e.getActionCommand() == "datenaustausch") {
							datenAustauschAnzeigen(aktivesItem);
						}

					}
				};

				JMenuItem pmVROUTKonf = new JMenuItem(messages.getString("guievents_msg2"));
				pmVROUTKonf.setActionCommand("vroutkonf");
				pmVROUTKonf.addActionListener(al);

				JMenuItem pmDesktopAnzeigen = new JMenuItem(messages.getString("guievents_msg3"));
				pmDesktopAnzeigen.setActionCommand("desktopanzeigen");
				pmDesktopAnzeigen.addActionListener(al);

				JMenuItem pmDatenAustauschAnzeigen = new JMenuItem(messages.getString("guievents_msg4"));
				pmDatenAustauschAnzeigen.setActionCommand("datenaustausch");
				pmDatenAustauschAnzeigen.addActionListener(al);


				if (aktivesItem.getKnoten() instanceof Rechner
						|| aktivesItem.getKnoten() instanceof Notebook) {
					popmen.add(pmDesktopAnzeigen);
					popmen.add(pmDatenAustauschAnzeigen);
				}
				if (aktivesItem.getKnoten() instanceof Modem) {
					popmen.add(pmDatenAustauschAnzeigen);
				}

				GUIContainer.getGUIContainer().getSimpanel().add(popmen);
				popmen.setVisible(true);
				popmen.show(GUIContainer.getGUIContainer().getSimpanel(), e.getX(), e.getY());

			}
		}
	}

	private void datenAustauschAnzeigen(GUIKnotenItem item) {
		Betriebssystem bs;
		VermittlungsrechnerBetriebssystem vbs;

		if (item.getKnoten() instanceof Host) {
			bs = (Betriebssystem)((Host)item.getKnoten()).getSystemSoftware();
			LauscherDialog.getLauscherDialog(JMainFrame.getJMainFrame()).addTabelle(bs, bs.holeMACAdresse());
			LauscherDialog.getLauscherDialog(JMainFrame.getJMainFrame()).setVisible(true);
		}
		else if (item.getKnoten() instanceof Modem) {
			ModemFirmware firmware = (ModemFirmware)((Modem)item.getKnoten()).getSystemSoftware();
			LauscherDialog.getLauscherDialog(JMainFrame.getJMainFrame()).addTabelle(firmware, firmware.toString());
			LauscherDialog.getLauscherDialog(JMainFrame.getJMainFrame()).setVisible(true);
		}
	}

	/**
	 * @author Johannes Bade & Thomas Gerding
	 *
	 * Bei rechter Maustaste auf ein Item (bei Laufendem Entwurfsmodus) wird ein
	 * Kontextmenü angezeigt, in dem z.B. das Item gelöscht, kopiert oder
	 * ausgeschnitten werden kann.
	 *
	 * @param templabel
	 *            Item auf dem das Kontextmenü erscheint
	 * @param e
	 *            MouseEvent (Für Position d. Kontextmenü u.a.)
	 */
	private void kontextMenueEntwurfsmodus(JSidebarButton templabel, MouseEvent e) {

		String textKabelEntfernen;

		updateAktivesItem(e);

		if (aktivesItem != null) {
			if (aktivesItem.getKnoten() instanceof Rechner
					|| aktivesItem.getKnoten() instanceof Notebook) {
				textKabelEntfernen = messages.getString("guievents_msg5");
			} else {
				textKabelEntfernen = messages.getString("guievents_msg6");
			}

			final JMenuItem pmKabelEntfernen = new JMenuItem(textKabelEntfernen);
			pmKabelEntfernen.setActionCommand("kabelentfernen");
			final JMenuItem pmLoeschen = new JMenuItem(messages.getString("guievents_msg7"));
			pmLoeschen.setActionCommand("del");

			JPopupMenu popmen = new JPopupMenu();
			ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					if (e.getActionCommand() == pmLoeschen.getActionCommand()) {
						itemLoeschen(loeschlabel,loeschitem);
					}

					if (e.getActionCommand() == pmKabelEntfernen
							.getActionCommand()) {
						kabelEntfernen();
					}
				}
			};

			pmLoeschen.addActionListener(al);
			pmKabelEntfernen.addActionListener(al);

			popmen.add(pmKabelEntfernen);

			popmen.add(pmLoeschen);

			GUIContainer.getGUIContainer().getDraftpanel().add(popmen);
			popmen.setVisible(true);
			popmen.show(GUIContainer.getGUIContainer().getDraftpanel(), e.getX(), e.getY());

			loeschlabel = templabel;
			loeschitem = aktivesItem;
		}
	}

	/**
	 * context menu in case of clicking on single cable item
	 * --> used for deleting a single cable
	 */
	private void contextMenuCable(final GUIKabelItem cable, MouseEvent e) {

			final JMenuItem pmRemoveCable = new JMenuItem(messages.getString("guievents_msg5"));
			pmRemoveCable.setActionCommand("removecable");

			JPopupMenu popmen = new JPopupMenu();
			ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getActionCommand() == pmRemoveCable.getActionCommand()) {
						removeSingleCable(cable);
					}
				}
			};

			pmRemoveCable.addActionListener(al);
			popmen.add(pmRemoveCable);

			GUIContainer.getGUIContainer().getDraftpanel().add(popmen);
			popmen.setVisible(true);
			popmen.show(GUIContainer.getGUIContainer().getDraftpanel(), e.getX(), e.getY());
	}

	/**
	 *
	 * Löscht das durch loeschlabel angegebene Item
	 * NOTE: made public for using del key to delete items without local context menu action (cf. JMainFrame) 
	 */
	public void itemLoeschen(JSidebarButton loeschlabel, GUIKnotenItem loeschitem) {
		loeschlabel.setVisible(false);
		GUIContainer.getGUIContainer().setProperty(null);
		ListIterator<GUIKabelItem> iteratorAlleKabel = GUIContainer.getGUIContainer().getCablelist().listIterator();
		GUIKabelItem kabel = new GUIKabelItem();
		LinkedList<GUIKabelItem> loeschKabel = new LinkedList<GUIKabelItem>();
		
		// Zu löschende Elemente werden in eine temporäre Liste gepackt
		while (iteratorAlleKabel.hasNext()) {
			kabel = (GUIKabelItem) iteratorAlleKabel.next();
			if (kabel.getKabelpanel().getZiel1().equals(loeschitem) || kabel.getKabelpanel().getZiel2().equals(loeschitem)) {
				loeschKabel.add(kabel);
			}
		}
		
		// Temporäre Liste der zu löschenden Kabel wird iteriert und dabei
		// werden die Kabel aus der globalen Kabelliste gelöscht
		// und vom Panel entfernt
		ListIterator<GUIKabelItem> iteratorLoeschKabel = loeschKabel.listIterator();
		while (iteratorLoeschKabel.hasNext()) {
			kabel = iteratorLoeschKabel.next();

			this.removeSingleCable(kabel);
		}

		GUIContainer.getGUIContainer().getGUIKnotenItemList().remove(loeschitem);
		GUIContainer.getGUIContainer().getDraftpanel().remove(loeschlabel);
		GUIContainer.getGUIContainer().getDraftpanel().updateUI();
		GUIContainer.getGUIContainer().updateViewport();

	}

	// remove a single cable without using touching the connected node
	private void removeSingleCable(GUIKabelItem cable) {
		if(cable == null) return; // no cable to be removed (this variable should be set in contextMenuCable)
		try {
			cable.getDasKabel().anschluesseTrennen();
		}
		catch (VerbindungsException e) {
			e.printStackTrace(Main.debug);
		}
		GUIContainer.getGUIContainer().getCablelist().remove(cable);
		GUIContainer.getGUIContainer().getDraftpanel().remove(cable.getKabelpanel());
		GUIContainer.getGUIContainer().updateViewport();
	}

	/**
	 *
	 * Entfernt das Kabel, welches am aktuellen Item angeschlossen ist
	 *
	 * Ersetzt spaeter kabelEntfernen!
	 *
	 */
	private void kabelEntfernen() {
		ListIterator<GUIKabelItem> iteratorAlleKabel = GUIContainer.getGUIContainer().getCablelist().listIterator();
		GUIKabelItem tempKabel = null;
		LinkedList<GUIKabelItem> loeschListe = new LinkedList<GUIKabelItem>();

		// Zu löschende Elemente werden in eine temporäre Liste gepackt
		while (iteratorAlleKabel.hasNext()) {
			tempKabel = (GUIKabelItem) iteratorAlleKabel.next();
			if (tempKabel.getKabelpanel().getZiel1().equals(loeschitem)) {
				loeschListe.add(tempKabel);
			}

			if (tempKabel.getKabelpanel().getZiel2().equals(loeschitem)) {
				loeschListe.add(tempKabel);
				ziel2 = loeschitem;
			}
		}

		// Temporäre Liste der zu löschenden Kabel wird iteriert und dabei
		// werden die Kabel aus der globalen Kabelliste gelöscht
		// und vom Panel entfernt
		ListIterator<GUIKabelItem> iteratorLoeschKabel = loeschListe.listIterator();
		while (iteratorLoeschKabel.hasNext()) {
			tempKabel = iteratorLoeschKabel.next();
			this.removeSingleCable(tempKabel);
		}

		GUIContainer.getGUIContainer().updateViewport();

	}

	private void satTabelleAnzeigen(final GUIKnotenItem aktivesItem) {
		Switch sw = (Switch) aktivesItem.getKnoten();

		JFrame jfSATTabelle = new JFrame(messages.getString("guievents_msg8") +" "+ sw.getName());
		jfSATTabelle.setBounds(100, 100, 320, 240);

		ImageIcon icon = new ImageIcon(getClass().getResource("/gfx/hardware/switch.png"));
		jfSATTabelle.setIconImage(icon.getImage());

		DefaultTableModel dtm = new DefaultTableModel(0, 2);
		Iterator it = ((SwitchFirmware) sw.getSystemSoftware())
				.holeSAT().iterator();
		while (it.hasNext()) {
			Vector zeile = (Vector) it.next();
			dtm.addRow(zeile);
		}

		JTable tableSATNachrichten = new JTable(dtm);
		DefaultTableColumnModel dtcm = (DefaultTableColumnModel) tableSATNachrichten
				.getColumnModel();
		dtcm.getColumn(0).setHeaderValue(messages.getString("guievents_msg9"));
		dtcm.getColumn(1).setHeaderValue(messages.getString("guievents_msg10"));
		JScrollPane spSAT = new JScrollPane(tableSATNachrichten);
		jfSATTabelle.getContentPane().add(spSAT);
		jfSATTabelle.setVisible(true);

	}
}
