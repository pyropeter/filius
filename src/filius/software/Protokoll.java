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
package filius.software;

import filius.software.system.SystemSoftware;

/** Diese Klasse ist Oberklasse der Protokolle auf den verschiedenen
 * Schichten. Es werden Protokolle fuer vier Schichten implementiert:
 * <ol>
 *   <li> Netzzugang </li>
 *   <li> Vermittlung </li>
 *   <li> Transport </li>
 *   <li> Anwendung </li>
 * </ol>
 * Die Anwendungsschicht wird durch die verschiedenen Anwendungen
 * implementiert. <br />
 * Peterson und Davie beschreiben zwei moegliche Grundarten der
 * Implementierung des Protokollstapels mit Prozessen / Threads. Den
 * ersten Ansatz nennen sie Prozess-pro-Protokoll. D. h., fuer jede
 * Schicht wird ein Prozess gestartet, der Daten von der hoeheren und
 * von der niedrigeren Schicht entgegen nimmt und weitergibt. Der zweite
 * Ansatz ist Prozess-pro-Nachricht. D. h. für jede Nachricht existiert
 * ein Prozess, der die Verarbeitung auf allen Schichten veranlasst. <br />
 * Hier werden fuer die Weiterleitung der Daten zur naechst
 * hoeheren Schicht jeweils ein Thread implementiert. Der Versand
 * der Daten von der obersten zur untersten Schicht erfolgt durch den
 * Thread der Anwendung. Damit ist das eine Verbindung der zwei
 * beschriebenen Ansaetze.
 *
 * @author stefan
 */
public abstract class Protokoll {

	/** Die Systemsoftware, die auf der Hardware-Komponente laeuft */
	private SystemSoftware systemSoftware;

	/** Konstruktor fuer Protokolle. Hier wird lediglich die
	 * Systemsoftware initialisiert.
	 * @param systemSoftware die Systemsoftware der Hardware-Komponente,
	 *   auf der das Protokoll laeuft. */
	public Protokoll(SystemSoftware systemSoftware) {
		this.systemSoftware = systemSoftware;
	}

	/** Methode fuer den Zugriff auf die Systemsoftware */
	public SystemSoftware holeSystemSoftware() {
		return systemSoftware;
	}

	/** Methode zum Starten der Protokoll-Threads. Diese Methode wird beim
	 * Wechsel vom Entwurfs- zum Aktionsmodus durch die Systemsoftware
	 * aufgerufen. Hier werden die notwendigen Threads gestartet.
	 */
	public abstract void starten();

	/** Methode zum Beenden der Protokoll-Threads. Diese Methode wird
	 * beim Wechsel vom Entwurfs- zum Aktionsmodus durch die Systemsoftware
	 * aufgerufen. Hier werden alle vorhandenen Threads dieser Schicht
	 * beendet.
	 */
	public abstract void beenden();
}
