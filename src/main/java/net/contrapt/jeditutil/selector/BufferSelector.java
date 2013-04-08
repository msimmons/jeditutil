package net.contrapt.jeditutil.selector;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditAction;
import org.gjt.sp.jedit.jEdit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;

/**
 * Allow selection of open buffers
 */
public class BufferSelector extends UtilSelector<Object, Buffer> {

   public BufferSelector(String defaultValue) {
      super(defaultValue);
   }

   public String getTitle() {
      return "Buffer Selector";
   }

   public Map<String, Buffer> loadValueMap() {
      Map<String, Buffer> bufs = new TreeMap<String, Buffer>();
      for (Buffer b : jEdit.getBuffers()) {
         String key = b.getName() + " (" + b.getDirectory() + ")";
         bufs.put(key, b);
      }
      return bufs;
   }

   public String getDefault() {
      return defaultValue;
   }

   public ListCellRenderer getListCellRenderer() {
      return new DefaultListCellRenderer() {
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Buffer buffer = getValue(value.toString());
            label.setIcon(buffer.getIcon());
            return label;
         }
      };
   }

   public Collection<Action> getActions() {
      java.util.List<Action> actions = new ArrayList<Action>();
      Action currentAction;
      // Close a buffer
      currentAction = new AbstractAction("Close") {
         public void actionPerformed(ActionEvent e) {
            Buffer buffer = getCurrentValue();
            if (buffer == null) return;
            jEdit.closeBuffer(jEdit.getActiveView(), buffer);
            if (buffer.isClosed()) removeCurrentValue();
         }
      };
      currentAction.putValue(currentAction.MNEMONIC_KEY, KeyEvent.VK_C);
      setCloseDialog(currentAction, false);
      actions.add(currentAction);
      // Save a buffer
      currentAction = new AbstractAction("Save") {
         public void actionPerformed(ActionEvent e) {
            Buffer buffer = getCurrentValue();
            if (buffer == null) return;
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
            if (buffer == null) return;
            jEdit.getActiveView().goToBuffer(buffer);
         }
      };
      currentAction.putValue(currentAction.MNEMONIC_KEY, KeyEvent.VK_O);
      setCloseDialog(currentAction, false);
      actions.add(currentAction);
      // All done
      return actions;
   }

}
