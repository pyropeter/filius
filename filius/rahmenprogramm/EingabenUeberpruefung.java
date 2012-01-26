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

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import filius.Main;

/**
 *
 * Dient dazu Eingaben auf Richtigkeit zu Pruefen.
 * Dazu stehen verschiedene Pattern zur Verfuegung, z.B. fuer IP-Adressen oder Klassen-Namen.
 * Mit diesen Pattern wird die Funktion isGueltig aufgerufen, die den String auf ein Muster prueft.
 *
 * @author Johannes Bade
 *
 */
public class EingabenUeberpruefung implements I18n {

	// NOTE: include *.*.*.0 to be able to still use this pattern for routing table configuration as network identifier
	public static final Pattern musterIpAdresse = Pattern.compile("(0*([1-9][0-9]?|1[0-9]{1,2}|2[0-4]?[0-9]|25[0-5]))\\.((0*(1?[0-9]{1,2}|2[0-4]?[0-9]|25[0-5]))\\.){2}(0*(1?[0-9]{1,2}|2[0-4]?[0-9]|25[0-5]))");
	public static final Pattern musterIpAdresseAuchLeer = Pattern.compile("((0*([1-9][0-9]?|1[0-9]{1,2}|2[0-4]?[0-9]|25[0-5]))\\.((0*(1?[0-9]{1,2}|2[0-4]?[0-9]|25[0-5]))\\.){2}(0*(1?[0-9]{1,2}|2[0-4]?[0-9]|25[0-5]))){0,1}");
	
	public static final Pattern musterSubNetz = Pattern.compile("(0*([1-9]|1?[0-9]{1,2}|2[0-4]?[0-9]|25[0-5]))\\.((0*([0-9]|1?[0-9]{1,2}|2[0-4]?[0-9]|25[0-5]))\\.){2}(0*([0-9]|1?[0-9]{1,2}|2[0-4]?[0-9]|25[0-5]))");
//	public static final Pattern musterEmailAdresse = Pattern.compile("^[a-zA-Z0-9\\-_][a-zA-Z0-9\\-_\\.]*[a-zA-Z0-9\\-_]@[a-zA-Z0-9][a-zA-Z0-9\\-_]*(\\.[a-zA-Z0-9][a-zA-Z0-9\\-_]*)*$");
	public static final Pattern musterEmailAdresse = Pattern.compile(
			"^"+   // beginning of line
			"("+   // start choice
			"[a-z A-Z0-9\\-_\\.]* <"+  // address with name, beginning; Name part
			"[a-zA-Z0-9\\-_][a-zA-Z0-9\\-_\\.]*[a-zA-Z0-9\\-_]@[a-zA-Z0-9][a-zA-Z0-9\\-_]*(\\.[a-zA-Z0-9][a-zA-Z0-9\\-_]*)*"+  // standard mail address
			">"+   // address with name, end
			"|"+   // choice select
			"[a-zA-Z0-9\\-_][a-zA-Z0-9\\-_\\.]*[a-zA-Z0-9\\-_]@[a-zA-Z0-9][a-zA-Z0-9\\-_]*(\\.[a-zA-Z0-9][a-zA-Z0-9\\-_]*)*"+  // standard mail address
			")"+   // end choice
//			"(, "+ // boundary for multiple mails
//			"("+   // start choice
//			"[a-z A-Z0-9\\-_\\.]* <"+  // address with name, beginning; Name part
//			"[a-zA-Z0-9\\-_][a-zA-Z0-9\\-_\\.]*[a-zA-Z0-9\\-_]@[a-zA-Z0-9][a-zA-Z0-9\\-_]*(\\.[a-zA-Z0-9][a-zA-Z0-9\\-_]*)*"+  // standard mail address
//			">"+   // address with name, end
//			"|"+   // choice select
//			"[a-zA-Z0-9\\-_][a-zA-Z0-9\\-_\\.]*[a-zA-Z0-9\\-_]@[a-zA-Z0-9][a-zA-Z0-9\\-_]*(\\.[a-zA-Z0-9][a-zA-Z0-9\\-_]*)*"+  // standard mail address
//			")"+   // end choice
//			")"+   // end boundary
			"$");
	public static final Pattern musterEmailAdresseExt = Pattern.compile("^[a-z A-Z0-9\\-_\\.]* <[a-zA-Z0-9\\-_][a-zA-Z0-9\\-_\\.]*[a-zA-Z0-9\\-_]@[a-zA-Z0-9][a-zA-Z0-9\\-_]*(\\.[a-zA-Z0-9][a-zA-Z0-9\\-_]*)*>$");
	public static final Pattern musterKlassenName = Pattern.compile("[A-Z]([a-zA-Z]{2,})?");
	public static final Pattern musterPort = Pattern.compile("([1-9]{1,4}|[1-5][0-9]{1,4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])");
	public static final Pattern musterKeineLeerzeichen = Pattern.compile("[^\\s]*");
	public static final Pattern musterEmailBenutzername = Pattern.compile("([a-zA-Z0-9]|\\.|\\_|\\-)*");
	public static final Pattern musterMindEinZeichen = Pattern.compile("(.){1,}");
	public static final Pattern musterNurZahlen = Pattern.compile("\\d");
	//public static final Pattern musterDomain = Pattern.compile("([a-zA-Z0-9]|\\.){2,}");
	public static final Pattern musterDomain = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9\\-_]*(\\.[a-zA-Z0-9][a-zA-Z0-9\\-_]*)*");


	public static final Color farbeFalsch = new Color(255,20,20);
	public static final Color farbeRichtig = new Color(0,0,0);

	/**
	 * Die Funktion isGueltig bekommt einen String und ein Muster
	 * (Vorzugsweise ein Muster, dass in der Klasse EingabenUeberpruefung vorgegeben ist)
	 * Moeglicher Aufruf: isGueltig(ipAdresse, EingabeUeberpruefung.musterIpAdresse)
	 *
	 * @param zuPruefen
	 * @param muster
	 * @return
	 */
	public static boolean isGueltig(String zuPruefen, Pattern muster)
	{
		Matcher m = muster.matcher(zuPruefen);
		return m.matches();
	}

}

