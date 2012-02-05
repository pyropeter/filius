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
package filius.software.netzzugangsschicht;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import filius.Main;
import filius.hardware.knoten.Modem;
import filius.software.ProtokollThread;
import filius.software.system.ModemFirmware;

/**
 * Diese Klasse setzt die Beobachtung eines TCP/IP-Sockets um, die ein Modem mit
 * einem zweiten Modem verbindet und leitet die Frames an das angeschlossene
 * virtuelle Rechnernetz weiter.
 */
public class ModemAnschlussBeobachterExtern extends ProtokollThread {

	/**
	 * Die Modem-Firmware zur Verbindung von Rechnernetzen ueber eine
	 * TCP/IP-Verbindung
	 */
	private ModemFirmware firmware;

	/**
	 * Der Stream zur Uebertragung der Frames ueber den TCP/IP-Socket
	 */
	private InputStream in;

	private boolean neuverbinden;

	/**
	 * Konstruktor zur Initialisierung der Firmware und des Sockets. Der
	 * Konstruktor der Oberklasse wird <b>nicht</b> aufgerufen.
	 */
	public ModemAnschlussBeobachterExtern(ModemFirmware firmware, InputStream in) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ModemAnschlussBeobachterExtern), constr: ModemAnschlussBeobachterExtern("+firmware+","+in+")");
		this.firmware = firmware;
		this.in = in;
	}


	/**
	 * Die run()-Methode der Oberklasse muss ueberschrieben werden, weil hier
	 * kein Puffer als LinkedList sondern ein Socket bzw. Stream ueberwacht
	 * wird.
	 */
	public void run() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ModemAnschlussBeobachterExtern), run()");
		Object object;
		ObjectInputStream in = null;

		try {
			in = new ObjectInputStream(this.in);
		}
		catch (IOException e) {
			e.printStackTrace(Main.debug);
		}

		while (running) {
			try {
				object = in.readObject();
				verarbeiteDatenEinheit(object);
			}
			catch (Exception e) {
				e.printStackTrace(Main.debug);
				running = false;
				if (neuverbinden) {
					firmware.trennen();
					if (firmware.getMode()
							== firmware.SERVER) {
						firmware.starteServer();
					}
				} else {
					firmware.trennen();
				}
			}
		}
	}

	/**
	 * Hier werden ankommende Frames von einem Modem an das angeschlossene
	 * virtuelle Rechnernetz weitergegeben.
	 */
	protected void verarbeiteDatenEinheit(Object datenEinheit) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (ModemAnschlussBeobachterExtern), verarbeiteDatenEinheit("+datenEinheit.toString()+")");
		EthernetFrame frame;

		if (firmware.istGestartet()) {
			frame = (EthernetFrame) datenEinheit;

			synchronized (((Modem) firmware.getKnoten()).getErstenAnschluss().holeAusgangsPuffer()) {
				((Modem) firmware.getKnoten()).getErstenAnschluss().holeAusgangsPuffer().add(frame);
				((Modem) firmware.getKnoten()).getErstenAnschluss().holeAusgangsPuffer().notify();
			}
		}
	}

 	public void starten() {
 		neuverbinden = true;
 		super.starten();
 	}
 	public void beenden() {
 		neuverbinden = false;
 		super.beenden();
 	}
}
