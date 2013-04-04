package net.contrapt.jeditutil;

import javax.swing.JComponent;


import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.PluginJAR;

/**
* This interface describes a jEdit service that supplies buffer status
* information to be displayed on the <code>BufferStatusPanel</code>.  Any plugin
* can supply buffer specific info to be displayed
*/
public interface BufferStatusService<C extends JComponent> {

   /**
   * Get a plugin specific buffer status component to display in the
   * buffer status panel
   *
   * @return A <code>JComponent</code> that indicates plugin specific status
   */
   public C getComponent();

   /**
   * Instruct the service provider to update the buffer status component
   * for the given buffer
   *
   * @param buffer The current <code>Buffer</code>
   * @param bufferChanged Whether this is in response to buffer change or not
   */
   public void update(C component, Buffer buffer, boolean bufferChanged);

   /**
   * Return the plugins jar file object for keeping track of components
   * when plugins are loaded and unloaded
   */
   public PluginJAR getPluginJAR();

}
