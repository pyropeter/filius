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

import filius.Main;
import filius.rahmenprogramm.*;

/**
 *
 * @author Andre Asschoff.
 *
 */
public class EmailKonto
{
	private String benutzername;
	private String passwort;
	private String nachname = "";
	private String vorname = "";
	private String pop3server, smtpserver, pop3port, smtpport;
	private String emailAdresse;

	private LinkedList nachrichten = new LinkedList();

	public EmailKonto() {}

	/* Liest durch Semicolon getrennte Werte ein */
	public EmailKonto(String kontoString)
	{
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (EmailKonto), constr: EmailKonto("+kontoString+")");
		String[] teile = kontoString.split(";");
		if (teile.length > 8)
		{
			 pop3server  = teile[0];
			 smtpserver = teile[1];
			 pop3port = teile[2];
			 smtpport = teile[3];
			 benutzername = teile[4];
			 passwort = teile[5];
			 nachname = teile[6];
			 vorname = teile[7];
			 emailAdresse = teile[8];
		}
	}

	public String toString()
	{
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (EmailKonto), toString()");
		String ergebnis = ""
		+ pop3server +";"
		+ smtpserver+";"
		+ pop3port+";"
		+ smtpport+";"
		+ benutzername+";"
		+ passwort+";"
		+ nachname+";"
		+ vorname+";"
		+ emailAdresse;
		return ergebnis;
	}

	public String getBenutzername()
	{
		return benutzername;
	}

	public String getPasswort()
	{
		return passwort;
	}

	public LinkedList getNachrichten()
	{
		return nachrichten;
	}

	public void setBenutzername(String benutzername)
	{
		this.benutzername = benutzername;
	}

	public void setPasswort(String passwort)
	{
		this.passwort = passwort;
	}

	public void setNachrichten(LinkedList nachrichten)
	{
		this.nachrichten = nachrichten;
	}

	public String getNachname() {
		return nachname;
	}

	public void setNachname(String nachname) {
		if (nachname != null) this.nachname = nachname;
		else this.nachname = "";
	}

	public String getVorname() {
		return vorname;
	}

	public void setVorname(String vorname) {
		if (vorname != null) this.vorname = vorname;
		else this.vorname = "";
	}

	/**
	 * @return the pop3port
	 */
	public String getPop3port() {
		return pop3port;
	}

	/**
	 * @param pop3port the pop3port to set
	 */
	public void setPop3port(String pop3port) {
		this.pop3port = pop3port;
	}

	/**
	 * @return the pop3server
	 */
	public String getPop3server() {
		return pop3server;
	}

	/**
	 * @param pop3server the pop3server to set
	 */
	public void setPop3server(String pop3server) {
		this.pop3server = pop3server;
	}

	/**
	 * @return the smtpport
	 */
	public String getSmtpport() {
		return smtpport;
	}

	/**
	 * @param smtpport the smtpport to set
	 */
	public void setSmtpport(String smtpport) {
		this.smtpport = smtpport;
	}

	/**
	 * @return the smtpserver
	 */
	public String getSmtpserver() {
		return smtpserver;
	}

	/**
	 * @param smtpserver the smtpserver to set
	 */
	public void setSmtpserver(String smtpserver) {
		this.smtpserver = smtpserver;
	}

	/**
	 * @return the emailAdresse
	 */
	public String getEmailAdresse() {
		return emailAdresse;
	}

	/**
	 * @param emailAdresse the emailAdresse to set
	 */
	public void setEmailAdresse(String emailAdresse) {
		this.emailAdresse = emailAdresse;
	}
}
