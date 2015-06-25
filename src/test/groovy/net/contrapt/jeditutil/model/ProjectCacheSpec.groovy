package net.contrapt.jeditutil.model

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by mark on 6/25/15.
 */
class ProjectCacheSpec extends Specification {

    def directory = System.getProperty("testDataDir")

    @Unroll
    def "Test basic project file caching"() {
        given:

        String inFile = directory+"/project1.json"
        ProjectData projects = BaseModel.readData(inFile, ProjectData)
        ProjectDef project = projects.getProjects().get(ndx)
        ProjectCache cache = new ProjectCache(project)

        expect:
        println project
        cache.getDirectories().size() == directories
        cache.getFiles().size() == files
        !cache.isOutOfDate()


        where:
        file|ndx|directories|files
        'project1.json'|0|3|2
        'project1.json'|1|6|4
    }

    def "Test project file caching with modified files"() {
        given:

        String inFile = directory+"/project1.json"
        ProjectData projects = BaseModel.readData(inFile, ProjectData)
        ProjectDef project = projects.getProjects().get(1)

        when:
        ProjectCache cache = new ProjectCache(project)
        println project

        then:
        cache.getDirectories().size() == 6
        cache.getFiles().size() == 4
        !cache.isOutOfDate()

        when:
        File newOne = File.createTempFile("file","suf", cache.getLocation());
        newOne.deleteOnExit();
        Thread.sleep(1000);

        then:
        cache.isOutOfDate()
        cache.getFiles().size() == 5

        when:
        newOne.delete();
        Thread.sleep(1000);

        then:
        cache.isOutOfDate()
        cache.getFiles().size() == 4

    }

}
