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
package filius.software.transportschicht;

import java.util.LinkedList;

import filius.Main;
import filius.exception.SocketException;
import filius.exception.VerbindungsException;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.vermittlungsschicht.IpPaket;

/**
 * Der UDP-Socket stellt die Schnittstelle zum Versenden von Segmenten zur
 * Verfuegung, ohne eine Verbindung herzustellen. Ausserdem wird jede Nachricht
 * in genau einem Segment uebertragen.
 */
public class UDPSocket extends Socket {

	/** Liste eingehender Segmente */
	private LinkedList<UdpSegment> puffer = new LinkedList<UdpSegment>();

	/**
	 * Ob der Socket verbunden ist, d. h. Aufruf von verbinden() war erfolgreich
	 * und Socket wurde noch nicht durch schliessen() geschlossen.
	 */
	private boolean verbunden = false;

	/**
	 * Konstruktor zur Initialisierung des Sockets. Dazu wird das mit dem Socket
	 * verbundene Transport-Protokoll initialisiert und ein beliebiger Port wird
	 * reserviert.
	 *
	 * @param betriebssystem
	 * @param zielAdresse
	 * @param zielPort
	 * @throws VerbindungsException
	 */
	public UDPSocket(InternetKnotenBetriebssystem betriebssystem,
			String zielAdresse, int zielPort) throws VerbindungsException {
		super(betriebssystem, zielAdresse, zielPort, IpPaket.UDP);
		Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+" (UDPSocket), constr: UDPSocket("+betriebssystem+","+zielAdresse+","+zielPort+")");
	}

	/**
	 * Konstruktor zur Initialisierung des Sockets. Dazu wird das mit dem Socket
	 * verbundene Transport-Protokoll initialisiert und ein bestimmter Port wird
	 * reserviert.
	 *
	 * @param betriebssystem
	 * @param zielAdresse
	 * @param zielPort
	 * @param lokalerPort
	 *            ein bestimmter lokaler Port, der beim Betriebssystem
	 *            reserviert werden soll. Dieser Parameter wird nur dann
	 *            verwendet, wenn der Wert groesser 0 ist.
	 * @throws VerbindungsException
	 */
	public UDPSocket(InternetKnotenBetriebssystem betriebssystem,
			String zielAdresse, int zielPort, int lokalerPort)
			throws VerbindungsException {
		super(betriebssystem, zielAdresse, zielPort, IpPaket.UDP, lokalerPort);
		Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+" (UDPSocket), constr: UDPSocket("+betriebssystem+","+zielAdresse+","+zielPort+","+lokalerPort+")");
	}

	/**
	 * Konstruktor zur Initialisierung eines Sockets mit Belegung eines
	 * bestimmten lokalen Ports aber ohne eine feste Ziel-IP-Adresse. Diese wird
	 * beim Empfang eines Segments bestimmt.
	 *
	 * @param betriebssystem
	 * @param lokalerPort
	 * @param zielAdresse
	 * @param zielPort
	 * @throws VerbindungsException
	 */
	public UDPSocket(InternetKnotenBetriebssystem betriebssystem,
			int lokalerPort) throws VerbindungsException {
		super(betriebssystem, lokalerPort, IpPaket.UDP);
		Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+" (UDPSocket), constr: UDPSocket("+betriebssystem+","+lokalerPort+")");
	}

	/**
	 * Pakete fuer diesen Socket werden ueber diese Funktion in den Puffer
	 * geschrieben.
	 *
	 * @author carsten
	 */
	public void hinzufuegen(String startIp, int startPort, Object segment) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (UDPSocket), hinzufuegen("+startIp+","+startPort+","+segment+")");
		zielIp = startIp;
		zielPort = startPort;

		//Main.debug.println(getClass().toString()
				//+ "\n\thinzufuegen() wurde aufgerufen"
				//+ "\n\tAbsender-Adresse: " + startIp + ":" + startPort
				//+ "\n\tDaten: " + segment.toString());

		synchronized (puffer) {
			puffer.add((UdpSegment) segment);
			puffer.notifyAll();
		}
	}

	/**
	 * Methode zum Empfang einer Nachricht (entspricht in dieser Implementierung
	 * genau einem UDP-Segment) mit einem Timeout. Wenn der Timeout vor
	 * eintreffen eines Datagramms auftritt, ist der Rueckgabewert 'null'.
	 *
	 * @return gibt den empfangenen Datenstring zurueck.
	 */
	public synchronized String empfangen(long millis) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (UDPSocket), empfangen("+millis+")");
		UdpSegment segment;

		synchronized (puffer) {
			if (puffer.size() < 1) {
				try {
					puffer.wait(millis);
				}
				catch (InterruptedException e) {
					e.printStackTrace(Main.debug);
				}
			}
			if (puffer.size() >= 1) {
				segment = (UdpSegment) puffer.removeFirst();
				return segment.getDaten();
			}
			else {
				return null;
			}
		}
	}

	/**
	 * Methode zum Empfang einer Nachricht (entspricht in dieser Implementierung
	 * genau einem UDP-Segment)
	 *
	 * @return gibt den empfangenen Datenstring zurueck.
	 */
	public synchronized String empfangen() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (UDPSocket), empfangen()");
		return empfangen(0);
	}

	/** Methode zum Senden einer Nachricht ueber UDP */
	public synchronized void senden(String nachricht) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (UDPSocket), senden("+nachricht+")");
		UdpSegment segment;

		segment = new UdpSegment();
		segment.setDaten(nachricht);
		segment.setQuellPort(lokalerPort);
		segment.setZielPort(zielPort);

		super.sende(segment);
	}

	/** Methode zum senden eines Broadcast Datagramms mit UDP */
	public synchronized void sendeBroadcast(String nachricht) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (UDPSocket), sendeBroadcast("+nachricht+")");
		UdpSegment segment;

		segment = new UdpSegment();
		segment.setDaten(nachricht);
		segment.setQuellPort(lokalerPort);
		segment.setZielPort(zielPort);

		protokoll.senden("255.255.255.255", segment);
	}

	/**
	 * Methode zum Schliessen eines Sockets. Der Port wird wieder freigegeben!
	 */
	public void schliessen() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (UDPSocket), schliessen()");
		synchronized (puffer) {
			puffer.notifyAll();
		}
		austragenPort();
		verbunden = false;
	}

	/**
	 * Methode zum unterbrechen eines Methodenaufrufs zum empfangen eines
	 * Datagramms. Diese Methode wird beim Wechsel vom Aktions- zum
	 * Entwurfsmodus aufgerufen.
	 *
	 */
	public void beenden() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (UDPSocket), beenden()");
		synchronized (puffer) {
			puffer.notifyAll();
		}
	}

	public void verbinden() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (UDPSocket), verbinden()");

		if (!verbunden) {
			if (modus == PASSIV) {
				synchronized (puffer) {
					try {
						puffer.wait();
					}
					catch (InterruptedException e) {
					}
				}
			}
			
			try {
				eintragenPort();

				verbunden = true;
			}
			catch (SocketException e) {
				Main.debug.println("EXCEPTION ("+this.hashCode()+"): verbinden() NICHT erfolgreich");
				e.printStackTrace(Main.debug);
			}
		}
	}

	public boolean istVerbunden() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (UDPSocket), istVerbunden()");
		return verbunden;
	}
	
	public String getStateAsString() {
		if (this.istVerbunden()) {
			return "CONNECTED";
		}
		else {
			return "DISCONNECTED";
		}
	}

}
