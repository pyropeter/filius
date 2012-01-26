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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

import filius.Main;
import filius.hardware.Port;
import filius.hardware.knoten.Switch;
import filius.rahmenprogramm.I18n;
import filius.software.netzzugangsschicht.EthernetFrame;
import filius.software.netzzugangsschicht.SwitchPortBeobachter;

/**
 * Diese Klasse stellt die Funktionalitaet des Switches zur Verfuegung.
 * Wichtiges Element ist die Source Address Table (SAT). Der Switch operiert nur
 * auf der Netzzugangsschicht, auf der MAC-Adressen verwendet werden.
 */
public class SwitchFirmware extends SystemSoftware implements I18n {

	private static final long serialVersionUID = 1L;

	/**
	 * Die Source Address Tabel (SAT), in der die MAC-Adressen den physischen
	 * Anschluessen des Switch zugeordnet werden
	 */
	private HashMap<String, Port> sat = null;

	/**
	 * Liste der Anschlussbeobachter. Sie implementieren die Netzzugangsschicht.
	 */
	private LinkedList<SwitchPortBeobachter> switchBeobachter;

	/**
	 * Hier werden bereits weitergeleitete Frames gespeichert. Wird ein Frame
	 * wiederholt verschickt, beispielsweise wegen einer Verbindung, die zwei
	 * Anschluesse kurzschliesst, wird der Frame verworfen.
	 *
	 * @see filius.software.netzzugangsschicht.SwitchPortBeobachter
	 */
	private LinkedList<EthernetFrame> durchgelaufeneFrames = new LinkedList<EthernetFrame>();

	/**
	 * Hier wird die Netzzugangsschicht des Switch initialisiert und gestartet.
	 * Ausserdem wird die SAT zurueckgesetzt.
	 */
	public void starten() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (SwitchFirmware), starten()");
		SwitchPortBeobachter anschlussBeobachter;

		sat = new HashMap<String, Port>();
		switchBeobachter = new LinkedList<SwitchPortBeobachter>();

		ListIterator it = ((Switch) getKnoten()).getAnschluesse()
				.listIterator();
		while (it.hasNext()) {
			Port anschluss = (Port) it.next();
			anschlussBeobachter = new SwitchPortBeobachter(this, anschluss);
			anschlussBeobachter.starten();
			switchBeobachter.add(anschlussBeobachter);
		}
	}

	/** Hier wird die Netzzugangsschicht des Switch gestoppt. */
	public void beenden() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (SwitchFirmware), beenden()");
		ListIterator it = switchBeobachter.listIterator();
		while (it.hasNext()) {
			SwitchPortBeobachter anschlussBeobachter = (SwitchPortBeobachter) it
					.next();
			anschlussBeobachter.beenden();
		}
	}

	/** Diese Methode wird genutzt, um die SAT abzurufen. */
	public Vector holeSAT() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (SwitchFirmware), holeSAT()");
		Vector<Vector> eintraege = new Vector<Vector>();
		Vector<String> eintrag;
		String ausgabe;

		for (String elem : sat.keySet()) {
			Port anschluss = (Port) sat.get(elem);
			ausgabe = messages.getString("sw_switchfirmware_msg1")
					+ " "+((Switch) getKnoten()).getAnschluesse()
							.indexOf(anschluss);
			eintrag = new Vector<String>();
			eintrag.add(elem.toUpperCase());
			eintrag.add(ausgabe);
			eintraege.add(eintrag);
		}

		return eintraege;
	}

	/**
	 * Methode zum erzeugen eines neuen Eintrags in der SAT. Wenn bereits ein
	 * Eintrag zu der uebergebenen MAC-Adresse vorliegt, wird der alte Eintrag
	 * aktualisiert.
	 *
	 * @param macAdresse
	 *            die MAC-Adresse des entfernten Anschlusses
	 * @param anschluss
	 *            der Anschluss des Switch, der mit dem entfernten Anschluss
	 *            verbunden ist
	 */
	public void hinzuSatEintrag(String macAdresse, Port anschluss) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (SwitchFirmware), hinzuSatEintrag("+macAdresse+","+anschluss+")");
		if (!sat.containsKey(macAdresse)) {
//			Main.debug.println("SwitchFirmware: Neuer Eintrag in SAT-Tabelle: " + macAdresse + "->" + anschluss.toString());
		}
		else {
			sat.remove(macAdresse);
//			Main.debug.println("SwitchFirmware: Eintrag fuer " + macAdresse + " in SAT aktualisiert.");
		}

		sat.put(macAdresse, anschluss);
	}

	/**
	 * Mit dieser Methode wird der Anschluss ausgewaehlt, der die Verbindung zum
	 * Anschuss mit der uebergebenen MAC-Adresse herstellt. Dazu wird die SAT
	 * verwendet.
	 *
	 * @param macAdresse
	 *            die Zieladresse eines Frames nach der in der SAT gesucht
	 *            werden soll
	 * @return der Anschluss zur MAC oder null, wenn kein passender Eintrag
	 *         existiert
	 */
	public Port holeAnschlussFuerMAC(String macAdresse) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (SwitchFirmware), holeAnschlussFuerMAC("+macAdresse+")");
		if (sat.containsKey(macAdresse)) {
			return (Port) sat.get(macAdresse);
		}
		else {
			return null;
		}
	}

	/**
	 * Methode zum Zugriff auf die bereits durchgelaufenen Frames. Diese wird
	 * dazu genutzt um Fehler durch Zyklen zu vermeiden.
	 *
	 * @return Liste der bereits weitergeleiteten Frames.
	 */
	public LinkedList<EthernetFrame> holeDurchgelaufeneFrames() {
		return durchgelaufeneFrames;
	}
}
