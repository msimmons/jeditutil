package net.contrapt.jeditutil.model

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by mark on 6/25/15.
 */
class ProjectManagerSpec extends Specification {

    @Shared
    def directory = System.getProperty("testDataDir")

    @Unroll
    def "Find a project given a file"() {
        given:
        String inFile = directory+File.separator+"project1.json"
        ProjectManager manager = new ProjectManager(inFile)

        expect:
        found ? manager.findProjectForFile(file).getProject().getName()==project : manager.findProjectForFile(file)==null

        where:
        file|project|found
        new File("${directory}${File.separator}project1${File.separator}build${File.separator}file2.txt")|'project1'|true
        new File("${directory}${File.separator}project1${File.separator}file1.txt")|'project1'|true
        new File("${directory}${File.separator}project1.json")|_|false

    }

    @Unroll
    def "Find a project given its name"() {
        given:
        String inFile = directory+File.separator+"project1.json"
        ProjectManager manager = new ProjectManager(inFile)

        expect:
        found ? manager.findProject(name) : !manager.findProject('name')

        where:
        name|found
        'project1'|true
        'project2'|true
        'noexist'|false

    }

}
