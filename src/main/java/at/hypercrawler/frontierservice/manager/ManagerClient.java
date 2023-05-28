package at.hypercrawler.frontierservice.manager;

import at.hypercrawler.frontierservice.config.ClientProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

@Component
public class ManagerClient {
    private static final String MANAGER_ROOT_API = "/crawlers/";
    private static final String MANAGER_STATUS_API = "/status";

    private final ClientProperties clientProperties;
    private final WebClient webClient;

    public ManagerClient(ClientProperties clientProperties, WebClient webClient) {
        this.clientProperties = clientProperties;
        this.webClient = webClient;
    }


    public Mono<StatusResponse> getCrawlerStatusById(UUID crawlerId) {
        return webClient
                .get()
                .uri(clientProperties.managerServiceUri() + MANAGER_ROOT_API + crawlerId + MANAGER_STATUS_API)
                .retrieve()
                .bodyToMono(StatusResponse.class)
                .timeout(Duration.ofSeconds(3), Mono.empty())
                .onErrorResume(WebClientResponseException.NotFound.class, exception -> Mono.empty())
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .onErrorResume(Exception.class, exception -> Mono.empty());
    }

}
