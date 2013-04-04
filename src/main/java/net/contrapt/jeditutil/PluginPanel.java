package net.contrapt.jeditutil;

import java.awt.Component;
import javax.swing.JPanel;
import java.awt.Dimension;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.textarea.TextArea;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;

/**
* Plugins can extend this panel to either add plugin specific GUI to a split pane
* with a particular text buffer or to add a plugin specific GUI as a separate
* buffer
*/
public abstract class PluginPanel extends JPanel {

   /** The text area for split pane panels */
   private TextArea textArea;

   /** The buffer for split pane panels */
   private Buffer buffer;

   //
   // Public
   //

   /**
   * Return the <code>PluginPanel</code> for the current buffer in the given view if it exists.
   * This method is called by plugins to retrieve the panel they are interested in
   *
   * @param view The view requesting the plugin panel
   */
   public static <P> P getPluginPanel(View view, Class<P> clazz) {
      return (P)UtilPlugin.getInstance().getPluginPanel(view, clazz);
   }

   /**
   * Add a <code>PluginPanel</code> to this view's current buffer
   * This method is called by client plugins to add a new panel
   */
   public static void addPluginPanel(View view, PluginPanel panel) {
      UtilPlugin.getInstance().addPluginPanel(view, panel);
   }

   /**
   * Remove all plugin panels of the given class; a plugin should call this to remove
   * all panels it has added
   */
   public static void removePluginPanels(Class cl) {
      UtilPlugin.getInstance().removePluginPanels(cl);
   }

   /**
   * Call this method to perform client initiated removal of this panel from
   * the edit pane
   */
   public final void removePluginPanel() {
      UtilPlugin.getInstance().removePluginPanel(buffer, getClass());
   }

   /**
   * Return the name of this panel;
   * defaults to last component of class name.  Subclasses
   * can override to provide a custom name
   */
   public String getPanelName() {
      String fullName = getClass().getName();
      return fullName.substring(fullName.lastIndexOf(".")+1);
   }

   /**
   * Override the following method to return the component which should receive
   * focus when the <code>PluginPanel</code> is shown
   */
   public Component getFocusComponent() {
      return null;
   }

   /**
   * The following is called when this panel is removed from the
   * edit pane.  Implement it to cleanup resources etc
   */
   public abstract void pluginPanelRemoved();

   /**
   * Convenience method to return selected text of associated buffer
   * @deprecated
   */
   public final String getSelectedText() {
      if ( textArea != null ) return textArea.getSelectedText();
      else return jEdit.getActiveView().getTextArea().getSelectedText();
   }

   /**
   * Convenience method to return all text of associated buffer
   * @deprecated
   */
   public final String getText() {
      if ( textArea != null ) return textArea.getText();
      else return jEdit.getActiveView().getTextArea().getText();
   }

   /**
   * Should this panel be shown in a split pane with its associated text buffer; or
   * should a new buffer be created which shows only this panel without an associated
   * text buffer.  Default is to show in a split pane.  Override if you want to show
   * in a separate buffer
   */
   public boolean showWithBuffer() { return true; }

   /**
   * If your panel has a particular size it wants to be, implement this method to
   * return it.  Otherwise, half of the split pane will be allocated to your panel
   */
   public Dimension getInitialSize() {
      return null;
   }

   //
   // Package protected
   //

   /**
   * Store the text area for convenience
   */
   final void setTextArea(TextArea textArea) {
      this.textArea = textArea;
   }

   /**
   * Store the buffer associated with this panel
   */
   final void setBuffer(Buffer buffer) {
      this.buffer = buffer;
   }

}
