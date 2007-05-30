/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
package org.jajuk.ui;

import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.UrlImageIcon;
import org.jdesktop.swingx.JXPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;

import com.l2fprod.common.swing.JButtonBar;

/**
 * Menu bar used to choose the current perspective.
 */
public class PerspectiveBarJPanel extends JXPanel implements ITechnicalStrings {

	private static final long serialVersionUID = 1L;

	/** Perspectives tool bar* */
	private JButtonBar jtbPerspective;

	/** Self instance */
	static private PerspectiveBarJPanel pb;

	/**
	 * Perspective button
	 */
	private ArrayList<JButton> alButtons = new ArrayList<JButton>(10);

	/**
	 * Singleton access
	 * 
	 * @return
	 */
	public static synchronized PerspectiveBarJPanel getInstance() {
		if (pb == null) {
			pb = new PerspectiveBarJPanel();
		}
		return pb;
	}

	/**
	 * Constructor for PerspectiveBarJPanel.
	 */
	private PerspectiveBarJPanel() {
		setOpaque(false);
		update();
	}

	/**
	 * update contents
	 * 
	 */
	public void update() {
		// Perspectives tool bar
		jtbPerspective = new JButtonBar(JButtonBar.VERTICAL);
		jtbPerspective.setOpaque(false);
		jtbPerspective.setBorder(null);
		Iterator it = PerspectiveManager.getPerspectives().iterator();
		int index = 0;
		while (it.hasNext()) {
			final IPerspective perspective = (IPerspective) it.next();
			JButton jb = new JButton(perspective.getDesc(), new UrlImageIcon(perspective
					.getIconPath()));
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// no thread, it causes ugly screen repaint
					PerspectiveManager.setCurrentPerspective(perspective.getID());
				}
			});
			jtbPerspective.add(jb);
			alButtons.add(jb);
			index++;
		}
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(new JScrollPane(jtbPerspective));
	}

	/**
	 * Show selected perspective
	 * 
	 * @param perspective
	 */
	public void setActivated(IPerspective perspective) {
		Set<IPerspective> perspectives = PerspectiveManager.getPerspectives();
		Iterator it = alButtons.iterator();
		Iterator it2 = perspectives.iterator();
		while (it.hasNext()) {
			final JButton jb = (JButton) it.next();
			IPerspective perspective2 = (IPerspective) it2.next();
			if (perspective2.equals(perspective)) { // this perspective is
				// selected
				jb.setSelected(true);
			} else {
				jb.setSelected(false);
			}
		}
	}

	/**
	 * ToString() method
	 */
	public String toString() {
		return getClass().getName();
	}
}