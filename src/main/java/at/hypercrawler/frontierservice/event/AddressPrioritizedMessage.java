package at.hypercrawler.frontierservice.event;

import java.net.URL;
import java.util.UUID;

public record AddressPrioritizedMessage(UUID crawlerId, URL address) {
}
