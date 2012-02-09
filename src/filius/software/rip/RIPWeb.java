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

		html += "<table border=1>";
		html += "<tr>";
		html += "<th colspan=2>Netz</th>";
		html += "<th>Hops</th>";
		html += "<th>Gültig</th>";
		html += "<th colspan=2>Nächster Hop</th>";
		html += "</tr>";

		html += "<tr>";
		html += "<th>Adresse</th>";
		html += "<th>Maske</th>";
		html += "<th></th>";
		html += "<th>(sec)</th>";
		html += "<th>privat</th>";
		html += "<th>öffentlich</th>";
		html += "</tr>";

		synchronized (table) {
			for (RIPRoute route : table.routes) {
				html += routeToHtml(route);
			}
		}

		html += "</table>";

		return html;
	}

	private String routeToHtml(RIPRoute route) {
		String html = "";

		html += "<td>" + route.netAddr + "</td>";
		html += "<td>" + route.netMask + "</td>";
		html += "<td>" + route.hops + "</td>";

		if (route.expires == 0) {
			html += "<td>(dauerhaft)</td>";
		} else {
			long gueltig = (route.expires - RIPUtil.getTime()) / 1000;
			html += "<td>" + gueltig + "</td>";
		}

		html += "<td>" + route.nextHop + "</td>";
		html += "<td><a href=\"http://" + route.hopPublicIp
				+ "/routes.html\">" + route.hopPublicIp + "</a></td>";

		if (route.hops == 0) {
			return "<tr style='background-color:#aaffaa'>" + html + "</tr>";
		} else if (route.hops == 16) {
			return "<tr style='background-color:#ffaaaa'>" + html + "</tr>";
		}
		return "<tr>" + html + "</tr>";
	}
}
