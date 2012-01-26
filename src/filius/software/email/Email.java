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

/**
 *
 * @author Andre Asschoff
 *
 */
public class Email {
	private String absender;

	private LinkedList<String> empfaenger = new LinkedList<String>();

	private LinkedList<String> cc = new LinkedList<String>();

	private LinkedList<String> bcc = new LinkedList<String>();

	private String betreff;

	private String text = "";

	private boolean neu = true;// dient dazu, um festzustellen, ob eine Email
								// schon mal abgerufen,
	// und somit wahrscheinlich schon mal gelesen wurde. Standardmaessig
	// ist das true, d.h. sie wurde noch nicht gelesen.

	private String dateReceived = "";// dieser Wert wird vom Server, wenn
										// erhalten gesetzt

	private boolean delete = false;// wird als zu loeschen markiert, wenn sich
									// ausgeloggt wird, wird

	// sie endgueltig geloescht. Sie wird dann nur nicht mehr angezeigt.
	private boolean versendet = false;

	public Email() {}

	/**
	 * Dies ist der Konstruktor der Email. Immer wenn eine neue Email erzeugt
	 * wird, wird ihr der String ihrer Attribute übergeben, die dann ausgelesen
	 * werden, und in ihren jeweiligen Attributen gespeichert werden. Bsp.:
	 * <br />
	 * <code>
	 * From: <bob@filius.de>
	 * To: <eve@filius.de>, <ken@uni.de>
	 * Cc: <john@uni.de>
	 * Bcc: <berta@filius.de>
	 * Subject: Eine kurze Nachricht
	 *
	 * Das ist der Nachrichtentext.
	 * </code>
	 *
	 * @param nachricht
	 */
	public Email(String nachricht) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Email), constr: Email("+nachricht+")");
		String tmp;
		String[] liste;
		String[] emaildaten = nachricht.split("\n");
		int pos1, pos2;

		for (int i = 0; i < emaildaten.length; i++) {
			pos1 = 0;
			pos2 = emaildaten[i].indexOf(":");
			if (pos2 > pos1) tmp = emaildaten[i].substring(pos1, pos2).trim();
			else tmp = "";

			if (tmp.equalsIgnoreCase("from")) {
//				pos1 = emaildaten[i].indexOf("<") + 1;
//				pos2 = emaildaten[i].indexOf(">");
				absender = emaildaten[i].substring(emaildaten[i].indexOf(":")+1).trim();
			}
			else if (tmp.equalsIgnoreCase("to")) {
				empfaenger.clear();
				liste = emaildaten[i].split(",");
				for (int j = 0; j < liste.length; j++) {
					pos1 = liste[j].indexOf("<") + 1;
					pos2 = liste[j].indexOf(">");
					if (pos1 >= 0 && pos2 > pos1)
						empfaenger.add(liste[j].substring(pos1, pos2).trim());
				}
			}
			else if (tmp.equalsIgnoreCase("cc")) {
				cc.clear();
				liste = emaildaten[i].split(",");
				for (int j = 0; j < liste.length; j++) {
					pos1 = liste[j].indexOf("<") + 1;
					pos2 = liste[j].indexOf(">");
					if (pos1 >= 0 && pos2 > pos1)
						cc.add(liste[j].substring(pos1, pos2).trim());
				}
			}
			else if (tmp.equalsIgnoreCase("bcc")) {
				bcc.clear();
				liste = emaildaten[i].split(",");
				for (int j = 0; j < liste.length; j++) {
					pos1 = liste[j].indexOf("<") + 1;
					pos2 = liste[j].indexOf(">");
					if (pos1 >= 0 && pos2 > pos1)
						bcc.add(liste[j].substring(pos1, pos2).trim());
				}
			}
			else if (tmp.equalsIgnoreCase("subject")) {
				pos1 = emaildaten[i].indexOf(":") + 1;
				pos2 = emaildaten[i].length();
				if (pos1 > 0)
					betreff = emaildaten[i].substring(pos1, pos2).trim();
			}
			else {
				if (!text.equals("")) text = text + "\n";
				text = text + emaildaten[i];
			}
		}
	}


	/**
	 * In dieser Methode werden die Attribute der Email wieder zu einem langen
	 * String zusammen gesetzt. Er hat anschliessend wieder die gleiche Form,
	 * wie der String, der beim Erzeugen eines neuen Email-Objektes mit
	 * uebergeben wurde. <br />
	 * <b> Achtung: </b> Die BCC-Empfaenger werden nicht mit ausgegeben! <br />
	 * Bsp.: <br />
	 * <code>
	 * From: <bob@filius.de>
	 * To: <eve@filius.de>, <ken@uni.de>
	 * Cc: <john@uni.de>
	 * Subject: Eine kurze Nachricht
	 *
	 * Das ist der Nachrichtentext.
	 * </code>
	 */
	public String toString() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Email), toString()");
		String ergebnis;
		ListIterator<String> it;
		String toListe = "", ccListe = "";

		it = empfaenger.listIterator();
		String nextReceiver;
		while (it.hasNext()) {
			nextReceiver = (String) it.next();
			if(nextReceiver.indexOf("<")>=0)
				toListe = toListe + nextReceiver;
			else
				toListe = toListe + "<" + nextReceiver + ">";
			if (it.hasNext()) toListe = toListe + ", ";
		}

		it = cc.listIterator();
		while (it.hasNext()) {
			nextReceiver = (String) it.next();
			if(nextReceiver.indexOf("<")>=0)
				ccListe = ccListe + nextReceiver;
			else
				ccListe = ccListe + "<" + nextReceiver + ">";
			if (it.hasNext()) ccListe = ccListe + ", ";
		}

		ergebnis = "";
		if (absender != null) {
			ergebnis += "From: " + absender.trim() + "" + "\n";
		}
		if (!toListe.equals("")) {
			ergebnis += "To: " + toListe + "\n";
		}
		if (!ccListe.equals("")) {
			ergebnis += "Cc: " + ccListe + "\n";
		}
		if (betreff != null) {
			ergebnis += "Subject: " + betreff.trim()+"\n";
		}
		if (!dateReceived.equals("")) {
			ergebnis += "Date Received: "+dateReceived+"\n";
		}
		if (text != null) {
			ergebnis+= "\n" + text.trim();
		}

		return ergebnis;
	}

	public boolean getDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public boolean getNeu() {
		return neu;
	}

	public void setNeu(boolean neu) {
		this.neu = neu;
	}

	public String getAbsender() {
		return absender;
	}

	public LinkedList<String> getEmpfaenger() {
		return empfaenger;
	}

	public String getBetreff() {
		return betreff;
	}

	public String getText() {
		return text;
	}

	public void setAbsender(String absender) {
		this.absender = absender;
	}

	public void setBetreff(String betreff) {
		this.betreff = betreff;
	}

	public void setEmpfaenger(LinkedList<String> empfaenger) {
		this.empfaenger = empfaenger;
	}

	public void setText(String text) {
		this.text = text;
	}


	public String getDateReceived() {
		return dateReceived;
	}

	public void setDateReceived(String dateReceived) {
		this.dateReceived = dateReceived;
	}

	public boolean isVersendet() {
		return versendet;
	}

	public void setVersendet(boolean versendet) {
		this.versendet = versendet;
	}

	public LinkedList<String> getBcc() {
		return bcc;
	}

	public void setBcc(LinkedList<String> bcc) {
		this.bcc = bcc;
	}

	public LinkedList<String> getCc() {
		return cc;
	}

	public void setCc(LinkedList<String> cc) {
		this.cc = cc;
	}
}
