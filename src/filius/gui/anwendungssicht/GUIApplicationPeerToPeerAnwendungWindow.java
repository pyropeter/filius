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
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Observable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import filius.software.email.Email;
import filius.software.system.Betriebssystem;
import filius.software.system.Datei;

import filius.rahmenprogramm.EingabenUeberpruefung;


import filius.software.dateiaustausch.PeerToPeerAnwendung;


/**
 * Diese Klasse ist die grafische Benutzungsoberflaeche fuer die Peer2Peer Anwendung.
 *
 * @author Johannes Bade & Thomas Gerding
 */
public class GUIApplicationPeerToPeerAnwendungWindow extends GUIApplicationWindow{


	private static final long serialVersionUID = 1L;
	private boolean pruefungOK = true;
	private boolean zahlOK=true;
	private JPanel mainPanel;
	private JPanel networkPanel;
	private JPanel searchPanel;
	private JLabel networkIpLabel;
	private JButton networkIpButton;
	private JTextField networkIpField;
	private JLabel searchLabel;
	private JButton searchButton, downloadButton, stopSearchButton, emptyListButton;
	private JTextField searchField;
	private JTable netzwerkTabelle, ergebnisTabelle, dateiTabelle;
	private JPanel filePanel;
	private JLabel filePanelLabel;
	private JLabel maxClientsLabel;
	private JTextField maxClientsField;
	private JPanel configPanel;
	private JLabel tabHead;




	public GUIApplicationPeerToPeerAnwendungWindow(final GUIDesktopPanel desktop, String appName) {
		super(desktop, appName);

		JTabbedPane tabbedPane = new JTabbedPane();


		mainPanel = new JPanel(new BorderLayout());

		networkPanel = new JPanel(new BorderLayout());
		networkIpLabel = new JLabel(messages.getString("peertopeeranwendung_msg1"));
		networkIpButton = new JButton(messages.getString("peertopeeranwendung_msg2"));
		networkIpButton.setToolTipText(messages.getString("peertopeeranwendung_msg3"));
		tabHead = new JLabel(messages.getString("peertopeeranwendung_msg4"));
		networkIpField = new JTextField("192.168.0.1");

		networkIpField.addKeyListener(new KeyAdapter()
		{
			public void keyReleased(KeyEvent e)
			{
				ipPruefen(networkIpField);
			}

		});

		networkIpButton.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
            {
            	/*
            	 * Ueberprueft ob es sich ueberhaupt um eine korrekt aufgebaute IP handelt
            	 * und verhindert das Hinzufuegen ggf.
            	 */
            	if(pruefungOK == true)
            	{
            		((PeerToPeerAnwendung)holeAnwendung()).beitretenNetzwerk(networkIpField.getText());
            	}
            	else
            	{
            		JOptionPane.showMessageDialog(desktop, messages.getString("peertopeeranwendung_msg5"));
            	}
            }
            }});


		DefaultTableModel neuesTabellenModell = new DefaultTableModel(0, 2);
		netzwerkTabelle = new JTable(neuesTabellenModell);
//		Add a mouse listener to the table
		netzwerkTabelle.setEnabled(false);
		netzwerkTabelle.setIntercellSpacing(new Dimension(10,10));
		netzwerkTabelle.setRowHeight(30);
		netzwerkTabelle.setShowGrid(false);
		netzwerkTabelle.setFillsViewportHeight(true);
		netzwerkTabelle.setBackground(Color.WHITE);
		netzwerkTabelle.setShowHorizontalLines(true);


		TableColumnModel tcm = netzwerkTabelle.getColumnModel();
		tcm.getColumn(0).setHeaderValue(messages.getString("peertopeeranwendung_msg6"));
		tcm.getColumn(0).setPreferredWidth(20);
		tcm.getColumn(1).setHeaderValue(messages.getString("peertopeeranwendung_msg7"));
		tcm.getColumn(1).setPreferredWidth(300);

		JScrollPane tabellenScrollPane = new JScrollPane(netzwerkTabelle);

		neuesTabellenModell = new DefaultTableModel(0, 3);
		ergebnisTabelle = new JTable(neuesTabellenModell);
		ergebnisTabelle.setDragEnabled(false);
		ergebnisTabelle.setIntercellSpacing(new Dimension(10,10));
		ergebnisTabelle.setRowHeight(30);
		ergebnisTabelle.setShowGrid(false);
		ergebnisTabelle.setFillsViewportHeight(true);
		ergebnisTabelle.setBackground(Color.WHITE);
		ergebnisTabelle.setShowHorizontalLines(true);
		ergebnisTabelle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		tcm = ergebnisTabelle.getColumnModel();
		tcm.getColumn(0).setHeaderValue(messages.getString("peertopeeranwendung_msg8"));
		tcm.getColumn(0).setPreferredWidth(200);
		tcm.getColumn(1).setHeaderValue(messages.getString("peertopeeranwendung_msg9"));
		tcm.getColumn(1).setPreferredWidth(200);
		tcm.getColumn(2).setHeaderValue(messages.getString("peertopeeranwendung_msg10"));
		tcm.getColumn(2).setPreferredWidth(50);



		JScrollPane tabellenScrollPaneErgebnis = new JScrollPane(ergebnisTabelle);


		Box verticalTopBox = Box.createVerticalBox();

		Box topBox = Box.createHorizontalBox();
		topBox.add(networkIpLabel);
		topBox.add(Box.createHorizontalStrut(5));
		topBox.add(networkIpField);
		topBox.add(Box.createHorizontalStrut(15));
		topBox.add(networkIpButton);
		topBox.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		Box labelBox = Box.createHorizontalBox();
		labelBox.add(tabHead);

		verticalTopBox.add(topBox);
		verticalTopBox.add(Box.createVerticalStrut(5));
		verticalTopBox.add(tabHead);

		networkPanel.add(verticalTopBox, BorderLayout.NORTH);
		networkPanel.add(tabellenScrollPane, BorderLayout.CENTER);

		tabbedPane.addTab("Netzwerk", new ImageIcon(getClass().getResource("/gfx/desktop/peertopeer_netzwerk_klein.png")), networkPanel);

		searchPanel = new JPanel(new BorderLayout());
		searchLabel = new JLabel(messages.getString("peertopeeranwendung_msg11"));
		searchField = new JTextField(messages.getString("peertopeeranwendung_msg12"));
		searchButton = new JButton(messages.getString("peertopeeranwendung_msg13"));
		searchButton.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
            {
            	((PeerToPeerAnwendung)holeAnwendung()).sucheDatei(searchField.getText());
            }
            }});

		stopSearchButton = new JButton(messages.getString("peertopeeranwendung_msg14"));
		stopSearchButton.setToolTipText(messages.getString("peertopeeranwendung_msg15"));
		stopSearchButton.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
            {
            	((PeerToPeerAnwendung)holeAnwendung()).abbrechenSuche();
            }
            }});

		emptyListButton = new JButton(messages.getString("peertopeeranwendung_msg16"));
		emptyListButton.setToolTipText(messages.getString("peertopeeranwendung_msg17"));
		emptyListButton.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
            {
            	((PeerToPeerAnwendung)holeAnwendung()).loescheSuchergebnisse();
            	updateErgebnisTabelle();
            }
            }});


		downloadButton = new JButton(messages.getString("peertopeeranwendung_msg18"));
		downloadButton.setToolTipText(messages.getString("peertopeeranwendung_msg19"));
		downloadButton.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
            {
            	int zeilenNummer = ergebnisTabelle.getSelectedRow();
				if (zeilenNummer > -1)
				{
    			((PeerToPeerAnwendung)holeAnwendung()).herunterladenDatei(zeilenNummer);
				}

            }
            }});


		Box searchBox = Box.createHorizontalBox();
		searchBox.add(searchLabel);
		searchBox.add(Box.createHorizontalStrut(5));
		searchBox.add(searchField);
		searchBox.add(Box.createHorizontalStrut(5));
		searchBox.add(searchButton);
		searchBox.add(Box.createHorizontalStrut(2));
		searchBox.add(stopSearchButton);
		searchBox.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		Box downloadBox = Box.createHorizontalBox();
		downloadBox.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		downloadBox.add(downloadButton);
		downloadBox.add(Box.createHorizontalStrut(5));
		downloadBox.add(emptyListButton);

		searchPanel.add(searchBox, BorderLayout.NORTH);
		searchPanel.add(tabellenScrollPaneErgebnis, BorderLayout.CENTER);
		searchPanel.add(downloadBox, BorderLayout.SOUTH);
		tabbedPane.addTab(messages.getString("peertopeeranwendung_msg20"), new ImageIcon(getClass().getResource("/gfx/desktop/peertopeer_suchen_klein.png")),searchPanel);


		filePanel = new JPanel(new BorderLayout());
		filePanelLabel = new JLabel(messages.getString("peertopeeranwendung_msg21"));

		DefaultTableModel dateiTabellenModell = new DefaultTableModel(0, 2);
		dateiTabelle = new JTable(dateiTabellenModell);
		dateiTabelle.setDragEnabled(false);
		dateiTabelle.setEnabled(false);
		dateiTabelle.setIntercellSpacing(new Dimension(10,10));
		dateiTabelle.setRowHeight(30);
		dateiTabelle.setShowGrid(false);
		dateiTabelle.setFillsViewportHeight(true);
		dateiTabelle.setBackground(Color.WHITE);
		dateiTabelle.setShowHorizontalLines(true);

		tcm = dateiTabelle.getColumnModel();
		tcm.getColumn(0).setHeaderValue(messages.getString("peertopeeranwendung_msg22"));
		tcm.getColumn(0).setPreferredWidth(300);
		tcm.getColumn(1).setHeaderValue(messages.getString("peertopeeranwendung_msg23"));
		tcm.getColumn(1).setPreferredWidth(50);
		JScrollPane dateiScrollPane = new JScrollPane(dateiTabelle);

		this.updateDateiTabelle();

		Box dateiBox = Box.createHorizontalBox();
		dateiBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dateiBox.add(filePanelLabel);

		filePanel.add(dateiBox, BorderLayout.NORTH);
		filePanel.add(dateiScrollPane, BorderLayout.CENTER);



		tabbedPane.addTab(messages.getString("peertopeeranwendung_msg24"),new ImageIcon(getClass().getResource("/gfx/desktop/peertopeer_dateien_klein.png")) , filePanel);

		configPanel = new JPanel(new BorderLayout());
		maxClientsLabel = new JLabel(messages.getString("peertopeeranwendung_msg25"));
		maxClientsField = new JTextField(""+((PeerToPeerAnwendung)holeAnwendung()).getMaxTeilnehmerZahl());
		maxClientsField.addKeyListener(new KeyAdapter()
		{
			public void keyReleased(KeyEvent e)
			{
				zahlPruefen(maxClientsField);
			}

		});
		JButton maxClientButton = new JButton(messages.getString("peertopeeranwendung_msg26"));
		maxClientButton.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
            {
                if(zahlOK==true)
                {
                	((PeerToPeerAnwendung)holeAnwendung()).setMaxTeilnehmerZahl(Integer.parseInt(maxClientsField.getText()));
                }
                else
                {
                	JOptionPane.showMessageDialog(desktop, messages.getString("peertopeeranwendung_msg27"));
                }



            }
            }});




		Box configBox = Box.createHorizontalBox();
		configBox.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		configBox.add(maxClientsLabel);
		configBox.add(Box.createHorizontalStrut(10));
		configBox.add(maxClientsField);
		configBox.add(Box.createHorizontalStrut(10));
		configBox.add(maxClientButton);

		configPanel.add(configBox, BorderLayout.NORTH);

		tabbedPane.addTab(messages.getString("peertopeeranwendung_msg28"), new ImageIcon(getClass().getResource("/gfx/desktop/peertopeer_einstellungen.png")), configPanel);

		tabbedPane.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent arg0) {
				updateDateiTabelle();
				updateErgebnisTabelle();
				updateNetzwerkTabelle();
			}

		});

		mainPanel.add(tabbedPane, BorderLayout.CENTER);

		this.getContentPane().add(mainPanel);
		pack();

		updateNetzwerkTabelle();
		updateErgebnisTabelle();
		updateDateiTabelle();

	}

	/**
	 * Diese Funktion erstellt die Tabelle mit den von der P2P Anwendung "verwalteten"
	 * Dateien zur Verfuegung.
	 * Sie wird ï¿½ber das Beobachtermuster ausgeloest.
	 *
	 * @author Thomas Gerding & Johannes Bade
	 *
	 */
	public void updateDateiTabelle()
	{

		DefaultTableModel tabellenModell = (DefaultTableModel) dateiTabelle.getModel();
		tabellenModell.setRowCount(0);

		LinkedList tempListe = holeAnwendung().getSystemSoftware().getDateisystem().holeDateien(((PeerToPeerAnwendung)holeAnwendung()).holeVerzeichnis());


		ListIterator it = tempListe.listIterator();
		while (it.hasNext())
		{
			Datei tmpDatei = (Datei) it.next();
			Vector<Comparable> v = new Vector<Comparable>();
			v.add(tmpDatei.getName());
			v.add(tmpDatei.getDateiTyp());
			tabellenModell.addRow(v);
		}
	}


	/**
	 * Diese Funktion erstellt die Tabelle mit den bekannten Peer2Peer
	 * Teilnehmern.
	 * Sie wird ueber das Beobachtermuster ausgeloest.
	 *
	 * @author Thomas Gerding & Johannes Bade
	 *
	 */
	public void updateNetzwerkTabelle()
	{

		DefaultTableModel tabellenModell = (DefaultTableModel) netzwerkTabelle.getModel();
		tabellenModell.setRowCount(0);

		LinkedList tempListe = ((PeerToPeerAnwendung)holeAnwendung()).holeBekanntePeerToPeerTeilnehmer();

		int zaehler = 0;
		ListIterator it = tempListe.listIterator();
		while (it.hasNext())
		{
			String ip = it.next().toString();
			zaehler++;
			Vector v = new Vector();
			v.add(zaehler);
			v.add(ip);
			tabellenModell.addRow(v);
		}
	}


	/**
	 * Diese Funktion erstellt die Tabelle mit den empfangenen Suchergebnissen.
	 * Sie wird ueber das Beobachtermuster ausgeloest.
	 *
	 * @author Thomas Gerding & Johannes Bade
	 *
	 */
	public void updateErgebnisTabelle()
	{
		String tmp;
		DefaultTableModel tabellenModell = (DefaultTableModel) ergebnisTabelle.getModel();
		tabellenModell.setRowCount(0);

		LinkedList tempListe = ((PeerToPeerAnwendung)holeAnwendung()).holeErgebnisse();

		ListIterator it = tempListe.listIterator();
		while (it.hasNext())
		{
			String ergebnis = it.next().toString();

			Vector v = new Vector();

			StringTokenizer tempTokenizer=new StringTokenizer(ergebnis,"/");
			v.add(tempTokenizer.nextToken());
			tmp = tempTokenizer.nextToken();
			v.add(tmp.substring(0, tmp.lastIndexOf(":")));
			v.add(tmp.substring(tmp.lastIndexOf(":")+1));

			tabellenModell.addRow(v);
		}
	}

	/**
	 * Funktion die wï¿½hrend der Eingabe ï¿½berprï¿½ft ob die bisherige Eingabe einen
	 * korrekten Wert darstellt.
	 *
	 * @author Johannes Bade & Thomas Gerding
	 * @param pruefRegel
	 * @param feld
	 */
	public void ipPruefen(JTextField feld)
	{
		if (EingabenUeberpruefung.isGueltig(feld.getText(), EingabenUeberpruefung.musterIpAdresse))
		{
			feld.setForeground(EingabenUeberpruefung.farbeRichtig);
			pruefungOK=true;
		}
		else
		{
			feld.setForeground(EingabenUeberpruefung.farbeFalsch);
			pruefungOK = false;

		}

	}

	/**
	 * Funktion die wï¿½hrend der Eingabe ï¿½berprï¿½ft ob die bisherige Eingabe einen
	 * korrekten Wert darstellt.
	 *
	 * @author Johannes Bade & Thomas Gerding
	 * @param pruefRegel
	 * @param feld
	 */
	public void zahlPruefen(JTextField feld)
	{
		if (EingabenUeberpruefung.isGueltig(feld.getText(), EingabenUeberpruefung.musterNurZahlen))
		{
			feld.setForeground(EingabenUeberpruefung.farbeRichtig);
			zahlOK=true;
		}
		else
		{
			feld.setForeground(EingabenUeberpruefung.farbeFalsch);
			zahlOK = false;

		}

	}

	/**
	 * Sorgt dafuer, dass die DateiTabelle bei Reaktivierung des Fensters aktualisiert wird
	 */
	public void internalFrameActivated(InternalFrameEvent e) {
		this.updateDateiTabelle();
	}

	public void internalFrameClosed(InternalFrameEvent e) {
		this.setVisible(false);
	}

	public void internalFrameClosing(InternalFrameEvent e) {
		this.setVisible(false);
	}

	public void internalFrameDeactivated(InternalFrameEvent e) {

	}

	public void internalFrameDeiconified(InternalFrameEvent e) {
		this.updateDateiTabelle();
	}

	public void internalFrameIconified(InternalFrameEvent e) {
		this.updateDateiTabelle();
	}

	public void internalFrameOpened(InternalFrameEvent e) {
		this.updateDateiTabelle();
	}

	public void update(Observable arg0, Object arg1) {
		updateNetzwerkTabelle();
		updateErgebnisTabelle();
		updateDateiTabelle();
	}
}
