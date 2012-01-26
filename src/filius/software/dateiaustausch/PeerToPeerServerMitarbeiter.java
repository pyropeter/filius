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
package filius.software.dateiaustausch;

import java.util.LinkedList;

import filius.Main;
import filius.software.clientserver.ServerMitarbeiter;
import filius.software.system.Betriebssystem;
import filius.software.system.Datei;
import filius.software.transportschicht.Socket;
import filius.software.www.HTTPNachricht;

/**
 * In dieser Klasse wird ein Thread implementiert, der eingehende Anfragen an
 * eine Peer-to-Peer-Anwendung verarbeitet.
 */
public class PeerToPeerServerMitarbeiter extends ServerMitarbeiter {

	private PeerToPeerAnwendung peerToPeerAnwendung;

	/** die GUID der eingegangenen zu verarbeitenden Nachricht */
	private int guid;

	/**
	 * Aufruf des Konstruktors der Oberklasse und Initialisierung der
	 * zugehoerigen Instanz von PeerToPeerAnwendung
	 *
	 * @param server
	 * @param socket
	 * @param peerToPeerAnwendung
	 */
	PeerToPeerServerMitarbeiter(PeerToPeerServer server, Socket socket,
			PeerToPeerAnwendung peerToPeerAnwendung) {
		super(server, socket);
		Main.debug.println("INVOKED-2 ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerServerMitarbeiter), constr: PeerToPeerServerMitarbeiter("+server+","+socket+","+peerToPeerAnwendung+")");

		this.peerToPeerAnwendung = peerToPeerAnwendung;
	}

	/** Methode zum versenden von Antwortnachrichten */
	void senden(String nachricht) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerServerMitarbeiter), senden("+nachricht+")");
		if (socket != null && socket.istVerbunden()) {
			try {
				socket.senden(nachricht);
			}
			catch (Exception e) {
				e.printStackTrace(Main.debug);
			}
		}
	}

	/**
	 * Zugriff auf die GUID der Anfrage-Nachricht, die durch diesen
	 * Mitarbeiter-Thread verarbeitet wird.
	 *
	 * @return
	 */
	int holeGuid() {
		return guid;
	}

	/**
	 * Diese Operation verarbeitet eine eingegangene HTTP-Anfrage:
	 * <ol>
	 * <li> Lesen der gesuchten Datei aus lokalem Dateisystem </li>
	 * <li> Wenn die Datei vorhanden ist, Versenden der Datei; wenn die Datei
	 * nicht vorhanden ist, verschicken einer HTTP-Antwort mit dem
	 * Fehler-Status-Code 404 </li>
	 * <li> Versenden der Antwort ueber den geoeffneten Socket </li>
	 * </ol>
	 *
	 * @param element
	 *            die zu verarbeitende HTTP Anfrage in Form eines
	 *            TcpPufferElements
	 */
	private void httpAnfrageVerarbeiten(String nachricht) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerServerMitarbeiter), httpAnfrageVerarbeiten("+nachricht+")");
		HTTPNachricht http, antwort;
		Datei datei;

		http = new HTTPNachricht(nachricht);
		antwort = new HTTPNachricht(HTTPNachricht.SERVER);
		datei = peerToPeerAnwendung.holeDatei(http.getPfad());

		if (datei != null) {
			antwort.setStatusCode(200);
			antwort.setContentType(datei.getDateiTyp());
			antwort.setDaten(datei.getDateiInhalt());
		}
		else {
			antwort.setStatusCode(404);
		}

		try {
			socket.senden(antwort.toString());
		}
		catch (Exception e) {
			e.printStackTrace(Main.debug);
		}
	}

	/**
	 * Methode zur Verarbeitung einer eingehenden Ping-Nachricht.
	 * <ol>
	 * <li> Absender des Ping-Pakets wird der Liste der bekannten Teilnehmer im
	 * Peer-to-Peer-Netzwerk hinzugefuegt. </li>
	 * <li> Erzeugen einer entsprechenden Pong-Nachricht, wenn auf diese
	 * Ping-Nachricht nicht schon geantwortet wurde. </li>
	 * <li> Erhoehung des Hop-Zahlers und Dekrementierung des TTL-Zaehlers der
	 * Ping-Nachricht. </li>
	 * <li> Weiterleitung des Ping-Pakets </li>
	 * </ol>
	 *
	 * @param pingPaket
	 */
	private void verarbeitePing(PingPaket pingPaket) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerServerMitarbeiter), verarbeitePing("+pingPaket+")");
		String pongNachricht;

		peerToPeerAnwendung.hinzuTeilnehmer(pingPaket.getIp());

		pongNachricht = peerToPeerAnwendung.erstellePong(pingPaket).toString();
		if (pongNachricht != null) {
			try {
				socket.senden(pongNachricht);
			}
			catch (Exception e) {
				e.printStackTrace(Main.debug);
			}
		}

		pingPaket.setHops(pingPaket.getHops() + 1);
		pingPaket.setTtl(pingPaket.getTtl() - 1);

		guid = pingPaket.getGuid();
		peerToPeerAnwendung.sendePing(pingPaket, socket.holeZielIPAdresse());
	}

	/**
	 * Methode zur Verarbeitung einer eingehenden Suchanfrage (Query).
	 * <ol>
	 * <li> Erstellen der Ergebnisliste fuer eine Suchanfrage. Wenn auf die
	 * Query-Nachricht bereits geantwortet wurde, wird keine Liste erzeugt.
	 * Ausserdem wird damit zugleich veranlasst, dass die Suchanfrage
	 * ggf. weitergeleitet wird.
	 * </li>
	 * <li>
	 * <ul>
	 * <li> Wenn eine Liste mit mindestens einem Eintrag vorliegt, wird fuer
	 * jede Datei eine Antwortnachricht ueber den Socket verschickt (Query-Hit).
	 * </li>
	 * </ul>
	 * </li>
	 * </ol>
	 *
	 * @param queryPaket
	 */
	private void verarbeiteQuery(QueryPaket queryPaket) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerServerMitarbeiter), verarbeiteQuery("+queryPaket+")");
		LinkedList<Datei> dateien;
		Datei aktuelleDatei;
		Betriebssystem bs;
		QueryHitPaket antwortPaket;

		guid = queryPaket.getGuid();

		dateien = peerToPeerAnwendung.verarbeiteAnfrage(socket
				.holeZielIPAdresse(), queryPaket);

		if (dateien != null && dateien.size() > 0) {
			bs = (Betriebssystem) peerToPeerAnwendung.getSystemSoftware();

			for (int i = 0; i < dateien.size(); i++) {
				aktuelleDatei = (Datei) dateien.get(i);
				antwortPaket = new QueryHitPaket(dateien.size(), 6346, bs
						.holeIPAdresse(), "2", "", " ");
				antwortPaket.setGuid(queryPaket.getGuid());
				antwortPaket.setHops(0);
				antwortPaket.setTtl(8);
				antwortPaket.setErgebnis(aktuelleDatei.getName() + ": "
						+ aktuelleDatei.holeGroesse() + " B");

				try {
					socket.senden(antwortPaket.toString());
				}
				catch (Exception e) {
					e.printStackTrace(Main.debug);
				}
			}
		}
	}

	/**
	 * Wenn eine Nachricht auf dem zu ueberwachenden Socket eintrifft, wird die
	 * Verarbeitung an diese Methode delegiert. <br />
	 * Unterschieden wird eine HTTP-GET-Anfrage, eine eingehende Ping-Nachricht
	 * und eine eingehende Suchanfrage (Query).
	 */
	protected void verarbeiteNachricht(String nachricht) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerServerMitarbeiter), verarbeiteNachricht("+nachricht+")");
		PeerToPeerPaket paket;
		PingPaket pingPaket;
		QueryPaket queryPaket;

		if (nachricht != null) {
			if (nachricht.startsWith("GET")) {
				httpAnfrageVerarbeiten(nachricht);
			}
			else {
				paket = new PeerToPeerPaket(nachricht);
				guid = paket.getGuid();

				if (paket.getPayload().equals("0x00")) {
					pingPaket = new PingPaket(nachricht);
					verarbeitePing(pingPaket);
				}
				else if (paket.getPayload().equals("0x80")) {
					queryPaket = new QueryPaket(nachricht);
					verarbeiteQuery(queryPaket);
				}
			}
		}
	}
}
