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
package filius.rahmenprogramm;

import filius.Main;

/**
 * @author freischlad
 *
 *
 * Aus der Java-Doc fuer die Klasse ClassLoader: <br />
 * The ClassLoader class uses a delegation model to search for classes and
 * resources. Each instance of ClassLoader has an associated parent class
 * loader. When requested to find a class or resource, a ClassLoader instance
 * will delegate the search for the class or resource to its parent class loader
 * before attempting to find the class or resource itself. The virtual machine's
 * built-in class loader, called the "bootstrap class loader", does not itself
 * have a parent but may serve as the parent of a ClassLoader instance.
 */
public class FiliusClassLoader extends ClassLoader implements I18n {

	private static FiliusClassLoader classLoader;

	protected FiliusClassLoader(ClassLoader parent) {
		super(parent);
		Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+", constr: FiliusClassLoader("+parent+")");
	}

	public static FiliusClassLoader getInstance(ClassLoader parent) {
		Main.debug.println("INVOKED (static) filius.rahmenprogramm.FiliusClassLoader, getInstance()");
		if (classLoader == null) {
			classLoader = new FiliusClassLoader(parent);
		}

		return classLoader;
	}

	public Class<?> loadClass(String name) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", loadClass("+name+")");
		Class<?> klasse = null;

		try {
			klasse = getParent().loadClass(name);
			return klasse;
		} catch (Exception e2) {}

		try {
			klasse = findClass(name);
		} catch (Exception e) {
			e.printStackTrace(Main.debug);
		}

		return klasse;
	}

	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", findClass("+name+")");
		Class c = null;

		if (name.endsWith("BeanInfo"))
			return null;

		byte[] b = loadClassData(name);

		if (b != null) {
			c = defineClass(name, b, 0, b.length);
		} else {
			throw new ClassNotFoundException(messages
					.getString("rp_filiusclassloader_msg1")
					+ " "
					+ name
					+ " "
					+ messages.getString("rp_filiusclassloader_msg2"));
		}
		return c;
	}

	private byte[] loadClassData(String className) {
		Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", loadClassData("+className+")");
		java.io.CharArrayWriter byteInputFromClassFile = new java.io.CharArrayWriter();
		java.io.FileInputStream fileInput = null;
		int temp;
		char[] tempCharArray;
		byte[] classData = null;

		String classPath = Information.getInformation().getAnwendungenPfad()
				+ className.replace('.', System.getProperty("file.separator")
						.charAt(0)) + ".class";

		if ((new java.io.File(classPath)).exists()) {
			try {
				fileInput = new java.io.FileInputStream(classPath);
				while ((temp = fileInput.read()) != -1) {
					byteInputFromClassFile.append((char) temp);
				}
				tempCharArray = byteInputFromClassFile.toCharArray();
				classData = new byte[tempCharArray.length];
				for (int i = 0; i < classData.length; i++) {
					classData[i] = (byte) tempCharArray[i];
				}
			} catch (Exception e) {
				e.printStackTrace(Main.debug);
				classData = null;
			} finally {
				try {
					fileInput.close();
				} catch (Exception e) {
				}
			}
		}

		return classData;
	}

}
