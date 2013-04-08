package net.contrapt.jeditutil.selector;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
* A frame allowing parent specific selections for various items -- files, classes, tools etc
* TODO Save the dialog instance for repeated use; just reset the title and selector
* TODO Save preferredSize and preferredLocation between sessions
*/
public class ValueSelectionDialog<P,V> extends JDialog {
   
   //
   // Properties
   //
   private static final int HGAP=10;
   private static final int VGAP=10;
   private ValueSelector<P,V> selector;
   private JComboBox parentChoice;
   private JTextField searchField;
   private JTextField statusField;
   private String lastCompletedValue;
	private JSplitPane splitPane;
   private JList valueList;
   private JToolBar toolBar;
   private JPopupMenu popupMenu;
   private Thread loader;
   private Dimension preferredSize;
   private Point preferredLocation;
	private Integer preferredSplit;
   /** Actions that shouldn't close the dialog */
   private Set<String> noCloseActions = new HashSet<String>();


   //
   // Constructors
   //
   protected ValueSelectionDialog(JFrame parent, String title) {
      this(parent, title, false);
   }
   
   protected ValueSelectionDialog(JFrame parent, String title, boolean unDecorated) {
      super(parent, title, true);
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      setResizable(true);
      setUndecorated(unDecorated);
   }

   protected void setSelector(ValueSelector<P,V> selector) {
      this.selector = selector;
   }
   
   protected void open() {
      layoutComponents();
   }

   /**
   * Layout the components in this dialog
   */
   private void layoutComponents() {
      // Get preferred size and loc from selector
      setPreferredPlacement();
      // Create the parent panel if necessary
      JPanel parentPanel = createParentPanel();
      // Create the search panel
      JPanel searchPanel = createSearchPanel();
      // Create the status panel
      JPanel statusPanel = createStatusPanel();
		// Either add split pane to accomodate extra info, or just the main panel
		JComponent extraInfo = selector.getExtraComponent();
		if ( extraInfo != null ) splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, searchPanel, extraInfo);
      // Add the panels to a main panel
      BorderLayout layout = new BorderLayout();
      layout.setVgap(VGAP);
      layout.setHgap(HGAP);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(layout);
      if ( parentPanel != null ) mainPanel.add(parentPanel, BorderLayout.NORTH);
      if ( splitPane != null) mainPanel.add(splitPane, BorderLayout.CENTER);
      else mainPanel.add(searchPanel, BorderLayout.CENTER);
      mainPanel.add(statusPanel, BorderLayout.SOUTH);
      // Add to content pane
		getContentPane().add(mainPanel);
      setPreferredSize(preferredSize);
      setLocation(preferredLocation);
      // Setupt actions
      setupActions();
      pack();
      // Start loading the value list and make the dialog visible
      initializeList();
      searchField.requestFocusInWindow();
      setModal(selector.isModal());
      setVisible(true);
      if ( splitPane != null ) splitPane.setDividerLocation(preferredSplit);
   }

   /**
   * Set the preferred size and location; set to values stored in the
   * selector.  If no values are stored yet, initialize to a useful default
   * based on the size and location of the parent window
   */
   private void setPreferredPlacement() {
      double x=500.00, y=500.00;
      int dx=0, dy=0;
      preferredSize = selector.getPreferredSize();
      if ( preferredSize==null ) {
         preferredSize = getParent().getSize();
         x = preferredSize.getWidth()<x ? x : preferredSize.getWidth();
         y = preferredSize.getHeight()<y ? y : preferredSize.getHeight();
         preferredSize.setSize(x/2, y/2);
         dx = (int)preferredSize.getWidth()/3;
         dy = (int)preferredSize.getHeight()/3;
      }
      preferredLocation = selector.getPreferredLocation();
      if ( preferredLocation==null ) {
         preferredLocation = getParent().getLocation();
         preferredLocation.translate(dx, dy);
      }
		preferredSplit = selector.getPreferredSplit();
		if ( preferredSplit == null ) preferredSplit = preferredSize.width/2;
   }

   /**
   * If the selector provides any actions, setup a menu bar and popup menu
   */
   private void setupActions() {
      Collection<Action> actions = selector.getActions();
      if ( actions==null ) return;
      ActionListener listener = new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if ( noCloseActions.contains(e.getActionCommand()) ) focusOnList(false);
            else handleSelection();
         }
      };
      popupMenu = new JPopupMenu("Actions");
      for ( Action a : actions ) {
         (popupMenu.add(a)).addActionListener(listener);
         (toolBar.add(a)).addActionListener(listener);
         if ( !selector.isCloseDialog(a) ) noCloseActions.add((String)a.getValue(Action.NAME));
      }
   }

   /**
   * Initialize the list depending on state of parent choice box
   */
   private void initializeList() {
      String parentKey = selector.getParentKey();
      // No parent choice
      if ( parentChoice == null ) {
         reloadList();
         return;
      }
      // Yes parent choice
      if ( !parentChoice.getSelectedItem().equals(parentKey) ) parentChoice.setSelectedItem(parentKey);
      parentSelected(parentKey);
   }

   /**
   * Create the parent choice panel if necessary
   */
   private JPanel createParentPanel() {
   	// Do the tool bar first
      toolBar = new JToolBar();
      JPanel parentPanel = new JPanel();
      parentPanel.setLayout(new BorderLayout());
      parentPanel.add(toolBar, BorderLayout.NORTH);
      // See if we need to add parents
      Map<String,P> parents = selector.getParents();
      String parentKey = selector.getParentKey();
      if ( parents == null && parentKey == null ) return parentPanel;
      JPanel choicePanel = new JPanel();
      if ( parents == null ) {
         parents = new HashMap<String, P>();
         parents.put(parentKey, selector.getParent());
      }
      choicePanel.add(new JLabel(selector.getParentLabel()));
      String[] choices = parents.keySet().toArray(new String[0]);
      parentChoice = new JComboBox(choices);
      parentChoice.addKeyListener(new ParentKeyListener());
      choicePanel.add(parentChoice);
      parentPanel.add(choicePanel, BorderLayout.SOUTH);
      return parentPanel;
   }

   /**
   * Create the search panel with search box and value list
   */
   private JPanel createSearchPanel() {
      JPanel searchPanel = new JPanel(new BorderLayout());
      ((BorderLayout)searchPanel.getLayout()).setVgap(VGAP);
      ((BorderLayout)searchPanel.getLayout()).setHgap(HGAP);
      // The search field
      searchField = new JTextField(selector.getDefault(), 60);
      searchField.setFocusTraversalKeysEnabled(false);
      searchField.selectAll();
      searchField.addKeyListener(new SearchKeyListener());
      searchPanel.add(searchField, BorderLayout.NORTH);
      // The value list
      valueList = new JList(selector);
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
      statusField = new JTextField(selector.getStatusDocument(), "", 60);
      statusField.setEditable(false);
      statusField.setBackground(Color.WHITE);
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
      preferredSize = getSize(preferredSize);
      preferredLocation = getLocation(preferredLocation);
		preferredSplit = (splitPane==null) ? null : splitPane.getDividerLocation();
      selector.setPreferredSize(preferredSize);
      selector.setPreferredLocation(preferredLocation);
		selector.setPreferredSplit(preferredSplit);
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
            }
            finally {
               loader = null;
					selector.setStatusText("Loaded "+selector.getSize(), false);
               filterList(false);
            }
         }
      };
      loader.start();
   }
   
   /**
   * When a new parent is selected, refresh the selector 
   */
   private void parentSelected(String name) {
      Map<String,P> parents = selector.getParents();
      P parent = (parents==null) ? selector.getParent() : parents.get(name);
      selector.setParent(parent);
      reloadList();
   }

   /**
   * Set the status bar to indicate filtered size/total size of value
   * list
   */
   private void setFilteredStatus() {
      selector.setStatusText("", true);
   }

   //
   // Various Helper Classes -- listeners, models etc
   //

   /**
   * An action listener detects when a new parent is selected in the
   * parent combo box
   */
   class ParentKeyListener extends KeyAdapter {
      public void keyPressed(KeyEvent e) {
         int keyCode = e.getKeyCode();
         switch ( keyCode ) {
            case KeyEvent.VK_ENTER:
               parentSelected((String)parentChoice.getSelectedItem());
               focusOnSearch();
               break;
            case KeyEvent.VK_ESCAPE:
               closeDialog();
               break;
         }
      }
   }

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
            case KeyEvent.VK_UP:
               // Set focus to parent choice if it exists
               focusOnParent();
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
   * Focus on the parent choice list if it exists and are more than 1 choice
   */
   private void focusOnParent() {
      if ( selector.getParents()!=null && selector.getParents().size() > 1 && parentChoice != null )
         parentChoice.requestFocusInWindow();
   }

   /**
   * Focus on the list of values if there are any choices available
   *
   * @param setSelection If true, set the list selection to the first item
   */
   private void focusOnList(boolean setSelection) {
      if ( selector.getSize() == 0 ) return; 
      valueList.requestFocusInWindow();
      if ( setSelection ) valueList.setSelectedIndex(0);
   }

   /**
   * Focus on the list of values and select the first item in the list
   */
   private void focusOnList() {
   	focusOnList(true);
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
   * Open the value selection dialog for the given selector
   */
   public static <P,V> void open(JFrame parent,  ValueSelector<P,V> selector) {
      ValueSelectionDialog<P,V> dialog = new ValueSelectionDialog<P,V>(parent, selector.getTitle());
      dialog.setSelector(selector);
      dialog.open();
   }
   
   /**
   * Open the value selection dialog for the given selector specifying whether the
   * dialog should be decorated or not
   */
   public static <P,V> void open(JFrame parent,  ValueSelector<P,V> selector, boolean undecorated) {
      ValueSelectionDialog<P,V> dialog = new ValueSelectionDialog<P,V>(parent, selector.getTitle(), undecorated);
      dialog.setSelector(selector);
      dialog.open();
   }

}
