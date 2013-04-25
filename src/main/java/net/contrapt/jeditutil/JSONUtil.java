package net.contrapt.jeditutil;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.IOException;

/**
 * Utilities for working with JSON
 */
public class JSONUtil {

   public static String json2xml(String jsonString) throws IOException {
      if ( jsonString == null ) return null;
      JsonFactory f = new JsonFactory();
      JsonParser p = f.createJsonParser(jsonString);
      return json2xml(p);
   }

   private static String json2xml(JsonParser parser) throws IOException {
      StringBuilder result = new StringBuilder();
      String currentParent = null;
      while ( parser.nextToken()!=null ) {
         JsonToken t = parser.nextValue();
         String parent = (parser.getParsingContext().getParent()==null) ? null : parser.getParsingContext().getParent().getCurrentName();
         if ( t.isScalarValue() ) {
            if ( parent != currentParent ) {
               result.append("</"+currentParent+">\n");
               currentParent = null;
            }
            result.append("<"+parser.getCurrentName()+">"+parser.getText()+"</"+parser.getCurrentName()+">\n");
         }
         else if ( t.toString().equals("START_OBJECT")) {
            currentParent = parser.getCurrentName();
            result.append("<"+currentParent+">\n");
         }
      }
      return result.toString();
   }
}
