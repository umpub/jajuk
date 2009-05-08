/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision$$
 */

package org.jajuk.ui.views;

import ext.AutoCompleteDecorator;
import ext.SwingWorker;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.AuthorManager;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.ItemManager;
import org.jajuk.base.StyleManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.FIFO;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.ILaunchCommand;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.ui.helpers.PreferencesJMenu;
import org.jajuk.ui.helpers.TableTransferHandler;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukTable;
import org.jajuk.ui.widgets.JajukToggleButton;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.error.CannotRenameException;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.error.NoneAccessibleFileException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;
import org.jdesktop.swingx.decorator.SortOrder;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Abstract table view : common implementation for both files and tracks table
 * views
 */
public abstract class AbstractTableView extends ViewAdapter implements ActionListener,
    ItemListener, TableModelListener {

  private static final long serialVersionUID = -4418626517605128694L;

  JajukTable jtable;

  private JPanel jpControl;

  JajukToggleButton jtbEditable;

  private JLabel jlFilter;

  private JComboBox jcbProperty;

  private JLabel jlEquals;

  private JTextField jtfValue;

  /** Table model */
  JajukTableModel model;

  /** Currently applied filter */
  private String sAppliedFilter = "";

  /** Currently applied criteria */
  private String sAppliedCriteria;

  /** Do search panel need a search */
  private boolean bNeedSearch = false;

  /** Default time in ms before launching a search automatically */
  private static final int WAIT_TIME = 300;

  /** Date last key pressed */
  private long lDateTyped;

  /** Editable table configuration name, must be overwritten by child classes */
  String editableConf;

  /**
   * Columns to show table configuration name, must be overwritten by child
   * classes
   */
  String columnsConf;

  JMenuItem jmiPlay;
  JMenuItem jmiPush;
  JMenuItem jmiFrontPush;
  JMenuItem jmiDelete;
  JMenuItem jmiPlayRepeat;
  JMenuItem jmiPlayShuffle;
  JMenuItem jmiBookmark;
  JMenuItem jmiProperties;
  JMenuItem jmiFileCopyURL;

  PreferencesJMenu pjmTracks;

  /**
   * Launches a thread used to perform dynamic filtering when user is typing
   */
  Thread filteringThread = new Thread("Dynamic user input filtering thread") {
    @Override
    public void run() {
      while (true) {
        try {
          Thread.sleep(200);
        } catch (InterruptedException ie) {
          Log.error(ie);
        }
        if (bNeedSearch && (System.currentTimeMillis() - lDateTyped >= WAIT_TIME)) {
          sAppliedFilter = jtfValue.getText();
          sAppliedCriteria = getApplyCriteria();
          applyFilter(sAppliedCriteria, sAppliedFilter);
          bNeedSearch = false;
        }
      }
    }
  };

  /**
   * 
   * @return Applied criteria
   */
  private String getApplyCriteria() {
    int indexCombo = jcbProperty.getSelectedIndex();
    if (indexCombo == 0) { // first criteria is special: any
      sAppliedCriteria = XML_ANY;
    } else { // otherwise, take criteria from model
      sAppliedCriteria = model.getIdentifier(indexCombo);
    }
    return sAppliedCriteria;
  }

  /**
   * Code used in child class SwingWorker for long delay computations (used in
   * initUI())
   * 
   * @return
   */
  public Object construct() {
    model = populateTable();
    jtable = new JajukTable(model, true, columnsConf);

    // Add generic menus
    jmiPlay = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_SELECTION));
    jmiPlay.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jtable.getMenu().add(jmiPlay);

    jmiFrontPush = new JMenuItem(ActionManager.getAction(JajukActions.PUSH_FRONT_SELECTION));
    jmiFrontPush.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jtable.getMenu().add(jmiFrontPush);

    jmiPush = new JMenuItem(ActionManager.getAction(JajukActions.PUSH_SELECTION));
    jmiPush.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jtable.getMenu().add(jmiPush);

    jmiPlayRepeat = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_REPEAT_SELECTION));
    jmiPlayRepeat.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jtable.getMenu().add(jmiPlayRepeat);

    jmiPlayShuffle = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_SHUFFLE_SELECTION));
    jmiPlayShuffle.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jtable.getMenu().add(jmiPlayShuffle);

    jtable.getMenu().addSeparator();

    jmiDelete = new JMenuItem(ActionManager.getAction(JajukActions.DELETE));
    jmiDelete.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jtable.getMenu().add(jmiDelete);

    jmiFileCopyURL = new JMenuItem(ActionManager.getAction(JajukActions.COPY_TO_CLIPBOARD));
    jmiFileCopyURL.putClientProperty(Const.DETAIL_CONTENT, jtable.getSelection());
    jtable.getMenu().add(jmiFileCopyURL);

    jmiBookmark = new JMenuItem(ActionManager.getAction(JajukActions.BOOKMARK_SELECTION));
    jmiBookmark.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());

    jmiProperties = new JMenuItem(ActionManager.getAction(JajukActions.SHOW_PROPERTIES));
    jmiProperties.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());

    pjmTracks = new PreferencesJMenu(jtable.getSelection());

    // Set a default behavior for double click or click on the play column
    jtable.setCommand(new ILaunchCommand() {
      public void launch(int nbClicks) {
        // Ignore event if several rows are selected
        if (jtable.getSelectedColumnCount() != 1) {
          return;
        }

        int iSelectedCol = jtable.getSelectedColumn();
        // Convert column selection as columns may have been moved
        iSelectedCol = jtable.convertColumnIndexToModel(iSelectedCol);

        // We launch the selection :
        // - In any case if user clicked on the play column (column 0)
        // - Or in case of double click on any column when table is not editable
        if (iSelectedCol == 0 || // click on play icon
            // double click on any column and edition state false
            (nbClicks == 2 && !jtbEditable.isSelected())) {
          Item item = model.getItemAt(jtable.convertRowIndexToModel(jtable.getSelectedRow()));
          List<File> files = UtilFeatures.getPlayableFiles(item);
          if (files.size() > 0) {
            // launch it
            FIFO.push(UtilFeatures.createStackItems(UtilFeatures.applyPlayOption(files), Conf
                .getBoolean(Const.CONF_STATE_REPEAT), true), Conf
                .getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK));
          } else {
            Messages.showErrorMessage(10);
          }
        }
      }
    });
    return null;
  }

  /**
   * Code used in child class SwingWorker for display computations (used in
   * initUI())
   * 
   * @return
   */
  public void finished() {
    // Control panel
    jpControl = new JPanel();
    jpControl.setBorder(BorderFactory.createEtchedBorder());
    jtbEditable = new JajukToggleButton(IconLoader.getIcon(JajukIcons.EDIT));
    jtbEditable.setToolTipText(Messages.getString("AbstractTableView.11"));
    jtbEditable.addActionListener(this);
    jlFilter = new JLabel(Messages.getString("AbstractTableView.0"));
    // properties combo box, fill with columns names expect ID
    jcbProperty = new JComboBox();
    // "any" criteria
    jcbProperty.addItem(Messages.getString("AbstractTableView.8"));
    for (int i = 1; i < model.getColumnCount(); i++) {
      // Others columns except ID
      jcbProperty.addItem(model.getColumnName(i));
    }
    jcbProperty.setToolTipText(Messages.getString("AbstractTableView.1"));
    jcbProperty.addItemListener(this);
    jlEquals = new JLabel(Messages.getString("AbstractTableView.7"));
    jtfValue = new JTextField();
    jtfValue.setBorder(BorderFactory.createLineBorder(Color.BLUE));
    jtfValue.setFont(FontManager.getInstance().getFont(JajukFont.SEARCHBOX));
    jtfValue.setForeground(new Color(172, 172, 172));
    jtfValue.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        bNeedSearch = true;
        lDateTyped = System.currentTimeMillis();
      }
    });
    jtfValue.setToolTipText(Messages.getString("AbstractTableView.3"));
    jpControl.setLayout(new MigLayout("insets 2,gapy 6", "[20][grow,gp 70][grow]"));
    jpControl.add(jtbEditable, "gapleft 5,gapright 15");
    jpControl.add(jlFilter, "split 2");
    jpControl.add(jcbProperty, "grow,gapright 15");
    jpControl.add(jlEquals, "split 2");
    jpControl.add(jtfValue, "grow,gapright 2");
    setCellEditors();
    JScrollPane jsp = new JScrollPane(jtable);
    setLayout(new MigLayout("", "[grow]"));
    add(jpControl, "wrap,grow");
    add(jsp, "grow");
    jtable.setDragEnabled(true);
    jtable.setTransferHandler(new TableTransferHandler(jtable));
    jtable.showColumns(jtable.getColumnsConf());
    applyFilter(null, null);
    jtable.setSortOrder(0, SortOrder.ASCENDING);
    jtable.setHighlighters(UtilGUI.getAlternateHighlighter());

    // Hide the copy url if several items selection. Do not simply disable them
    // as the getMenu() method enable all menu items
    jtable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        jmiFileCopyURL.setVisible(jtable.getSelectedRowCount() < 2);
      }
    });
    // Register on the list for subject we are interested in
    ObservationManager.register(this);
    // refresh columns conf in case of some attributes been removed
    // or added before view instantiation
    Properties properties = ObservationManager
        .getDetailsLastOccurence(JajukEvents.CUSTOM_PROPERTIES_ADD);
    JajukEvent event = new JajukEvent(JajukEvents.CUSTOM_PROPERTIES_ADD, properties);
    update(event);
    initTable(); // perform type-specific init
    // Start filtering thread
    filteringThread.start();
    // Register keystrokes
    setKeystrokes();
  }

  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.DEVICE_MOUNT);
    eventSubjectSet.add(JajukEvents.DEVICE_UNMOUNT);
    eventSubjectSet.add(JajukEvents.DEVICE_REFRESH);
    eventSubjectSet.add(JajukEvents.SYNC_TREE_TABLE);
    eventSubjectSet.add(JajukEvents.CUSTOM_PROPERTIES_ADD);
    eventSubjectSet.add(JajukEvents.CUSTOM_PROPERTIES_REMOVE);
    eventSubjectSet.add(JajukEvents.RATE_CHANGED);
    eventSubjectSet.add(JajukEvents.TABLE_CLEAR_SELECTION);
    eventSubjectSet.add(JajukEvents.PARAMETERS_CHANGE);
    eventSubjectSet.add(JajukEvents.VIEW_REFRESH_REQUEST);
    eventSubjectSet.add(JajukEvents.TABLE_SELECTION_CHANGED);
    return eventSubjectSet;
  }

  /**
   * Apply a filter, to be implemented by files and tracks tables, alter the
   * model
   */
  public void applyFilter(final String sPropertyName, final String sPropertyValue) {
    SwingWorker sw = new SwingWorker() {
      @Override
      public Object construct() {
        model.removeTableModelListener(AbstractTableView.this);
        model.populateModel(sPropertyName, sPropertyValue, jtable.getColumnsConf());
        model.addTableModelListener(AbstractTableView.this);
        model.fireTableDataChanged();
        return null;
      }

      @Override
      public void finished() {
        // Force table repaint (for instance for rating stars update)
        jtable.revalidate();
        jtable.repaint();
        UtilGUI.stopWaiting();
      }
    };
    UtilGUI.waiting();
    sw.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(final JajukEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @SuppressWarnings("unchecked")
      public void run() {
        try {
          jtable.setAcceptColumnsEvents(false); // flag reloading to avoid wrong
          // column
          // events
          JajukEvents subject = event.getSubject();
          if (JajukEvents.TABLE_CLEAR_SELECTION.equals(subject)) {
            jtable.clearSelection();
          }
          if (JajukEvents.DEVICE_MOUNT.equals(subject)
              || JajukEvents.DEVICE_UNMOUNT.equals(subject)) {
            jtable.clearSelection();
            // force filter to refresh
            applyFilter(sAppliedCriteria, sAppliedFilter);
          } else if (JajukEvents.SYNC_TREE_TABLE.equals(subject)) {
            // Consume only events from the same perspective for
            // table/tree synchronization
            if (event.getDetails() != null
                && !(event.getDetails().getProperty(Const.DETAIL_ORIGIN).equals(getPerspective()
                    .getID()))) {
              return;
            }
            // Update model tree selection
            model.setTreeSelection((Set<Item>) event.getDetails().get(Const.DETAIL_SELECTION));
            // force redisplay to apply the filter
            jtable.clearSelection();
            // force filter to refresh
            applyFilter(sAppliedCriteria, sAppliedFilter);
          } else if (JajukEvents.PARAMETERS_CHANGE.equals(subject)) {
            // force redisplay to apply the filter
            jtable.clearSelection();
            // force filter to refresh
            applyFilter(sAppliedCriteria, sAppliedFilter);
          } else if (JajukEvents.DEVICE_REFRESH.equals(subject)) {
            // force filter to refresh
            applyFilter(sAppliedCriteria, sAppliedFilter);
          } else if (JajukEvents.VIEW_REFRESH_REQUEST.equals(subject)) {
            // force filter to refresh if the events has been triggered by the
            // table itself after a column change
            JTable table = (JTable) event.getDetails().get(Const.DETAIL_CONTENT);
            if (table.equals(jtable)) {
              applyFilter(sAppliedCriteria, sAppliedFilter);
            }
          } else if (JajukEvents.RATE_CHANGED.equals(subject)) {
            // Ignore the refresh if the event comes from the table itself
            Properties properties = event.getDetails();
            if (properties != null
                && AbstractTableView.this.equals(properties.get(Const.DETAIL_ORIGIN))) {
              return;
            }
            // Keep current selection and nb of rows
            int[] selection = jtable.getSelectedRows();
            // force filter to refresh
            applyFilter(sAppliedCriteria, sAppliedFilter);
            jtable.setSelectedRows(selection);
          } else if (JajukEvents.CUSTOM_PROPERTIES_ADD.equals(subject)) {
            Properties properties = event.getDetails();
            if (properties == null) {
              // can be null at view populate
              return;
            }
            model = populateTable();
            model.addTableModelListener(AbstractTableView.this);
            jtable.setModel(model);
            setCellEditors();
            // add new item in configuration columns
            jtable.addColumnIntoConf((String) properties.get(Const.DETAIL_CONTENT));
            jtable.showColumns(jtable.getColumnsConf());
            applyFilter(sAppliedCriteria, sAppliedFilter);
            jcbProperty.addItem(properties.get(Const.DETAIL_CONTENT));
          } else if (JajukEvents.CUSTOM_PROPERTIES_REMOVE.equals(subject)) {
            Properties properties = event.getDetails();
            if (properties == null) { // can be null at view
              // populate
              return;
            }
            // remove item from configuration columns
            model = populateTable();// create a new model
            model.addTableModelListener(AbstractTableView.this);
            jtable.setModel(model);
            setCellEditors();
            jtable.addColumnIntoConf((String) properties.get(Const.DETAIL_CONTENT));
            jtable.showColumns(jtable.getColumnsConf());
            applyFilter(sAppliedCriteria, sAppliedFilter);
            jcbProperty.removeItem(properties.get(Const.DETAIL_CONTENT));
          } else if (JajukEvents.TABLE_SELECTION_CHANGED.equals(subject)) {
            // Refresh the preference menu according to the selection
            pjmTracks.resetUI(jtable.getSelection());
          }

        } catch (Exception e) {
          Log.error(e);
        } finally {
          jtable.setAcceptColumnsEvents(true); // make sure to remove this flag
        }
      }
    });
  }

  /**
   * Add keystroke support on the tree
   */
  private void setKeystrokes() {
    jtable.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    InputMap inputMap = jtable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    ActionMap actionMap = jtable.getActionMap();

    // Delete
    Action action = ActionManager.getAction(JajukActions.DELETE);
    inputMap.put(KeyStroke.getKeyStroke("DELETE"), "delete");
    actionMap.put("delete", action);

    // Properties ALT/ENTER
    action = ActionManager.getAction(JajukActions.SHOW_PROPERTIES);
    inputMap.put(KeyStroke.getKeyStroke("alt ENTER"), "properties");
    actionMap.put("properties", action);
  }

  /** Fill the table */
  abstract JajukTableModel populateTable();

  private void setCellEditors() {
    for (TableColumn tc : ((DefaultTableColumnModelExt) jtable.getColumnModel()).getColumns(true)) {
      TableColumnExt col = (TableColumnExt) tc;
      String sIdentifier = model.getIdentifier(col.getModelIndex());
      // create a combo box for styles, note that we can't add new
      // styles dynamically
      if (Const.XML_STYLE.equals(sIdentifier)) {
        JComboBox jcb = new JComboBox(StyleManager.getInstance().getStylesList());
        jcb.setEditable(true);
        AutoCompleteDecorator.decorate(jcb);
        col.setCellEditor(new ComboBoxCellEditor(jcb));
        col.setSortable(true);
      }
      // create a combo box for authors, note that we can't add new
      // authors dynamically
      if (Const.XML_AUTHOR.equals(sIdentifier)) {
        JComboBox jcb = new JComboBox(AuthorManager.getAuthorsList());
        jcb.setEditable(true);
        AutoCompleteDecorator.decorate(jcb);
        col.setCellEditor(new ComboBoxCellEditor(jcb));
      }
      // create a button for playing
      else if (Const.XML_PLAY.equals(sIdentifier)) {
        col.setMinWidth(PLAY_COLUMN_SIZE);
        col.setMaxWidth(PLAY_COLUMN_SIZE);
      } else if (Const.XML_TRACK_RATE.equals(sIdentifier)) {
        col.setMinWidth(RATE_COLUMN_SIZE);
        col.setMaxWidth(RATE_COLUMN_SIZE);
      }
    }
  }

  /**
   * Detect property change
   */
  public void itemStateChanged(ItemEvent ie) {
    if (ie.getSource() == jcbProperty) {
      sAppliedFilter = jtfValue.getText();
      sAppliedCriteria = getApplyCriteria();
      applyFilter(sAppliedCriteria, sAppliedFilter);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
   */
  public void tableChanged(TableModelEvent e) {
    // Check the table change event has not been generated by a
    // fireModelDataChange call
    if (e.getColumn() < 0) {
      return;
    }
    String sKey = model.getIdentifier(e.getColumn());
    Object oValue = model.getValueAt(e.getFirstRow(), e.getColumn());
    /* can be Boolean or String */
    Item item = model.getItemAt(e.getFirstRow());
    try {
      // file filter used by physical table view to change only the
      // file, not all files associated with the track
      Set<File> filter = null;
      if (item instanceof File) {
        filter = new HashSet<File>();
        filter.add((File) item);
      }
      Item itemNew = ItemManager.changeItem(item, sKey, oValue, filter);
      model.setItemAt(e.getFirstRow(), itemNew); // update model
      // user message
      InformationJPanel.getInstance().setMessage(
          Messages.getString("PropertiesWizard.8") + ": " + ItemManager.getHumanType(sKey),
          InformationJPanel.INFORMATIVE);
      // Require refresh of all tables
      Properties properties = new Properties();
      properties.put(Const.DETAIL_ORIGIN, AbstractTableView.this);
      ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH, properties));

    } catch (NoneAccessibleFileException none) {
      Messages.showErrorMessage(none.getCode());
      ((JajukTableModel) jtable.getModel()).undo(e.getFirstRow(), e.getColumn());
    } catch (CannotRenameException cre) {
      Messages.showErrorMessage(cre.getCode());
      ((JajukTableModel) jtable.getModel()).undo(e.getFirstRow(), e.getColumn());
    } catch (JajukException je) {
      Log.error("104", je);
      Messages.showErrorMessage(104, je.getMessage());
      ((JajukTableModel) jtable.getModel()).undo(e.getFirstRow(), e.getColumn());
    }
  }

  /**
   * Table initialization after table display
   * 
   */
  abstract void initTable();

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    // Editable state
    if (e.getSource() == jtbEditable) {
      Conf.setProperty(editableConf, Boolean.toString(jtbEditable.isSelected()));
      model.setEditable(jtbEditable.isSelected());
      return;
    }

  }

}
