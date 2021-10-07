package ch.nomisp.confluence.publisher

import groovy.transform.TypeChecked
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.sahli.asciidoc.confluence.publisher.client.ConfluencePublisher
import org.sahli.asciidoc.confluence.publisher.client.ConfluencePublisherListener
import org.sahli.asciidoc.confluence.publisher.client.OrphanRemovalStrategy
import org.sahli.asciidoc.confluence.publisher.client.PublishingStrategy
import org.sahli.asciidoc.confluence.publisher.client.http.ConfluencePage
import org.sahli.asciidoc.confluence.publisher.client.http.ConfluenceRestClient
import org.sahli.asciidoc.confluence.publisher.client.metadata.ConfluencePublisherMetadata
import org.sahli.asciidoc.confluence.publisher.converter.*

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths

@TypeChecked
class PublishToConfluenceTask extends DefaultTask {

    @InputDirectory
    final DirectoryProperty asciiDocRootFolder = project.objects.directoryProperty()
    @OutputDirectory
    final DirectoryProperty confluencePublisherBuildFolder = project.objects.directoryProperty()
    @Internal
    final Property<String> rootConfluenceUrl = project.objects.property(String)
    @Internal
    final Property<String> spaceKey = project.objects.property(String)
    @Internal
    final Property<String> username = project.objects.property(String)
    @Internal
    final Property<String> password = project.objects.property(String)
    @Internal
    final Property<String> pageTitlePrefix = project.objects.property(String)
    @Internal
    final Property<String> pageTitleSuffix = project.objects.property(String)
    @Internal
    final Property<Integer> ancestorId = project.objects.property(Integer)
    @Internal
    final Property<String> versionMessage = project.objects.property(String)
    @Internal
    final Property<Boolean> notifyWatchers = project.objects.property(Boolean)

    @Internal
    final Property<String> sourceEncoding = project.objects.property(String)
    @Internal
    final Property<PublishingStrategy> publishingStrategy = project.objects.property(PublishingStrategy) // APPEND_TO_ANCESTOR (default) | REPLACE_ANCESTOR
    @Internal
    final Property<OrphanRemovalStrategy> orphanRemovalStrategy = project.objects.property(OrphanRemovalStrategy) // REMOVE_ORPHANS (default) | KEEP_ORPHANS
    @Internal
    final Property<Integer> maxRequestsPerSecond = project.objects.property(Integer)

    @Internal
    final Property<String> proxyScheme = project.objects.property(String)

    // Optional proxy settings. Defaults to empty.
    @Internal
    final Property<String> proxyHost = project.objects.property(String)
    @Internal
    final Property<Integer> proxyPort = project.objects.property(Integer)
    @Internal
    final Property<String> proxyUsername = project.objects.property(String)
    @Internal
    final Property<String> proxyPassword = project.objects.property(String)

    @Internal
    boolean convertOnly = false
    @Internal
    boolean skipSslVerification = false
    @Internal
    boolean enableHttpClientSystemProperties = false

    @Internal
    Map<String, Object> attributes = [:]

    @TaskAction
    def publishToConfluence() {
        checkProperties()
        try {
            String prefix = pageTitlePrefix.isPresent() ? pageTitlePrefix.get() : ''
            String suffix = pageTitleSuffix.isPresent() ? pageTitleSuffix.get() : ''
            PageTitlePostProcessor pageTitlePostProcessor = new PrefixAndSuffixPageTitlePostProcessor(prefix, suffix)

            def encoding = sourceEncoding.isPresent() ? Charset.forName(sourceEncoding.get()) : StandardCharsets.UTF_8
//            Path asciiDocFolder = asciiDocRootFolder.isPresent() ? asciiDocRootFolder.get().asFile.toPath() : project.projectDir.toPath().resolve('src/docs')
            AsciidocPagesStructureProvider asciidocPagesStructureProvider = new FolderBasedAsciidocPagesStructureProvider(asciiDocRootFolder.get().asFile.toPath(), encoding)

            AsciidocConfluenceConverter asciidocConfluenceConverter = new AsciidocConfluenceConverter(spaceKey.get(), ancestorId.get().toString())

//            def buildDir = confluencePublisherBuildFolder.getOrNull()
//            Path outDir
//            if (!buildDir) {
//                outDir = project.buildDir.toPath().resolve('asciidoc-confluence-publisher')
//            } else {
//                outDir = buildDir.asFile.toPath()
//            }

            Path outDir = confluencePublisherBuildFolder.isPresent() ? confluencePublisherBuildFolder.get().asFile.toPath()
                    : project.buildDir.toPath().resolve('asciidoc-confluence-publisher')

            ConfluencePublisherMetadata confluencePublisherMetadata = asciidocConfluenceConverter.convert(asciidocPagesStructureProvider,
                    pageTitlePostProcessor,
                    outDir,
                    attributes)

            if (this.convertOnly) {
                logger.info("Publishing to Confluence skipped ('convert only' is enabled)")
            } else {
                ConfluenceRestClient.ProxyConfiguration proxyConfiguration = null
                if (useProxy()) {
                    proxyConfiguration = new ConfluenceRestClient.ProxyConfiguration(this.proxyScheme.get(),
                            this.proxyHost.get(),
                            this.proxyPort.get(),
                            this.proxyUsername.get(),
                            this.proxyPassword.get())
                }

                Double maxReqPerSec = maxRequestsPerSecond.isPresent() ? maxRequestsPerSecond.get() : Double.valueOf(10.0d)
                ConfluenceRestClient confluenceRestClient = new ConfluenceRestClient(rootConfluenceUrl.get(),
                        proxyConfiguration,
                        this.skipSslVerification,
                        this.enableHttpClientSystemProperties,
                        maxReqPerSec,
                        this.username.get(),
                        this.password.get())
                ConfluencePublisherListener confluencePublisherListener = new LoggingConfluencePublisherListener(logger)

                confluencePublisherMetadata.getPages().forEach(page -> {
                    logger.info("Publishing page: " + page.title + " -- " + page.contentFilePath)
                })

                ConfluencePublisher confluencePublisher = new ConfluencePublisher(confluencePublisherMetadata,
                        this.publishingStrategy.getOrElse(PublishingStrategy.APPEND_TO_ANCESTOR),
                        this.orphanRemovalStrategy.getOrElse(OrphanRemovalStrategy.REMOVE_ORPHANS),
                        confluenceRestClient,
                        confluencePublisherListener,
                        this.versionMessage.getOrElse(''),
                        this.notifyWatchers.getOrElse(true))
                confluencePublisher.publish()
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Publishing to Confluence failed", e)
            } else {
                logger.error("Publishing to Confluence failed: " + e.getMessage())
            }

            throw new GradleException("Publishing to Confluence failed", e)
        }
    }

    private void checkProperties() {
        if (!asciiDocRootFolder || !asciiDocRootFolder.isPresent()) {
            throw new GradleException('asciiDocRootFolder property not set')
        }

        if (!rootConfluenceUrl || !rootConfluenceUrl.isPresent()) {
            throw new GradleException('rootConfluenceUrl property not set')
        }

        if (!username || !username.isPresent()) {
            throw new GradleException('username property not set')
        }

        if (!password || !password.isPresent()) {
            throw new GradleException('password property not set')
        }

        if (!spaceKey || !spaceKey.isPresent()) {
            throw new GradleException('password spaceKey not set')
        }

    }

    private boolean useProxy() {
        return proxyScheme.isPresent() &&
                proxyHost.isPresent() &&
                proxyPort.isPresent() &&
                proxyUsername.isPresent() &&
                proxyPassword.isPresent()
    }

    @Option(option = 'convertOnly', description = 'Defines whether to only convert AsciiDoc sources, but not publish to Confluence (for checking documentation sanity without publishing).')
    void setConvertOnly(boolean convertOnly) {
        this.convertOnly = convertOnly
    }

    @Option(option = 'skipSslVerification', description = 'Defines whether to disable SSL certificate verification when connecting to Confluence via HTTPS while using self- signed certificates.')
    void setSkipSslVerification(boolean skipSslVerification) {
        this.skipSslVerification = skipSslVerification
    }

    @Option(option = 'enableHttpClientSystemProperties', description = 'Defines whether to enable support for configuring the underlying HTTP client using system properties.')
    void setEnableHttpClientSystemProperties(boolean enableHttpClientSystemProperties) {
        this.enableHttpClientSystemProperties = enableHttpClientSystemProperties
    }

    private static class LoggingConfluencePublisherListener implements ConfluencePublisherListener {

        private Logger log

        LoggingConfluencePublisherListener(Logger log) {
            this.log = log;
        }

        @Override
        void pageAdded(ConfluencePage addedPage) {
            this.log.info("Added page '" + addedPage.getTitle() + "' (id " + addedPage.getContentId() + ")");
        }

        @Override
        void pageUpdated(ConfluencePage existingPage, ConfluencePage updatedPage) {
            this.log.info("Updated page '" + updatedPage.getTitle() + "' (id " + updatedPage.getContentId() + ", version " + existingPage.getVersion() + " -> " + updatedPage.getVersion() + ")");
        }

        @Override
        void pageDeleted(ConfluencePage deletedPage) {
            this.log.info("Deleted page '" + deletedPage.getTitle() + "' (id " + deletedPage.getContentId() + ")");
        }

        @Override
        void attachmentAdded(String attachmentFileName, String contentId) {
            this.log.info("Added attachment '" + attachmentFileName + "' (page id " + contentId + ")");
        }

        @Override
        void attachmentUpdated(String attachmentFileName, String contentId) {
            this.log.info("Updated attachment '" + attachmentFileName + "' (page id " + contentId + ")");
        }

        @Override
        void attachmentDeleted(String attachmentFileName, String contentId) {
            this.log.info("Deleted attachment '" + attachmentFileName + "' (page id " + contentId + ")");
        }

        @Override
        void publishCompleted() {
            this.log.info('Publishing completed');
        }

    }
}
