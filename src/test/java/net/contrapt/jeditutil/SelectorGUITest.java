package net.contrapt.jeditutil;

import java.util.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.testng.annotations.*;

/**
* Interactive tests of GUI components; these should be excluded from
* automated tests since they display GUI components
*/
public class SelectorGUITest {

   static Map<String,String> testMap = new TreeMap<String,String>();
   {
      testMap.put("foo (/net/contrapt/phlp/foo.java)","foo");
      testMap.put("bar","bar");
      testMap.put("baz","baz");
      testMap.put("zip","zip");
      testMap.put("UPP","zap");
      testMap.put("DOW","zap");
      testMap.put("OVE","zap");
      testMap.put("jEdit","JEDIT");
      testMap.put("fooBar","foobar");
   }
   static Map<String,String> parentMap = new TreeMap<String,String>();
   {
      parentMap.put("1","P1");
      parentMap.put("2","P2");
      parentMap.put("3","P3");
      parentMap.put("4","P4");
      parentMap.put("5","P5");
   }

   private List<Action> actions;
   private JMenuBar menuBar;
   private JPopupMenu popupMenu;
   private Action actionChosen;

   /**
   * Default constructor creates various components
   */
   public SelectorGUITest() {
      createActions();
   }

   /**
   * An action
   */
   private void createActions() {
      actions = new ArrayList<Action>();
      Action action = new AbstractAction("actionOne") {
         public void actionPerformed(ActionEvent e) {
            actionChosen=this;
         }
      };
      action.putValue(action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, 2));
      actions.add(action);
      // Another action
      action = new AbstractAction("Action Two") {
         public void actionPerformed(ActionEvent e) {
            actionChosen=this;
         }
      };
      action.putValue(action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, 2));
      actions.add(action);
   }

   @Test(groups={"gui"})
   public void testSelector() {
      ValueSelector<String,String> s = new ValueSelector<String,String>() {
         String parent = "P3";
         public boolean isCaseSensitive() { return false; }
         public String getTitle() { return "Test Selector"; }
         public Map<String, String> loadValueMap() { return testMap; }
         public String getDefault() { return ""; }
         public String getParentLabel() { return "Label:"; }
         public String getParentKey() { return "3"; }
         public Map<String,String> getParents() { return parentMap; }
         public void setParent(String parent) { this.parent = parent; }
         public String getParent() { return this.parent; }
      };
      ValueSelectionDialog.open(null, s);
      System.out.println("Selected: "+s.getSelectedString());
   }

   @Test(groups={"gui"})
   public void noParentSelector() {
      ValueSelector<String,String> s = new ValueSelector<String,String>() {
         public boolean isCaseSensitive() { return false; }
         public String getTitle() { return "No Parent Selector"; }
         public Map<String, String> loadValueMap() { return testMap; }
         public String getDefault() { return ""; }
      };
      ValueSelectionDialog.open(null, s);
      System.out.println("Selected: "+s.getSelectedString());
   }

   @Test(groups={"gui"})
   public void caseSensitiveSelector() {
      ValueSelector<String,String> s = new ValueSelector<String,String>() {
         public boolean isCaseSensitive() { return true; }
         public String getTitle() { return "Case Sensitive Selector"; }
         public Map<String, String> loadValueMap() { return testMap; }
         public String getDefault() { return ""; }
      };
      ValueSelectionDialog.open(null, s);
      System.out.println("Selected: "+s.getSelectedString());
   }

   @Test(groups={"gui"})
   public void loadedSelector() {
      actionChosen=null;
      ValueSelector<String,String> s = new ValueSelector<String,String>() {
         public boolean isCaseSensitive() { return true; }
         public String getTitle() { return "Action Selector"; }
         public Map<String, String> loadValueMap() { return testMap; }
         public String getDefault() { return ""; }
         public Collection<Action> getActions() { return actions; }
      };
      ValueSelectionDialog.open(null, s);
      String action = (actionChosen==null) ? null : (String)actionChosen.getValue(actionChosen.NAME);
      System.out.println("Selected: "+s.getSelectedString()+"; Action: "+action);
   }

   @Test(groups={"gui"})
   public void undecoratedSelector() {
      actionChosen=null;
      ValueSelector<String,String> s = new ValueSelector<String,String>() {
         public boolean isCaseSensitive() { return true; }
         public String getTitle() { return "Action Selector"; }
         public Map<String, String> loadValueMap() { return testMap; }
         public String getDefault() { return ""; }
         public Collection<Action> getActions() { return actions; }
      };
      ValueSelectionDialog.open(null, s, true);
      String action = (actionChosen==null) ? null : (String)actionChosen.getValue(actionChosen.NAME);
      System.out.println("Selected: "+s.getSelectedString()+"; Action: "+action);
   }

   @Test(groups={"gui"},enabled=true)
   public void completionDialog() {
   }

	@Test(groups={"gui"},enabled=true)
	public void treeListRenderer() {
	}
}
