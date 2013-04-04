package net.contrapt.jeditutil.model;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: mark
 * Date: 4/4/13
 * Time: 9:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurationDataTest {

   String directory;

   public ConfigurationDataTest() {
      directory = System.getProperty("testDataDir");
   }

   @Test
   public void testExisting() throws IOException {
      String inFile = directory+"/configuration1.json";
      String outFile = directory+"/configuration1.out.json";
      ConfigurationData data1 = BaseModel.readData(inFile, ConfigurationData.class);
      data1.writeData(outFile);
      ConfigurationData data2 = BaseModel.readData(outFile, ConfigurationData.class);
      assertEquals(data2, data1, "Objects should be equal");
   }

   @Test
   public void testNew() throws IOException {
      String outFile = directory+"/configuration2.out.json";
      ConfigurationData data1 = new ConfigurationData();
      data1.addGlobalMenu((new MenuDef("menu1", "html")).addMenuItem(new MenuItem("name1", "action","accel")));
      data1.addModeMenu((new MenuDef("menu1", "*", "java")).addMenuItem(new MenuItem("name2", "action","accel")));
      data1.writeData(outFile);
      ConfigurationData data2 = BaseModel.readData(outFile, ConfigurationData.class);
      assertEquals(data2, data1, "Objects should be equal");
   }
}
