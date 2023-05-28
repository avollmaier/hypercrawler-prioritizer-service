package at.hypercrawler.frontierservice.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "hypercrawler.frontier-service.client")
public record ClientProperties(

        @NotNull
        URI managerServiceUri

) {
}
