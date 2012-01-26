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
package filius.gui.quelltextsicht;

import java.awt.Font;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import filius.Main;

public class PanelQuelltext extends JTabbedPane {

	private static final long serialVersionUID = 1L;

	/**
	 * Die Editoren fuer die Quelltextdateien. Als Schluessel wird der Dateipfad
	 * der Quelltextdatei und als Wert das EditorPane verwendet.
	 */
	private Hashtable<String, JEditorPane> editorPanes;

	/** Konstruktor zur Initialisierung der GUI-Komponenten */
	public PanelQuelltext() {
		super();

		editorPanes = new Hashtable<String, JEditorPane>();
	}


	/**
	 * Zum Hinzufuegen eines neuen Editors zur Bearbeitung des Quelltextes.
	 *
	 * @param neueDatei
	 *            Dateipfad der Datei, in der der bearbeitete Quelltext
	 *            gespeichert werden soll. Dieser Dateiname muss den neuen
	 *            Klassennamen enthalten (z. B. /home/foo/NeueKlasse.java)
	 */
	public void hinzuEditor(String klassenName, String neueDatei) {
		JScrollPane scrollPane;
		JEditorPane editorPane;

		editorPane = new JEditorPane();
		editorPane.setEditable(true);
		editorPane.setEnabled(true);
		editorPane.setContentType("text/plain");
		editorPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
		editorPane.setText(ladeQuelltext(neueDatei));

		scrollPane = new JScrollPane(editorPane);
		add(klassenName, scrollPane);

		editorPanes.put(neueDatei, editorPane);
	}

	private String ladeQuelltext(String datei) {
		String quellText = "";
		RandomAccessFile quellDatei = null;

		try {
			quellDatei = new RandomAccessFile(datei, "r");
			for (String line; (line = quellDatei.readLine()) != null;) {
				quellText += line + "\n";
			}
		}
		catch (Exception e1) {
			e1.printStackTrace(Main.debug);
		}
		finally {
			try {
				quellDatei.close();
			}
			catch (IOException e) {
				e.printStackTrace(Main.debug);
			}
		}
		return quellText;
	}

	/**
	 * Methode zum Speichern aller bearbeiteten bzw. neu erstellten Quelltexte
	 */
	public void speicherQuelltexte() {
		Enumeration dateipfade, editors;
		JEditorPane pane;
		String pfad;
		FileWriter writer;

		dateipfade = editorPanes.keys();
		editors = editorPanes.elements();
		while (dateipfade.hasMoreElements() && editors.hasMoreElements()) {
			pfad = (String) dateipfade.nextElement();
			pane = (JEditorPane) editors.nextElement();

			try {
				writer = new FileWriter(pfad, false);
				writer.write(pane.getText());
				writer.close();
			}
			catch (IOException e2) {
				e2.printStackTrace(Main.debug);
			}
		}
	}
}
