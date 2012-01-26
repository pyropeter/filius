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
package filius.software.dhcp;

import filius.rahmenprogramm.I18n;

/**
 *
 * @author Johannes Klebert
 *
 */
public class IPEintrag implements I18n {

	private String ip;
	private String mac; // hiermit ist die mac-adresse gemeint.
	private long ttl; // time to life in miliseks.

	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}



	public long getTtl() {
		return ttl;
	}

	public void setTtl(long ttl) {
		this.ttl = ttl;
	}

	public String getMAC() {
		return mac;
	}

	public void setMAC(String owner) {
		this.mac = owner;
	}

	public String toString()
	{
		return messages.getString("sw_ipeintrag_msg1")+mac+ messages.getString("sw_ipeintrag_msg2")+ip;
	}

}
