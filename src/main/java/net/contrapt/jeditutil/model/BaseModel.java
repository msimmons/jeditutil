package net.contrapt.jeditutil.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

import java.io.File;
import java.io.IOException;

/**
 * Define common behaviour for models
 */
public abstract class BaseModel {

   private static JsonFactory jsonFactory = new MappingJsonFactory();

   public static <T> T readData(String file, Class<T> theClass) throws IOException {
      File f = new File (file);
      if ( !f.exists() ) return null;
      JsonParser parser = jsonFactory.createJsonParser(new File(file));
      return parser.readValueAs(theClass);
   }

   public void writeData(String file) throws IOException {
      JsonGenerator generator = jsonFactory.createJsonGenerator(new File(file), JsonEncoding.UTF8);
      generator.setPrettyPrinter(new DefaultPrettyPrinter());
      generator.writeObject(this);
      //TODO write temp file then rename in case of error?
   }

   public String toString() {
      return new ReflectionToStringBuilder(this).toString();
   }

   public boolean equals(Object o) {
      return EqualsBuilder.reflectionEquals(this, o);
   }

   public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
   }

}
