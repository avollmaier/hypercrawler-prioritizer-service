package at.hypercrawler.prioritizerservice.domain.service.metric;

import at.hypercrawler.prioritizerservice.domain.config.MetricProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.List;

@Component
@Slf4j
public class PriorityClassifier {

    private final MetricProperties metricProperties;
    private final List<Evaluator> evaluators;

    public PriorityClassifier(MetricProperties metricProperties, List<Evaluator> evaluators) {
        this.metricProperties = metricProperties;
        this.evaluators = evaluators;
    }

    public int evaluatePriority(URL address) {
        BigDecimal priority = BigDecimal.valueOf(metricProperties.getBasePriority());

        for (Evaluator evaluator : evaluators) {
            BigDecimal evaluatePriority = evaluator.evaluatePriority(address);
            priority = priority.multiply(evaluatePriority);
        }

        log.info("Priority for url: {} is: {}", address, priority);
        return priority.setScale(0, RoundingMode.HALF_UP).intValue();
    }

}
