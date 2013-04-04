package net.contrapt.jeditutil;

import java.util.List;

import org.gjt.sp.jedit.Buffer;

/**
* This interface describes a jEdit service that consumes and/or provides
* properties whose values depend on a given buffer.  For instance, a java
* plugin may provide the classname of a given buffer as a named property; a
* project plugin may provide project specific properties.  Each plugin that
* implements this service can declare what properties they need so that another
* plugin can be configured to provide them.
*/
public interface DynamicPropertyService {

   /**
   * Return a list of properties you would like to receive from another plugin
   *
   * @return A <code>List</code> of <code>DynamicPropertyDescriptor</code>
   *         describing properties you need
   */
   public List<DynamicPropertyDescriptor> getConsumedProperties();

   /**
   * Return a list of properties that you can provide to other plugins
   *
   * @return A <code>List</code> of <code>DynamicPropertyDescriptor</code>
   *         describing properties you can provide
   */
   public List<DynamicPropertyDescriptor> getProvidedProperties();

   /**
   * Does this implementation provide the given property?
   *
   * @param name The name of the property
   * @return <code>true</code> if provided <code>false</code> if not
   */
   public boolean providesProperty(String name);

   /**
   * Return the value of the given property name for the given buffer provided
   * by any implementation of this service
   *
   * @param name The name of the property
   * @param buffer The buffer for which you want the property
   * @return The value of the property or null if not found
   */
   public Object getProperty(String name, Buffer buffer);

}
