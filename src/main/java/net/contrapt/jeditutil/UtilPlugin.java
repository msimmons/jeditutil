package net.contrapt.jeditutil;

import net.contrapt.jeditutil.pluginpanel.DiffPluginPanel;
import net.contrapt.jeditutil.pluginpanel.PluginPanel;
import net.contrapt.jeditutil.process.ProcessRunner;
import net.contrapt.jeditutil.process.ProcessSelector;
import net.contrapt.jeditutil.selector.*;
import net.contrapt.jeditutil.service.CompletionService;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.util.Log;

import java.util.*;
import javax.swing.JPopupMenu;
import javax.swing.JFileChooser;
import java.awt.Point;
import javax.swing.JMenuItem;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import java.io.File;
import java.awt.Component;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;

import net.contrapt.jeditutil.model.*;


/**
* A utility plugin for jEdit.  Offers various utilities for direct use or for use
* by other plugins including a reusable value selection dialog framework with keyboard centric
* search and filtering; implementations of the value selector for choosing buffers, recent files,
* jEdit actions
*/
public class UtilPlugin extends EBPlugin {
   
   //
   // PROPERTIES
   //
   public static final String NAME = "jeditutil";
   public static final String PROPERTY_PREFIX = "plugin."+NAME;
   public static final String OPTION_PREFIX = "options."+NAME;
   public static final String MENU = PROPERTY_PREFIX+"menu";
   
   public static final String EXEC_ACTION_ERROR = PROPERTY_PREFIX+"exec-action-error";
   public static final String REINIT_ERROR = PROPERTY_PREFIX+"reinit-error";

   public static final String BUFFER_DOCKABLE = PROPERTY_PREFIX+".buffer";

   public static final String DATA_DIR = "jeditutil";
   public static final String DATA_FILE = "jeditutil.json";

   public enum ActionEnum {
      BUFFERS,
      RECENT,
      LOCAL,
		FILE_DIFF,
      ACTIONS,
      PROCESSES,
      DUMP_KS,
      LOAD_KS,
      MODE_MENU,
      REINIT,
      FORWARD,
      BACKWARD
   }
   
   private static UtilPlugin INSTANCE;
   
   /** The configuration data */
   private ConfigurationData data;

   /** Are we initialized? */
   private boolean initialized = false;

   /** The last buffer we handled change message for */
   private Buffer lastBuffer;

   /** The last buffer that we switched from */
   private String switchFromBuffer;

   /** The last action chosen by user */
   private String lastAction;

   /** Each buffer gets a tabbed pane for buffer related panels */
   private Map<EditPane, EditPanePanel> editPanePanels = new HashMap<EditPane, EditPanePanel>();

   /** A navigation history manager for forward and backward navigation */
   private NavigationHistoryManager navigationManager = new NavigationHistoryManager();

   //
   // CONSTRUCTORS
   //
   
   //
   // IMPLEMENT EditPlugin
   //
   
   /**
   * Startup routine; set the instance variable for use by others and create a repository
   */
   @Override
   public void start() {
      INSTANCE = this;
      init(null);
   }
   
   /**
   * Wrap up, save files, release resources etc.
   */
   @Override
   public void stop() {
      UtilityPanel.shutdown();
      MenuAction.shutdown();
      INSTANCE = null;
      //TODO write the data file
   }
   
   //
   // STATIC METHODS
   //

   /**
   * Return the constructed instance of this plugin (as assigned in the start() method)
   */
   public static UtilPlugin getInstance() {
      return INSTANCE; 
   }

   //
   // PUBLIC METHODS
   //

   /**
   * Initialize the plugin
   *
   * @param view The jedit view this was called from
   */
   public void init(View view) {
      if ( initialized ) return;
      if ( view == null ) view = jEdit.getActiveView();
      String dataFile = ( jEdit.getSettingsDirectory() == null ) ? 
         DATA_FILE : 
         jEdit.getSettingsDirectory()+File.separator+DATA_DIR+File.separator+DATA_FILE;
      Log.log(Log.DEBUG, this, "Starting jEdit Util using "+dataFile);
      try {
         data = BaseModel.readData(dataFile, ConfigurationData.class);
      }
      catch (Exception e) {
         if ( view == null ) {
            Log.log(Log.DEBUG, this, "Error initializing plugin: "+e);
            e.printStackTrace();
         }
         else handleException(view, REINIT_ERROR, new Object[] {});
      }
      if ( data == null ) data = new ConfigurationData();
      initialized = true;
      MenuAction.initialize(data.getGlobalMenus());
      if ( view == null ) return;
      for ( EditPane pane : view.getEditPanes() ) addEditPanePanel(pane);
   }
   
   /**
   * Reinitialize the plugin
   */
   public void reinit(View view) {
      initialized = false;
      data = null;
      MenuAction.shutdown();
      init(view);
   }
   
   /**
   * Run the given action on the given file
   */
   public void performAction(View view, ActionEnum action) {
      try {
         switch ( action ) {
            case BUFFERS:
               switchToBuffer(view);
               break;
            case RECENT:
               chooseRecentFile(view);
               break;
            case ACTIONS:
               chooseAction(view);
               break;
            case DUMP_KS:
               dumpShortcuts(view);
               break;
            case LOAD_KS:
               loadShortcuts(view);
               break;
            case MODE_MENU:
               showModeMenu(view);
               break;
            case LOCAL:
               showLocalDiff(view);
               break;
            case FILE_DIFF:
               showFileDiff(view);
               break;
            case PROCESSES:
               showProcesses(view);
               break;
            case FORWARD:
               navigateForwards(view);
               break;
            case BACKWARD:
               navigateBackwards(view);
               break;
         }
      }
      catch (Exception e) {
         e.printStackTrace();
         handleException(view, EXEC_ACTION_ERROR, new Object[] {action, e.getMessage()+"\n"+e.getCause()});
      }
   }

   /**
   * Show a code completion popup
   */
   public void showCompletionDialog(View view) {
      CompletionSelector selector = findCompletionSelector(view);
      if ( selector == null ) {
         view.getTextArea().selectWord();
         selector = CompletionSelector.getDefaultCompletionSelector(view, view.getTextArea().getSelectedText());
      }
      else {
         int selectionBegin = view.getTextArea().getCaretPosition() - (selector.getDefault()==null ? 0 : selector.getDefault().length());
         view.getTextArea().extendSelection(selectionBegin, view.getTextArea().getCaretPosition());
      }
      int columnOffset = -1*(( selector.getDefault() == null ) ? 0 : selector.getDefault().length());
      Point location = getPointFromCaret(view, 0, columnOffset, true);
      CompletionDialog.open(view, location, selector);
      int offset = view.getTextArea().getCaretPosition();
      if ( selector.getSelectedObject() != null ) {
         if ( view.getTextArea().getSelectedText() != null ) view.getTextArea().delete();
         offset = view.getTextArea().getCaretPosition();
      }
      view.getBuffer().insert(offset, selector.getCompletionString());
   }

   /**
   * Return the <code>PluginPanel</code> for the current buffer in the given view if it exists.
   *
   * @param view The view requesting the plugin panel
   * @param clazz The class of plugin panel
   */
   public PluginPanel getPluginPanel(View view, Class clazz) {
      EditPanePanel ep = editPanePanels.get(view.getEditPane());
      if ( ep == null ) return null;
      return ep.getPluginPanel(view.getBuffer(), clazz);
   }

   /**
   * Add a dock panel to this view's current buffer
   * This method is called by client plugins to add a new panel
   */
   public void addPluginPanel(View view, PluginPanel panel) {
      EditPanePanel ep = editPanePanels.get(view.getEditPane());
      if ( ep == null ) return;
      ep.addPluginPanel(view.getBuffer(), panel);
      ep.update(false);
   }

   /**
   * Remove the dock panel of the given class from the given buffer
   */
   public void removePluginPanel(Buffer buffer, Class cl) {
      View view = jEdit.getActiveView();
      EditPanePanel ep = editPanePanels.get(view.getEditPane());
      if ( ep == null ) return;
      ep.removePluginPanel(buffer, cl);
      ep.update(false);
   }

   /**
   * Remove all dock panels of the given class; a plugin should call this to remove
   * all of its dock panels
   * This method is called by client plugins to remove all panels that it added
   */
   public void removePluginPanels(Class cl) {
      Log.log(Log.DEBUG, this, "removePluginPanels: "+cl);
      EditPanePanel ep = editPanePanels.get(jEdit.getActiveView().getEditPane());
      if ( ep == null ) return;
      ep.removePluginPanels(cl);
   }
   
   /**
   * Remove all plugin panels for the given buffer
   */
   void removePluginPanels(Buffer buffer) {
      Log.log(Log.DEBUG, this, "removePluginPanels: "+buffer);
      EditPanePanel ep = editPanePanels.get(jEdit.getActiveView().getEditPane());
      if ( ep == null ) return;
      ep.removePluginPanels(buffer);
   }

   /**
   * Show a popup menu below the current caret position; I derived this from 
   * other jEdit code to show context menu
   */
   public static void showPopupMenu(View view, JPopupMenu menu) {
//      Point location = getPointFromCaret(view, 1, 0, false);
      Point location = view.getTextArea().getPainter().getLocation();
      if ( location == null ) return;
      menu.show(view.getTextArea().getPainter(), location.x, location.y);
   }

   /**
   * Add an entry to the navigation history -- plugins should call this to record
   * navigation history when appropriate
   *
   * @param view The current view -- the current buffer and position in this view will be added
   */
   public static void addNavigationHistory(View view) {
      if ( INSTANCE == null ) return;
      INSTANCE.navigationManager.addEntry(view);
   }

   /**
   * Request focus on the lower component of <code>EditPanePanel</code>
   */
   public void focusOnPluginPanel(View view) {
      EditPanePanel panel = editPanePanels.get(view.getEditPane());
      if ( panel==null ) return;
      panel.focusOnPluginPanel();
   }

   /**
   * When a process starts running, update count of running/error processes
   */
   public static void processStarted() {
      if ( INSTANCE == null ) return;
      INSTANCE.updateEditPanePanel(jEdit.getActiveView().getEditPane(), false);
   }

   /**
   * When a process has finished running, update buffer status on the current
   * buffer
   */
   public static void processFinished() {
      if ( INSTANCE == null ) return;
      INSTANCE.updateEditPanePanel(jEdit.getActiveView().getEditPane(), false);
   }

   /**
   * Switch to the buffer selected by the user
   */
   private void switchToBuffer(View view) {
      Buffer buf = promptForBuffer(view);
      if ( buf == null ) return;
      view.goToBuffer(buf);
   }

   /**
   * Prompt user for buffer to switch to
   */
   private Buffer promptForBuffer(View view) {
      String bufferName = view.getBuffer().getName();
      ValueSelector<Object,Buffer> selector = new BufferSelector(switchFromBuffer);
      ValueSelectionDialog.open(view, selector);
      Buffer buf = selector.getSelectedObject();
      switchFromBuffer = (buf==null) ? switchFromBuffer : bufferName;
      return buf;
   }

   /**
   * Allow choice of a recent file
   */
   private void chooseRecentFile(View view) {
      BufferHistory.Entry entry = promptForRecentFile(view);
      if ( entry == null ) return;
      // Open the recent file and move to the caret position
      Buffer buf = jEdit.openFile(view, entry.path);
      try {
         view.getTextArea().setCaretPosition(entry.caret);
      }
      catch (NullPointerException e) {
         // jEdit bug?
         Log.log(Log.DEBUG, this, "chooseRecentFile setCaretPosition(): "+e);
      }
   }

   /**
   * Prompt user for a recent file
   */
   private BufferHistory.Entry promptForRecentFile(View view) {
      ValueSelector<Object,BufferHistory.Entry> selector = new RecentSelector(null);
      ValueSelectionDialog.open(view, selector);
      return selector.getSelectedObject();
   }

   /**
   * Allow user to choose an action and invoke it
   */
   private void chooseAction(View view) {
      EditAction action = promptForAction(view);
      if ( action == null ) return;
      // Invoke the action on this view
      action.invoke(view);
   }

   /**
   * Prompt user for an action
   */
   private EditAction promptForAction(View view) {
      ValueSelector<Object,EditAction> selector = new ActionSelector(lastAction);
      ValueSelectionDialog.open(view, selector);
      EditAction action = selector.getSelectedObject();
      if ( action != null ) lastAction = action.getName();
      return action;
   }

   /**
   * Choose a local version of the current buffer and show a diff between
   * version and current
   */
   private void showLocalDiff(View view) {
      ValueSelector<Object,File> selector = new BackupSelector(view.getBuffer().getPath());
      ValueSelectionDialog.open(view, selector);
      File backup = selector.getSelectedObject();
      if ( backup == null ) return;
      List<String> files = new ArrayList<String>();
      files.add(backup.getPath());
      files.add(view.getBuffer().getPath());
      DiffPluginPanel.showDiff(view, view.getBuffer().getName(), files);
   }

   /**
   * Show a diff between the current buffer and a user selected file
   */
   private void showFileDiff(View view) {
   	File dir = new File(view.getBuffer().getDirectory());
		JFileChooser chooser = new JFileChooser(dir);
		if ( chooser.showOpenDialog(view) != JFileChooser.APPROVE_OPTION ) return;
		File selectedFile = chooser.getSelectedFile();
      if ( selectedFile == null ) return;
      List<String> files = new ArrayList<String>();
      files.add(selectedFile.getPath());
      files.add(view.getBuffer().getPath());
      DiffPluginPanel.showDiff(view, view.getBuffer().getName(), files);
   }

   /**
   * Choose a process from the list of running processes and show its output in a buffer
   */
   private void showProcesses(View view) {
      ValueSelector<Object,ProcessRunner> selector = new ProcessSelector();
      ValueSelectionDialog.open(view, selector);
      ProcessRunner runner = selector.getSelectedObject();
      if ( runner == null ) return;
      runner.loadBuffer();
   }

   /**
   * Dump all jEdit keyboard shortcuts to a buffer orderd by property
   * name.  This allows moving keyboard shortcut definitions around to
   * other computers.
   */
   private void dumpShortcuts(View view) {
      Enumeration<String> names = (Enumeration<String>)jEdit.getProperties().propertyNames();
      SortedSet<String> sortedNames = new TreeSet<String>();
      while ( names.hasMoreElements() ) {
         String name = names.nextElement();
         if ( name.endsWith(".shortcut") || name.endsWith(".shortcut2") ) {
            sortedNames.add(name);
         }
      }
      Buffer buf = jEdit.newFile(view);
      int offset = 0;
      for ( String name : sortedNames ) {
         String value = jEdit.getProperty(name);
         String line = name+"="+value+"\n";
         buf.insert(offset, line);
         offset += line.length();
      }
      buf.setDirty(false);
      view.getTextArea().goToBufferStart(false);
   }

   /**
   * Load keyboard shortcuts from the current buffer; in order for the 
   * new shortcuts to take effect, you must open the global options and
   * click "OK" or "Apply" on the shortcuts tab.
   */
   private void loadShortcuts(View view) {
      Buffer buf = view.getBuffer();
      int lines = buf.getLineCount();
      for ( int i=0; i<lines; i++ ) {
         String[] nameValue = buf.getLineText(i).split("=");
         if ( nameValue.length < 1 ) continue;
         jEdit.setProperty(nameValue[0], (nameValue.length==2) ? nameValue[1] : "");
      }
      //TODO Pop a dialog saying to click "OK" or "Apply" for changes to take place
      EditAction action = jEdit.getAction("global-options");
      action.invoke(view);
   }

   /**
   * Create a custom context menu if there is one defined for the current buffer's
   * mode
   */
   private void showModeMenu(View view) {
      String mode = view.getBuffer().getMode().getName();
      MenuDef def = findModeMenu(mode);
      if ( def == null ) return;
      JPopupMenu menu = new JPopupMenu(def.getName());
      for ( MenuItem item : def.getMenuItems() ) {
         menu.add(createMenuItem(item));
      }
      showPopupMenu(view, menu);
   }

   /**
   * Find a mode menu for the given mode
   */
   private MenuDef findModeMenu(String mode) {
      MenuDef starMenu = null;
      for ( MenuDef def : data.getModeMenus() ) {
         for ( String m : def.getModes() ) {
            if ( m.equals(mode) ) return def;
            if ( m.equals("*") ) starMenu = def;
         }
      }
      return starMenu;
   }

   /**
   * Show the given user defined global menu
   */
   public void showGlobalMenu(View view, String name) {
      MenuDef def = null;
      for ( MenuDef d : data.getGlobalMenus() ) {
         if ( d.getName().equals(name) ) {
            def = d;
            break;
         }
      }
      if ( def == null || !def.getName().equals(name) ) return;
      JPopupMenu menu = new JPopupMenu(def.getName());
      for ( MenuItem item : def.getMenuItems() ) {
         menu.add(createMenuItem(item));
      }
      showPopupMenu(view, menu);
   }

   /**
   * Create a menu item from an item definition
   */
   private Component createMenuItem(MenuItem item) {
      if ( item.isSeparator() ) return new JPopupMenu.Separator();
      if ( item.getMenuItems().size() > 0 ) return createSubMenu(item);
      String name = item.getName();
      String action = item.getAction();
      Integer key = getKeyCode(item.getAccelerator());
      return createMenuItem(name, key, action);
   }

   /**
   * Navigate forward
   */
   private void navigateForwards(View view) {
      navigationManager.addEntry(view);
      navigationManager.navigateForwards(view);
   }

   /**
   * Navigate backwards
   */
   private void navigateBackwards(View view) {
      navigationManager.addEntry(view);
      navigationManager.navigateBackwards(view);
   }

   /**
   * Implement the edit bus listener interface
   */
   @Override
   public void handleMessage(EBMessage message) {
      if ( message instanceof BufferUpdate ) handleBufferUpdate((BufferUpdate)message);
      else if ( message instanceof EditPaneUpdate ) handleEditPaneUpdate((EditPaneUpdate)message);
      else if ( message instanceof ViewUpdate ) handleViewUpdate((ViewUpdate)message);
      else if ( message instanceof PluginUpdate ) handlePluginUpdate((PluginUpdate)message);
   }

   /**
   * Handle buffer update messages
   */
   private void handleBufferUpdate(BufferUpdate message) {
      if ( message.getWhat() == BufferUpdate.DIRTY_CHANGED ) {
         updateEditPanePanel(jEdit.getActiveView().getEditPane(), true);
      }
      if ( message.getWhat() == BufferUpdate.CLOSED ) {
         removePluginPanels(message.getBuffer());
      }
   }
   
   /**
   * Handle edit pane update messages
   */
   private void handleEditPaneUpdate(EditPaneUpdate message) {
      // Track buffer switches and show the appropriate dockable panel
      if ( message.getWhat() == EditPaneUpdate.BUFFER_CHANGED ) {
         updateEditPanePanel(message.getEditPane(), true);
      }
      else if ( message.getWhat() == EditPaneUpdate.CREATED ) {
         addEditPanePanel(message.getEditPane());
         updateEditPanePanel(message.getEditPane(), true);
      }
      else if ( message.getWhat() == EditPaneUpdate.DESTROYED ) {
         removeEditPanePanel(message.getEditPane());
      }
   }

   /**
   * Handle view creation
   */
   private void handleViewUpdate(ViewUpdate message) {
      if ( message.getWhat() == ViewUpdate.CREATED ) {
         addEditPanePanel(message.getView().getEditPane());
         updateEditPanePanel(message.getView().getEditPane(), true);
      }
   }

   /**
   * Implement the edit bus listener to handle plugin events
   */
   private void handlePluginUpdate(PluginUpdate message) {
      if ( message.isExiting() ) return;
      if ( message.getWhat().equals(PluginUpdate.LOADED) ) {
         UtilityPanel.addPluginTabs();
         for ( Map.Entry<EditPane,EditPanePanel> entry : editPanePanels.entrySet() ) 
            entry.getValue().addPluginComponents(entry.getKey());
      }
      if ( message.getWhat().equals(PluginUpdate.DEACTIVATED) ) {
         UtilityPanel.removePluginTab(message.getPluginJAR());
         for ( EditPanePanel panel : editPanePanels.values() ) 
            panel.removePluginComponent(message.getPluginJAR());
      }
   }

   /**
   * Update the buffer status bar on buffer change if necessary
   */
   private void updateEditPanePanel(EditPane pane, boolean bufferChanging) {
      EditPanePanel t = editPanePanels.get(pane);
      if ( t == null ) return;
      // If the read-only attribute of the underlying file has changed
      File file = new File(pane.getBuffer().getPath());
      if ( (file.exists() && !file.canWrite()) ^ pane.getBuffer().isReadOnly() ) {
         pane.getBuffer().setReadOnly(!file.canWrite());
         pane.getBuffer().reload(pane.getView());
         bufferChanging=true;
      }
      t.update(bufferChanging);
   }

   /**
   * Add a buffer status component to the given edit pane
   */
   private void addEditPanePanel(EditPane pane) {
      if ( pane == null ) pane = jEdit.getActiveView().getEditPane();
      if ( pane == null ) return;
      if ( editPanePanels.containsKey(pane) ) return;
      EditPanePanel t = new EditPanePanel(pane);
      Log.log(Log.DEBUG, this, "Adding "+t+" for "+pane);
      editPanePanels.put(pane, t);
      t.update(true);
   }

   /**
   * Remove the <code>EditPanePanel</code> associated with the given 
   * <code>EditPane</code>
   */
   private void removeEditPanePanel(EditPane pane) {
      EditPanePanel panel = editPanePanels.remove(pane);
      if ( panel == null ) return;
      Log.log(Log.DEBUG, this, "Removing "+panel+" for "+pane);
      panel.removeAll();
   }

   /**
   * Return the <code>EditPanePanel</code> for the given edit pane
   */
   EditPanePanel getEditPanePanel(EditPane pane) {
      return editPanePanels.get(pane);
   }

   /**
   * Handle the exception by showing an error dialog and logging an error
   */
   private void handleException(View view, String propertyName, Object[] params) {
      GUIUtilities.error(view, propertyName, params);
      GUIUtilities.requestFocus(view, view);
   }

   /**
   * Creates a menu item that will execute a jedit action
   */
   private JMenuItem createMenuItem(String name, Integer key, String action) {
      Action a = new AbstractAction(name) {
         public void actionPerformed(ActionEvent e) {
            EditAction action = (EditAction)((JMenuItem)e.getSource()).getAction().getValue("jedit.action");
            if ( action != null ) action.invoke(jEdit.getActiveView());
         }
      };
      a.putValue("jedit.action", jEdit.getAction(action));
      JMenuItem i = new JMenuItem(a);
      if ( key != null ) i.setMnemonic(key);
      return i;
   }

   /**
   * Creates a sub-menu 
   */
   private JMenu createSubMenu(MenuItem parent) {
      JMenu menu = new JMenu(parent.getName());
      Integer key = getKeyCode(parent.getAccelerator());
      if ( key != null ) menu.setMnemonic(key);
      for ( MenuItem item : parent.getMenuItems() ) {
         menu.add(createMenuItem(item));
      }
      return menu;
   }

   /**
   * Return the integer representing the given virtual key string as defined in <code>KeyEvent</code>
   *
   * @param keyCodeString A string of the form VK_?? to convert into the virtual key identifier
   * @return The virtual key code
   */
   private int getKeyCode(String keyCodeString) {
      try {
         Integer key = ( keyCodeString == null ) ? null : KeyEvent.class.getField(keyCodeString).getInt(null);
         return key;
      }
      catch (Exception e) {
         throw new IllegalStateException("Attempting to get value of virtual key "+keyCodeString, e);
      }
   }

   /**
   * Return the <code>Point</code> that is one line below the current caret position
   *
   * @param view The current view
   * @param toScreen Whether or not to convert to screen coordinates
   * @return A point either in textArea painter coordinate system or converted to screen coords
   */
   private static Point getPointFromCaret(View view, int lineOffset, int columnOffset, boolean toScreen) {
      int line = view.getTextArea().getCaretLine();
      int column = view.getTextArea().getCaretPosition()-view.getTextArea().getLineStartOffset(line)+columnOffset;
      Point belowCaret = view.getTextArea().offsetToXY(line, column, new Point(0,0));
      if ( belowCaret == null ) return null;
      int charHeight = view.getTextArea().getPainter().getFontMetrics().getHeight();
      belowCaret.y += (charHeight*lineOffset);
      if ( toScreen ) SwingUtilities.convertPointToScreen(belowCaret, view.getTextArea().getPainter());
      return belowCaret;
  }

  /**
  * Find a code completion <code>ValueSelector</code> for the current buffer's mode
  */
  private CompletionSelector findCompletionSelector(View view) {
     Mode mode = view.getBuffer().getMode();
     String serviceName = CompletionService.class.getName();
     for ( String providerName : ServiceManager.getServiceNames(serviceName) ) {
        CompletionService service = (CompletionService)ServiceManager.getService(serviceName, providerName);
        if ( service == null ) continue;
        if ( !service.supportsMode(mode.getName()) ) continue;
        return service.getCompletionSelector(view);
     }
     return null;
  }

}
