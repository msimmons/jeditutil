package net.contrapt.jeditutil.model;

import java.io.File;
import java.util.*;

/**
 * Caches live data about project
 */
public class ProjectCache {

   private ProjectDef project;

   /** When did we last cache files */
   private long lastCached = 0;

   private File location;

   private Map<String,File> files;

   private Set<File> directories;

   public ProjectCache(ProjectDef project) {
      this.project = project;
      this.location = new File(project.getLocation());
      files = new HashMap<String,File>();
      directories = new HashSet<File>();
      directories.add(location);
   }

   public void clear() {
      directories.clear();
      directories.add(location);
      files.clear();
      lastCached = 0;
   }

   public Set<File> getDirectories() {
      for ( File d : directories ) {
         cacheDirectory(d);
      }
      lastCached = System.currentTimeMillis();
      return directories;
   }

   public Map<String,File> getFiles() {
      getDirectories();
      return files;
   }

   private boolean isExcluded(File dir) {
      String relativePath = dir.getPath().replace(location.getPath(),"");
      for ( String e : project.getExclusions() ) {
         if ( relativePath.startsWith(e) ) {
            for ( String i : project.getInclusions() ) {
               if ( relativePath.startsWith(i) ) return false;
            }
            return true;
         }
      }
      return false;
   }

   private boolean isChildDir(File dir) {
      return false;
   }

   private void cacheDirectory(File dir) {
      if ( isExcluded(dir) ) return;
      else if ( isChildDir(dir) ) return;
      else {
         if ( !directories.contains(dir) ) directories.add(dir);
         if (dir.lastModified() > lastCached) cacheFiles(dir);
      }
   }

   private void cacheFiles(File dir) {
      File[] fileList = dir.listFiles();
      if ( fileList == null ) return;
      for (File f : fileList ) {
         cacheFile(f);
      }
   }

   private void cacheFile(File file) {
      if ( file.isDirectory() ) {
         cacheDirectory(file);
      }
      else {
         files.put(createFileKey(file), file);
      }
   }

   public String createFileKey(File file) {
      return file.getName()+" ("+file.getParent().replace(location.getPath(), "")+")";
   }

}
