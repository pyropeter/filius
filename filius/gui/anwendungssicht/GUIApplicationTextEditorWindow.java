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
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ListIterator;
import java.util.Observable;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.InternalFrameEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import filius.Main;
import filius.gui.CloseableBrowserTabbedPaneUI;
import filius.software.system.Betriebssystem;
import filius.software.system.Datei;

/**
 * Applikationsfenster fuer TextEditor
 * 
 * @author Johannes Bade & Thomas Gerding
 * 
 */
public class GUIApplicationTextEditorWindow extends GUIApplicationWindow {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea editorField;
	private JPanel backPanel;
	private GUIApplicationWindow diesesFenster;
	private Datei aktuelleDatei = null;
	private String original = "";
	private DefaultMutableTreeNode arbeitsVerzeichnis;
	private JTabbedPane tpTabs;

	public GUIApplicationTextEditorWindow(GUIDesktopPanel desktop,
			String appName) {
		super(desktop, appName);
		this.diesesFenster = this;

		this.setTitle(messages.getString("texteditor_msg1"));
		editorField = new JTextArea("");
		editorField.setEditable(true);
		editorField.setFont(new Font("Courier New", Font.PLAIN, 11));

		this.arbeitsVerzeichnis = holeAnwendung().getSystemSoftware()
				.getDateisystem().getArbeitsVerzeichnis();

		String dateiName = holeParameter()[0];
		if (!dateiName.equals("")) {

			if (this.arbeitsVerzeichnis == null) {

				this.arbeitsVerzeichnis = holeAnwendung().getSystemSoftware()
						.getDateisystem().getRoot();

			}
			Datei datei = holeAnwendung().getSystemSoftware().getDateisystem()
					.holeDatei(arbeitsVerzeichnis, dateiName);
			if (datei != null) {
				this.setTitle(dateiName);
				editorField.setText(datei.getDateiInhalt());
				original = datei.getDateiInhalt();
				aktuelleDatei = datei;
			}
		}

		JScrollPane tpPane = new JScrollPane(editorField);
		tpPane.setBorder(null);

		/* Tabs */
		tpTabs = new JTabbedPane();
		tpTabs.setUI(new CloseableBrowserTabbedPaneUI());
		Box editorBox = Box.createHorizontalBox();

		// editorBox.add(editorField);
		editorBox.add(tpPane);
		editorBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		tabVerhalten();

		backPanel = new JPanel(new BorderLayout());
		backPanel.add(editorBox, BorderLayout.CENTER);

		this.getContentPane().add(backPanel);

		JMenuBar mb = new JMenuBar();

		JMenu menuDatei = new JMenu(messages.getString("texteditor_msg2"));

		menuDatei
				.add(new AbstractAction(messages.getString("texteditor_msg3")) {
					private static final long serialVersionUID = 4307765243000198382L;

					public void actionPerformed(ActionEvent arg0) {
						neu();
					}
				});

		menuDatei
				.add(new AbstractAction(messages.getString("texteditor_msg4")) {
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent arg0) {
						oeffnen();
					}
				});

		menuDatei
				.add(new AbstractAction(messages.getString("texteditor_msg5")) {
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent arg0) {
						speichern();
					}
				});
		menuDatei
				.add(new AbstractAction(messages.getString("texteditor_msg6")) {
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent arg0) {
						speichernUnter();
					}
				});
		menuDatei.addSeparator();
		menuDatei
				.add(new AbstractAction(messages.getString("texteditor_msg7")) {
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent arg0) {
						beenden();
					}
				});

		mb.add(menuDatei);

		this.setJMenuBar(mb);
		pack();
	}

	public void speichern() {
		if (aktuelleDatei != null) {
			aktuelleDatei.setDateiInhalt(editorField.getText());
			original = editorField.getText();
		} else {
			speichernUnter();
		}
	}

	public void speichernUnter() {
		DMTNFileChooser fc = new DMTNFileChooser(
				(Betriebssystem) holeAnwendung().getSystemSoftware());
		int rueckgabe = fc.saveDialog();

		if (rueckgabe == DMTNFileChooser.OK) {
			String dateiNameNeu = fc.getAktuellerDateiname();
			aktuelleDatei = new Datei(dateiNameNeu, messages
					.getString("texteditor_msg8"), editorField.getText());
			this.holeAnwendung().getSystemSoftware().getDateisystem()
					.speicherDatei(fc.getAktuellerOrdner(), aktuelleDatei);
			this.setTitle(aktuelleDatei.getName());
		}
	}

	public void oeffnen() {
		DMTNFileChooser fc = new DMTNFileChooser(
				(Betriebssystem) holeAnwendung().getSystemSoftware());
		int rueckgabe = fc.openDialog();

		if (rueckgabe == DMTNFileChooser.OK) {
			aktuelleDatei = holeAnwendung().getSystemSoftware()
					.getDateisystem().holeDatei(fc.getAktuellerOrdner(),
							fc.getAktuellerDateiname());
			this.aktualisiereDateiInhalt();
		} else {
			Main.debug.println("ERROR (" + this.hashCode()
					+ "): Fehler beim oeffnen einer Datei");
		}
	}

	private void aktualisiereDateiInhalt() {
		if (aktuelleDatei != null) {
			this.setTitle(aktuelleDatei.getName());
			editorField.setText(aktuelleDatei.getDateiInhalt());
			original = aktuelleDatei.getDateiInhalt();
		} else {
			Main.debug
					.println("ERROR ("
							+ this.hashCode()
							+ "): Fehler beim oeffnen einer Datei: keine Datei ausgewaehlt");
		}
	}

	public void beenden() {
		if (original != editorField.getText()) {
			if (JOptionPane.showConfirmDialog(this, messages
					.getString("texteditor_msg9"), messages
					.getString("texteditor_msg10"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				speichern();
			}

		}
		diesesFenster.doDefaultCloseAction();
	}

	public void starten(String[] param) {
		String dateiName = holeParameter()[0];
		if (!dateiName.equals("")) {
			this.arbeitsVerzeichnis = this.holeAnwendung().getSystemSoftware()
					.getDateisystem().getArbeitsVerzeichnis();
			if (this.arbeitsVerzeichnis == null) {
				this.arbeitsVerzeichnis = this.holeAnwendung()
						.getSystemSoftware().getDateisystem().getRoot();
			}
			Datei datei = this.holeAnwendung().getSystemSoftware()
					.getDateisystem().holeDatei(arbeitsVerzeichnis, dateiName);
			if (datei != null) {
				editorField = new JTextArea();
				editorField.setFont(new Font("Courier New", Font.PLAIN, 11));
				this.setTitle(dateiName);
				editorField.setText(datei.getDateiInhalt());
				original = datei.getDateiInhalt();
				aktuelleDatei = datei;

				JScrollPane tpPane = new JScrollPane(editorField);
				tpPane.setBorder(null);

				/* Tabs */
				tpTabs.addTab(datei.getName(), tpPane);
				tpTabs.setSelectedIndex(tpTabs.getTabCount() - 1);
			}

		}

	}

	public void tabVerhalten() {
		/* Tabs schliessbar machen */
		tpTabs.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				if (me.getButton() == 3) {

					JPopupMenu popmen = new JPopupMenu();
					final JMenuItem miTabsSchliessen = new JMenuItem(messages
							.getString("texteditor_msg11"));
					miTabsSchliessen.setActionCommand("tabsschliessen");
					final JMenuItem miAndereTabsSchliessen = new JMenuItem(
							messages.getString("texteditor_msg12"));
					miAndereTabsSchliessen
							.setActionCommand("anderetabsschliessen");

					ActionListener al = new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (e.getActionCommand().equals(
									miTabsSchliessen.getActionCommand())) {
								while (tpTabs.getTabCount() > 0) {
									tpTabs.remove(tpTabs.getTabCount() - 1);
								}
							}
							if (e.getActionCommand().equals(
									miAndereTabsSchliessen.getActionCommand())) {
								Component komponente = tpTabs
										.getSelectedComponent();
								String tmpTitel = tpTabs.getTitleAt(tpTabs
										.getSelectedIndex());

								while (tpTabs.getTabCount() > 0) {
									tpTabs.remove(tpTabs.getTabCount() - 1);
								}
								if (komponente != null) {
									tpTabs.addTab(tmpTitel, komponente);
									tpTabs.setSelectedComponent(komponente);
								}

							}
						}

					};

					miTabsSchliessen.addActionListener(al);
					miAndereTabsSchliessen.addActionListener(al);

					popmen.add(miTabsSchliessen);
					popmen.add(miAndereTabsSchliessen);
					popmen.setVisible(true);

					zeigePopupMenu(popmen, me.getX(), me.getY());

				}
				if (me.getButton() == 1) {
					boolean treffer = false;
					Rectangle aktuellesRect = null;
					CloseableBrowserTabbedPaneUI tpui = (CloseableBrowserTabbedPaneUI) tpTabs
							.getUI();

					ListIterator it = tpui.getButton_positionen()
							.listIterator();
					while (it.hasNext()) {
						Rectangle rect = (Rectangle) it.next();
						if (rect.intersects(new Rectangle(me.getX(), me.getY(),
								1, 1))) {
							treffer = true;
							aktuellesRect = rect;
						}
					}

					if (treffer) {
						int abfrage = showConfirmDialog(messages
								.getString("texteditor_msg13"));

						if (abfrage == JOptionPane.YES_OPTION) {
							tpui.getButton_positionen().remove(aktuellesRect);
							tpTabs.remove(tpTabs.getSelectedIndex());
						}
					}

					/* Neuer Tab bei Doppelklick */
					if (me.getClickCount() == 2) {
						neu();
					}

				}
			}
		});
	}

	public void neu() {
		editorField.setText("");
		setTitle(messages.getString("texteditor_msg1"));
		aktuelleDatei = null;
	}

	public void windowActivated(WindowEvent e) {
		
	}

	public void windowClosing(WindowEvent e) {
		

	}

	public void windowDeactivated(WindowEvent e) {
		

	}

	public void windowDeiconified(WindowEvent e) {
		
	}

	public void windowIconified(WindowEvent e) {
		

	}

	public void windowOpened(WindowEvent e) {
		

	}

	public void internalFrameActivated(InternalFrameEvent e) {
		
	}

	public void internalFrameClosed(InternalFrameEvent e) {
		

	}

	public void internalFrameClosing(InternalFrameEvent e) {
		

	}

	public void internalFrameDeactivated(InternalFrameEvent e) {
		

	}

	public void internalFrameDeiconified(InternalFrameEvent e) {
		

	}

	public void internalFrameIconified(InternalFrameEvent e) {
		

	}

	public void internalFrameOpened(InternalFrameEvent e) {
		
	}

	public void update(Observable arg0, Object arg1) {
		
	}
}
