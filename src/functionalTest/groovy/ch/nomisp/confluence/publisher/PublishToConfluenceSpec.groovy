package ch.nomisp.confluence.publisher

import org.gradle.internal.impldep.org.junit.Rule
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class PublishToConfluenceSpec extends Specification{

    @Rule
    public TemporaryFolder testProjectDir = new TemporaryFolder()
    protected String testProjectDirPath
    protected File buildFile

    private File asciiDocDir

    def setup() {
        testProjectDir.create()
        testProjectDir.newFolder('tmplib')
        testProjectDirPath = testProjectDir.root.canonicalPath.replaceAll('\\\\', '/')

        buildFile = testProjectDir.newFile('build.gradle')
        asciiDocDir = testProjectDir.newFolder('docs/asciidoc')
        asciiDocDir.createNewFile()
    }

    def createProject() {
        buildFile << """
plugins {
    id 'java'
    id 'ch.nomisp.confluence.publisher'
}

confluencePublisher {
    asciiDocRootFolder = file("\${projectDir}/docs/asciidoc")
    confluencePublisherBuildFolder = file("\${buildDir}/docs/confluence")
    rootConfluenceUrl = 'https://confluence.athome.com'
    spaceKey = 'NOMISP'
    ancestorId = 3774579
    username = 'schnipp.schnapp@techuser.com'
    password = 'jf47AjxRJrYzzW9'
    pageTitlePrefix = 'Generated-Test -- '
    notifyWatchers = false
}

"""
    }

    GradleRunner createGradleRunner() {
        return GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
//                .withDebug(true)
    }
}
