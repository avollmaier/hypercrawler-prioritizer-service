package at.hypercrawler.prioritizerservice.domain.service.metric;

import at.hypercrawler.prioritizerservice.domain.config.MetricProperties;
import at.hypercrawler.prioritizerservice.domain.model.Threshold;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@Slf4j
public class UpdateEvaluator implements Evaluator {

    private final MetricProperties metricProperties;

    public UpdateEvaluator(MetricProperties metricProperties) {
        this.metricProperties = metricProperties;
    }

    @Override
    public BigDecimal evaluatePriority(URL address) {
        try {
            HttpURLConnection connection = (HttpURLConnection) address.openConnection();
            long lastModified = connection.getLastModified();

            long differenceInDays = calculateTimeDifferenceInDays(lastModified);
            log.info("Difference in days since now for url: {} is: {}", address, differenceInDays);

            for (Threshold threshold : metricProperties.getThresholds()) {
                if (differenceInDays < threshold.days()) {
                    return threshold.multiplier();
                }
            }

            return BigDecimal.ONE;

        } catch (Exception e) {
            log.error("Error while evaluating update priority for address: {}", address, e);
        }

        return BigDecimal.ZERO;
    }

    private long calculateTimeDifferenceInDays(long lastModifiedMillis) {
        Instant instant = Instant.ofEpochMilli(lastModifiedMillis);
        LocalDateTime lastModifiedDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);

        //TODO call the crawler-service to get the last crawled date and calculate the difference in days

        LocalDateTime currentDateTime = LocalDateTime.now(ZoneOffset.UTC);
        return currentDateTime.toLocalDate().toEpochDay() - lastModifiedDateTime.toLocalDate().toEpochDay();
    }

}
