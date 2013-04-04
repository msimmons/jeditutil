package net.contrapt.jeditutil;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.ServiceManager;
import org.gjt.sp.util.Log;


/**
* This class provides a default implementation of <code>DynamicPropertyService</code>
* and in general should be extended by plugins providing this service since it
* provides some important default implementations
*/
public abstract class DynamicPropertyProvider implements DynamicPropertyService {

   /**
   * Override this method to define the properties that your plugin consumes
   */
   public List<DynamicPropertyDescriptor> getConsumedProperties() { return Collections.EMPTY_LIST; }

   /**
   * Override this method to define the properties that you plugin provides
   */
   public List<DynamicPropertyDescriptor> getProvidedProperties() { return Collections.EMPTY_LIST; }

   /**
   * Override this method to return the named property for the given buffer
   */
   protected abstract Object getProvidedProperty(String name, Buffer buffer);

   /**
   * Override this method to indicate whether you provide the given property
   */
   public abstract boolean providesProperty(String name);
 
   /**
   * This method looks for the given property first in the current implementation
   * then in any available implementation of the service
   */
   public final Object getProperty(String name, Buffer buffer) {
      if ( providesProperty(name) ) {
         Log.log(Log.DEBUG, this, "Providing property "+name);
         return getProvidedProperty(name, buffer);
      }
      DynamicPropertyService service = findPropertyProvider(name);
      if ( service != null ) return service.getProperty(name, buffer);
      Log.log(Log.DEBUG, this, "No provider found for property "+name);
      return null;
   }

   /**
   * Loop through all registerd <code>DynamicPropertyService</code> to find one
   * that provides the given property
   *
   * @param name The name of the property
   * @return The first <code>DynamicPropertyService</code> found that provides
   *         the given property or null if not found
   */
   public final static DynamicPropertyService findPropertyProvider(String name) {
      String serviceName = DynamicPropertyService.class.getName();
      for ( String providerName : ServiceManager.getServiceNames(serviceName) ) {
         DynamicPropertyService service = (DynamicPropertyService)ServiceManager.getService(serviceName, providerName);
         if ( service == null ) continue;
         if ( service.providesProperty(name) ) return service;
      }
      return null;
   }

   /**
   * Find all properties that various plugins are expecting to consume
   *
   * @return A <code>List</code> of consumed properties
   */
   public final static List<DynamicPropertyDescriptor> findConsumedProperties() {
      List<DynamicPropertyDescriptor> result = new ArrayList<DynamicPropertyDescriptor>();
      String serviceName = DynamicPropertyService.class.getName();
      for ( String providerName : ServiceManager.getServiceNames(serviceName) ) {
         DynamicPropertyService service = (DynamicPropertyService)ServiceManager.getService(serviceName, providerName);
         result.addAll(service.getConsumedProperties());
      }
      return result;
   }

   /**
   * Find all properties that various plugins are providing
   *
   * @return A <code>List</code> of provided properties
   */
   public final static List<DynamicPropertyDescriptor> findProvidedProperties() {
      List<DynamicPropertyDescriptor> result = new ArrayList<DynamicPropertyDescriptor>();
      String serviceName = DynamicPropertyService.class.getName();
      for ( String providerName : ServiceManager.getServiceNames(serviceName) ) {
         DynamicPropertyService service = (DynamicPropertyService)ServiceManager.getService(serviceName, providerName);
         result.addAll(service.getProvidedProperties());
      }
      return result;
   }

}
