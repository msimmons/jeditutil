package net.contrapt.jeditutil.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * Collection of project related metadata
 */
public class ProjectData extends BaseModel {

   @JsonProperty
   List<ProjectDef> projects;

}
