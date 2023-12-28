package at.hypercrawler.frontierservice.frontier.domain.service;

import at.hypercrawler.frontierservice.frontier.domain.model.Page;
import at.hypercrawler.frontierservice.frontier.domain.repository.FrontierRepository;
import at.hypercrawler.frontierservice.frontier.domain.service.metric.PriorityClassifier;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import reactor.util.retry.Retry;

@Service
@Slf4j
public class PagePersistanceService {

  private final PriorityClassifier priorityClassifier;
  private final FrontierRepository frontierRepository;

  public PagePersistanceService(PriorityClassifier priorityClassifier, FrontierRepository frontierRepository) {
    this.priorityClassifier = priorityClassifier;
    this.frontierRepository = frontierRepository;
  }

  public Mono<List<Page>> persistPages(UUID crawlerId, List<URL> addresses) {
    List<Page> pages = addresses.stream()
                           .map(address -> new Page(address, priorityClassifier.evaluatePriority(address), crawlerId))
                           .collect(Collectors.toList());

    log.info("Persisting pages: {}", pages);
    return frontierRepository
               .saveAll(pages)
               .collectList()
               .doOnError(e -> log.error("Error while persisting pages: {}", pages, e))
               .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
               .onErrorResume(Exception.class, exception -> Mono.empty());

  }

  public Mono<List<URL>> filterAlreadyCrawled(List<URL> urls) {
    return frontierRepository.findAll()
               .map(Page::url)
               .collectList()
               .map(crawledUrls -> urls.stream()
                                       .filter(url -> !crawledUrls.contains(url))
                                       .collect(Collectors.toList()))
               .doOnError(e -> log.error("Error while filtering already crawled urls: {}", urls, e))
               .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
               .onErrorResume(Exception.class, exception -> Mono.empty());
  }
}
