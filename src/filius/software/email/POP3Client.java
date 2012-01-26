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
package filius.software.email;

import java.util.LinkedList;
import java.util.ListIterator;

import filius.Main;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.I18n;
import filius.software.clientserver.ClientAnwendung;
import filius.software.transportschicht.TCPSocket;

public class POP3Client extends ClientAnwendung implements I18n {
	private EmailAnwendung anwendung;

	public POP3Client(EmailAnwendung anwendung) {
		super();
		Main.debug.println("INVOKED-2 ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Client), constr: POP3Client("+anwendung+")");

		this.anwendung = anwendung;
		this.setSystemSoftware(anwendung.getSystemSoftware());
	}

	/**
	 * Diese Methode startet die blockierenden Methoden zum E-Mail-Abruf in dem
	 * Thread der Anwendung. Deshalb ist der Aufruf dieser Methode <b> nicht
	 * blockierend </b>!
	 *
	 * @param pop3Server
	 * @param pop3Port
	 * @param benName
	 * @param pw
	 */
	public void emailsHolen(String pop3Server, String pop3Port, String benName,
			String pw) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Client), emailsHolen("+pop3Server+","+pop3Port+","+benName+","+pw+")");
		Object[] args;

		args = new Object[2];
		args[0] = pop3Server;
		args[1] = new Integer(pop3Port);
		ausfuehren("initialisiereSocket", args);

		args = new Object[2];
		args[0] = benName;
		args[1] = pw;
		ausfuehren("starteVerarbeitung", args);

		ausfuehren("schliesseSocket", null);
	}

	/**
	 * Hier wird ein neuer Socket erzeugt und verbunden
	 *
	 * @param zielAdresse
	 * @param port
	 */
	public void initialisiereSocket(String zielAdresse, Integer port) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Client), initialisiereSocket("+zielAdresse+","+port+")");
		try {
			socket = new TCPSocket(getSystemSoftware(), zielAdresse, port);
			socket.verbinden();
		}
		catch (Exception e) {
			e.printStackTrace(Main.debug);
			socket = null;
			anwendung.benachrichtigeBeobachter(e);
		}
	}

	/**
	 * Hier wird das Schema runtergebetet, nach dem Emails vom POP3Server
	 * abgeholt werden - benutzerauthentifizierung * benutzername * passwort -
	 * status der Mailbox - emails auflisten - email abrufen
	 *
	 * @return boolean
	 */
	public void starteVerarbeitung(String benName, String pw) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Client), starteVerarbeitung("+benName+","+pw+")");
		boolean erfolg = true;

		if (socket != null && socket.istVerbunden()) {
			try {
				erfolg = socket.empfangen().startsWith("+");
				if (erfolg) erfolg = eingabeBenutzername(benName);
				if (erfolg) erfolg = eingabePasswort(pw);
				if (erfolg) erfolg = alleEmailsAbrufen();
				sitzungBeenden();
			}
			catch (Exception e) {
				e.printStackTrace(Main.debug);
				anwendung.benachrichtigeBeobachter(e);
			}

			anwendung.benachrichtigeBeobachter();
		}
	}

	private boolean alleEmailsAbrufen() throws Exception {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Client), alleEmailsAbrufen()");
		String[] statusteile;
		int anzahlMails = 0;
		String auflistung = "";
		LinkedList<Integer> emailIDs = new LinkedList<Integer>();

		statusteile = statusAbrufen().split(" ");
		if (statusteile.length > 0) {
			anzahlMails = Integer.parseInt(statusteile[0]);
		}
		if (anzahlMails > 0) {
			// Auflistung aller Emails (Mit Nummer (Leerzeichen) Gre in Bytes)
			auflistung = emailsAuflisten();

			// nach jeder Email wird getrennt
			String[] zeilen = auflistung.split("\n");
			// es wird bei 1 angefangen um das "+OK" zu Beginn der Übertragung
			// auszusparen
			for (int i = 1; i < zeilen.length; i++) {
				String zeile = zeilen[i];
				String[] zeichen = zeile.split(" ");
				emailIDs.add(Integer.parseInt(zeichen[0])); // es wird immer der
															// Index zur Liste
															// hinzugefügt
			}
		}
		/*
		 * Aus dem String aus emailsAuflisten wird eine LinkedList der Email-IDs
		 * erzeugt. Diese wird iteriert und RETR fr jede Mail aufgerufen. Die
		 * Emails werden als String geliefert
		 */
		ListIterator iter = emailIDs.listIterator();
		while (iter.hasNext()) {
			int id = (Integer) iter.next();

			emailAbrufen(id);
			emailLoeschen(id);
		}

		return true;
	}

	/**
	 * Diese Methode ist <b>blockierend</b>.
	 *
	 */
	public void schliesseSocket() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Client), schliesseSocket()");
		if (socket != null) {
			socket.schliessen();
			socket = null;
			benachrichtigeBeobachter(messages.getString("sw_pop3client_msg1"));
		}
	}

	/**
	 * FUNKTIONIERT
	 *
	 * @param benutzername
	 * @throws Exception
	 */
	private boolean eingabeBenutzername(String benutzername) throws Exception {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Client), eingabeBenutzername("+benutzername+")");
		if (EingabenUeberpruefung.isGueltig(benutzername,
				EingabenUeberpruefung.musterMindEinZeichen)) {
			socket.senden("USER " + benutzername);
			String empfangen = socket.empfangen();

			if (empfangen.startsWith("+OK")) return true;
			else throw new Exception(empfangen);
		}
		else {
			return false;
		}
	}

	/**
	 * FUNKTIONIERT
	 *
	 * @param passwort
	 * @throws Exception
	 */
	private boolean eingabePasswort(String passwort) throws Exception {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Client), eingabePasswort("+passwort+")");
		if (EingabenUeberpruefung.isGueltig(passwort,
				EingabenUeberpruefung.musterMindEinZeichen)) {
			socket.senden("PASS " + passwort);
			String z = socket.empfangen();
			if (z.startsWith("+OK")) return true;
			else throw new Exception(z);
		}
		else {
			return false;
		}
	}

	/**
	 * FUNKTIONIERT Hiermit wird der Status der Mailbox abgefragt. Es wird der
	 * Befehl pop3-maessig (STAT) an den POP3Server gesendet.
	 *
	 * @throws Exception
	 *
	 */
	private String statusAbrufen() throws Exception {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Client), statusAbrufen()");
		String antwort, ergebnis;
		String[] temp;

		socket.senden("STAT");
		antwort = socket.empfangen();

		if (antwort.startsWith("+OK")) {
			temp = antwort.split(" ");
			ergebnis = temp[1] + " " + temp[2];
			return ergebnis;
		}
		else {
			throw new Exception(antwort);
		}
	}

	/**
	 * FUNKTIONIERT Hier wird der Befehl LIST gesendet, einmal ohne Attribut,
	 * wenn alle emails aufgelistet werden sollen, und einmal mit, wenn nur die
	 * Email mit dem Index i aufgelistet werden soll.
	 *
	 * @param i
	 * @throws Exception
	 */
	private boolean emailsAuflisten(int i) throws Exception {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Client), emailsAuflisten("+i+")");
		String ergebnis;

		socket.senden("LIST " + i);
		ergebnis = socket.empfangen();

		if (ergebnis.startsWith("+OK")) return true;
		else throw new Exception(ergebnis);
	}

	/**
	 * In dieser Methode werden alle Emails aufgelistet.
	 *
	 * @return String
	 * @throws Exception
	 */
	private String emailsAuflisten() throws Exception {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Client), emailsAuflisten()");
		String ergebnis;

		socket.senden("LIST");
		ergebnis = socket.empfangen();
		if (ergebnis.startsWith("+OK")) return ergebnis;
		else throw new Exception(ergebnis);
	}

	/**
	 * FUNKTIONIERT ruft eine email vom Server ab. Es wird die Email mit dem als
	 * Attribut uebergebenen Index abgerufen.
	 *
	 * @param i
	 * @throws Exception
	 */
	private void emailAbrufen(int i) throws Exception {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Client), emailsAbrufen("+i+")");
		int pos;
		Email neMail;

		socket.senden("RETR " + i);
		String result = socket.empfangen();

		if (result.startsWith("+OK")) {
			// hier wird das Email-Objekt dem Nachrichtenkonto hinzugefuegt

			pos = result.indexOf("\n");
			if (pos > 0) {
				neMail = new Email(result.substring(pos + 1));
				anwendung.getEmpfangeneNachrichten().add(neMail);
			}
		}
		else {
			throw new Exception(result);
		}
	}

	/**
	 * FUNKTIONIERT UEBERARBEITEN DENNOCH 2008 Hier wird der Befehl zum
	 * loeschen, d.h. eine Email als zu loeschen nach dem Ende der Sitzung
	 * markiert, gesendet.
	 *
	 * @param i
	 * @throws Exception
	 */
	private boolean emailLoeschen(int i) throws Exception {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Client), emailLoeschen("+i+")");
		String ergebnis;

		socket.senden("DELE " + i);
		ergebnis = socket.empfangen();

		if (ergebnis.startsWith("+OK")) return true;
		else throw new Exception(ergebnis);

	}

	/**
	 * UEBERARBEITEN Hiermit wird die Sitzung beendet. Das heißt auch, dass der
	 * eigene Thread, die Kommunikation beendet werden muss.
	 *
	 * @throws Exception
	 *
	 */
	private boolean sitzungBeenden() throws Exception {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Client), sitzungBeenden()");
		String ergebnis;

		socket.senden("QUIT");
		ergebnis = socket.empfangen();

		if (ergebnis.startsWith("+OK")) return true;
		else throw new Exception(ergebnis);
	}

	/**
	 * FUNKTIONIERT Diese Methode provoziert lediglich einen response vom
	 * Server...
	 *
	 */
	private String noop() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Client), noop()");
		String ergebnis = "";
		try {
			socket.senden("NOOP");
			ergebnis = socket.empfangen();
		}
		catch (Exception e) {
			e.printStackTrace(Main.debug);
			ergebnis = "-ERR NOOP failure in Email-Client";
		}
		return ergebnis;
	}
}
