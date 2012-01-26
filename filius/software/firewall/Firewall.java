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
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

import filius.Main;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.rahmenprogramm.I18n;
import filius.software.Anwendung;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.transportschicht.Segment;
import filius.software.transportschicht.SocketSchnittstelle;
import filius.software.transportschicht.TCPSocket;
import filius.software.transportschicht.TcpSegment;
import filius.software.transportschicht.UDPSocket;
import filius.software.vermittlungsschicht.IpPaket;
import filius.software.vermittlungsschicht.VermittlungsProtokoll;


/**
 * Die Firewall kann in zwei verschiedenen Modi betrieben werden.
 * <p> Als <b>Personal Firewall</b> werden lediglich Port-Regeln ausgewertet.
 * Eine Port-Regel spezifiziert zugelassene TCP-/UDP-Ports und ob
 * diese nur von IP-Adressen im lokalen Rechnernetz oder global
 * kontaktiert werden koennen. </p>
 * <p> Wenn die Firewall in einem <b>Gateway</b> betrieben wird, gibt es
 * vier verschiedene Regeltypen. Alle Regeln spezifizieren - im Gegensatz
 * zum Betrieb als Personal Firewall - Dateneinheiten, die nicht zugelassen
 * werden. Geprueft werden:
 * <ol>
 * <li> Sender-IP-Adresse </li>
 * <li> Absender-IP-Adresse </li>
 * <li> TCP-/UDP-Port </li>
 * <li> ACK(=0)+SYN(=1)-Bit der TCP-Segmente (indiziert Initialisierung
 * des Verbindungsaufbaus)</li>
 * </ol>
 */
public class Firewall extends Anwendung implements I18n {

	public static int PERSONAL = 1, GATEWAY = 2;

	public static String ABSENDER_FILTER = "Quelle", EMPFAENGER_FILTER = "Ziel";

	private boolean aktiviert = true;
	private int bitRegel = 0;
	private LinkedList<String> absenderFilter, empfaengerFilter;
	/** Liste mit Portregeln besetehen aus dem jeweiligen TCP/UDP-Port
	 * und einem Flag, das angibt, ob diese Ausschlussregel nur auf IP-Pakete
	 * aller Absender (oder nur auf Absender ausserhalb des eigenen lokalen
	 * Rechnernetzes) angewendet wird. Das Flag wird nur im Betrieb als Personal
	 * Firewall ausgewertet!
	 */
	private LinkedList<Object[]> portList;

	/** Das Verhalten der Firewall ist abhaengig davon, ob sie
	 * als Personal Firewall oder als Gateway benutzt wird.
	 */
	private int modus;
	private LinkedList<FirewallThread> threads = new LinkedList<FirewallThread>();
	
	private Vector<Integer> inactiveNics = new Vector<Integer>();


	/** Konstruktor initialisiert Listen mit Regeln für die Firewall. Außerdem setzt ein
	Firewall-Beobachter Nachrichten ins Logfenster
	*/
	public Firewall(){
		super();
		Main.debug.println("INVOKED-2 ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Firewall), constr: Firewall()");

		setModus(PERSONAL);

		absenderFilter = new LinkedList<String>();
		empfaengerFilter = new LinkedList<String>();
		portList = new LinkedList<Object[]>();
	}


	/** startet die Anwendung Firewall.
	 */
	public void starten(){
		if (this.holeNetzwerkInterfaces() != null) {
			Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
			        + " (Firewall), starten()");
			super.starten();

			for (NetzwerkInterface nic : this.holeNetzwerkInterfaces()) {
				this.starteFirewallThread(nic);
			}
		}
	}
	
	private void starteFirewallThread(NetzwerkInterface nic) {
		FirewallThread thread = new FirewallThread(this, nic);
		thread.starten();
		this.threads.add(thread);
	}


	/**
	 * ruft die Methoden zum ordnungsgemäßen Stoppen aller existierenden Threads auf
	 */
	public void beenden() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Firewall), beenden()");
		super.beenden();

		this.beendeFirewallThread(null);
	}
	
	private void beendeFirewallThread(NetzwerkInterface nic) {
		for (FirewallThread thread : this.threads) {
			if (nic == null) {
				thread.beenden();
			}
			else if (nic == thread.getNetzwerkInterface()) {
				thread.beenden();
				break;
			}
		}
	}


	/**
	 * <p> Untersucht im <b>Modus Gateway</b> das uebergebene IP-Paket nach
	 * einer Uebereinstimmung mit einer Regel, die gefiltert werden muss.
	 * Wenn das zutrifft wird der Rueckgabewert auf true gesetzt. </p>
	 * <p> Im <b>Modus PERSONAL</b> wird zunaechst geprueft, ob das Paket
	 * einer Ausnahmeregelung entspricht und damit weitergegeben werden kann.
	 * Wenn dies nicht der Fall ist, wird noch geprueft, ob das enthaltene
	 * Segment an einen geoeffnet Client-Socket gerichtet ist. Dann wird es
	 * dennoch weitergeleitet. </p>
	 */
	public boolean pruefePaketVerwerfen(IpPaket ipPaket){
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Firewall), pruefePaketVerwerfen("+ipPaket+")");
		Segment segment;
		InternetKnoten knoten;
		SocketSchnittstelle socket;

		boolean verwerfen = false;

//		Nur wenn Firewall eingeschaltet ist, passiert was:
		if(isAktiviert()){
			Main.debug.println("INFO ("+this.hashCode()+"): Firewall ist eingeschaltet. Pruefung beginnt!");

			if (modus == PERSONAL) {
				segment = (Segment)ipPaket.getSegment();

				// gibt es eine Regel, die diesen Port als Ausnahme
				// fuer den Datenaustausch zulaesst?
				if (!pruefePortOffen(""+segment.getZielPort(), ipPaket.getSender())) {
					verwerfen = true;

					// ist die Zieladresse eine Adresse dieses Knotens
					// und der Zielport ein Client-Socket?
					// -> dann wird das Segment trotzdem weitergeleitet
					knoten = (InternetKnoten)getSystemSoftware().getKnoten();
					if(knoten.getNetzwerkInterfaceByIp(ipPaket.getEmpfaenger()) != null) {
					  if (ipPaket.getProtocol() == IpPaket.TCP) {
						  try {
							  socket = getSystemSoftware().holeTcp().holeSocket(segment.getZielPort());

							  if (socket instanceof TCPSocket) {
								  verwerfen = false;
							  }
						  }
						  catch (Exception e) {}
					  }
					  else if (ipPaket.getProtocol() == IpPaket.UDP) {
						  try {
							  socket = getSystemSoftware().holeUdp().holeSocket(segment.getZielPort());

							  if (socket instanceof UDPSocket) {
								  verwerfen = false;
							  }
						  }
						  catch (Exception e) {}
					  }
					}
				}
			}
			else {
			//Pruefen von Ip-Adressen:
				if(pruefeIPEmpfaenger(ipPaket.getEmpfaenger())){
					verwerfen = true;
					benachrichtigeBeobachter(messages.getString("sw_firewall_msg1")+verwerfen);
				}
				if(pruefeIPSender(ipPaket.getSender())){
					verwerfen = true;
				}

			//Port untersuchen:
			segment =  (Segment)ipPaket.getSegment();

				//Main.debug.println("Firewall: Filtern nach Ziel-Port: "+ segment.getZielPort());
				if(pruefePortGeschlossen(Integer.toString(segment.getZielPort()))){
					verwerfen = true;
					benachrichtigeBeobachter(messages.getString("sw_firewall_msg2")+ Integer.toString(segment.getQuellPort()));
				}

			//Syn-Ack-Bit prüfen:
				if(getBitRegel() == 1 && segment instanceof TcpSegment){
					if(((TcpSegment)segment).isSyn()&& !((TcpSegment)segment).isAck()){
						verwerfen = true;
						benachrichtigeBeobachter(messages.getString("sw_firewall_msg3"));
					}
				}
			}

		}
		//Ende der Untersuchung durch die Firewall

		return verwerfen;
	}

	/** Ueberprueft, ob die uebergebene IP-Adresse einem Adressbereich in
	 * den Empfaenger-Regeln entspricht.
	 * @return ob die uebergebene Adresse in einem Adressbereich einer Regel liegt
	 */
	private boolean pruefeIPEmpfaenger(String anfragendeIP){
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Firewall), pruefeIPEmpfaenger("+anfragendeIP+")");
		boolean regelVorhanden = false;
		//Main.debug.println("Firewall: prüfe nun IP-Empfänger");

		//Der Fall von Range (Bereich) wird hier bearbeitet.
		//Im Array sind Spalte 1 und 2 eingetragen
		for(int i = 0; i<empfaengerFilter.size() && !regelVorhanden; i++){

			String tmp = (String)empfaengerFilter.get(i);
			String [] tmpArray = tmp.split("#");

			if(tmpArray[0].equals(anfragendeIP)){
				regelVorhanden = true;
			}
			else if(!tmpArray[1].trim().equals("")){
				if(inPruefbereich(tmpArray[0], tmpArray[1], anfragendeIP)){
					regelVorhanden = true;
				}
			}
		}

		return regelVorhanden;
	}


	/**
	 * Ueberprueft, ob die uebergebene IP-Adresse einem Adressbereich in
	 * den Absender-Regeln entspricht.
	 * @return ob die uebergebene Adresse in einem Adressbereich einer Regel liegt
	 */
	private boolean pruefeIPSender(String anfragendeIP){
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Firewall), pruefeIPSender("+anfragendeIP+")");
		boolean regelVorhanden = false;

		for(int i = 0; i<absenderFilter.size() && !regelVorhanden; i++){
			String tmp = (String)absenderFilter.get(i);
			String [] tmpArray = tmp.split("#");

			if(tmpArray[0].equals(anfragendeIP)){
				Main.debug.println("INFO ("+this.hashCode()+"): Quelle "+anfragendeIP+" gefunden, daher blockieren");
				regelVorhanden = true;
			}
			else if(!tmpArray[1].trim().equals("")){
				if(inPruefbereich(tmpArray[0], tmpArray[1], anfragendeIP) ==true){
					regelVorhanden = true;
				}
			}
		}

		return regelVorhanden;
	}

	/**
	 * Ueberprueft, ob der uebergebene Port einer Regel entspricht.
	 * Diese Art der Regel fuehrt immer dazu, dass das Paket verworfen
	 * wird!
	 * @return ob zum uebergebenen Port eine Regel existiert
	 */
	private boolean pruefePortGeschlossen(String port){
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Firewall), pruefePortGeschlossen("+port+")");
		Object[] regel;
		String tmp;

		boolean regelVorhanden = false;

		//prüfe 1.Spalte in der Liste
		for(int i = 0; i<portList.size(); i++){
			regel = (Object[]) portList.get(i);
			tmp = (String)regel[0];

			if(tmp.equals(port)){
				regelVorhanden = true;
			}
		}

		return regelVorhanden;
	}

	/**
	 * Ueberprueft, ob uebergebener Port und uebergebene IP-Adresse einer
	 * Port-Regel entsprechen. Diese Art der Regel fuehrt immer dazu, dass das
	 * Paket akzeptiert wird! <br />
	 * Das ist der Fall, wenn zum Port ein Eintrag vorhanden
	 * ist und entweder die Adressbereiche nicht beruecksichtigt werden oder
	 * die Adresse zum gleichen Rechnernetz gehoert.
	 * @return ob zu uebergebenem Paar aus Port und IP-Adresse eine Regel existiert
	 */
	private boolean pruefePortOffen(String port, String ipAdresse) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Firewall), pruefePortOffen("+port+","+ipAdresse+")");
		Object[] regel;
		String tmp;

		boolean gleichesNetzwerk, regelVorhanden = false;

		//prüfe 1.Spalte in der Liste
		for(int i = 0; i<portList.size(); i++){
			regel = (Object[]) portList.get(i);
			tmp = (String)regel[0];

			if(tmp.equals(port)){
				if ((Boolean)regel[1]) {
					gleichesNetzwerk = VermittlungsProtokoll.gleichesRechnernetz(ipAdresse,
							getSystemSoftware().holeIPAdresse(),
							getSystemSoftware().holeNetzmaske());
					regelVorhanden = gleichesNetzwerk;
				}
				else {
					regelVorhanden = true;
				}
			}
		}

		return regelVorhanden;
	}

	/**
	 * fuegt eine IP-Adressenregel in die Liste mit IP-Adressen ein
	 */
	public void eintragHinzufuegen(String von, String bis, String typ){
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Firewall), eintragHinzufuegen("+von+","+bis+","+typ+")");
		if (!EingabenUeberpruefung.isGueltig(von, EingabenUeberpruefung.musterIpAdresse)
				|| !EingabenUeberpruefung.isGueltig(bis, EingabenUeberpruefung.musterIpAdresseAuchLeer)) {
			return;
		}
		
		if(bis.equals("")){
			bis = " ";
		}
		String tmp = von + "#" + bis +"#" + typ;

		if (typ.equals(ABSENDER_FILTER)) {
			absenderFilter.add(tmp);
			//Main.debug.println("Erfolgreich hinzugefügt: "+tmp );
			benachrichtigeBeobachter(messages.getString("sw_firewall_msg4")+von + " - "+bis);
		}
		else if (typ.equals(EMPFAENGER_FILTER)) {
			empfaengerFilter.add(tmp);
			//Main.debug.println("Erfolgreich hinzugefügt: "+tmp );
			benachrichtigeBeobachter(messages.getString("sw_firewall_msg4")+von + " - "+bis);
		}
	}
	/**
	 * fuegt eine Portregel in die Liste mit Portregeln ein
	 */
	public void eintragHinzufuegenPort(String port){
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Firewall), eintragHinzufuegenPort("+port+")");
		Object[] regel;

		if (port != null) {
			regel = new Object[2];
			regel[0] = port;
			regel[1] = Boolean.FALSE;

			portList.add(regel);

			//Main.debug.println("Erfolgreich hinzugefuegt: "+port+ "Listenindex: "+ portList.indexOf(regel));
			benachrichtigeBeobachter(messages.getString("sw_firewall_msg5")+port);
		}

	}

	public void eintragHinzufuegenPort(String port, boolean unterscheideNetzwerk) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Firewall), eintragHinzufuegenPort("+port+","+unterscheideNetzwerk+")");
		Object[] regel;

		if (port != null) {
			regel = new Object[2];
			regel[0] = port;
			regel[1] = new Boolean(unterscheideNetzwerk);

			portList.add(regel);

			//Main.debug.println("Erfolgreich hinzugefuegt: "+port+ "Listenindex: "+ portList.indexOf(regel));
			benachrichtigeBeobachter(messages.getString("sw_firewall_msg5")+port);
		}
	}

	/**
	 * entfernt eine IP-Adressen-Regel aus dem Regelkatalog
	 */
	public void entferneAbsenderRegel(int nummer){
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Firewall), entferneAbsenderRegel("+nummer+")");

		if(nummer >= 0 && nummer < absenderFilter.size()){  //damit keine zu große Zahl in GUI eingegeben werden kann
			benachrichtigeBeobachter(messages.getString("sw_firewall_msg6")+ absenderFilter.get(nummer)); //Log

		//Main.debug.println("Entfernt aus Liste:" + nummer);
		//Main.debug.println(absenderFilter.get(nummer));
			absenderFilter.remove(nummer);
		}

	}

	/**
	 * entfernt eine IP-Adressen-Regel aus dem Regelkatalog
	 */
	public void entferneEmpfaengerRegel(int nummer){
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Firewall), entferneEmpfaengerRegel("+nummer+")");

		if(nummer >= 0 && nummer < empfaengerFilter.size()){  //damit keine zu große Zahl in GUI eingegeben werden kann
			benachrichtigeBeobachter(messages.getString("sw_firewall_msg6")+ empfaengerFilter.get(nummer)); //Log

		//Main.debug.println("Entfernt aus Liste:" + nummer);
		//Main.debug.println(empfaengerFilter.get(nummer));
		empfaengerFilter.remove(nummer);
		}

	}

	/**
	 * entfernt eine Portregel aus dem Regelkatalog
	 */
	public void entferneRegelPort(int nummer){
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Firewall), entferneRegel("+nummer+")");
			//Main.debug.println("Listenfeld wird gelöscht: " +nummer);
			benachrichtigeBeobachter(messages.getString("sw_firewall_msg7")+ ((Object[])portList.get(nummer))[0]); //Log

			if(nummer >= 0 && nummer < portList.size()){  //damit keine zu große Zahl in GUI eingegeben werden kann
				portList.remove(nummer);
			}


	}

	/**
	 *
	 * @param untereGrenze
	 * @param obereGrenze
	 * @param pruefWert
	 * @return wandelt zunächst die Adressen in Zahlen um.
	 * Dann wird geprueft, ob der pruefWert inmitten der oberen und unteren Grenze liegt
	 */
	private boolean inPruefbereich(String untereGrenze, String obereGrenze, String pruefWert){
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Firewall), inPruefbereich("+untereGrenze+","+obereGrenze+","+pruefWert+")");

		boolean pruef = false;

		StringTokenizer untereTokens = new StringTokenizer(untereGrenze, ".");
		double ersteZahl = Integer.parseInt(untereTokens.nextToken());
		ersteZahl  = ersteZahl  * 16777216;  // entspricht 2^24
		double zweiteZahl = Integer.parseInt(untereTokens.nextToken());
		zweiteZahl  = zweiteZahl  * 65536;  //entspricht 2^16
		double dritteZahl = Integer.parseInt(untereTokens.nextToken());
		dritteZahl  = dritteZahl * 256;
		double vierteZahl = Integer.parseInt(untereTokens.nextToken());

		double untereSumme = ersteZahl + zweiteZahl+ dritteZahl + vierteZahl;
		//Main.debug.println("Firewall: untereSumme = "+untereSumme);

		//String obereGrenze in eine Zahl umrechnen:
		StringTokenizer obereTokens = new StringTokenizer(obereGrenze, ".");
		double ersteObereZahl = Integer.parseInt(obereTokens.nextToken());
		ersteObereZahl  = ersteObereZahl  * 16777216;  // entspricht 2^24
		double zweiteObereZahl = Integer.parseInt(obereTokens.nextToken());
		zweiteObereZahl  = zweiteObereZahl  * 65536;  //entspricht 2^16
		double dritteObereZahl = Integer.parseInt(obereTokens.nextToken());
		dritteObereZahl  = dritteObereZahl * 256;  //entspricht 2^8
		double vierteObereZahl = Integer.parseInt(obereTokens.nextToken());

		double obereSumme = ersteObereZahl + zweiteObereZahl+ dritteObereZahl + vierteObereZahl;
		//Main.debug.println("Firewall: obereSumme = "+obereSumme);

		//String pruefWert in eine Zahl umrechnen:
		StringTokenizer pruefTokens = new StringTokenizer(pruefWert, ".");
		double erstePruefZahl = Integer.parseInt(pruefTokens.nextToken());
		erstePruefZahl  = erstePruefZahl  * 16777216;  // entspricht 2^24
		double zweitePruefZahl = Integer.parseInt(pruefTokens.nextToken());
		zweitePruefZahl  = zweitePruefZahl  * 65536;  //entspricht 2^16
		double drittePruefZahl = Integer.parseInt(pruefTokens.nextToken());
		drittePruefZahl  = drittePruefZahl * 256;  //entspricht 2^8
		double viertePruefZahl = Integer.parseInt(pruefTokens.nextToken());

		double pruefZahlSumme = erstePruefZahl + zweitePruefZahl+ drittePruefZahl + viertePruefZahl;
		//Main.debug.println("Firewall: pruefZahlSumme = "+pruefZahlSumme);

		//pruefen ob der pruefWert zwischen Obersumme und Untersumme liegt
		if(pruefZahlSumme >= untereSumme && pruefZahlSumme <= obereSumme){
			pruef = true;
		}

		return pruef;
	}
	
	/**
	 * Methode fuer den Zugriff auf das Betriebssystem, auf dem diese Anwendung
	 * laeuft.
	 *
	 * @param bs
	 */
	public void setSystemSoftware(InternetKnotenBetriebssystem bs) {
		super.setSystemSoftware(bs);
	}



	//getter & setter:
	public int getBitRegel() {
		return bitRegel;
	}
	public void setBitRegel(int bitRegel) {
		this.bitRegel = bitRegel;
	}

	public boolean isAktiviert() {
		return aktiviert;
	}
	public void setAktiviert(boolean aktiviert) {
		benachrichtigeBeobachter("Firewall "+(aktiviert?"aktiviert":"deaktiviert"));
		this.aktiviert = aktiviert;
	}
	public LinkedList<String> getEmpfaengerFilterList() {
		return empfaengerFilter;
	}
	public void setEmpfaengerFilterList(LinkedList<String> ipList) {
		this.empfaengerFilter = ipList;
	}

	public LinkedList<String> getAbsenderFilterList() {
		return absenderFilter;
	}
	public void setAbsenderFilterList(LinkedList<String> ipList) {
		this.absenderFilter = ipList;
	}

	public LinkedList<Object[]> getPortList() {
		return portList;
	}
	public void setPortList(LinkedList<Object[]> portList) {
		this.portList = portList;
	}

	public void setModus(int modus) {
		this.modus = modus;
	}

	public int getModus() {
		return modus;
	}


	public void setzeNetzwerkInterfaces(LinkedList<NetzwerkInterface> netzwerkInterfaces) {
		LinkedList<NetzwerkInterface> allNics = this.getAllNetworkInterfaces();
		
		this.inactiveNics.removeAllElements();
	    for (NetzwerkInterface nic : allNics) {
	    	if (netzwerkInterfaces.indexOf(nic) == -1) {
	    		this.inactiveNics.add(new Integer(allNics.indexOf(nic)));
	    	}
	    }
    }


	public LinkedList<NetzwerkInterface> holeNetzwerkInterfaces() {
		LinkedList<NetzwerkInterface> allNics = this.getAllNetworkInterfaces();
		LinkedList<NetzwerkInterface> result = new LinkedList<NetzwerkInterface>();
		
		for (NetzwerkInterface nic : allNics) {
			try {
				if (!this.inactiveNics.contains(new Integer(allNics.indexOf(nic)))) {
					result.addLast(nic);
				}
			} catch (IndexOutOfBoundsException e) {
			}
		}
		return result;
    }
	
	private LinkedList<NetzwerkInterface> getAllNetworkInterfaces() {
		InternetKnoten host = (InternetKnoten) this.getSystemSoftware().getKnoten();
			
		return host.getNetzwerkInterfaces();
	}
	
	public boolean hinzuNetzwerkInterface(NetzwerkInterface nic) {
		int idx = this.getAllNetworkInterfaces().indexOf(nic);
		if (this.inactiveNics.contains(new Integer(idx))) {
			this.inactiveNics.remove(new Integer(idx));
			
			if (this.running) {
				this.starteFirewallThread(nic);
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean entferneNetzwerkInterface(NetzwerkInterface nic) {
		int idx = this.getAllNetworkInterfaces().indexOf(nic);
		if (! this.inactiveNics.contains(new Integer(idx))) {
			this.inactiveNics.add(new Integer(idx));
			
			if (this.running) {
				this.beendeFirewallThread(nic);
			}
			return true;
		}
		else {
			return false;
		}
	}

	public Vector<Integer> getInactiveNics() {
    	return inactiveNics;
    }


	public void setInactiveNics(Vector<Integer> inactiveNics) {
    	this.inactiveNics = inactiveNics;
    }
}
