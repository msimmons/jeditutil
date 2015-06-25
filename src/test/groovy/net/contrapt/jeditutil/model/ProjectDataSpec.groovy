package net.contrapt.jeditutil.model

import spock.lang.Specification

/**
 * Created by mark on 6/25/15.
 */
class ProjectDataSpec extends Specification {

    def directory = System.getProperty("testDataDir")

    def "Read and write a project data file"() {
        given:
        String inFile = directory+"/project1.json"
        String outFile = directory+"/project1.out.json"
        ProjectData data1 = BaseModel.readData(inFile, ProjectData.class)

        when:
        data1.writeData(outFile)
        ProjectData data2 = BaseModel.readData(outFile, ProjectData.class)

        then:
        data1 == data2

    }

    def "Add a new project to the data file"() {
        given:
        String outFile = directory+"/project2.out.json"
        ProjectData data1 = new ProjectData()

        when:
        data1.addProject(new ProjectDef("project3", directory+"/project3"))
        data1.writeData(outFile)
        ProjectData data2 = BaseModel.readData(outFile, ProjectData.class)

        then:
        data2 == data1
    }
}
