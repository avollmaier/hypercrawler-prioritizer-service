import at.hypercrawler.prioritizerservice.event.AddressPrioritizedMessage;
import at.hypercrawler.prioritizerservice.event.AddressSupplyMessage;
import at.hypercrawler.prioritizerservice.event.PrioritizingFunctions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.function.Function;


@FunctionalSpringBootTest
@AutoConfiguration
public class PrioritizingFunctionsIntegrationTest {

    @Autowired
    private FunctionCatalog catalog;

    @Test
    void prioritize() {
        Function<Flux<AddressSupplyMessage>, Flux<AddressPrioritizedMessage>> prioritize =
                catalog.lookup(Function.class, "prioritize");

        Flux<AddressSupplyMessage> addressSupplyMessageFlux = Flux.just(new AddressSupplyMessage(UUID.randomUUID(), "http://www.google.com"));
        StepVerifier.create(prioritize.apply(addressSupplyMessageFlux))
                .expectNextMatches(addressPrioritizedMessage -> addressPrioritizedMessage.address().equals("http://www.google.com"))
                .verifyComplete();
    }
}
