package at.hypercrawler.frontierservice.domain.service.metric;

import at.hypercrawler.frontierservice.domain.config.FrontierConfiguration;
import at.hypercrawler.frontierservice.domain.config.MetricProperties;
import at.hypercrawler.frontierservice.domain.model.Threshold;
import at.hypercrawler.frontierservice.domain.service.metric.Evaluator;

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
        if (differenceInDays < threshold.getDays()) {
          return threshold.getMultiplier();
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