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
import filius.software.clientserver.ServerMitarbeiter;
import filius.software.transportschicht.TCPSocket;

/**
 *
 * @author Andre Asschoff
 *
 * In der POP3 Schicht empfangeDaten gehe ich direkt auf TCP, ueberwache den
 * IncomingPuffer (TCP- PufferElemente) um den von mir benaetigten String direkt
 * auszulesen und zu bearbeiten.
 */

public class POP3Mitarbeiter extends ServerMitarbeiter {
	private EmailServer emailServer;

	private String benutzername;

	private String password;

	private boolean benAuth = false;

	private boolean transactionState = false;

	private boolean authenticationState = true; // solange nicht angemeldet
												// "true"

	// das Konto, mit dem gearbeitet wird, auf das man sich angemeldet hat
	private EmailKonto aktivesKonto;

	public POP3Mitarbeiter(TCPSocket socket, POP3Server pop3Server) {
		super(pop3Server, socket);
		Main.debug.println("INVOKED-2 ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), constr: POP3Mitarbeiter("+socket+","+pop3Server+")");
		this.socket = socket;
		this.emailServer = pop3Server.holeEmailServer();

		sendeAntwort("+OK POP3 server ready");

	}

	@Override
	protected void verarbeiteNachricht(String nachricht) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), verarbeiteNachricht("+nachricht+")");
		// ist der Befehl fuer den POP3Server (USER, STAT, ...)
		String befehl = "";
		// ist das Array, das die einzelnen Elemente der Clientdaten aufnimmt
		String[] incoming = new String[3]; // 3 Elemente fuer Befehl,
											// Information und eins als Toni
											// Polster

		incoming = nachricht.split(" ");

		// das erste Element ist immer der Befehl, bspw. DELE, RETR, STAT...
		befehl = incoming[0];
		//Main.debug
				//.println("========================================================================================================");
		//Main.debug
				//.println("================================POP3Komm (run): NACHRICHT lautet: "
						//+ nachricht + " ========================");
		//Main.debug
				//.println("================================POP3Komm (run): BEFEHL lautet: "
						//+ befehl + " ==============================");
		//Main.debug
				//.println("========================================================================================================");

		// es folgt die Abfrage, welcher Befehl angekommen ist
		if (befehl.equalsIgnoreCase("USER")) {
			String antwort = "";
			if (!(isTransactionState() && isBenAuth())
					&& isAuthenticationState()) {
				antwort = user(incoming[1]);
				if (isBenAuth()) {
					benutzername = incoming[1];
				}

			} else {
				antwort = "-ERR Please enter USER";
			}
			//Main.debug.println("Antwort: "+antwort);
			sendeAntwort(antwort);
		} else if (befehl.equalsIgnoreCase("PASS")) {
			String antwort = "";
			if (!(isTransactionState()) && isBenAuth()) {
				antwort = pass(incoming[1]);
				password = incoming[1];
				aktivesKonto = emailServer.sucheKonto(benutzername, password);
				//Main.debug
						//.println("===========================================================================");
				//Main.debug
						//.println("===========================================================================");
				//Main.debug
						//.println("==========================POP3Server arbeitet nun mit dem Konto: ==========");
				//Main.debug
						//.println("============Benutzername: "
								//+ aktivesKonto.getBenutzername()
								//+ " PW: "
								//+ aktivesKonto.getPasswort()
								//+ "===============================================================");

			} else {
				antwort = "-ERR Please enter PASS";
			}

			sendeAntwort(antwort);
		} else if (befehl.equalsIgnoreCase("STAT")) {
			String antwort = stat(aktivesKonto);

			sendeAntwort(antwort);
		} else if (befehl.equalsIgnoreCase("LIST")) {
			if (incoming.length < 1) {
				// listet die Email mit der nr == attribut auf
				String antwort = list(Integer.parseInt(incoming[1]),
						aktivesKonto);

				sendeAntwort(antwort);
			} else {
				// Listet alle Emails auf
				String antwort = list(aktivesKonto);

				sendeAntwort(antwort);
			}
		} else if (befehl.equalsIgnoreCase("RETR")) {
			// holt die Email mit der nr == attribut vom Server
			String antwort = retr(Integer.parseInt(incoming[1]), aktivesKonto);

			sendeAntwort(antwort);
		} else if (befehl.equalsIgnoreCase("DELE")) {
			// loescht Emails vom Server. Die Email wird als geloescht markiert,
			// bleibt aber ungeloescht, bis zum naechsten update, oder zum
			// konformen
			// Ende einer Sitzung. Die zu loeschende Email wird durch das
			// attibut ref.
			String antwort = dele(Integer.parseInt(incoming[1]), aktivesKonto);

			sendeAntwort(antwort);
		} else if (befehl.equalsIgnoreCase("RSET")) {
			// setzt alle DELE-Befehle zurueck
			String antwort = rset(aktivesKonto);

			sendeAntwort(antwort);
		} else if (befehl.equalsIgnoreCase("QUIT")) {
			// beendet die Sitzung
			String antwort = quit(aktivesKonto);
			try {
				sendeAntwort(antwort);
				schliesseSocket();
			}
			// hier kann ich nicht die Verbindungsexception werfen, das gibt
			// Fehler...
			catch (Exception e) {
				schliesseSocket();
			}
		} else if (befehl.equalsIgnoreCase("NOOP")) {
			// provoziert lediglich eine Antwort vom Server
			String antwort = noop();
			sendeAntwort(antwort);
		}
	}

	public void schliesseSocket() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), schliesseSocket()");
		if (socket != null) {
			socket.schliessen();
			socket = null;
			beenden();
			// benachrichtigeBeobachter("Verbindung getrennt");
		}
	}

	/**
	 * Hier wird der Nutzername ueberprueft. Dazu wird die Methode sucheBenutzer
	 * aufgerufen, die ueberprueft, ob es einen solchen angemeldeten Benutzer
	 * gibt. Wenn das zutrifft, wird der boolean setBenutzernameIstTrue auf
	 * (true) gesetzt und dem Client das mitgeteilt.
	 *
	 * @param benutzername
	 * @param verbindungsId
	 */
	public String user(String benutzername) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), user("+benutzername+")");
		String ergebnis = "";

		if (sucheBenutzer(benutzername)) {
				setBenAuth(true);
				ergebnis = "+OK enter password";
			} else {
				ergebnis = "-ERR user or password wrong";
			}

		return ergebnis;
	}

	/**
	 * Es wird ueberprueft, ob das Passwort richtig ist, fuer den zuvor
	 * uebertragenen Benutzernamen.
	 *
	 * @param passwort
	 * @param verbindungsId
	 */
	public String pass(String passwort) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), pass("+passwort+")");
		String ergebnis = "";
		try {
			if (pruefePasswort(passwort)) {
				if (isBenAuth()) {
					setTransactionState(true);
					ergebnis = "+OK Mailbox locked and ready";
				}
			} else {
				ergebnis = "-ERR user or password wrong";
			}
		} catch (Exception e) {
			e.printStackTrace(Main.debug);
			ergebnis = "-ERR user or password wrong";
		}
		return ergebnis;
	}

	/**
	 * Diese Methode liefert den Status der Mailbox. Es wird zunaechst die Fktn.
	 * des EmailServers aufgerufen, die die Anzahl der Emails im Postfach
	 * zaehlt. Diese Ergebnisse werden in einem int- Array aufgefangen. Groesse
	 * und Anzahl werden dann ausgelesen und in 2 int-Werten gespeichert. Der
	 * datenstring enthaelt die auszugebende Message. Anschlieï¿½end wird der
	 * Status durch die Fktn. emailServer.sendeAntwort an den Client
	 * uebertragen.
	 *
	 * @param verbindungsId
	 */
	public String stat(EmailKonto uebergebenesAktivesKonto) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), stat("+uebergebenesAktivesKonto+")");
		//Main.debug
				//.println("===========================================STAT - BenName: "
						//+ uebergebenesAktivesKonto.getBenutzername()
						//+ " Passwd: " + uebergebenesAktivesKonto.getPasswort());
		String daten = "";
		if (isTransactionState()) {
			try {
				int[] a = anzahlEmailsImPostfach(uebergebenesAktivesKonto);

				daten = "+OK " + a[1] + " " + a[0];
			} catch (Exception e) {
				daten = "-ERR please try again";
			}
		}
		return daten;
	}

	/**
	 * Hier werden alle auf dem Server gespeicherten Emails aufgelistet.
	 * Zunaechst wird die Methode stat auf- gerufen, die den Status ausgibt, und
	 * anschliessend werden aus dem String-Array die daten fuer alle emails
	 * (indexnr und groesse in bytes) ausgelesen. Die werden dann in einer
	 * while-Schleife solange immer jede Email einzeln ausgegeben, bis alle
	 * daten uebertragen wurden.
	 *
	 * @param verbindungsId
	 */
	public String list(EmailKonto uebergebenesAktivesKonto) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), list("+uebergebenesAktivesKonto+")");
		String ergebnis = "";
		if (isTransactionState()) {
			try {
				// zunaechst wird der Status aufgefuehrt, dann folgt das
				// eigentliche listing... Da ist auch das +OK bei
				ergebnis = stat(aktivesKonto) + "\n";

				String a = emailsAuflisten(uebergebenesAktivesKonto);

				String[] ergServer = a.split(" ");

				int i = 0;
				while (i < ergServer.length) {
					ergebnis = ergebnis + ergServer[i] + " " + ergServer[i + 1]
							+ "\n";
					i = i + 2;
				}
			} catch (Exception e) {
				e.printStackTrace(Main.debug);
				ergebnis = "-ERR no such message";
			}
		}
		return ergebnis;
	}

	/**
	 * FUNKTIONIERT Diese Methode verfaehrt ebenso wie die "list", jedoch werden
	 * hier nur einmal Daten uebertragen, und nicht in einer for-Schleife, und
	 * zwar fuer die email mit dem index i im String der zurueckgegebenen
	 * Emails. Dafuer wird zunaechst die Methode emailsAuflisten des
	 * EmailServers aufgerufen, um alle Emails in einen langen String zu
	 * speichern. Dann wird die Email mit dem index "int i" mit ihrer groesse an
	 * den Client gesendet.
	 *
	 * @param i
	 * @param verbindungsId
	 */
	public String list(int i, EmailKonto uebergebenesAktivesKonto) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), list("+i+","+uebergebenesAktivesKonto+")");
		String ergebnis = "";

		if (isTransactionState()) {
			try {
				String a = emailsAuflisten(uebergebenesAktivesKonto);

				String[] ergServer = a.split(" ");

				int j = 0;
				while (j < ergServer.length) {
					// wenn der Index der Email (j) mit der gewuenschten
					// Abfragenummer (i) uebereinstimmt
					if (j == i) {
						ergebnis = "+OK " + ergServer[i] + " "
								+ ergServer[i + 1];
					}
					i = i + 2;
				}
			} catch (Exception e) {
				e.printStackTrace(Main.debug);
				ergebnis = "-ERR no such message";
			}
		}
		return ergebnis;
	}

	/**
	 * FUNKTIONIERT Diese Methode dient zum abrufen der Emails vom Server,
	 * hiermit werden die Emails an den Client geschickt. Wir bekommen immer nur
	 * genau eine email vom Server, eben jene mit Index i. Dieser Email
	 * Attribute werden einzelnen Strings zugeordnet. Dieser wird zu einem
	 * einzig langen String verarbeitet, der sinn- voll getrennt wird. Dieser
	 * lange String wird dann an den Client gesendet, und zuvor die Statusaus-
	 * gabe. Klappt das nicht, wird ein Fehler geworfen. Es wird auf jeden Fall
	 * noch abgefragt, ob die Email bereits als zu loeschen markiert wurde, wenn
	 * ja, dann darf sie nicht mehr abgesendet werden.
	 *
	 * @param i
	 * @param verbindungsId
	 */
	public String retr(int i, EmailKonto uebergebenesAktivesKonto) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), retr("+i+","+uebergebenesAktivesKonto+")");
		Email abgerufeneEmail = emailsAbrufen(i, uebergebenesAktivesKonto);
		String ergebnis = "";

		if (isTransactionState()) {
			try {
				if (abgerufeneEmail.getDelete() == true
						|| abgerufeneEmail.getNeu() == false) {
					ergebnis = "-ERR no such message";
				} else {
					ergebnis = "+OK message follows " + "\n"
							+ abgerufeneEmail.toString();
					abgerufeneEmail.setNeu(false);
				}
			} catch (Exception e) {
				e.printStackTrace(Main.debug);
				ergebnis = "-ERR no such message";
			}
		}
		return ergebnis;
	}

	/**
	 * FUNKTIONIERT Hiermit wird die Methode des EmailServers aufgerufen, dass
	 * deine Email, die der Client wuenscht, geloescht wird. Wenn diese Methode
	 * erfolgreich ausgefuehrt werden konnte, dann wird die Antwort an den
	 * Client uebermittelt. Sonst erfolgt eine Exception, mit der ent-
	 * sprechenden Antwort fuer den Client.
	 *
	 * @param i
	 * @param verbindungsId
	 */
	public String dele(int i, EmailKonto uebergebenesAktivesKonto) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), dele("+i+","+uebergebenesAktivesKonto+")");
		String ergebnis = "";
		if (isTransactionState()) {
			try {
				if (emailsAlsGeloeschtMarkieren(i, uebergebenesAktivesKonto) == true) {
					ergebnis = "+OK message marked for delete";
				}
			} catch (Exception e) {
				e.printStackTrace(Main.debug);
				ergebnis = "-ERR no such message";
			}
		}
		return ergebnis;
	}

	/**
	 * FUNKTIONIERT In dieser Methode wird der Status der Emails, ob diese nach
	 * dem Abmelden geloescht werden sollen oder nicht korrigiert. Hier wird das
	 * Nachrichtenkonto des angemeldeten Users ueber- prueft, ob sich darin
	 * irgendwelche Mails befinden, die geloescht werden sollen (getDelete =
	 * true). Dieser Wert aller Emails in diesem Postfach wird dann auf false
	 * gesetzt, sodass nach dem Abmelden keine Email geloescht wird. Dann wird
	 * dem Client eine Nachricht ueber den erfolgreichen Vorgang uebermittelt.
	 *
	 * @param verbindungsId
	 */
	public String rset(EmailKonto uebergebenesAktivesKonto) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), rset("+uebergebenesAktivesKonto+")");
		String ergebnis = "";
		if (isTransactionState()) {
			try {
				for (ListIterator iter = uebergebenesAktivesKonto
						.getNachrichten().listIterator(); iter.hasNext();) {
					Email email = (Email) iter.next();
					email.setDelete(false);
				}
				ergebnis = "+OK";

			} catch (Exception e) {
				e.printStackTrace(Main.debug);
				// Der Server gibt dem Client, auch wenn nichts geaendert werden
				// konnte, keine negative
				// Antwort mit -ERR.
			}
		}
		return ergebnis;
	}

	/**
	 * NACHTRAG 2008: Nicht wird die Verbindung zwischen Client und Server (das
	 * ja vom POP3Server uebernommen wird) getrennt. Nur die Emails werden
	 * geloescht. In dieser Methode wird die Sitzung zwischen POP3Server und
	 * Client geschlossen. Die als zu loeschen markierten Emails werden
	 * endgueltig beseitigt, und die Verbindung aus der Liste des EmailServers
	 * entfernt. Anschliessend erfolgt eine Ausgabe des Servers und eine
	 * Anweisung an den Client, selbst die Verbindung auch zu loeschen, wird
	 * uebermittelt.
	 *
	 * @param verbindungsId
	 */
	public String quit(EmailKonto uebergebenesAktivesKonto) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), quit("+uebergebenesAktivesKonto+")");
		String ergebnis = "";
		int i;
		if (isTransactionState()) {
			try {
				for(int idx=uebergebenesAktivesKonto.getNachrichten().size()-1; idx>=0; idx--) {
					if(((Email) uebergebenesAktivesKonto.getNachrichten().get(idx)).getDelete()) {
						uebergebenesAktivesKonto.getNachrichten().remove(idx);
					}
				}
				ergebnis = "+OK";
			} catch (Exception e) {
				Main.debug.println("EXCEPTION ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), quit: ");
				e.printStackTrace(Main.debug);
			}
		}

		benutzername = "";
		password = "";

		emailServer.benachrichtigeBeobachter();
		return ergebnis;
	}

	/**
	 * 2008 immernoch? FUNKTIONIERT... aber VERBINDUNGS-TIMEOUT noop soll
	 * lediglich, vom Client aus gesendet, beim EmailServer ein +OK provozieren,
	 * um zu sehen, ob man noch mit dem Server verbunden ist, und damit im Falle
	 * evtl. Inaktivität kein autologoff gestartet wird. Beim Anstossen dieser
	 * Methode wird einfach versucht ein System.out. zurueckzugeben, wenn wir
	 * noch angemeldet sind, ansonsten wirft er einen Fehler.
	 *
	 * @return boolean
	 */
	public String noop() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), noop()");
		String ergebnis = "";
		try {
			if (isTransactionState() == true) {
				// es muss das Verbindungstimeout resettet werden!!!Macht das
				// Carsten in den Sockets?
				ergebnis = "+OK";
			} else {
				ergebnis = "-ERR unknown command";
			}

		} catch (Exception e) {
			ergebnis = "-ERR unknown command";
			e.printStackTrace(Main.debug);
		}
		return ergebnis;
	}

	/**
	 * FUNKTIONIERT In dieser Methode wird ueberprueft, ob in der LinkedList aller
	 * registrierten Nutzer dieses EmailServers ein Konto existiert, das auf
	 * diesen Benutzernamen passt, also ob es ein Konto gibt, das diesen Namen
	 * hat.
	 *
	 * @param benutzernamen
	 * @return boolean
	 */
	public boolean sucheBenutzer(String benutzernamen) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), sucheBenutzer("+benutzernamen+")");
			for (ListIterator iter = emailServer.getListeBenutzerkonten()
					.listIterator(); iter.hasNext();) {
				EmailKonto konto = (EmailKonto) iter.next();
				if ((konto.getBenutzername().equalsIgnoreCase(benutzernamen))) {
					return true;
				}
			}
		return false;
	}

	/**
	 * FUNKTIONIERT diese Methode prueft das Passwort, dass von der TCP Schicht
	 * uebermittelt, vom Lauscher Thread aufgefangen und uebergeben, vom Client
	 * gesendet wurde, und schaut, ob es in der vom EmailServer verwalteten
	 * LinkedList aller auf dem Server registierten Benutzerkonten in genau dem
	 * Konto vorhanden ist, ueber dessen Benutzernamen sich der Client auch ange-
	 * meldet hat oder will.
	 *
	 * @param passwort
	 * @return boolean
	 */
	public boolean pruefePasswort(String passwort) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), pruefePasswort("+passwort+")");
		boolean erfolg = false;

		EmailKonto nowKonto = emailServer.sucheKonto(benutzername);
		if (nowKonto.getPasswort().equals(passwort)) {
			erfolg = true;
		}

		return erfolg;
	}

	/**
	 * FUNKTIONIERT In dieser Methode wird versucht, eine Email mit dem Index,
	 * den der USER angibt, als zu loeschen markieren. Diese soll dann nicht
	 * mehr aufgefuehrt werden, und wird zum Schluss der Sitzung wirklich
	 * geloescht.
	 *
	 * @param i
	 * @return boolean
	 */
	public boolean emailsAlsGeloeschtMarkieren(int i,
			EmailKonto uebergebenesAktivesKonto) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), emailsAlsGeloeschtMarkieren("+i+","+uebergebenesAktivesKonto+")");
		Email email;
		LinkedList emails = uebergebenesAktivesKonto.getNachrichten();
		try {
			if (i >= emails.size()) {
				return false;
			}
			((Email) emails.get(i)).setDelete(true);
			emailServer.kontenSpeichern();
			return true;
		} catch (Exception e) {
			e.printStackTrace(Main.debug);
			emailServer.kontenSpeichern();
		}
		return false;
	}

	/**
	 * FUNKTIONIERT Hier wird zunaechst eine LL erstellt, die die gewuenschten
	 * Emails enthaelt und spaeter zurueck gegeben wird. Anschliessend wird das
	 * gesuchte Konto mit Hilfe der Methode sucheKontoZuVerb er- mittelt.
	 * Anschlieï¿½end wird die LL mit der Liste aller Nachrichten dieses Kontos
	 * gefuellt. Sollen alle Emails abgerufen werden (i == 0), so werden
	 * lediglich der Wert, ob diese Emails schon einmal abgerufen wurden (der in
	 * jeder Email gespeichert ist (boolean isNeu)), auf false gesetzt und die
	 * LL emails kann einfach zurueckgegeben werden. Wird aber eine bestimmte
	 * Email verlangt (i != 0), so wird die Email mit dem Index i, der beim
	 * Speichern in die LL ja immer noch derselbe ist, wie der, als der Status
	 * oder aufgelistet wurde, zurueckgegeben. Dazu wird der Wert isNeu auf
	 * false gesetzt, die LL emails geleert (.clear()) und dann die gewuenschte
	 * Email in der LL gespeichert als einzigstes Element.
	 *
	 * @param verbindungsId
	 * @return LinkedList
	 */
	public Email emailsAbrufen(int i, EmailKonto uebergebenesAktivesKonto) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), emailsAbrufen("+i+","+uebergebenesAktivesKonto+")");
		LinkedList gespeicherteEmails = new LinkedList();
		Email abgerufeneEmail = new Email();

		try {
			//Main.debug
					//.println("===============================================================================");
			//Main.debug.println("==============Konto: "
					//+ uebergebenesAktivesKonto.getBenutzername()
					//+ " ==============================");
			//Main.debug
					//.println("====================in emails Abrufen==========================================");
			gespeicherteEmails = uebergebenesAktivesKonto.getNachrichten();

			// nur die Email mit dem Index i abrufen
			abgerufeneEmail = (Email) gespeicherteEmails.get(i);
			//Main.debug
					//.println("===================abgerufeneEmail: ===========================================");
			//Main.debug.println("================Absender: "
					//+ abgerufeneEmail.getAbsender() + " ====================");
		} catch (Exception e) {
			e.printStackTrace(Main.debug);
			emailServer.kontenSpeichern();
		}

		return abgerufeneEmail;
	}

	/**
	 * FUNKTIONIERT BESCHREIBUNG UEBERARBEITEN Hier werden die Emails der Reihe
	 * nach aufgelistet und wï¿½hrenddessen indiziert. Zunaechst wird ein Konto zu
	 * der (bestehenden?) Verbindung gesucht, und dann abgefragt, ob diese Email
	 * auch nicht geloescht werden soll. Wenn nein, wird anschliessend die
	 * Groesse jeder Email einzeln ermittelt. Der Index der Email im Konto
	 * (fortlaufend nummeriert ohne besonderes) und die Lï¿½nge der Email in bytes
	 * wird dann in einem 2-dimensionalen Array gesichert. Hier liegt auch die
	 * Groesse des Postfaches. Das Array kann max. 1000 verschiedene Emails
	 * erfassen. Das Array ist lediglich 2 Felder breit, fuer Index und
	 * Email-Groesse(bytes).
	 *
	 * @param verbindungsId
	 * @return String
	 */
	public String emailsAuflisten(EmailKonto uebergebenesAktivesKonto) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), emailsAuflisten("+uebergebenesAktivesKonto+")");
		String str = "";

		try {
			for (ListIterator iter = uebergebenesAktivesKonto.getNachrichten()
					.listIterator(); iter.hasNext();) {
				int index = iter.nextIndex();
				Email email = (Email) iter.next();

				if (email.getDelete() == false) {
					// Postfachgroesse in Oktetten, hier in bytes
					String emailgroesse = email.getText() + email.getAbsender()
							+ email.getBetreff() + email.getEmpfaenger()
							+ email.getDateReceived();

					// Ich mache hier einen langen String, der durch
					// Sonderzeichen geteilt wird, um ihn spaeter wieder
					// aufsplitten zu
					// koennen. Es wird nach einem zusammengehoerigen Komplex,
					// also nach INDEX und EMAILGROESSE, mit "$$" getrennt,
					// zwischen den beiden mit "%%"
					str = str + index + " " + emailgroesse.length() + " ";
				}
			}
		} catch (Exception e) {
			e.printStackTrace(Main.debug);
			emailServer.kontenSpeichern();
		}
		return str;
	}

	/**
	 * FUNKTIONIERT Es wird die Anzahl aller Emails, die in einem EmailKonto
	 * gespeichert sind ausgegeben. Dazu wird zunaechst geschaut, ob der User,
	 * der anfragt, mit dieser VerbindungsId ein EmailKonto hat. Dafuer wird in
	 * der (temp.) Verbindung nach dem Username und Passwort gefragt, mit dem er
	 * sich angemeldet hat. Gibt es ein solches Konto, das mit der Methode
	 * "sucheKontoZuVerbindung" gesucht wird, wird fuer jede Email in dem
	 * Postfach die Lï¿½nge ihrer Bytes gemessen und aufaddiert zu einem langen
	 * String. Hat man dann die gesamte Lï¿½nge aller Strings im Postfach, wird
	 * diese, und die gesamtanzahl aller Emails im Postfach in einem
	 * 2-dimensionalen int- Array der Grï¿½ï¿½e 1 zurueckgegeben. Vor der Erzeugung
	 * des Strings wird natuerlich noch abgefragt, ob die Email, die man gerade
	 * betrachtet, nicht zum loeschen ist (getDelete). Wenn sie nicht geloescht
	 * werden soll, dann wird sie dem String hinzugefuegt.
	 *
	 * @return int [][]
	 */
	public int[] anzahlEmailsImPostfach(EmailKonto uebergebenesAktivesKonto) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), anzahlEmailsImPostfach("+uebergebenesAktivesKonto+")");
		String str = "";
		int[] a = new int[2];
		int i = 0;
		a[0] = 0;
		a[1] = 0;
		// EmailKonto konto;
		Email email;
		try {
			for (ListIterator iter = uebergebenesAktivesKonto.getNachrichten()
					.listIterator(); iter.hasNext();) {
				email = (Email) iter.next();
				//Main.debug.println("==============EMAILS im Postfach von "
						//+ uebergebenesAktivesKonto.getBenutzername()
						//+ "===============");
				//Main.debug.println("==============temp Email von: "
						//+ email.getAbsender() + " =========================");
				//Main.debug.println("==============temp Email isToDelete: "
						//+ email.getDelete() + " ====================");
				//Main.debug.println("==============temp Email isNeu: "
						//+ email.getNeu() + " ============================");
				//Main.debug
						//.println("===============================================================================");
				if (email.getDelete() != true) {
					str = str + email.getText() + email.getAbsender()
							+ email.getBetreff() + email.getEmpfaenger()
							+ email.getDateReceived();

					i++;
				}
			}
			a[0] = str.length();
			a[1] = i;
		} catch (Exception e) {
			e.printStackTrace(Main.debug);
			emailServer.kontenSpeichern();
		}
		return a;
	}

	/**
	 * FUNKTIONIERT Eine Methode zum Senden von Antworten an den Client, damit
	 * dieser weiß, ueber welche Verbindung die Kommunikation jetzt laeuft. Dazu
	 * wird die Id der Verbindung, die in der Liste aller Verbindungen beim
	 * EmailServer gespeichert sind, uebergeben, und die Daten, die dem Client
	 * uebermittelt werden sollen. Dafuer wird in der Liste aller Verbindungen
	 * beim EmailServer zunaechst gesucht, ob die geforderte Ver- bindung mit
	 * dieser (eindeutigen) Id existiert. Wenn ja uebernimmt man von ihr die
	 * benoetigten Daten, um die nachrichten an den Client zu senden. Die daten
	 * sind einem konformen Schema entnommen. So werden z.B. fuer die
	 * Benachrichtigung eines Clients, dass eine neue Verbindung aufgebaut
	 * wurde, der String wie folgt uebergeben: IPClient +"$"+ IPServer +"$"+
	 * Port +"$"+ IDVerbindung.
	 */
	public void sendeAntwort(String daten) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), sendeAntwort("+daten+")");
		try {
			if (isTransactionState() || isBenAuth()) {
				emailServer.kontenSpeichern();
			}
		} catch (Exception e) {
		}
		sendeNachricht(daten);
	}

	/**
	 * Diese Mehtode wandelt eine LL in einen String, die einzelnen
	 * Listenelemente durch Kommata getrennt.
	 *
	 * @param args
	 * @return
	 */
	private String llzuStr(LinkedList args) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (POP3Mitarbeiter), llzuStr("+args+")");
		String str = "";
		for (ListIterator iter = args.listIterator(); iter.hasNext();) {
			str = str + ((String) iter.next()) + ",";
		}

		return str;
	}

	// GET & SET-METHODEN

	public String holeBenutzername() {
		return benutzername;
	}

	public void setzeBenutzername(String benutzername) {
		this.benutzername = benutzername;
	}

	public String holePassword() {
		return password;
	}

	public void setzePassword(String password) {
		this.password = password;
	}

	public boolean isTransactionState() {
		return transactionState;
	}

	public void setTransactionState(boolean transactionState) {
		this.transactionState = transactionState;
	}

	public boolean isBenAuth() {
		return benAuth;
	}

	public void setBenAuth(boolean benAuth) {
		this.benAuth = benAuth;
	}

	public boolean isAuthenticationState() {
		return authenticationState;
	}

	public void setAuthenticationState(boolean authenticationState) {
		this.authenticationState = authenticationState;
	}

	public EmailServer getEmailServer() {
		return emailServer;
	}

	public void setEmailServer(EmailServer emailServer) {
		this.emailServer = emailServer;
	}
}
