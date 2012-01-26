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
import java.util.zip.*;
import java.io.*;

import filius.Main;

public class ErzeugeZipDatei {


/**
 * @author weyer
 *  	Die Klasse kopiert den Inhalt eines Verzeichnisses in eine
 * 		Zip-Datei und speichert diese in diesem Verzeichnis.
 * @param dir  Verzeichnis, dessen Inhalt in eine Zip-Datei eingelesen werden soll.
 * @param zipFileName  Der Name, den die Zip-Datei haben soll.
 * @throws IOException
 */
	public ErzeugeZipDatei(String verzeichnis, String zipFileName) throws IOException {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", constr: ErzeugeZipDatei("+verzeichnis+","+zipFileName+")");
	 // Puffer zum Auslesen der Dateien. 	// Der vollständige Pfad zur zukünftigen Zip-Datei.
	 String dirFile = verzeichnis + zipFileName;
	 // Die Dateien werden in diese Datei geschrieben.
	 ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(dirFile));
	 // Das Verzeichnis, dessen Dateien gesichert werden sollen.
	 File dirZip = new File(verzeichnis);
	 zipDatei(dirZip, zipOut);
	 // Die Zip-Datei ist erzeugt.
	 zipOut.close();
	 }

	/**
	 * @author weyer
	 * @param dirZip
	 * @param zipOut
	 * @throws IOException
	 */
	public void zipDatei(File verzeichnisZip, ZipOutputStream zipOut) throws IOException {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", zipDatei("+verzeichnisZip+","+zipOut+")");
		 byte[] buf = new byte[4096];
		 // Alle Dateinamen aus dem Verzeichnis werden in des Array eingelesen.
		 File[] fileArray = verzeichnisZip.listFiles(); String fileName = "";
		 // Alle Dateien auslesen.
		 for (int i = 0; i < fileArray.length; i++) {
		 // Der Name der Datei wird ermittelt.
			 fileName = fileArray[i].getAbsolutePath();
			 // Zip-Dateien ignorieren.
			 if (fileName.endsWith(".zip")) continue;
			 if (fileArray[i].isDirectory()) {
				 zipDatei(fileArray[i], zipOut); } else {
					 // Die Datei wird zum Lesen geöffnet.
					 FileInputStream inFile = new FileInputStream(fileName);
					 // Info an Zip-Datei: Jetzt kommt neuer Eintrag.
					 zipOut.putNextEntry(new ZipEntry(fileName)); int len;
					 // Der Inhalt der Datei wird in die Zip-Datei kopiert.
					 while ((len = inFile.read(buf)) > 0) { zipOut.write(buf, 0, len); }
					 inFile.close(); }
			 }
	 }
}



