package at.hypercrawler.frontierservice.domain.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class FrontierConfig {
    @Bean
    @LoadBalanced
    public WebClient webClient() {

        return WebClient.builder().build();
    }
}
