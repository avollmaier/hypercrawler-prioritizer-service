package at.hypercrawler.frontierservice.frontier.domain.service.metric;

import at.hypercrawler.frontierservice.config.MetricProperties;
import at.hypercrawler.frontierservice.frontier.domain.model.UpdatePriorityThreshold;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

      for (UpdatePriorityThreshold threshold : metricProperties.thresholds()) {
        if (differenceInDays < threshold.days()) {
          return threshold.multiplier();
        }
      }

      return BigDecimal.ONE;

    } catch (IOException e) {
      log.error("Error while evaluating update priority for address: {}", address, e);
    }

    return BigDecimal.ZERO;
  }

  private long calculateTimeDifferenceInDays(long lastModifiedMillis) {
    Instant instant = Instant.ofEpochMilli(lastModifiedMillis);
    LocalDateTime lastModifiedDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);

    log.info("Last modified date time was: {}", lastModifiedDateTime);

    LocalDateTime currentDateTime = LocalDateTime.now(ZoneOffset.UTC);
    return currentDateTime.toLocalDate().toEpochDay() - lastModifiedDateTime.toLocalDate().toEpochDay();
  }

}