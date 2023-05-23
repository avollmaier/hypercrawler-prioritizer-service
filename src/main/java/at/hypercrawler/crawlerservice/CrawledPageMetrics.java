package at.hypercrawler.crawlerservice;

import java.time.Instant;

public record CrawledPageMetrics(Instant lastCrawled) {
}
