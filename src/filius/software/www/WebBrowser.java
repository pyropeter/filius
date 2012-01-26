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
package filius.software.www;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ListIterator;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.visitors.TagFindingVisitor;

import filius.Main;
import filius.exception.VerbindungsException;
import filius.rahmenprogramm.Base64;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.software.clientserver.ClientAnwendung;
import filius.software.transportschicht.Socket;
import filius.software.transportschicht.TCPSocket;

/**
 * Diese Klasse implementiert die Funktionalitaet eines HTTP-Clients fuer einen
 * Webbrowser. Dazu werden folgende Funktionen zur Verfuegung gestellt:
 * <ol>
 * <li> Zum Abruf einer Webseite von einem Webserver gibt es die Methoden
 * <b>holeRessource(url: URL)</b> fuer eine GET-Abfrage und </li>
 * <li> <b>holeRessource(url: URL, post: String)</b> fuer eine POST-Abfrage mit
 * Daten im Datenteil einer HTTP-Nachricht. </li>
 * </ol>
 * Diese Methoden haben keinen Rueckgabewert und blockieren auch nicht. Die vom
 * Server gelieferten Daten werden an den Beobachter als HTTPNachricht
 * weitergegeben.
 *
 */
public class WebBrowser extends ClientAnwendung implements I18n {

	private static final int ABRUF_HTML = 1, ABRUF_IMG = 2;

	private LinkedList<String> bilddateien = new LinkedList<String>();

	private int zustand;

	private String host;

	public void holeWebseite(URL url) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebBrowser), holeWebseite("+url+")");
		Object[] args;

		zustand = ABRUF_HTML;

		if (socket != null) {
			socket.schliessen();
			socket = null;
		}

		args = new Object[2];
		args[0] = url;
		args[1] = "";
		ausfuehren("internHoleRessource", args);
	}

	public void holeWebseite(URL url, String post) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebBrowser), holeWebseite("+url+","+post+")");
		Object[] args;

		zustand = ABRUF_HTML;

		if (socket != null) {
			socket.schliessen();
			socket = null;
		}

		args = new Object[2];
		args[0] = url;
		args[1] = post;
		ausfuehren("internHoleRessource", args);
	}

	public void internHoleRessource(URL url, String post) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebBrowser), internHoleRessource("+url+","+post+")");
		HTTPNachricht nachricht;
		HTTPNachricht fehler;
		Socket aktuellerSocket = socket;

		nachricht = new HTTPNachricht(HTTPNachricht.CLIENT);
		nachricht.setPfad(url.getFile());
		if (url.getHost() != null && !url.getHost().equals("")) {
			host = url.getHost();
		}
		nachricht.setHost(host);
		if (nachricht.getHost() != null && !nachricht.getHost().equals("")) {
			if (post != null && !post.equals("")) {
				nachricht.setMethod(HTTPNachricht.POST);
				nachricht.setDaten(post);
			}
			else {
				nachricht.setMethod(HTTPNachricht.GET);
			}

			if (zustand == ABRUF_HTML || aktuellerSocket == null) {
				try {
					socket = new TCPSocket(getSystemSoftware(), host, 80);
					aktuellerSocket = socket;
				}
				catch (VerbindungsException e) {
					if (zustand == ABRUF_HTML) {
						fehler = new HTTPNachricht(HTTPNachricht.CLIENT);
						fehler.setDaten(erzeugeHtmlFehlermeldung(0));
						benachrichtigeBeobachter(fehler);
					}
					// print not necessary, since this is a controlled exception.
					//e.printStackTrace(Main.debug);
				}
			}
			if (aktuellerSocket != null && !aktuellerSocket.istVerbunden() && aktuellerSocket == socket) {
				try {
					aktuellerSocket.verbinden();
				}
				catch (Exception e) {
					if (zustand == ABRUF_HTML && aktuellerSocket == socket) {
						fehler = new HTTPNachricht(HTTPNachricht.CLIENT);
						fehler.setDaten(erzeugeHtmlFehlermeldung(0));
						benachrichtigeBeobachter(fehler);
					}
					e.printStackTrace(Main.debug);
				}

			}

			if (aktuellerSocket != null && aktuellerSocket.istVerbunden() && aktuellerSocket == socket) {
				try {
					aktuellerSocket.senden(nachricht.toString());
					verarbeiteNachricht();
				}
				catch (Exception e) {
					e.printStackTrace(Main.debug);
				}
			}
		}
	}

	public String holeHost() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebBrowser), holeHost()");
		return host;
	}

	public void starten() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebBrowser), starten()");
		super.starten();
		bilddateien = new LinkedList<String>();
	}

	/**
	 * liest eine reale Textdatei vom Format .txt ein. Diese befinden sich im
	 * Ordner /config
	 */
	private String einlesenTextdatei(String datei)
			throws FileNotFoundException, IOException {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebBrowser), einlesenTextdatei("+datei+")");
		BufferedReader dateiPuffer;
		StringBuffer fullFile;
		String input;

		dateiPuffer = new BufferedReader(new FileReader(datei));
		fullFile = new StringBuffer();
		while ((input = dateiPuffer.readLine()) != null) {
			fullFile.append(input + "\n");
		}
		return fullFile.toString();
	}

	private String erzeugeHtmlFehlermeldung(int statusCode) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebBrowser), erzeugeHtmlFehlermeldung("+statusCode+")");
		String quelltext;
		String dateipfad;
		String meldung;

		if (statusCode == 0) {
			quelltext = messages.getString("sw_webbrowser_msg1");
		}
		else {
			dateipfad = Information.getInformation().getProgrammPfad()
					+ "config/http_fehler.txt";
			try {
				quelltext = einlesenTextdatei(dateipfad);
			}
			catch (Exception e) {
				quelltext = messages.getString("sw_webbrowser_msg2");
				e.printStackTrace(Main.debug);
			}

			quelltext = quelltext.replace(":code:", "" + statusCode);
			meldung = HTTPNachricht.holeStatusNachricht(statusCode);
			quelltext = quelltext.replace(":meldung:", meldung);
		}

		return quelltext;
	}

	/**
	 * Methode zur Verarbeitung von IMG-Tags. Mit einem Parser werden IMG-Tags
	 * im uebergebenen Quelltext gesucht. Alle Bilddateien werden in eine Liste
	 * geschrieben. Fuer jedes Bild wird dann ein Aufruf der Methode
	 * internHoleRessource() in die Befehlswarteschlange geschrieben.
	 *
	 * @param quelltext
	 * @param host
	 */
	private void verarbeiteIMGTags(String quelltext, String host) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebBrowser), verarbeiteIMGTags("+quelltext+","+host+")");
		Parser parser;
		TagFindingVisitor nodeVisitor;
		Node[] nodes;
		ImageTag img;
		ListIterator it;
		String dateipfad;
		URL url;
		Object[] args;

		zustand = ABRUF_IMG;

		parser = Parser.createParser(quelltext, null);

		nodeVisitor = new TagFindingVisitor(new String[] { "img" });
		try {
			parser.visitAllNodesWith(nodeVisitor);
			nodes = nodeVisitor.getTags(0);

			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i] instanceof ImageTag) {
					img = (ImageTag) nodes[i];
					synchronized (bilddateien) {
						bilddateien.add(img.getImageURL());
					}
				}

			}
		}
		catch (Exception e) {
			e.printStackTrace(Main.debug);
		}

		/*
		 * Liste der gefundenen IMG SRCs wird iteriert, die einzelnen Bilder
		 * werden abgerufen
		 */
		synchronized (bilddateien) {
			it = bilddateien.listIterator();
			while (it.hasNext()) {

				dateipfad = it.next().toString();
				try {
					url = new URL("http", host, dateipfad);
					args = new Object[2];
					args[0] = url;
					args[1] = "";
					ausfuehren("internHoleRessource", args);
				}
				catch (MalformedURLException e) {
					e.printStackTrace(Main.debug);
				}
			}
		}
	}

	/**
	 * Mit dieser Metode wird eine HTTP-Nachricht empfangen und
	 * weiterverarbeitet. Wenn waehrend des Empfangs ein Fehler auftritt oder
	 * keine Nachricht empfangen wird, werden Beobachter darueber informiert,
	 * dass ein Verbindungsfehler aufgetreten ist. <br />
	 * Wenn eine Nachricht empfangen wurde, wird zunaechst der HTTP-Statuscode
	 * geprueft. Nur wenn dieser 200 ist, erfolgt eine weitere Verarbeitung.
	 * Andernfalls wird eine Fehlernachricht erzeugt und an Beobachter weiter
	 * gegeben, wenn es sich um die Anwort auf eine Webseitenanfrage handelt.
	 * Wenn es sich um die Antwort auf die Anfrage nach einem Bild handelt, wird
	 * 'null' an die Beobachter weiter gegeben! <br />
	 * Zuletzt wird der Socket geschlossen, wenn keine Antworten auf Anfragen
	 * fuer Bilddateien mehr zu bearbeiten sind.
	 *
	 */
	protected void verarbeiteNachricht() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebBrowser), verarbeiteNachricht()");
		HTTPNachricht antwort;
		String contentType;
		String dateipfad;
		String nachricht = null;
		Socket aktuellerSocket = socket;

		try {
			// blockieren, bis eine Nachricht eintrifft
			nachricht = aktuellerSocket.empfangen();
		}
		catch (Exception e) {
			nachricht = null;
		}

//		Main.debug.println(getClass() + "\n\tverarbeiteNachricht()"
//				+ "\n>>>>\n" + nachricht + "\n<<<<");

		if (nachricht != null) {
			antwort = new HTTPNachricht(nachricht);

			// Nur wenn der HTTP-Statuscode 200 ist,
			// erfolgt die weitere Verarbeitung.
			if (antwort.getStatusCode() == 200) {
				contentType = antwort.getContentType();

				if (contentType == null) {
					antwort.setDaten("");
				}
				else if (contentType.equalsIgnoreCase(HTTPNachricht.TEXT_HTML)
						&& antwort.getDaten() != null) {
					verarbeiteIMGTags(antwort.getDaten(), antwort.getHost());
				}
				else if (contentType.equalsIgnoreCase(HTTPNachricht.IMAGE_BMP)
						|| contentType
								.equalsIgnoreCase(HTTPNachricht.IMAGE_GIF)
						|| contentType
								.equalsIgnoreCase(HTTPNachricht.IMAGE_JPG)
						|| contentType
								.equalsIgnoreCase(HTTPNachricht.IMAGE_PNG)) {

					synchronized (bilddateien) {
						if (bilddateien.size() > 0) {
							dateipfad = bilddateien.removeFirst();
							Base64.decodeToFile(antwort.getDaten(), Information.getInformation().getTempPfad()+ dateipfad);
						}
					}
					antwort = null;
				}
			}
			else if (zustand == ABRUF_HTML && aktuellerSocket == socket) {
				antwort.setDaten(erzeugeHtmlFehlermeldung(antwort
						.getStatusCode()));
			}
			else {
				antwort = null;
			}

			if (aktuellerSocket == socket)
			benachrichtigeBeobachter(antwort);

		}
		else if (zustand == ABRUF_HTML && aktuellerSocket == socket) {
			antwort = new HTTPNachricht(HTTPNachricht.CLIENT);
			antwort.setDaten(erzeugeHtmlFehlermeldung(0));
			benachrichtigeBeobachter(antwort);
		}

		if ((bilddateien == null || bilddateien.size() == 0) && aktuellerSocket == socket) {
			if (socket != null) {
				socket.schliessen();
				socket = null;
			}
		}

	}
}
