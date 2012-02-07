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
package filius.software.lokal;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.tree.DefaultMutableTreeNode;

import filius.Main;
import filius.exception.SocketException;
import filius.rahmenprogramm.I18n;
import filius.software.clientserver.ClientAnwendung;
import filius.software.system.Betriebssystem;
import filius.software.system.Datei;
import filius.software.system.Dateisystem;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.transportschicht.ServerSocket;
import filius.software.transportschicht.Socket;
import filius.software.transportschicht.SocketSchnittstelle;
import filius.software.transportschicht.TransportProtokoll;
import filius.software.vermittlungsschicht.IP;
import filius.software.vermittlungsschicht.IcmpPaket;


/**
 * Diese Klasse soll eine Art Eingabeaufforderung oder Unix-Shell darstellen,
 * in der zumindest rudimentaere Befehle wie dir/ls/rename etc. moeglich sein sollen.
 * Auerdem soll hierin auch der Start von bestimmten Serveranwendungen und netcat moeglich
 * sein.
 *
 * @author Thomas Gerding & Johannes Bade
 *
 */
public class Terminal extends ClientAnwendung implements I18n {

	//Betriebssystem betriebssystem;
	boolean abfrageVar;

	private DefaultMutableTreeNode aktuellerOrdner;
	private boolean interrupted = false;

	public void setSystemSoftware(InternetKnotenBetriebssystem bs) {
		super.setSystemSoftware(bs);
		this.aktuellerOrdner = getSystemSoftware().getDateisystem().getRoot();
	}


	/**
	 * Diese Funktion bildet "move" bzw. "rename" ab und erlaubt es eine bestimmte
	 * Datei umzubenennen.
	 *
	 * @param alterName Der bisherige Dateiname
	 * @param neuerName Der gewnschte neue Dateiname
	 * @return Gibt eine Meldung ueber den Erfolg oder Misserfolg des Umbenennens zurck.
	 * @author Thomas Gerding & Johannes Bade
	 */
	public String move(String [] args) {
		return mv(args);
	}
	public String mv(String [] args)
	{
		Main.debug.print("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Terminal), mv(");
		for (int i=0; i<args.length; i++) { Main.debug.print(i+"='"+args[i]+"' "); }
		Main.debug.println(")");
		
		if(!numParams(args,2)) {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg40"));
			return messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg40");   // wrong number of parameters
		}
		if(pureCopy(args)) {										// positive case, everything worked fine
			this.getSystemSoftware().getDateisystem().deleteFile(filius.software.system.Dateisystem.absoluterPfad(getAktuellerOrdner())+Dateisystem.FILE_SEPARATOR+args[0]);
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg35"));
			return messages.getString("sw_terminal_msg35"); 
		}
		else {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg36"));
			return messages.getString("sw_terminal_msg36"); 
		}	// negative case, something wrong
	}

	/**
	 * delete file
	 */
	public String rm(String[] args) {
		return del(args);
	}
	public String del(String[] args) {
		Main.debug.print("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Terminal), del(");
		for (int i=0; i<args.length; i++) { Main.debug.print(i+"='"+args[i]+"' "); }
		Main.debug.println(")");
		if(!numParams(args,1)) { 
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg41"));
			return messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg41");   // wrong number of parameters
		}
		if (this.getSystemSoftware().getDateisystem().deleteFile(filius.software.system.Dateisystem.absoluterPfad(getAktuellerOrdner())+Dateisystem.FILE_SEPARATOR+args[0])) {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg37"));
			return messages.getString("sw_terminal_msg37"); 
		}
		else {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg38"));
			return messages.getString("sw_terminal_msg38");
		}
	}
	
	/**
	 * Kopiert eine Datei
	 *
	 * @param Parameter Array (String)
	 * @return
	 */
	//// common functionality for move and copy...
	private boolean pureCopy(String [] args) {
		Main.debug.print("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Terminal), pureCopy(");
		for (int i=0; i<args.length; i++) { Main.debug.print(i+"='"+args[i]+"' "); }
		Main.debug.println(")");
		this.getSystemSoftware().getDateisystem().printTree();
		String srcString = args[0];
		if(srcString.length()>0 && srcString.substring(0,1).equals(Dateisystem.FILE_SEPARATOR)) {   // 'pfad' is absolute path!
			srcString = Dateisystem.evaluatePathString(srcString);
		}
		else {
			srcString = Dateisystem.evaluatePathString(filius.software.system.Dateisystem.absoluterPfad(getAktuellerOrdner())+Dateisystem.FILE_SEPARATOR+srcString);
		}
		String destString = args[1];
		if(destString.length()>0 && destString.substring(0,1).equals(Dateisystem.FILE_SEPARATOR)) {   // 'pfad' is absolute path!
			destString = Dateisystem.evaluatePathString(destString);
		}
		else {
			destString = Dateisystem.evaluatePathString(filius.software.system.Dateisystem.absoluterPfad(getAktuellerOrdner())+Dateisystem.FILE_SEPARATOR+destString);
		}
		String destDir = Dateisystem.getDirectory(destString);
		String destFile = Dateisystem.getBasename(destString);

		//Main.debug.println("DEBUG: pureCopy: source '"+srcDir+"'-'"+srcFile+"', destination '"+destDir+"'-'"+destFile+"'");
		Datei sfile = this.getSystemSoftware().getDateisystem().holeDatei(srcString);
		if (sfile == null) return false;
		Datei dfile = new Datei(destFile,sfile.getDateiTyp(),sfile.getDateiInhalt());
		return this.getSystemSoftware().getDateisystem().speicherDatei(destDir, dfile);
	}
	// individual functionality for copy only
	public String copy(String [] args)
	{
		return cp(args);
	}
	public String cp(String [] args)
	{
		Main.debug.print("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (Terminal), cp(");
		for (int i=0; i<args.length; i++) { Main.debug.print(i+"='"+args[i]+"' "); }
		Main.debug.println(")");
		if(!numParams(args,2)) {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg40"));
			return messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg40");   // wrong number of parameters
		}
		if(pureCopy(args)) {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg33"));
			return messages.getString("sw_terminal_msg33"); 
		}	// positive case, everything worked fine
		else {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg34"));
			return messages.getString("sw_terminal_msg34");
		}				// negative case, something wrong
	}

	/* */
	public String ipconfig(String [] args)
	{
		Main.debug.print("INVOKED ("+this.hashCode()+", "+this.getId()+") "+getClass()+" (Terminal), ipconfig(");
		for (int i=0; i<args.length; i++) { Main.debug.print(i+"='"+args[i]+"' "); }
		Main.debug.println(")");
		if(!numParams(args,0)) {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg42"));
			return messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg42");   // wrong number of parameters
		}
		Betriebssystem bs = (Betriebssystem) getSystemSoftware();
		String ausgabe = "";

		ausgabe += messages.getString("sw_terminal_msg4") + " " + bs.holeIPAdresse() + "\n";
		ausgabe += messages.getString("sw_terminal_msg5") + " " + bs.holeNetzmaske() + "\n";
		ausgabe += messages.getString("sw_terminal_msg26") + " " + bs.holeMACAdresse() + "\n";
		ausgabe += messages.getString("sw_terminal_msg6") + " " + bs.getStandardGateway() + "\n";
		ausgabe += messages.getString("sw_terminal_msg27") + " " + bs.getDNSServer() + "\n";

		benachrichtigeBeobachter(ausgabe);
		return ausgabe;
	}

	/* Entspricht route print unter windows  */
	public String route(String [] args)
	{
		Main.debug.print("INVOKED ("+this.hashCode()+", "+this.getId()+") "+getClass()+" (Terminal), route(");
		for (int i=0; i<args.length; i++) { Main.debug.print(i+"='"+args[i]+"' "); }
		Main.debug.println(")");
		if(!numParams(args,0)) {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg42"));
			return messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg42");   // wrong number of parameters
		}
		String ausgabe = messages.getString("sw_terminal_msg7");

		LinkedList<String[]> routingTabelle = getSystemSoftware().getWeiterleitungstabelle().holeTabelle();
		ListIterator<String[]> it = routingTabelle.listIterator();

		while (it.hasNext())
		{
			String[] eintrag = (String[])it.next();
			ausgabe+="| ";
			for (int i=0;i<eintrag.length;i++)
			{
				ausgabe += eintrag[i] + stringFuellen(15-eintrag[i].length(), " ") + " | ";
			}
			ausgabe+="\n";
		}

		benachrichtigeBeobachter(ausgabe);
		return ausgabe;
	}

	/**
	 * Diese Funktion bietet Aehnliches wie "ls" oder "dir" auf der normalen Eingabeaufforderung.
	 * Es gibt eine Liste aller Dateien des Rechners und deren Groesse zurueck.
	 *
	 * @return Gibt die Liste der vorhandenen Dateien (und Verzeichnisse) in
	 * einem formatierten String zurueck, der direkt ausgegeben
	 * werden kann.
	 *
	 * @author Thomas Gerding & Johannes Bade
	 * @param Parameter Array (String)
	 */
	public String ls(String [] args)
	{
		return dir(args);
	}
	public String dir(String [] args)
	{
		Main.debug.print("INVOKED ("+this.hashCode()+", "+this.getId()+") "+getClass()+" (Terminal), dir(");
		for (int i=0; i<args.length; i++) { Main.debug.print(i+"='"+args[i]+"' "); }
		Main.debug.println(")");
		if(!numParams(args,0,1)) {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg43"));
			return messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg43");   // wrong number of parameters
		}
		LinkedList<Object> liste;
		StringBuffer inhalt;
		String currPath;
		int anzahlVerzeichnisse = 0;
		int anzahlDateien = 0;
		Datei tmpDatei;
		int leerzeichen;

		if(args[0].isEmpty()) {
			liste = getSystemSoftware().getDateisystem().listeVerzeichnis(aktuellerOrdner);
			currPath = Dateisystem.absoluterPfad(aktuellerOrdner);
		}
		else {
			if(args[0].length()>0 && args[0].substring(0,1).equals(Dateisystem.FILE_SEPARATOR)) {  // argument given as absolute path!
				liste = getSystemSoftware().getDateisystem().listeVerzeichnis(getSystemSoftware().getDateisystem().verzeichnisKnoten(args[0]));
				currPath = Dateisystem.evaluatePathString(args[0]);
			}
			else {
				liste = getSystemSoftware().getDateisystem().listeVerzeichnis(Dateisystem.verzeichnisKnoten(aktuellerOrdner,args[0]));
				currPath = Dateisystem.evaluatePathString(Dateisystem.absoluterPfad(aktuellerOrdner) + Dateisystem.FILE_SEPARATOR + args[0]);
			}
		}			

		if (liste == null || liste.size() == 0) {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg8"));
			return messages.getString("sw_terminal_msg8");
		}
		else {
			inhalt = new StringBuffer();
			inhalt.append(messages.getString("sw_terminal_msg9") + " " + currPath + ":\n");
			
			for (Object tmp : liste) {
				// Fall Datei:
				if (tmp instanceof Datei) {
					anzahlDateien++;
					tmpDatei = (Datei) tmp;
					leerzeichen = 40 - tmpDatei.getName().length();
					inhalt.append(tmpDatei.getName()+ stringFuellen(leerzeichen, ".")
							+ tmpDatei.holeGroesse() + "\n");
				}
				// Fall Ordner:
				else {
					anzahlVerzeichnisse++;
					inhalt.append("[" + tmp + "]\n");
				}
			}

			inhalt.append(messages.getString("sw_terminal_msg10") + anzahlVerzeichnisse);
			inhalt.append(messages.getString("sw_terminal_msg11") + anzahlDateien + "\n");

		}

		benachrichtigeBeobachter(inhalt.toString());
		return inhalt.toString();
	}



	/**
	 *
	 * touch
	 *
	 */
	public String touch(String [] args)
	{
		Main.debug.print("INVOKED ("+this.hashCode()+", "+this.getId()+") "+getClass()+" (Terminal), touch(");
		for (int i=0; i<args.length; i++) { Main.debug.print(i+"='"+args[i]+"' "); }
		Main.debug.println(")");
		if(!numParams(args,1)) {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg41"));
			return messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg41");   // wrong number of parameters
		}
		String ergebnis = messages.getString("sw_terminal_msg12");
		String absPath;
		if(args[0].length()>0 && args[0].substring(0,1).equals(Dateisystem.FILE_SEPARATOR)) {   // 'pfad' is absolute path!
			absPath = Dateisystem.evaluatePathString(args[0]);
		}
		else {
			absPath = Dateisystem.evaluatePathString(Dateisystem.absoluterPfad(aktuellerOrdner)+Dateisystem.FILE_SEPARATOR+args[0]);
		}
		String filePath = Dateisystem.getDirectory(absPath);
		String dateiName = Dateisystem.getBasename(absPath);
		if (!dateiName.equals(""))
		{
			if (!getSystemSoftware().getDateisystem().dateiVorhanden(filePath, dateiName))
			{
				getSystemSoftware().getDateisystem().speicherDatei(filePath, new Datei(dateiName,"text/txt",""));
				ergebnis = messages.getString("sw_terminal_msg13");
			}
			else
			{
				ergebnis = messages.getString("sw_terminal_msg14");
			}
		}
		else
		{
			ergebnis = messages.getString("sw_terminal_msg15");
		}
		benachrichtigeBeobachter(ergebnis);
		return ergebnis;
	}

	/**
	 *
	 * mkdir
	 *
	 */
	public String mkdir(String [] args)
	{
		Main.debug.print("INVOKED ("+this.hashCode()+", "+this.getId()+") "+getClass()+" (Terminal), mkdir(");
		for (int i=0; i<args.length; i++) { Main.debug.print(i+"='"+args[i]+"' "); }
		Main.debug.println(")");
		if(!numParams(args,1)) {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg41"));
			return messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg41");   // wrong number of parameters
		}
		String ergebnis = messages.getString("sw_terminal_msg16");
		String absPath;
		if(args[0].length()>0 && args[0].substring(0,1).equals(Dateisystem.FILE_SEPARATOR)) {   // 'pfad' is absolute path!
			absPath = Dateisystem.evaluatePathString(args[0]);
		}
		else {
			absPath = Dateisystem.evaluatePathString(Dateisystem.absoluterPfad(aktuellerOrdner)+Dateisystem.FILE_SEPARATOR+args[0]);
		}
		String filePath = Dateisystem.getDirectory(absPath);
		String dateiName = Dateisystem.getBasename(absPath);
		if (!dateiName.equals(""))
		{
			if (!getSystemSoftware().getDateisystem().dateiVorhanden(filePath, dateiName)
					&& getSystemSoftware().getDateisystem().erstelleVerzeichnis(filePath, dateiName))
			{
				ergebnis = messages.getString("sw_terminal_msg17");
			}
			else
			{
				ergebnis = messages.getString("sw_terminal_msg18");
			}
		}
		else
		{
			ergebnis = messages.getString("sw_terminal_msg19");
		}
		benachrichtigeBeobachter(ergebnis);
		return ergebnis;
	}

	/**
	 *
	 * cd
	 *
	 */
	public String cd(String [] args)
	{
		Main.debug.print("INVOKED ("+this.hashCode()+", "+this.getId()+") "+getClass()+" (Terminal), cd(");
		for (int i=0; i<args.length; i++) { Main.debug.print(i+"='"+args[i]+"' "); }
		Main.debug.println(")");
		String ergebnis = "";
		if(!numParams(args,0,1)) {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg43"));
			return messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg43");   // wrong number of parameters
		}
		if (numParams(args,1))
		{
			DefaultMutableTreeNode newDir;
			if(args[0].charAt(0) == '/')  // absolute path
				newDir = getSystemSoftware().getDateisystem().changeDirectory(args[0]);
			else // relative path
				newDir = getSystemSoftware().getDateisystem().changeDirectory(Dateisystem.absoluterPfad(aktuellerOrdner),args[0]);
			if(newDir != null) {    // first, check whether directory change was successful; otherwise stay in current directory
				aktuellerOrdner = newDir;
			}
			else {
				ergebnis = messages.getString("sw_terminal_msg20");
			}
		}
		else {
			ergebnis=Dateisystem.absoluterPfad(aktuellerOrdner);
		}

		benachrichtigeBeobachter(ergebnis);
		return ergebnis;
	}
	// Unix Tool 'pwd': print working directory 
	public String pwd(String[] args) {
		Main.debug.print("INVOKED ("+this.hashCode()+", "+this.getId()+") "+getClass()+" (Terminal), pwd(");
		for (int i=0; i<args.length; i++) { Main.debug.print(i+"='"+args[i]+"' "); }
		Main.debug.println(")");
		if(!numParams(args,0)) {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg42"));
			return messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg42");   // wrong number of parameters
		}
		String ergebnis=Dateisystem.absoluterPfad(aktuellerOrdner);
		benachrichtigeBeobachter(ergebnis);
		return ergebnis;
	}
	
	public String netstat(String[] args) {
		TransportProtokoll transport;
		StringBuffer ergebnis = new StringBuffer();
		String protocol;
		
		ergebnis.append(messages.getString("sw_terminal_msg49"));
		ergebnis.append("--------------------------------------------------------------------------\n");
		
		transport = this.getSystemSoftware().holeTcp();
		protocol = "TCP";
		this.processTransportProtocol(ergebnis, transport, protocol);
		
		transport = this.getSystemSoftware().holeUdp();
		protocol = "UDP";
		this.processTransportProtocol(ergebnis, transport, protocol);		
		
		benachrichtigeBeobachter(ergebnis);
		return ergebnis.toString();
	}
	
	private void processTransportProtocol(StringBuffer ergebnis, TransportProtokoll transport, String protocol) {
		Enumeration<Integer> benutztePorts;
		int port;
		SocketSchnittstelle tmpSocket;
		String[] socketData;
		
		benutztePorts = transport.holeAktiveSockets().keys();
		while (benutztePorts.hasMoreElements()) {
			port = benutztePorts.nextElement().intValue();
			
			try {
				tmpSocket = transport.holeSocket(port);
				socketData = this.getSocketInformation(tmpSocket);
				
				ergebnis.append(String.format("| %-8s ", protocol));
				ergebnis.append(String.format("| %-6s ", ""+port));
				ergebnis.append(String.format("| %-15s ", socketData[0]));
				ergebnis.append(String.format("| %-15s ", socketData[1]));
				ergebnis.append(String.format("| %-14s |\n", socketData[2]));
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String[] getSocketInformation(SocketSchnittstelle socket) {
		String[] routingEntry;
		String localAddress="", remoteAddress="", state="";
		if (socket instanceof Socket) {
			remoteAddress = ((Socket) socket).holeZielIPAdresse();
			routingEntry = ((InternetKnotenBetriebssystem)this.getSystemSoftware()).getWeiterleitungstabelle().holeWeiterleitungsZiele(remoteAddress);
			if (routingEntry != null) {
				localAddress = routingEntry[1];
			}
			else {
				localAddress = "<unknown>";
			}
			state = ((Socket)socket).getStateAsString();
		}
		else if (socket instanceof ServerSocket) {
			remoteAddress = "-";
			localAddress = "-";
			state = "LISTEN";
		}
		
		return new String[]{localAddress, remoteAddress, state};
	}
	
	/**
	 *
	 * test
	 *
	 */
	public String test(String [] args)
	{
		String ergebnis = messages.getString("sw_terminal_msg23");

		if (this.getSystemSoftware().getDateisystem().speicherDatei(args[0], new Datei("test","txt","blaaa")))
		{
			ergebnis = messages.getString("sw_terminal_msg24");
		}

		benachrichtigeBeobachter(ergebnis);
		return ergebnis;
	}

	/**
	 * 
	 * help command to list all available commands implemented in this terminal application
	 * 
	 */
	public String help(String [] args) {
		benachrichtigeBeobachter(messages.getString("sw_terminal_msg25"));
		return messages.getString("sw_terminal_msg25");
	}

	/**
	 * 
	 * 'host' command to resolve URL to an IP address using the client's DNS server entry
	 * 
	 */
	public String host(String [] args) {
		Main.debug.print("INVOKED ("+this.hashCode()+", "+this.getId()+") "+getClass()+" (Terminal), host(");
		for (int i=0; i<args.length; i++) { Main.debug.print(i+"='"+args[i]+"' "); }
		Main.debug.println(")");
		if(!numParams(args,1)) {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg44"));
			return messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg44");   // wrong number of parameters
		}
		Betriebssystem bs = (Betriebssystem) getSystemSoftware();
		filius.software.dns.Resolver res = bs.holeDNSClient();
		if(res == null) { 
			filius.Main.debug.println("ERROR ("+this.hashCode()+"): Terminal 'host': Resolver is null!");
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg30"));
			return messages.getString("sw_terminal_msg30"); 
		}
		String result;
		
		try {
			result = res.holeIPAdresse(args[0]);
			if (result != null) {
				benachrichtigeBeobachter(args[0]+" "+messages.getString("sw_terminal_msg28")+" "+result+"\n");
				return args[0]+" "+messages.getString("sw_terminal_msg28")+" "+result+"\n";
			}
			else {
				filius.Main.debug.println("ERROR ("+this.hashCode()+"): Terminal 'host': result is null!");
				benachrichtigeBeobachter(messages.getString("sw_terminal_msg30"));
				return messages.getString("sw_terminal_msg30");
			}
		}
		catch (Exception e) {
			e.printStackTrace(filius.Main.debug);
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg29"));
			return messages.getString("sw_terminal_msg29");
		}
	}

	/**
	 * 
	 * 'ping' command to check connectivity via ICMP echo request/reply
	 * 
	 */
	public String ping(String [] args) {
		Main.debug.print("INVOKED ("+this.hashCode()+", "+this.getId()+") "+getClass()+" (Terminal), ping(");
		for (int i=0; i<args.length; i++) { Main.debug.print(i+"='"+args[i]+"' "); }
		Main.debug.println(")");
		if(!numParams(args,1)) {
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg44"));
			return messages.getString("sw_terminal_msg32")+messages.getString("sw_terminal_msg44");   // wrong number of parameters
		}
		filius.software.dns.Resolver res = getSystemSoftware().holeDNSClient();
		if(res == null) { 
			filius.Main.debug.println("ERROR ("+this.hashCode()+"): Terminal 'host': Resolver is null!");
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg30"));
			return messages.getString("sw_terminal_msg30"); 
		}

		// first: resolve host name
		String destIp;
		try {
			destIp = IP.ipCheck(args[0]);
			if(destIp == null) {   // args[0] is not an IP address
				destIp = res.holeIPAdresse(args[0]);
			}
			if (destIp == null) {  // args[0] could also not be resolved
				filius.Main.debug.println("ERROR ("+this.hashCode()+"): Terminal 'host': result is null!");
				benachrichtigeBeobachter(messages.getString("sw_terminal_msg30"));
				return messages.getString("sw_terminal_msg30");
			}
		}
		catch (Exception e) {
			e.printStackTrace(filius.Main.debug);
			benachrichtigeBeobachter(messages.getString("sw_terminal_msg29"));
			return messages.getString("sw_terminal_msg29");
		}

		// second: send several (=4) ICMP echo requests
		long timeStart, timeDiff;
		benachrichtigeBeobachter(new Boolean(true));   // inform about a multiple data transmission to the observer
		benachrichtigeBeobachter("PING "+args[0]+" ("+destIp+")");
		
		int receivedReplies = 0;
		int num;
		for (num=0; !interrupted; num++) {
			try {
				timeStart = Calendar.getInstance().getTimeInMillis();
				/// CAVE: wahrscheinlich hier Queue nötig und blockieren, bis Ergebnis da ist!!!
				int resTTL = getSystemSoftware().holeICMP().startSinglePing(destIp, num+1);
				timeDiff = 1000  // wait 1s between single ping executions
						 - (Calendar.getInstance().getTimeInMillis() - timeStart);  // subtract needed time for former ping
				//Main.debug.println("DEBUG: Terminal, ping (num="+(num+1)+"), resTTL="+resTTL+", delay="+(1000-timeDiff)+", timeDiff="+timeDiff);
				if(resTTL >= 0) {
					benachrichtigeBeobachter("\nFrom "+args[0]+" ("+destIp+"): icmp_seq="+(num+1)+" ttl="+resTTL+
							" time="+(Calendar.getInstance().getTimeInMillis()-timeStart)+"ms");
					receivedReplies++;
				}
				if (timeDiff > 0) { 			
					try {
				//		Main.debug.println("DEBUG: Terminal wartet für "+timeDiff+"ms");
						Thread.sleep(timeDiff);
				//		Main.debug.println("DEBUG: Terminal fertig mit Warten");
					}
					catch (InterruptedException e) {}
				}
				
			}
			catch (java.util.concurrent.TimeoutException e) {
				benachrichtigeBeobachter("\nFrom "+args[0]+" ("+destIp+"): icmp_seq="+(num+1)+"   -- Timeout!");
			}
			catch (Exception e) {
				e.printStackTrace(filius.Main.debug);
			}
		}
		benachrichtigeBeobachter(new Boolean(false));   // inform about a multiple data transmission to the observer
		// print statistics
		benachrichtigeBeobachter("\n--- "+args[0]+" "+messages.getString("sw_terminal_msg45")+" ---\n"
							   + num+" "+messages.getString("sw_terminal_msg46")+", "
							   + receivedReplies+" "+messages.getString("sw_terminal_msg47")+", "
							   + ((int) Math.round((1-(((double) receivedReplies) / ((double) num)))*100))+"% "+messages.getString("sw_terminal_msg48") 
							   + "\n");
		return "";
	}

	/**
	 * 
	 * 'traceroute' prints the route packets take to the network host
	 * (using ICMP Echo Request and ICMP Time Exceeded)
	 * 
	 */
	public String traceroute(String [] args) {
		if (!numParams(args,1)) {
			benachrichtigeBeobachter("Benutzung:  traceroute <ziel-IP>");
			return null;
		}

		int maxHops = 20;

		// 1.: Hostnamen auflösen
		String destIP = IP.ipCheck(args[0]);
		if (destIP == null) {
			try {
				filius.software.dns.Resolver res = getSystemSoftware().holeDNSClient();
				if (res == null) {
					benachrichtigeBeobachter(
							"Fehler: Du hast keinen DNS-Server eingestellt.");
					return null;
				}

				destIP = res.holeIPAdresse(args[0]);
			} catch (Exception e) {
				e.printStackTrace(filius.Main.debug);
				benachrichtigeBeobachter("Fehler: Irgendwas ist ganz doll kaputt.");
				return null;
			}
		}
		if (destIP == null) {
			benachrichtigeBeobachter("Fehler: Hostname konnte nicht aufgeloest werden. Sorry!");
			return null;
		}


		benachrichtigeBeobachter(new Boolean(true));
		benachrichtigeBeobachter("traceroute zu " + args[0] + " (" + destIP + "), " + 
				maxHops + " Spruenge maximal\n\n");


		// 2.: Pings senden und gucken, was alles zurueckkommt
		IcmpPaket recv = null;
		int seqNr = 42*23;
		int fehler = 0;
		int ttl;

		for (ttl = 1; ttl <= maxHops && !interrupted; ttl++) {
			benachrichtigeBeobachter(" " + ttl + "    ");

			for (int i = 0; i < 3 && !interrupted; i++) {
				seqNr++;
				recv = getSystemSoftware().holeICMP().sendProbe(destIP, ttl, seqNr);
				if (recv != null && recv.getSeqNr() == seqNr) {
					fehler = 0;
					break;
				}
				fehler++;
				benachrichtigeBeobachter("* ");
			}

			if (fehler == 0) {
				benachrichtigeBeobachter(recv.getQuellIp());
				if (recv.getIcmpType() != 11) {
					break;
				}
			} else if (fehler > 5) {
				break;
			}

			benachrichtigeBeobachter("\n");
		}

		benachrichtigeBeobachter(new Boolean(false));
		if (ttl >= maxHops) {
			benachrichtigeBeobachter("\n\n" + args[0] + " scheint seeehr weit weg zu sein.");
		} else if (interrupted) {
			benachrichtigeBeobachter("\n\noh, ich wurde interrupted :(");
		} else if (recv != null && recv.getIcmpType() == 3) {
			switch (recv.getIcmpCode()) {
				case 0:
					benachrichtigeBeobachter("\n\nFehler: ICMP Network Unreachable von "
							+ recv.getQuellIp());
					break;
				case 1:
					benachrichtigeBeobachter("\n\nFehler: ICMP Host Unreachable von "
							+ recv.getQuellIp());
					break;
				default:
					benachrichtigeBeobachter("\n\nFehler: ICMP Destination Unreachable (code "
							+ recv.getIcmpCode() + ") von " + recv.getQuellIp());
					break;
			}
		} else if (fehler == 0) {
			benachrichtigeBeobachter("\n\n" + args[0] + " wurde nach " + ttl + " Spruengen erreicht.");
		} else {
			benachrichtigeBeobachter("\n\nZu viele Fehler, ich geb' auf.");
		}

		return null;
	}

	public void setInterrupt(boolean val) {
		this.interrupted = val;
	}

	public void beenden() {
		setInterrupt(true);
		super.beenden();
	}

	public void terminalEingabeAuswerten(String enteredCommand, String[] enteredParameters)
	{
		Main.debug.println("INVOKED ("+this.hashCode()+", "+this.getId()+") "+getClass()+" (Terminal), terminalEingabeAuswerten("+enteredCommand+","+enteredParameters+")");
		Object[] args= new Object[1];
		args[0]=enteredParameters;
		try {
			// test, whether method exists; if not, exception will be evaluated
			this.getClass().getDeclaredMethod(enteredCommand,enteredParameters.getClass());

//			Main.debug.println("DEBUG:   Terminal, terminalEingabeAuswerten: \n\tMethode '"
//					+ enteredCommand + "' gefunden.");
			setInterrupt(false); // man will ja auch wieder was ausführen
			ausfuehren(enteredCommand, args);
		} catch (NoSuchMethodException e) {
			benachrichtigeBeobachter(messages.getString("terminal_msg2")+"\n" + messages.getString("terminal_msg3") + "\n");
		} catch (Exception e) {
			e.printStackTrace(Main.debug);
		}
	}

	public DefaultMutableTreeNode getAktuellerOrdner() {
		return aktuellerOrdner;
	}

	public void setAktuellerOrdner(DefaultMutableTreeNode aktuellerOrdner) {
		this.aktuellerOrdner = aktuellerOrdner;
	}

	public String addSlashes(String sl)
	{
		Main.debug.println("INVOKED ("+this.hashCode()+", "+this.getId()+") "+getClass()+" (Terminal), addSlashes("+sl+")");
		String slNeu = "";
		String letztesZ = ""+sl.charAt(sl.length()-1);
		if (!letztesZ.equals("/")){slNeu = sl+"/";}

		return slNeu;
	}

	/**
	 *
	 * @author Hannes Johannes Bade & Thomas Gerding
	 *
	 * fuellt einen String mit Leerzeichen auf (bis zur länge a)
	 *
	 * @param a
	 * @param fueller
	 * @return
	 */
	//// welche der beiden Methoden wird denn wirklich verwendet?
	//// bisher war Implementierung exakt identisch --> Verweis aufeinander eingefügt!
	private String stringFuellen(int a, String fueller) {
		Main.debug.println("INVOKED ("+this.hashCode()+", "+this.getId()+") "+getClass()+" (Terminal), stringFuellen("+a+","+fueller+")");
		String tmp = "";
		for (int i = 0; i < a; i++) {
			tmp = tmp + fueller;
		}
		return tmp;
	}
	public String makeEmptyString(int a, String fueller)
	{
		return stringFuellen(a,fueller);
	}
	
	/**
	 * method to check for correct number of parameters
	 */
	private int countParams(String[] args) {
		Main.debug.println("INVOKED ("+this.hashCode()+", "+this.getId()+") "+getClass()+" (Terminal), countParams("+args+")");
		int count=0;
		for (int i=0; i<args.length; i++) {
			if (!args[i].isEmpty()) { count++; }
			else return count;   // return on first empty entry
		}
		return count;
	}
	private boolean numParams(String[] args,int exactNum) {
		return (exactNum==countParams(args));
	}
	private boolean numParams(String[] args,int minNum,int maxNum) {
		int count=countParams(args);
		return ((count>=minNum) && (count<=maxNum));
	}
}
