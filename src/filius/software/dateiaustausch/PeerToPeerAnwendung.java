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
import java.util.StringTokenizer;

import javax.swing.tree.DefaultMutableTreeNode;

import filius.Main;
import filius.software.Anwendung;
import filius.software.system.Betriebssystem;
import filius.software.system.Datei;
import filius.software.system.Dateisystem;
import filius.software.system.InternetKnotenBetriebssystem;

/**
 * Diese Klasse verwaltet die notwendigen Daten fuer den Dateiaustausch mit
 * 'Gnutella'. Das ist ein Programm fuer den Dateiaustausch im Internet in einem
 * Peer-to-Peer-Netzwerk. Fuer die Verarbeitung von Anfragen werden ein
 * PeerToPeerClient und ein PeerToPeerServer verwendet.
 * <ul>
 * <li> Der Server verarbeitet alle eingehenden Anfragen von anderen
 * Teilnehmern. Und veranlasst gegebenenfalls die Weiterleitung von Ping-, Pong-
 * und Query-Nachrichten sowie die Verarbeitung von eingehenden
 * HTTP-GET-Anfragen. </li>
 * <li> Der Client versendet eigene und fremde Anfragen an andere
 * PeerToPeerServer und sorgt fuer die Verarbeitung eingehende Antworten auf
 * diese Anfragen. </li>
 * </ul>
 *
 */

public class PeerToPeerAnwendung extends Anwendung {

	/**
	 * Liste der Teilnehmer im Peer-to-Peer-Netzwerk, die dem Prozess bekannt
	 * sind. Die Liste enthaelt die IP-Adressen(?????)
	 */
	private LinkedList<String> bekanntePeerToPeerTeilnehmer = new LinkedList<String>();

	/** Liste eigener Anfragen, die verschickt worden sind */
	private LinkedList<Integer> eigeneAnfragen = new LinkedList<Integer>();

	/**
	 * Eine Liste der Anfragen, die von anderen Teilnehmern im
	 * Peer-to-Peer-Netzwerk empfangen wurden. wofuer???
	 */
	private LinkedList<Integer> fremdeAnfragen = new LinkedList<Integer>();

	/**
	 * Liste bereits eingegangener und weitergeleiteter Anfragen (zur
	 * Verhinderung von Schleifen)
	 */
	private LinkedList<Integer> schonmalVerschicktListe = new LinkedList<Integer>();

	/**
	 * Liste der Dateien, zu welchen eine Anfrage zum Herunterladen an einen
	 * bestimmten Teilnehmer verschickt wurde und deren Empfang erwartet wird
	 */
	private LinkedList<String> erwarteteDateien = new LinkedList<String>();

	/**
	 * Liste der im Peer-to-Peer-Netzwerk vorhandenen Dateien, die auf eine
	 * Anfrage von anderen Teilnehmern angeboten wurden
	 */
	private LinkedList<String> ergebnisse = new LinkedList<String>();

	/** Der Teil der Anwendung, der auf eingehende Anfragen wartet */
	private PeerToPeerServer peerToPeerServer;

	/** Der Teil der Anwendung, der eigene Anfragen verschickt */
	private PeerToPeerClient peerToPeerClient;

	/**
	 * Verzeichnis, in dem die Dateien gespeichert werden, die anderen angeboten
	 * werden und in das heruntergeladene Dateien gespeichert werden
	 */
	private DefaultMutableTreeNode verzeichnis;

	/** maximale Anzahl von Teilnehmern, zu denen eine Verbindung aufgebaut wird */
	private int maxTeilnehmerZahl;

	/**
	 * herkoemmlicher Konstruktor
	 */
	public PeerToPeerAnwendung() {
		super();
		Main.debug.println("INVOKED-2 ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), constr: PeerToPeerAnwendung()");

		// maximale Teilnehmerzahl mit einem Zufallswert 3, 4 oder 5 belegen
		setMaxTeilnehmerZahl(Math.round((float) Math.random() * 2) + 3);
	}

	/**
	 * Hier wird das Betriebssystem gesetzt und das Standardverzeichnis
	 * initialisiert und wenn noetig im Dateisystem erstellt
	 */
	public void setSystemSoftware(InternetKnotenBetriebssystem betriebssystem) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), setSystemSoftware("+betriebssystem+")");
		super.setSystemSoftware(betriebssystem);

		Dateisystem dateisystem = betriebssystem.getDateisystem();

		dateisystem.erstelleVerzeichnis(betriebssystem.getDateisystem()
				.getRoot(), "peer2peer");
		verzeichnis = dateisystem.verzeichnisKnoten(dateisystem.holeRootPfad()
				+ Dateisystem.FILE_SEPARATOR + "peer2peer");
	}

	/**
	 * Starten der Anwendung
	 */
	public void starten() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), starten()");
		super.starten();

		eigeneAnfragen = new LinkedList<Integer>();
		fremdeAnfragen = new LinkedList<Integer>();
		schonmalVerschicktListe = new LinkedList<Integer>();

		peerToPeerServer = new PeerToPeerServer(this);
		peerToPeerServer.setSystemSoftware(getSystemSoftware());
		peerToPeerServer.starten();
		peerToPeerClient = new PeerToPeerClient(this);
		peerToPeerClient.setSystemSoftware(getSystemSoftware());
		peerToPeerClient.starten();
	}

	/**
	 * Beenden der Anwendung
	 */
	public void beenden() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), beenden()");
		super.beenden();

		peerToPeerServer.beenden();
		peerToPeerClient.beenden();
	}

	/**
	 * Diese Methode erstellt ein Pong-Paket als Antwort auf ein Ping-Paket
	 * zurueck. Wenn auf das uebergebene Ping-Paket schon geantwortet wurde wird
	 * null zurueck gegeben.
	 *
	 * @param paket
	 *            das Ping-Paket, zu dem das zu erzeugende Pong-Paket die
	 *            Antwort darstellen soll
	 * @return
	 */
	PongPaket erstellePong(PingPaket ping) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), erstellePong("+ping+")");
		LinkedList<Datei> dateien;
		Datei aktuelle;
		PongPaket pong;
		long anzahlBytes = 0;
		Betriebssystem bs;

		if (schonmalVerschicktListe.contains(ping.getGuid())) {
			return null;
		}
		else {
			bs = (Betriebssystem) getSystemSoftware();

			dateien = bs.getDateisystem().holeDateien(verzeichnis);

			for (int i = 0; i < dateien.size(); i++) {
				aktuelle = (Datei) dateien.get(i);
				anzahlBytes = anzahlBytes + aktuelle.holeGroesse();
			}
			pong = new PongPaket(bs.holeIPAdresse(), 6346, dateien.size(),
					anzahlBytes);
			pong.setGuid(ping.getGuid());
			pong.setIpAdresse(bs.holeIPAdresse());

			return pong;
		}
	}

	/**
	 * Mit dieser Methode wird ein Ping-Paket versendet. Das kann entweder ein
	 * selbst erzeugtes oder ein weiterzuleitendes Paket sein. Die GUID wird,
	 * wenn die TTL nicht abgelaufen ist und das Paket bisher noch nicht
	 * verschickt wurde, durch den Aufruf der entsprechenden Methode des
	 * PeerToPeerClient verschickt, der dann einen Thread startet, der die
	 * Antworten darauf verarbeitet.
	 *
	 * @param ping
	 * @param absender
	 */
	void sendePing(PingPaket ping, String absender) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), sendePing("+ping+","+absender+")");
		if (ping.getTtl() > 0
				&& !schonmalVerschicktListe.contains(ping.getGuid())) {
			schonmalVerschicktListe.add(ping.getGuid());
			fremdeAnfragen.add(ping.getGuid());
			peerToPeerClient.sendePing("", ping, absender);
		}
	}

	/**
	 * wenn eine neue Anfrage-Nachricht eingetroffen ist, wird diese Operation
	 * aufgerufen. Sie verarbeitet die Anfrage, schickt sie weiter, verwirft sie
	 * oder sendet ein Antwortpaket
	 *
	 * @param pufferElement
	 *            die ampfangene Anfrage
	 */
	LinkedList<Datei> verarbeiteAnfrage(String absender, QueryPaket anfrage) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), verarbeiteAnfrage("+absender+","+anfrage+")");
		LinkedList<Datei> ergebnisListe;
		Datei ergebnis = null;
		LinkedList<Datei> dateien;
		String aktuellerName;
		String gesuchte;

		if (!schonmalVerschicktListe.contains(anfrage.getGuid())) {
			fremdeAnfragen.add(anfrage.getGuid());
			schonmalVerschicktListe.add(anfrage.getGuid());

			dateien = getSystemSoftware().getDateisystem().holeDateien(
					verzeichnis);
			ergebnisListe = new LinkedList<Datei>();
			for (int i = 0; i < dateien.size(); i++) {
				ergebnis = (Datei) dateien.get(i);

				aktuellerName = ergebnis.getName().toLowerCase();
				gesuchte = anfrage.getSuchKriterien().toLowerCase();
				if (aktuellerName.contains(gesuchte)) {
					ergebnisListe.add(ergebnis);
				}
			}

			if (anfrage.getTtl() > 0) {
				anfrage.setTtl(anfrage.getTtl() - 1);
				anfrage.setHops(anfrage.getHops() + 1);

				peerToPeerClient.sendeAnfrage(anfrage, absender);
			}
			return ergebnisListe;
		}
		else {
			return null;
		}
	}

	/**
	 * Diese Methode dient dazu, einen Rechner mit einem PeerToPeer-Netzwerk zu
	 * verbinden. Dazu wird die IP-Adresse eines bekannten Teilnehmers
	 * uebergeben. <br />
	 * Die Liste der bisher bekannten Teilnehmer wird geloescht. <br />
	 * An die IP-Adresse des bekannten Teilnehmers wird ein Ping-Paket
	 * versendet. Das Ping-Paket wird durch das Netz geflutet, jeder Servant,
	 * der weniger als die jeweilige maximale Anzahl bekannter Nachbarn hat,
	 * antwortet mit einem Pong. Der Teilnehmer fuegt die Absender der ersten
	 * ankommenden Pongs in seine Liste der Nachbarn hinzu, bis die maximale
	 * Anzahl von benachbarten Teilnehmern erreicht ist.
	 *
	 * @param teilnehmerIP
	 *            Die IP des Servants, an den das Ping-Paket zuerst geschickt
	 *            wird
	 */
	public void beitretenNetzwerk(String teilnehmerIP) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), beitretenNetzwerk("+teilnehmerIP+")");
		Betriebssystem bs;
		PingPaket pingPaket;

		bs = (Betriebssystem) getSystemSoftware();

		if (!bs.holeIPAdresse().equals(teilnehmerIP)) {
			bekanntePeerToPeerTeilnehmer.clear();
			benachrichtigeBeobachter(bekanntePeerToPeerTeilnehmer);

			pingPaket = new PingPaket();
			pingPaket.setIp(bs.holeIPAdresse());
			eigeneAnfragen.add(pingPaket.getGuid());

			peerToPeerClient.sendePing(teilnehmerIP, pingPaket, bs
					.holeIPAdresse());
		}
	}

	/**
	 * Methode zum herunterladen einer zuvor von einem anderen Teilnehmer im
	 * Peer-to-Peer-Netzwerk angebotenen Datei.
	 *
	 * @param ergebnisIndex
	 *            Der Index der angebotenen Datei in der Liste der Ergebnisse
	 *            (Attribut 'ergebnisse').
	 */
	public void herunterladenDatei(int ergebnisIndex) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), herunterladenDatei("+ergebnisIndex+")");
		StringTokenizer tempTokenizer;
		String tmpBesitzer;
		StringTokenizer tempTokenizerDatei;
		String tmpDateiname;

		tempTokenizer = new StringTokenizer(ergebnisse.get(ergebnisIndex), "/");
		tmpBesitzer = tempTokenizer.nextToken();
		tempTokenizerDatei = new StringTokenizer(tempTokenizer.nextToken(), ":");
		tmpDateiname = tempTokenizerDatei.nextToken();

		erwarteteDateien.add(tmpDateiname);
		peerToPeerClient.dateiVomTeilnehmerAnfordern(tmpBesitzer, tmpDateiname);
		benachrichtigeBeobachter();
	}

	/**
	 * Methode zum Suchen von Dateien im Peer-to-Peer-Netzwerk
	 *
	 * @param datei
	 *            der Dateiname der zu suchenden Datei
	 */
	public void sucheDatei(String datei) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), sucheDatei("+datei+")");
		Betriebssystem bs;
		QueryPaket anfragePaket;

		ergebnisse.clear();
		anfragePaket = new QueryPaket("1", datei);
		eigeneAnfragen.add(anfragePaket.getGuid());

		bs = (Betriebssystem) getSystemSoftware();
		peerToPeerClient.sendeAnfrage(anfragePaket, bs.holeIPAdresse());
	}

	/** Zum Abbruch aller Suchanfragen, die zuvor gestartet worden sind. */
	public void abbrechenSuche() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), abbrechenSuche()");
		peerToPeerClient.abbrechenSuche();
	}

	/** Methode zum Zuruecksetzen der Liste mit Suchergebnissen */
	public void loescheSuchergebnisse() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), loescheSuchergebnisse()");
		ergebnisse.clear();
	}

	/**
	 * Hilfsmethode fuer den lesenden Zugriff auf die Dateien im
	 * Peer-To-Peer-Verzeichnis des eigenen Rechners.
	 */
	Datei holeDatei(String dateiName) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), holeDatei()");
		Datei datei;

		datei = (Datei) getSystemSoftware().getDateisystem().holeDatei(
				verzeichnis, dateiName);
		return datei;
	}

	/**
	 * Hilfsmethode fuer den schreibenden Zugriff auf die Dateien im
	 * Peer-To-Peer-Verzeichnis des eigenen Rechners.
	 */
	void speicherDatei(Datei datei) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), speicherDatei("+datei+")");
		getSystemSoftware().getDateisystem().speicherDatei(verzeichnis, datei);
	}

	/**
	 * Diese Methode verarbeitet eine eingegangene Nachricht. Sie befindet sich
	 * in der PeerToPeerAnwendung (und nicht im Server oder Client), weil sowohl
	 * Client, als auch Server solche Antwortnachrichten erhalten können
	 *
	 * @param antwortPaket
	 *            das eingegangene Antwortpaket
	 */
	void verarbeiteQueryHit(QueryHitPaket antwortPaket) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), verarbeiteQueryHit("+antwortPaket+")");
		// warte ich selbst auf diese Antwort?
		if (eigeneAnfragen.contains(antwortPaket.getGuid())) {
			hinzuErgebnis(antwortPaket);
			benachrichtigeBeobachter();
		}
		// ich kenne die Anfrage, die Antwort ist nicht fuer mich
		else if (fremdeAnfragen.contains(antwortPaket.getGuid())) {
			if (antwortPaket.getTtl() > 0) {
				antwortPaket.setTtl(antwortPaket.getTtl() - 1);
				antwortPaket.setHops(antwortPaket.getHops() + 1);

				peerToPeerServer.sendePaket(antwortPaket);
			}
		}
	}

	/**
	 * Hier werden eingehende Pong-Pakete verarbeitet. Wenn es sich um die
	 * Antwort auf eine eigene Anfrage handelt, wird die Absenderadresse der
	 * Liste der bekannten Teilnehmer im Peer-To-Peer-Netzwerk hinzugefuegt.
	 * Andernfalls wird das Pong-Paket mit dekrementierter TTL und
	 * inkrementierter Hop-Zahl weitergeleitet.
	 */
	void verarbeitePong(PongPaket pongPaket) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), verarbeitePong("+pongPaket+")"
				+ "\n\tPong-Nachricht bei '"
				+ getSystemSoftware().getKnoten().getName() + "' eingetroffen: "
				+ pongPaket.toString());

		if (eigeneAnfragen.contains(pongPaket.getGuid())) {
			hinzuTeilnehmer(pongPaket.getIpAdresse());
		}
		else {
			pongPaket.setTtl(pongPaket.getTtl() - 1);
			pongPaket.setHops(pongPaket.getHops() + 1);

			peerToPeerServer.sendePaket(pongPaket);
		}
	}

	/** Zugriff auf die Liste bekannter Teilnehmer im Peer-To-Peer-Netzwerk. */
	public LinkedList<String> holeBekanntePeerToPeerTeilnehmer() {
		return bekanntePeerToPeerTeilnehmer;
	}

	/**
	 * Zugriff auf die Liste der Dateien, die auf eine Anfrage angeboten wurden.
	 *
	 * @return
	 */
	public LinkedList<String> holeErgebnisse() {
		return ergebnisse;
	}

	/**
	 * Zugriff auf das Verzeichnis im lokalen Dateisystem, in dem die Dateien
	 * gespeichert sind, die anderen Peer-To-Peer-Teilnehmern angeboten werden.
	 *
	 * @return
	 */
	public DefaultMutableTreeNode holeVerzeichnis() {
		return verzeichnis;
	}

	/**
	 * Hier wird der Liste mit Antworten auf eine Suchanfrage im
	 * Peer-To-Peer-Netzwerk ein Eintrag hinzugefuegt, wenn ein Query-Hit-Paket
	 * eingetroffen ist.
	 *
	 * @param ergebnis
	 */
	void hinzuErgebnis(QueryHitPaket ergebnis) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), hinzuErgebnis("+ergebnis+")");
		String neuesErgebnis;

		neuesErgebnis = ergebnis.getIpAdresse() + "/" + ergebnis.getErgebnis();
		if (!ergebnisse.contains(neuesErgebnis)) {
			ergebnisse.add(neuesErgebnis);
			benachrichtigeBeobachter();
		}
	}

	/**
	 * In dieser Methode wird ein neu gefundener Teilnehmer der Liste bekannter
	 * Teilnehmer des Peer-to-Peer-Netzwerks mit der IP-Adresse hinzugefuegt.
	 * Wenn die IP-Adresse in der Liste bereits vorhanden ist, oder die Anzahl
	 * der bekannten Teilnehmer schon erreicht ist, wird die neue Adresse nicht
	 * mehr hinzugefuegt.
	 *
	 * @param ipAdresse
	 */
	void hinzuTeilnehmer(String ipAdresse) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (PeerToPeerAnwendung), hinzuTeilnehmer("+ipAdresse+")");
		if (bekanntePeerToPeerTeilnehmer.contains(ipAdresse)) {
			//Main.debug.println(getClass() + "\n\tAn Rechner "
			//		+ getSystemSoftware().getKnoten().getName()
			//		+ " ist bereits der Teilnehmer " + ipAdresse
			//		+ " eingetragen");
		}
		else if (bekanntePeerToPeerTeilnehmer.size() >= maxTeilnehmerZahl) {
			//Main.debug
					//.println(getClass()
							//+ "\n\tAn Rechner "
							//+ getSystemSoftware().getKnoten().getName()
							//+ " ist bereits die maximale Anzahl bekannter Teilnehmer eingetragen."
							//+ "\n\tTeilnehmer " + ipAdresse
							//+ " wird nicht mehr eingetragen");
		}
		else {
			bekanntePeerToPeerTeilnehmer.add(ipAdresse);
			benachrichtigeBeobachter();
		}
	}

	/**
	 * Methode fuer den Zugriff auf die Liste mit GUID's zu eigenen Anfragen,
	 * die im Peer-to-Peer-Netzwerk verschickt worden sind.
	 *
	 * @return
	 */
	LinkedList<Integer> holeEigeneAnfragen() {
		return eigeneAnfragen;
	}

	/**
	 * Zugriff auf die maximale Anzahl von Teilnehmern, die der Programminstanz
	 * bekannt sind. Eine maximale Anzahl ist notwendig, weil nicht jedem
	 * Teilnehmer immer alle anderen Teilnehmer bekannt sein koennen. Die Anzahl
	 * muss variieren, weil in dem Fall, dass diese Anzahl fuer alle
	 * Programminstanzen identisch ist und die maximale Teilnehmerzahl gerade
	 * erreicht ist, ein neu beitretender Teilnehmer bei keiner Programminstanz
	 * hinzugefuegt werden kann.
	 *
	 * @return
	 */
	public int getMaxTeilnehmerZahl() {
		return maxTeilnehmerZahl;
	}

	/**
	 * Methode fuer den Zugriff auf die maximale Anzahl von Teilnehmen im
	 * Peer-to-Peer-Netzwerk, die dieser Programminstanz bekannt sind.
	 */
	public void setMaxTeilnehmerZahl(int maxTeilnehmerZahl) {
		this.maxTeilnehmerZahl = maxTeilnehmerZahl;
	}
}
