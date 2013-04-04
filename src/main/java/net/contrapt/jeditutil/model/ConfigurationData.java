package net.contrapt.jeditutil.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.codehaus.jackson.util.MinimalPrettyPrinter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
* Model the configuration data for this plugin
*/
public class ConfigurationData extends BaseModel {

   static JsonFactory jsonFactory = new MappingJsonFactory();

   @JsonProperty
   private List<MenuDef> globalMenus = new ArrayList<MenuDef>();

   @JsonProperty
   private List<MenuDef> modeMenus = new ArrayList<MenuDef>();

   public List<MenuDef> getGlobalMenus() {
      return globalMenus;
   }

   public List<MenuDef> getModeMenus() {
      return modeMenus;
   }

   public void addGlobalMenu(MenuDef menu) {
      globalMenus.add(menu);
   }

   public void addModeMenu(MenuDef menu) {
      modeMenus.add(menu);
   }

}
