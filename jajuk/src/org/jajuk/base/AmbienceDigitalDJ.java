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

import java.util.ArrayList;

import org.jajuk.util.ITechnicalStrings;

/**
 *  Ambience DJ
 *
 * @author     Bertrand Florat
 * @created    19 march 2006
 */
public class AmbienceDigitalDJ extends DigitalDJ implements ITechnicalStrings{

    /**Used ambience*/
    private Ambience ambience;
    
    /**
     * @param sName
     */
    public AmbienceDigitalDJ(String sName) {
        super(sName);
    }

    /* (non-Javadoc)
     * @see org.jajuk.base.DigitalDJ#generatePlaylist()
     */
    @Override
    public ArrayList<File> generatePlaylist() {
        return null;
    }
    
    /**
     * @return Ambience
     */
    public Ambience getAmbience() {
        return this.ambience;
    }
    
   /**
    * (non-Javadoc)
    * @see org.jajuk.base.DigitalDJ#toXML()
    **/
    public String toXML(){
        StringBuffer sb = new StringBuffer(2000);
        sb.append(toXMLGeneralParameters());
        sb.append("\t<"+XML_DJ_AMBIENCE+" "+XML_DJ_VALUE+"='");
        sb.append(ambience.getName()+"'/>\n");
        sb.append("</"+XML_DJ_DJ+">\n");
        return sb.toString();
    }

	public void setAmbience(Ambience ambience) {
		this.ambience = ambience;
	}
}