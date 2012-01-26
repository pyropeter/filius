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
package filius.rahmenprogramm.nachrichten;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

import filius.Main;
import filius.rahmenprogramm.I18n;
import filius.software.netzzugangsschicht.EthernetFrame;
import filius.software.transportschicht.TcpSegment;
import filius.software.transportschicht.UdpSegment;
import filius.software.vermittlungsschicht.ArpPaket;
import filius.software.vermittlungsschicht.IcmpPaket;
import filius.software.vermittlungsschicht.IpPaket;

public class Lauscher implements I18n {

	public static final String ETHERNET = "", ARP = "ARP", IP = "IP", ICMP="ICMP",
			TCP = "TCP", UDP = "UDP";

	public static final String HTTP = "HTTP", SMTP = "SMTP", POP = "POP3",
			DNS = "DNS", DHCP = "DHCP";

	public static final String[] SPALTEN = { messages.getString("rp_lauscher_msg1"),
		messages.getString("rp_lauscher_msg2"),
		messages.getString("rp_lauscher_msg3"),
		messages.getString("rp_lauscher_msg4"),
		messages.getString("rp_lauscher_msg5"),
		messages.getString("rp_lauscher_msg6"),
		messages.getString("rp_lauscher_msg7") };

	public static final String[] PROTOKOLL_SCHICHTEN = { messages.getString("rp_lauscher_msg8"),
		messages.getString("rp_lauscher_msg9"),
		messages.getString("rp_lauscher_msg10"),
		messages.getString("rp_lauscher_msg11") };

	/** Singleton */
	private static Lauscher lauscher = null;

	private HashMap<String, LinkedList<LauscherBeobachter>> beobachter;

	private HashMap<String, LinkedList<Object[]>> datenEinheiten;

	private Lauscher() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", constr: Lauscher()");
		beobachter = new HashMap<String, LinkedList<LauscherBeobachter>>();
		reset();
	}

	public void reset() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", reset()");
		//lauscher = null;
		datenEinheiten = new HashMap<String, LinkedList<Object[]>>();
		this.benachrichtigeBeobachter(null);
	}

	public static Lauscher getLauscher() {
		if (lauscher == null) {
			lauscher = new Lauscher();
		}

		return lauscher;
	}

	public void addBeobachter(String rechnerId, LauscherBeobachter beobachter) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", addBeobachter("+rechnerId+","+beobachter+")");
		LinkedList<LauscherBeobachter> liste;

		liste = this.beobachter.get(rechnerId);
		if (liste == null) {
			liste = new LinkedList<LauscherBeobachter>();
			this.beobachter.put(rechnerId, liste);
		}
		liste.add(beobachter);
	}

	private void benachrichtigeBeobachter(String rechnerId) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", benachrichtigeBeobachter("+rechnerId+")");
		LinkedList<LauscherBeobachter> liste;
		Collection<LinkedList<LauscherBeobachter>> collection;
		ListIterator<LauscherBeobachter> it;
		Iterator<LinkedList<LauscherBeobachter>> valueIt;

		if (rechnerId == null) {
			collection = this.beobachter.values();
			liste = new LinkedList<LauscherBeobachter>();
			valueIt = collection.iterator();
			while (valueIt.hasNext()) {
				liste.addAll((LinkedList<LauscherBeobachter>)valueIt.next());
			}
		}
		else {
			liste = this.beobachter.get(rechnerId);
		}
//		Main.debug.println("\tbenachrichtigeBeobachter for "+rechnerId+" gave list "+(liste==null ? "<null>" : liste.toString()));
		if (liste != null) {
			it = liste.listIterator();
			while (it.hasNext()) {
				((LauscherBeobachter) it.next()).update();
			}
		}

	}

	/**
	 * Hinzufuegen von einem EthernetFrame zu den Daten
	 *
	 * @param interfaceId
	 *            Uebergeben wird der String des NetzwerkInterface nach Aufruf
	 *            von toString()
	 * @param frame
	 */
	public void addDatenEinheit(String interfaceId, EthernetFrame frame) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", addDatenEinheit("+interfaceId+","+frame+")");
		LinkedList<Object[]> liste;
		Object[] frameMitZeitstempel;

		//Main.debug.println("Lauscher: neuer Frame von " + interfaceId
//				+ " hinzugefuegt");
		frameMitZeitstempel = new Object[2];
		frameMitZeitstempel[0] = new Long(System.currentTimeMillis());
		frameMitZeitstempel[1] = frame;

		liste = (LinkedList<Object[]>) datenEinheiten.get(interfaceId);
		if (liste == null) {
			liste = new LinkedList<Object[]>();
			liste.add(frameMitZeitstempel);
		}
		else {
			liste.addLast(frameMitZeitstempel);
		}

		datenEinheiten.put(interfaceId, liste);
		benachrichtigeBeobachter(interfaceId);
	}

	public Object[][] getDaten(String interfaceId) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", getDaten("+interfaceId+")");
		Vector<Object[]> vector;
		Object[][] daten;

		vector = datenVorbereiten(interfaceId);
		if (vector == null) {
			daten = new Object[0][SPALTEN.length];
			return daten;
		}
		else {
			daten = new Object[vector.size()][SPALTEN.length];
			for (int i = 0; i < vector.size(); i++) {
				daten[i] = (Object[]) vector.elementAt(i);
			}
			return daten;
		}
	}

	public void print(String interfaceId) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", print("+interfaceId+")");
		Object[][] daten;

		daten = getDaten(interfaceId);
		for (int i = 0; i < daten.length; i++) {
			for (int j = 0; j < daten[i].length; j++) {
				Main.debug.print("\t" + daten[i][j]);
			}
			Main.debug.println();
		}
	}

	private Vector<Object[]> datenVorbereiten(String interfaceId) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", datenVorbereiten("+interfaceId+")");
		Vector<Object[]> daten;
		LinkedList<Object[]> liste;
		Object[] frameMitZeitstempel, neuerEintrag;
		ListIterator<Object[]> it;
		Calendar zeit;
		EthernetFrame frame;
		IpPaket ipPaket;
		IcmpPaket icmpPaket;
		ArpPaket arpPaket;
		TcpSegment tcpSeg = null;
		UdpSegment udpSeg = null;
		String timestampStr = "";

		liste = datenEinheiten.get(interfaceId);
		if (liste == null) {
			return null;
		}
		else {
			daten = new Vector<Object[]>();

			it = liste.listIterator();
			for (int i = 1; it.hasNext(); i++) {
				frameMitZeitstempel = (Object[]) it.next();
				neuerEintrag = new Object[SPALTEN.length];
				neuerEintrag[0] = "" + i;

				zeit = new GregorianCalendar();
				zeit.setTimeInMillis(((Long) frameMitZeitstempel[0])
						.longValue());
				timestampStr =  (zeit.get(Calendar.HOUR_OF_DAY)<10 ? "0"+zeit.get(Calendar.HOUR_OF_DAY) : zeit.get(Calendar.HOUR_OF_DAY)) 
				  + ":"
				  + (zeit.get(Calendar.MINUTE)<10 ? "0"+zeit.get(Calendar.MINUTE) : zeit.get(Calendar.MINUTE))
				  + ":"
				  + (zeit.get(Calendar.SECOND)<10 ? "0"+zeit.get(Calendar.SECOND) : zeit.get(Calendar.SECOND))
				  + "."
				  + (zeit.get(Calendar.MILLISECOND)<10 ? "00"+zeit.get(Calendar.MILLISECOND) :
					  (zeit.get(Calendar.MILLISECOND)<100 ? "0"+zeit.get(Calendar.MILLISECOND) : 
						  zeit.get(Calendar.MILLISECOND)));

				neuerEintrag[1] = timestampStr;
				frame = (EthernetFrame) frameMitZeitstempel[1];
				neuerEintrag[2] = frame.getQuellMacAdresse();
				neuerEintrag[3] = frame.getZielMacAdresse();
				neuerEintrag[4] = ETHERNET;
				neuerEintrag[5] = PROTOKOLL_SCHICHTEN[0];
				neuerEintrag[6] = frame.getTyp();

				daten.addElement(neuerEintrag);

				neuerEintrag = new Object[SPALTEN.length];
				neuerEintrag[0] = "" + i;

				neuerEintrag[1] = timestampStr;

				if (frame.getTyp().equals(EthernetFrame.IP) && !frame.isICMP()) {
					ipPaket = (IpPaket) frame.getDaten();
					neuerEintrag[2] = ipPaket.getSender();
					neuerEintrag[3] = ipPaket.getEmpfaenger();
					neuerEintrag[4] = IP;
					neuerEintrag[5] = PROTOKOLL_SCHICHTEN[1];
					neuerEintrag[6] = messages.getString("rp_lauscher_msg12") + ipPaket.getProtocol()
							+ ", TTL: " + ipPaket.getTtl();
					daten.addElement(neuerEintrag);

					neuerEintrag = new Object[SPALTEN.length];
					neuerEintrag[0] = "" + i;

					neuerEintrag[1] = timestampStr;

					if (ipPaket.getProtocol() == IpPaket.TCP) {
						tcpSeg = (TcpSegment) ipPaket.getSegment();
						neuerEintrag[2] = tcpSeg.getQuellPort();
						neuerEintrag[3] = tcpSeg.getZielPort();
						neuerEintrag[4] = TCP;
						neuerEintrag[5] = PROTOKOLL_SCHICHTEN[2];
						if (tcpSeg.isAck() && !tcpSeg.isSyn()
								&& !tcpSeg.isFin()) {
							neuerEintrag[6] = "ACK: " + tcpSeg.getAckNummer();
						}
						else {
							if (tcpSeg.isSyn()) {
								neuerEintrag[6] = "SYN"; 	
							}
							else if (tcpSeg.isFin()) {
								neuerEintrag[6] = "FIN"; 
							}							
							if (tcpSeg.isAck()) {
								neuerEintrag[6] = neuerEintrag[6] + ", ACK:" + tcpSeg.getAckNummer();
							}
								
							// Sequenznummer nur, wenn SYN-Segment
							// oder Nutzdaten-Segment
							if (tcpSeg.isSyn()
									|| (!tcpSeg.isAck() && !tcpSeg.isFin())) {
								neuerEintrag[6] = ((neuerEintrag[6] == null) ? "" : neuerEintrag[6]) 
								                + (tcpSeg.isSyn() ? ", " : "")
								                + "SEQ: "
								                + tcpSeg.getSeqNummer();
							}
						}
					}
					else if (ipPaket.getProtocol() == IpPaket.UDP) {
						udpSeg = (UdpSegment) ipPaket.getSegment();
						neuerEintrag[2] = udpSeg.getQuellPort();
						neuerEintrag[3] = udpSeg.getZielPort();
						neuerEintrag[4] = UDP;
						neuerEintrag[5] = PROTOKOLL_SCHICHTEN[2];
						neuerEintrag[6] = "";
					}
					else {
						Main.debug.println("ERROR ("+this.hashCode()+"): Protokoll der Transportschicht ("
										+ ipPaket.getProtocol()
										+ ") nicht bekannt.");
					}
					daten.addElement(neuerEintrag);

					neuerEintrag = new Object[SPALTEN.length];
					neuerEintrag[0] = "" + i;

					neuerEintrag[1] = timestampStr;
					neuerEintrag[2] = "";
					neuerEintrag[3] = "";
					neuerEintrag[4] = "";
					neuerEintrag[5] = PROTOKOLL_SCHICHTEN[3];
					if (ipPaket.getProtocol() == IpPaket.TCP) {
						neuerEintrag[6] = tcpSeg.getDaten();
						if(neuerEintrag[6] != null) 
							neuerEintrag[6] = ((String) neuerEintrag[6]).replace('\n', ' ');
					}
					else if (ipPaket.getProtocol() == IpPaket.UDP) {
						neuerEintrag[6] = udpSeg.getDaten();
						if(neuerEintrag[6] != null) 
							neuerEintrag[6] = ((String) neuerEintrag[6]).replace('\n', ' ');
					}

					if (neuerEintrag[6] != null
							&& !((String) neuerEintrag[6]).trim().equals(""))
						daten.addElement(neuerEintrag);
				}
				else if (frame.getTyp().equals(EthernetFrame.ARP)) {
					arpPaket = (ArpPaket) frame.getDaten();
					neuerEintrag[2] = arpPaket.getQuellIp();
					neuerEintrag[3] = arpPaket.getZielIp();
					neuerEintrag[4] = ARP;
					neuerEintrag[5] = PROTOKOLL_SCHICHTEN[1];
					if (arpPaket.getZielMacAdresse()
							.equalsIgnoreCase("ff:ff:ff:ff:ff:ff")) {
						neuerEintrag[6] = messages.getString("rp_lauscher_msg13")
								+ " " + arpPaket.getZielIp() + ", ";
					}
					else {
						neuerEintrag[6] = "";
					}
					neuerEintrag[6] = neuerEintrag[6] + arpPaket.getQuellIp()
							+ ": " + arpPaket.getQuellMacAdresse();

					daten.addElement(neuerEintrag);
				}
				else if (frame.getTyp().equals(EthernetFrame.IP) && frame.isICMP()) {
					icmpPaket = (IcmpPaket) frame.getDaten();
					neuerEintrag[2] = icmpPaket.getQuellIp();
					neuerEintrag[3] = icmpPaket.getZielIp();
					neuerEintrag[4] = ICMP;
					neuerEintrag[5] = PROTOKOLL_SCHICHTEN[1];
					if(icmpPaket.getIcmpType() == 8) {  // Echo Request
						neuerEintrag[6] = "ICMP Echo Request (ping)";
					}
					else {
						neuerEintrag[6] = "ICMP Echo Reply (pong)";
					}

					daten.addElement(neuerEintrag);
				}
			}
		}
		return daten;
	}
}
