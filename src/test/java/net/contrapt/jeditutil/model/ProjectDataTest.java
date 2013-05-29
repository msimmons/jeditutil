package net.contrapt.jeditutil.model;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: mark
 * Date: 4/4/13
 * Time: 9:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectDataTest {

   String directory;

   public ProjectDataTest() {
      directory = System.getProperty("testDataDir");
   }

   @Test
   public void testExisting() throws IOException {
      String inFile = directory+"/project1.json";
      String outFile = directory+"/project1.out.json";
      ProjectData data1 = BaseModel.readData(inFile, ProjectData.class);
      data1.writeData(outFile);
      ProjectData data2 = BaseModel.readData(outFile, ProjectData.class);
      assertEquals(data2, data1, "Objects should be equal");
   }

   @Test
   public void testNew() throws IOException {
      String outFile = directory+"/project2.out.json";
      ProjectData data1 = new ProjectData();
      data1.addProject(new ProjectDef("project3", directory+"/project3"));
      data1.writeData(outFile);
      ProjectData data2 = BaseModel.readData(outFile, ProjectData.class);
      assertEquals(data2, data1, "Objects should be equal");
   }
}
