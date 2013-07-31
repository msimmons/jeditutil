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
      JsonToken nt;
      String currentName="";
      while ( (nt = parser.nextToken())!=null ) {
         if ( nt.toString().equals("START_OBJECT")) {
            result.append("<"+parser.getCurrentName()+">\n");
         }
         else if ( nt.toString().equals("END_OBJECT")) {
            result.append("</"+parser.getCurrentName()+">\n");
         }
         else if ( nt.toString().startsWith("VALUE_")) {
            result.append("<"+currentName+">"+parser.getText()+"</"+currentName+">\n");
         }
         else if ( nt.toString().equals("START_ARRAY")) {
            currentName = parser.getCurrentName();
         }
         else {
            currentName = parser.getCurrentName();
         }
      }
      return result.toString();
   }
}
