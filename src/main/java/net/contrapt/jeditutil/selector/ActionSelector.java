package net.contrapt.jeditutil.selector;

import org.gjt.sp.jedit.EditAction;
import org.gjt.sp.jedit.jEdit;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Allow selection of beanshell actions
 */
public class ActionSelector extends UtilSelector<Object, EditAction> {

   public ActionSelector(String defaultValue) {
      super(defaultValue);
   }

   public String getTitle() {
      return "Action Selector";
   }

   public Map<String, EditAction> loadValueMap() {
      Map<String, EditAction> actions = new TreeMap<String, EditAction>();
      for (String action : (String[]) jEdit.getActionNames()) {
         actions.put(action, jEdit.getAction(action));
      }
      return actions;
   }

   public String getDefault() {
      return defaultValue;
   }

}
