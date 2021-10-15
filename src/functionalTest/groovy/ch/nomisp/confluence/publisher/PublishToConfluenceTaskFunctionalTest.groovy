package ch.nomisp.confluence.publisher

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

        when:
        def result = createGradleRunner()
                .withArguments(['publishToConfluence', '--convertOnly', '--info'])
                .withProjectDir(testProjectDir.root)
                .build()

        then:
        result.output.contains("Publishing to Confluence skipped ('convert only' is enabled)")
    }

    def "Task must publish to confluence"() {
        given:
        createProject()

        when:
        def result = createGradleRunner()
                .withArguments(['publishToConfluence', '--skipSslVerification', '--info'])
                .withProjectDir(testProjectDir.root)
                .build()

        then:
        result.output.contains("Publishing page")
    }
}
