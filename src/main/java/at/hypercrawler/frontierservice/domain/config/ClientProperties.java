package at.hypercrawler.frontierservice.domain.config;

import at.hypercrawler.frontierservice.domain.model.Threshold;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.List;

@Value
@ConfigurationProperties(prefix = "hypercrawler.frontier-service.client")
public class ClientProperties {
    @NotNull
    URI managerServiceUri;
}
