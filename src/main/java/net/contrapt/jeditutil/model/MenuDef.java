package net.contrapt.jeditutil.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* Definition of a menu
*/
public class MenuDef extends BaseModel {

   @JsonProperty
   private String name;

   @JsonProperty
   private List<String> modes;

   @JsonProperty
   private List<MenuItem> menuItems = new ArrayList<MenuItem>();

   protected MenuDef() {}

   public MenuDef(String name, String... modes) {
      this.name = name;
      this.modes = Arrays.asList(modes);
   }

   public String getName() {
      return name;
   }

   public List<String> getModes() {
      return modes;
   }

   public List<MenuItem> getMenuItems() {
      return menuItems;
   }

   public MenuDef addMenuItem(MenuItem item) {
      menuItems.add(item);
      return this;
   }
   
}
