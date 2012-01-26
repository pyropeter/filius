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
package filius.software.transportschicht;

import java.io.Serializable;

/**
 *
 * @author carsten
 *
 */
public class TcpSegment extends Segment implements Serializable {

	private static final long serialVersionUID = 1L;
	private long seqNummer, ackNummer;
	private int dataOffset;
	private int reservedField = 0;
	private boolean urg = false;
	private boolean ack = false;
	private boolean psh = false;
	private boolean rst = false;
	private boolean syn = false;
	private boolean fin = false;
	private int window;
	private int urgentPointer;

	public TcpSegment(){

	}

	public boolean isAck() {
		return ack;
	}

	public void setAck(boolean ack) {
		this.ack = ack;
	}

	public long getAckNummer() {
		return ackNummer;
	}

	public void setAckNummer(long ackNummer) {
		this.ackNummer = ackNummer;
	}

	public int getDataOffset() {
		return dataOffset;
	}

	public void setDataOffset(int dataOffset) {
		this.dataOffset = dataOffset;
	}

	public boolean isFin() {
		return fin;
	}

	public void setFin(boolean fin) {
		this.fin = fin;
	}

	public boolean isPush() {
		return psh;
	}

	public void setPush(boolean psh) {
		this.psh = psh;
	}

	public int getReservedField() {
		return reservedField;
	}

	public void setReservedField(int reservedField) {
		this.reservedField = reservedField;
	}

	public boolean isRst() {
		return rst;
	}

	public void setRst(boolean rst) {
		this.rst = rst;
	}

	public long getSeqNummer() {
		return seqNummer;
	}

	public void setSeqNummer(long seqNummer) {
		this.seqNummer = seqNummer;
	}

	public boolean isSyn() {
		return syn;
	}

	public void setSyn(boolean syn) {
		this.syn = syn;
	}

	public boolean isUrg() {
		return urg;
	}

	public void setUrg(boolean urg) {
		this.urg = urg;
	}

	public int getUrgentPointer() {
		return urgentPointer;
	}

	public void setUrgentPointer(int urgentPointer) {
		this.urgentPointer = urgentPointer;
	}

	public int getWindow() {
		return window;
	}

	public void setWindow(int window) {
		this.window = window;
	}
}
