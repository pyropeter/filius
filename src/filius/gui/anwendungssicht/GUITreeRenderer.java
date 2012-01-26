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
package filius.gui.anwendungssicht;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.*;

import filius.software.system.Datei;

class GUITreeRenderer extends DefaultTreeCellRenderer {
    private Icon dateiIcon, ordnerIcon;
    private boolean dateienAnzeigen;
    public GUITreeRenderer(Icon dateiIcon, Icon ordnerIcon) {
        this.dateiIcon = dateiIcon;
        this.ordnerIcon = ordnerIcon;
    }

    public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {

        super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);
        if (leaf && isDatei(value)) {
            setIcon(dateiIcon);
            setText(this.getDateiName(value));
            
        } else {
            setIcon(ordnerIcon);
        } 
        if (isDatei(value) && !isDateienAnzeigen())
        {
        	setVisible(false);
        	setText("");
        	setIcon(null);
        }
        return this;
    }
    protected boolean isDatei(Object value)
    {
    	DefaultMutableTreeNode node =
            (DefaultMutableTreeNode)value;
    	if (node.getUserObject().getClass().equals(Datei.class))
    	{
    		return true;
    	}
    	return false;
    }
    
    protected String getDateiName(Object value)
    {
    	DefaultMutableTreeNode node =
            (DefaultMutableTreeNode)value;
    	if (node.getUserObject().getClass().equals(Datei.class))
    	{
    		Datei datei = (Datei) node.getUserObject();
    		return datei.getName();
    	}
    	return "";
    }

	public boolean isDateienAnzeigen() {
		return dateienAnzeigen;
	}

	public void setDateienAnzeigen(boolean dateienAnzeigen) {
		this.dateienAnzeigen = dateienAnzeigen;
	}

}
