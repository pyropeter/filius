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
package filius.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import filius.Main;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;

public class GUIHilfe implements I18n {

	private JDialog jf;
	private static GUIHilfe ref = null;
	private JScrollPane spHtmlScroller;
	private JEditorPane epHtml;

	private GUIHilfe()
	{
		JFrame hauptFrame = JMainFrame.getJMainFrame();
		jf = new JDialog(hauptFrame, messages.getString("guihilfe_msg1"), false);
		ImageIcon frameIcon = new ImageIcon(getClass().getResource("/gfx/allgemein/hilfe.png"));
		jf.setIconImage(frameIcon.getImage());


		epHtml = new JEditorPane();

		epHtml.setContentType("text/html");
		epHtml.setText(messages.getString("guihilfe_msg2"));
		laden("entwurfsmodus");
		spHtmlScroller = new JScrollPane(epHtml);

		jf.getContentPane().add(spHtmlScroller, BorderLayout.CENTER);
	}


	public static GUIHilfe getGUIHilfe()
	{
		if(ref == null)
		{
		 ref = new GUIHilfe();
		}

		return ref;
	}

	public void anzeigen()
	{
		int breite=350, hoehe=600, x, y;
		int absBreite, absHoehe;

		absBreite = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		absHoehe = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

		JFrame hauptFrame = JMainFrame.getJMainFrame();
		x = hauptFrame.getX()+hauptFrame.getWidth();
		y = hauptFrame.getY();

		if (y+hoehe > absHoehe) y=0;
		if (x+breite > absBreite && x+breite-50 < absBreite) {
			breite = absBreite - x;
		}
		else if (x+breite > absBreite) {
			breite = breite - 50;
			x = absBreite-breite;
		}

		jf.setBounds(x,y,breite,hoehe);

		jf.setVisible(true);

	}

	public void laden(String modus)
	{
		File file;
		if (modus.equalsIgnoreCase("entwurfsmodus")) {
		  file = new File(Information.getInformation().getProgrammPfad()+"hilfe"+File.separator + messages.getString("hilfedatei_entwurf"));
		}
		else {
			file = new File(Information.getInformation().getProgrammPfad()+"hilfe"+File.separator + messages.getString("hilfedatei_simulation"));
		}
		try {
			StringBuffer sb = new StringBuffer((int) file.length()+5);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			char[] chars = new char[(int) file.length()+5];
			int numRead = 0;
			while( (numRead = reader.read(chars)) > -1){
				sb.append(String.valueOf(chars));	
			}
			reader.close();
			String newText = sb.toString();
//			newText = newText.replaceAll("hilfe/gfx", Information.getInformation().getRelativePathToProgramDir()+"hilfe/gfx");
			String newPath = Information.getInformation().getRelativePathToProgramDir();
//			Main.debug.println("DEBUG: Hilfe, laden:   (1) newPath='"+newPath+"'");
			if(File.separator.equals("\\")) {
				newPath = newPath.replace('\\', '/');
//				newPath = newPath.replaceAll("/", "\\\\\\\\");
			}
//			Main.debug.println("DEBUG: Hilfe, laden:   (2) newPath='"+newPath+"'");
			newText = newText.replaceAll("hilfe/gfx/", newPath+"hilfe/gfx/");
			epHtml.read(new java.io.StringReader(newText), null);
			
			epHtml.setCaretPosition(0);
//			Main.debug.println("DEBUG: Hilfe, laden:\n"+epHtml.getText());
		} catch (FileNotFoundException e) {
			e.printStackTrace(Main.debug);
		} catch (IOException e) {
			e.printStackTrace(Main.debug);
		}
	}
}
