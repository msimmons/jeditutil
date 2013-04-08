package net.contrapt.jeditutil.process;

import net.contrapt.jeditutil.selector.UtilSelector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

/**
 * Selector for listing and manipulating running processes
 */
public class ProcessSelector extends UtilSelector<Object, ProcessRunner> {

   public ProcessSelector() {
      super(null);
   }

   public String getTitle() {
      return "Process Selector";
   }

   public Map<String, ProcessRunner> loadValueMap() {
      Map<String, ProcessRunner> processes = new LinkedHashMap<String, ProcessRunner>();
      for (ProcessRunner r : ProcessRunner.getProcesses()) {
         processes.put(r.toString(), r);
      }
      return processes;
   }

   public String getDefault() {
      return null;
   }

   public ListCellRenderer getListCellRenderer() {
      return new DefaultListCellRenderer() {
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            ProcessRunner runner = getValue(value.toString());
            if (runner.isStarting()) label.setForeground(Color.ORANGE);
            else if (runner.isRunning()) label.setForeground(Color.GREEN);
            else if (runner.hasErrors()) label.setForeground(Color.RED);
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
            if (runner == null) return;
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
            if (runner == null) return;
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
            if (runner == null) return;
            runner.killProcess();
         }
      };
      currentAction.putValue(currentAction.MNEMONIC_KEY, KeyEvent.VK_K);
      setCloseDialog(currentAction, false);
      actions.add(currentAction);
      // All done
      return actions;
   }
}
