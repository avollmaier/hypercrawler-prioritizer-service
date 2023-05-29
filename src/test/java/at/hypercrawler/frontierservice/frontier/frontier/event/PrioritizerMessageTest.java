package at.hypercrawler.frontierservice.frontier.frontier.event;

import at.hypercrawler.frontierservice.frontier.event.AddressPrioritizedMessage;
import at.hypercrawler.frontierservice.frontier.event.AddressSuppliedMessage;
import at.hypercrawler.frontierservice.manager.CrawlerStatus;
import at.hypercrawler.frontierservice.manager.ManagerClient;
import at.hypercrawler.frontierservice.manager.StatusResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;


@FunctionalSpringBootTest
@AutoConfiguration
class PrioritizerMessageTest {

    @MockBean
    ManagerClient managerClient;
    @Autowired
    private FunctionCatalog catalog;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OutputDestination output;

    @BeforeEach
    void setUp() {
        Mockito.mock();
    }

    @Test
    void whenPrioritizeWithRunningCrawler_thenMessageSend() throws IOException {

        URL address = new URL("http://www.google.com");
        UUID uuid = UUID.randomUUID();

        when(managerClient.getCrawlerStatusById(uuid)).then(invocation -> Mono.just(new StatusResponse(CrawlerStatus.STARTED)));

        Function<Flux<AddressSuppliedMessage>, Flux<Message<AddressPrioritizedMessage>>> prioritize =

                catalog.lookup(Function.class, "prioritize");


        Flux<AddressSuppliedMessage> addressSupplyMessageFlux = Flux.just(new AddressSuppliedMessage(uuid, address));
        StepVerifier.create(prioritize.apply(addressSupplyMessageFlux))
                .expectNextCount(0)
                .verifyComplete();

        assertThat(objectMapper.readValue(output.receive().getPayload(), AddressPrioritizedMessage.class))
                .isEqualTo(new AddressPrioritizedMessage(uuid, address));

    }


    @Test
    void whenPrioritizeWithStoppedCrawler_thenNoMessageSend() throws IOException {

        URL address = new URL("http://www.google.com");
        UUID uuid = UUID.randomUUID();

        when(managerClient.getCrawlerStatusById(uuid)).then(invocation -> Mono.just(new StatusResponse(CrawlerStatus.STOPPED)));

        Function<Flux<AddressSuppliedMessage>, Flux<Message<AddressPrioritizedMessage>>> prioritize =

                catalog.lookup(Function.class, "prioritize");


        Flux<AddressSuppliedMessage> addressSupplyMessageFlux = Flux.just(new AddressSuppliedMessage(uuid, address));
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


        Flux<AddressSuppliedMessage> addressSupplyMessageFlux = Flux.just(new AddressSuppliedMessage(UUID.randomUUID(), address));
        StepVerifier.create(prioritize.apply(addressSupplyMessageFlux))
                .expectNextCount(0)
                .verifyComplete();

        assertNull(output.receive());
    }
}
