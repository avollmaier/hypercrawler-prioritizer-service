package at.hypercrawler.frontierservice.domain.service;

import java.net.URL;
import java.util.UUID;

import managerservice.dto.CrawlerStatus;
import managerservice.dto.StatusResponse;
import org.springframework.stereotype.Service;

import at.hypercrawler.frontierservice.domain.service.metric.PriorityClassifier;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class PrioritizerService {
    private final PriorityClassifier priorityClassifier;
    private final WebClient webClient;

    public PrioritizerService(PriorityClassifier priorityClassifier, WebClient webClient) {
        this.priorityClassifier = priorityClassifier;
        this.webClient = webClient;
    }

    public int evaluatePriority(URL address) {
        return priorityClassifier.evaluatePriority(address);
    }

    public boolean isCrawlerRunning(UUID crawlerId) {
        StatusResponse response = webClient.get().uri("http://localhost:9003/crawler/" + crawlerId + "/status").retrieve().bodyToMono(StatusResponse.class).block();

        if (response != null) {
            return response.status() == CrawlerStatus.STARTED;
        }

       return false;
    }
}
