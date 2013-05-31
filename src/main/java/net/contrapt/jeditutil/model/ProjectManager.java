package net.contrapt.jeditutil.model;

import net.contrapt.jeditutil.model.BaseModel;
import net.contrapt.jeditutil.model.ProjectData;
import net.contrapt.jeditutil.model.ProjectDef;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Manage project data
 */
public class ProjectManager {


   private ProjectData projects;

   private Map<File, ProjectCache> projectsByLocation;

   private Map<String, ProjectCache> projectsByName;

   public ProjectManager(String dataFile) {
      openProjects(dataFile);
      mapProjects();
   }

   public void shutdown() {
      // Write the data file
      projects = null;
      for ( ProjectCache cache : projectsByLocation.values() ) {
         cache.clear();
      }
      projectsByLocation.clear();
      projectsByName.clear();
   }

   /**
    * Open the projects data file
    */
   private void openProjects(String dataFile) {
      try {
         projects = BaseModel.readData(dataFile, ProjectData.class);
      }
      catch (Exception e) {
         throw new RuntimeException("Error opening project data file "+dataFile, e);
      }
      if ( projects == null ) projects = new ProjectData();
   }

   /**
    * Map projects by location for convenience
    */
   private void mapProjects() {
      projectsByLocation = new HashMap<File, ProjectCache>();
      projectsByName = new HashMap<String, ProjectCache>();
      for ( ProjectDef project : projects.getProjects() ) {
         mapProject(project);
      }
   }

   private void mapProject(ProjectDef project) {
      ProjectCache cache = new ProjectCache(project);
      projectsByLocation.put(cache.getLocation(), cache);
      projectsByName.put(cache.getProject().getName(), cache);
   }

   /**
    * Find the project for the given file by recursively searching parent directories until a project location is found;
    * return null if no project is found
    */
   public ProjectCache findProjectForFile(File file) {
      File dir = file.getParentFile();
      if ( dir == null ) return null;
      else if ( projectsByLocation.keySet().contains(file.getParentFile()) ) return projectsByLocation.get(file.getParentFile());
      else return findProjectForFile(dir);
   }

   /**
    * Return the project for the given name, null if there is no such project
    */
   public ProjectCache findProject(String name) {
      return projectsByName.get(name);
   }

   /**
    * Return the map of project name to cache
    */
   public Map<String,ProjectCache> getProjectsByName() {
      return projectsByName;
   }

}
