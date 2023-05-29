package at.hypercrawler.frontierservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class FrontierConfig {
    @Bean
    public WebClient webClient() {

        return WebClient.builder().build();
    }
}
