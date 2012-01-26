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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ListIterator;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import filius.Main;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;

public class PanelCompiler extends JPanel implements I18n, Runnable {

	private static final long serialVersionUID = 1L;

	private String[] quelltextDateien;

	private String anwendungsKlasse;

	private String anwendungsName;

	private JEditorPane ausgabe;

	private JProgressBar progressBar;

	private boolean fehlerfreiKompiliert = false;

	public PanelCompiler(String[] quelltextDateien, String anwendungsName,
			String anwendungsKlasse) {
		super();
	  	Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+", constr: PanelCompiler("+quelltextDateien+","+anwendungsName+","+anwendungsKlasse+")");
		this.quelltextDateien = quelltextDateien;
		this.anwendungsKlasse = anwendungsKlasse;
		this.anwendungsName = anwendungsName;

		setPreferredSize(new Dimension(700, 460));
		setMaximumSize(new Dimension(700, 460));

		initKomponenten();
	}

	private void initKomponenten() {
	  	Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", initKomponenten()");
		JScrollPane scrollPane;

		this.setBorder(new EtchedBorder());

		ausgabe = new JEditorPane();

		scrollPane = new JScrollPane(ausgabe);
		scrollPane.setPreferredSize(new Dimension(600, 300));

		add(scrollPane, BorderLayout.CENTER);

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(600, 20));
		add(progressBar, BorderLayout.SOUTH);
	}


	public void run() {
	  	Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", run()");

		progressBar.setString(messages.getString("panelcompiler_msg1")
				+" "+ anwendungsName);

		if (!kompilieren(quelltextDateien)) {
			ausgabe.setContentType("text/html");
			ausgabe
					.setText(messages.getString("panelcompiler_msg2"));
		}
		else if (fehlerfreiKompiliert) {
			ausgabe.setContentType("text/html");
			ausgabe.setText(messages.getString("panelcompiler_msg3"));
		}

		remove(progressBar);
		updateUI();
	}

	public void speichern() {
	  	Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", speichern()");
		boolean vorhanden = false;
		FileWriter fw = null;
		ListIterator it;
		HashMap map;

		if (fehlerfreiKompiliert) {
			try {
				it = Information.getInformation().ladeProgrammListe().listIterator();
				while (it.hasNext() && !vorhanden) {
					map = (HashMap) it.next();
					if (map.get("Klasse").equals(
							"filius.software.clientserver." + anwendungsKlasse)) {
						vorhanden = true;
					}
				}
			}
			catch (IOException e1) {
				e1.printStackTrace(Main.debug);
			}

			if (!vorhanden) {
				try {
					fw = new FileWriter(Information.getInformation().getAnwendungenPfad()
							+ "EigeneAnwendungen.txt", true);
					fw.write("\n" + anwendungsName
							+ ";filius.software.clientserver."
							+ anwendungsKlasse
							+ ";filius.gui.anwendungssicht.GUIApplication"
							+ anwendungsKlasse
							+ "Window;gfx/desktop/icon_clientbaustein.png");
				}
				catch (IOException e) {
					Main.debug.println("EXCEPTION ("+this.hashCode()+"): Konnte Datei nicht erstellen");
					ausgabe.setContentType("text/html");
					ausgabe
							.setText(ausgabe.getText()
									+ messages.getString("panelcompiler_msg4"));
				}
				finally {
					if (fw != null) try {
						fw.close();
					}
					catch (IOException e) {}
				}
			}
		}
		else {

		}
	}

	private boolean kompilieren(String[] quelltextDateien) {
	  	Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", kompilieren()");
		StringWriter strWriter = new StringWriter();
		StandardJavaFileManager fileManager;
		JavaCompiler jc;
		Iterable<? extends JavaFileObject> kompilierEinheiten;
		File[] dateien;

		jc = ToolProvider.getSystemJavaCompiler();

		if (jc == null) {
			Main.debug.println("ERROR ("+this.hashCode()+"): Kein Java-Compiler erzeugt!");
			fehlerfreiKompiliert = false;

			return false;
		}
		else {
			fileManager = jc.getStandardFileManager(null, null, null);

			dateien = new File[quelltextDateien.length];
			for (int i = 0; i < dateien.length; i++) {
				dateien[i] = new File(quelltextDateien[i]);
			}

			kompilierEinheiten = fileManager.getJavaFileObjectsFromFiles(Arrays
					.asList(dateien));
			fehlerfreiKompiliert = jc.getTask(strWriter, fileManager, null, null, null,
					kompilierEinheiten).call();
			if (!fehlerfreiKompiliert) {
				ausgabe.setContentType("text/plain");
				ausgabe.setText(strWriter.toString());
			}
			return true;
		}
	}
}
