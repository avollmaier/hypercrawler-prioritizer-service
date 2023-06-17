package at.hypercrawler.frontierservice.frontier.domain.service;

import at.hypercrawler.frontierservice.frontier.domain.service.metric.PriorityClassifier;
import at.hypercrawler.frontierservice.frontier.event.AddressPrioritizedMessage;
import at.hypercrawler.frontierservice.frontier.event.AddressSuppliedMessage;
import at.hypercrawler.frontierservice.manager.CrawlerStatus;
import at.hypercrawler.frontierservice.manager.ManagerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PrioritizerService {
    public static final String PRIORITIZE_ADDRESS_OUT = "prioritize-out-0";
    public static final String PRIORITY_HEADER = "priority";

    private final PriorityClassifier priorityClassifier;
    private final ManagerClient managerClient;
    private final StreamBridge streamBridge;

    public PrioritizerService(PriorityClassifier priorityClassifier, ManagerClient managerClient, StreamBridge streamBridge) {
        this.priorityClassifier = priorityClassifier;
        this.managerClient = managerClient;
        this.streamBridge = streamBridge;
    }


    public Flux<AddressPrioritizedMessage> consumeAddressSuppliedEvent(Flux<AddressSuppliedMessage> flux) {
        return flux.flatMap(addressSuppliedMessage -> prioritizeAddresses(addressSuppliedMessage.crawlerId(), addressSuppliedMessage.address()));
    }

    private Flux<AddressPrioritizedMessage> prioritizeAddresses(UUID crawlerId, List<URL> addresses) {
        return isCrawlerRunning(crawlerId)
                .filter(Boolean::booleanValue)
                .map(address -> addresses)
                .flatMapIterable(address -> address)
                .flatMap(address -> Mono.just(publishAddressPrioritizeEvent(crawlerId, priorityClassifier.evaluatePriority(address), address)))
                .map(Message::getPayload)
                .doOnError(throwable -> log.error("Error while prioritizing addresses", throwable));
    }

    private Message<AddressPrioritizedMessage> publishAddressPrioritizeEvent(UUID crawlerId, int priority, URL address) {
        Message<AddressPrioritizedMessage> addressPrioritizeMessage = MessageBuilder.withPayload(new AddressPrioritizedMessage(
                crawlerId, address
        )).setHeader(PRIORITY_HEADER, priority).build();

        log.info("Sending data with address {} of crawler with id: {}", address, crawlerId);
        boolean result = streamBridge.send(PRIORITIZE_ADDRESS_OUT, addressPrioritizeMessage);
        log.info("Result of sending address {} for crawler with id: {} is {}", address, crawlerId, result);

        return addressPrioritizeMessage;
    }

    private Mono<Boolean> isCrawlerRunning(UUID crawlerId) {
        return managerClient.getCrawlerStatusById(crawlerId)
                .mapNotNull(statusResponse -> statusResponse.status() == CrawlerStatus.STARTED)
                .defaultIfEmpty(Boolean.FALSE)
                .onErrorReturn(Boolean.FALSE);
    }

}
