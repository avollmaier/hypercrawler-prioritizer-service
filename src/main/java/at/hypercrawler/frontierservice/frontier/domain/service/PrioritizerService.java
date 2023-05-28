package at.hypercrawler.frontierservice.frontier.domain.service;

import at.hypercrawler.frontierservice.frontier.domain.service.metric.PriorityClassifier;
import at.hypercrawler.frontierservice.frontier.event.AddressPrioritizedMessage;
import at.hypercrawler.frontierservice.frontier.event.AddressSuppliedMessage;
import at.hypercrawler.frontierservice.manager.CrawlerStatus;
import at.hypercrawler.frontierservice.manager.ManagerClient;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.UUID;

@Service
public class PrioritizerService {
    private final PriorityClassifier priorityClassifier;
    private final ManagerClient managerClient;


    public PrioritizerService(PriorityClassifier priorityClassifier, ManagerClient managerClient) {
        this.priorityClassifier = priorityClassifier;
        this.managerClient = managerClient;
    }

    public int evaluatePriority(URL address) {
        return priorityClassifier.evaluatePriority(address);
    }


    public Mono<URL> consumeAddressSuppliedEvent(Flux<AddressSuppliedMessage> flux) {
        return flux.map(addressSuppliedMessage -> {
            return isCrawlerRunning(addressSuppliedMessage.crawlerId())
                    .map(running -> createAddressPrioritizedMessage(addressSuppliedMessage.crawlerId(), 10, addressSuppliedMessage.address()));
        })
    }


    public Mono<Boolean> isCrawlerRunning(UUID crawlerId) {
        return managerClient.getCrawlerStatusById(crawlerId)
                .map(statusResponse -> statusResponse.status() == CrawlerStatus.STARTED)
                .defaultIfEmpty(Boolean.FALSE);
    }


    private Message<AddressPrioritizedMessage> createAddressPrioritizedMessage(UUID crawlerId, int priority, URL address) {
        return MessageBuilder.withPayload(new AddressPrioritizedMessage(
                crawlerId, address
        )).setHeader("priority", priority).build();
    }
}
