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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;

import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;


/* Basiert auf SplashScreen von Tony Colston, JavaWorld.com, 11/17/00 */
public class SplashScreen extends JWindow implements I18n
{
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public SplashScreen(String filename, Frame f)
    {
        super(f);
        JLabel l = new JLabel(new ImageIcon(getClass().getResource("/"+filename)));
        getContentPane().add(l, BorderLayout.CENTER);

        JLabel info = new JLabel(" Version "+Information.getVersion()+", " + messages.getString("splashscreen_msg1"));
        info.setForeground(Color.BLACK);
        info.setFont(new Font("Dialog", Font.PLAIN, 10));
        getContentPane().add(info, BorderLayout.SOUTH);
        pack();

        Dimension screenSize =
          Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = l.getPreferredSize();
        setLocation(screenSize.width/2 - (labelSize.width/2),
                    screenSize.height/2 - (labelSize.height/2));


    }
}
