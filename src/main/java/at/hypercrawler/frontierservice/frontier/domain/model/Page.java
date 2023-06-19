package at.hypercrawler.frontierservice.frontier.domain.model;

import org.springframework.data.annotation.Id;

import java.net.URL;
import java.util.UUID;

public record Page(@Id URL url, int priority, UUID crawlerId) {
}
