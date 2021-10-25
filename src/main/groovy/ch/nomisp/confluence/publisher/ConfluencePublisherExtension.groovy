package ch.nomisp.confluence.publisher

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.sahli.asciidoc.confluence.publisher.client.OrphanRemovalStrategy
import org.sahli.asciidoc.confluence.publisher.client.PublishingStrategy

import javax.inject.Inject

class ConfluencePublisherExtension {

    final DirectoryProperty asciiDocRootFolder
    String outputDir
    final Property<String> rootConfluenceUrl
    final Property<String> spaceKey
    final Property<String> username
    final Property<String> password
    final Property<String> pageTitlePrefix
    final Property<String> pageTitleSuffix
    final Property<Integer> ancestorId
    final Property<String> versionMessage
    final Property<Boolean> notifyWatchers

    final Property<String> sourceEncoding
    final Property<PublishingStrategy> publishingStrategy // APPEND_TO_ANCESTOR (default) | REPLACE_ANCESTOR
    final Property<OrphanRemovalStrategy> orphanRemovalStrategy // REMOVE_ORPHANS (default) | KEEP_ORPHANS
    final Property<Integer> maxRequestsPerSecond

//    final Property<Boolean> skipSslVerification

    final Property<String> proxyScheme
    final Property<String> proxyHost
    final Property<Integer> proxyPort
    final Property<String> proxyUsername
    final Property<String> proxyPassword

    final Map<String, Object> attributes = [:]

//    final Property<String> serverId

    @Inject
    ConfluencePublisherExtension(ObjectFactory objects) {
        asciiDocRootFolder = objects.directoryProperty()
        rootConfluenceUrl = objects.property(String)
        spaceKey = objects.property(String)
        username = objects.property(String)
        password = objects.property(String)
        pageTitlePrefix = objects.property(String)
        pageTitleSuffix = objects.property(String)
        ancestorId = objects.property(Integer)
        versionMessage = objects.property(String)
        notifyWatchers = objects.property(Boolean)

        sourceEncoding = objects.property(String)
        publishingStrategy = objects.property(PublishingStrategy) // APPEND_TO_ANCESTOR (default) | REPLACE_ANCESTOR
        orphanRemovalStrategy = objects.property(OrphanRemovalStrategy) // REMOVE_ORPHANS (default) | KEEP_ORPHANS
        maxRequestsPerSecond = objects.property(Integer)

//        skipSslVerification = objects.property(Boolean)

        proxyScheme = objects.property(String)
        proxyHost = objects.property(String)
        proxyPort = objects.property(Integer)
        proxyUsername = objects.property(String)
        proxyPassword = objects.property(String)
    }

    /* -------------------------
   tag::extension-property[]
    attributes:: Asciidoctor attributes.
      Use `attributes` to append and `setAttributes` to replace any current attributes with a new set.
      Attribute values are lazy-evaluated to strings. See <<attributes>> for more detail.
   end::extension-property[]
   ------------------------- */

    /**
     * Returns all of the Asciidoctor options.
     */
    Map<String, Object> getAttributes() {
        attributes
    }

    /**
     * Apply a new set of Asciidoctor attributes, clearing any attributes previously set.
     * This can be set globally for all Asciidoctor tasks in a project. If this is set in a task
     * it will override the global attributes.
     *
     * @param m Map with new options
     */
    void setAttributes(Map m) {
        this.attributes.clear()
        this.attributes.putAll(m)
    }
}
