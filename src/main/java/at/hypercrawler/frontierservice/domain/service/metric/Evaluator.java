package at.hypercrawler.frontierservice.domain.service.metric;

import java.math.BigDecimal;
import java.net.URL;

public interface Evaluator {
    BigDecimal evaluatePriority(URL address);
}
