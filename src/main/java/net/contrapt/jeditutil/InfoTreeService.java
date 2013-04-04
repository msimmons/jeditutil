package net.contrapt.jeditutil;

import javax.swing.tree.TreeModel;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import javax.swing.event.TreeWillExpandListener;

import org.gjt.sp.jedit.PluginJAR;

/**
* Describes implementation of a tree model for displaying information; plugins
* may extend this class and offer a service to display there own plugin information
* in tree form in the project dockable.  A tab will be added and the tree displayed
* in that space
*/
public interface InfoTreeService {

   /**
   * Returns a name for the tab containing your tree
   */
   public String getTabName();

   /**
   * Initialize the service -- called after your tree has been added to 
   * the dockable
   */
   public void init();

   /**
   * Return the plugin jar for your plugin
   */
   public PluginJAR getPluginJAR();

   /**
   * Return the tree model for your tree
   */
   public TreeModel getTreeModel();

   /**
   * Return a key listener for your tree
   */
   public KeyListener getKeyListener();

   /**
   * Return a mouse listener for your tree
   */
   public MouseListener getMouseListener();

   /**
   * Return listener for tree will expand events
   */
   public TreeWillExpandListener getTreeWillExpandListener();

}
