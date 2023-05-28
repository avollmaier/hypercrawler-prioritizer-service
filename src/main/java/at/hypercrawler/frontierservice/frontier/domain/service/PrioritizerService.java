package at.hypercrawler.frontierservice.frontier.domain.service;

import at.hypercrawler.frontierservice.frontier.domain.service.metric.PriorityClassifier;
import at.hypercrawler.frontierservice.frontier.event.AddressPrioritizedMessage;
import at.hypercrawler.frontierservice.frontier.event.AddressSuppliedMessage;
import at.hypercrawler.frontierservice.manager.CrawlerStatus;
import at.hypercrawler.frontierservice.manager.ManagerClient;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.UUID;

@Service
public class PrioritizerService {
    public static final String PRIORITIZE_ADDRESS_OUT = "prioritizeAddress-out-0";

    private final PriorityClassifier priorityClassifier;
    private final ManagerClient managerClient;
    private final StreamBridge streamBridge;

    public PrioritizerService(PriorityClassifier priorityClassifier, ManagerClient managerClient, StreamBridge streamBridge) {
        this.priorityClassifier = priorityClassifier;
        this.managerClient = managerClient;
        this.streamBridge = streamBridge;
    }


    public Flux<AddressPrioritizedMessage> consumeAddressSuppliedEvent(Flux<AddressSuppliedMessage> flux) {
        return flux.flatMap(addressSuppliedMessage ->
                isCrawlerRunning(addressSuppliedMessage.crawlerId())
                        .flatMap(running -> {
                            if (running == Boolean.TRUE) {
                                Message<AddressPrioritizedMessage> m = createAddressPrioritizedMessage(addressSuppliedMessage.crawlerId(), 10, addressSuppliedMessage.address());
                                streamBridge.send(PRIORITIZE_ADDRESS_OUT, m);

                                return Mono.just(m.getPayload());
                            }
                            return Mono.empty();
                        }).switchIfEmpty(Mono.empty())
        );
    }

    private Message<AddressPrioritizedMessage> createAddressPrioritizedMessage(UUID crawlerId, int priority, URL address) {
        return MessageBuilder.withPayload(new AddressPrioritizedMessage(
                crawlerId, address
        )).setHeader("priority", priority).build();
    }

    private Mono<Boolean> isCrawlerRunning(UUID crawlerId) {
        return managerClient.getCrawlerStatusById(crawlerId)
                .map(statusResponse -> statusResponse.status() == CrawlerStatus.STARTED)
                .defaultIfEmpty(Boolean.FALSE);
    }

    public int evaluatePriority(URL address) {
        return priorityClassifier.evaluatePriority(address);
    }
}
