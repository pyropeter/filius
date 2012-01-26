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
package filius.software.www;

import java.util.StringTokenizer;

import filius.Main;
import filius.rahmenprogramm.I18n;

public class HTTPNachricht implements I18n {

	public static final int SERVER = 0, CLIENT = 1;

	public static final String GET = "GET", POST = "POST";

	public static final String TEXT_HTML = "text/html",
			IMAGE_PNG = "image/png", IMAGE_BMP = "image/bmp",
			IMAGE_GIF = "image/gif", IMAGE_JPG = "image/jpg";

	private String contentType = null;

	private String host = null;

	private String protocolVersion = "HTTP/1.1";

	private String method = null;

	private int statusCode = 0;

	private int mode = CLIENT;

	private StringBuffer daten = null;

	private String pfad = null;

	/**
	 * Zur Erzeugung einer neuen HTTP-Nachricht. Unterschieden werden Server-
	 * und Client-Nachrichten. Danach richtet sich, welche Attribute des
	 * Kopfteils (Header) verwendet werden.
	 *
	 * @param mode
	 *            ob es sich um eine SERVER- oder CLIENT-Nachricht handelt.
	 */
	public HTTPNachricht(int mode) {
		this.mode = mode;
	}

	/**
	 * Methode zur Rekonstruktion einer HTTP-Nachricht aus einem String, der
	 * durch Aufruf der Methode toString() erzeugt wurde.
	 *
	 * @param nachricht
	 */
	public HTTPNachricht(String nachricht) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (HTTPNachricht), constr: HTTPNachricht("+nachricht+")");
		StringTokenizer tokenizer;
		String token;
		String[] zeilen;

		zeilen = nachricht.split("\n");

		if (zeilen.length > 0) {
			tokenizer = new StringTokenizer(zeilen[0], " ");
			if (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken().trim();

				if (token.equalsIgnoreCase(GET) || token.equalsIgnoreCase(POST)) {
					method = token;

//					Main.debug.println("Method: " + method);

					token = tokenizer.nextToken().trim();
					pfad = token;

//					Main.debug.println("Pfad: " + pfad);

					token = tokenizer.nextToken().trim();
					protocolVersion = token;

//					Main.debug.println("Protokoll-Version: " + protocolVersion);

					for (int i = 1; i < zeilen.length; i++) {
						if (zeilen[i].equals("")
								&& method.equalsIgnoreCase(POST)) {
							daten = new StringBuffer();
							for (int j = i; j < zeilen.length; j++) {
								daten.append(zeilen[j]);
								if (j < zeilen.length - 1) daten.append("\n");
							}
							i = zeilen.length;
						}
						else {
							tokenizer = new StringTokenizer(zeilen[i], " ");

							token = tokenizer.nextToken().trim();
							if (token.equalsIgnoreCase("host:")) {
								host = tokenizer.nextToken().trim();
							}
						}
					}
				}
				else {
					protocolVersion = token;
					//Main.debug.println("Protokoll-Version: " + protocolVersion);

					token = tokenizer.nextToken().trim();
					statusCode = Integer.parseInt(token);
//					Main.debug.println("Status-Code: " + statusCode);

					for (int i = 1; i < zeilen.length; i++) {
						if (zeilen[i].equals("")) {
							daten = new StringBuffer();
							for (int j = i+1; j < zeilen.length; j++) {
								daten.append(zeilen[j]);
								if (j < zeilen.length - 1) daten.append("\n");
							}
							i = zeilen.length;
//							Main.debug.println("Daten: " + daten.toString());
						}
						else {
							tokenizer = new StringTokenizer(zeilen[i], " ");
							token = tokenizer.nextToken().trim();
							if (token.equalsIgnoreCase("content-type:")) {
								contentType = tokenizer.nextToken().trim();
							}
						}
					}
				}
			}
		}
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		if (mode == CLIENT && method != null && pfad != null
				&& protocolVersion != null) {
			buffer.append(method + " " + getPfad() + " " + protocolVersion + "\n");
			if (host != null) {
				buffer.append("Host: " + host + "\n");
			}

			if (method.equals(POST) && daten != null) {
				buffer.append("\n" + daten);
			}
		}
		else if (mode == SERVER && protocolVersion != null) {
			buffer.append(protocolVersion + " " + statusCode + " "
					+ holeStatusNachricht(statusCode) + "\n");
			if (contentType != null) {
				buffer.append("Content-type: " + contentType + "\n");

			}
			if (daten != null) {
				buffer.append("\n" + daten);
			}
		}
		return buffer.toString();
	}

	public static String holeStatusNachricht(int code) {
		Main.debug.println("INVOKED (static) filius.software.www.HTTPNachricht, holeStatusNachricht("+code+")");
		if (code == 100) return "Continue";
		if (code == 101) return "Switching Protocols";
		if (code == 102) return "Processing";
		if (code == 200) return "OK";
		if (code == 201) return "Created";
		if (code == 202) return "Accepted";
		if (code == 203) return "Non-Authoritative Information";
		if (code == 204) return "No Content";
		if (code == 205) return "Reset Content";
		if (code == 206) return "Partial Content";
		if (code == 207) return "Multi-Status";
		if (code == 300) return "Multiple Choice";
		if (code == 301) return "Moved Permanently";
		if (code == 302) return "Found";
		if (code == 303) return "See Other";
		if (code == 304) return "Not Modified";
		if (code == 305) return "Use Proxy";
		if (code == 307) return "Temporary Redirect";
		if (code == 400) return "Bad Request";
		if (code == 401) return "Unauthorized";
		if (code == 402) return "Payment Required";
		if (code == 403) return "Forbidden";
		if (code == 404) return "Not Found";
		if (code == 405) return "Method Not Allowed";
		if (code == 406) return "Not Acceptable";
		if (code == 407) return "Proxy Authentication Required";
		if (code == 408) return "Request Time-out";
		if (code == 409) return "Conflict";
		if (code == 410) return "Gone";
		if (code == 411) return "Length Required";
		if (code == 412) return "Precondition Failed";
		if (code == 413) return "Request Entity Too Large";
		if (code == 414) return "Request-URI Too Long";
		if (code == 415) return "Unsupported Media Type";
		if (code == 416) return "Requested range not satisfiable";
		if (code == 417) return "Expectation Failed";
		if (code == 422) return "Unprocessable Entity";
		if (code == 423) return "Locked";
		if (code == 424) return "Failed Dependency";
		if (code == 500) return "Internal Server Error";
		if (code == 501) return "Not Implemented";
		if (code == 502) return "Bad Gateway";
		if (code == 503) return "Service Unavailable";
		if (code == 504) return "Gateway Time-out";
		if (code == 505) return "HTTP Version not supported";
		if (code == 507) return "Insufficient Storage";
		if (code == 509) return "Bandwidth Limit Exceeded";

		if (code >= 100 && code < 200) return messages.getString("sw_httpnachricht_msg1");
		if (code >= 200 && code < 300) return messages.getString("sw_httpnachricht_msg2");
		if (code >= 300 && code < 400) return messages.getString("sw_httpnachricht_msg3");
		if (code >= 400 && code < 500) return messages.getString("sw_httpnachricht_msg4");
		if (code >= 500 && code < 600) return messages.getString("sw_httpnachricht_msg5");

		return null;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType
	 *            the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return the daten
	 */
	public String getDaten() {
		if (daten == null) return null;
		else return daten.toString();
	}

	/**
	 * @param daten
	 *            the daten to set
	 */
	public void setDaten(String data) {
		if (data == null) daten = null;
		else daten = new StringBuffer(data);
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method
	 *            the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * @return the pfad
	 */
	public String getPfad() {
		if (pfad.trim().equals("")) return "/";
		else return pfad;
	}

	/**
	 * @param pfad
	 *            the pfad to set
	 */
	public void setPfad(String pfad) {
		this.pfad = pfad;
	}

	/**
	 * @return the protocolVersion
	 */
	public String getProtocolVersion() {
		return protocolVersion;
	}

	/**
	 * @param protocolVersion
	 *            the protocolVersion to set
	 */
	public void setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	/**
	 * @return the statusCode
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode
	 *            the statusCode to set
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

}
