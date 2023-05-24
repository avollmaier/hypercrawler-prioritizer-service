package at.hypercrawler.frontierservice.domain.service;

import java.net.URL;

import org.springframework.stereotype.Service;

import at.hypercrawler.frontierservice.domain.service.metric.PriorityClassifier;

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
