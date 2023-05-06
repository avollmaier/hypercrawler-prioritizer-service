package at.hypercrawler.prioritizerservice.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Slf4j
@Configuration
public class PrioritizingFunctions {
    @Bean
    public Function<Flux<AddressSupplyMessage>, Flux<AddressPrioritizedMessage>> prioritize() {
        return addressSupplyMessageFlux -> addressSupplyMessageFlux.map(addressSupplyMessage -> {
            log.info("Prioritizing address {}", addressSupplyMessage.address());
            return new AddressPrioritizedMessage(addressSupplyMessage.id(), addressSupplyMessage.address(), 1);
        });
    }

}
