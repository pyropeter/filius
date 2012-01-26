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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.tree.DefaultMutableTreeNode;

import filius.Main;
import filius.rahmenprogramm.Base64;
import filius.rahmenprogramm.I18n;
import filius.software.Anwendung;
import filius.software.system.Datei;

public class FileExplorer extends Anwendung implements I18n {

	//Betriebssystem betriebssystem;
	private HashMap<String, String> fileTypeMap;


	public FileExplorer(//Betriebssystem betriebssystem
			)	{
		super();
		Main.debug.println("INVOKED-2 ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (FileExplorer), constr: FileExplorer()");
		this.getFileTypeMap();
	}


	public void beenden() {
		// TODO Auto-generated method stub
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (FileExplorer), beenden()");
	}



	public void starten() {
		// TODO Auto-generated method stub
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (FileExplorer), starten()");
	}

	/**
	 * Diese Funktion importiert die ausgewaehlte Datei in das Betriebssystem des Rechners. Dabei
	 * wird die Datei Base64 kodiert, um sie als String verarbeiten zu koennen!
	 *
	 * @param dateiname
	 * @return
	 */
	public String addFile(String pfadname, String dateiname, DefaultMutableTreeNode ordner, String neuerName)
	{
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (FileExplorer), addFile("+pfadname+","+dateiname+","+ordner+","+neuerName+")");
		String ergebnis = messages.getString("sw_fileexplorer_msg1");
		java.io.File file = new java.io.File(pfadname+dateiname);
		if(!file.exists() || file.length() > 150000) {     // max filesize 150.000 Bytes
			return ergebnis +"\n"+ messages.getString("sw_fileexplorer_msg3");
		}

		if (ordner.getUserObject().getClass().equals(Datei.class))
		{
			Main.debug.println("ERROR ("+this.hashCode()+"): Es können keine Dateien in Dateien angeleget werden!");
		}
		else
		{
			if(neuerName.equals("")) neuerName="noName";
			String dateityp = this.getFileType(dateiname);
			Datei tempDatei;
			if (dateityp!=null && dateityp.equals("text"))
			{
				String txtInhalt = "";
			    try {
			        BufferedReader in = new BufferedReader(new FileReader(pfadname+dateiname));
			        String str;
			        while ((str = in.readLine()) != null) {
			            txtInhalt += str + "\r\n";
			        }
			        in.close();
					ergebnis = messages.getString("sw_fileexplorer_msg2");
			    }
			    catch (IOException e) {
			    	e.printStackTrace(Main.debug);
			    }
			    tempDatei = new Datei(neuerName,dateityp,txtInhalt);
			}
			else {
				tempDatei = new Datei(neuerName,dateityp,Base64.encodeFromFile(pfadname+dateiname));
				tempDatei.setSize(file.length());
				ergebnis = messages.getString("sw_fileexplorer_msg2");
			}				
			getSystemSoftware().getDateisystem().speicherDatei(ordner, tempDatei);
		}

		return ergebnis;
	}

	/**
	 * Ermittelt an Hand der Dateiendung den Dateitypen, damit dieser im Dateikonstruktor gesetzt werden kann.
	 * Es werden folgende Typen unterstuetzt und entsprechend gesetzt. Die Dateitypen werden in der Konfigurationsdate
	 * filetypes.txt festgelegt.
	 *
	 * @param dateiname Der zu ueberpruefende Dateiname
	 * @return Typ der Datei als String
	 */
	public String getFileType(String dateiname)
	{
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (FileExplorer), getFileType("+dateiname+")");
		String tmpType = "";
		/* Schritt 1: Die Datei wird in LowerCase konvertiert und der letzte Token wird gesucht */
		dateiname = dateiname.toLowerCase();
		StringTokenizer st = new StringTokenizer(dateiname,".");
		//Main.debug.println("Die Datei hat " + st.countTokens() + " Tokens!");
		int counting = st.countTokens();
		if(counting>1)
		{
			for(int i=1;i<counting;i++)
			{
				st.nextToken();
			}
			String a = st.nextToken();
			tmpType = fileTypeMap.get(a);
		}
		else
		{
			tmpType = "text";
		}
		//Main.debug.println("Der Dateityp ist: " + tmpType);
		return tmpType;
	}

	/**
	 * Liest einmalig die definierten Filetypen ein, damit diese beim Importieren
	 * der Dateien passend zugewiesen werden koennen. Wird einmalig im Konstruktor
	 * des FileImporters aufgerufen.
	 *
	 * @author Johannes Bade & Thomas Gerding
	 *
	 */
	public void getFileTypeMap()
	{
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (FileExplorer), getFileTypeMap()");
		fileTypeMap = new HashMap<String, String>();
		RandomAccessFile configFile;
	    try {
			configFile = new RandomAccessFile( "config/filetypes.txt", "r" );
			for ( String line; (line=configFile.readLine()) != null; )
			  {
				  StringTokenizer stx = new StringTokenizer(line,";");
				  String tmpType = stx.nextToken();
				  StringTokenizer sty = new StringTokenizer(stx.nextToken(),",");

				  while(sty.hasMoreElements())
				  {
					  fileTypeMap.put(sty.nextToken(), tmpType);
				  }



			  }
		}
	    catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(Main.debug);
		}
	    catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(Main.debug);
		}

	    //Main.debug.println(fileTypeMap);

	}


}
