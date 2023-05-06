package at.hypercrawler.prioritizerservice.event;

import java.util.UUID;

public record AddressPrioritizedMessage(UUID id, String address, int priority) {
}
