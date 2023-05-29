package at.hypercrawler.frontierservice.frontier.manager;

import at.hypercrawler.frontierservice.config.ClientProperties;
import at.hypercrawler.frontierservice.manager.CrawlerStatus;
import at.hypercrawler.frontierservice.manager.ManagerClient;
import at.hypercrawler.frontierservice.manager.StatusResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.Random.class)
class ManagerClientTest {

    private MockWebServer mockWebServer;
    private ManagerClient managerClient;

    @BeforeEach
    void setup() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();

        var webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").uri().toString())
                .build();
        this.managerClient = new ManagerClient(new ClientProperties(mockWebServer.url("/").uri()), webClient);
    }

    @AfterEach
    void clean() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    void whenCrawlerExists_thenReturnStatus() {
        UUID crawlerId = UUID.randomUUID();

        MockResponse mockResponse = new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        	{
                        		"status": "STARTED"
                        	}
                        """);

        mockWebServer.enqueue(mockResponse);

        Mono<StatusResponse> statusResponseMono = managerClient.getCrawlerStatusById(crawlerId);

        StepVerifier.create(statusResponseMono)
                .expectNextMatches(s -> s.status() == CrawlerStatus.STARTED)
                .verifyComplete();
    }

    @Test
    void whenCrawlerNotExists_thenReturnEmpty() {
        UUID crawlerId = UUID.randomUUID();

        MockResponse mockResponse = new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(404);

        mockWebServer.enqueue(mockResponse);

        StepVerifier.create(managerClient.getCrawlerStatusById(crawlerId))
                .expectNextCount(0)
                .verifyComplete();
    }

}