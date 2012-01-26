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

/**
 * special class extending OutputStream to realise double output
 * to System.out/err and log file
 *
 * @author Christian Eibl
 * Feb. 2010
*/
package filius.rahmenprogramm;

import java.io.*;

public class TeeOutputStream extends OutputStream {

	/**
	 *  Attributes
	 */
	private OutputStream outFile = null;
	private OutputStream outScreen = null;

	public TeeOutputStream(OutputStream fileOS, OutputStream screenOS)
    {
      this.outFile = fileOS;
      this.outScreen = screenOS;
    }
      
    /**
     * write characters to both OutputStreams
     */
     public void write(int c) throws IOException
     {
       if(this.outFile != null) this.outFile.write(c);
       if(this.outScreen != null) {
	       this.outScreen.write(c);
	       this.outScreen.flush();
       }
     }
   
   
     /**
      * close streams
      */
      public void close() throws IOException
      {
        if(this.outFile != null) { 
            flush();
        	this.outFile.close();
        }
        if(this.outFile != null) this.outScreen.close();
      }
	      
  
     /**
      * flushes file stream
      */
      public void flush() throws IOException
      {
        this.outFile.flush();
      }
}
