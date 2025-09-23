package br.com.ml.mktplace.orders.adapter.inbound.messaging;

import br.com.ml.mktplace.orders.domain.port.ProcessOrderUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Listens to order domain events and triggers processing for ORDER_CREATED events.
 */
@Component
public class OrderEventsListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventsListener.class);

    private final ObjectMapper objectMapper;
    private final ProcessOrderUseCase processOrderUseCase;

    public OrderEventsListener(ObjectMapper objectMapper, ProcessOrderUseCase processOrderUseCase) {
        this.objectMapper = objectMapper;
        this.processOrderUseCase = processOrderUseCase;
    }

    @KafkaListener(topics = "orders.events", groupId = "${app.kafka.consumer.group-id:orders-service-consumers}")
    public void onOrderEvent(@Payload String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = text(root, "eventType");
            String aggregateId = text(root, "aggregateId");
            if ("ORDER_CREATED".equals(eventType) && aggregateId != null) {
                log.info("[OrderEventsListener] Processing ORDER_CREATED event for order {}", aggregateId);
                try {
                    processOrderUseCase.processOrder(aggregateId);
                } catch (Exception e) {
                    log.error("Failed to process order {} from event: {}", aggregateId, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse order event message: {}", e.getMessage());
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return n != null && !n.isNull() ? n.asText() : null;
    }
}
