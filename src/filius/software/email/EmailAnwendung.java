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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

import filius.Main;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.software.Anwendung;
import filius.software.system.Datei;

/**
 *
 * @author Andre Asschoff
 *
 */
public class EmailAnwendung extends Anwendung {
	// Attribute
	private Vector adressbuch = new Vector();

	private int sitzungsnummer = 0;

	private LinkedList<Email> erstellteNachrichten = new LinkedList<Email>();

	private LinkedList<Email> empfangeneNachrichten = new LinkedList<Email>();

	private LinkedList<Email> gesendeteNachrichten = new LinkedList<Email>();

	private HashMap kontoListe = new HashMap();

	private POP3Client pop3client;

	private SMTPClient smtpclient;

	// Methoden
	/**
	 * Startet die Email-Anwendung und für Sie jeweils einen Pop3- und
	 * Smtp-Client.
	 */
	public void starten() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (EmailAnwendung), starten()");
		super.starten();

		pop3client = new POP3Client(this);
		pop3client.starten();

		smtpclient = new SMTPClient(this);
		smtpclient.starten();
	}

	/**
	 * Methode beendet die EmailAnwendung inkl. der dazu gehörigen smtp und pop3
	 * clients. Dazu wird die Methode der Superklasse aufgerufen und der Socket
	 * geschlossen.
	 */
	public void beenden() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (EmailAnwendung), beenden()");
		super.beenden();
		if (pop3client != null)
			pop3client.beenden();
		if (smtpclient != null)
			smtpclient.beenden();
	}

	/**
	 * ruft die Methode versendeEmail in SMTPClient auf, um eine Email zu
	 * versenden. Diese Methode selbst ist nicht blockierend und übernimmt auch
	 * den Verbindungs- auf-,bzw. abbau.
	 *
	 * @param email
	 * @param remoteServerIP
	 */
	public void versendeEmail(String remoteServerIP, Email email,
			String absender) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (EmailAnwendung), versendeEmail("+remoteServerIP+","+email+","+absender+")");

		String rcpts = "";

		if (email.getEmpfaenger().size() > 0)
			rcpts += llzuStr(email.getEmpfaenger());
		if (email.getCc().size() > 0) {
			if (!rcpts.equals(""))
				rcpts += ",";
			rcpts += llzuStr(email.getCc());
		}
		if (email.getBcc().size() > 0) {
			if (!rcpts.equals(""))
				rcpts += ",";
			rcpts += llzuStr(email.getBcc());
		}
		smtpclient.versendeEmail(remoteServerIP, email, absender, rcpts);

	}

	/**
	 *
	 * @param emailAdresse
	 * @param benutzername
	 * @param passwort
	 * @param pop3Port
	 * @param pop3Server
	 */
	public void emailsAbholenEmails(String benutzername, String passwort,
			String pop3Port, String pop3Server) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (EmailAnwendung), emailsAbholenEmails("+benutzername+","+passwort+","+pop3Port+","+pop3Server+")");
		pop3client.emailsHolen(pop3Server, pop3Port, benutzername, passwort);
	}

	/**
	 * Diese Mehtode wandelt eine LL in einen String, die einzelnen
	 * Listenelemente durch Kommata getrennt.
	 *
	 * @param args
	 * @return
	 */
	public String llzuStr(LinkedList args) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (EmailAnwendung), llzuStr("+args+")");
		String str = "";
		for (ListIterator iter = args.listIterator(); iter.hasNext();) {
			str = str + ((String) iter.next());
			if (iter.hasNext())
				str += ",";
		}

		return str;
	}

	/**
	 * FUNKTIONIERT
	 *
	 * @param name
	 * @param vorname
	 * @param strasse
	 * @param hausnr
	 * @param plz
	 * @param wohnort
	 * @param email
	 * @param telefon
	 * @return
	 */
	public boolean kontaktHinzufuegen(String name, String vorname,
			String strasse, int hausnr, int plz, String wohnort, String email,
			String telefon) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (EmailAnwendung), kontaktHinzufuegen("+name+","+vorname+","+strasse+","+hausnr+","+plz+","+wohnort+","+email+","+telefon+")");
		if (EingabenUeberpruefung.isGueltig(name,
				EingabenUeberpruefung.musterMindEinZeichen)
				&& EingabenUeberpruefung.isGueltig(vorname,
						EingabenUeberpruefung.musterMindEinZeichen)
				&& EingabenUeberpruefung.isGueltig(email,
						EingabenUeberpruefung.musterEmailAdresse)) {
			try {
				Kontakt kontaktNeu = new Kontakt();

				kontaktNeu.setName(name);
				kontaktNeu.setVorname(vorname);
				kontaktNeu.setStrasse(strasse);
				kontaktNeu.setHausnr(hausnr);
				kontaktNeu.setPlz(plz);
				kontaktNeu.setWohnort(wohnort);
				kontaktNeu.setEmail(email);
				kontaktNeu.setTelefon(telefon);

				getAdressbuch().add(kontaktNeu);
			} catch (Exception e) {
				e.printStackTrace(Main.debug);
				return false;
			}
		} else {
			return false;
		}

		return true;
	}

	/**
	 * FUNKTIONIERT
	 *
	 * @param name
	 * @param vorname
	 * @param email
	 * @return
	 */
	public boolean kontaktLoeschen(String name, String vorname, String email) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (EmailAnwendung), kontaktLoeschen("+name+","+vorname+","+email+")");
		if (EingabenUeberpruefung.isGueltig(name,
				EingabenUeberpruefung.musterMindEinZeichen)
				&& EingabenUeberpruefung.isGueltig(vorname,
						EingabenUeberpruefung.musterMindEinZeichen)
				&& EingabenUeberpruefung.isGueltig(email,
						EingabenUeberpruefung.musterEmailAdresse)) {
			for (ListIterator iter = adressbuch.listIterator(); iter.hasNext();) {
				Kontakt kontakt = (Kontakt) iter.next();

				if (email.equalsIgnoreCase(kontakt.getEmail())) {
					if (name.equals(kontakt.getName())
							&& vorname.equals(kontakt.getVorname())) {
						adressbuch.remove(kontakt);
						return true;
					}
				}
			}
		} else {
		}

		return false;
	}

	public void speichern() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (EmailAnwendung), speichern()");
		Datei datei = new Datei();
		String kontenString = "";
		Iterator iter = kontoListe.values().iterator();

		while (iter.hasNext()) {
			EmailKonto konto = (EmailKonto) iter.next();

			kontenString += konto.toString() + "\n";
		}
		datei.setDateiInhalt(kontenString);
		datei.setName("konten.txt");
		datei.setDateiTyp("text/txt");
		getSystemSoftware().getDateisystem().speicherDatei(
				getSystemSoftware().getDateisystem().getRoot(), datei);
	}

	public void laden() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (EmailAnwendung), laden()");
		Datei datei = getSystemSoftware().getDateisystem().holeDatei(
				getSystemSoftware().getDateisystem().getRoot(), "konten.txt");
		if (datei != null) {
			String kontenString = datei.getDateiInhalt();
			String[] konten = kontenString.split("\n");
			for (int i = 0; i < konten.length; i++) {
				String konto = konten[i];
				EmailKonto tmpKonto = new EmailKonto(konto);
				kontoListe.put(tmpKonto.getBenutzername(), tmpKonto);
			}
		}

	}

	public POP3Client holePOP3Client() {
		return pop3client;
	}

	// GET- und SET-Methoden

	public Vector getAdressbuch() {
		return adressbuch;
	}

	public void setAdressbuch(Vector adressbuch) {
		this.adressbuch = adressbuch;
	}

	public int getSitzungsnummer() {
		return sitzungsnummer;
	}

	public void setSitzungsnummer(int sitzungsnummer) {
		this.sitzungsnummer = sitzungsnummer;
	}

	public LinkedList getEmpfangeneNachrichten() {
		return empfangeneNachrichten;
	}

	public void setEmpfangeneNachrichten(LinkedList empfangeneNachrichten) {
		this.empfangeneNachrichten = empfangeneNachrichten;
	}

	public LinkedList getErstellteNachrichten() {
		return erstellteNachrichten;
	}

	public void setErstellteNachrichten(LinkedList erstellteNachrichten) {
		this.erstellteNachrichten = erstellteNachrichten;
	}

	public LinkedList getGesendeteNachrichten() {
		return gesendeteNachrichten;
	}

	public void setGesendeteNachrichten(LinkedList gesendeteNachrichten) {
		this.gesendeteNachrichten = gesendeteNachrichten;
	}

	public HashMap getKontoListe() {
		return kontoListe;
	}

	public void setKontoListe(HashMap kontoListe) {
		this.kontoListe = kontoListe;
	}
}
