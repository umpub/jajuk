/*
 *  Jajuk
 *  Copyright (C) 2004 bflorat
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *  $Revision$
 */

package org.jajuk.base;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

/**
 *  Manages Digital DJs
 *  <p>Singleton</p>
 * @author     Bertrand Florat
 * @created    01/03/2006
 */
public class DigitalDJManager {

    /**List of registated DJs name->DJ*/
    private HashMap<String,DigitalDJ> djs;
    
    /**self instance*/
    private static DigitalDJManager dj;
    
    /**
     * no instanciation
     */
    private DigitalDJManager() {
        djs = new HashMap();
    }
    
    /**
     * @return self instance
     */
    public static DigitalDJManager getInstance(){
        if (dj == null){
            dj = new DigitalDJManager();
        }
        return dj;
    }
    
    /**
     * Register a DJ stores in fio file
     * @param fio
     */
    public void registerDJ(File fio){
        
    }
    
    /**
     * 
     * @return DJs iteration
     */
    public Iterator getDJs(){
        return djs.values().iterator();
    }
    
    /**
     * 
     * @return DJ by name
     */
    public DigitalDJ getDJ(String sName){
        return djs.get(sName);
    }

}
