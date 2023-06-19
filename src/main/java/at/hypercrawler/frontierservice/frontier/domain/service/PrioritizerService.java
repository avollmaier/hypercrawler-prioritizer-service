package at.hypercrawler.frontierservice.frontier.domain.service;

import at.hypercrawler.frontierservice.frontier.domain.model.Page;
import at.hypercrawler.frontierservice.frontier.domain.repository.FrontierRepository;
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

    private final FrontierRepository frontierRepository;

    public PrioritizerService(PriorityClassifier priorityClassifier, ManagerClient managerClient, StreamBridge streamBridge, FrontierRepository frontierRepository) {
        this.priorityClassifier = priorityClassifier;
        this.managerClient = managerClient;
        this.streamBridge = streamBridge;
        this.frontierRepository = frontierRepository;
    }


    public Flux<AddressPrioritizedMessage> consumeAddressSuppliedEvent(Flux<AddressSuppliedMessage> flux) {
        return flux.flatMap(addressSuppliedMessage -> prioritizeAddresses(addressSuppliedMessage.crawlerId(), addressSuppliedMessage.address()));
    }

    private Flux<AddressPrioritizedMessage> prioritizeAddresses(UUID crawlerId, List<URL> addresses) {
        return isCrawlerRunning(crawlerId)
                .filter(Boolean::booleanValue)
                .flatMap(a -> filterAlreadyCrawled(addresses))
                .flatMapIterable(address -> address)
                .flatMap(address -> persistAndSendMessage(crawlerId, address))
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

    private Mono<Message<AddressPrioritizedMessage>> persistAndSendMessage(UUID crawlerId, URL address) {
        int priority = priorityClassifier.evaluatePriority(address);
        return frontierRepository.save(new Page(address, priority, crawlerId))
                .map(m -> publishAddressPrioritizeEvent(crawlerId, priority, address))
                .doOnNext(page -> log.info("Page {} is persisted", page))
                .doOnError(throwable -> log.error("Error while persisting page", throwable));
    }

    private Mono<Boolean> isCrawlerRunning(UUID crawlerId) {
        return managerClient.getCrawlerStatusById(crawlerId)
                .mapNotNull(statusResponse -> statusResponse.status() == CrawlerStatus.STARTED)
                .defaultIfEmpty(Boolean.FALSE)
                .onErrorReturn(Boolean.FALSE);
    }

    private Mono<Boolean> isAlreadyCrawled(URL url) {
        return frontierRepository.existsById(url)
                .mapNotNull(exists -> exists)
                .doOnNext(exists -> log.info("Url {} is crawled: {}", url, exists))
                .defaultIfEmpty(Boolean.FALSE)
                .doOnError(throwable -> log.error("Error while checking if url is already crawled", throwable))
                .onErrorReturn(Boolean.FALSE);
    }

    private Mono<List<URL>> filterAlreadyCrawled(List<URL> urls) {
        return Flux.fromIterable(urls)
                .filterWhen(url -> isAlreadyCrawled(url).map(exists -> !exists))
                .distinct()
                .doOnNext(url -> log.info("Url {} is not crawled yet", url))
                .collectList();
    }
}
