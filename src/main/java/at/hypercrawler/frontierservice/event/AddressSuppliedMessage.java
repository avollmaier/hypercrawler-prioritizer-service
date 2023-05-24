package at.hypercrawler.frontierservice.event;

import java.net.URL;
import java.util.UUID;

public record AddressSuppliedMessage(UUID crawlerId, URL address) {

}
