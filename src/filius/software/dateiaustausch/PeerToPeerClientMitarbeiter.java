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

import filius.Main;
import filius.software.system.Datei;
import filius.software.transportschicht.TCPSocket;
import filius.software.www.HTTPNachricht;

/**
 * In dieser Klasse werden die Mitarbeiter-Threads zum Versand von Anfragen und
 * zur Verarbeitung der Antworten implementiert.
 *
 */
class PeerToPeerClientMitarbeiter extends Thread {

	/**
	 * Der Thread kann verschiedene Funktionalitaeten erfuellen. Dazu stehen
	 * verschiedene Modi zur verfuegung.:
	 * <ul>
	 * <li> Versenden einer Ping-Nachricht und Verarbeitung eingehender
	 * Pong-Antworten </li>
	 * <li> Versenden einer Suchanfrage (Query) und Verarbeitung der Antworten
	 * darauf. </li>
	 * <li> Versenden von HTTP-GET-Anfragen und verarbeiten der Antworten mit
	 * der angeforderten Datei. </li>
	 * </ul>
	 */
	private static final int PING = 1, QUERY = 3, HTTP = 4;

	/** die verwaltende Instanz der Peer-to-Peer-Anwendung */
	private PeerToPeerAnwendung peerToPeerAnwendung;

	/** die IP, zu der der ClientMitarbeiter eine Verbindung aufbaut */
	private String ip;

	/** die verschickte Nachricht als String */
	private String nachricht;

	/** ob der Thread laeuft */
	private boolean running;

	/**
	 * der Socket, der zum Versenden der Nachricht und zum Empfang
	 * entsprechender Antworten genutzt wird.
	 */
	private TCPSocket socket;

	/**
	 * der Modus, der die Funktionalitaet des Mitarbeiter-Threads bestimmt
	 *
	 * @see filius.software.dateiaustausch.PeerToPeerClientMitarbeiter#HTTP
	 * @see filius.software.dateiaustausch.PeerToPeerClientMitarbeiter#QUERY
	 * @see filius.software.dateiaustausch.PeerToPeerClientMitarbeiter#PING
	 */
	private int modus;

	/**
	 * Konstruktor fuer einen Mitarbeiter-Thread zur Verarbeitung einer
	 * Suchanfrage oder einer Ping-Nachricht
	 *
	 * @param peerToPeerAnwendung
	 * @param ip
	 * @param paket
	 * @see filius.software.dateiaustausch.PeerToPeerClientMitarbeiter#QUERY
	 * @see filius.software.dateiaustausch.PeerToPeerClientMitarbeiter#PING
	 */
	public PeerToPeerClientMitarbeiter(PeerToPeerAnwendung peerToPeerAnwendung,
			String ip, PeerToPeerPaket paket) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerClientMitarbeiter), constr: PeerToPeerClientMitarbeiter("+peerToPeerAnwendung+","+ip+","+paket+")");
		this.peerToPeerAnwendung = peerToPeerAnwendung;
		this.ip = ip;
		this.nachricht = paket.toString();

		if (paket instanceof PingPaket)
			modus = PING;
		else
			modus = QUERY;

		running = true;
	}

	/**
	 * Konstruktor fuer einen Mitarbeiter-Thread zur Verarbeitung einer
	 * HTTP-GET-Anfrage
	 *
	 * @param peerToPeerAnwendung
	 * @param ip
	 * @param paket
	 * @see filius.software.dateiaustausch.PeerToPeerClientMitarbeiter#HTTP
	 */
	public PeerToPeerClientMitarbeiter(PeerToPeerAnwendung anwendung,
			String ip, String dateiname) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerClientMitarbeiter), constr: PeerToPeerClientMitarbeiter("+anwendung+","+ip+","+dateiname+")");
		HTTPNachricht nachricht;

		this.ip = ip;
		peerToPeerAnwendung = anwendung;
		modus = HTTP;

		nachricht = new HTTPNachricht(HTTPNachricht.CLIENT);
		nachricht.setPfad(dateiname);
		nachricht.setHost(ip);
		nachricht.setMethod(HTTPNachricht.GET);

		this.nachricht = nachricht.toString();

		running = true;
	}

	/**
	 * Hier wird die Funktionalitaet des Threads implementiert.
	 * <ol>
	 * <li> Aufbau einer TCP/IP-Verbindung zur Ziel-IP-Adresse </li>
	 * <li> Versenden der im Konstruktor initialisierten Nachricht </li>
	 * <li> Verarbeiten der Antworten:
	 * <ul>
	 * <li> im HTTP-Modus: empfangen der Datei und speichern im lokalen
	 * Dateisystem </li>
	 * <li> im QUERY- und PING-Modus: Empfang und Verarbeitung aller eingehenden
	 * Nachrichten bis der Thread beendet wird. </li>
	 * </ul>
	 * </li>
	 * <li> schliessen des Sockets fuer die TCP/IP-Verbindung </li>
	 * </ol>
	 */
	public void run() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerClientMitarbeiter), run()");
		String antwort;
		HTTPNachricht http, abfrage;
		QueryHitPaket queryHitPaket;
		PongPaket pongPaket;
		Datei datei;

		try {
			socket = new TCPSocket(
					this.peerToPeerAnwendung.getSystemSoftware(), ip, 6346);

			socket.verbinden();

			socket.senden(nachricht);

			if (modus == HTTP) {
				antwort = socket.empfangen();
				http = new HTTPNachricht(antwort);

				if (http.getStatusCode() == 200) {
					abfrage = new HTTPNachricht(nachricht);
					datei = new Datei();
					datei.setDateiTyp(http.getContentType());
					datei.setName(abfrage.getPfad());
					datei.setDateiInhalt(http.getDaten());

					// peerToPeerAnwendung.speicherDatei(http.getDaten());
					peerToPeerAnwendung.speicherDatei(datei);
				}
			}
			else if (modus == QUERY) {
				while (running) {
					antwort = socket.empfangen();
					if (antwort != null) {
						queryHitPaket = new QueryHitPaket(antwort);
						// wenn es zurueck geschickt wird
						peerToPeerAnwendung.verarbeiteQueryHit(queryHitPaket);
					}
				}
			}
			else if (modus == PING) {
				while (running) {
					antwort = socket.empfangen();
					//Main.debug.println(getClass()
					//		+"\n\tPong-Nachricht an "
					//		+peerToPeerAnwendung.getSystemSoftware().getKnoten().getName()
					//		+" angekommen: "+antwort);
					if (antwort != null) {
						pongPaket = new PongPaket(antwort);
						peerToPeerAnwendung.verarbeitePong(pongPaket);
					}
				}
			}
			socket.schliessen();
		}
		catch (Exception e) {
			e.printStackTrace(Main.debug);
		}
	}

	/** Methode zum beenden des Mitarbeiter-Threads */
	public void beenden() {
		running = false;
		socket.beenden();
	}
}
