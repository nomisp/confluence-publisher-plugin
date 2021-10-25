/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package ch.nomisp.confluence.publisher

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Plugin to publish asciidoc to confluence.
 *
 * This plugin is based and makes use of https://github.com/confluence-publisher/confluence-publisher and it's Maven plugin.
 *
 * See also: https://confluence-publisher.atlassian.net/wiki/spaces/CPD/overview?mode=global
 */
class ConfluencePublisherPlugin implements Plugin<Project> {

    private static final Logger logger = Logging.getLogger(ConfluencePublisherPlugin)

    static final String CONFLUENCE_PUBLISHING_EXTENSION = 'confluencePublisher'
    static final String CONFLUENCE_PUBLISHING_TASK = 'publishToConfluence'
    static final String PLUGIN_GROUP = 'publishing'

    void apply(Project project) {
        project.extensions.create(CONFLUENCE_PUBLISHING_EXTENSION, ConfluencePublisherExtension)

        registerPublishToConfluenceTask(project)
    }

    static def registerPublishToConfluenceTask(final Project project) {
        return project.tasks.register(CONFLUENCE_PUBLISHING_TASK, PublishToConfluenceTask) {
            ConfluencePublisherExtension extension = project.confluencePublisher
            setGroup(PLUGIN_GROUP)
            setDescription('Publishes generated asciidoc HTML to confluence.')
            asciiDocRootFolder = extension.asciiDocRootFolder
            outputDir = extension.outputDir
            rootConfluenceUrl = extension.rootConfluenceUrl
            spaceKey = extension.spaceKey
            username = extension.username
            password = extension.password
            pageTitlePrefix = extension.pageTitlePrefix
            pageTitleSuffix = extension.pageTitleSuffix
            ancestorId = extension.ancestorId
            versionMessage = extension.versionMessage
            notifyWatchers = extension.notifyWatchers

            sourceEncoding = extension.sourceEncoding
            publishingStrategy = extension.publishingStrategy
            orphanRemovalStrategy = extension.orphanRemovalStrategy
            maxRequestsPerSecond = extension.maxRequestsPerSecond

            proxyScheme = extension.proxyScheme
            proxyHost = extension.proxyHost
            proxyPort = extension.proxyPort
            proxyUsername = extension.proxyUsername
            proxyPassword = extension.proxyPassword

            attributes = extension.attributes
        }
    }
}