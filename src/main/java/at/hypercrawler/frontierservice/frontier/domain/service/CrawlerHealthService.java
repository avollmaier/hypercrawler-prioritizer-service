package at.hypercrawler.frontierservice.frontier.domain.service;

import at.hypercrawler.frontierservice.manager.CrawlerStatus;
import at.hypercrawler.frontierservice.manager.ManagerClient;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service

public class CrawlerHealthService {

  private final ManagerClient managerClient;


  public CrawlerHealthService(ManagerClient managerClient) {
    this.managerClient = managerClient;
  }

  public Mono<Boolean> isCrawlerRunning(UUID crawlerId) {
    return managerClient.getCrawlerStatusById(crawlerId)
               .mapNotNull(statusResponse -> statusResponse.status() == CrawlerStatus.STARTED)
               .defaultIfEmpty(Boolean.FALSE)
               .onErrorReturn(Boolean.FALSE);
  }
}
