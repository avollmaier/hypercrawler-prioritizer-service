package at.hypercrawler.frontierservice.frontier.domain.service;

import at.hypercrawler.frontierservice.frontier.event.AddressPrioritizedMessage;
import java.util.List;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.UUID;

@Service
public class FrontierEventPublisher {

  public static final String PRIORITIZE_ADDRESS_OUT = "prioritize-out-0";
  public static final String PRIORITY_HEADER = "priority";

  private final StreamBridge streamBridge;

  public FrontierEventPublisher(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  public Message<AddressPrioritizedMessage> createMessage(UUID crawlerId, int priority, URL address) {
    return MessageBuilder.withPayload(new AddressPrioritizedMessage(crawlerId, address))
               .setHeader(PRIORITY_HEADER, priority)
               .build();
  }

  private Mono<Message<AddressPrioritizedMessage>> send(Message<AddressPrioritizedMessage> message) {
    UUID crawlerId = message.getPayload().crawlerId();
    URL address = message.getPayload().address();
    boolean result = streamBridge.send(PRIORITIZE_ADDRESS_OUT, message);
    if (!result) {
      throw new RuntimeException("Failed to send message for address: " + address + " of crawler with id: " + crawlerId);
    }
    return Mono.just(message);
  }

  public Flux<Message<AddressPrioritizedMessage>> send(List<Message<AddressPrioritizedMessage>> messages) {
    return Flux.fromIterable(messages)
               .flatMap(this::send);
  }
}