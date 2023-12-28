package at.hypercrawler.frontierservice.frontier.domain.service;

import at.hypercrawler.frontierservice.frontier.domain.model.Page;
import at.hypercrawler.frontierservice.frontier.event.AddressPrioritizedMessage;
import at.hypercrawler.frontierservice.frontier.event.AddressSuppliedMessage;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@Service
public class FrontierService {

    private final CrawlerHealthService crawlerService;
    private final PagePersistanceService pagePersistanceService;
    private final FrontierEventPublisher frontierEventPublisher;

    public FrontierService(CrawlerHealthService crawlerHealthService, PagePersistanceService pagePersistanceService, FrontierEventPublisher frontierEventPublisher) {
        this.crawlerService = crawlerHealthService;
        this.pagePersistanceService = pagePersistanceService;
        this.frontierEventPublisher = frontierEventPublisher;
    }

    public Flux<AddressPrioritizedMessage> consumeAddressSuppliedEvent(Flux<AddressSuppliedMessage> flux) {
        return flux.flatMap(this::handleAddressSuppliedMessage)
                   .map(Message::getPayload);
    }

    private Flux<Message<AddressPrioritizedMessage>> handleAddressSuppliedMessage(AddressSuppliedMessage addressSuppliedMessage) {
        UUID crawlerId = addressSuppliedMessage.crawlerId();
        List<URL> addresses = addressSuppliedMessage.address();

        return crawlerService.isCrawlerRunning(crawlerId)
                   .filter(Boolean::booleanValue)
                   .flatMapMany(filter -> pagePersistanceService.filterAlreadyCrawled(addresses))
                   .filter(urls -> !urls.isEmpty())
                   .flatMap(filteredAddresses -> pagePersistanceService.persistPages(crawlerId, filteredAddresses))
                   .map(this::createMessage)
                   .flatMap(frontierEventPublisher::send);
    }

    private List<Message<AddressPrioritizedMessage>> createMessage(List<Page> page) {
        return page.stream()
                   .map(p -> frontierEventPublisher.createMessage(p.crawlerId(), p.priority(), p.url()))
                   .toList();

    }
}