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

import filius.Main;
import filius.exception.TimeOutException;
import filius.exception.VerbindungsException;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.I18n;
import filius.software.Anwendung;
import filius.software.clientserver.ClientAnwendung;
import filius.software.transportschicht.TCPSocket;

public class SMTPClient extends ClientAnwendung implements I18n {
	/** Die Anwendung, die diesen SMTP-Client verwendet
	 * (das kann eine EmailAnwendung zum Versand einer erstellten
	 * Nachricht oder ein EMailServer zur Weiterleitung einer
	 * empfangenen Nachricht sein) */
	private Anwendung anwendung = null;

	// Konstruktoren

	public SMTPClient(Anwendung anwendung) {
		super();
		Main.debug.println("INVOKED-2 ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPClient), constr: SMTPClient("+anwendung+")");

		this.anwendung = anwendung;
		this.setSystemSoftware(anwendung.getSystemSoftware());
	}

	// Methoden

	/**
	 * Versenden einer Email Hier wird eine defaultServerIP übergeben, die
	 * anzeigt, diese Methode als Teil einer Weiterleitung aufgerufen wird. Wenn
	 * hier NULL übergeben wird, dann wird diese Client-Mehtode vom
	 * SMTP-Mitarbeiter als Teil der Weiterleitung aufgerufen. Ist eine
	 * DefaultServerIP mit über- geben, dann wird sie von der Email Anwendung
	 * gesendet, und die hat ihren Standartmailserver in den Einstellungen, der
	 * somit dann übergeben werden kann.
	 *
	 * Neben der Email, die eindeutig ist, werden dann noch alle Empfaenger
	 * übereben, die rcpts, die aus einem langen String von Empfaenger, Cc, und
	 * Bcc bestehen, wenn sie von der Email- Anwendung aufgerufen werden, und
	 * aus einem langen String von rcpts (NUR-NOCH-EMPFAENGER, die ausgesondert
	 * wurden, je nach dem, ob sie im Teil der Weiterleitung des
	 * SMTP-Mitarbeiters schon abgespeichert wurden, oder eben nicht und
	 * deswegen weiter versendet werden.)
	 *
	 * <ul>
	 * <li> evtl. Aufloesen der ServerIP-Adresse (bei weiterleiten, sonst nicht)
	 * </li>
	 * <li> initialisieren des Sockets (Verbindungsaufbau) </li>
	 * <li> Versand der Email </li>
	 * <li> Verbindungsabbau </li>
	 * </ul>
	 */
	public void versendeEmail(String defaultServerIP, Email email, String absender, String rcpts) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPClient), versendeEmail("+defaultServerIP+","+email+","+absender+","+rcpts+")");
		String serverAdresse;
		String empfaengerAdresse;
		String[] zuSendenAn = rcpts.split(",");

		for (int i = 0; i < zuSendenAn.length; i++) {
			empfaengerAdresse = (String) zuSendenAn[i];

			if (defaultServerIP == null) {
				serverAdresse = loeseURLauf(empfaengerAdresse);
				Main.debug
						.println(getClass()
								+ "\n\tServer Adresse bei Weiterleitung fuer Maildomain "
								+ empfaengerAdresse + " ist: " + serverAdresse);
			}
			else {
				serverAdresse = defaultServerIP;
			}

			Object[] args;
			args = new Object[2];
			args[0] = serverAdresse;
			args[1] = new Integer(25);
			ausfuehren("initialisiereSocket", args);

			args = new Object[3];
			args[0] = email;
			args[1] = absender;
			args[2] = empfaengerAdresse;
			ausfuehren("versenden", args);

			ausfuehren("schliesseSocket", null);
		}

	}

	/** Diese Methode ist blockierend */
	public void initialisiereSocket(String zielAdresse, Integer port) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPClient), initialisiereSocket("+zielAdresse+","+port+")");
		try {
			socket = new TCPSocket(getSystemSoftware(), zielAdresse, port);
			socket.verbinden();

			if (socket.istVerbunden())
				anwendung.benachrichtigeBeobachter(messages.getString("sw_smtpclient_msg1")
						+" " + socket.holeZielIPAdresse() + ":"
						+ socket.holeZielPort() + " " + messages.getString("sw_smtpclient_msg2"));
		}
		catch (Exception e) {
			e.printStackTrace(Main.debug);
			socket = null;
			anwendung.benachrichtigeBeobachter(e);
		}
	}

	/**
	 * Diese Methode ist <b>blockierend</b>.
	 */
	public void schliesseSocket() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPClient), schliesseSocket()");
		if (socket != null) {
			socket.schliessen();
			anwendung.benachrichtigeBeobachter(messages.getString("sw_smtpclient_msg1")
					+ socket.holeZielIPAdresse() + ":" + socket.holeZielPort()
					+ messages.getString("sw_smtpclient_msg3"));
			socket = null;
		}
	}

	private boolean schickeHelo() throws Exception {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPClient), schickeHelo()");
		String empfang;

		empfang = socket.empfangen();
		if (empfang.startsWith("220")) {
			socket.senden("HELO " + this.getSystemSoftware().holeIPAdresse());
			empfang = socket.empfangen();
			if (empfang.startsWith("250")) {
				return true;
			}
			else {
				throw new Exception (empfang);
			}
		}
		else {
			return false;
		}
	}

	/**
	 * Mit dieser Methode wird eine Email versendet. Es wird eine Email
	 * entgegengenommen, und aus ihr Attribute entnommen, die dann einzeln
	 * versendet werden nach SMTP-manier. Dazu werden verschiedene Methoden der
	 * Reihe nach aufgerufen, die in vordefinierter Abfolge die einzelnen
	 * Elemente einer Email versenden und auf ein Response des Servers warten.
	 * Nur bei erfolgreichem Response geht es weiter.
	 *
	 * @param email
	 */
	public boolean versenden(Email email, String absender, String rcpts) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPClient), versenden("+email+","+absender+","+rcpts+")");
		boolean erfolg = true;

		if (socket == null) return false;
		else {
			try {
				erfolg = schickeHelo();
				if (erfolg) erfolg = schickeMailFrom(absender);
				if (erfolg) erfolg = schickeRcptTo(rcpts);
				if (erfolg) erfolg = schickeDataBeginn();
				if (erfolg) erfolg = schickeData(email);
				if (erfolg) schickeQuit();
				if (erfolg) {
					if (anwendung instanceof EmailAnwendung) {
						((EmailAnwendung)anwendung).getGesendeteNachrichten().add(email);
					}
				}

				anwendung.benachrichtigeBeobachter();
			}
			catch (Exception e) {
				e.printStackTrace(Main.debug);
				anwendung.benachrichtigeBeobachter(e);

				erfolg = false;
			}

			//Main.debug.println("SMTPClient: versenden() beendet");
			return erfolg;
		}
	}

	/**
	 * Dient der Aufloesung der EmailAdressen-Domains auf den Empfaengerangaben
	 * zu einer Ip-Adresse, damit zum dortigen MTA eine Verbindung aufgebaut
	 * werden kann. Es muss nur der Domainnamen eingegeben werden, der Socket
	 * loest sofort auf die IP auf...
	 *
	 * @param url
	 * @return
	 */
	public String loeseURLauf(String url) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPClient), loeseURLauf("+url+")");
		String ipAdresse = "";
		String[] teileDerEmail = url.split("@");

		try {
			ipAdresse = getSystemSoftware().holeDNSClient().holeIPAdresseMailServer(teileDerEmail[1]);
		}
		catch (java.util.concurrent.TimeoutException e) {
			e.printStackTrace(Main.debug);
		}

		return ipAdresse;
	}

	/**
	 * Hier wird der MailFrom an den Empfaenger versendet bsp fuer MailFrom:
	 * "andre.asschoff@web.de", "carsten@gmx.de" Dazu wird aus dem Absender der
	 * Email der getMailFrom() gebastelt
	 *
	 * @param email
	 * @return
	 * @throws Exception
	 */
	private boolean schickeMailFrom(String absender)
			throws Exception {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPClient), schickeMailFrom("+absender+")");
		if (EingabenUeberpruefung.isGueltig(absender,
				EingabenUeberpruefung.musterEmailAdresse)) {

			socket.senden("MAIL FROM: <" + absender + ">");
			String empfangen = socket.empfangen();

			if (empfangen.substring(0, 3).equals("250")) {
				return true;
			}
			else {
				throw new Exception(empfangen);
			}
		}
		else {
			return false;
		}
	}

	/**
	 * Hier wird RcptTo und RcptToDomain versendet Es können auch mehrere
	 * Empfaenger angegeben werden, die durch ein Komma voneinander getrennt
	 * sind.
	 *
	 * @param email
	 * @return
	 * @throws Exception
	 */
	private boolean schickeRcptTo(String rcpts) throws Exception {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPClient), schickeRcptTo("+rcpts+")");
		String[] empfaenger = rcpts.split(",");

		for (int i = 0; i < empfaenger.length; i++) {
			if (EingabenUeberpruefung.isGueltig(empfaenger[i],
					EingabenUeberpruefung.musterEmailAdresse)) {
				socket.senden("RCPT TO:<" + empfaenger[i] + ">");
				String empfangen2 = socket.empfangen();

				if (!empfangen2.substring(0, 3).equals("250")
						&& !empfangen2.substring(0, 3).equals("251")) {
					throw new Exception (empfangen2);
				}
			}
			else {
				return false;
			}
		}
		return true;
	}

	/**
	 * Hier wird nur "DATA" gesendet, was den nachfolgende Email-Versand
	 * anzeigt. Da DATA laut RFC 821 nur einen Intermediate Status andeutet,
	 * bliebe zu ueberlegen, ob nicht das anschliessende senden der Nachricht in
	 * dieser Methode gleich mit aufgerufen wird...
	 *
	 * @return
	 * @throws Exception
	 */
	private boolean schickeDataBeginn() throws Exception {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPClient), schickeDataBeginn()");
		socket.senden("DATA");
		String empfangen3 = socket.empfangen();

		if (empfangen3.substring(0, 3).equals("354")) {
			return true;
		}
		else {
			throw new Exception(empfangen3);
		}
	}

	/**
	 * Hier wird der Datenteil der Email an sich versendet.
	 *
	 * @param email
	 * @return
	 * @throws Exception
	 */
	private boolean schickeData(Email email) throws Exception {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPClient), schickeData("+email+")");
		if (email != null) {
			socket.senden(email.toString() + "\r\n.\r\n");
			String empfangen34 = socket.empfangen();

			if (empfangen34.startsWith("250")) {
				return true;
			}
			else {
			throw new Exception (empfangen34);
			}
		}
		else {
			return false;
		}
	}

	/**
	 * Hiermit wird ein Quit gesendet, die zum Beenden der Uebertragung dient.
	 *
	 * @return
	 * @throws TimeOutException
	 * @throws VerbindungsException
	 */
	private boolean schickeQuit() throws VerbindungsException, TimeOutException {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPClient), schickeQuit()");
		String empfangen;

		socket.senden("QUIT");

		empfangen = socket.empfangen();
		if (empfangen.startsWith("221")) {
			return true;
		}
		else {
			return false;
		}
	}
}
