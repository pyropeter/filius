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

import filius.Main;
import filius.software.clientserver.ServerMitarbeiter;
import filius.software.system.Datei;
import filius.software.transportschicht.TCPSocket;

/*
 *
 */
public class WebServerMitarbeiter extends ServerMitarbeiter {


	/**
	 * Konstruktor: setzt den webserver, socket und webkonfig. startet
	 * anschließend den Thread
	 */
	public WebServerMitarbeiter(WebServer server, TCPSocket socket) {
		super(server, socket);
	}

	private HTTPNachricht verarbeiteAnfrage(HTTPNachricht anfrage) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebServerMitarbeiter), verarbeiteAnfrage("+anfrage+")");
		HTTPNachricht antwort;
		WebServerPlugIn plugin;
		String dateipfad;
		Datei datei;
		String tmp = null;

		antwort = new HTTPNachricht(HTTPNachricht.SERVER);

		if (anfrage.getPfad().equals("/")) {
			dateipfad = "index.html";
		}
		else if (anfrage.getPfad().startsWith("/")) {
			dateipfad = anfrage.getPfad().substring(1);
		}
		else {
			dateipfad = anfrage.getPfad();
		}
		
		// add subdirectory in front of 'dateipfad' according to vhost settings
		String vHostRelPath = ((WebServer) server).vhostPrefix(anfrage.getHost());
		if(!vHostRelPath.endsWith("/")) vHostRelPath+="/";
		dateipfad = vHostRelPath + dateipfad;
		if (dateipfad.startsWith("/")) {
			dateipfad = dateipfad.substring(1);
		}

		plugin = ((WebServer) server).holePlugin(dateipfad);
		if (plugin != null) {
			if (anfrage.getMethod().equals(HTTPNachricht.POST)) {
				tmp = plugin.holeHtmlSeite(anfrage.getDaten());
			}
			else {
				tmp = plugin.holeHtmlSeite(null);
			}

			if (tmp != null) {
				antwort.setDaten(tmp);
				antwort.setContentType(HTTPNachricht.TEXT_HTML);
				antwort.setStatusCode(200);
			}
			else {
				antwort.setStatusCode(500);
			}
		}
		else {
			datei = ((WebServer) server).dateiLiefern(dateipfad);
			if (datei != null) {
				antwort.setDaten(datei.getDateiInhalt());
				if (datei.getName().endsWith("html")) {
					antwort.setContentType(HTTPNachricht.TEXT_HTML);
					antwort.setStatusCode(200);
				}
				else if (datei.getName().endsWith("png")) {
					antwort.setContentType(HTTPNachricht.IMAGE_PNG);
					antwort.setStatusCode(200);
				}
				else if (datei.getName().endsWith("bmp")) {
					antwort.setContentType(HTTPNachricht.IMAGE_BMP);
					antwort.setStatusCode(200);
				}
				else if (datei.getName().endsWith("gif")) {
					antwort.setContentType(HTTPNachricht.IMAGE_GIF);
					antwort.setStatusCode(200);
				}
				else if (datei.getName().endsWith("jpg")) {
					antwort.setContentType(HTTPNachricht.IMAGE_JPG);
					antwort.setStatusCode(200);
				}
				else {
					antwort.setStatusCode(415);
				}
			}
			else {
				antwort.setStatusCode(404);
			}
		}

		return antwort;
	}

	protected void verarbeiteNachricht(String nachricht) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebServerMitarbeiter), verarbeiteNachricht("+nachricht+")");
		HTTPNachricht anfrage, antwort;

		anfrage = new HTTPNachricht(nachricht);

		if (anfrage.getMethod().equals(HTTPNachricht.GET)
				|| anfrage.getMethod().equals(HTTPNachricht.POST)) {
			antwort = verarbeiteAnfrage(anfrage);
		}
		else {
			antwort = new HTTPNachricht(HTTPNachricht.SERVER);
			antwort.setStatusCode(501);
		}

		sendeNachricht(antwort.toString());
	}

}
