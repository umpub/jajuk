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
import java.util.HashSet;

import org.jajuk.util.ITechnicalStrings;

/**
 *  Type description
 *
 * @author     Bertrand Florat
 * @created    1 mars 2006
 */
public class ProportionDigitalDJ extends DigitalDJ implements ITechnicalStrings{

    /**Set of proportions*/
    private HashSet<Proportion> proportions;
    
    /**
     * @param sName
     */
    public ProportionDigitalDJ(String sName) {
        super(sName);
        this.proportions = new HashSet(10);
    }

    /* (non-Javadoc)
     * @see org.jajuk.base.DigitalDJ#generatePlaylist()
     */
    @Override
    public ArrayList<File> generatePlaylist() {
        return null;
    }
    
    /**
     * @return Proportions
     */
    public HashSet<Proportion> getProportions() {
        return this.proportions;
    }
    
   /**
    * (non-Javadoc)
    * @see org.jajuk.base.DigitalDJ#toXML()
    **/
    public String toXML(){
        StringBuffer sb = new StringBuffer(2000);
        sb.append(toXMLGeneralParameters());
        sb.append("\t<"+XML_DJ_PROPORTIONS+">\n");
        for (Proportion proportion: proportions){
            String stylesDesc = "";
            for (Style style:proportion.getStyles()){
                stylesDesc += style.getId()+',';
            }
            //remove trailing coma
            stylesDesc = stylesDesc.substring(0,stylesDesc.length() - 1);
            sb.append("\t\t<"+XML_DJ_PROPORTION+" "+XML_DJ_STYLES+"='"+stylesDesc+"' "+
            		XML_DJ_VALUE+"='"+proportion.getProportion()+"'/>\n");
        }
        sb.append("\t</"+XML_DJ_PROPORTIONS+">\n");
        sb.append("</"+XML_DJ_DJ+">\n");
        return sb.toString();
    }

	public void setProportions(HashSet<Proportion> proportions) {
		this.proportions = proportions;
	}
    
    

}
