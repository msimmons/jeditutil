package net.contrapt.jeditutil.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection of project related metadata
 */
public class ProjectData extends BaseModel {

   @JsonProperty
   List<ProjectDef> projects = new ArrayList<ProjectDef>();


   public void addProject(ProjectDef project) {
      projects.add(project);
   }

   public List<ProjectDef> getProjects() {
      return projects;
   }
}
