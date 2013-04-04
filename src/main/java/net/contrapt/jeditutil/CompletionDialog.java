package net.contrapt.jeditutil;

import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import org.gjt.sp.util.Log;

/**
* A general purpose code completion dialog similar to <code>ValueSelectionDialog</code>
* Services from other plugins can provide code completion selectors; the dialog facilitates
* keyboard centric choosing of the desired item
*
* TODO Use the completion trigger key to cycle through different filters if available (plugin provided)
*/
public class CompletionDialog extends JDialog {

   //
   // Properties
   //
   private static final int HGAP=0;
   private static final int VGAP=0;
   private static final int WIDTH=40;
   private CompletionSelector selector;
   private JTextField searchField;
   private JTextField statusField;
   private String lastCompletedValue;
   private JList valueList;
   private JMenuBar menuBar;
   private JPopupMenu popupMenu;
   private Thread loader;

   //
   // Constructors
   //
   protected CompletionDialog(JFrame parent, String title, boolean unDecorated) {
      super(parent, title, true);
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      setResizable(true);
      setUndecorated(unDecorated);
   }

   protected void setSelector(CompletionSelector selector) {
      this.selector = selector;
   }
   
   protected void open() {
      layoutComponents();
   }

   /**
   * Layout the components in this dialog
   */
   private void layoutComponents() {
      // Create the search panel
      JPanel searchPanel = createSearchPanel();
      // Create the status panel
      JPanel statusPanel = createStatusPanel();
      // Add the panels to the dialog
      BorderLayout layout = new BorderLayout();
      layout.setVgap(VGAP);
      layout.setHgap(HGAP);
      getContentPane().setLayout(layout);
      getContentPane().add(searchPanel, BorderLayout.CENTER);
      getContentPane().add(statusPanel, BorderLayout.SOUTH);
      // Create the main menu bar
      menuBar = new JMenuBar();
      setJMenuBar(menuBar);
      pack();
      // Start loading the value list and make the dialog visible
      initializeList();
      searchField.requestFocusInWindow();
      setVisible(true);
   }

   /**
   * Initialize the list
   */
   private void initializeList() {
      reloadList();
   }

   /**
   * Create the search panel with search box and value list
   */
   private JPanel createSearchPanel() {
      JPanel searchPanel = new JPanel(new BorderLayout());
      ((BorderLayout)searchPanel.getLayout()).setVgap(VGAP);
      ((BorderLayout)searchPanel.getLayout()).setHgap(HGAP);
      searchPanel.setBorder(null);
      // The search field
      searchField = new JTextField(selector.getDefault(), WIDTH);
      searchField.setBorder(null);
      searchField.setFocusTraversalKeysEnabled(false);
      searchField.setCaretPosition(selector.getDefault()==null ? 0 : selector.getDefault().length());
      searchField.addKeyListener(new SearchKeyListener());
      searchPanel.add(searchField, BorderLayout.NORTH);
      // The value list
      valueList = new JList(selector);
      valueList.setBorder(null);
      valueList.setFocusTraversalKeysEnabled(false);
      valueList.addKeyListener(new ListKeyListener());
      valueList.addMouseListener(new ListMouseListener());
      valueList.setCellRenderer(selector.getListCellRenderer());
      ListSelectionListener listener = selector.getListSelectionListener();
      if ( listener != null ) valueList.addListSelectionListener(listener);
      JScrollPane valuePane = new JScrollPane(valueList);
      valuePane.setAutoscrolls(true);
      searchPanel.add(valuePane, BorderLayout.CENTER);
      return searchPanel;
   }

   /**
   * Create the status panel with status text boxl
   */
   private JPanel createStatusPanel() {
      JPanel statusPanel = new JPanel(new BorderLayout());
      // The search field
      statusField = new JTextField(selector.getStatusDocument(), "", WIDTH);
      statusField.setEditable(false);
      statusPanel.add(statusField, BorderLayout.CENTER);
      return statusPanel;
   }

   /**
   * Handle a selection; either on the list or in the search field
   */
   private void handleSelection() {
      String value = (String)valueList.getSelectedValue();
      // A value was selected in the list
      if ( value != null ) {
         selector.setFinalSelection(value);
         closeDialog();
      }
      // Enter was hit in search field
      else {
         filterList(true);
         if ( selector.setFinalSelection() ) closeDialog();
      }
   }

   /**
   * Close the dialog; interrupt any loader thread if necessary
   */
   private void closeDialog() {
      if ( loader != null ) loader.interrupt();
      dispose();
   }

   /**
   * Filter the value list using the current value of the text field
   */
   private void filterList(boolean autoComplete) {
      if ( loader != null ) return;
      String filterString = searchField.getText();
      String completedValue = selector.filterList(filterString);
      // For search as you type (not auto complete)
      if ( !autoComplete ) {
         //TODO Consolidate into one call to selector?
         if ( !selector.hasMatches() ) selector.clearList();
         selector.reloaded();
      }
      // For auto completion on TAB
      else if ( selector.hasMatches() ) {
         lastCompletedValue = completedValue;
         searchField.setText(completedValue);
         selector.reloaded();
      }
      else { 
         searchField.setText(lastCompletedValue);
      }
      setFilteredStatus();
   }

   /**
   * Reload the value list
   */
   private void reloadList() {
      if ( loader != null ) return;
      loader = new Thread() {
         public void run() {
            try {
					selector.setStatusText("...Loading...", false);
               selector.reloadList();
            }
            catch (Exception e) {
               Log.log(Log.DEBUG, this, "Error loading list: "+e);
            }
            finally {
               loader = null;
               filterList(false);
            }
         }
      };
      loader.start();
   }
   
   /**
   * Set the status bar to indicate filtered size/total size of value
   * list
   */
   private void setFilteredStatus() {
		selector.setStatusText(selector.getTitle(), true);
   }

   //
   // Various Helper Classes -- listeners, models etc
   //

   /**
   * A keyboard listener for the value list.  By default
   * VK_ENTER selects an item.  We also allow the value selector
   * to decide whether a particular key selects the value
   */
   class ListKeyListener extends KeyAdapter {
      /**
      * Select a list item by pressing enter
      */
      public void keyPressed(KeyEvent argEvent) {
         int iKeyCode = translateKeyCode(argEvent);
         switch ( iKeyCode ) {
            case KeyEvent.VK_ENTER:
               handleSelection();
               break;
            case KeyEvent.VK_ESCAPE:
               // Close dialog
               closeDialog();
               break;
            case KeyEvent.VK_UP:
               // If selected index is 0 clear any selection (leaving the component)
               if ( valueList.getSelectedIndex() == 0 ) {
                  valueList.clearSelection();
                  focusOnSearch();
               }
               break;
         }
      }
   }

   /**
   * A mouse listener for the value list.  By default, double-click
   * selects the value.  In addition, if the selector has defined
   * a popup menu, that can be triggered by a right click
   */
   class ListMouseListener extends MouseAdapter {
      /**
      * Select a list item by double clicking
      */
      public void mouseClicked(MouseEvent argEvent) {
         if ( argEvent.getClickCount() == 2 ) {
            handleSelection();
         }
         // Trigger optional popup menu on trigger
         else if ( argEvent.getButton() == argEvent.BUTTON3 ) {
            if ( popupMenu == null ) return;
            popupMenu.show(valueList, argEvent.getX(), argEvent.getY());
         }
      }
   }


   /**
   * A keyboard listener for the search text field.  By default
   *  - VK_ENTER selects/filters the current value; 
   *  - ESC cancels the dialog; 
   *  - VK_TAB filters the list
   * For other keys, ask the selector if they should trigger selection.
   * This allows the selector to record the key stroke and take different actions
   * on the selected value
   */
   class SearchKeyListener extends KeyAdapter {

      String previousText="";

      /**
      * Filter on tab; filter or select on enter
      */
      public void keyPressed(KeyEvent argEvent) {
         previousText = ( searchField.getText()==null ) ? "" : searchField.getText();
         // Translate key code if necessary
         int iKeyCode = translateKeyCode(argEvent);
         switch (iKeyCode) {
            case KeyEvent.VK_ENTER:
               // select
               handleSelection();
               argEvent.consume();
               break;
            case KeyEvent.VK_ESCAPE:
               // Close dialog
               closeDialog();
               break;
            case KeyEvent.VK_TAB:
               // filter
               filterList(true);
               argEvent.consume();
               break;
            case KeyEvent.VK_DOWN:
               // Set focus to list of values
               focusOnList();
               argEvent.consume();
               break;
         }
      }
      
      /**
      * When key is released, if the text was changed refilter the list
      */
      public void keyReleased(KeyEvent argEvent) {
         if ( previousText.equals(searchField.getText()) ) return;
         filterList(false);
      }

   }

   /**
   * Focus on the list of values if there are any choices available 
   */
   private void focusOnList() {
      if ( selector.getSize() == 0 ) return; 
      valueList.requestFocusInWindow();
      valueList.setSelectedIndex(0);
   }

   /**
   * Focus on the search field
   */
   private void focusOnSearch() {
      searchField.requestFocusInWindow();
   }

   /**
   * Translate the given key combination to one that is handled by this dialog; this
   * allows alternate mappings for things like up and down arrow
   */
   private int translateKeyCode(KeyEvent event) {
      if ( event.getKeyCode() == KeyEvent.VK_N && event.isControlDown() ) {
         event.setKeyChar(KeyEvent.CHAR_UNDEFINED);
         event.setKeyCode(KeyEvent.VK_DOWN);
         event.setModifiers(0);
         return KeyEvent.VK_DOWN;
      }
      if ( event.getKeyCode() == KeyEvent.VK_P && event.isControlDown() ) {
         event.setKeyChar(KeyEvent.CHAR_UNDEFINED);
         event.setKeyCode(KeyEvent.VK_UP);
         event.setModifiers(0);
         return KeyEvent.VK_UP;
      }
      return event.getKeyCode();
   }

   //
   // Static
   //
   
   /**
   * Open the completion dialog for the given selector
   */
   public static void open(JFrame parent,  Point location, CompletionSelector selector) {
      CompletionDialog dialog = new CompletionDialog(parent, selector.getTitle(), true);
      dialog.setSelector(selector);
      dialog.setLocation(location);
      dialog.open();
   }

}
