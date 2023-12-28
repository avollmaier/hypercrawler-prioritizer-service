package at.hypercrawler.frontierservice.frontier.event;

import at.hypercrawler.frontierservice.frontier.domain.service.FrontierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class PrioritizingFunctions {

    private final FrontierService frontierService;

    public PrioritizingFunctions(FrontierService frontierService) {
        this.frontierService = frontierService;
    }

    @Bean
    public Consumer<Flux<AddressSuppliedMessage>> prioritize() {
        return flux -> frontierService.consumeAddressSuppliedEvent(flux)
                .subscribe();
    }


}
