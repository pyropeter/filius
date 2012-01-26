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
import java.util.ListIterator;

import filius.Main;
import filius.software.Anwendung;

/**
 * In dieser Klasse werden Anfragen an andere Teilnehmer im
 * Peer-To-Peer-Netzwerk verschickt bzw. weitergeleitet und dafuer gesorgt, dass
 * die dazu gehoerigen eingehenden Antworten verarbeitet werden. Dazu wird ein
 * neuer Thread mit einem PeerToPeerClientMitarbeiter gestartet.
 *
 */
class PeerToPeerClient extends Anwendung {

	/**
	 * Die zugehoerige PeerToPeerAnwendung, die diesen Client zur Verarbeitung
	 * von eigenen Anfragen verwendet.
	 */
	private PeerToPeerAnwendung peerToPeerAnwendung;

	/**
	 * Eine Liste mit allen Mitarbeiter-Threads, die zur Verarbeitung der
	 * Antworten auf eigene Anfragen verwendet werden.
	 */
	private LinkedList<PeerToPeerClientMitarbeiter> mitarbeiterEigeneAnfragen = new LinkedList<PeerToPeerClientMitarbeiter>();

	/**
	 * Eine Liste mit allen Mitarbeiter-Threads, die zur Verarbeitung von
	 * Antworten auf fremde, d. h. weitergeleitete, Anfragen verwendet werden.
	 */
	private LinkedList<PeerToPeerClientMitarbeiter> mitarbeiterFremdeAnfragen = new LinkedList<PeerToPeerClientMitarbeiter>();

	/**
	 * Konstruktor, in dem die zugehoerige PeerToPeerAnwendung initialisiert und
	 * der Konstruktor der Oberklasse aufgerufen wird.
	 *
	 * @param peerToPeerAnwendung
	 */
	PeerToPeerClient(PeerToPeerAnwendung peerToPeerAnwendung) {
		super();
		Main.debug.println("INVOKED-2 ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerClient), constr: PeerToPeerClient("+peerToPeerAnwendung+")");

		this.peerToPeerAnwendung = peerToPeerAnwendung;
	}

	/** Zum beenden der Threads, die durch diesen Client gestartet wurden. */
	public void beenden() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerClient), beenden()");
		ListIterator<?> it;
		PeerToPeerClientMitarbeiter m;

		super.beenden();

		it = mitarbeiterEigeneAnfragen.listIterator();
		while (it.hasNext()) {
			m = (PeerToPeerClientMitarbeiter) it.next();
			m.beenden();
		}

		it = mitarbeiterFremdeAnfragen.listIterator();
		while (it.hasNext()) {
			m = (PeerToPeerClientMitarbeiter) it.next();
			m.beenden();
		}
	}

	/**
	 * Verschicken eines eigenden oder weitersenden einer fremden
	 * Ping-Nachricht. Wenn eine IP-Adresse angegeben wird, zu dem diese
	 * Nachricht verschickt werden soll, wird die Ping-Nachricht als eigene
	 * Anfrage behandelt. Eine Ping-Nachricht wird zum Beitreten zu einem
	 * Peer-To-Peer-Netzwerk verschickt.
	 *
	 * @param wohinZuerst
	 *            die IP-Adresse des Servents, mit welchem man sich zuerst
	 *            verbinden moechte
	 * @param pingPaket
	 *            das Ping-Paket, welches verschickt wird
	 * @param peerToPeerServerMitarbeiter
	 *            der peerToPeerServerMitarbeiter, ueber den eine moegliche
	 *            Antwort zurueck geschickt wird
	 * @param nichtIP
	 *            die IP, an die das Paket nicht weitergeleitet werden darf
	 *            (weil es daher kommt)
	 */
	void sendePing(String wohinZuerst, PingPaket pingPaket, String nichtIP) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerClient), sendePing("+wohinZuerst+","+pingPaket+","+nichtIP+")");
		// der erste Schritt => nur an den senden, den man angegeben hat, der
		// kuemmert sich um das weitere fluten
		if (!wohinZuerst.equals("")) {
			PeerToPeerClientMitarbeiter lauscher = new PeerToPeerClientMitarbeiter(
					peerToPeerAnwendung, wohinZuerst, pingPaket);
			mitarbeiterEigeneAnfragen.add(lauscher);
			lauscher.start();
		}
		// Ping weiter fluten (an alle, die in der Liste stehen, ausser an den,
		// von dem es aktuell kam und
		// den, wo es urspruenglich her kam
		else {
			for (int i = 0; i < peerToPeerAnwendung
					.holeBekanntePeerToPeerTeilnehmer().size(); i++) {
				String aktuelle = (String) peerToPeerAnwendung
						.holeBekanntePeerToPeerTeilnehmer().get(i);
				if (aktuelle.equals(nichtIP) == false
						&& aktuelle.equals(pingPaket.getIp()) == false) {
					PeerToPeerClientMitarbeiter lauscher = new PeerToPeerClientMitarbeiter(
							peerToPeerAnwendung, aktuelle, pingPaket);
					mitarbeiterFremdeAnfragen.add(lauscher);
					lauscher.start();
				}
			}
		}
	}

	/**
	 * Damit wird eine initiierte Suchanfrage abgebrochen. D. h., dass alle
	 * Threads, die auf eingehende Antworten auf eine Suchanfrage warten,
	 * beendet werden.
	 */
	void abbrechenSuche() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerClient), abbrechenSuche()");
		ListIterator<?> it;
		PeerToPeerClientMitarbeiter mitarbeiter;

		it = mitarbeiterEigeneAnfragen.listIterator();
		while (it.hasNext()) {
			mitarbeiter = (PeerToPeerClientMitarbeiter) it.next();

			mitarbeiter.beenden();
		}
	}

	/**
	 * erstellt eine HTTP Nachricht und schickt sie an den Teilnehmer, auf
	 * welchem die entsprechende Datei liegt. Auch dazu wird ein
	 * Mitarbeiter-Thread gestartet und dieser in die Liste der Threads fuer
	 * eigene Anfragen eingefuegt.
	 *
	 * @param teilnehmerIp
	 *            der Teilnehmer, auf welchem die Datei liegt
	 * @param dateiName
	 *            der Name der gesuchten Datei
	 */
	void dateiVomTeilnehmerAnfordern(String teilnehmerIp, String dateiName) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerClient), dateiVomTeilnehmerAnfordern("+teilnehmerIp+","+dateiName+")");
		PeerToPeerClientMitarbeiter m;

		m = new PeerToPeerClientMitarbeiter(peerToPeerAnwendung, teilnehmerIp,
				dateiName);
		m.start();
		mitarbeiterEigeneAnfragen.add(m);
	}

	/**
	 * Initiierung und weiterleitung einer Suchanfrage (Query). Zur weiteren
	 * Verarbeitung wird ein neuer Mitarbeiter-Thread erzeugt. Der Thread wird
	 * der Liste fuer eigene Anfragen hinzugefuegt, wenn die GUID in der Liste
	 * der eigenen Anfragen der PeerToPeerAnwendung vorhanden ist. Die Anfrage
	 * wird immer an alle bekannten Teilnehmer im Peer-To-Peer-Netzwerk
	 * verschickt.
	 *
	 * @param anfragePaket
	 *            das weiterzusendende Paket
	 * @param absenderIP
	 *            die IP-Adresse des Absenders
	 * @param peerToPeerServerMitarbeiter
	 *            der PeerToPeerServermitarbeiter, an den moegliche Antworten
	 *            zurueck uebergeben werden muessen
	 */
	void sendeAnfrage(QueryPaket anfragePaket, String absenderIP) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerClient), sendeAnfrage("+anfragePaket+","+absenderIP+")");
		LinkedList<String> dieNachbarn;
		PeerToPeerClientMitarbeiter lauscher;
		String nachbar;

		dieNachbarn = peerToPeerAnwendung.holeBekanntePeerToPeerTeilnehmer();
		/* Anfrage an ALLE direkten Nachbarn */
		for (int i = 0; i < dieNachbarn.size(); i++) {
			nachbar = (String) dieNachbarn.get(i);
			if (!nachbar.equals(absenderIP)) {
				lauscher = new PeerToPeerClientMitarbeiter(
						peerToPeerAnwendung, nachbar, anfragePaket);
				if (peerToPeerAnwendung.holeEigeneAnfragen().contains(
						anfragePaket.getGuid())) {
					mitarbeiterEigeneAnfragen.add(lauscher);
				}
				else {
					mitarbeiterFremdeAnfragen.add(lauscher);
				}
				lauscher.start();
			}
		}
	}
}
