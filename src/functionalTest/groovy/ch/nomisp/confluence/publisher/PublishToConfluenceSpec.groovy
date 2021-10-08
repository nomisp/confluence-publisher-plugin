package ch.nomisp.confluence.publisher

import org.apache.http.HttpEntity
import org.apache.http.StatusLine
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicHeader
import org.gradle.internal.impldep.org.junit.Rule
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import java.util.stream.Collectors
import java.util.stream.IntStream

class PublishToConfluenceSpec extends Specification{

    private static final String CONFLUENCE_ROOT_URL = "http://localhost:8090"

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
    rootConfluenceUrl = '$CONFLUENCE_ROOT_URL'
    spaceKey = 'MNET'
    ancestorId = 123
    username = 'rest.swrd@techuser.com'
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



    private CloseableHttpClient recordHttpClientForRequestException(IOException exception) throws IOException {
        CloseableHttpClient httpClientMock = Mock(CloseableHttpClient)
        HttpRequestBase httpRequestBaseMock = Mock(HttpRequestBase)
        httpClientMock.execute(httpRequestBaseMock) >> exception

        return httpClientMock
    }

    private CloseableHttpClient recordHttpClientForSingleResponseWithContentAndStatusCode(String contentPayload, int statusCode) throws IOException {
        return recordHttpClientForSingleResponseWithContentAndStatusCode(contentPayload, statusCode, null)
    }

    private CloseableHttpClient recordHttpClientForSingleResponseWithContentAndStatusCode(String contentPayload, int statusCode, String reason) throws IOException {
        CloseableHttpResponse httpResponseMock = Mock(CloseableHttpResponse)
        HttpEntity httpEntityMock = recordHttpEntityForContent(contentPayload);

        httpResponseMock.entity >> httpEntityMock

        StatusLine statusLineMock = recordStatusLine(statusCode, reason)
        httpResponseMock.statusLine >> statusLineMock

        CloseableHttpClient httpClientMock = Mock(CloseableHttpClient)
        httpClientMock.execute(_ as HttpRequestBase) >> httpResponseMock

        return httpClientMock
    }

    private HttpEntity recordHttpEntityForContent(String content) {
        HttpEntity httpEntityMock = Mock(HttpEntity)
        httpEntityMock.content >> new ByteArrayInputStream(content.getBytes())
        httpEntityMock.contentEncoding >> new BasicHeader("Content-Encoding", "UTF-8")

        return httpEntityMock
    }

    private StatusLine recordStatusLine(int statusCode, String reason) {
        StatusLine statusLineMock = Mock(StatusLine)
        statusLineMock.statusCode >> statusCode
        statusLineMock.reasonPhrase >> reason

        return statusLineMock
    }

    private String generateJsonPageResults(int numberOfPages) {
        return IntStream.range(1, numberOfPages + 1)
                .boxed()
                .map(pageNumber -> "{" +
                        "\"id\": \"" + pageNumber + "\", " +
                        "\"title\": \"Page " + pageNumber + "\", " +
                        "\"version\": {\"number\": 1}," +
                        "\"ancestors\": [{\"id\": \"ancestor\"}]" +
                        "}")
                .collect(Collectors.joining(",\n"))
    }
}
