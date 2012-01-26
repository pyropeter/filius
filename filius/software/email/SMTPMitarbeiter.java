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

/** Der Mitarbeiter dient dazu, den Datenaustausch mit einem Client durchzufuehren. <br />
 * Dazu durchlaeuft er folgende Zustaende:
 * <ol>
 * <li> START: Empfang des Begruessungskommandos (HELO oder EHLO) </li>
 * <li> ENVELOPE: Empfang der 'Briefumschlags'-Informationen (Sender, Empfaenger etc) </li>
 * <li> DATA: Empfang der eigentlichen E-Mail mit Kopfdaten und Nutzdaten, die
 *  mit einem Zeilenumbruch, einem Punkt, einem Zeilenumbruch beendet werden
 *  (&lt;CR&gt;&lt;LF&gt;.&lt;CR&gt;&lt;LF&gt;) </li>
 * <li> END: Der Datenaustausch wurde beendet. (Das ist der einzig gueltige
 *   Endzustand!) </li>
 * </ol>
 * Nachdem eine Nachricht ordnungsgemaess empfangen wurde, befindet sich der
 * Mitarbeiter im END-Zustand. Das heisst, dass dieser Mail-Server es nicht ermoeglicht
 * mit einer Verbindung mehrere E-Mails zu uebertragen.
 *
 */
public class SMTPMitarbeiter extends ServerMitarbeiter
{
	private static final int START=0, ENVELOPE=1, DATA=2, END=3;
	private EmailServer emailServer;

	private int zustand = START;

	private LinkedList rcptTo = new LinkedList();
	private String mailFrom = "";
	private String quelltext="";

	private Email email = null;
	private EmailKonto empfaengerKonto = new EmailKonto();


	//	Konstruktor(en)
	public SMTPMitarbeiter(//Betriebssystem bs,
			TCPSocket socket, SMTPServer server)
	{
		super(server, socket);
		Main.debug.println("INVOKED-2 ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPMitarbeiter), constr: SMTPMitarbeiter("+socket+","+server+")");

		this.socket = socket;
		this.emailServer = server.holeEmailServer();
		senden(messages.getString("sw_smtpmitarbeiter_msg1") + " " + emailServer.getMailDomain());
	}

//	Methoden

	/** In dieser Methode werden die ankommenden Befehle und
	 * Daten zur Uebertragung einer E-Mail verarbeitet.
	 */
	protected void verarbeiteNachricht(String nachricht) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPMitarbeiter), verarbeiteNachricht("+nachricht+")");
		String[] tokens;
		String tmp;
		int pos, pos2;

		emailServer.benachrichtigeBeobachter(socket.holeZielIPAdresse()+"< "+nachricht);

		// Verarbeitung der vom Client ankommenden Daten
		if (nachricht.trim().equalsIgnoreCase("QUIT")) {
			senden(messages.getString("sw_smtpmitarbeiter_msg2"));
			socket.schliessen();
		}
		else if (zustand == START) {
			pos = nachricht.indexOf(" ");
			if (pos > 0) tmp = nachricht.substring(0, pos).trim();
			else tmp = nachricht.trim();

			if (tmp.equalsIgnoreCase("HELO") || tmp.equalsIgnoreCase("EHLO"))
			{
				if (pos > 0) tmp = nachricht.substring(pos, nachricht.length()).trim();
				else tmp = "";

				senden("250 Hello "+ tmp);
				zustand = ENVELOPE;
			}
			else if (tmp.equalsIgnoreCase("NOOP")) {
				senden(messages.getString("sw_smtpmitarbeiter_msg3"));
			}
		}
		else if (zustand == ENVELOPE) {
			pos = nachricht.indexOf(":");
			if (pos > 0) tmp = nachricht.substring(0, pos).trim();
			else tmp = nachricht.trim();

			if (tmp.equalsIgnoreCase("MAIL FROM")) {
				pos = nachricht.indexOf("<");
				pos2 = nachricht.indexOf(">");
				if (pos > 0 && pos2 > pos) {
					mailFrom = nachricht.substring(pos+1, pos2).trim();
					senden(messages.getString("sw_smtpmitarbeiter_msg4"));
				}
				else {
					senden(messages.getString("sw_smtpmitarbeiter_msg5"));
				}
			}
			else if (tmp.equalsIgnoreCase("RCPT TO")) {
				pos = nachricht.indexOf("<");
				pos2 = nachricht.indexOf(">");
				if (pos > 0 && pos2 > pos) {
					rcptTo.add(nachricht.substring(pos+1, pos2).trim());
					senden(messages.getString("sw_smtpmitarbeiter_msg6"));
				}
				else {
					senden(messages.getString("sw_smtpmitarbeiter_msg7"));
				}
			}
			else if (tmp.equalsIgnoreCase("DATA")) {
				if (!mailFrom.equals("") && rcptTo.size() > 0) {
					senden(messages.getString("sw_smtpmitarbeiter_msg8"));
					zustand = DATA;
				}
				else {
					senden(messages.getString("sw_smtpmitarbeiter_msg9"));
				}
			}
			else if (tmp.equalsIgnoreCase("NOOP")) {
				senden("250 OK");
			}
		}
		else if (zustand == DATA) {
			tokens = nachricht.split("\n");
			if ((tokens.length > 0 && tokens[tokens.length-1].trim().equals("."))
					|| (tokens.length > 1 && tokens[tokens.length-2].trim().equals(".") && tokens[tokens.length-2].trim().equals(""))) {
				quelltext = quelltext + nachricht.substring(0, nachricht.lastIndexOf("."));

				email = new Email(quelltext);
				senden(messages.getString("sw_smtpmitarbeiter_msg10"));
				verarbeiteEmail();

				zustand = END;
			}
			else {
				quelltext = quelltext + nachricht;
			}
		}
		else if (zustand == END) {
			senden(messages.getString("sw_smtpmitarbeiter_msg11"));
			zustand = ENVELOPE;
		}
	}


	/** Nachdem eine E-Mail erfolgreich empfangen wurde, wird sie in dieser
	 * Methode in Abhaengigkeit der angegebenen Empfaenger in einem lokalen Postfach
	 * abgelegt bzw. an einen neuen Empfaenger weiter geleitet.
	 */
	private void verarbeiteEmail()
	{
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPMitarbeiter), verarbeiteEmail()");
		String aktuellerEmpfaenger = "";
		String weitereEmpfaenger = "";
		ListIterator it;
		Email kopie = new Email(email.toString());

		it = rcptTo.listIterator();
		while (it.hasNext())
		{
			aktuellerEmpfaenger = (String)it.next();
			String [] temp = aktuellerEmpfaenger.split("@");
			String benutzer = temp[0];

			if(pruefeAufSelbeDomain(aktuellerEmpfaenger))
			{
				empfaengerKonto = emailServer.sucheKonto(benutzer);
				empfaengerKonto.getNachrichten().add(kopie);
				emailServer.benachrichtigeBeobachter(messages.getString("sw_smtpmitarbeiter_msg12")+" "+empfaengerKonto.getBenutzername()+" "+ messages.getString("sw_smtpmitarbeiter_msg13"));
			}
			else
			{
				weitereEmpfaenger = weitereEmpfaenger + aktuellerEmpfaenger + ",";
				//Main.debug.println("=================================WeiterleitungsEmpfaenger sieht so aus: "+weitereEmpfaenger+" =========================");
			}
		}
		if (!weitereEmpfaenger.equals("")) emailServer.emailWeiterleiten(kopie, mailFrom, weitereEmpfaenger);
	}


	/**
	 * Methode prüft, ob die Domain, an die die Email geschickt werden soll, mit der
	 * des eigenen MailServers übereinstimmt.
	 * @param str
	 * @return boolean
	 */
	private boolean pruefeAufSelbeDomain(String str)
	{
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPMitarbeiter), pruefeAufSelbeDomain("+str+")");
		boolean check = false;

		String [] emailAdresse = str.split("@");
		String pruefdomain = emailAdresse[1].substring(0, emailAdresse[1].length());
		if(pruefdomain.equalsIgnoreCase(emailServer.getMailDomain()))
		{
			check = true;
		}
		return check;
	}


	private void zeigeEmail(Email email)
	{
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPMitarbeiter), zeigeEmail("+email+")");

		//Main.debug.println("********************************************************");
		//Main.debug.println("Email Absender: "+email.getAbsender());
		for(ListIterator iter = email.getEmpfaenger().listIterator(); iter.hasNext();){
			//Main.debug.println("Email Empfaenger: "+(String)iter.next());
		}

		for(ListIterator iterc = email.getCc().listIterator(); iterc.hasNext();){
			//Main.debug.println("Email Cc: "+(String)iterc.next());
		}
		for(ListIterator iterb = email.getBcc().listIterator(); iterb.hasNext();){
			//Main.debug.println("Email Bcc: "+(String)iterb.next());
		}
		//Main.debug.println("Email Betreff: "+email.getBetreff());
		//Main.debug.println("Email Datum erhalten: "+email.getDateReceived());
		//Main.debug.println("Email Text: "+email.getText());
		//Main.debug.println("*********************************************************");

	}



	/** Methode zum Senden von Nachrichten. Gleichzeitig wird der Beobachter des
	 * Mail-Servers ueber die verschickte Nachricht informiert!
	 */
	private void senden(String nachricht) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (SMTPMitarbeiter), senden("+nachricht+")");
		try {
			socket.senden(nachricht);
			emailServer.benachrichtigeBeobachter(socket.holeZielIPAdresse()+"> "+nachricht);
		}
		catch (Exception e) {
			e.printStackTrace(Main.debug);
			emailServer.benachrichtigeBeobachter(messages.getString("sw_smtpmitarbeiter_msg14") + " " + socket.holeZielIPAdresse());
		}
	}
}
