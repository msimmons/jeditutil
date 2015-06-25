package net.contrapt.jeditutil.model

import spock.lang.Specification

/**
 * Created by mark on 6/25/15.
 */
class ConfigurationDataSpec extends Specification {
    def directory = System.getProperty("testDataDir")

    def "Read and write a configuration data file"() {
        given:
        String inFile = directory+"/configuration1.json"
        String outFile = directory+"/configuration1.out.json"
        ConfigurationData data1 = BaseModel.readData(inFile, ConfigurationData.class)

        when:
        data1.writeData(outFile)
        ConfigurationData data2 = BaseModel.readData(outFile, ConfigurationData.class)

        then:
        data1 == data2

    }

    def "Add new configuration to the data file"() {
        given:
        String outFile = directory+"/configuration2.out.json"
        ConfigurationData data1 = new ConfigurationData()

        when:
        data1.addGlobalMenu((new MenuDef("menu1", "html")).addMenuItem(new MenuItem("name1", "action","accel")))
        data1.addModeMenu((new MenuDef("menu1", "*", "java")).addMenuItem(new MenuItem("name2", "action","accel")))
        data1.writeData(outFile)
        ConfigurationData data2 = BaseModel.readData(outFile, ConfigurationData.class)

        then:
        data2 == data1
    }
}
