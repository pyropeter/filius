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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.InternalFrameEvent;

import filius.Main;
import filius.software.lokal.Terminal;
import filius.software.system.Dateisystem;

import java.util.ArrayList;

/**
 * Applikationsfenster fuer ein Terminal
 *
 * @author Johannes Bade & Thomas Gerding
 *
 */
public class GUIApplicationTerminalWindow extends GUIApplicationWindow {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea terminalField;
	private JPanel backPanel;
	private JTextField inputField;
	private JLabel inputLabel;
	private JScrollPane tpPane;
	
	private boolean jobRunning;
	private String enteredCommand;
	private String[] enteredParameters;
	
	private boolean multipleObserverEvents;

	ArrayList<String> terminalCommandList = new ArrayList<String>(); // für pfeil-nach-oben-holt-letzten-befehl-wieder
	int terminalCommandListStep = -1;


	public GUIApplicationTerminalWindow(GUIDesktopPanel desktop, String appName) {
		super(desktop, appName);
		this.setMaximizable(false);
		this.setResizable(false);
		jobRunning = false;
		multipleObserverEvents = false;

		terminalField = new JTextArea("");
		terminalField.setEditable(false);
		terminalField.setCaretColor(new Color(222,222,222));
		terminalField.setForeground(new Color(222,222,222));
		terminalField.setBackground(new Color(0,0,0));
		terminalField.setFont(new Font("Courier New",Font.PLAIN,11));
		terminalField.setFocusable(false);
		terminalField.setBorder(null);

		tpPane = new JScrollPane(terminalField);  // make textfield scrollable
		tpPane.setBorder(null);
		tpPane.setBackground(new Color(0,0,0));
		tpPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER); // do not show vert. scrollbar
		tpPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); // do not show hor. scrollbar
		
		inputField = new JTextField("");
		inputField.setEditable(true);
		inputField.setBackground(new Color(0,0,0));
		inputField.setForeground(new Color(222,222,222));
		inputField.setCaretColor(new Color(222,222,222));
		inputField.setBorder(null);
		inputField.setFont(new Font("Courier New",Font.PLAIN,11));

		inputField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					terminalCommandListStep = -1; // lass uns doch besser wieder von unten/vorne beginnen
					if(!(inputField.getText().isEmpty() || inputField.getText().replaceAll(" ", "").isEmpty())) {  // only process non-empty input
						//Main.debug.println("DEBUG: "+getClass()+", keyPressed ('"+inputField.getText()+" + ENTER') event started");
						terminalField.append("\n"+inputLabel.getText()+inputField.getText()+"\n");
						StringTokenizer tk = new StringTokenizer(inputField.getText(), " ");
	
						/* Erstes Token enthaelt den Befehl*/
						enteredCommand = tk.nextToken();
	
						/* restliche Tokens werden in String Array geschrieben.
						 * Array wird sicherheitshalber mit mindestens 3 leeren Strings gefüllt! */
						enteredParameters = new String[3+tk.countTokens()];
						for (int i =0; i< 3+tk.countTokens(); i++)
						{
							enteredParameters[i] = new String();
						}
						int iti = 0;
						while (tk.hasMoreTokens())
						{
							enteredParameters[iti] =tk.nextToken();
							iti++;
						}
	
						if (enteredCommand.equals("exit"))
						{
							doDefaultCloseAction();
						}
						else if (enteredCommand.equals("reset")) {
							terminalField.setText( "" );
							for (int i=0; i<15; i++) { terminalField.append(" \n"); } // padding with new lines for bottom alignment of new output
							terminalField.append(  "================================================================\n"  );
							terminalField.append(messages.getString("sw_terminal_msg25")
									     + "================================================================"
									     + "\n");
						}
						else
						{
							inputLabel.setVisible(false);
							jobRunning = true;
							terminalCommandList.add(inputField.getText());
							((Terminal) holeAnwendung()).terminalEingabeAuswerten(enteredCommand,enteredParameters);
						}
					}
					else { terminalField.append("\n"); }
					//Main.debug.println("DEBUG: "+getClass()+", keyPressed ('"+inputField.getText()+" + ENTER') event finished");
					inputField.setText("");
				}
				if (e.getKeyCode() == KeyEvent.VK_C && e.getModifiers() == 2) { // [strg] + [c]
					System.out.println("INTERRUPT");
					((Terminal) holeAnwendung()).setInterrupt(true);
				}
				if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) { // 38 arrow-up / 40 arrow-down
					if (e.getKeyCode() == KeyEvent.VK_UP) {
						terminalCommandListStep++;
					}
					if (e.getKeyCode() == KeyEvent.VK_DOWN) {
						terminalCommandListStep--;
					}
					if (terminalCommandListStep < -1) {
						terminalCommandListStep = -1;
					}
					if (terminalCommandListStep >= terminalCommandList.size()) {
						terminalCommandListStep = terminalCommandList.size() - 1;
					}
					try {
						if (terminalCommandListStep != -1) {
							inputField.setText(terminalCommandList.get(terminalCommandList.size() - 1 - terminalCommandListStep));
						}
						else if (terminalCommandListStep == -1) {
							inputField.setText("");
						}
					}
					catch (IndexOutOfBoundsException eis) {
					
					}
				}
			}

			public void keyReleased(KeyEvent arg0) {
				
			}

			public void keyTyped(KeyEvent arg0) {

			}

		});


		inputLabel = new JLabel(">");
		inputLabel.setBackground(new Color(0,0,0));
		inputLabel.setForeground(new Color(222,222,222));
		inputLabel.setFont(new Font("Courier New",Font.PLAIN,11));

		Box terminalBox = Box.createHorizontalBox();
		terminalBox.setBackground(new Color(0,0,0));
		terminalBox.add(tpPane);  // terminalField embedded in ScrollPane
		terminalBox.setBorder(BorderFactory.createEmptyBorder(5,5,1,5));

		Box inputBox = Box.createHorizontalBox();
		inputBox.setBackground(new Color(0,0,0));
		inputBox.add(inputLabel);
		inputBox.add(Box.createHorizontalStrut(1));
		inputBox.add(inputField);
		inputBox.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));

		backPanel = new JPanel(new BorderLayout());
		backPanel.setBackground(new Color(0,0,0));
		backPanel.add(terminalBox, BorderLayout.CENTER);
		backPanel.add(inputBox, BorderLayout.SOUTH);
		this.getContentPane().add(backPanel);

		terminalField.setText( "" );

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
		this.tpPane.getVerticalScrollBar().setValue(this.tpPane.getVerticalScrollBar().getMaximum());
		
		pack();

		inputField.requestFocus();
		this.inputLabel.setText("root "+Dateisystem.absoluterPfad(((Terminal)holeAnwendung()).getAktuellerOrdner())+"> ");
	}

	public void setMultipleObserverEvents(boolean flag) {
	}
	
	public void windowActivated(WindowEvent e) {

	}

	public void windowClosing(WindowEvent e) {

	}

	public void windowDeactivated(WindowEvent e) {

	}

	public void windowDeiconified(WindowEvent e) {

	}

	public void windowIconified(WindowEvent e) {

	}

	public void windowOpened(WindowEvent e) {
	}

	public void internalFrameActivated(InternalFrameEvent e) {

	}

	public void internalFrameClosed(InternalFrameEvent e) {

	}

	public void internalFrameClosing(InternalFrameEvent e) {
		

	}

	public void internalFrameDeactivated(InternalFrameEvent e) {
		

	}

	public void internalFrameDeiconified(InternalFrameEvent e) {
		

	}

	public void internalFrameIconified(InternalFrameEvent e) {
		

	}

	public void internalFrameOpened(InternalFrameEvent e) {
		
	}

	public void update(Observable arg0, Object arg1) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (GUIApplicationTerminalWindow), update("+arg0+","+arg1+")");
		if (arg1 == null) return;
		if (jobRunning) {
			if (arg1 instanceof Boolean) { 
				multipleObserverEvents=((Boolean) arg1).booleanValue(); 
			}
			else {   // expect String
				this.terminalField.append(arg1.toString());
				try {
					// mini delay to let the terminalField reliably update its new height
					Thread.sleep(200);
				} catch (InterruptedException e) {}
				this.tpPane.repaint();
				this.tpPane.getVerticalScrollBar().setValue(this.tpPane.getVerticalScrollBar().getMaximum());
				if (!multipleObserverEvents) {  // is this observer call expected to be the last one for the current command, i.e., multipleOverserverEvents=false?
					this.inputLabel.setText("root "+Dateisystem.absoluterPfad(((Terminal)holeAnwendung()).getAktuellerOrdner())+"> ");
					this.inputLabel.setVisible(true);
					jobRunning=false;
				}
			}
		}
	}


}
