package net.contrapt.jeditutil.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * Metadata about a project
 */
public class ProjectDef extends BaseModel {

   @JsonProperty
   private String name;

   @JsonProperty
   private String location;

   @JsonProperty
   private String parent;

   @JsonProperty
   private List<String> exclusions;

   @JsonProperty
   private List<String> inclusions;

   protected ProjectDef() {}

   public String getLocation() {
      return location;
   }

   public List<String> getExclusions() {
      return exclusions;
   }

   public List<String> getInclusions() {
      return inclusions;
   }
}
