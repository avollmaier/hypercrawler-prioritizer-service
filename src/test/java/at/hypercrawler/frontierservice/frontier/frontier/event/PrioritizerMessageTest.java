package at.hypercrawler.frontierservice.frontier.frontier.event;

import at.hypercrawler.frontierservice.frontier.event.AddressPrioritizedMessage;
import at.hypercrawler.frontierservice.frontier.event.AddressSuppliedMessage;
import at.hypercrawler.frontierservice.manager.CrawlerStatus;
import at.hypercrawler.frontierservice.manager.ManagerClient;
import at.hypercrawler.frontierservice.manager.StatusResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.messaging.Message;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportAutoConfiguration(TestChannelBinderConfiguration.class)
@Testcontainers
class PrioritizerMessageTest {

    @Container
    private static final MongoDBContainer mongoContainer =
            new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @MockBean
    ManagerClient managerClient;

    @Autowired
    private FunctionCatalog catalog;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OutputDestination output;

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl);
    }

    @Test
    void whenPrioritizeWithRunningCrawler_thenMessageSend() throws IOException {

        URL address = new URL("http://www.gooogle.com");
        UUID uuid = UUID.randomUUID();

        when(managerClient.getCrawlerStatusById(uuid)).then(invocation -> Mono.just(new StatusResponse(CrawlerStatus.STARTED)));

        Function<Flux<AddressSuppliedMessage>, Flux<Message<AddressPrioritizedMessage>>> prioritize =

                catalog.lookup(Function.class, "prioritize");


        Flux<AddressSuppliedMessage> addressSupplyMessageFlux = Flux.just(new AddressSuppliedMessage(uuid, List.of(address, address)));
        StepVerifier.create(prioritize.apply(addressSupplyMessageFlux))
                .expectNextCount(0)
                .verifyComplete();


    }


    @Test
    void whenPrioritizeWithStoppedCrawler_thenNoMessageSend() throws IOException {

        URL address = new URL("http://www.google.com");
        UUID uuid = UUID.randomUUID();

        when(managerClient.getCrawlerStatusById(uuid)).then(invocation -> Mono.just(new StatusResponse(CrawlerStatus.STOPPED)));

        Function<Flux<AddressSuppliedMessage>, Flux<Message<AddressPrioritizedMessage>>> prioritize =

                catalog.lookup(Function.class, "prioritize");


        Flux<AddressSuppliedMessage> addressSupplyMessageFlux = Flux.just(new AddressSuppliedMessage(uuid, Collections.singletonList(address)));
        StepVerifier.create(prioritize.apply(addressSupplyMessageFlux))
                .expectNextCount(0)
                .verifyComplete();

        assertNull(output.receive());

    }

    @Test
    void whenPrioritizeWithUnknownCrawler_thenNoMessageSend() throws IOException {
        URL address = new URL("http://www.google.com");

        Function<Flux<AddressSuppliedMessage>, Flux<Message<AddressPrioritizedMessage>>> prioritize =
                catalog.lookup(Function.class, "prioritize");


        Flux<AddressSuppliedMessage> addressSupplyMessageFlux = Flux.just(new AddressSuppliedMessage(UUID.randomUUID(), Collections.singletonList(address)));
        StepVerifier.create(prioritize.apply(addressSupplyMessageFlux))
                .expectNextCount(0)
                .verifyComplete();

        assertNull(output.receive());
    }
}
