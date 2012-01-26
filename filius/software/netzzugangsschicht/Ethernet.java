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

import java.util.LinkedList;
import java.util.ListIterator;

import filius.Main;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.nachrichten.Lauscher;
import filius.software.Protokoll;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.system.SystemSoftware;
import filius.software.vermittlungsschicht.ArpPaket;
import filius.software.vermittlungsschicht.IpPaket;
import filius.software.vermittlungsschicht.IcmpPaket;

/** Diese Klasse implementiert die Netzzugangsschicht */
public class Ethernet extends Protokoll {

	/**
	 * Liste der Threads fuer die Ueberwachung der Netzwerkkarten
	 */
	private LinkedList<EthernetThread> threads = new LinkedList<EthernetThread>();

	/** Puffer fuer eingehende ARP-Pakete */
	private LinkedList<ArpPaket> arpPakete = new LinkedList<ArpPaket>();

	/** Puffer fuer eingehende IP-Pakete */
	private LinkedList<IpPaket> ipPakete = new LinkedList<IpPaket>();

	/** Puffer fuer eingehende ICMP-Pakete */
	private LinkedList<IcmpPaket> icmpPakete = new LinkedList<IcmpPaket>();

	/** Konstruktor zur Initialisierung der Systemsoftware */
	public Ethernet(SystemSoftware systemSoftware) {
		super(systemSoftware);
		Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+" (Ethernet), constr: Ethernet("+systemSoftware+")");
	}

	/** Methode fuer den Zugriff auf den Puffer mit ARP-Paketen */
	public LinkedList<ArpPaket> holeARPPuffer() {
		return arpPakete;
	}

	/** Methode fuer den Zugriff auf den Puffer mit IP-Paketen */
	public LinkedList<IpPaket> holeIPPuffer() {
		return ipPakete;
	}

	/** Methode fuer den Zugriff auf den Puffer mit ICMP-Paketen */
	public LinkedList<IcmpPaket> holeICMPPuffer() {
		return icmpPakete;
	}

	/** Methode fuer den Zugriff auf den Puffer mit IP-Paketen */
	public void setzeIPPuffer(LinkedList<IpPaket> puffer) {
		ipPakete = puffer;
	}

	/**
	 * sendet Pakete ueber als Ethernet-Frame weiter. Zuerst wird dazu
	 * ueberprueft, ob die Ziel-MAC-Adresse eine eigene Netzwerkkarte
	 * adressiert. Wenn dies nicht der Fall ist, wird der Frame ueber die
	 * Netzwerkkarte verschickt, die durch die Quell-MAC-Adresse spezifiziert
	 * wird.
	 */
	public void senden(Object daten, String startMAC, String zielMAC, String typ) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Ethernet), senden("+daten+","+startMAC+","+zielMAC+","+typ+")");
		EthernetFrame ethernetFrame;
		ListIterator it;
		NetzwerkInterface nic;
		boolean gesendet = false;

		if(daten instanceof IcmpPaket) {
			ethernetFrame = new EthernetFrame(daten, startMAC, zielMAC, typ, true);
		}
		else {
			ethernetFrame = new EthernetFrame(daten, startMAC, zielMAC, typ);
		}

		it = ((InternetKnoten) holeSystemSoftware().getKnoten())
				.getNetzwerkInterfaces().listIterator();
		while (it.hasNext()) {
			nic = (NetzwerkInterface) it.next();

			if (nic.getMac().equalsIgnoreCase(zielMAC)) {
				synchronized (nic.getPort().holeEingangsPuffer()) {
					nic.getPort().holeEingangsPuffer().add(ethernetFrame);
					nic.getPort().holeEingangsPuffer().notify();
				}
				gesendet = true;
			}
		}

		if (!gesendet) {
			it = ((InternetKnoten) holeSystemSoftware().getKnoten())
					.getNetzwerkInterfaces().listIterator();
			while (it.hasNext()) {
				nic = (NetzwerkInterface) it.next();

				if (nic.getMac().equalsIgnoreCase(startMAC)) {
					synchronized (nic.getPort().holeAusgangsPuffer()) {
						//Main.debug
								//.println("EthernetThread: Paket wird in Ausgangspuffer geschrieben "
										//+ nic.getPort());
						nic.getPort().holeAusgangsPuffer().add(ethernetFrame);
						nic.getPort().holeAusgangsPuffer().notify();
					}
					Lauscher.getLauscher().addDatenEinheit(nic.getMac(),
							ethernetFrame);
				}
			}
		}
	}

	public LinkedList<EthernetThread> getEthernetThreads() {
		return threads;
	}
	
	/**
	 * Hier wird zu jeder Netzwerkkarte ein Thread zur Ueberwachung des
	 * Eingangspuffers gestartet.
	 */
	public void starten() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Ethernet), starten()");
		InternetKnoten knoten;
		EthernetThread interfaceBeobachter;

		if (holeSystemSoftware().getKnoten() instanceof InternetKnoten) {
			knoten = (InternetKnoten) holeSystemSoftware().getKnoten();

			ListIterator iter = knoten.getNetzwerkInterfaces().listIterator();
			while (iter.hasNext()) {
				NetzwerkInterface netzwerkInterface = (NetzwerkInterface) iter
						.next();

				interfaceBeobachter = new EthernetThread(this,
						netzwerkInterface);

				interfaceBeobachter.starten();
				try {
					threads.add(interfaceBeobachter);
				}
				catch (Exception e) {
					e.printStackTrace(Main.debug);
				}
			}
		}
	}

	/**
	 * beendet alle laufenden EthernetThreads zur Ueberwachung der
	 * Eingangspuffer der Netzwerkkarten
	 */
	public void beenden() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Ethernet), beenden()");
		EthernetThread interfaceBeobachter;

		for (int x = 0; x < threads.size(); x++) {
			interfaceBeobachter = (EthernetThread) threads.get(x);
			interfaceBeobachter.beenden();
		}
	}
}
