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
import java.util.Observable;

import filius.Main;
import filius.hardware.knoten.Knoten;

/** Die Klasse SystemSoftware umfasst die grundlegenden Funktionen einer
 * Systemsoftware, die auf allen Stationen (Endsysteme und Uebertragungseinheiten)
 * zur Verfuegung stehen muss. <br />
 * Die Klasse ist abstrakt und wird fuer die verschiedenen Stationen
 * unterschiedlich implementiert.
 */
public abstract class SystemSoftware extends Observable implements Serializable{

	/** Die Hardware, auf der diese Systemsoftware laeuft. */
	private Knoten hardware;

	/** Diese Methode wird beim Wechsel vom Konfigurationsmodus
	 * (zum Aufbau des Rechnernetzes und Konfiguration der Komponenten)
	 * zum Aktionsmodus (mit der Moeglichkeit den Datenaustausch zu
	 * simulieren) ausgefuehrt! <br />
	 * In den implementierenden Unterklassen sollen an dieser Stelle
	 * alle Threads zur Simulation des virtuellen Netzwerks gestartet
	 * werden.
	 */
	public abstract void starten();

	/** Diese Methode wird beim Wechsel vom Aktionsmodus (mit der
	 * Moeglichkeit den Datenaustausch zu simulieren) zum
	 * Konfigurationsmodus (zum Aufbau des Rechnernetzes und
	 * Konfiguration der Komponenten) ausgefuehrt! <br />
	 * In den implementierenden Unterklassen sollen an dieser Stelle
	 * alle Threads zur Simulation des virtuellen Netzwerks angehalten
	 * werden.
	 */
	public abstract void beenden();

	public Knoten getKnoten() {
		return hardware;
	}

	public void setKnoten(Knoten hardware) {
		this.hardware = hardware;
//		Main.debug.println("DEBUG: SystemSoftware ("+this.hashCode()+") now is connected to Knoten ("+hardware.hashCode()+")");
	}

	/**
	 * Statusnachrichten werden damit an die Beobachter
	 * weitergeleitet.
	 */
	public void benachrichtigeBeobacher(Object o) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (SystemSoftware), benachrichtigeBeobachter("+o+")");
		setChanged();
		notifyObservers(o);
	}
}
