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
 *  $Revision$
 */

package org.jajuk.ui.views;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jajuk.base.ITechnicalStrings;

/**
 *  Default implementation for views
 *
 * @author     bflorat
 * @created    15 nov. 2003
 */
public abstract class ViewAdapter extends JPanel implements IView,ITechnicalStrings {
	
	/**Displayed state */
	private boolean bIsPopulated = false;
	/**View width*/	
	private int iLogicalWidth;
	/**View height*/
	private int iLogicalHeight;
	/**View X coordonate*/
	private int iLogicalX;
	/**View Y coordonate*/
	private int iLogicalY;
	/**Should be shown option for this view*/
	private boolean bShouldBeShown = true;
	
	/**
	 * Constructor
	 */
	public ViewAdapter()  {
		super();
		setOpaque(true);
	}
	
	/**
	 * toString method
	 */
	public String toString(){
		return "View[name="+getID()+" description='"+getDesc()+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
		
	/**
	 * @return Returns the bIsPopulated.
	 */
	public boolean isPopulated() {
		return bIsPopulated;
	}
	
	/**
	 * @param isDisplayed The bIsPopulated to set.
	 */
	public void setIsPopulated(boolean isPopulated) {
		bIsPopulated = isPopulated;
	}
	
	/**
	 * View refresh
	 */
	public void refresh(){
		removeAll();
		populate();
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	/**
	 * @return Returns the iHeight.
	 */
	public int getLogicalHeight() {
		return iLogicalHeight;
	}
	/**
	 * @param height The iHeight to set.
	 */
	public void setLogicalHeight(int height) {
		iLogicalHeight = height;
	}
	/**
	 * @return Returns the iWidth.
	 */
	public int getLogicalWidth() {
		return iLogicalWidth;
	}
	/**
	 * @param width The iWidth to set.
	 */
	public void setLogicalWidth(int width) {
		iLogicalWidth = width;
	}
	/**
	 * @return Returns the iX.
	 */
	public int getLogicalX() {
		return iLogicalX;
	}
	/**
	 * @param ix The iX to set.
	 */
	public void setLogicalX(int ix) {
		iLogicalX = ix;
	}
	/**
	 * @return Returns the iY.
	 */
	public int getLogicalY() {
		return iLogicalY;
	}
	/**
	 * @param iy The iY to set.
	 */
	public void setLogicalY(int iy) {
		iLogicalY = iy;
	}
	
	/**
	 * Set all view coordonates
	 * @param iWidth
	 * @param iHeight
	 * @param iX
	 * @param iY
	 * @return view itself
	 */
	public IView setLogicalCoord(int iWidth,int iHeight,int iX,int iY){
		setLogicalWidth(iWidth);
		setLogicalHeight(iHeight);
		setLogicalX(iX);
		setLogicalY(iY);
		return this;
	}
	
	/**
	 * Set the should be shown flag
	 * @param b 
	 */
	public IView setShouldBeShown(boolean b){
		this.bShouldBeShown = b;
		return this;
	}
	
	/**
	 * @return should be shown flag
	 */
	public boolean isShouldBeShown(){
		return bShouldBeShown;
	}
	
}
