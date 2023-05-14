package at.hypercrawler.prioritizerservice.event;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;


@FunctionalSpringBootTest
@AutoConfiguration
public class PrioritizingFunctionsIntegrationTest {

    @Autowired
    private FunctionCatalog catalog;

    @Test
    @Disabled(value = "Not working yet")
    void prioritize() {

       /*
        Function<Flux<AddressSuppliedMessage>, Flux<Message<AddressPrioritizedMessage>>> prioritize =

                catalog.lookup(Function.class, "prioritize");


        Flux<AddressSuppliedMessage> addressSupplyMessageFlux = Flux.just(new AddressSuppliedMessage(UUID.randomUUID(), "http://www.google.com"));
        StepVerifier.create(prioritize.apply(addressSupplyMessageFlux))
                .expectNextMatches(addressPrioritizedMessage -> {
                    AddressPrioritizedMessage m = (AddressPrioritizedMessage) new SimpleMessageConverter().fromMessage(addressPrioritizedMessage, AddressPrioritizedMessage.class);
                    return m.getAddress().equals("http://www.google.com");
                })
                .verifyComplete();
    }
    */
    }
}
