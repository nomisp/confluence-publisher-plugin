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
                .withArguments('publishToConfluence')
                .withArguments('-PconvertOnly=true')
                .withArguments('--stacktrace')
//                .withArguments('--debug')
                .withProjectDir(testProjectDir.root)
                .build()

        then:
        result.output.contains("Publishing to Confluence skipped ('convert only' is enabled)")
    }
}
