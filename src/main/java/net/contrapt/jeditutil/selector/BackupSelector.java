package net.contrapt.jeditutil.selector;

import org.gjt.sp.jedit.jEdit;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Allow the selection of jedit backup versions of a given file;
 * defaultValue should be the full path of the file whose local versions we are looking
 * for
 */
public class BackupSelector extends UtilSelector<Object, File> {

   File localFile = new File(defaultValue);
   DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

   public BackupSelector(String defaultValue) {
      super(defaultValue);
   }

   public String getTitle() {
      return "Local Versions - " + localFile.getName();
   }

   public Map<String, File> loadValueMap() {
      FilenameFilter filter = getFilenameFilter();
      File dir = getBackupDir();
      Map<String, File> entries = new TreeMap<String, File>();
      for (File file : dir.listFiles(filter)) {
         String key = format.format(new Date(file.lastModified()));
         entries.put(key, file);
      }
      return entries;
   }

   public String getDefault() {
      return "";
   }

   private FilenameFilter getFilenameFilter() {
      return new FilenameFilter() {
         public boolean accept(File dir, String name) {
            name = name.replace(jEdit.getProperty("backup.suffix"), "");
            name = name.replace(jEdit.getProperty("backup.prefix"), "");
            return name.startsWith(localFile.getName());
         }
      };
   }

   private File getBackupDir() {
      String backupDir = jEdit.getProperty("backup.directory");
      String localDir = localFile.getParent().replace(":", "");
      return new File(backupDir + File.separator + localDir);
   }
}
