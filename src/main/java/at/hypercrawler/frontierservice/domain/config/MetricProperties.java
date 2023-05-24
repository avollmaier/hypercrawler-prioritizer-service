package at.hypercrawler.frontierservice.domain.config;

import at.hypercrawler.frontierservice.domain.model.Threshold;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "hypercrawler.frontier-service.metric")
public class MetricProperties {
    private int basePriority;
    private List<Threshold> thresholds;
}
