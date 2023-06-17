package at.hypercrawler.frontierservice.frontier.event;

import at.hypercrawler.frontierservice.frontier.domain.service.PrioritizerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class PrioritizingFunctions {

    private final PrioritizerService prioritizerService;

    public PrioritizingFunctions(PrioritizerService prioritizerService) {
        this.prioritizerService = prioritizerService;
    }

    @Bean
    public Consumer<Flux<AddressSuppliedMessage>> prioritize() {
        return flux -> prioritizerService.consumeAddressSuppliedEvent(flux)
                .doOnNext(e -> log.info("The address {} of crawler with id {} is prioritized", e.address(), e.crawlerId()))
                .subscribe();
    }


}
