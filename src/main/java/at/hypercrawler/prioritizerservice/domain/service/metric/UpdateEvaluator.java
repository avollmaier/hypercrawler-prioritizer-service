package at.hypercrawler.prioritizerservice.domain.service.metric;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import at.hypercrawler.crawlerservice.CrawledPageMetrics;
import at.hypercrawler.prioritizerservice.domain.config.MetricProperties;
import at.hypercrawler.prioritizerservice.domain.model.Threshold;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UpdateEvaluator
  implements Evaluator {

  private final MetricProperties metricProperties;
  private final WebClient webClient;

  public UpdateEvaluator(MetricProperties metricProperties, WebClient webClient) {
    this.metricProperties = metricProperties;
    this.webClient = webClient;
  }

  @Override
  public BigDecimal evaluatePriority(URL address) {
    try {
      CrawledPageMetrics crawledPageMetrics =
        webClient.get().uri(address.toURI()).retrieve().bodyToMono(CrawledPageMetrics.class).block();

      Instant lastModified = getLastModifiedInstant(address);
      Instant lastCrawled = crawledPageMetrics.lastCrawled();

      //calculate page age

      long differenceInDays = calculateTimeDifferenceInDays(lastModified, lastCrawled);

      log.info("Difference in days since now for url: {} is: {}", address, differenceInDays);

      for (Threshold threshold : metricProperties.getThresholds()) {
        log.info(differenceInDays + " " + threshold.getDays());
        if (differenceInDays >= threshold.getDays()) {
          return threshold.getMultiplier();
        }
      }

      return BigDecimal.ONE;

    }
    catch (IOException e) {
      log.error("Error while evaluating update priority for address: {}", address, e);
    }

    return BigDecimal.ZERO;
  }

  private long calculateTimeDifferenceInDays(Instant lastModified, Instant lastCrawled) {
    LocalDateTime lastModifiedLocalDateTime = LocalDateTime.ofInstant(lastModified, ZoneOffset.UTC);
    LocalDateTime lastCrawledLocalDateTime = LocalDateTime.ofInstant(lastCrawled, ZoneOffset.UTC);

    return lastModifiedLocalDateTime.toLocalDate().toEpochDay() - lastCrawledLocalDateTime.toLocalDate()
      .toEpochDay();
  }

  private Instant getLastModifiedInstant(URL address) {
    try {
      HttpURLConnection connection = (HttpURLConnection) address.openConnection();
      return Instant.ofEpochMilli(connection.getLastModified());
    }
    catch (IOException e) {
      log.error("Error while getting last modified date for address: {}", address, e);
    }
    return Instant.now();
  }

}
