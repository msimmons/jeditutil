package net.contrapt.jeditutil.model;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: mark
 * Date: 4/4/13
 * Time: 9:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectManagerTest {

   String directory;

   public ProjectManagerTest() {
      directory = System.getProperty("testDataDir");
   }

   @Test
   public void testFindForFile() throws IOException {
      String inFile = directory+File.separator+"project1.json";
      ProjectManager manager = new ProjectManager(inFile);
      File f = new File(directory+File.separator+"project1"+File.separator+"build"+File.separator+"file2.txt");
      ProjectCache cache = manager.findProjectForFile(f);
      assertEquals(cache.getProject().getName(), "project1", "Project for file2");
      f = new File(directory+File.separator+"project1"+File.separator+"file1.txt");
      cache = manager.findProjectForFile(f);
      assertEquals(cache.getProject().getName(), "project1", "Project for file1");
   }

   @Test
   public void testNotFoundForFile() throws IOException {
      String inFile = directory+File.separator+"project1.json";
      ProjectManager manager = new ProjectManager(inFile);
      File f = new File(directory+File.separator+"project1.json");
      ProjectCache cache = manager.findProjectForFile(f);
      assertNull(cache, "Should not find project");
   }

   @Test
   public void testByName() throws IOException {
      String inFile = directory+File.separator+"project1.json";
      ProjectManager manager = new ProjectManager(inFile);
      ProjectCache cache = manager.findProject("project2");
      assertEquals(cache.getProject().getName(), "project2", "Project by name");
   }

   @Test
   public void testNoProject() throws IOException {
      String inFile = directory+File.separator+"project1.json";
      ProjectManager manager = new ProjectManager(inFile);
      ProjectCache cache = manager.findProject("noexist");
      assertNull(cache, "Should not find project with that name");
   }

}
