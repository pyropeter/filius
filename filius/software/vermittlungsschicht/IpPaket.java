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
package filius.software.vermittlungsschicht;

import java.io.Serializable;

/**
 * Diese Klasse umfasst die Attribute bzw. Felder eines IP-Pakets
 */
public class IpPaket implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int UDP = 17, TCP = 6;

	private int version, ihl, tos, totalLength;

	private int identification, offset;

	private boolean ff, df, mf;

	private int ttl, protocol, checksum;

	private String empfaenger, sender;

	private Object paket;

	public int getChecksum() {
		return checksum;
	}

	public void setChecksum(int checksum) {
		this.checksum = checksum;
	}

	public String getEmpfaenger() {
		return empfaenger;
	}

	public void setEmpfaenger(String empfaenger) {
		this.empfaenger = empfaenger;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public boolean isDf() {
		return df;
	}

	public void setDf(boolean df) {
		this.df = df;
	}

	public boolean isFf() {
		return ff;
	}

	public void setFf(boolean ff) {
		this.ff = ff;
	}

	public int getIdentification() {
		return identification;
	}

	public void setIdentification(int identification) {
		this.identification = identification;
	}

	public int getIhl() {
		return ihl;
	}

	public void setIhl(int ihl) {
		this.ihl = ihl;
	}

	public boolean isMf() {
		return mf;
	}

	public void setMf(boolean mf) {
		this.mf = mf;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getProtocol() {
		return protocol;
	}

	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}

	public int getTos() {
		return tos;
	}

	public void setTos(int tos) {
		this.tos = tos;
	}

	public int getTotalLength() {
		return totalLength;
	}

	public void setTotalLength(int totalLength) {
		this.totalLength = totalLength;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Object getSegment() {
		return paket;
	}

	public void setSegment(Object paket) {
		this.paket = paket;
	}
	
	public String toString() {
		return "["
		     + "version="+version+", "
		     + "ihl="+ihl+", "
		     + "tos="+tos+", "
		     + "totalLength="+totalLength+", "
		     + "identification="+identification+", "
		     + "offset="+offset+", "
		     + "ff="+ff+", "
		     + "df="+df+", "
		     + "mf="+mf+", "
		     + "ttl="+ttl+", "
		     + "protocol="+protocol+", "
		     + "checksum="+checksum+", "
		     + "empfaenger="+empfaenger+", "
		     + "sender="+sender
			 + "]";
	}
}
