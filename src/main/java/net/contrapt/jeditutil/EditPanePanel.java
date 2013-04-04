package net.contrapt.jeditutil;

import java.util.HashMap;
import java.util.Map;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.swing.JPanel;
import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;

import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.textarea.TextArea;
import org.gjt.sp.jedit.PluginJAR;

/**
* Manages the display of <code>BufferStatusPanel</code>, <code>TextArea</code> and non-text panels
* supplied by plugins.  Each <code>EditPane</code> gets a <code>EditPanePanel</code>.  By default
* it shows the <code>BufferStatusPanel</code> on the top followed by the jedit <code>TextArea</code>.
* Any plugin can add a panel that can either be displayed in a split pane along with the <code>TextArea</code>
* or as a separate buffer without displaying the <code>TextArea</code>
*/
public class EditPanePanel extends JPanel {
   
   //
   // PROPERTIES
   //
   /** The name of the textarea card */
   private static final String TEXTAREA_CARD = "JEDITUTIL.TEXTAREA";

   /** The name of the null card (for split pane) */
   private static final String NULL_CARD = "JEDITUTIL.NULL";

   /** A counter to create unique buffer names */
   private static int panelCount;

   /** The buffer status component */
   BufferStatusPanel statusPanel;

   /** The textarea/plugin panel */
   JPanel mainPanel;

   /** The card layout for the main panel */
   CardLayout mainCards;

   /** The split pane for the textarea */
   JSplitPane textAreaPane;

   /** The panel for plugin half of the split pane */
   JPanel lowerPanel;

   /** The card layout for the plugin half of the split */
   CardLayout lowerCards;

   /** Buffer specific panels, if any, shown on the mainPanel */
   private Map<Buffer, PluginPanel> bufferPanels = new HashMap<Buffer, PluginPanel>();

   /** Plugin panels stored by name and class */
   private Map<Buffer, Map<Class,PluginPanel>> lowerPanels = new HashMap<Buffer, Map<Class,PluginPanel>>();

   /** The edit pane associated with this panel manager */
   private EditPane editPane;

   /** The standard jEdit textarea */
   private TextArea textArea;

   /** The last buffer handled */
   private Buffer lastBuffer;

   /** The original size of the split pane divider */
   private int dividerSize;

   /** A map of divider locations by buffer for split panes */
   private Map<Buffer, Integer>  dividerLocations = new HashMap<Buffer, Integer>();

   //
   // CONSTRUCTORS
   //
   public EditPanePanel(EditPane editPane) {
      this.editPane = editPane;
      this.textArea = editPane.getTextArea();
      initialize(editPane);
   }

   //
   // OVERRIDES
   //

   //
   // PUBLIC METHODS
   //

   /**
   * Update the tabs and status panel for the current edit pane
   */
   public void update(boolean bufferChanged) {
      update(editPane.getBuffer(), bufferChanged);
   }

   /**
   * Update the tabs and the status panel
   */
   public void update(Buffer buffer, boolean bufferChanged) {
      statusPanel.updateBufferStatus(editPane, buffer, bufferChanged);
      statusPanel.resetPanelChoices(lowerPanels.get(buffer));
      if ( buffer == lastBuffer ) return;
      saveDividerLocation(lastBuffer);
      lastBuffer = buffer;
      if ( bufferPanels.containsKey(buffer) ) {
         mainCards.show(mainPanel, buffer.getPath());
         bufferPanels.get(buffer).requestFocusInWindow();
      }
      else {
         int location = showPluginPanel(buffer);
         mainCards.show(mainPanel, TEXTAREA_CARD);
         if ( location > 0 ) textAreaPane.setDividerLocation(location);
         else {
            textAreaPane.setDividerLocation(1.0d);
            textArea.requestFocusInWindow();
         }
      }
   }

   /**
   * Request focus on the lower panel of split pane if it is showing
   */
   public void focusOnPluginPanel() {
      lowerPanel.requestFocusInWindow();
   }

   /**
   * Close all resources associated with this edit pane panel
   */
   public void removeAll() {
      for ( PluginPanel panel : bufferPanels.values() ) {
         mainPanel.remove(panel);
         panel.pluginPanelRemoved();
      }
      bufferPanels.clear();
      for ( Map<Class, PluginPanel> panelMap : lowerPanels.values() ) {
         for ( PluginPanel panel : panelMap.values() ) {
            lowerPanel.remove(panel);
            panel.pluginPanelRemoved();
         }
      }
      lowerPanels.clear();
   }

   /**
   * Add a panel either in a split pane with the given buffer,
   * or as a separate buffer
   */
   public void addPluginPanel(Buffer buffer, PluginPanel panel) {
      if ( panel.showWithBuffer() ) addPanelToBuffer(buffer, panel);
      else addPanelAsBuffer(panel);
   }

   /**
   * Return the  panel for the given buffer and class, if any
   */
   public PluginPanel getPluginPanel(Buffer buffer, Class clazz) {
      Map<Class,PluginPanel> panelMap = lowerPanels.get(buffer);
      if ( panelMap==null ) return null;
      return panelMap.get(clazz);
   }

   /**
   * Remove the buffer panel for the given buffer and class
   */
   public void removePluginPanel(Buffer buffer, Class cl) {
      PluginPanel panel=null;
      // Try the splits
      Map<Class,PluginPanel> panelMap = lowerPanels.get(buffer);
      if ( panelMap != null ) panel=panelMap.remove(cl);
      if ( panel != null ) {
         lowerPanel.remove(panel);
         panel.pluginPanelRemoved();
      }
      // Try the components
      panel = bufferPanels.remove(buffer);
      if ( panel != null ) {
         mainPanel.remove(panel);
         panel.pluginPanelRemoved();
      }
      lastBuffer = null;
      update(buffer, false);
   }

   /**
   * Remove all the plugin panels for the given class
   */
   public void removePluginPanels(Class cl) {
      for ( Map<Class, PluginPanel> panelMap : lowerPanels.values() ) {
         PluginPanel panel = panelMap.remove(cl);
         if ( panel != null ) {
            lowerPanel.remove(panel);
            panel.pluginPanelRemoved();
         }
      }
      for ( Buffer b : bufferPanels.keySet() ) {
         if ( bufferPanels.get(b).getClass() == cl ) {
            PluginPanel panel = bufferPanels.remove(b);
            if ( panel != null ) {
               mainPanel.remove(panel);
               panel.pluginPanelRemoved();
            }
         }
      }
      lastBuffer = null;
      update(false);
   }

   /**
   * Remove all the plugin panels for the given buffer; call this when the
   * buffer is closed
   */
   public void removePluginPanels(Buffer buffer) {
      // Try the splits
      Map<Class,PluginPanel> panelMap = lowerPanels.remove(buffer);
      if ( panelMap!=null ) {
         for ( PluginPanel panel : panelMap.values() ) {
            lowerPanel.remove(panel);
            panel.pluginPanelRemoved();
         }
         panelMap.clear();
      }
      // Try the components
      PluginPanel panel = bufferPanels.remove(buffer);
      if ( panel != null ) {
         mainPanel.remove(panel);
         panel.pluginPanelRemoved();
      }
      lastBuffer = null;
      update(false);
   }

   /**
   * Add plugin specific components in response to plugin loading
   */
   public void addPluginComponents(EditPane pane) {
      statusPanel.addPluginComponents(pane);
   }

   /**
   * Remove plugin specific components in response to plugin unloading
   */
   public void removePluginComponent(PluginJAR jar) {
      statusPanel.removePluginComponent(jar);
   }

   //
   // PRIVATE METHODS
   //

   /**
   * Initialize variables and layout
   */
   private void initialize(EditPane editPane) {
      textArea.setMinimumSize(new Dimension(0,0));
      // Add listeners
      // Add components
      setLayout(new BorderLayout());
      statusPanel = new BufferStatusPanel(editPane);
      mainCards = new CardLayout();
      mainPanel = new JPanel(mainCards);
      lowerCards = new CardLayout();
      lowerPanel = new JPanel(lowerCards);
      FocusPropertyChangeListener focusListener = new FocusPropertyChangeListener(lowerPanel);
      KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", focusListener);
      JPanel nullPanel = new JPanel();
      nullPanel.setMaximumSize(new Dimension(0,0));
      lowerPanel.add(nullPanel, NULL_CARD);
      KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", new FocusPropertyChangeListener(textArea));
      textAreaPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textArea, lowerPanel);
      textAreaPane.setResizeWeight(1.0d);
      dividerSize = textAreaPane.getDividerSize();
      mainPanel.add(textAreaPane, TEXTAREA_CARD);
      add(BorderLayout.NORTH, statusPanel);
      add(BorderLayout.CENTER, mainPanel);
      editPane.add(this);
   }

   /**
   * Add the given panel to a split pane with the given buffer's textarea
   */
   private void addPanelToBuffer(Buffer buffer, PluginPanel panel) {
      Map<Class, PluginPanel> panelMap = lowerPanels.get(buffer);
      if ( panelMap == null ) panelMap = new HashMap<Class, PluginPanel>();
      panelMap.put(panel.getClass(), panel);
      lowerPanels.put(buffer, panelMap);
      lowerPanel.add(panel, buffer.getPath()+"/"+panel.getClass());
      panel.setTextArea(textArea);
      panel.setBuffer(buffer);
      lastBuffer = null;
      Dimension initial = panel.getInitialSize();
      if ( initial != null ) {
         float pct = ((float)initial.height)/((float)textAreaPane.getSize().height);
         int location = textAreaPane.getMaximumDividerLocation()-(int)(textAreaPane.getMaximumDividerLocation()*pct);
         location -= 10;
         if ( location > 0 ) dividerLocations.put(buffer, location);
      }
      update(buffer, false);
   }

   /**
   * Add the given panel as a separate buffer -- this is a non-text buffer
   */
   private void addPanelAsBuffer(PluginPanel panel) {
      View view = jEdit.getActiveView();
      Buffer buffer = jEdit.openTemporary(view, null, panel.getPanelName()+"-"+(++panelCount), true);
      buffer.setDirty(false);
      buffer.setReadOnly(true);
      jEdit.commitTemporary(buffer);
      panel.setBuffer(buffer);
      bufferPanels.put(buffer, panel);
      mainPanel.add(panel, buffer.getPath());
      update(buffer, false);
      editPane.setBuffer(buffer, true);
   }

   /**
   * Show the correct plugin for the given buffer in the text area split pane
   *
   * @return The location of the split pane divider
   */
   private int showPluginPanel(Buffer buffer) {
      lowerCards.show(lowerPanel, NULL_CARD);
      Map<Class,PluginPanel> panelMap = lowerPanels.get(buffer);
      if ( panelMap != null ) {
         for ( Class c : panelMap.keySet() ) {
            lowerCards.show(lowerPanel, buffer.getPath()+"/"+c);
            textAreaPane.setDividerSize(dividerSize);
            textAreaPane.setOneTouchExpandable(true);
            lowerPanel.setVisible(true);
            return getDividerLocation(buffer);
         }
      }
      textAreaPane.setDividerSize(0);
      textAreaPane.setOneTouchExpandable(false);
      lowerPanel.setVisible(false);
      return -1;
   }

   /**
   * Store the divider location for the given buffer if it is in the split pane;  this is
   * so we can restore the last divider location for each split
   */
   private void saveDividerLocation(Buffer buffer) {
      Map<Class,PluginPanel> panelMap = lowerPanels.get(buffer);
      // If this buffer is a split pane, save the location
      if ( panelMap != null && panelMap.size() > 0 ) {
         dividerLocations.put(buffer, textAreaPane.getDividerLocation());
      }
   }

   /**
   * Retrieve a divider location for the given buffer; if not found, set a default of 50%
   */
   private int getDividerLocation(Buffer buffer) {
      Integer location = dividerLocations.get(buffer);
      if ( location == null ) {
         location = textAreaPane.getMaximumDividerLocation() / 2;
         dividerLocations.put(buffer, location);
      }
      return location;
   }

}
