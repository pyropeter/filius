package filius.software.rip;

import filius.software.www.WebServerPlugIn;

public class RIPWeb extends WebServerPlugIn {
	private RIPTable table;

	public RIPWeb(RIPTable table) {
		super();

		this.table = table;
	}

	public String holeHtmlSeite(String postDaten){
		String html = "<html>";
		html += "<title>RIP Routen</title>";
		html += "<h1>RIP Routen</h1>";

		html += "<table>";
		html += "<tr>";
		html += "<th>Netz</th>";
		html += "<th>Maske</th>";
		html += "<th>Router</th>";
		html += "<th>Hops</th>";
		html += "<th>Gueltig (sec)</th>";
		html += "</tr>";

		synchronized (table) {
			for (RIPRoute route : table.routes) {
				html += "<tr>" + routeToHtml(route) + "</tr>";
			}
		}

		html += "</table>";

		return html;
	}

	private String routeToHtml(RIPRoute route) {
		String html = "";

		html += "<td>" + route.netAddr + "</td>";
		html += "<td>" + route.netMask + "</td>";
		html += "<td><a href=\"http://" + route.nextHop
				+ "/routes.html\">" + route.nextHop + "</a></td>";
		html += "<td>" + route.hops + "</td>";

		if (route.expires == 0) {
			html += "<td>(dauerhaft)</td>";
		} else {
			long gueltig = (route.expires - RIPUtil.getTime()) / 1000;
			html += "<td>" + gueltig + "</td>";
		}

		return html;
	}
}
