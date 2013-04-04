package net.contrapt.jeditutil.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
* Define a menu item
*/
public class MenuItem extends BaseModel {

   @JsonProperty
   private String name;

   @JsonProperty
   private String action;

   @JsonProperty
   private String accelerator;

   @JsonProperty
   private boolean separator;

   @JsonProperty
   private List<MenuItem> menuItems = new ArrayList<MenuItem>();

   protected MenuItem() {}

   public MenuItem(String name, String action, String accelerator) {
      this.name = name;
      this.action = action;
      this.accelerator = accelerator;
      this.separator = false;
   }

   public MenuItem(boolean separator) {
      this.name="-";
      this.separator = true;
   }

   public String getName() {
      return name;
   }

   public String getAction() {
      return action;
   }

   public String getAccelerator() {
      return accelerator;
   }

   public boolean isSeparator() {
      return separator;
   }

   public List<MenuItem> getMenuItems() {
      return menuItems;
   }

   public void addMenuItem(MenuItem item) {
      menuItems.add(item);
   }
}

