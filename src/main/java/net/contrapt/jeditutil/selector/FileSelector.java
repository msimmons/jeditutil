package net.contrapt.jeditutil.selector;

import net.contrapt.jeditutil.model.ProjectCache;
import net.contrapt.jeditutil.model.ProjectManager;
import java.io.File;
import java.util.Map;

/**
 * Allow selection of project files
 */
public class FileSelector extends UtilSelector<ProjectCache, File> {

   private ProjectCache cache;
   private ProjectManager manager;

   public FileSelector(String defaultValue, ProjectManager manager, ProjectCache cache) {
      super(defaultValue);
      this.cache = cache;
      this.manager = manager;
      if ( this.cache == null ) {
         this.cache = manager.getProjectsByName().values().toArray(new ProjectCache[0])[0];
      }
   }

   public String getTitle() {
      return "Project Files";
   }

   public Map<String, File> loadValueMap() {
      return cache.getFiles();
   }

   public String getDefault() {
      return defaultValue;
   }

   public Map<String, ProjectCache> getParents() {
      return manager.getProjectsByName();
   }

   public String getParentLabel() {
      return "Projects";
   }

   public String getParentKey() {
      return cache.getProject().getName();
   }

   public void setParent(ProjectCache cache) {
      this.cache = cache;
   }

   public ProjectCache getParent() {
      return cache;
   }

}
