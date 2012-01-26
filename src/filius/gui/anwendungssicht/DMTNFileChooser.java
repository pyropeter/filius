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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;

import filius.gui.JMainFrame;
import filius.rahmenprogramm.I18n;
import filius.software.system.Betriebssystem;
import filius.software.system.Datei;
import filius.software.system.Dateisystem;

public class DMTNFileChooser implements I18n {

	private JPanel pHaupt;
	private JList lVerzeichnisse;
	private DefaultMutableTreeNode aktuellerOrdner;
	private String aktuellerDateiname;
	private JButton btEbeneHoch;
	private JTextField tfDateiname;
	private JLabel lbDateiname, lbAktuellerOrdner;
	private JButton btAktion, btAbbrechen;
	private Betriebssystem betriebssystem;
	private int rueckgabe = 0;
	private JDialog dialog;

	public static final int OK = 1;
	public static final int CANCEL = 2;

	public DMTNFileChooser(Betriebssystem bs) {
		dialog = new JDialog();
		dialog.setIconImage(JMainFrame.getJMainFrame().getIconImage());

		this.betriebssystem = bs;
		this.aktuellerOrdner = betriebssystem.getDateisystem().getRoot();
		pHaupt = new JPanel(new BorderLayout());
		Box boxHaupt = Box.createVerticalBox();
		Box tmpBox = Box.createHorizontalBox();
		tmpBox.add(Box.createHorizontalStrut(5));

		lbAktuellerOrdner = new JLabel(aktuellerOrdner.toString());
		lbAktuellerOrdner.setHorizontalAlignment(JLabel.LEFT);

		boxHaupt.add(Box.createVerticalStrut(10));
		tmpBox.add(lbAktuellerOrdner);

		tmpBox.add(Box.createHorizontalStrut(20));
		btEbeneHoch = new JButton(messages.getString("dmtnfilechooser_msg1"));
		btEbeneHoch.setActionCommand("ebeneHoch");
		btEbeneHoch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals(btEbeneHoch.getActionCommand())) {
					if (aktuellerOrdner.getParent() != null) {
						aktuellerOrdner = (DefaultMutableTreeNode) aktuellerOrdner
								.getParent();

						ordnerInhaltAnzeigen(aktuellerOrdner);

					}

				}
			}
		});
		tmpBox.add(Box.createHorizontalStrut(5));
		tmpBox.add(btEbeneHoch);
		boxHaupt.add(Box.createVerticalStrut(10));
		boxHaupt.add(tmpBox);

		DefaultListModel lmDateiListe = new DefaultListModel();
		lVerzeichnisse = new JList(lmDateiListe);
		lVerzeichnisse.setFixedCellHeight(16);
		JScrollPane dateiListenScrollPane = new JScrollPane(lVerzeichnisse);
		boxHaupt.add(Box.createVerticalStrut(10));
		boxHaupt.add(dateiListenScrollPane);

		lVerzeichnisse.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				/* Doppelklick (Linke Maustaste) */
				if (e.getClickCount() == 2 && e.getButton() == 1) {
					int index = ((JList) e.getSource()).locationToIndex(e
							.getPoint());

					DefaultListModel lm = (DefaultListModel) lVerzeichnisse
							.getModel();
					int selektiert = selektierteZelle(index, e.getPoint());
					if (selektiert > -1) {
						String[] teile = lm.getElementAt(index).toString()
								.split(";");
						if (teile.length > 0) {
							if (teile[0].equals("Ordner")) {
								DefaultMutableTreeNode ordnerNode = Dateisystem
										.verzeichnisKnoten(aktuellerOrdner,
												teile[1]);
								aktuellerOrdner = ordnerNode;
								ordnerInhaltAnzeigen(ordnerNode);
							}
							if (teile[0].equals("Datei")) {
								Datei datei = betriebssystem.getDateisystem()
										.holeDatei(aktuellerOrdner, teile[1]);
								tfDateiname.setText(datei.getName());
							}

						}
					}
				}
			}
		});
		Box boxDateiname = Box.createHorizontalBox();
		lbDateiname = new JLabel(messages.getString("dmtnfilechooser_msg2"));
		boxDateiname.add(lbDateiname);
		tfDateiname = new JTextField();
		boxDateiname.add(Box.createHorizontalStrut(5));
		boxDateiname.add(tfDateiname);
		boxHaupt.add(Box.createVerticalStrut(10));
		boxHaupt.add(boxDateiname);

		Box boxButtons = Box.createHorizontalBox();
		btAktion = new JButton(messages.getString("dmtnfilechooser_msg3"));
		btAbbrechen = new JButton(messages.getString("dmtnfilechooser_msg4"));
		btAbbrechen.setActionCommand("cancel");
		boxButtons.add(Box.createHorizontalStrut(5));
		boxButtons.add(btAktion);
		boxButtons.add(Box.createHorizontalStrut(5));
		boxButtons.add(btAbbrechen);
		boxHaupt.add(Box.createVerticalStrut(10));
		boxHaupt.add(boxButtons);
		boxHaupt.add(Box.createVerticalStrut(10));

		btAbbrechen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals(btAbbrechen.getActionCommand())) {
					rueckgabe = CANCEL;
					dialog.setVisible(false);
					dialog.dispose();

				}
			}
		});

		pHaupt.add(boxHaupt, BorderLayout.CENTER);
		dialog.setModal(true);
		dialog.setBounds(100, 100, 320, 240);
		dialog.add(pHaupt);

	}

	public int saveDialog() {
		dialog.setTitle(messages.getString("dmtnfilechooser_msg5"));
		btAktion.setText(messages.getString("dmtnfilechooser_msg6"));
		btAktion.setActionCommand("save");
		btAktion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals(btAktion.getActionCommand())) {
					rueckgabe = OK;
					aktuellerDateiname = tfDateiname.getText();
					dialog.setVisible(false);
					dialog.dispose();
				}
			}
		});
		ordnerInhaltAnzeigen(aktuellerOrdner);
		dialog.setVisible(true);
		return rueckgabe;
	}

	public int openDialog() {
		dialog.setTitle(messages.getString("dmtnfilechooser_msg7"));
		btAktion.setText(messages.getString("dmtnfilechooser_msg8"));
		btAktion.setActionCommand("open");
		btAktion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals(btAktion.getActionCommand())) {
					rueckgabe = OK;
					aktuellerDateiname = tfDateiname.getText();
					dialog.setVisible(false);
					dialog.dispose();
				}
			}
		});
		ordnerInhaltAnzeigen(aktuellerOrdner);
		dialog.setVisible(true);
		return rueckgabe;
	}

	public DefaultMutableTreeNode getAktuellerOrdner() {
		return aktuellerOrdner;
	}

	public void setAktuellerOrdner(DefaultMutableTreeNode aktuellerOrdner) {
		this.aktuellerOrdner = aktuellerOrdner;
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
		DefaultListModel lm = (DefaultListModel) lVerzeichnisse.getModel();
		lm.clear();

		lVerzeichnisse.setCellRenderer(new OrdnerInhaltListRenderer());
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			DefaultMutableTreeNode enode = (DefaultMutableTreeNode) e
					.nextElement();
			if (enode.getUserObject().getClass().equals(Datei.class)) {
				Datei dat = (Datei) enode.getUserObject();
				lm.addElement("Datei;" + dat.getName());
			}
			else {
				lm.addElement("Ordner;" + enode.toString());
			}
		}
		lbAktuellerOrdner.setText(messages.getString("dmtnfilechooser_msg11")
				+ " " + aktuellerOrdner.toString());
	}

	/**
	 * Ueberprueft ob eine (per locationToIndex) ermittelte Zelle wirklich
	 * geklickt wurde Das ist noetig, weil im leeren, unteren Teil der JList
	 * automatisch der unterste Index zurueckgegeben wird.
	 */
	public int selektierteZelle(int index, Point punkt) {
		int ergebnis = -1;
		if (lVerzeichnisse.indexToLocation(index) != null) {
			if (lVerzeichnisse.indexToLocation(index).getY()
					+ lVerzeichnisse.getFixedCellHeight() > punkt.getY()) {
				ergebnis = index;
			}
		}
		return ergebnis;
	}

	public String getAktuellerDateiname() {
		return aktuellerDateiname;
	}

	public void setAktuellerDateiname(String aktuellerDateiname) {
		this.aktuellerDateiname = aktuellerDateiname;
	}
}
