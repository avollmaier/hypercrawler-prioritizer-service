package at.hypercrawler.frontierservice.config;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@Value
@ConfigurationProperties(prefix = "hypercrawler.frontier-service.client")
public class ClientProperties {
    @NotNull
    URI managerServiceUri;
}
