package net.contrapt.jeditutil.pluginpanel;

import java.awt.BorderLayout;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import java.io.InputStream;
import java.io.IOException;

import org.gjt.sp.jedit.View;

/**
* A plugin panel that shows an HTML buffer.  Any plugin can call this to show
* a bunch of HTML in a jEdit buffer
* Use:
*    <code>HTMLPluginPanel.showHTML(View view, String name, InputStream html)</code>
*/
public class HTMLPluginPanel extends PluginPanel {

   private String name;
   private JEditorPane viewer;

   /**
   * Convenience method to show the given HTML
   *
   * @param view The jEdit view you are calling from
   * @param name A name for the buffer
   * @param html The html to show
   */
   public static void showHTML(View view, String name, InputStream html) {
      HTMLPluginPanel panel = new HTMLPluginPanel(name, html);
      HTMLPluginPanel.addPluginPanel(view, panel);
   }

   /**
   * Construct an html panel and add it as a buffer
   * 
   */
   private HTMLPluginPanel(String name, InputStream html) {
      this.name = name;
      viewer = new JEditorPane("text/html", "");
      try {
         viewer.read(html, null);
      }
      catch (IOException e) {
         System.out.println("Problem reading an html input stream: "+e);
      }
      viewer.setEditable(false);
      setLayout(new BorderLayout());
      add(new JScrollPane(viewer), BorderLayout.CENTER);
   }

   /**
   * Return the name of this plugin panel
   */
   @Override
   public String getPanelName() {
      return name;
   }

   /**
   * Cleanup resources when this panel is closed
   */
   @Override
   public void pluginPanelRemoved() {
      //TODO Implement this
   }

   /**
   * Show this panel as a separate buffer
   */
   @Override
   public boolean showWithBuffer() {
      return false;
   }
}
