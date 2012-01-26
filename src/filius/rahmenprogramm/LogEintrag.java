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
package filius.rahmenprogramm;

import filius.Main;
import filius.gui.netzwerksicht.GUIKnotenItem;

import java.util.Calendar;
import java.util.Date;

/**
 * Klasse für LogEinträge. Diese wird benötigt um Einträge sortierbar und filterbar zu machen.
 * 
 * @author Thomas Gerding & Johannes Bade
 *
 */
public class LogEintrag {

	private String logNachricht; //Text des LogEintrags
	private Calendar timestamp; //Zeitpunkt der Aktion in Form eines Calendar Objekts zur besseren Konvertierbarkeit (gibts das Wort?)
	private int schicht; //Netzwerkschicht zu der die Meldung gehört
	/*
	 * Schichten:
	 * 1 - Vermittlungsschicht
	 * 2 - Transportschicht
	 * 3 - Anwendungsschicht
	 */
	private GUIKnotenItem ausloesendesItem; //GUIItem zu dem der LogEintrag gehört (auslösende Stelle, wenn möglich?!)
	//FIXME: Diese GUIItem Geschichte erscheint noch nicht wirklich ideal, da muss nochmal richtig drüber nachgedacht werden!
	
	/**
	 * Konstruktor der LogEintrag Klasse.
	 * @author Thomas Gerding & Johannes Bade
	 * @param Aktion
	 * @param schicht
	 */
	public LogEintrag(String Aktion, int schicht)
	{
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", LogEintrag("+Aktion+","+schicht+")");
		timestamp = Calendar.getInstance();
		timestamp.setTimeInMillis(new Date().getTime());
		logNachricht = Aktion;
		this.schicht = schicht;
	}

	public String getLogNachricht() {
		return logNachricht;
	}

	public void setLogNachricht(String logNachricht) {
		this.logNachricht = logNachricht;
	}

	public int getSchicht() {
		return schicht;
	}

	public void setSchicht(int schicht) {
		this.schicht = schicht;
	}

	public Calendar getTimestamp() {
		return timestamp;
	}
}
