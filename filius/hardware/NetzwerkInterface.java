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
package filius.hardware;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import filius.Main;
import filius.rahmenprogramm.Information;

public class NetzwerkInterface implements Serializable{

	private static final long serialVersionUID = 1L;
	private String mac;
	private String ip;
	private String subnetzMaske;
	private String gateway;
	private String dns;
	private Port anschluss;


	public NetzwerkInterface() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (NetzwerkInterface), constr: NetzwerkInterface()");
		setMac(Information.getInformation().holeFreieMACAdresse());
		// set initial IP address to the same value for all new devices
		// QUESTION: Is this actually wanted for educational reasons? 
		//			 Or is it rather annoying to be enforced to change this address for each device?
		setIp("192.168.0.10");
		setSubnetzMaske("255.255.255.0");
		setGateway("");
		setDns("");
		anschluss = new Port(this);
		
		this.initPersistenceAttributes();
	}
	
	/** This method is used to mark the MAC address as transient property in order to avoid 
	 * use of the same MAC address if the same project is opened twice.
	 */
	private void initPersistenceAttributes() {
		BeanInfo info;
		try {
			info = Introspector.getBeanInfo(NetzwerkInterface.class);
			PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
			for (PropertyDescriptor descriptor : propertyDescriptors) {

				if (descriptor.getName().equals("mac")) {
					descriptor.setValue("transient", Boolean.TRUE);
					break;
				}
			}
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
	}

	public Port getPort() {
		return anschluss;
	}

	public void setPort (Port port) {
		this.anschluss = port;
	}

	/** IP-Adresse des DNS-Servers, der zur Aufloesung von Domainnamen
	 * verwendet wird.
	 *
	 * NOTE: die IP-Adresse des DNS-Servers ist kein Parameter des
	 *   Netzwerk-Interfaces, wird aber aus Gruenden der Kompatibilitaet hier
	 *   verwaltet. Jedes  Netzwerk-Interface der gleichen Komponente sollte
	 *   hier den gleichen Wert haben!
	 */
	public String getDns() {
		return dns;
	}

	/** IP-Adresse des DNS-Servers, der zur Aufloesung von Domainnamen
	 * verwendet wird.
	 *
	 * @deprecated die IP-Adresse des DNS-Servers ist kein Parameter des
	 *   Netzwerk-Interfaces, wird aber aus Gruenden der Kompatibilitaet hier
	 *   verwaltet. Jedes  Netzwerk-Interface der gleichen Komponente sollte
	 *   hier den gleichen Wert haben!
	 */
	public void setDns(String dns) {
		this.dns = dns;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMac() {
		return mac.toUpperCase();
	}

	public void setMac(String mac) {
		if (mac != null) {
			Information.getInformation().macHinzufuegen(mac);
			this.mac = mac;
		}
	}

	public String getSubnetzMaske() {
		return subnetzMaske;
	}

	public void setSubnetzMaske(String subnetzMaske) {
		this.subnetzMaske = subnetzMaske;
	}

	/** IP-Adresse des Standard-Gateways. Dorthin werden alle Pakete gesendet,
	 * fuer dessen Zieladresse kein anderer Eintrag in der Weiterleitungstabelle
	 * vorhanden ist.
	 *
	 * NOTE: die IP-Adresse des Standard-Gateways ist kein Parameter des
	 *   Netzwerk-Interfaces, wird aber aus Gruenden der Kompatibilitaet hier
	 *   verwaltet. Jedes Netzwerk-Interface der gleichen Komponente sollte hier
	 *   den gleichen Wert haben!
	 */
	public String getGateway() {
		return gateway;
	}

	/** IP-Adresse des Standard-Gateways. Dorthin werden alle Pakete gesendet,
	 * fuer dessen Zieladresse kein anderer Eintrag in der Weiterleitungstabelle
	 * vorhanden ist.
	 *
	 * @deprecated die IP-Adresse des Standard-Gateways ist kein Parameter des
	 *   Netzwerk-Interfaces, wird aber aus Gruenden der Kompatibilitaet hier
	 *   verwaltet. Jedes Netzwerk-Interface der gleichen Komponente sollte hier
	 *   den gleichen Wert haben!
	 */
	public void setGateway(String gateway) {
		this.gateway = gateway;
	}
}
