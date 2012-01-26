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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import filius.Main;

public class LadeZipDatei {

	public void extrahierenArchiv(File archiv, File zielDir) throws Exception {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", extrahiereArchiv("+archiv+","+zielDir+")");
		if (!zielDir.exists()) {
			zielDir.mkdir();
		}

		ZipFile zipFile = new ZipFile(archiv);
		Enumeration eingaenge = zipFile.entries();

		byte[] buffer = new byte[16384];
		int laenge;
		while (eingaenge.hasMoreElements()) {
			ZipEntry eingang = (ZipEntry) eingaenge.nextElement();

			String eingangFileName = eingang.getName();

			File verzeichnis = baueVerzeichnisHierarchieFuer(eingangFileName, zielDir);
			if (!verzeichnis.exists()) {
				verzeichnis.mkdirs();
			}

			if (!eingang.isDirectory()) {
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(new File(zielDir, eingangFileName)));

				BufferedInputStream bis = new BufferedInputStream(zipFile
						.getInputStream(eingang));

				while ((laenge = bis.read(buffer)) > 0) {
					bos.write(buffer, 0, laenge);
				}

				bos.flush();
				bos.close();
				bis.close();
			}
		}
	}

	private File baueVerzeichnisHierarchieFuer(String eingangName, File zielDir) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", baueVerzeichnisHierarchieFuer("+eingangName+","+zielDir+")");
		int lastIndex = eingangName.lastIndexOf('/');
		String internalPathToEntry = eingangName.substring(0, lastIndex + 1);
		return new File(zielDir, internalPathToEntry);
	}
}
