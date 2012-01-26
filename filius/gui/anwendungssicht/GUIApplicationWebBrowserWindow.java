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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.FormSubmitEvent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;

import filius.Main;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.Information;
import filius.software.www.HTTPNachricht;
import filius.software.www.WebBrowser;

public class GUIApplicationWebBrowserWindow extends GUIApplicationWindow {

	private static final long serialVersionUID = 1L;

	private JPanel browserPanel;

	private JTextField urlFeld;

	private JEditorPane anzeigeFeld;

	private JButton goButton;

	public GUIApplicationWebBrowserWindow(final GUIDesktopPanel desktop,
			String appName) {
		super(desktop, appName);

		HTMLEditorKit ek;

		browserPanel = new JPanel(new BorderLayout());
		getContentPane().add(browserPanel);

		Box topBox = Box.createHorizontalBox();

		urlFeld = new JTextField("http://");
		urlFeld.setVisible(true);

		topBox.add(urlFeld);
		topBox.add(Box.createHorizontalStrut(5)); // Platz zw. urlFeld und
		// senden

		goButton = new JButton(messages.getString("webbrowser_msg2"));
		topBox.add(goButton);
		topBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		browserPanel.add(topBox, BorderLayout.NORTH);

		Box middleBox = Box.createHorizontalBox();
		middleBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		/* ActionListener */
		ActionListener al = new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				URL url;

				url = erzeugeURL(urlFeld.getText());
				abrufenWebseite(url, null);
			}
		};
		goButton.addActionListener(al);

		/* KeyListener */
		urlFeld.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				URL url;

				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					//Main.debug.println("\t"+getClass()+": enter pressed");
					url = erzeugeURL(urlFeld.getText());
					abrufenWebseite(url, null);
				}
			}

			public void keyReleased(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
			}
		});

		ek = new HTMLEditorKit(); // Braucht er für SubmitEvent!
		ek.setAutoFormSubmission(false); // muss!

		anzeigeFeld = new JEditorPane();
		anzeigeFeld.setEditorKit(ek);
		anzeigeFeld.setContentType("text/html"); // text/html muss bleiben
		// wegen folgendem Quelltext:
		anzeigeFeld
				.setText("<html><head><base href=\"file:bilder\"></head><body margin=\"0\">"
						+ "<center><img src=\"browser_waterwolf_logo.png\" align=\"top\"></center>"
						+ "</font>" + "</body></html>");

//		filius.rahmenprogramm.SzenarioVerwaltung.kopiereDatei(Information
//				.getInformation().getProgrammPfad()
//				+ "gfx/desktop/browser_waterwolf_logo.png", Information
//				.getInformation().getTempPfad()
//				+ "browser_waterwolf_logo.png");
		filius.rahmenprogramm.SzenarioVerwaltung.saveStream(getClass().getResourceAsStream("/gfx/desktop/browser_waterwolf_logo.png"),
				Information.getInformation().getTempPfad() + "browser_waterwolf_logo.png");
		try {
			((HTMLDocument) anzeigeFeld.getDocument()).setBase(new URL("file:"
					+ Information.getInformation().getTempPfad()));
		} catch (MalformedURLException e1) {
			e1.printStackTrace(Main.debug);
		}
		anzeigeFeld.setEditable(false);
		anzeigeFeld.setBorder(null);
		hyperLinkListener(anzeigeFeld);
		anzeigeFeld.setVisible(true);
		JScrollPane spAnzeige = new JScrollPane(anzeigeFeld);

		middleBox.add(spAnzeige);

		browserPanel.add(middleBox, BorderLayout.CENTER);

		this.pack();
	}

	private URL erzeugeURL(String ressource) {
		URL url = null;
		String[] teilstrings;
		String host = null, pfad = "";

		teilstrings = ressource.split("/");
		// Fuer den Fall, dass URL-Eingabe mit Hostadresse beginnt
		if (teilstrings.length > 0 && !teilstrings[0].equalsIgnoreCase("http:")) {
			if (EingabenUeberpruefung.isGueltig(teilstrings[0],
					EingabenUeberpruefung.musterDomain)
					|| EingabenUeberpruefung.isGueltig(teilstrings[0],
							EingabenUeberpruefung.musterIpAdresse)) {
				host = teilstrings[0];

				for (int i = 1; i < teilstrings.length; i++) {
					pfad = pfad + "/" + teilstrings[i];
				}
			}
		}
		// Fuer den Fall, dass URL-Eingabe mit http:// beginnt
		if (teilstrings.length > 2 && teilstrings[0].equalsIgnoreCase("http:")) {
			if (EingabenUeberpruefung.isGueltig(teilstrings[2],
					EingabenUeberpruefung.musterDomain)
					|| EingabenUeberpruefung.isGueltig(teilstrings[2],
							EingabenUeberpruefung.musterIpAdresse)) {
				host = teilstrings[2];
			}
			for (int i = 3; i < teilstrings.length; i++) {
				pfad = pfad + "/" + teilstrings[i];
			}
		}

		if (host != null) {
			if (pfad.equals(""))
				pfad = "/";

			try {
				url = new URL("http", host, pfad);
			} catch (MalformedURLException e) {
				e.printStackTrace(Main.debug);
			}
		}

		return url;
	}

	private void abrufenWebseite(URL url, String postDaten) {
		String host;

		if (url != null) {
			if (postDaten == null)
				((WebBrowser) holeAnwendung()).holeWebseite(url);
			else
				((WebBrowser) holeAnwendung()).holeWebseite(url, postDaten);

			if (url.getHost() == null || url.getHost().equals("")) {
				host = ((WebBrowser) holeAnwendung()).holeHost();
			} else {
				host = url.getHost();
			}
			urlFeld.setText(url.getProtocol() + "://" + host + url.getPath());
			setTitle(url.getProtocol() + "://" + host + url.getPath());

		} else {
			urlFeld.setText("http://");
		}
	}

	private void initialisiereWebseite(String quelltext) {
		Parser parser;
		NodeList liste;
		Tag tag;

		anzeigeFeld.setContentType("text/html");
		anzeigeFeld.setText(quelltext);

		parser = Parser.createParser(quelltext, null);

		try {
			liste = parser.parse(new TagNameFilter("title"));
			if (liste.size() > 0) {
				tag = (Tag) liste.elementAt(0);
				if (tag.getChildren() != null && tag.getChildren().size() > 0)
					setTitle(tag.getChildren().elementAt(0).toHtml());
			}
		} catch (Exception e) {
			e.printStackTrace(Main.debug);
		}
	}

	private void hyperLinkListener(JEditorPane editorPane) {
		editorPane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				URL url = null, tmp;
				String pfad;

				// Hier wird auf einen Klick reagiert
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					tmp = ((HTMLDocument) anzeigeFeld.getDocument()).getBase();

					if (e.getURL().getProtocol().equals(tmp.getProtocol())) {
						try {
							pfad = e.getURL().getFile().replace(tmp.getFile(), "/");
							url = new URL("http", "", pfad);
						} catch (MalformedURLException e1) {
							e1.printStackTrace(Main.debug);
						}
					}
					else {
						url = e.getURL();
					}

					// in diesem Fall kam das Event vom Submit-Button:
					if (e instanceof FormSubmitEvent) {
						FormSubmitEvent evt = (FormSubmitEvent) e;
						// Zerlegen erfolgt erst im Server
						String postDatenteil = evt.getData();
						abrufenWebseite(url, postDatenteil);
					}
					else {
						abrufenWebseite(url, null);
					}

				}

			}
		});
	}

	public void update(Observable arg0, Object arg1) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (GUIApplicationWebBrowserWindow), update("+arg0+","+arg1+")");
		if (arg1 == null) {
			anzeigeFeld.updateUI();
		} else if (arg1 instanceof HTTPNachricht) {
			if (((HTTPNachricht) arg1).getDaten() == null) {
				anzeigeFeld.updateUI();
			} else {
				initialisiereWebseite(((HTTPNachricht) arg1).getDaten());
			}
		} else {
			//Main.debug.println(arg1);
		}
	}
}
