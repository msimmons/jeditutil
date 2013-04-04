package net.contrapt.jeditutil;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.gjt.sp.jedit.ServiceManager;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.PluginJAR;

/**
* A dockable panel that displays a list of processes in a top panel and trees provided
* by other plugins in middle tabbed panels
*/
public class UtilityPanel extends JPanel implements DefaultFocusComponent {

   private static List<UtilityPanel> panels = new ArrayList<UtilityPanel>();

   private JList toolList;
   private JTabbedPane middlePane;
   private ListModel listModel;
   private Map<PluginJAR,JScrollPane> pluginPanes;
   private FocusPropertyChangeListener focusListener;

   protected UtilityPanel() {
      super();
      pluginPanes = new HashMap<PluginJAR,JScrollPane>();
      setup();
   }

   /**
   * Return the instance of this thing
   */
   public static UtilityPanel getInstance() {
      UtilityPanel panel = new UtilityPanel();
      panels.add(panel);
      return panel;
   }

   /**
   * Add tabs for any existing instances of this panel.  This would be in response
   * to plugin loading typically
   */
   public static void addPluginTabs() {
      for ( UtilityPanel panel : panels ) panel.addTabs();
   }

   /**
   * Remove tab for the given plugin from existing instances of this panel.  This would
   * typically be called in response to plugin being unloaded
   */
   public static void removePluginTab(PluginJAR plugin) {
      for ( UtilityPanel panel : panels ) panel.removeTab(plugin);
   }

   /**
   * Shutdown this panel by clearing any global state
   */
   public static void shutdown() {
      panels.clear();
   }

   /**
   * Setup the components belonging to this
   * panel
   */
   private void setup() {
      // The top tabbed pane contains the process monitor
      JTabbedPane topPane = new JTabbedPane();
      JPanel toolPanel = new JPanel();
      listModel = ProcessRunner.getListModel();
      toolList = new JList(listModel);
      toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      toolList.addMouseListener(ProcessRunner.getMouseListener());
      toolList.addKeyListener(ProcessRunner.getKeyListener());
      JScrollPane scrollPane = new JScrollPane(toolList);
      toolPanel.add(scrollPane);
      topPane.addTab("Processes", scrollPane);
      // The middle tabbed pane contains plugin supplied trees
      middlePane = new JTabbedPane();
      addTabs();
      // Add components to this Dockable panel in a split pane
      JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
      splitPane.setTopComponent(topPane);
      splitPane.setBottomComponent(middlePane);
      setLayout(new BorderLayout());
      add(BorderLayout.CENTER, splitPane);
      focusListener = new FocusPropertyChangeListener(this);
      KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", focusListener);
   }

   /**
   * Implement DefaultFocusComponent to focus on a component when
   * user switches to this window
   */
   public void focusOnDefaultComponent() {
      toolList.requestFocusInWindow();
   }

   /**
   * Add phlp project tree to the tab pane; then add other plugin
   * trees advertised as services
   */
   private void addTabs() {
      String serviceName = InfoTreeService.class.getName();
      for ( String name : ServiceManager.getServiceNames(serviceName) ) {
         InfoTreeService service = (InfoTreeService)ServiceManager.getService(serviceName, name);
         if ( service == null ) continue;
         if ( service.getTreeModel() == null ) continue;
         if ( pluginPanes.containsKey(service.getPluginJAR()) ) continue;
         JTree tree = new JTree(service.getTreeModel());
         if ( service.getKeyListener() != null ) 
            tree.addKeyListener(service.getKeyListener());
         if ( service.getMouseListener() != null ) 
            tree.addMouseListener(service.getMouseListener());
         if ( service.getTreeWillExpandListener() != null ) 
            tree.addTreeWillExpandListener(service.getTreeWillExpandListener());
         tree.setRootVisible(false);
         JScrollPane scrollPane = new JScrollPane(tree);
         middlePane.addTab(service.getTabName(), scrollPane);
         pluginPanes.put(service.getPluginJAR(), scrollPane);
         service.init();
      }
   }
   
   /**
   * Remove a tab
   */
   private void removeTab(PluginJAR plugin) {
      JScrollPane pane = pluginPanes.get(plugin);
      if ( pane == null ) return;
      middlePane.remove(pane);
   }

}
