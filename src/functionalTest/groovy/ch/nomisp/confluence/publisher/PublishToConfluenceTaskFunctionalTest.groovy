package ch.nomisp.confluence.publisher

//import spock.lang.Ignore

class PublishToConfluenceTaskFunctionalTest extends PublishToConfluenceSpec {

    def "Tasks must be available on project"() {
        given:
        createProject()

        when:
        def result = createGradleRunner()
                .withArguments('tasks')
                .withProjectDir(testProjectDir.root)
                .build()

        then:
        result.output.contains('publishToConfluence')
    }

    def "Task must convert to confluence format"() {
        given:
        createProject()
        createIndexAdoc()

        when:
        def result = createGradleRunner()
                .withArguments(['publishToConfluence', '--convertOnly', '--info'])
                .withProjectDir(testProjectDir.root)
                .build()

        then:
        result.output.contains("Publishing to Confluence skipped ('convert only' is enabled)")
    }

//    @Ignore("can only be run with credentials to  https://nomisp.atlassian.net/")
    def "Task must publish to confluence"() {
        given:
        createProject()
        createIndexAdoc()
        createSubpageAdoc()

        when:
        def result = createGradleRunner()
                .withArguments(['publishToConfluence', '--skipSslVerification', '--info'])
                .withProjectDir(testProjectDir.root)
                .build()

        then:
        result.output.contains("Publishing page")
    }

//    @Ignore("can only be run with credentials to  https://nomisp.atlassian.net/")
    def "Task must publish to confluence using Rest API v1"() {
        given:
        createProjectV1API()
        createV1IndexAdoc()
        createV1SubpageAdoc()

        when:
        def result = createGradleRunner()
                .withArguments(['publishToConfluence', '--skipSslVerification', '--info'])
                .withProjectDir(testProjectDir.root)
                .build()

        then:
        result.output.contains("Publishing page")
    }
}
