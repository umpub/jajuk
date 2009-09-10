/*
 *  Jajuk
 *  Copyright (C) 2004 The Jajuk Team
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

package org.jajuk.services.dj;

import java.util.HashSet;
import java.util.Set;

import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.util.log.Log;

/**
 * An ambience is a set of styles <br>
 * Note that an ambience is identified by an id and not its name Several
 * ambiences can eventually share the same name. This is because ambience is
 * translated and can change with current locale
 */
public class Ambience implements Comparable<Ambience> {

  /** List of styles */
  private Set<Style> styles;

  /** Ambience name */
  private String sName;

  /** Ambience ID */
  private final String sID;

  /**
   * Constructor
   * 
   * @param sID
   *          Ambience unique id
   * @param sName
   *          Ambience name
   * @param styles
   *          list of styles
   */
  public Ambience(String sID, String sName, Set<Style> styles) {
    this.sID = sID;
    this.sName = sName;
    this.styles = styles;
  }

  /**
   * Constructor
   * 
   * @param sID
   *          Ambience unique id
   * @param sName
   *          Ambience name
   * @param styles
   *          list by name
   */
  public Ambience(String sID, String sName, String[] styles) {
    Set<Style> hstyles = new HashSet<Style>(styles.length);
    for (String element : styles) {
      Style style = StyleManager.getInstance().getStyleByName(element);
      if (style != null) {
        hstyles.add(style);
      } else {
        Log.debug("Unknown style");
      }
    }
    this.sID = sID;
    this.sName = sName;
    this.styles = hstyles;
  }

  /**
   * Constructor
   * 
   * @param sName
   *          Ambience name
   */
  public Ambience(String sID, String sName) {
    this(sID, sName, new HashSet<Style>(10));
  }

  /**
   * Constructor
   */
  public Ambience() {
    this.sID = "" + System.currentTimeMillis();
    this.sName = "";
    this.styles = new HashSet<Style>(10);
  }

  public void addStyle(Style style) {
    if (style != null) {
      styles.add(style);
    }
  }

  public void removeStyle(Style style) {
    styles.remove(style);
  }

  public String getName() {
    return this.sName;
  }

  public String getID() {
    return this.sID;
  }

  public void setName(String name) {
    this.sName = name;
  }

  public Set<Style> getStyles() {
    return this.styles;
  }

  public void setStyles(Set<Style> styles) {
    this.styles = styles;
  }

  /**
   * From String, return style1,style2,...
   */
  public String getStylesDesc() {
    // check if we have styles at all
    if(getStyles().size() == 0) {
      return "";
    }
    
    StringBuilder out = new StringBuilder();
    for (Style s : getStyles()) {
      out.append(s.getName2()).append(',');
    }

    return out.substring(0, out.length() - 1); // remove trailling ,
  }

  /**
   * toString method
   * 
   * @return String representation of this item
   */
  @Override
  public String toString() {
    return sName + " " + styles;
  }

  /**
   * Equals method
   * 
   * @return true if ambience have the same same and contains the same styles
   */
  @Override
  public boolean equals(Object o) {
    // also catches null by definition
    if (!(o instanceof Ambience)) {
      return false;
    }
    Ambience ambienceOther = (Ambience) o;
    return this.sName.equals(ambienceOther.getName())
        && this.styles.equals(ambienceOther.getStyles());
  }

  @Override
  public int hashCode() {
    // for now just combine name and id to a hashcode
    // idea token from
    // http://www.geocities.com/technofundo/tech/java/equalhash.html
    int hash = 7;
    hash = 31 * hash + sName.hashCode();
    hash = 31 * hash + sID.hashCode();
    return hash;
  }

  /**
   * Compare to method : alphabetical
   */
  public int compareTo(Ambience ambience) {
    // check for null
    if(ambience == null) {
      return -1;
    }

    // otherwise just compare on the name
    return this.getName().compareToIgnoreCase(ambience.getName());
  }

  /**
   * return "style1,style2,..,style_n"
   * 
   * @return String used in DJ XML representation
   */
  public String toXML() {
    // check if we have styles at all 
    if(getStyles().size() == 0) {
      return "";
    }
    
    StringBuilder s = new StringBuilder();
    for (Style style : getStyles()) {
      s.append(style.getID()).append(',');
    }
    
    return s.substring(0, s.length() - 1); // remove last coma
  }
}
