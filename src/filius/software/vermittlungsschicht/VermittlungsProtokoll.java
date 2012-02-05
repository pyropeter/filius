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

import java.util.StringTokenizer;
import java.util.ListIterator;

import filius.Main;
import filius.software.Protokoll;
import filius.software.system.SystemSoftware;
import filius.hardware.knoten.InternetKnoten;
import filius.hardware.NetzwerkInterface;

/** Oberklasse von ARP und IP mit Hilfsmethoden */
public abstract class VermittlungsProtokoll extends Protokoll {

	/** Standard-Konstruktor der Oberklasse Protokoll */
	public VermittlungsProtokoll(SystemSoftware systemSoftware) {
		super(systemSoftware);
		Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+" (VermittlungsProtokoll), constr: VermittlungsProtokoll("+systemSoftware+")");
	}

	/**
	 * Methode zum pruefen, ob sich zwei IP-Adressen im gleichen Rechnernetz
	 * befinden. Dazu wird die Netzmaske benoetigt:
	 * <ol>
	 * <li> Bitweise ODER-Verknuepfung der ersten IP-Adresse und der Netzmaske
	 * </li>
	 * <li> Bitweise ODER-Verknuepfung der zweiten IP-Adresse und der Netzmaske
	 * </li>
	 * <li> Vergleich der zwei Netz-IDs, die in den vorangegangenen Schritten
	 * berechnet wurden </li>
	 * </ol>
	 *
	 * @param adresseEins
	 *            erste IP-Adresse als String
	 * @param adresseZwei
	 *            zweite IP-Adresse als String
	 * @param netzmaske
	 *            Netzmaske als String
	 * @return ob die Netz-IDs der zwei Adressen uebereinstimmen
	 */
	public static boolean gleichesRechnernetz(String adresseEins, String adresseZwei,
			String netzmaske) {
		Main.debug.println("INVOKED (static) filius.software.vermittlungsschicht.VermittlungsProtokoll, gleichesRechnernetz("+adresseEins+","+adresseZwei+","+netzmaske+")");
		int[] a1, a2, m;
		StringTokenizer tokenizer;
		boolean gleichesRechnernetz = true;

		tokenizer = new StringTokenizer(adresseEins, ".");
		a1 = new int[4];
		for (int i = 0; i < a1.length && tokenizer.hasMoreTokens(); i++) {
			a1[i] = Integer.parseInt(tokenizer.nextToken());
		}
		tokenizer = new StringTokenizer(adresseZwei, ".");
		a2 = new int[4];
		for (int i = 0; i < a2.length && tokenizer.hasMoreTokens(); i++) {
			a2[i] = Integer.parseInt(tokenizer.nextToken());
		}
		tokenizer = new StringTokenizer(netzmaske, ".");
		m = new int[4];
		for (int i = 0; i < m.length && tokenizer.hasMoreTokens(); i++) {
			m[i] = Integer.parseInt(tokenizer.nextToken());
		}

		for (int i = 0; i < 4 && gleichesRechnernetz; i++) {
			if ((a1[i] & m[i]) == (a2[i] & m[i])) gleichesRechnernetz = true;
			else gleichesRechnernetz = false;
		}

		return gleichesRechnernetz;
	}

	public boolean isLocal(String ip) {
		if (gleichesRechnernetz(ip, "127.0.0.0", "255.0.0.0")) {
			return true;
		}

		InternetKnoten knoten = (InternetKnoten) holeSystemSoftware().getKnoten();
		ListIterator it = knoten.getNetzwerkInterfaces().listIterator();
		NetzwerkInterface nic;
		while (it.hasNext()) {
			nic = (NetzwerkInterface) it.next();
			if (ip.equals(nic.getIp())) {
				return true;
			}
		}
		return false;
	}
}
