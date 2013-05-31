package net.contrapt.jeditutil.model;

import org.testng.annotations.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: mark
 * Date: 4/4/13
 * Time: 9:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectCacheTest {

   String directory;

   public ProjectCacheTest() {
      directory = System.getProperty("testDataDir");
   }

   @Test
   public void testProject1() throws IOException {
      String inFile = directory+"/project1.json";
      ProjectData projects = BaseModel.readData(inFile, ProjectData.class);
      ProjectDef project = projects.getProjects().get(0);
      ProjectCache cache = new ProjectCache(project);
      System.out.println(project);
      assertEquals(cache.getDirectories().size(), 5, "Number of directories");
      assertEquals(cache.getFiles().size(), 2, "Number of files");
   }

   @Test
   public void testProject2() throws IOException {
      String inFile = directory+"/project1.json";
      ProjectData projects = BaseModel.readData(inFile, ProjectData.class);
      ProjectDef project = projects.getProjects().get(1);
      ProjectCache cache = new ProjectCache(project);
      System.out.println(project);
      assertEquals(cache.getDirectories().size(), 6, "Number of directories");
      assertEquals(cache.getFiles().size(), 4, "Number of files");
   }

   @Test
   public void testOutOfDate() throws IOException, InterruptedException {
      String inFile = directory+"/project1.json";
      ProjectData projects = BaseModel.readData(inFile, ProjectData.class);
      ProjectDef project = projects.getProjects().get(1);
      ProjectCache cache = new ProjectCache(project);
      System.out.println(project);
      assertEquals(cache.getDirectories().size(), 6, "Number of directories");
      assertEquals(cache.getFiles().size(), 4, "Number of files");
      assertFalse(cache.isOutOfDate(), "Cache should be up to date");
      File newOne = File.createTempFile("file","suf", cache.getLocation());
      newOne.deleteOnExit();
      Thread.sleep(1000);
      assertTrue(cache.isOutOfDate(), "New file; Cache should be out of date");
      assertEquals(cache.getFiles().size(), 5, "Number of files");
      newOne.delete();
      Thread.sleep(1000);
      assertTrue(cache.isOutOfDate(), "Deleted file: Cache out of date");
      assertEquals(cache.getFiles().size(), 4, "Number of files");
   }
}
