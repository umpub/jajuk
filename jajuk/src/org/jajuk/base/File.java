/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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
 * $Log$
 * Revision 1.2  2003/10/17 20:36:45  bflorat
 * 17/10/2003
 *
 * Revision 1.1  2003/10/12 21:08:11  bflorat
 * 12/10/2003
 *
 */
package org.jajuk.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 *  A music file to be played
 *
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class File extends PropertyAdapter{
	/**File type ( mp3,ogg...)*/
	private Type type;
	/**File full path with file name*/
	private String sPath;
	/**File size in bytes*/
	private long lSize;
	
	/**
	 * File instanciation 
	 * @param sPath
	 * @param type
	 */
	public File(String sPath, Type type) {
		this.type = type;
		this.sPath = sPath;
	}

	/**
	 * @return
	 */
	public String getPath() {
		return sPath;
	}

	/**
	 * @return
	 */
	public Type getType() {
		return type;
	}


	public String toString(){
		return "File[Path="+sPath+" ; Type="+type+"]";	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
