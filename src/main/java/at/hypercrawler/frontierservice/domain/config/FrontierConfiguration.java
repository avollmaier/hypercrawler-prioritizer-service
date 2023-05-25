package at.hypercrawler.frontierservice.domain.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@EnableConfigurationProperties(MetricProperties.class)
public class FrontierConfiguration {

}
