package net.contrapt.jeditutil.model;

import java.io.File;
import java.util.*;

/**
 * Caches live data about project
 */
public class ProjectCache {

   private ProjectDef project;

   /**
    * When did we last cache files
    */
   private long lastCached = 0;

   private File location;

   private Map<String, File> files;

   private Map<File, Set<String>> directoryContents;

   private Set<File> directories;

   public ProjectCache(ProjectDef project) {
      this.project = project;
      this.location = new File(project.getLocation());
      files = new HashMap<String, File>();
      directories = new HashSet<File>();
      directories.add(location);
      directoryContents = new HashMap<File, Set<String>>();
   }

   public void clear() {
      directories.clear();
      directories.add(location);
      files.clear();
      lastCached = 0;
   }

   public Set<File> getDirectories() {
      long maxModified = 0;
      for (File d : directories) {
         cacheDirectory(d);
         maxModified = Math.max(maxModified, d.lastModified());
      }
      lastCached = maxModified;
      return directories;
   }

   public Map<String, File> getFiles() {
      getDirectories();
      return files;
   }

   public boolean isOutOfDate() {
      System.out.println("lastCached: " + lastCached);
      for (File dir : directories) {
         System.out.println("   " + dir + ": " + dir.lastModified());
         if (dir.lastModified() > lastCached) return true;
      }
      return false;
   }

   public File getLocation() {
      return location;
   }

   private boolean isExcluded(File dir) {
      String relativePath = dir.getPath().replace(location.getPath(), "");
      for (String e : project.getExclusions()) {
         if (relativePath.startsWith(e) || relativePath.startsWith(File.separator + e)) {
            for (String i : project.getInclusions()) {
               if (relativePath.startsWith(i) || relativePath.startsWith(File.separator + i)) return false;
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
      if (isExcluded(dir)) return;
      else if (isChildDir(dir)) return;
      else {
         if (!directories.contains(dir)) directories.add(dir);
         if (dir.lastModified() > lastCached) cacheFiles(dir);
      }
   }

   private void cacheFiles(File dir) {
      File[] fileList = dir.listFiles();
      if (fileList == null) return;
      for (File f : fileList) {
         cacheFile(dir, f);
      }
      removeDeletedFiles(dir);
   }

   private void cacheFile(File dir, File file) {
      if (file.isDirectory()) {
         cacheDirectory(file);
      } else {
         String fileKey = createFileKey(file);
         files.put(fileKey, file);
         trackDirectoryContents(dir, fileKey);
      }
   }

   private void trackDirectoryContents(File dir, String fileKey) {
      Set<String> fileKeys = directoryContents.get(dir);
      if (fileKeys == null) fileKeys = new HashSet<String>();
      fileKeys.add(fileKey);
      directoryContents.put(dir, fileKeys);
   }

   private void removeDeletedFiles(File dir) {
      if (directoryContents.get(dir) == null) return;
      List<String> removedKeys = new ArrayList<String>();
      for (String fileKey : directoryContents.get(dir)) {
         File file = files.get(fileKey);
         if (file == null) continue;
         if (!file.exists()) {
            files.remove(fileKey);
            removedKeys.add(fileKey);
         }
      }
      for (String fileKey : removedKeys) {
         directoryContents.get(dir).remove(fileKey);
      }
   }

   public String createFileKey(File file) {
      return file.getName() + " (" + file.getParent().replace(location.getPath(), "") + ")";
   }

}
