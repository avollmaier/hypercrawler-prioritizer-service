package at.hypercrawler.prioritizerservice.domain.service;

import at.hypercrawler.prioritizerservice.domain.service.metric.PriorityClassifier;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
public class PrioritizerService {

    private final PriorityClassifier priorityClassifier;

    public PrioritizerService(PriorityClassifier priorityClassifier) {
        this.priorityClassifier = priorityClassifier;
    }

    public int evaluatePriority(URL address) {
        return priorityClassifier.evaluatePriority(address);
    }
}
