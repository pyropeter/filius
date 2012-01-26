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

import java.util.LinkedList;

import filius.Main;

/** Diese Klasse ist die Oberklasse von Protokoll-Threads.
 * Die Aufgabe ist, den Puffer zu ueberwachen, und die
 * Verarbeitung der eingehenden Dateneinheiten zu starten.
 *
 * @author stefan
 *
 */
public abstract class ProtokollThread extends Thread {

	/** ob der Thread gerade am laufen ist */
	protected boolean running = false;

	/** der von dem Thread zu ueberwachende Puffer */
	private LinkedList<?> puffer;

	/** Leerer Konstruktur, der nur dann genutzt werden darf,
	 * wenn von diesem Thread kein Puffer ueberwacht werden
	 * soll. In diesem Fall <b>muss</b> auch die run()-Methode
	 * ueberschrieben werden!
	 */
	public ProtokollThread() {	}

	/** Konstruktur, der ausschliesslich die Initialisierung
	 * des zu ueberwachenden Puffers uebernimmt, der als
	 * Parameter uebergeben wird.
	 * @param puffer der zu ueberwachende Puffer
	 */
	public ProtokollThread(LinkedList<?> puffer) {
		this.puffer = puffer;
	}

	/** Diese Methode implementiert die Ueberwachung des Puffers
	 * in einem eigenen Thread. Solange sich keine Dateneinheit in
	 * dem Puffer befindet, geht der Thread in den wartenden Zustand.
	 * Wenn eine Dateneinheit eintrifft, wird diese mit dem Aufruf
	 * der Methode verarbeiteDatenEinheit() zur weiteren Verarbeitung
	 * weitergegeben.
	 */
	public void run() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ProtkollThread), run()");
		while (running) {
			synchronized(puffer){
				if(puffer.size() < 1){
					try {
						puffer.wait();
					}
					catch (InterruptedException e) { }
				}
				if(puffer.size() > 0){
					verarbeiteDatenEinheit(puffer.removeFirst());
				}
			}
		}
	}

	/** Diese Methode wird aufgerufen, wenn eine Dateneinheit in dem
	 * ueberwachten Puffer eintrifft.
	 * @param datenEinheit die im ueberwachten Puffer eingetroffene
	 *   Dateneinheit
	 */
	protected abstract void verarbeiteDatenEinheit(Object datenEinheit);

	/** Methode fuer den Zugriff auf den zu ueberwachenden Puffer */
	protected LinkedList<?> holeEingangsPuffer() {
		return puffer;
	}

	/** Methode zum Starten des Threads beim Wechsel vom Entwurfs-
	 * in den Aktionsmodus. Wenn sich der Thread noch in einem wartenden
	 * oder blockierten Zustand wird kein neuer Thread gestartet, sondern
	 * lediglich gewaehrleistet, dass der Thread nicht beendet wird.
	 */
	public void starten() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ProtokollThread), starten()");
		if (!running) {
			running = true;
			if (getState().equals(State.WAITING) || getState().equals(State.BLOCKED)) {
				//Main.debug.println(getClass()+"\n\tProtokollThread: Thread laeuft bereits.");
			}
			else {
				start();
			}
		}
	}

	/** Methode zum Beenden des Threads. Wenn der Thread noch in einem
	 * wartenden oder blockierten Zustand ist, wird interrupt() aufgerufen,
	 * um die Verarbeitung fortzusetzen, damit der Thread dann beendet
	 * werden kann.
	 */
	public void beenden() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ProtokollThread), beenden()");
		running = false;
		if (getState().equals(State.WAITING) || getState().equals(State.BLOCKED)) {
			interrupt();
		}
		if (this.puffer != null) this.puffer.clear();
	}
}
