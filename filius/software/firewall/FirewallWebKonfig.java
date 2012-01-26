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
package filius.software.firewall;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.Vector;

import filius.Main;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.Information;
import filius.software.www.WebServer;
import filius.software.www.WebServerPlugIn;


public class FirewallWebKonfig extends WebServerPlugIn{


	private WebServer webserver;
	private Firewall firewall;

	public void setFirewall(Firewall firewall){
		this.firewall = firewall;
	}

	public Firewall getFirewall() {
		return firewall;
	}

	public void setWebserver(WebServer server) {
		this.webserver = server;
	}

	public WebServer getWebserver() {
		return webserver;
	}

	/**
	 * Wird ueber das Interface WebServerPlugIn aufgerufen.
	 * Muss den gelieferten DatenString verarbeteiten, die Firewall bestuecken, und anschließend
	 * eine HTML-Seite zurueckliefern
	 */
	public String holeHtmlSeite(String postDaten){
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (FirewallWebKonfig), holeHtmlSeite("+postDaten+")");
		String seite = "";
		if(postDaten != null && !postDaten.equals("")){
			firewallBestuecken(postDaten);  //Dort wird die Methode postStringZerlegen() ausgefuehrt
		}
		//Main.debug.println("FirewallWebKonfig: Seite liefern= \n"+seite);
		seite = konfigSeiteErstellen();
		return seite;
	}

	/*
	 * @author weyer
	 * liefert zu einem ausgeführten Submit-Befehl die einzelnen Stücke zurück
	 *
	 */
	private String[][] postStringZerlegen(String post){
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (FirewallWebKonfig), postStringZerlegen("+post+")");

		String [] submitTeile;
		String[] element, tmp;
		String[][] tupel;

			//Main.debug.println("String mit submit in FirewallWebKonfig angekommen: "+post);
			//String zerlegen und überprüfen:
			try {
	            submitTeile = URLDecoder.decode(post, "UTF-8").split("&");
            } catch (UnsupportedEncodingException e) {
            	submitTeile = post.split("&");
            }

			//Die ersten 5 Einträge des Arrays sind immer gleich

			tupel = new String[submitTeile.length][2];
			for(int i=0; i<submitTeile.length; i++){
				tmp = submitTeile[i].split("=");
				element = new String[]{"",""};
				for (int j=0; j<tmp.length && j<element.length; j++) {
					element[j] = tmp[j].trim();
				}
				tupel[i] = element;
			}

			return tupel;
	}

	/**
	 *
	 * @param zeilen
	 */
	private void firewallBestuecken(String postTeil) {
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass()
		        + " (FirewallWebKonfig), firewallBestuecken(" + postTeil + ")");

		int regel;
		String empfaengerUnten = null, empfaengerOben = null;
		String absenderUnten = null, absenderOben = null;

		// Firewall bestücken:
		if (webserver.getSystemSoftware() != null) {

			// Main.debug.println("POST-String wird zerlegt");
			String[][] fwAttribute = postStringZerlegen(postTeil);
			Vector<Integer> activeNics = new Vector<Integer>();
			for (int i = 0; i < fwAttribute.length; i++) {
				// Main.debug.println("\t"+fwAttribute[i][0]+"="+fwAttribute[i][1]);
				if (fwAttribute[i][0].equals("nic")) {
					activeNics.add(Integer.parseInt(fwAttribute[i][1]));
				}

				else if (fwAttribute[i][0].equals("empfaenger_regel_loeschen")) {
					// Main.debug.println("\t\tempfaenger_regel_loeschen");
					try {
						regel = Integer.parseInt(fwAttribute[i][1]) - 1;
						firewall.entferneEmpfaengerRegel(regel);
					} catch (Exception e) {
					}
				}

				else if (fwAttribute[i][0].equals("absender_regel_loeschen")) {
					// Main.debug.println("\t\tabsender_regel_loeschen");
					try {
						regel = Integer.parseInt(fwAttribute[i][1]) - 1;
						firewall.entferneAbsenderRegel(regel);
					} catch (Exception e) {
					}
				}

				else if (fwAttribute[i][0].equals("port_regel_loeschen")) {
					// Main.debug.println("\t\tport_regel_loeschen");
					try {
						regel = Integer.parseInt(fwAttribute[i][1]) - 1;
						firewall.entferneRegelPort(regel);
					} catch (Exception e) {
					}
				}

				else if (fwAttribute[i][0].equals("empfaenger_untere_grenze")) {
					// Main.debug.println("\t\tempfaenger_untere_grenze");
					empfaengerUnten = fwAttribute[i][1];
				} else if (fwAttribute[i][0].equals("empfaenger_obere_grenze")) {
					// Main.debug.println("\t\tempfaenger_obere_grenze");
					empfaengerOben = fwAttribute[i][1];
				} else if (fwAttribute[i][0].equals("absender_untere_grenze")) {
					// Main.debug.println("\t\tabsender_untere_grenze");
					absenderUnten = fwAttribute[i][1];
				} else if (fwAttribute[i][0].equals("absender_obere_grenze")) {
					// Main.debug.println("\t\tabsender_obere_grenze");
					absenderOben = fwAttribute[i][1];
				} else if (fwAttribute[i][0].equals("port")) {
					// Main.debug.println("\t\tport");
					try {
						regel = Integer.parseInt(fwAttribute[i][1]);
						firewall.eintragHinzufuegenPort("" + regel);
					} catch (Exception e) {
					}
				}
			}

			if (empfaengerUnten != null && !empfaengerUnten.trim().equals("")) {
				if (empfaengerOben != null && !empfaengerOben.trim().equals("")) {
					firewall.eintragHinzufuegen(empfaengerUnten, empfaengerOben, Firewall.EMPFAENGER_FILTER);
				} else {
					firewall.eintragHinzufuegen(empfaengerUnten, empfaengerUnten, Firewall.EMPFAENGER_FILTER);
				}
			}
			if (absenderUnten != null && !absenderUnten.trim().equals("")) {
				if (absenderOben != null && !absenderOben.trim().equals("")) {
					firewall.eintragHinzufuegen(absenderUnten, absenderOben, Firewall.ABSENDER_FILTER);
				} else {
					firewall.eintragHinzufuegen(absenderUnten, absenderUnten, Firewall.ABSENDER_FILTER);
				}
			}

			LinkedList<NetzwerkInterface> allNics = ((InternetKnoten) this.getFirewall().getSystemSoftware()
			        .getKnoten()).getNetzwerkInterfaces();
			Vector<Integer> inactiveNics = new Vector<Integer>();
			for (int i = 0; i < allNics.size(); i++) {
				if (!activeNics.contains(new Integer(i))) {
					inactiveNics.add(new Integer(i));
				}
			}
			this.firewall.setInactiveNics(inactiveNics);
		}
	}

	private String erstelleZeilenEmpfaengerRegeln() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (FirewallWebKonfig), erstelleZeilenEmpfaengerRegeln()");
		LinkedList<String> regeln;
		String tmp;
		String [] ipArray;
		StringBuffer zeilen = new StringBuffer();

		try{
			regeln = firewall.getEmpfaengerFilterList();

			for(int i=0; i<regeln.size(); i++){
				tmp = (String)regeln.get(i);
				ipArray = tmp.split("#");

					zeilen.append("<tr><td>"+(i+1)+"</td><td>"
							+ipArray[0]+"</td><td>"+ipArray[1]+"</td></tr>");
			}
		}
		catch(Exception f){
			f.printStackTrace(Main.debug);
		}
		return zeilen.toString();
	}

	private String erstelleZeilenAbsenderRegeln() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (FirewallWebKonfig), erstelleZeilenAbsenderRegeln()");
		LinkedList<String> regeln;
		String tmp;
		String [] ipArray;
		StringBuffer zeilen = new StringBuffer();

		try{
			regeln = firewall.getAbsenderFilterList();

			for(int i=0; i<regeln.size(); i++){
				tmp = (String)regeln.get(i);
				ipArray = tmp.split("#");

					zeilen.append("<tr><td>"+(i+1)+"</td><td>"
							+ipArray[0]+"</td><td>"+ipArray[1]+"</td></tr>");
			}
		}
		catch(Exception f){
			f.printStackTrace(Main.debug);
		}
		return zeilen.toString();
	}

	private String erstelleZeilenPortRegeln() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (FirewallWebKonfig), erstelleZeilenPortRegeln()");
		LinkedList<Object[]> regeln;
		String tmp;
		StringBuffer zeilen = new StringBuffer();

		try{
			regeln = firewall.getPortList();

			for(int i=0; i<regeln.size(); i++){
				tmp = (String)((Object[])regeln.get(i))[0];

					zeilen.append("<tr><td>"+(i+1)+"</td><td>"+tmp+"</td></tr>");
			}
		}
		catch(Exception f){
			f.printStackTrace(Main.debug);
		}
		return zeilen.toString();
	}


	/*
	 * diese Seite erstellt den kompletten Quelltext für die konfig.html
	 */
	private String konfigSeiteErstellen(){
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (FirewallWebKonfig), konfigSeiteErstellen()");
		String html;

		//Main.debug.println("FirewallWebKonfig: dynamische Generierung der HTML-konfig-Seite!");

		if(firewall != null){

			try{

			html = textDateiEinlesen("config/firewall_konfig_webseite_"+Information.getInformation().getLocale().toString()+".txt");

			html = html.replaceAll(":action_pfad:", getPfad());

			LinkedList<NetzwerkInterface> allNics = ((InternetKnoten)this.getFirewall().getSystemSoftware().getKnoten()).getNetzwerkInterfaces();
			LinkedList<NetzwerkInterface> activeNics = firewall.holeNetzwerkInterfaces();
			StringBuffer nicSelectionHtml = new StringBuffer();
			int idx = 0;
			for (NetzwerkInterface nic : allNics) {
				nicSelectionHtml.append("\t\t<tr><td><input name=\"nic\" type=\"checkbox\" value=\""+(idx++)+"\" size=\"30\" maxlength=\"40\"");
				if (activeNics.contains(nic)) {
					nicSelectionHtml.append(" checked=\"checked\"");
				}
				nicSelectionHtml.append(" /></td><td>" + nic.getIp() + "</td></tr>");
				if (allNics.indexOf(nic) < allNics.size()-1) {
					nicSelectionHtml.append(" \n");
				}
			}
			html = html.replaceAll(":nic_activation:", nicSelectionHtml.toString());

			html = html.replaceAll(":empfaenger_regeln:", erstelleZeilenEmpfaengerRegeln());
			html = html.replaceAll(":absender_regeln:", erstelleZeilenAbsenderRegeln());
			html = html.replaceAll(":port_regeln:", erstelleZeilenPortRegeln());
		}
		catch(Exception f){
			f.printStackTrace(Main.debug);
			return null;
		}

		return html;
		}
		else {
			return null;
		}
	}

	/**
	 * liest eine reale Textdatei vom Format .txt ein. Diese befinden sich im
	 * Ordner  /config
	 */
	private String textDateiEinlesen(String datei) throws FileNotFoundException,
			IOException {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (FirewallWebKonfig), textDateiEinlesen("+datei+")");
		BufferedReader test = new BufferedReader(new FileReader(datei));
		String fullFile = "";
		String input = "";
		while ((input = test.readLine()) != null) {
			fullFile += input + "\n";
		}
		return fullFile;
	}
}
