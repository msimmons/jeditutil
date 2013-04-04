package net.contrapt.jeditutil;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.JList;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.Action;
import javax.swing.AbstractAction;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.awt.Color;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.BufferHistory;
import org.gjt.sp.jedit.EditAction;
import org.gjt.sp.jedit.jEdit;

/**
* Implement various jEdit specific value selectors; open buffers, recent files,
* actions.  In addition, a selector for the buffer dockables
*/
public abstract class UtilSelector<P,V> extends ValueSelector<P,V> {
   
   //
   // Properties
   //
   private String defaultValue;

   //
   // Constructors
   //
   protected UtilSelector(String defaultValue) {
      this.defaultValue = defaultValue;
   }

   public boolean isCaseSensitive() { return false; }

   /**
   * Allow selection of open buffers
   */
   public static UtilSelector<Object,Buffer> getBufferSelector(final String defaultValue) {
      return new UtilSelector<Object,Buffer>(defaultValue) {
         public String getTitle() { return "Buffer Selector"; }
         public Map<String,Buffer> loadValueMap() {
            Map<String,Buffer> bufs = new TreeMap<String,Buffer>();
            for ( Buffer b : jEdit.getBuffers() ) {
               String key = b.getName()+" ("+b.getDirectory()+")";
               bufs.put(key, b);
            }
            return bufs;
         }
         public String getDefault() { return defaultValue; }
         public ListCellRenderer getListCellRenderer() {
            return new DefaultListCellRenderer() {
               public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,boolean cellHasFocus) {
                  JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                  Buffer buffer = getValue(value.toString());
                  label.setIcon(buffer.getIcon());
                  return label; 
               }
            };
         }
         public Collection<Action> getActions() {
            List<Action> actions = new ArrayList<Action>();
            Action currentAction;
            // Close a buffer
            currentAction = new AbstractAction("Close") {
               public void actionPerformed(ActionEvent e) {
                  Buffer buffer = getCurrentValue();
                  if ( buffer == null ) return;
                  jEdit.closeBuffer(jEdit.getActiveView(), buffer);
                  if ( buffer.isClosed() ) removeCurrentValue();
               }
            };
            currentAction.putValue(currentAction.MNEMONIC_KEY, KeyEvent.VK_C);
            setCloseDialog(currentAction, false);
            actions.add(currentAction);
            // Save a buffer
            currentAction = new AbstractAction("Save") {
               public void actionPerformed(ActionEvent e) {
                  Buffer buffer = getCurrentValue();
                  if ( buffer == null ) return;
                  buffer.save(jEdit.getActiveView(), null);
                  reloaded();
               }
            };
            currentAction.putValue(currentAction.MNEMONIC_KEY, KeyEvent.VK_S);
            setCloseDialog(currentAction, false);
            actions.add(currentAction);
            // Open a buffer
            currentAction = new AbstractAction("Open") {
               public void actionPerformed(ActionEvent e) {
                  Buffer buffer = getCurrentValue();
                  if ( buffer == null ) return;
                  jEdit.getActiveView().goToBuffer(buffer);
               }
            };
            currentAction.putValue(currentAction.MNEMONIC_KEY, KeyEvent.VK_O);
            setCloseDialog(currentAction, false);
            actions.add(currentAction);
            // All dond
            return actions;
         }
      };
   }

   /**
   * Allow selection of recent files
   */
   public static UtilSelector<Object,BufferHistory.Entry> getRecentFileSelector(final String defaultValue) {
      return new UtilSelector<Object,BufferHistory.Entry>(defaultValue) {
         public String getTitle() { return "Recent File Selector"; }
         public Map<String,BufferHistory.Entry> loadValueMap() {
            Map<String,BufferHistory.Entry> entries = new TreeMap<String,BufferHistory.Entry>();
            for ( BufferHistory.Entry entry : (List<BufferHistory.Entry>)BufferHistory.getHistory() ) {
               String[] parts = entry.path.split(regexFileSeparator());
               String key = parts[parts.length-1]+" ("+entry.path+")";
               entries.put(key, entry);
            }
            return entries;
         }
         public String getDefault() { return defaultValue; }
         private String regexFileSeparator() { return ( File.separator.equals("\\") ) ? "\\\\" : File.separator; }
      };
   }

   /**
   * Allow selection of bean-shell actions
   */
   public static UtilSelector<Object,EditAction> getActionSelector(final String defaultValue) {
      return new UtilSelector<Object,EditAction>(defaultValue) {
         public String getTitle() { return "Action Selector"; }
         public Map<String,EditAction> loadValueMap() {
            Map<String,EditAction> actions = new TreeMap<String,EditAction>();
            for ( String action : (String[])jEdit.getActionNames() ) {
               actions.put(action, jEdit.getAction(action));
            }
            return actions;
         }
         public String getDefault() { return defaultValue; }
      };
   }

   /**
   * Allow the selection of jedit backup versions of a given file;
   * defaultValue should be the full path of the file whose local versions we are looking
   * for
   */
   public static UtilSelector<Object,File> getBackupSelector(final String defaultValue) {
      return new UtilSelector<Object,File>(defaultValue) {
         File localFile = new File(defaultValue);
         DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         public String getTitle() { return "Local Versions - "+localFile.getName(); }
         public Map<String,File> loadValueMap() {
            FilenameFilter filter = getFilenameFilter();
            File dir = getBackupDir();
            Map<String,File> entries = new TreeMap<String,File>();
            for ( File file : dir.listFiles(filter) ) {
               String key = format.format(new Date(file.lastModified()));
               entries.put(key, file);
            }
            return entries;
         }
         public String getDefault() { return ""; }
         private FilenameFilter getFilenameFilter() {
            return new FilenameFilter() {
               public boolean accept(File dir, String name) {
                  name = name.replace(jEdit.getProperty("backup.suffix"),"");
                  name = name.replace(jEdit.getProperty("backup.prefix"),"");
                  return name.startsWith(localFile.getName());
               }
            };
         }
         private File getBackupDir() {
            String backupDir = jEdit.getProperty("backup.directory");
            String localDir = localFile.getParent().replace(":","");
            return new File(backupDir+File.separator+localDir);
         }
      };
   }

   /**
   * Get a list of running processes -- can switch to the output, refresh the
   * tail, close, kill
   */
   public static UtilSelector<Object,ProcessRunner> getProcessSelector() {
      return new UtilSelector<Object,ProcessRunner>(null) {
         public String getTitle() { return "Process Selector"; }
         public Map<String,ProcessRunner> loadValueMap() {
            Map<String,ProcessRunner> processes = new LinkedHashMap<String,ProcessRunner>();
            for ( ProcessRunner r : ProcessRunner.getProcesses() ) {
               processes.put(r.toString(), r);
            }
            return processes;
         }
         public String getDefault() { return null; }
         public ListCellRenderer getListCellRenderer() {
            return new DefaultListCellRenderer() {
               public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,boolean cellHasFocus) {
                  JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                  ProcessRunner runner = getValue(value.toString());
                  if ( runner.isStarting() ) label.setForeground(Color.ORANGE);
                  else if ( runner.isRunning() ) label.setForeground(Color.GREEN);
                  else if ( runner.hasErrors() ) label.setForeground(Color.RED);
                  return label; 
               }
            };
         }
         public Collection<Action> getActions() {
            List<Action> actions = new ArrayList<Action>();
            Action currentAction;
            // Open the output of the current process
            currentAction = new AbstractAction("Open") {
               public void actionPerformed(ActionEvent e) {
                  ProcessRunner runner = getCurrentValue();
                  if ( runner == null ) return;
                  runner.loadBuffer();
               }
            };
            currentAction.putValue(currentAction.MNEMONIC_KEY, KeyEvent.VK_O);
            setCloseDialog(currentAction, false);
            actions.add(currentAction);
            // Close the current process buffer
            currentAction = new AbstractAction("Close") {
               public void actionPerformed(ActionEvent e) {
                  ProcessRunner runner = getCurrentValue();
                  if ( runner == null ) return;
                  runner.closeBuffer();
               }
            };
            currentAction.putValue(currentAction.MNEMONIC_KEY, KeyEvent.VK_C);
            setCloseDialog(currentAction, false);
            actions.add(currentAction);
            // Kill the current process
            currentAction = new AbstractAction("Kill") {
               public void actionPerformed(ActionEvent e) {
                  ProcessRunner runner = getCurrentValue();
                  if ( runner == null ) return;
                  runner.killProcess();
               }
            };
            currentAction.putValue(currentAction.MNEMONIC_KEY, KeyEvent.VK_K);
            setCloseDialog(currentAction, false);
            actions.add(currentAction);
            // All done
            return actions;
         }
      };
   }

}
