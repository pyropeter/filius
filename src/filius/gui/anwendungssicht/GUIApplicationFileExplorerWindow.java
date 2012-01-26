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
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import filius.Main;
import filius.software.lokal.FileExplorer;
import filius.software.system.Datei;
import filius.software.system.Dateisystem;

public class GUIApplicationFileExplorerWindow extends GUIApplicationWindow {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private JPanel backPanel;

	private JTree tv;

	private DefaultMutableTreeNode aktuellerOrdner;

	private JList dateiListe;

	private GUIApplicationFileExplorerWindow dies;

	private DefaultMutableTreeNode selektierteNode, zwischenAblageNode;

	private JButton btImportieren;

	private String datei, pfad;

	private JInternalFrame fileImportFrame;

	public GUIApplicationFileExplorerWindow(final GUIDesktopPanel desktop,
			String appName) {
		super(desktop, appName);
		this.dies = this;
		aktuellerOrdner = holeAnwendung().getSystemSoftware().getDateisystem()
				.getRoot();

		initialisiereKomponenten();
	}

	private void initialisiereKomponenten() {
		backPanel = new JPanel(new BorderLayout());

		tv = new JTree(aktuellerOrdner);

		tv.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tv
						.getLastSelectedPathComponent();

				if (node == null)
					return;

				aktuellerOrdner = node;

				ordnerInhaltAnzeigen(node);
			}
		});

		ImageIcon dateiIcon = new ImageIcon(getClass().getResource("/gfx/desktop/datei.png"));
		ImageIcon ordnerIcon = new ImageIcon(getClass().getResource("/gfx/desktop/ordner.png"));

		tv.setBounds(0, 0, 150, 100);
		tv.setCellRenderer(new GUITreeRenderer(dateiIcon, ordnerIcon));
		final Box box = Box.createVerticalBox();
		box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final JButton aktualisieren = new JButton(messages
				.getString("fileexplorer_msg1"));
		aktualisieren.setActionCommand("aktualisieren");
		aktualisieren.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals(
						aktualisieren.getActionCommand())) {
					aktualisieren();
				}
			}

		});
		box.add(aktualisieren);
		box.add(Box.createVerticalStrut(5));

		btImportieren = new JButton(messages.getString("fileexplorer_msg2"));
		btImportieren.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileImport();
			}
		});
		box.add(btImportieren);

		JScrollPane scrollpane = new JScrollPane(tv);


		Box horBox = Box.createHorizontalBox();
		horBox.add(scrollpane);
		horBox.setPreferredSize(new Dimension(180, 240));
		DefaultListModel lmDateiListe = new DefaultListModel();
		dateiListe = new JList(lmDateiListe);
		dateiListe.setFixedCellHeight(16);
		JScrollPane dateiListenScrollPane = new JScrollPane(dateiListe);
		horBox.add(dateiListenScrollPane);

		dateiListe.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				/* Rechte Maustaste (Einmal geklickt) */
				if (e.getButton() == 3) {
					if (aktuellerOrdner != null) {
						int index = ((JList) e.getSource()).locationToIndex(e
								.getPoint());

						DefaultListModel lm = (DefaultListModel) dateiListe
								.getModel();
						int selektiert = selektierteZelle(index, e.getPoint());
						JPopupMenu popmen = new JPopupMenu();
						final JMenuItem miNeuerOrdner = new JMenuItem(messages
								.getString("fileexplorer_msg3"));
						miNeuerOrdner.setActionCommand("neuerordner");
						final JMenuItem miLoeschen = new JMenuItem(messages
								.getString("fileexplorer_msg4"));
						miLoeschen.setActionCommand("loeschen");
						final JMenuItem miAusschneiden = new JMenuItem(messages
								.getString("fileexplorer_msg5"));
						miAusschneiden.setActionCommand("ausschneiden");
						final JMenuItem miKopieren = new JMenuItem(messages
								.getString("fileexplorer_msg6"));
						miKopieren.setActionCommand("kopieren");
						final JMenuItem miEinfuegen = new JMenuItem(messages
								.getString("fileexplorer_msg7"));
						miEinfuegen.setActionCommand("einfuegen");
						final JMenuItem miUmbenennen = new JMenuItem(messages
								.getString("fileexplorer_msg8"));
						miUmbenennen.setActionCommand("umbenennen");

						ActionListener al = new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								/* Neuer Ordner */
								if (e.getActionCommand().equals(
										miNeuerOrdner.getActionCommand())) {
									String ordnerName = JOptionPane
											.showInputDialog("");
									if (!ordnerName.equals("")) {
										holeAnwendung().getSystemSoftware()
												.getDateisystem()
												.erstelleVerzeichnis(
														aktuellerOrdner,
														ordnerName);
										aktualisieren();
									}
								}
								/* Loeschen */
								if (e.getActionCommand().equals(
										miLoeschen.getActionCommand())) {
									int loeschAbfrage = JOptionPane
											.showConfirmDialog(
													dies,
													messages
															.getString("fileexplorer_msg18"),
													messages
															.getString("fileexplorer_msg18"),
													JOptionPane.YES_NO_OPTION);

									if (loeschAbfrage == JOptionPane.YES_OPTION) {
										aktuellerOrdner.remove(selektierteNode);
										aktualisieren();
									}
								}
								/* Ausschneiden */
								if (e.getActionCommand().equals(
										miAusschneiden.getActionCommand())) {
									try {
										zwischenAblageNode = tiefesKopieren(selektierteNode);
									}
									catch (IOException e1) {
										e1.printStackTrace(Main.debug);
									}
									catch (ClassNotFoundException e1) {
										e1.printStackTrace(Main.debug);
									}
									aktuellerOrdner.remove(selektierteNode);
									aktualisieren();
								}
								/* Kopieren */
								if (e.getActionCommand().equals(
										miKopieren.getActionCommand())) {
									try {
										zwischenAblageNode = tiefesKopieren(selektierteNode);
									}
									catch (IOException e1) {
										e1.printStackTrace(Main.debug);
									}
									catch (ClassNotFoundException e1) {
										e1.printStackTrace(Main.debug);
									}
									aktualisieren();
								}
								/* Einfuegen */
								if (e.getActionCommand().equals(
										miEinfuegen.getActionCommand())) {
									aktuellerOrdner.add(zwischenAblageNode);
									aktualisieren();
								}
								/* Umbenennen */
								if (e.getActionCommand().equals(
										miUmbenennen.getActionCommand())) {
									String neuerName = JOptionPane
											.showInputDialog(
													dies,
													messages
															.getString("fileexplorer_msg9"));
									if (neuerName != "" && neuerName != null) {
										if (!holeAnwendung()
												.getSystemSoftware()
												.getDateisystem()
												.dateiVorhanden(
														aktuellerOrdner,
														neuerName)) {
											if (selektierteNode.getUserObject()
													.getClass().equals(
															Datei.class)) {
												/* Datei umbenennen */
												Datei dat = (Datei) selektierteNode
														.getUserObject();
												dat.setName(neuerName);
											}
											else {
												/* Ordner umbenennen */
												selektierteNode
														.setUserObject(neuerName);
											}
											aktualisieren();
										}
									}
								}
							}
						};
						miNeuerOrdner.addActionListener(al);
						miLoeschen.addActionListener(al);
						miAusschneiden.addActionListener(al);
						miKopieren.addActionListener(al);
						miEinfuegen.addActionListener(al);
						miUmbenennen.addActionListener(al);

						/*
						 * Neuer Ordner kann nur angelegt werden, wenn kein
						 * Eintrag selektiert ist
						 */
						if (selektiert == -1) {
							popmen.add(miNeuerOrdner);
							if (zwischenAblageNode != null) {
								popmen.add(miEinfuegen);
							}
							;
						}
						else {
							String[] teile = lm.getElementAt(index).toString()
									.split(";");
							if (teile.length > 0) {
								selektierteNode = Dateisystem
										.verzeichnisKnoten(aktuellerOrdner,
												teile[1]);
							}
							popmen.add(miLoeschen);
							popmen.add(miAusschneiden);
							popmen.add(miKopieren);
							popmen.add(miUmbenennen);

						}

						dies.add(popmen);

						popmen.show(dies.getRootPane().getLayeredPane(), e
								.getX()
								+ tv.getWidth(), e.getY()
								+ btImportieren.getY()
								+ btImportieren.getHeight());
					}

				}
			}
		});

		box.add(horBox);
		backPanel.add(box, BorderLayout.CENTER);
		ordnerInhaltAnzeigen(aktuellerOrdner);
		this.getContentPane().add(backPanel);
		pack();

	}

	/**
	 * Fuegt den Inhalt einer DefaultMutableTreeNode in ListModel der dateiListe
	 * ein. Um im CellRenderer zwischen Dateien und Ordnern unterscheiden zu
	 * koennen, wird der Typ (Datei/Ordner) gefolgt von einem Semicolon
	 * angegeben.
	 *
	 * @param node
	 *            Die DefaultMutableTreeNode deren Inhalt angezeigt werden soll.
	 */
	public void ordnerInhaltAnzeigen(DefaultMutableTreeNode node) {
		DefaultListModel lm = (DefaultListModel) dateiListe.getModel();
		lm.clear();
		dateiListe.setCellRenderer(new OrdnerInhaltListRenderer());
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			DefaultMutableTreeNode enode = (DefaultMutableTreeNode) e
					.nextElement();
			if (enode.getUserObject().getClass().equals(Datei.class)) {
				Datei dat = (Datei) enode.getUserObject();
				lm.addElement(messages.getString("fileexplorer_msg10")
						+ dat.getName());
			}
			else {
				lm.addElement(messages.getString("fileexplorer_msg11")
						+ enode.toString());
			}
		}
	}

	/**
	 * Ueberprueft ob eine (per locationToIndex) ermittelte Zelle wirklich
	 * geklickt wurde Das ist noetig, weil im leeren, unteren Teil der JList
	 * automatisch der unterste Index zurueckgegeben wird.
	 */
	public int selektierteZelle(int index, Point punkt) {
		int ergebnis = -1;
		if (dateiListe.indexToLocation(index) != null) {
			if (dateiListe.indexToLocation(index).getY()
					+ dateiListe.getFixedCellHeight() > punkt.getY()) {
				ergebnis = index;
			}
		}
		return ergebnis;
	}

	public void aktualisieren() {
		tv.updateUI();
		ordnerInhaltAnzeigen(aktuellerOrdner);
	}

	/**
	 * Da bei clone() nur das Objekt und nicht seine Referenzen kopiert werden,
	 * wird fuer DefaultMutableTreeNode Tiefes Kopieren gebraucht, um z.B. bei
	 * einem Ordner die komplette eingeschlossene Struktur zu erhalten
	 */
	public DefaultMutableTreeNode tiefesKopieren(DefaultMutableTreeNode original)
			throws IOException, ClassNotFoundException {
		DefaultMutableTreeNode ergebnis = null;

		// ObjectOutputStream erzeugen
		ByteArrayOutputStream bufOutStream = new ByteArrayOutputStream();
		ObjectOutputStream outStream = new ObjectOutputStream(bufOutStream);

		// Objekt im byte-Array speichern
		outStream.writeObject(original);
		outStream.close();

		// Pufferinhalt abrufen
		byte[] buffer = bufOutStream.toByteArray();
		// ObjectInputStream erzeugen
		ByteArrayInputStream bufInStream = new ByteArrayInputStream(buffer);
		ObjectInputStream inStream = new ObjectInputStream(bufInStream);
		// Objekt wieder auslesen
		ergebnis = (DefaultMutableTreeNode) inStream.readObject();

		return ergebnis;
	}

	public void fileImport() {
		fileImportFrame = new JInternalFrame(messages
				.getString("fileexplorer_msg12"));

		ImageIcon image = new ImageIcon(getClass().getResource("/gfx/desktop/icon_fileimporter.png"));
		image.setImage(image.getImage().getScaledInstance(16, 16, Image.SCALE_AREA_AVERAGING));
	    setFrameIcon(image);

		backPanel = new JPanel(new BorderLayout());

		final JTextArea outputField = new JTextArea("");
		outputField.setEditable(false);
		outputField.setSize(new Dimension(300, 80));
		JLabel fileLabel = new JLabel(messages.getString("fileexplorer_msg13"));

		final JTextField inputField = new JTextField("");
		inputField.setSize(new Dimension(150, 30));
		inputField.setEditable(false);

		final JTextField renameField = new JTextField("");
		renameField.setSize(new Dimension(150, 30));
		JLabel renameLabel = new JLabel(messages.getString("fileexplorer_msg9"));

		JButton fileButton = new JButton(messages
				.getString("fileexplorer_msg14"));
		fileButton.setSize(new Dimension(100, 30));
		fileButton.addMouseListener(new MouseInputAdapter() {

			public void mousePressed(MouseEvent e) {
				JFileChooser fc = new JFileChooser();
				if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					datei = fc.getSelectedFile().getName();
					pfad = fc.getSelectedFile().getParent();
					if (!pfad.endsWith(System.getProperty("file.separator")))
						pfad += System.getProperty("file.separator");
				}

				if (datei != null) {

					inputField.setText(pfad + datei);
					renameField.setText(datei);

				}

				try {
					fileImportFrame.setSelected(true);
				}
				catch (PropertyVetoException e1) {
					e1.printStackTrace(Main.debug);
				}
			}
		});

		Box importBox = Box.createHorizontalBox();
		importBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		importBox.add(fileLabel);
		importBox.add(Box.createHorizontalStrut(5));
		importBox.add(inputField);
		importBox.add(Box.createHorizontalStrut(5));
		importBox.add(fileButton);

		Box middleBox = Box.createHorizontalBox();

		middleBox.add(renameLabel);
		middleBox.add(Box.createHorizontalStrut(5));
		middleBox.add(renameField);
		middleBox.add(Box.createHorizontalStrut(5));

		Box upperBox = Box.createVerticalBox();
		upperBox.add(importBox);
		upperBox.add(middleBox);

		backPanel.add(upperBox, BorderLayout.NORTH);

		JButton importButton = new JButton(messages
				.getString("fileexplorer_msg15"));
		importButton.setSize(new Dimension(100, 30));
		importButton.addMouseListener(new MouseInputAdapter() {
			public void mousePressed(MouseEvent z) {
				if (inputField.getText().equals("")
						|| renameField.getText().equals("")) {
					outputField.setText(messages
							.getString("fileexplorer_msg16"));
				}
				else {

					if (aktuellerOrdner == null) {
						outputField.setText(messages
								.getString("fileexplorer_msg17"));
					}
					else {
//						if (inputField.getText().equals(pfad + datei)
//								&& datei.equals(renameField.getText())) {
							outputField.setText(((FileExplorer) holeAnwendung()).addFile(pfad, datei,
													aktuellerOrdner,
													renameField.getText()));
							aktualisieren();
//						}
//						else if (inputField.getText().equals(pfad + datei)
//								&& !renameField.getText().equals("")) {
//							outputField
//									.setText(((FileExplorer) holeAnwendung())
//											.addFile(outputField.getText(), "",
//													aktuellerOrdner,
//													renameField.getText()));
//							aktualisieren();
//						}
					}
				}
			}
		});

		Box lowerBox = Box.createHorizontalBox();
		lowerBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		lowerBox.add(importButton);

		backPanel.add(outputField, BorderLayout.CENTER);
		backPanel.add(lowerBox, BorderLayout.SOUTH);

		fileImportFrame.getContentPane().add(backPanel);

		fileImportFrame.setClosable(true);
		fileImportFrame.setResizable(false);
		fileImportFrame.setBounds(30, 80, 350, 200);
		fileImportFrame.setVisible(true);

		addFrame(fileImportFrame);
		try {
			fileImportFrame.setSelected(true);
		}
		catch (PropertyVetoException e1) {
			e1.printStackTrace(Main.debug);
		}
	}

	public void update(Observable arg0, Object arg1) {
	}

}
