package net.contrapt.jeditutil.selector;

import org.gjt.sp.jedit.BufferHistory;
import org.gjt.sp.jedit.EditAction;
import org.gjt.sp.jedit.jEdit;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Allow selection of recent files
 */
public class RecentSelector extends UtilSelector<Object, BufferHistory.Entry> {

   public RecentSelector(String defaultValue) {
      super(defaultValue);
   }

   public String getTitle() {
      return "Recent File Selector";
   }

   public Map<String, BufferHistory.Entry> loadValueMap() {
      Map<String, BufferHistory.Entry> entries = new TreeMap<String, BufferHistory.Entry>();
      for (BufferHistory.Entry entry : (List<BufferHistory.Entry>) BufferHistory.getHistory()) {
         String[] parts = entry.path.split(regexFileSeparator());
         String key = parts[parts.length - 1] + " (" + entry.path + ")";
         entries.put(key, entry);
      }
      return entries;
   }

   public String getDefault() {
      return defaultValue;
   }

   private String regexFileSeparator() {
      return (File.separator.equals("\\")) ? "\\\\" : File.separator;
   }

}
