package net.contrapt.jeditutil.model;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

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

   private Map<File, Set<String>> directoryFileKeys;

   private Set<File> directories;

   private List<Pattern> exclusions;

   private List<Pattern> inclusions;

   public ProjectCache(ProjectDef project) {
      this.project = project;
      this.location = new File(project.getLocation());
      files = new TreeMap<String, File>();
      directories = new HashSet<File>();
      directories.add(location);
      directoryFileKeys = new HashMap<File, Set<String>>();
   }

   public ProjectDef getProject() {
      return project;
   }

   public void clear() {
      directories.clear();
      directories.add(location);
      files.clear();
      lastCached = 0;
      directoryFileKeys.clear();
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
      for (File dir : directories) {
         if (dir.lastModified() > lastCached) return true;
      }
      return false;
   }

   public File getLocation() {
      return location;
   }

   private boolean isExcluded(File dir) {
      if ( exclusions == null ) exclusions = compileRegex(project.getExclusions());
      if ( inclusions == null ) inclusions = compileRegex(project.getInclusions());
      String relativePath = dir.getPath().replace(location.getPath(), "");
      if ( relativePath.startsWith(".") || relativePath.startsWith(File.separator+".")) return true;
      for (Pattern i : inclusions) {
         if ( i.matcher(relativePath).matches() ) return false;
      }
      for (Pattern e : exclusions) {
         if ( e.matcher(relativePath).matches() ) return true;
      }
      return false;
   }

   private List<Pattern> compileRegex(List<String> regexes) {
      List<Pattern> result = new ArrayList<Pattern>();
      for ( String regex : regexes ) {
         result.add(Pattern.compile(regex));
      }
      return result;
   }

   private boolean isChildDir(File dir) {
      return false;
   }

   private void cacheDirectory(File dir) {
      if (isChildDir(dir)) return;
      if (!directories.contains(dir)) directories.add(dir);
      if (dir.lastModified() > lastCached) cacheFiles(dir);
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
      if (file.isDirectory()) cacheDirectory(file);
      else if ( isExcluded(dir) ) return;
      else {
         String fileKey = createFileKey(file);
         files.put(fileKey, file);
         trackDirectoryContents(dir, fileKey);
      }
   }

   private void trackDirectoryContents(File dir, String fileKey) {
      Set<String> fileKeys = directoryFileKeys.get(dir);
      if (fileKeys == null) fileKeys = new HashSet<String>();
      fileKeys.add(fileKey);
      directoryFileKeys.put(dir, fileKeys);
   }

   private void removeDeletedFiles(File dir) {
      if (directoryFileKeys.get(dir) == null) return;
      List<String> removedKeys = new ArrayList<String>();
      for (String fileKey : directoryFileKeys.get(dir)) {
         File file = files.get(fileKey);
         if (file == null) continue;
         if (!file.exists()) {
            files.remove(fileKey);
            removedKeys.add(fileKey);
         }
      }
      for (String fileKey : removedKeys) {
         directoryFileKeys.get(dir).remove(fileKey);
      }
   }

   public String createFileKey(File file) {
      return file.getName() + " (" + file.getParent().replace(location.getPath(), "") + ")";
   }

}
