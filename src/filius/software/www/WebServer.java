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
package filius.software.www;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.tree.DefaultMutableTreeNode;

import filius.Main;
import filius.rahmenprogramm.Base64;
import filius.rahmenprogramm.Information;
import filius.software.clientserver.TCPServerAnwendung;
import filius.software.system.Datei;
import filius.software.system.Dateisystem;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.transportschicht.Socket;
import filius.software.transportschicht.TCPSocket;

/**
 * Anwendungklasse des Webservers
 *
 */
public class WebServer extends TCPServerAnwendung {

	private DefaultMutableTreeNode verzeichnis;

	private HashMap<String, WebServerPlugIn> plugins = new HashMap<String, WebServerPlugIn>();

	private String[][] vHostArray = new String[5][2];  // default size of 5x2

	public WebServer() {
		super();
		port = 80;
		this.resetVHosts();
	}
	
	public void resetVHosts() {
		for(int i=0; i<vHostArray.length; i++) {
			vHostArray[i][0]="";
			vHostArray[i][1]="";
		}
		this.benachrichtigeBeobachter();
	}

	/**
	 * nachdem ein WebServer erzeugt wurde, muss ueber die Vaterklasse Anwendung
	 * ein Betriebssystem zugewiesen werden
	 */
	public void setSystemSoftware(InternetKnotenBetriebssystem betriebssystem) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebServer), setSystemSoftware("+betriebssystem+")");
		super.setSystemSoftware(betriebssystem);

		erzeugeStandardVerzeichnis();
	}

	public void setzePlugIn(WebServerPlugIn pi) {
		plugins.put(pi.getPfad(), pi);
	}

	public void erzeugeIndexDatei(String dateipfad) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebServer), erzeugeIndexDatei("+dateipfad+")");
		BufferedReader dateiPuffer;
		StringBuffer fullFile;
		String input;

		try {
			dateiPuffer = new BufferedReader(new FileReader(dateipfad));
			fullFile = new StringBuffer();
			while ((input = dateiPuffer.readLine()) != null) {
				fullFile.append(input + "\n");
			}
			erzeugeDatei("index", "html", fullFile.toString());
		}
		catch (Exception e) {
			e.printStackTrace(Main.debug);
		}

	}

	public byte[] inputStreamToBytes(java.io.InputStream in) {
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(1024);
		byte[] buffer = new byte[1024];
		int len;

		try{
			while((len = in.read(buffer)) >= 0)
				out.write(buffer, 0, len);
			in.close();
			out.close();
		} catch ( java.io.IOException e ) {
			Main.debug.println("EXCEPTION ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebServer):  IOException in conversion of InputStream to Byte[]");
			e.printStackTrace(Main.debug);
		}
		return out.toByteArray();
	}
	
	/**
	 * erzeugt alle Dateien, fuer Fehlermeldungen etc. Die Dateien liegen als
	 * Textdateien im realen Ordner /konfig
	 */
	private void erzeugeStandardVerzeichnis() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebServer), erzeugeStandardVerzeichnis()");
		Dateisystem dateisystem;

		dateisystem = getSystemSoftware().getDateisystem();
		dateisystem.erstelleVerzeichnis(dateisystem.getRoot(), "webserver");
		verzeichnis = dateisystem.verzeichnisKnoten(dateisystem.holeRootPfad()
				+ Dateisystem.FILE_SEPARATOR + "webserver");
		try {
			if (!dateisystem.dateiVorhanden(verzeichnis, "index.html")) {

				erzeugeIndexDatei(Information.getInformation().getProgrammPfad()
						+ "config/webserver_index.txt");

				// this was used for externally stored file... no longer working
//				erzeugeDatei("splashscreen-mini", "png", Base64
//						.encodeFromFile(Information.getInformation().getProgrammPfad()
//								+ "gfx/allgemein/splashscreen-mini.png"));
				
				erzeugeDatei("splashscreen-mini", "png", 
						Base64.encodeBytes(inputStreamToBytes(getClass().getResourceAsStream("/gfx/allgemein/splashscreen-mini.png"))));
			}
		}
		catch (Exception e) {
			e.printStackTrace(Main.debug);
		}

	}

	/**
	 * erzeugt eine Datei fuer das Dateisystem von Filius. Dieses arbeitet mit
	 * TreeNodes
	 */
	private void erzeugeDatei(String dateiname, String endung, String quellcode) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebServer), erzeugeDatei("+dateiname+","+endung+","+quellcode+")");
		String kompletteDateiName = dateiname + "." + endung;
		Datei datei = new Datei(kompletteDateiName, endung, quellcode);
		getSystemSoftware().getDateisystem().speicherDatei(verzeichnis, datei);
	}

	/**
	 * holt eine Datei aus dem Verzeichnisbaum des WebServers. Dieser benutzt
	 * TreeNodes
	 */
	protected Datei dateiLiefern(String relativerPfad) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebServer), dateiLiefern("+relativerPfad+")");
		Datei tmpDatei;

		tmpDatei = getSystemSoftware().getDateisystem().holeDatei(verzeichnis, relativerPfad);
		return tmpDatei;
	}

	/**
	 * sucht nach einem bestimmten PlugIn. Zum Beispiel fuer eine Firewall
	 * Bisher installierte PlugIns: /konfig.html & /log.html
	 *
	 */
	public WebServerPlugIn holePlugin(String key) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebServer), holePlugin("+key+")");
		WebServerPlugIn plugIn = null;

		if (plugins.containsKey(key)) {
			plugIn = (WebServerPlugIn) plugins.get(key);

		}

		return plugIn;
	}

	public HashMap<String, WebServerPlugIn> getPlugins() {
		return plugins;
	}

	public void setPlugins(HashMap<String, WebServerPlugIn> plugins) {
		this.plugins = plugins;
	}

	public String printVHostTable() {
		String result = "";
		for(int i=0; i<vHostArray.length; i++) {
			result+=i+":\t";
			if(vHostArray[i][0]==null) 
				result+="<null>\t";
			else
				result+=vHostArray[i][0]+"\t";
			if(vHostArray[i][1]==null) 
				result+="<null>\n";
			else
				result+=vHostArray[i][1]+"\n";
		}
		return result;
	}
	
	public void changeVHostTable(int row, int col, String val) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebServer), changeVHostTable("+row+","+col+","+val+")");
		if(val!=null) vHostArray[row][col] = val;
		else vHostArray[row][col] = "";
//		Main.debug.println("DEBUG WebServer:  vHostArray (danach):\n"+printVHostTable());
		this.saveVHosts();
	}
	
	public String vhostPrefix(String vhost) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebServer), vhostPrefix("+vhost+")");
		for(int i=0; i<vHostArray.length; i++) {
		  if(vHostArray[i][0]!=null &&
			 vHostArray[i][0].equalsIgnoreCase(vhost)) {
			  if(vHostArray[i][1] != null)
				  return vHostArray[i][1];
		  }
	  }
	  return "";
	}

	public String[][] getVHostArray() {
		return vHostArray;
	}
	
	protected void neuerMitarbeiter(Socket socket) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebServer), neuerMitarbeiter("+socket+")");
		WebServerMitarbeiter wsMitarbeiter;

		if (socket instanceof TCPSocket) {
			wsMitarbeiter = new WebServerMitarbeiter(this, (TCPSocket)socket);
			wsMitarbeiter.starten();
			mitarbeiter.add(wsMitarbeiter);
		}

	}

	public void starten() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebServer), starten()");
		super.starten();

		if (!getSystemSoftware().getDateisystem().dateiVorhanden("www.conf", "vhosts")) {
			getSystemSoftware().getDateisystem().erstelleVerzeichnis("root", "www.conf");
		}

		initialisiereVHosts();
	}

	public void beenden() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (WebServer), beenden()");
		super.beenden();
	}

	private void saveVHosts() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (DNSServer), schreibeRecordListe()");
		Datei vhosts;
		StringBuffer text;
		Dateisystem dateisystem;
		String host, directory;

		dateisystem = getSystemSoftware().getDateisystem();

		text = new StringBuffer();

		for(int i=0; i<vHostArray.length; i++) {
			host = vHostArray[i][0];
			directory = vHostArray[i][1];
			text.append((host != null ? host : "") + "\n" + (directory != null ? directory : "") + "\n");
		}

		vhosts = new Datei();
		vhosts.setDateiInhalt(text.toString());
		vhosts.setName("vhosts");

		dateisystem.speicherDatei(dateisystem.holeRootPfad()+Dateisystem.FILE_SEPARATOR+"www.conf", vhosts);
	}

	private void initialisiereVHosts() {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+", initialisiereVHosts()");
		Datei vhosts;
		StringTokenizer tokenizer;
		String line;
		Dateisystem dateisystem;

		vHostArray = new String[5][2];
		int row=0;
		int col=0;
		
		dateisystem = getSystemSoftware().getDateisystem();
		vhosts = dateisystem.holeDatei(dateisystem.holeRootPfad()+Dateisystem.FILE_SEPARATOR+"www.conf"+Dateisystem.FILE_SEPARATOR+"vhosts");

		if (vhosts != null) {
			tokenizer = new StringTokenizer(vhosts.getDateiInhalt(), "\n");

			while (tokenizer.hasMoreTokens()) {
				line = tokenizer.nextToken().trim();
				vHostArray[row][col] = line;
				if(col==0) {
					col++;
				}
				else {
					col=0;
					row++;
				}
			}
		}
		for (; row < 5; row++) {
			vHostArray[row][0] = "";
			vHostArray[row][1] = "";
		}
	}

}
