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
package filius.software.system;

import java.io.Serializable;

import filius.rahmenprogramm.Base64;

/**
 * Dateien sind Objekte, die vom Betriebssystem verwaltet werden. Jede Datei hat
 * einen Dateinamen (der eindeutig sein sollte, einen Datei Typ (ähnlich einem
 * mime/type) und natürlich den Dateiinhalt selber.
 *
 * @author Nadja & Thomas Gerding
 *
 */
public class Datei implements Serializable {

	/*
	 * Attribute
	 * ----------------------------------------------------------------------------------
	 */

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** Typ der Datei, z.B. mp3, doc, txt,... */
	private String dateiTyp;

	/** Inhalt der Datei als String (ggf. Base64-kodiert) */
	private String dateiInhalt;

	/** Der Dateiname (ist auch der Rueckgabewert von toString()) */
	private String name; // Dateiname
	
	// real size of file without having enforced Base64 encoding
	private long decodedSize = -1;

	/**
	 * Erzeugt ein neues Objekt vom Typ Datei. Dieser Konstruktor wird
	 * benoetigt, damit die Klasse den Anforderungen an JavaBeans entspricht!
	 *
	 */
	public Datei() {

	}

	/**
	 * Erzeugt ein neues Objekt vom Typ Datei
	 *
	 * @param name
	 *            der Bezeichner der Datei
	 * @param typ
	 *            ein Dateittyp
	 * @param dateiInhalt
	 *            der eigentliche Inhalt. Binaerdateien sollten Base64-kodiert
	 *            gespeichert werden.
	 */
	public Datei(String name, String typ, String dateiInhalt) {
		this.dateiInhalt = dateiInhalt;
		this.name = name;
		this.dateiTyp = typ;
	}

	public void setSize(long size) {
		this.decodedSize = size;		
	}
	
	/**
	 * Liefert die Groesse der Datei zurueck
	 *
	 * @return die Laenge des Strings dateiInhalt
	 */
	public long holeGroesse() {
		if(this.getDateiInhalt() == null) return 0;
		if(this.dateiTyp!=null && this.dateiTyp.equals("text")) return this.getDateiInhalt().length();
		else {
			try {
				if(decodedSize<0) setSize(Base64.decode(this.getDateiInhalt()).length);  // set current size to be sure it's correct
				return decodedSize;
			} catch(Exception e) {
				// current file does not seem to be Base64 encoded (that's why an error occurred... hopefully)
				if(decodedSize<0) return this.getDateiInhalt().length();
				else return decodedSize;
			}
		}
	}

	/**
	 * Liefert den Dateiinhalt so zurueck, wie er gespeichert wurde. D. h., dass
	 * kodierte Dateien auch kodiert ausgegeben werden.
	 *
	 * @return der Dateiinhalt als String
	 */
	public String getDateiInhalt() {
		return dateiInhalt;
	}

	public void setDateiInhalt(String dateiInhalt) {
		this.dateiInhalt = dateiInhalt;
	}

	/**
	 * Methode fuer den Zugriff auf Base64-kodierte Dateien.
	 *
	 * @return Gibt den Base64-dekodierten Dateiinhalt als String zurück.
	 * @see setzeDateiInhaltDecoded
	 */
	public String holeDateiInhaltDecoded() {
		return (String) Base64.decodeToObject(this.getDateiInhalt());
	}

	/**
	 * Methode fuer den Zugriff auf Base64-kodierte Dateien.
	 *
	 * @param dateiInhalt
	 *            einen "binaeren" Dateiinhalt, der Base64-kodiert gespeichert
	 *            werden soll
	 */
	public void setzeDateiInhaltDecoded(String dateiInhalt) {
		this.dateiInhalt = Base64.encodeObject(dateiInhalt);
	}

	public String getDateiTyp() {
		return dateiTyp;
	}

	public void setDateiTyp(String dateiTyp) {
		this.dateiTyp = dateiTyp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}
