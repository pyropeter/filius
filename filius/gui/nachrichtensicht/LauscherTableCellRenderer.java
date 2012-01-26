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
package filius.gui.nachrichtensicht;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;

import filius.rahmenprogramm.nachrichten.Lauscher;

public class LauscherTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)  {
		String schichtString;
		int schicht = 5;
		int lfdNr = 0;
		int tblCols = table.getColumnCount();
		int tblRows = table.getRowCount();

		if (tblRows>row && tblCols>NachrichtenTabelle.SCHICHT_SPALTE &&
				table.getValueAt(row, NachrichtenTabelle.SCHICHT_SPALTE) != null)
			schichtString = table.getValueAt(row, NachrichtenTabelle.SCHICHT_SPALTE).toString();
		else schichtString = "";

		if (table.getValueAt(row, 0) != null) {
			try {
				lfdNr = Integer.parseInt((String) table.getValueAt(row, 0));
			}
			catch (Exception e) {}
		}


		for (int i=0; i<Lauscher.PROTOKOLL_SCHICHTEN.length; i++) {
			if (schichtString.equals(Lauscher.PROTOKOLL_SCHICHTEN[i])) schicht = i;
		}

		switch (schicht) {
		case 0:
			setForeground(Color.BLACK);
//			if (lfdNr%2==0) 
				setBackground(new Color(0.9f, 0.9f, 0.9f));
//			else setBackground(new Color(0.8f, 0.8f, 0.8f));
			break;
		case 1:
			setForeground(Color.BLACK);
			if (lfdNr%2==0) setBackground(new Color(0.3f, 1f, 0.3f));
			else setBackground(new Color(0.2f, 1f, 0.2f));
			break;
		case 2:
			setForeground(Color.BLACK);
			setBackground(Color.CYAN);
			setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
			break;
		case 3:
			setForeground(Color.WHITE);
			setBackground(Color.BLUE);
			setFont(new Font(Font.DIALOG, Font.BOLD, 12));
			break;
		default:
			setBackground(Color.DARK_GRAY);
		}

		switch (column) {
		case 0:
			setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
			break;
		case 1:
			setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
			break;
		case 2:
		case 3:
			setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
			break;
		case 4:
			setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
			break;
		case 5:
			setFont(new Font(Font.DIALOG, Font.BOLD + Font.ITALIC, 12));
			break;
		case 6:
			setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
			break;
		default:
			setFont(new Font(Font.DIALOG, Font.ITALIC, 12));
		}

		if (value != null) setText(value.toString());
		else setText("");

		return this;
	}
}
