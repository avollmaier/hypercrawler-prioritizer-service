package at.hypercrawler.frontierservice.config;

import at.hypercrawler.frontierservice.frontier.domain.model.UpdatePriorityThreshold;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "hypercrawler.frontier-service.metric")
public record MetricProperties(

        @Min(1)
        Integer basePriority,

        @Valid
        @NotNull
        List<UpdatePriorityThreshold> thresholds

) {

}
