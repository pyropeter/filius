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
package filius.software.vermittlungsschicht;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;

import filius.Main;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.I18n;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.vermittlungsschicht.IP;
import filius.software.rip.RIPTable;
import filius.software.rip.RIPRoute;

/**
 * Mit dieser Klasse wird die Weiterleitungstabelle implementiert. Es werden
 * manuell erstellte Eintraege und aus der IP-Konfiguration der Netzwerkkarten
 * des Knotens automatisch erzeugte Eintraege unterschieden. Gespeichert werden
 * nur manuelle Eintraege. Ausserdem werden bei jeder Abfrage die automatischen
 * Standard-Eintraege erzeugt.
 */
public class Weiterleitungstabelle implements I18n {

	/**
	 * Die Tabelle mit den manuellen Eintraegen der Weiterleitungstabelle. Sie
	 * werden als String-Arrays in einer Liste verwaltet. Ein Eintrag besteht
	 * aus folgenden Elementen:
	 * <ol>
	 * <li> Netz-ID der Zieladresse als IP-Adresse </li>
	 * <li> Netzmaske zur Berechnung der Netz-ID aus dem ersten Wert </li>
	 * <li> Das Standard-Gateway, ueber die die Ziel-IP-Adresse erreicht wird,
	 * wenn sie sich nicht im gleichen Rechnernetz wie der eigene Rechner
	 * befindet </li>
	 * <li> Die IP-Adresse der Netzwerkkarte, ueber die die Ziel-IP-Adresse
	 * erreicht wird </li>
	 * </ol>
	 */
	private LinkedList<String[]> manuelleTabelle;

	/**
	 * Eine Liste, in der angegeben wird, welche Eintraege in der erzeugten
	 * Tabelle automatisch erzeugt bzw. manuelle Eintraege sind
	 */
	private LinkedList<Boolean> manuelleEintraege;

	/** Die Systemsoftware */
	private InternetKnotenBetriebssystem firmware = null;

	/**
	 * Im Standard-Konstruktor wird die Methode reset() aufgerufen. Damit werden
	 * alle manuellen Eintraege geloescht
	 */
	public Weiterleitungstabelle() {
		reset();
	}

	/** Methode fuer den Zugriff auf die Systemsoftware */
	public void setInternetKnotenBetriebssystem(
			InternetKnotenBetriebssystem firmware) {
		this.firmware = firmware;
	}

	/** Methode fuer den Zugriff auf die Systemsoftware */
	public InternetKnotenBetriebssystem getInternetKnotenBetriebssystem() {
		return firmware;
	}

	/**
	 * Methode fuer den Zugriff auf die manuellen Eintrage. Diese Methode sollte
	 * nur fuer das speichern genutzt werden!
	 */
	public void setManuelleTabelle(LinkedList<String[]> tabelle) {
		this.manuelleTabelle = tabelle;
	}

	/**
	 * Methode fuer den Zugriff auf die manuellen Eintrage. Diese Methode sollte
	 * nur fuer das speichern genutzt werden!
	 */
	public LinkedList<String[]> getManuelleTabelle() {
		return manuelleTabelle;
	}

	/**
	 * Methode zum hinzufuegen eines neuen Eintrags. Ein Eintrag besteht aus
	 * folgenden Elementen:
	 * <ol>
	 * <li> Netz-ID der Zieladresse als IP-Adresse </li>
	 * <li> Netzmaske zur Berechnung der Netz-ID aus dem ersten Wert </li>
	 * <li> Das Standard-Gateway, ueber die die Ziel-IP-Adresse erreicht wird,
	 * wenn sie sich nicht im gleichen Rechnernetz wie der eigene Rechner
	 * befindet </li>
	 * <li> Die IP-Adresse der Netzwerkkarte, ueber die die Ziel-IP-Adresse
	 * erreicht wird </li>
	 * </ol>
	 *
	 * @param netzwerkziel
	 * @param netzwerkmaske
	 * @param gateway
	 * @param schnittstelle
	 */
	public void addManuellenEintrag(String netzwerkziel, String netzwerkmaske,
			String gateway, String schnittstelle) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Weiterleitungstabelle), addManuellenEintrag("+netzwerkziel+","+netzwerkmaske+","+gateway+","+schnittstelle+")");
		manuelleEintraege = null;

		if (netzwerkziel != null && netzwerkmaske != null && gateway != null
				&& schnittstelle != null) {
			String[] tmpString = { netzwerkziel, netzwerkmaske, gateway,
					schnittstelle };
			manuelleTabelle.addLast(tmpString);
		}

//		printTabelle(messages.getString("sw_weiterleitungstabelle_msg1"),
//				manuelleTabelle);
	}

	/**
	 * Hilfsmethode zum Debugging zur Ausgabe der Tabelleneintraege auf der
	 * Standardausgabe
	 *
	 * @param name
	 *            der Name, der in der Tabellenueberschrift ausgegeben werden
	 *            soll
	 * @param tabelle
	 *            die auszugebende Tabelle
	 */
	public void printTabelle(String name) {
//		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Weiterleitungstabelle), printTabelle("+name+","+tabelle+")");
		ListIterator it = holeTabelle().listIterator();
		String[] eintrag;

		Main.debug.println("DEBUG ("+name+") Weiterleitungstabelle (IP,mask,gw,if):");
		while (it.hasNext()) {
			eintrag = (String[]) it.next();
			Main.debug.printf("DEBUG (%s)  '%15s' | '%15s' | '%15s' | '%15s'\n", name, eintrag[0], eintrag[1], eintrag[2], eintrag[3]);
		}
	}

	/**
	 * Zugriff auf die Liste, in der steht, welche Eintraege automatisch erzeugt
	 * bzw. manuell erstellt wurden
	 */
	public LinkedList<Boolean> holeManuelleEintraegeFlags() {
		return manuelleEintraege;
	}

	/** Zuruecksetzen der Tabelle mit den manuellen Eintraegen */
	public void reset() {
		manuelleTabelle = new LinkedList<String[]>();
		manuelleEintraege = null;
	}

	/**
	 * Methode fuer den Zugriff auf die Weiterleitungstabelle bestehend aus
	 * automatisch erzeugten und manuellen Eintraegen
	 */
	public LinkedList<String[]> holeTabelle() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Weiterleitungstabelle), holeTabelle()");
		InternetKnoten knoten;
		NetzwerkInterface nic = null;
		String gateway;
		ListIterator it;
		LinkedList<String[]> tabelle;
		String[] tmp = new String[4];

		tabelle = new LinkedList<String[]>(manuelleTabelle);
		manuelleEintraege = new LinkedList<Boolean>();
		for (int i = 0; i < tabelle.size(); i++)
			manuelleEintraege.add(new Boolean(true));

		if (firmware != null) {
			// Eintrag fuer 'localhost'
			tmp = new String[4];
			tmp[0] = "127.0.0.0";
			tmp[1] = "255.0.0.0";
			tmp[2] = "127.0.0.1";
			tmp[3] = "127.0.0.1";
			tabelle.addFirst(tmp);
			manuelleEintraege.addFirst(new Boolean(false));

			knoten = (InternetKnoten) firmware.getKnoten();

			// Eintrag fuer eigenes Rechnernetz
			it = knoten.getNetzwerkInterfaces().listIterator();
			while (it.hasNext()) {
				nic = (NetzwerkInterface) it.next();

				tmp = new String[4];
				// tmp[0] = nic.getIp();
				tmp[0] = berechneNetzkennung(nic.getIp(), nic.getSubnetzMaske());
				tmp[1] = nic.getSubnetzMaske();
				tmp[2] = nic.getIp();
				tmp[3] = nic.getIp();
				tabelle.addFirst(tmp);
				manuelleEintraege.addFirst(new Boolean(false));
			}

			// Eintrag fuer eigene IP-Adresse
			it = knoten.getNetzwerkInterfaces().listIterator();
			while (it.hasNext()) {
				nic = (NetzwerkInterface) it.next();

				tmp = new String[4];
				tmp[0] = nic.getIp();
				tmp[1] = "255.255.255.255";
				tmp[2] = "127.0.0.1";
				tmp[3] = "127.0.0.1";
				tabelle.addFirst(tmp);
				manuelleEintraege.addFirst(new Boolean(false));
			}

			// Eintrag fuer Standardgateway, wenn es konfiguriert wurde
			gateway = firmware.getStandardGateway();
			if (gateway != null && !gateway.trim().equals("")) {
				gateway = gateway.trim();
				it = knoten.getNetzwerkInterfaces().listIterator();
				tmp = null;
				while (it.hasNext()) {
					nic = (NetzwerkInterface) it.next();

					if (nic != null
							&& VermittlungsProtokoll
									.gleichesRechnernetz(gateway, nic.getIp(),
											nic.getSubnetzMaske())) {
						tmp = new String[4];
						tmp[0] = "0.0.0.0";
						tmp[1] = "0.0.0.0";
						tmp[2] = gateway;
						tmp[3] = nic.getIp();
					}
				}
				if (tmp == null) {
					tmp = new String[4];
					tmp[0] = "0.0.0.0";
					tmp[1] = "0.0.0.0";
					tmp[2] = gateway;
					tmp[3] = firmware.holeIPAdresse();
				}
				tabelle.addLast(tmp);
				manuelleEintraege.addLast(new Boolean(false));
			}
		}

		return tabelle;
	}

	/**
	 * Methode, um aus einer IP-Adresse und einer Subnetzmaske eine
	 * Netzwerkkennung als String zu erzeugen. Bsp.: 192.168.2.6 und
	 * 255.255.255.0 wird zu 192.168.2.0
	 */
	private String berechneNetzkennung(String ipStr, String maskStr) {
		long ipAddr = IP.inetAton(ipStr);
		long maskAddr = IP.inetAton(maskStr);
		long netAddr = ipAddr & maskAddr;
		return IP.inetNtoa(netAddr);
	}

	/**
	 * Tabelle zur Abfrage der Weiterleitungstabelle nach einem passenden
	 * Eintrag fuer eine Ziel-IP-Adresse
	 *
	 * @param zielIP
	 *            die Ziel-IP-Adresse
	 * @return das Ergebnis als String-Array bestehend aus der IP-Adresse des
	 *         naechsten Gateways und der fuer den Versand zu verwendenden
	 *         Schnittstelle
	 */
	public String[] holeWeiterleitungsZiele(String zielStr) {
		RIPTable table = firmware.getRIPTable();
		if (table == null) {
			return holeStatisch(zielStr);
		} else {
			synchronized (table) {
				return holeDynamisch(table, zielStr);
			}
		}
	}

	public String[] holeStatisch(String zielStr) {
		long netAddr, maskAddr, zielAddr = IP.inetAton(zielStr);
		String[] route;

		long bestMask = -1;
		String[] bestRoute = null;
		
		ListIterator it = holeTabelle().listIterator();
		while (it.hasNext()) {
			route = (String[]) it.next();
			maskAddr = IP.inetAton(route[1]);
			if (maskAddr <= bestMask) {
				continue;
			}
			netAddr = IP.inetAton(route[0]);
			if (netAddr == (maskAddr & zielAddr)) {
				bestMask = maskAddr;
				bestRoute = new String[]{route[2], route[3]};
			}
		}
		return bestRoute;
	}

	public String[] holeDynamisch(RIPTable table, String ip) {
		// table must be synchronized by holeWeiterleitungsZiele

		String[] bestRoute = null;
		int bestHops = RIPTable.INFINITY - 1;
		long bestMask = -1;

		for (RIPRoute route : table.routes) {
			if (route.netAddr.equals(berechneNetzkennung(ip, route.netMask))) {
				if (bestHops < route.hops) {
					continue;
				}
				if (bestHops > route.hops
				|| bestMask < IP.inetAton(route.netMask)) {
					bestRoute = new String[]{route.nextHop, route.nic};
					bestHops = route.hops;
					bestMask = IP.inetAton(route.netMask);
				}
			}
		}
		return bestRoute;
	}
}
