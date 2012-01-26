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
package filius.software.system;

import filius.Main;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Die Klasse Dateisystem dient dazu, die Funktionalitaet des Dateisystems eines
 * Betriebssystems nachzubilden. Das Dateisystem ist als Baum strukturiert.
 * Daher werden die einzelnen Verzeichnisse und Dateien als Knoten gespeichert.
 * <br />
 * Dateien werden als Objekte der Klasse Datei uebergeben, die als Inhalt sowohl
 * einfache Textdateien wie auch Base64-kodierte binaere Dateien ermoeglicht.
 * <br />
 * Datei wird hier als generischer Begriff fuer eine Datei und ein Verzeichnis
 * verwendet.
 *
 * @see javax.swing.tree.DefaultMutableTree
 * @see filius.software.system.Datei
 */
public class Dateisystem implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Trennzeichen zwischen Verzeichnissen (und Datei) in einer Pfadangabe.
	 */
	public static final String FILE_SEPARATOR = "/";

	/**
	 * Das Attribut root stellt den Wurzelknoten dar. Im Betriebssystem Linux
	 * entspricht es dem Einhaengepunkt (Mount point) "/".
	 */
	private DefaultMutableTreeNode root;

	/** das aktuelle Arbeitsverzeichnis */
	private DefaultMutableTreeNode arbeitsVerzeichnis;

	/**
	 * Diese Klasse muss fuer die persistente Speicherung einer
	 * Filius-Projektdatei den Anforderungen einer JavaBean genuegen. Daher ist
	 * der Paramterlose Konstruktor wichtig!
	 */
	public Dateisystem() {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Dateisystem), constr: Dateisystem()");
		root = new DefaultMutableTreeNode("root");
		arbeitsVerzeichnis = root;
	}

	// print entire tree, starting from root node
	public void printTree() {
		printSubtree("",root);
	}
	private void printSubtree(String indent, DefaultMutableTreeNode tmpRoot) {
		DefaultMutableTreeNode node;
		Main.debug.print(indent+"--");
		if(tmpRoot.getUserObject() instanceof Datei) {
			//Main.debug.println(tmpRoot.getUserObject().toString());
		}
		else {
			Main.debug.println("["+tmpRoot.getUserObject().toString()+"]");
		}
		indent=indent+" |";
		for (Enumeration e = tmpRoot.children(); e.hasMoreElements();) {
			node = (DefaultMutableTreeNode) e.nextElement();
			printSubtree(indent,node);
		}
	}
	
	/**
	 * Diese Methode prueft, ob eine Datei (bzw. ein Verzeichnis) in einem
	 * konkreten Verzeichnis bereits vorhanden ist. Unterverzeichnisse werden
	 * <b> nicht </b> rekursiv durchsucht!
	 *
	 * @param verzeichnis
	 *            Das Verzeichnis, das auf einen Dateinamen geprueft wird (keine
	 *            Pfadangabe!).
	 * @param dateiName
	 *            Der Name einer Datei oder eines Verzeichnisses, nach dem
	 *            gesucht wird.
	 * @return Rueckgabewert ist, ob eine Datei oder ein Verzeichnis mit dem
	 *         Bezeichner "dateiName" im Verzeichnis "verzeichnis" vorhanden
	 *         ist.
	 */
	public boolean dateiVorhanden(DefaultMutableTreeNode verzeichnis,
			String dateiName) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Dateisystem), dateiVorhanden("+verzeichnis+","+dateiName+")");
		DefaultMutableTreeNode enode;

		if (verzeichnis == null) {
			return false;
		}
		else {
			for (Enumeration e = verzeichnis.children(); e.hasMoreElements();) {
				enode = (DefaultMutableTreeNode) e.nextElement();

				if (enode.getUserObject().toString()
						.equalsIgnoreCase(dateiName)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Diese Methode prueft, ob eine Datei (bzw. ein Verzeichnis) in einem
	 * konkreten Verzeichnis bereits vorhanden ist. Unterverzeichnisse werden
	 * <b> nicht </b> rekursiv durchsucht!
	 *
	 * @param verzeichnis
	 *            der absolute Pfad zu dem Verzeichnis, das auf einen Dateinamen
	 *            geprueft wird.
	 * @param dateiName
	 *            Der Name einer Datei oder eines Verzeichnisses, nach dem
	 *            gesucht wird.
	 * @return Rueckgabewert ist, ob eine Datei oder ein Verzeichnis mit dem
	 *         Bezeichner "dateiName" im Verzeichnis "verzeichnis" vorhanden
	 *         ist.
	 */
	public boolean dateiVorhanden(String verzeichnis, String dateiName) {
		return dateiVorhanden(verzeichnisKnoten(verzeichnis), dateiName);
	}

	/**
	 * Gibt einen String zurueck, der den Pfad zu des als Parameter angegebenen
	 * Knotens darstellt (z.B. root/ordner/unterordner).
	 *
	 * @param node
	 *            Der Knoten im Verzeichnisbaum kann ein Verzeichnis oder eine
	 *            Datei repraesentieren
	 * @return der Dateipfad als String. Verzeichnisse werden durch den
	 *         "File-Separator" getrennt.
	 *
	 * @see FILE_SEPARATOR
	 */
	public static String absoluterPfad(DefaultMutableTreeNode node) {
		Main.debug.println("INVOKED (static) filius.software.system.Dateisystem, absoluterPfad("+node+")");
		StringBuffer pfad;
		Object[] pfadObjekte;

		pfadObjekte = node.getUserObjectPath();
		pfad = new StringBuffer();
		pfad.append(pfadObjekte[0].toString());
		for (int i = 1; i < pfadObjekte.length; i++) {
			pfad.append(FILE_SEPARATOR + pfadObjekte[i].toString());
		}
		//Main.debug.println("\tpfad='"+stripRoot(pfad.toString())+"'");
		return stripRoot(pfad.toString());
	}

	public String holeRootPfad() {
		return absoluterPfad(root);
	}

	/**
	 * Gibt die Knoten des Verzeichnisbaums zurueck, der unter dem angegeben
	 * Pfad zu finden ist
	 *
	 * @param pfad
	 *            der absolute (!) Verzeichnis- oder Dateipfad als String
	 * @return der Knoten im Verzeichnisbaum, der durch durch den uebergebenen
	 *         Pfad bezeichnet wird
	 * @see absoluterPfad(DefaultMutableTreeNode)
	 */
	public DefaultMutableTreeNode verzeichnisKnoten(String pfad) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", verzeichnisKnoten("+pfad+")");
		pfad=stripRoot(evaluatePathString(pfad));
		
		if(pfad.equals(FILE_SEPARATOR) || pfad.isEmpty()) { return root; }
		Enumeration enumeration;
		DefaultMutableTreeNode node;

		enumeration = root.preorderEnumeration();
		while (enumeration.hasMoreElements()) {
			node = (DefaultMutableTreeNode) enumeration.nextElement();
			//Main.debug.println("DEBUG: verzeichnisKnoten:\n\t'"+pfad+"' =?= '"+absoluterPfad(node)+"'");
			if (pfad.equalsIgnoreCase(absoluterPfad(node))) {
				return node;
			}
		}

		return null;
	}

	/**
	 * Gibt die Knoten des Verzeichnisbaums zurueck, der unter dem angegeben
	 * Pfad zu finden ist
	 *
	 * @param verzeichnis Verzeichnisknoten, in dem nach einem Kinderknoten
	 *   gesucht werden soll
	 * @param pfad
	 *            der absolute Verzeichnis- oder Dateipfad als String
	 * @return der Knoten im Verzeichnisbaum, der durch durch den uebergebenen
	 *         Pfad bezeichnet wird
	 * @see absoluterPfad(DefaultMutableTreeNode)
	 */
	public static DefaultMutableTreeNode verzeichnisKnoten(DefaultMutableTreeNode verzeichnis, String pfad) {
		Main.debug.println("INVOKED (static) filius.software.system.Dateisystem, verzeichnisKnoten("+verzeichnis+","+pfad+")");
		Enumeration enumeration;
		DefaultMutableTreeNode node;
		String absolutePath;

		if(pfad.length()>0 && pfad.substring(0,1).equals(FILE_SEPARATOR)) {   // 'pfad' is absolute path!
			absolutePath = evaluatePathString(pfad);
		}
		else {
			absolutePath = evaluatePathString(absoluterPfad(verzeichnis)+FILE_SEPARATOR+pfad);
		}

		enumeration = verzeichnis.preorderEnumeration();
		while (enumeration.hasMoreElements()) {
			node = (DefaultMutableTreeNode) enumeration.nextElement();
			if (absolutePath.equalsIgnoreCase(absoluterPfad(node))) {
				return node;
			}
		}

		return null;
	}

	/**
	 * delete file from filesystem
	 * 
	 */
	public boolean deleteFile(String absolutePath) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Dateisystem), deleteFile("+absolutePath+")");
		DefaultMutableTreeNode node = verzeichnisKnoten(absolutePath);
		if (node != null) { 
			node.removeFromParent();
			return true;
		}
		else {
			return false;
		}
	}
	/**
	 * Mit dieser Methode wird eine Referenz auf eine Datei geholt.
	 *
	 * @param dateiPfad
	 *            der absolute Pfad zu der Datei
	 * @return die Datei wenn vorhanden, sonst "null"
	 */
	public Datei holeDatei(String dateiPfad) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Dateisystem), holeDatei("+dateiPfad+")");
		DefaultMutableTreeNode node;

		node = verzeichnisKnoten(dateiPfad);
		if (node != null && (node.getUserObject() instanceof Datei)) {
//			Main.debug.println("DEBUG ("+this.hashCode()+") "+getClass()+", holeDatei:  return='"+(Datei) node.getUserObject()+"'");
			return (Datei) node.getUserObject();
		}
		else {
//			Main.debug.println("DEBUG ("+this.hashCode()+") "+getClass()+", holeDatei:  return=<null>");
			return null;
		}
	}

	/**
	 * Mit dieser Methode wird eine Referenz auf eine Datei geholt.
	 *
	 * @param verzeichnis
	 *            ein Oberverzeichnis der zu holenden Datei
	 * @param dateiPfad
	 *            der relative Pfad der Datei im Bezug zu verzeichnis
	 * @return die Datei wenn vorhanden, sonst "null"
	 */
	public Datei holeDatei(DefaultMutableTreeNode verzeichnis, String dateiPfad) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Dateisystem), holeDatei("+verzeichnis+","+dateiPfad+")");
		String absoluterDateiPfad;

		absoluterDateiPfad = absoluterPfad(verzeichnis) + FILE_SEPARATOR + dateiPfad;
		return holeDatei(absoluterDateiPfad);
	}

	/**
	 * Mit dieser Methode wird eine Referenz auf eine Datei geholt.
	 *
	 * @param verzeichnisPfad
	 *            der absolute Pfad zu einem Oberverzeichnis der zu holenden
	 *            Datei
	 * @param dateiPfad
	 *            der relative Pfad der Datei im Bezug zu verzeichnisPfad
	 * @return die Datei wenn vorhanden, sonst "null"
	 */
	public Datei holeDatei(String verzeichnisPfad, String dateiPfad) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Dateisystem), holeDatei("+verzeichnisPfad+","+dateiPfad+")");
		String absoluterDateiPfad;

		absoluterDateiPfad = verzeichnisPfad + FILE_SEPARATOR + dateiPfad;
		return holeDatei(absoluterDateiPfad);
	}

	/**
	 * Sucht in einem Verzeichnis nach Dateien die dem Suchbegriff aehneln.
	 *
	 *
	 * @param suchVerzeichnis
	 *            Verzeichni, in dem nach Dateien gesucht wird, die dem
	 *            Suchstring entsprechen.
	 * @param suchString
	 *            ein String nach dem in Dateinnamen gesucht wird
	 * @return eine Liste von Dateien, in deren Dateiname der Suchstring
	 *         enthalten ist
	 */
	public LinkedList<Datei> dateiSuche(String suchVerzeichnis,
			String suchString) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+" (Dateisystem), dateiSuche("+suchVerzeichnis+","+suchString+")");
		LinkedList<Datei> dateien = new LinkedList<Datei>();
		DefaultMutableTreeNode verzeichnisNode, node;
		Datei tmpDatei;

		verzeichnisNode = verzeichnisKnoten(suchVerzeichnis);

		for (Enumeration e = verzeichnisNode.children(); e.hasMoreElements();) {
			node = (DefaultMutableTreeNode) e.nextElement();

			if (node.getUserObject() instanceof Datei) {
				tmpDatei = (Datei) node.getUserObject();
				if (tmpDatei.getName().toLowerCase().matches(
						"(.+)?" + suchString.toLowerCase() + "(.+)?")) {
					dateien.addLast(tmpDatei);
				}
			}
		}
		return dateien;
	}

	/**
	 * Methode zum speichern einer Datei. Existiert die Datei in dem angegebenen
	 * Verzeichnis bereits, wird sie ueberschrieben! Existiert der Knoten im
	 * Verzeichnisbaum noch nicht, wird er angelegt.
	 *
	 * @param verzeichnisPfad
	 *            absoluter Pfad des Verzeichnisses, in dem die Datei
	 *            gespeichert werden soll
	 * @param datei
	 *            der Dateiname der zu speichernden Datei
	 * @return ob das Speichern erfolgreich war
	 */
	public boolean speicherDatei(String verzeichnisPfad, Datei datei) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", speicherDatei("+verzeichnisPfad+","+datei+")");
		DefaultMutableTreeNode node = null;

		node = verzeichnisKnoten(verzeichnisPfad);

		if (node != null) {
			if (!dateiVorhanden(node, datei.getName())) {
				DefaultMutableTreeNode dateiNode = new DefaultMutableTreeNode(datei);
				node.add(dateiNode);
			}
			else {
				node = verzeichnisKnoten(verzeichnisPfad + FILE_SEPARATOR
						+ datei.getName());
				node.setUserObject(datei);
			}
			return true;
		}
		else {
			Main.debug.println("ERROR ("+this.hashCode()+"): Datei " + datei
					+ " konnte nicht gespeichert werden, "
					+ "weil Verzeichnis " + verzeichnisPfad
					+ " nicht existiert.");
			return false;
		}
	}

	/**
	 * Methode zum speichern einer Datei. Existiert die Datei in dem angegebenen
	 * Verzeichnis bereits, wird sie ueberschrieben! Existiert der Knoten im
	 * Verzeichnisbaum noch nicht, wird er angelegt. <br />
	 * Diese Methode verwendet speicherDatei(String, String).
	 *
	 * @param verzeichnis
	 *            Verzeichnis, in dem die Datei gespeichert werden soll
	 * @param datei
	 *            der Dateiname der zu speichernden Datei
	 * @return ob das Speichern erfolgreich war
	 */
	public boolean speicherDatei(DefaultMutableTreeNode verzeichnis, Datei datei) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", speicherDatei("+verzeichnis+","+datei+")");
		return speicherDatei(absoluterPfad(verzeichnis), datei);
	}

	/**
	 * Diese Methode legt ein neues Verzeichnis an. Wenn das Verzeichnis bereits
	 * existiert, passiert nichts.
	 *
	 * @param verzeichnisPfad
	 *            absoluter Pfad zu dem Verzeichnis, in dem das neue Verzeichnis
	 *            erstellt werden soll
	 * @param neuesVerzeichnis
	 *            der Name des neu zu erstellenden Verzeichnisses
	 * @return ob das Verzeichnis nach Ausfuehrung dieser Methode existiert. D.
	 *         h. wenn es bereits vorher vorhanden war, wird auch "true" zurueck
	 *         gegeben.
	 */
	public boolean erstelleVerzeichnis(String verzeichnisPfad,
			String neuesVerzeichnis) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", erstelleVerzeichnis("+verzeichnisPfad+","+neuesVerzeichnis+")");
		DefaultMutableTreeNode node;
		DefaultMutableTreeNode neuerNode = null;
		String absPath;
		if(neuesVerzeichnis.length()>0 && neuesVerzeichnis.substring(0,1).equals(FILE_SEPARATOR)) {   // 'pfad' is absolute path!
			absPath = evaluatePathString(neuesVerzeichnis);
		}
		else {
			absPath = evaluatePathString(verzeichnisPfad+FILE_SEPARATOR+neuesVerzeichnis);
		}
		verzeichnisPfad = getDirectory(absPath);
		neuesVerzeichnis = getBasename(absPath);
		
		node = verzeichnisKnoten(verzeichnisPfad);
		if (node != null) {
			if (dateiVorhanden(node, neuesVerzeichnis)) {
				Main.debug.println("WARNING ("+this.hashCode()+"): Verzeichnis "
						+ neuesVerzeichnis + " wurde nicht erzeugt, "
						+ "weil es im Verzeichnis " + verzeichnisPfad
						+ " bereits existiert.");
			}
			else {
				neuerNode = new DefaultMutableTreeNode(neuesVerzeichnis);
				node.add(neuerNode);
//				Main.debug.println("DEBUG ("+this.hashCode()+"): Verzeichnis "
//						+ neuesVerzeichnis + " wurde erstellt.");
			}
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Diese Methode legt ein neues Verzeichnis an. Wenn das Verzeichnis bereits
	 * existiert, passiert nichts.
	 *
	 * @param verzeichnis
	 *            Verzeichnis, in dem das neue Verzeichnis erstellt werden soll
	 * @param neuesVerzeichnis
	 *            der Name des neu zu erstellenden Verzeichnisses
	 * @return ob das Verzeichnis nach Ausfuehrung dieser Methode existiert. D.
	 *         h. wenn es bereits vorher vorhanden war, wird auch "true" zurueck
	 *         gegeben.
	 */
	public boolean erstelleVerzeichnis(DefaultMutableTreeNode verzeichnis,
			String neuesVerzeichnis) {
		return erstelleVerzeichnis(absoluterPfad(verzeichnis), neuesVerzeichnis);
	}

	/**
	 * Methode zum abrufen einer Liste aller Dateien in einem Verzeichnis.
	 *
	 * @param node
	 *            das Verzeichnis, in dem die Dateien gespeichert sind
	 * @return eine Liste der Dateien
	 */
	public LinkedList<Datei> holeDateien(DefaultMutableTreeNode node) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", holeDateien("+node+")");
		LinkedList<Datei> liste = new LinkedList<Datei>();

		if (node == null) {
			return null;
		}
		else {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				DefaultMutableTreeNode n = (DefaultMutableTreeNode) e
						.nextElement();
				if (n.getUserObject() instanceof Datei) {
					Datei dat = (Datei) n.getUserObject();
					liste.add(dat);
				}
			}
			return liste;
		}
	}

	/**
	 * Methode, um den Inhalt eines Verzeichnisses zu erhalten.
	 *
	 * @param verzeichnis
	 *            das Verzeichnis, dessen Inhalt zurueckgegeben werden soll
	 * @return wenn das Verzeichnis existiert, wird eine Liste mit allen
	 *         Objekten der Kinderknoten zurueckgeliefert. Die Liste enthaelt
	 *         Datei-Objekte fuer Dateien und Strings fuer Verzeichnisse. Wenn
	 *         das Verzeichnis nicht existiert wird null zurueckgeliefert.
	 */
	public LinkedList<Object> listeVerzeichnis(DefaultMutableTreeNode verzeichnis) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", listeVerzeichnis("+verzeichnis+")");
		LinkedList<Object> liste = new LinkedList<Object>();
		Enumeration enumeration;
		DefaultMutableTreeNode tmpNode;

		if (verzeichnis == null) {
			return null;
		}
		else {
			enumeration = verzeichnis.children();
			while (enumeration.hasMoreElements()) {
				tmpNode = (DefaultMutableTreeNode) enumeration.nextElement();
				liste.addLast(tmpNode.getUserObject());
			}
			return liste;
		}
	}

	public DefaultMutableTreeNode getRoot() {
		return root;
	}

	public void setRoot(DefaultMutableTreeNode root) {
		this.root = root;
	}

	public DefaultMutableTreeNode getArbeitsVerzeichnis() {
		return arbeitsVerzeichnis;
	}

	public void setArbeitsVerzeichnis(DefaultMutableTreeNode arbeitsVerzeichnis) {
		this.arbeitsVerzeichnis = arbeitsVerzeichnis;
	}
	
	// change current working directory
	public DefaultMutableTreeNode changeDirectory(String absPath) {
		return verzeichnisKnoten(absPath);
	}
	public DefaultMutableTreeNode changeDirectory(String currDir,String relPath) {
		return verzeichnisKnoten(currDir+Dateisystem.FILE_SEPARATOR+relPath);
	}
	
	/**
	 * bunch of path calculation and assignment functions for nodes in the tree
	 */
	// evaluate '.' and '..' as special directories
	public static String evaluatePathString(String path) {
		Main.debug.println("INVOKED (static) filius.software.system.Dateisystem, evaluatePathString("+path+")");
		String result="";
		StringTokenizer tk = new StringTokenizer(path, Dateisystem.FILE_SEPARATOR);
		String[] pathElements = new String[tk.countTokens()];
		int currIndex=-1;
		String currString;
		while(tk.hasMoreTokens()) {
			currString = tk.nextToken();
			if (currString.equals(".")) {}   // current directory is referenced again by "."; ignore this path element
			else if (currString.equals("..")) {
				currIndex--;
			}
			else if (!currString.equals("")) {
				currIndex++;
				pathElements[currIndex] = currString;
			}
		}
		for (int i=0; i<=currIndex; i++) {    // NOTE: if currIndex<0, e.g. because of multiple '..' elements, then empty path will be returned!
			result+=pathElements[i];
			if(i<currIndex) result+=Dateisystem.FILE_SEPARATOR;
		}
		if(currIndex>=0 && path.substring(0,1).equals(FILE_SEPARATOR))
			result=FILE_SEPARATOR+result;   // add leading slash if it was present before
		//Main.debug.println("	\tevaluatePathString, result="+result);
		return result;
	}
	
	// strip root node denomination, e.g. 'root', from actual (absolute!) path String representation
	private static String stripRoot(String path) {
		Main.debug.println("INVOKED (static) filius.software.system.Dateisystem, stripRoot("+path+")");
		if(path.indexOf(Dateisystem.FILE_SEPARATOR)>=0) 
			return path.substring(path.indexOf(Dateisystem.FILE_SEPARATOR));
		else
			return "/";
	}
	
	// add root node denomination again to path representation (if not existing already)
	private String addRoot(String path) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", addRoot("+path+")");
		if(path.substring(0,1).equals(Dateisystem.FILE_SEPARATOR)) {
			return root.toString()+path;
		}
		else return path;
	}
	
	private DefaultMutableTreeNode pathToNode(String path) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", pathToNode("+path+")");
		return null;
	}
	
	private String nodeToPath(DefaultMutableTreeNode node) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", nodeToPath("+node+")");
		return "";
	}

	// get directory part in absolute file pathname
	public static String getDirectory (String path) {
		Main.debug.println("INVOKED (static) filius.software.system.Dateisystem, getDirectory("+path+")");
		if(path.lastIndexOf(Dateisystem.FILE_SEPARATOR) >= 0)
			return path.substring(0, path.lastIndexOf(Dateisystem.FILE_SEPARATOR));
		else
			return "";
	}
	// get filename part in absolute file pathname
	public static String getBasename (String path) {
		Main.debug.println("INVOKED (static) filius.software.system.Dateisystem, getBasename("+path+")");
		if(path.lastIndexOf(Dateisystem.FILE_SEPARATOR) >= 0)
			return path.substring(path.lastIndexOf(Dateisystem.FILE_SEPARATOR)+1);
		else
			return path;
	}
}
