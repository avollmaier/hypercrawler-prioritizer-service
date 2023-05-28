package at.hypercrawler.frontierservice.config;

import at.hypercrawler.frontierservice.frontier.domain.model.Threshold;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Value
@ConfigurationProperties(prefix = "hypercrawler.frontier-service.metric")
public class MetricProperties {
    @Min(1)
    int basePriority;
    @Valid
    @NotNull
    List<Threshold> thresholds;
}
