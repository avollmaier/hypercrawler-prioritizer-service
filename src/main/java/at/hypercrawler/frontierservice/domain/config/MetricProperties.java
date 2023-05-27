package at.hypercrawler.frontierservice.domain.config;

import at.hypercrawler.frontierservice.domain.model.Threshold;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
