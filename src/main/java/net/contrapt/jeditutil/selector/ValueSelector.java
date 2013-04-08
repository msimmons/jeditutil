package net.contrapt.jeditutil.selector;

import java.util.*;
import java.io.File;
import javax.swing.Action;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.ListCellRenderer;
import javax.swing.DefaultListCellRenderer;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JList;
import javax.swing.JComponent;
import javax.swing.AbstractListModel;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.BadLocationException;

/**
* The data model for the value selection dialog; implement this class to provide
* a model for a list of values to choose from.  The dialog provides a keyboard centric
* method of searching through the list and returning the value that was chosen; it also 
* supports concept of a parent so list of values can be subdivided by the chosen parent
*/
public abstract class ValueSelector<P,V> extends AbstractListModel {
   
   //
   // Properties
   //
   private static final String CLOSE_DIALOG_PROPERTY = "CLOSE_DIALOG_ACTION";

   /** For storing size and location info per selector class */
   private static Map<Class,Dimension> preferredSizes = new HashMap<Class,Dimension>();
   private static Map<Class,Point> preferredLocations = new HashMap<Class,Point>();
   private static Map<Class,Integer> preferredSplits = new HashMap<Class,Integer>();

   /** The string that was selected */
   protected String finalSelection;

   /** Map of values to show in the list */
   protected Map<String, V> valueMap;

   /** The currently selected index in the list */
   private int currentIndex = -1;

   /** The filtered list of values */
   private List<String> filteredList;

   /** Whether or not the last filter had any matches */
   private boolean hasMatches = false;

	/** Document to model the status bar text */
	private Document statusDocument;

	/** Recent choices -- need to be stored by parent value? */
	//private static List<String> recentChoices = new ArrayList<String>();

   //
   // Constructors
   //

   protected ValueSelector() {
      filteredList = new ArrayList<String>();
		statusDocument = new PlainDocument();
   }

   /**
   * Sets whether or not the given <code>Action</code> should cause the dialog to close
   * or not.  By default, all actions close the dialog
   *
   * @param action The action in question
   * @param close Whether or not it causes the dialog to close
   */
   protected final void setCloseDialog(Action action, boolean close) {
      action.putValue(CLOSE_DIALOG_PROPERTY, close);
   }

   /**
   * Returns whether the given action should cause the dialog to close
   */
   protected final boolean isCloseDialog(Action action) {
      if ( action.getValue(CLOSE_DIALOG_PROPERTY) == null ) return true;
      return (Boolean)action.getValue(CLOSE_DIALOG_PROPERTY);
   }

   /**
   * Return the key of the currently selected list item if any
   */
   protected String getCurrentKey() {
      return ( currentIndex >=0 && currentIndex < filteredList.size() ) ? filteredList.get(currentIndex) : null;
   }

   /**
   * Return the currently selected value if any
   */
   protected V getCurrentValue() {
      return ( getCurrentKey()==null ) ? null : getValue(getCurrentKey());
   }

   /**
   * Remove the currently selected item from the list -- that is, remove it
   * from the data model, both full list and filtered list
   */
   protected V removeCurrentValue() {
      V value = null;
      if ( currentIndex >= 0 && currentIndex < filteredList.size() ) {
         String key = filteredList.remove(currentIndex);
         value = getValueMap().remove(key);
      }
      reloaded();
      return value;
   }
   
   /**
   * Define the title of the dialog
   *
   * @return The title of the selection dialog
   */
   public abstract String getTitle();

   /**
   * Load the values to be selected in the list
   *
   * @return A map of values in the list.  The key is the string you would search for
   */
   public abstract Map<String, V> loadValueMap();

   /**
   * The default search value
   *
   * @return A value that would appear in the search box when the dialog is opened
   */
   public abstract String getDefault();

   /**
   * The values for the parent selection list.  Override this
   * to add the parent dimension to your selection dialog
   *
   * @return A map of parent objects.  The key is the string you choose to choose the parent
   */
   public Map<String,P> getParents() {
      return null;
   }

   /**
   * A label for the parent selection list.  Override this if you
   * are using parents
   *
   * @return What is the parent called?
   */
   public String getParentLabel() {
      return null;
   }

   /**
   * Key value for the current parent object.  Override this if
   * you are using parents
   */
   public String getParentKey() {
      return null;
   }

   /**
   * Set the current parent.  Override this if you
   * are using parents
   */
   public void setParent(P parent) {
   }

   /**
   * Get the current selected parent.  Override this if you are using
   * parents
   */
   public P getParent() {
      return null;
   }

   /**
   * Should list filtering be case sensitive?
   */
   public abstract boolean isCaseSensitive();

	/**
	* Should the entire list view be populated when nothing is typed in search box?  For large
	* lists you might want to override to return false.  Defaults to true
	*/
	public boolean showFullList() {
		return true;
	}

   /**
   * Override this method to return a collection of actions;
   * these actions will be added to the dialog menu bar, a right-click
   * popup menu and, and the key handler if ACCELERATOR_KEY is set
   *
   * @return A collection of actions that can be taken on list selections
   */
   public Collection<Action> getActions() {
      return null;
   }

   /**
   * Override this method if you have special list cell rendering logic to 
   * display each list item as you like
   *
   * @return A custom <code>ListCellRenderer</code>
   */
   public ListCellRenderer getListCellRenderer() {
      return new DefaultListCellRenderer();
   }

	/**
	* Override this method if your selector provides a component that displays 
	* extra information about selected items; or whatever you like.  If provided,
	* it will be displayed in a split pane to the right of the normal selection
	* dialog.  By default it returns null
	*
	* @return A <code>JComponent</code> containing your extra info
	*/
	public JComponent getExtraComponent() {
		return null;
	}

	/**
	* Override this method to return false if your dialog is not modal; by default
	* the dialogs will be modal
	*/
	public boolean isModal() { return true; }

   // Final methods; used by the value selection dialog and dialog clients

   /**
   * Return the object - the value from the value map - that was selected by the user
   */
   public final V getSelectedObject() { return (finalSelection==null) ? null : valueMap.get(finalSelection); }

   /**
   * Return the string - the key of the value map - that was selected
   */
   final public String getSelectedString() { return finalSelection; }

   /**
   * Return the value corresponding to the given key
   */
   final public V getValue(String key) {
      return getValueMap().get(key);
   }

   /**
   * Force a reload of the value map
   */
   final void reloadValueMap() { 
      valueMap = loadValueMap(); 
   }

   /**
   * Clear the filtered list and reload with the unfiltered list
   */
   final void clearList() {
      filteredList.clear();
   }

   /**
   * Reload the entire list
   */
   final void reloadList() {
      filteredList.clear();
      reloaded();
      reloadValueMap();
      filteredList.addAll(getValueKeys());
      reloaded();
   }

   /**
   * Filter the value list using the given filter string.  Return the completed
   * string value
   *
   * @param filterString The string the user entered to filter on
   * @return The common string for values that matched the filter
   */
   final String filterList(String filterString) {
      String completedString = filterString;
      hasMatches = false;
      // Wildcard reloads entire list (if nothing is entered, subclasses can choose to show entire list or not
      if ( filterString == null || filterString.equals("") || filterString.equals("*") ) {
         filteredList.clear();
			if ( showFullList() ) {
				filteredList.addAll(getValueMap().keySet());
				hasMatches = true;
			}
			//else {
			//	filteredList.addAll(recentChoices);
			//	hasMatches = ( recentChoices.size() > 0 );
			//}
         return completedString;
      }
      // Filter the list on anything else
      if ( !isCaseSensitive() ) filterString = filterString.toUpperCase();
      completedString=null;
      String regexString = convertToRegex(filterString);
      Pattern regex = Pattern.compile(regexString);
      for ( String value : getValueKeys() ) {
         String cValue = value;
         if ( !isCaseSensitive() ) cValue = value.toUpperCase();
         boolean match = false;
         if ( regex.matcher(cValue).matches() ) match=true;
         else if ( cValue.startsWith(filterString) ) match=true;
         if ( match ) {
            if ( !hasMatches ) filteredList.clear();
            completedString = ( completedString==null ) ? value : getCommonString(completedString, value);
            filteredList.add(value);
            hasMatches = true;
         }
      }
      return completedString;
   }

   /**
   * Whether or not the last filter had any matches
   */
   final boolean hasMatches() {
      return hasMatches;
   }

   /**
   * Return the map of selectable values
   */
   final Map<String,V> getValueMap() {
      if ( valueMap == null ) valueMap = loadValueMap();
      return valueMap;
   }

   /**
   * Return the set of keys for the value map
   */
   final Set<String> getValueKeys() {
      return getValueMap().keySet();
   }

   /**
   * Set the final selection if it was picked from a list
   *
   * @param selection The value that was selected in the list
   */
   final void setFinalSelection(String selection) { 
      this.finalSelection = selection;
		//if ( !recentChoices.contains(selection) ) recentChoices.add(0,selection);
		//if ( recentChoices.size() > 10 ) recentChoices.remove(recentChoices.size()-1);
   }

   /**
   * Set the final select if it is the only one left in the list
   *
   * @return false if there were more than one choice still in the list, true otherwise
   */
   final boolean setFinalSelection() {
      if ( filteredList.size() != 1 ) return false;
      setFinalSelection(filteredList.get(0));
      return true;
   }

	/**
	* Set the status text to the given text; if asked, also show the filter statistics
	* first #filtered/total
	*
	* @param text The text to display
	* @param showFilterStatus Whether to show the filter info or not
	*/
	final public void setStatusText(String text, boolean showFilterStats) {
		String filter = ( showFilterStats ) ? "["+getSize()+"/"+getValueMap().size()+"] " : "";
		try {
			statusDocument.remove(0, statusDocument.getLength());
			statusDocument.insertString(0, filter+text, null);
		}
		catch (BadLocationException e) {
			throw new IllegalStateException("Unexpected error updating document", e);
		}
	}

   final Dimension getPreferredSize() {
      return preferredSizes.get(this.getClass());
   }
   
   final void setPreferredSize(Dimension d) {
      preferredSizes.put(this.getClass(), d);
   }

   final Point getPreferredLocation() {
      return preferredLocations.get(this.getClass());
   }

   final void setPreferredLocation(Point p) {
      preferredLocations.put(this.getClass(),p);
   }

   final Integer getPreferredSplit() {
      return preferredSplits.get(this.getClass());
   }

   final void setPreferredSplit(Integer i) {
      preferredSplits.put(this.getClass(),i);
   }

   /**
   * Implement the list model
   */
   public Object getElementAt(int index) {
      return (index>=filteredList.size()) ? null : filteredList.get(index); 
   }

   /**
   * Implement list model
   */
   public int getSize() { 
      return filteredList.size(); 
   }

   /**
   * Implement list model
   */
   public void reloaded() {
      fireContentsChanged(this, 0, filteredList.size());
   }

   /**
   * Convert the given string pattern to a regex for use in list filtering
   */
   private String convertToRegex(String pattern) {
      StringBuilder buf = new StringBuilder(pattern.length());
      boolean wasDot = false;
		boolean isRE = false;
      for ( char c : pattern.toCharArray() ) {
         switch (c) {
            case '*':
               if ( wasDot ) wasDot=false;
               else buf.append('.');
               break;
            case '(':
            case ')':
            case '\\':
					if ( wasDot ) wasDot=false;
               buf.append('\\');
               break;
            case '.': wasDot=true;
               break;
				default:
					wasDot=false;
         }
         buf.append(c);
      }
		buf.append(".*");
      return buf.toString();
   }

   /**
   * Return a string containing only the common characters of the two given strings
   */
   private String getCommonString(String value1, String value2) {
      int length = ( value1.length() < value2.length() ) ? value1.length() : value2.length();
      int ndx = 0;
      for ( ; ndx<length; ndx++ ) if ( value1.charAt(ndx) != value2.charAt(ndx) ) break;
      return value1.substring(0, ndx);
   }

	/**
	* Do whatever you need to do when the currently selected list item changes; 
	* some possibilities are update status, show additional info etc.  Current index
	* will be in <code>currentIndex</code>.  Default does nothing
	*
	*/
	public void handleListSelectionChange() {
	}

   /**
   * Keeps track of the currently selected value in the list
   */
   final ListSelectionListener getListSelectionListener() {
      return new ListSelectionListener() {
         public void valueChanged(ListSelectionEvent e) {
            JList list = (JList)e.getSource();
            currentIndex = list.getSelectedIndex();
				handleListSelectionChange();
         }
      };
   }

	/**
	* Return the status <code>Document</code> model for use in the status bar
	* UI component
	*/
	final Document getStatusDocument() {
		return statusDocument;
	}

}

