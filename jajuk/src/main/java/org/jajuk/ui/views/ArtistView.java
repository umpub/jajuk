/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
 *  http://jajuk.info
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
 *  $Revision: 3132 $
 */
package org.jajuk.ui.views;

import ext.SwingWorker;
import ext.services.lastfm.ArtistInfo;
import ext.services.lastfm.LastFmService;

import java.awt.Dimension;
import java.awt.Insets;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Author;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.thumbnails.LastFmAuthorThumbnail;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jdesktop.swingx.JXBusyLabel;

/**
 * Display Artist bio and albums
 */
public class ArtistView extends SuggestionView {

  private static final long serialVersionUID = 1L;

  /** The artist picture + labels */
  private LastFmAuthorThumbnail authorThumb;

  /** The artist bio (from last.fm wiki) */
  private JTextArea jtaArtistDesc;

  /** The artist albums (LastFm thumbs) with labels in a flow layout */
  private JPanel jpAlbums;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("ArtistView.0");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#initUI()
   */
  public void initUI() {
    // register to player events
    ObservationManager.register(this);

    // by default, show reseted view
    reset();

    // Update initial status
    UtilFeatures.updateStatus(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.WEBRADIO_LAUNCHED);
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    return eventSubjectSet;
  }

  /**
   * Build the GUI for a given author
   * <p>
   * Must be called from the EDT
   * </p>
   */
  private void displayAuthor() {
    SwingWorker sw = new SwingWorker() {
      JScrollPane jspAlbums;
      String bio;

      @Override
      public Object construct() {
        // Call last.fm wiki
        bio = LastFmService.getInstance().getWikiText(author);
        jspAlbums = getLastFMSuggestionsPanel(SuggestionType.OTHERS_ALBUMS, true);
        return null;
      }

      @Override
      public void finished() {
        super.finished();
        removeAll();
        ArtistInfo artistInfo = LastFmService.getInstance().getArtist(author);
        // Artist unknown from last.fm, leave
        if (artistInfo == null) {
          reset();
          return;
        }
        authorThumb = new LastFmAuthorThumbnail(artistInfo);
        // No known icon next to artist thumb
        authorThumb.setArtistView(true);
        authorThumb.populate();

        jtaArtistDesc = new JTextArea(bio) {
          private static final long serialVersionUID = 9217998016482118852L;

          // We set the margin this way, setMargin() doesn't work due to
          // existing border
          public Insets getInsets() {
            return new Insets(2, 4, 0, 4);
          }
        };
        jtaArtistDesc.setBorder(null);
        jtaArtistDesc.setEditable(false);
        jtaArtistDesc.setLineWrap(true);
        jtaArtistDesc.setWrapStyleWord(true);

        JScrollPane jspWiki = new JScrollPane(jtaArtistDesc);
        jspWiki.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jspWiki.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        // Add items
        setLayout(new MigLayout("ins 5,gapy 5", "[grow]", "[grow][20%!][grow]"));
        add(authorThumb, "center,wrap");
        add(jspWiki, "growx,wrap");
        add(jspAlbums, "grow,wrap");

        revalidate();
        repaint();
      }

    };
    sw.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#update(org.jajuk.events.JajukEvent)
   */
  public void update(final JajukEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JajukEvents subject = event.getSubject();
        if (JajukEvents.WEBRADIO_LAUNCHED.equals(subject)
            || JajukEvents.ZERO.equals(event.getSubject())) {
          reset();
        } else if (JajukEvents.FILE_LAUNCHED.equals(subject)) {
          // If no playing track, reset the view
          StackItem currentItem = QueueModel.getCurrentItem();
          if (currentItem == null) {
            reset();
            return;
          }
          Author author = currentItem.getFile().getTrack().getAuthor();
          // If we already display the artist, leave
          if (author.getName().equals(ArtistView.this.author)) {
            return;
          } else {
            // Display a busy panel in the mean-time
            setLayout(new MigLayout("ins 5", "[grow]", "[grow]"));
            JXBusyLabel busy1 = new JXBusyLabel(new Dimension(50, 50));
            busy1.setBusy(true);
            removeAll();
            add(busy1, "center");
            revalidate();
            repaint();

            ArtistView.this.author = author.getName();
            // Display the panel only if the artist is not unknown
            if (author != null && !author.isUnknown()) {
              // This is done in a swing worker
              displayAuthor();
            } else {
              reset();
            }
          }
        }
      }
    });

  }

  /**
   * Show reseted view (show a message)
   * <p>
   * Must be called from the EDT
   * </p>
   */
  private void reset() {
    removeAll();
    setLayout(new MigLayout("ins 5", "grow"));
    add(new JLabel(Messages.getString("ArtistView.1")), "center");
    revalidate();
    repaint();
  }

  public void onPerspectiveSelection() {
    // override the suggestion view behavior
  }

}