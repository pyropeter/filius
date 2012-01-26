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

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import filius.software.dns.DNSServer;
import filius.software.www.WebServer;

public class JTableEditable extends JTable {

	private static final long serialVersionUID = 1L;
	private boolean editable;
	
	// optional parameter for identifying the table, e.g., whether storing MX or A entries for DNS
	private String typeID = null;
	
	private Object parentGUI;

	public JTableEditable(TableModel model, boolean editable) {
		super(model);
		setEditable(editable);
	}
	
	public JTableEditable(TableModel model, boolean editable, String type) {
		super(model);
		setEditable(editable);
		this.typeID = type;
	}

	public void setParentGUI(Object parent) {
		this.parentGUI = parent;
	}

	/**
	 * @return the editable
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * @param editable the editable to set
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public boolean isCellEditable(int row, int column) {
		return editable;
	}
	
    public void editingStopped(ChangeEvent e) {
//		Main.debug.println("editingStopped, source='"+e.getSource()+"'");
		
        TableCellEditor editor = getCellEditor();
        if (editor != null) {
            // Take in the new value
            String value = (String) editor.getCellEditorValue();
            if(value==null) value="";
            setValueAt(value, editingRow, editingColumn);

            // store value in DNS records
            if(parentGUI instanceof GUIApplicationDNSServerWindow) {
            	if (typeID!=null && typeID.equals("A")) {
			        if (editingColumn == 0) {
			        	((DNSServer) ((GUIApplicationDNSServerWindow) parentGUI).holeAnwendung()).changeSingleEntry(editingRow, 0, filius.software.dns.ResourceRecord.ADDRESS, value);
			        }
			        else {
			        	((DNSServer) ((GUIApplicationDNSServerWindow) parentGUI).holeAnwendung()).changeSingleEntry(editingRow, 3, filius.software.dns.ResourceRecord.ADDRESS, value);
			        }
            	}
            	else if(typeID!=null && typeID.equals("MX")) {
			        if (editingColumn == 0) {
			        	((DNSServer) ((GUIApplicationDNSServerWindow) parentGUI).holeAnwendung()).changeSingleEntry(editingRow, 0, filius.software.dns.ResourceRecord.MAIL_EXCHANGE, value);
			        }
			        else {
			        	((DNSServer) ((GUIApplicationDNSServerWindow) parentGUI).holeAnwendung()).changeSingleEntry(editingRow, 3, filius.software.dns.ResourceRecord.MAIL_EXCHANGE, value);
			        }
            	}
            }
            if(parentGUI instanceof GUIApplicationWebServerWindow) {
				if(typeID!=null && typeID.equals("WWW")) {
					((WebServer) ((GUIApplicationWebServerWindow) parentGUI).holeAnwendung()).changeVHostTable(editingRow,editingColumn,value);
				}
            }
            removeEditor();
        }
    }
}
