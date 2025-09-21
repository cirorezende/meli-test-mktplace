package br.com.ml.mktplace.orders.adapter.outbound.messaging;

import br.com.ml.mktplace.orders.domain.model.Order;
import br.com.ml.mktplace.orders.domain.port.EventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Event publisher implementation that logs events.
 * In production, this would integrate with Kafka or another message broker.
 */
@Component
public class KafkaEventPublisher implements EventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaEventPublisher.class);
    
    private final ObjectMapper objectMapper;
    private final String topicPrefix;
    
    @Autowired
    public KafkaEventPublisher(
            ObjectMapper objectMapper,
            @Value("${app.events.topic-prefix:mktplace}") String topicPrefix) {
        this.objectMapper = objectMapper;
        this.topicPrefix = topicPrefix;
    }
    
    @Override
    public void publishOrderProcessed(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        Map<String, Object> eventData = createEventData("ORDER_PROCESSED", order);
        publishEvent("order.processed", eventData);
    }
    
    @Override
    public void publishOrderFailed(Order order, String reason, Throwable error) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (reason == null) {
            throw new IllegalArgumentException("Reason cannot be null");
        }
        
        Map<String, Object> eventData = createEventData("ORDER_FAILED", order);
        eventData.put("failureReason", reason);
        if (error != null) {
            eventData.put("errorMessage", error.getMessage());
            eventData.put("errorClass", error.getClass().getSimpleName());
        }
        
        publishEvent("order.failed", eventData);
    }
    
    @Override
    public void publishOrderFailed(Order order, String reason) {
        publishOrderFailed(order, reason, null);
    }
    
    @Override
    public void publishOrderCreated(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        Map<String, Object> eventData = createEventData("ORDER_CREATED", order);
        publishEvent("order.created", eventData);
    }
    
    @Override
    public void publishDomainEvent(String eventType, Object eventData) {
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("Event type cannot be null or empty");
        }
        
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("eventData", eventData);
        event.put("timestamp", Instant.now().toString());
        event.put("aggregateType", "Order");
        
        String topic = topicPrefix + "." + eventType.toLowerCase().replace("_", ".");
        publishEvent(topic, event);
    }
    
    private Map<String, Object> createEventData(String eventType, Order order) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", eventType);
        eventData.put("aggregateId", order.getId());
        eventData.put("aggregateType", "Order");
        eventData.put("customerId", order.getCustomerId());
        eventData.put("totalItemsCount", order.getTotalItemsCount());
        eventData.put("status", order.getStatus().name());
        eventData.put("timestamp", Instant.now().toString());
        eventData.put("version", 1);
        
        return eventData;
    }
    
    private void publishEvent(String topic, Map<String, Object> eventData) {
        try {
            String payload = objectMapper.writeValueAsString(eventData);
            
            logger.info("Publishing event to topic: {} - Event: {}", topic, eventData.get("eventType"));
            logger.debug("Event payload: {}", payload);
            
            // In production, this would send to Kafka:
            // kafkaTemplate.send(topic, eventData.get("aggregateId").toString(), payload);
            
            // For now, we just log the event
            logger.info("Event published successfully: {}", eventData.get("eventType"));
            
        } catch (Exception e) {
            logger.error("Failed to publish event: {}", eventData.get("eventType"), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}