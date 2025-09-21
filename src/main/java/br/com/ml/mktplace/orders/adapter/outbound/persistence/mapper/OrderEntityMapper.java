package br.com.ml.mktplace.orders.adapter.outbound.persistence.mapper;

import br.com.ml.mktplace.orders.adapter.outbound.persistence.entity.OrderEntity;
import br.com.ml.mktplace.orders.adapter.outbound.persistence.entity.OrderItemEntity;
import br.com.ml.mktplace.orders.domain.model.Address;
import br.com.ml.mktplace.orders.domain.model.DistributionCenter;
import br.com.ml.mktplace.orders.domain.model.Order;
import br.com.ml.mktplace.orders.domain.model.OrderItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class to convert between JPA entities and domain objects.
 * Handles JSON serialization/deserialization and PostGIS geometry conversion.
 */
@Component
public class OrderEntityMapper {

    private final ObjectMapper objectMapper;
    private final GeometryFactory geometryFactory;

    public OrderEntityMapper() {
        this.objectMapper = new ObjectMapper();
        this.geometryFactory = new GeometryFactory();
    }

    /**
     * Converts Order domain object to OrderEntity for persistence.
     */
    public OrderEntity toEntity(Order order) {
        try {
            String addressJson = serializeAddress(order.getDeliveryAddress());
            Point coordinates = createPoint(order.getDeliveryAddress().coordinates());
            
            OrderEntity entity = new OrderEntity(
                order.getId(),
                order.getCustomerId(),
                addressJson,
                coordinates,
                order.getStatus(),
                order.getCreatedAt() != null ? order.getCreatedAt() : Instant.now()
            );

            List<OrderItemEntity> itemEntities = order.getItems().stream()
                .map(item -> toOrderItemEntity(item, entity))
                .collect(Collectors.toList());
            
            entity.setItems(itemEntities);
            
            return entity;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize address to JSON", e);
        }
    }

    /**
     * Converts OrderEntity to Order domain object.
     */
    public Order toDomain(OrderEntity entity) {
        try {
            Address address = deserializeAddress(entity.getDeliveryAddressJson());
            
            List<OrderItem> items = entity.getItems().stream()
                .map(this::toOrderItemDomain)
                .collect(Collectors.toList());

            // Using the existing constructor with all parameters
            return new Order(
                entity.getId(),
                entity.getCustomerId(),
                items,
                address,
                entity.getStatus(),
                entity.getCreatedAt()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize address from JSON", e);
        }
    }

    /**
     * Converts OrderItem domain object to OrderItemEntity.
     */
    private OrderItemEntity toOrderItemEntity(OrderItem item, OrderEntity orderEntity) {
        String assignedDCCode = null;
        if (item.getAssignedDistributionCenter() != null) {
            assignedDCCode = item.getAssignedDistributionCenter().code();
        }
        
        return new OrderItemEntity(
            orderEntity,
            item.getItemId(),
            item.getQuantity(),
            assignedDCCode
        );
    }

    /**
     * Converts OrderItemEntity to OrderItem domain object.
     */
    private OrderItem toOrderItemDomain(OrderItemEntity entity) {
        OrderItem item = new OrderItem(entity.getItemId(), entity.getQuantity());
        
        // For now, we'll create a minimal DistributionCenter from just the code
        // In a full implementation, we would need to load the full DC data
        if (entity.getAssignedDistributionCenter() != null) {
            DistributionCenter dc = createMinimalDistributionCenter(entity.getAssignedDistributionCenter());
            item.assignDistributionCenter(dc);
        }
        
        return item;
    }

    /**
     * Creates a minimal DistributionCenter for reconstruction.
     * In a full implementation, this would load the complete DC data.
     */
    private DistributionCenter createMinimalDistributionCenter(String code) {
        // Create a minimal address for the DC (this is not ideal, but needed for reconstruction)
        Address minimalAddress = new Address(
            "Unknown",
            "Unknown", 
            "Unknown",
            "Unknown",
            "00000-000",
            new Address.Coordinates(BigDecimal.ZERO, BigDecimal.ZERO)
        );
        
        return new DistributionCenter(code, "DC " + code, minimalAddress);
    }

    /**
     * Serializes Address to JSON string.
     */
    private String serializeAddress(Address address) throws JsonProcessingException {
        return objectMapper.writeValueAsString(address);
    }

    /**
     * Deserializes JSON string to Address object.
     */
    private Address deserializeAddress(String json) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(json);
        
        String street = node.get("street").asText();
        String city = node.get("city").asText();
        String state = node.get("state").asText();
        String country = node.get("country").asText();
        String postalCode = node.get("postalCode").asText();
        
        JsonNode coordinatesNode = node.get("coordinates");
        Address.Coordinates coordinates = new Address.Coordinates(
            new BigDecimal(coordinatesNode.get("latitude").asText()),
            new BigDecimal(coordinatesNode.get("longitude").asText())
        );
        
        return new Address(street, city, state, country, postalCode, coordinates);
    }

    /**
     * Creates JTS Point from Address coordinates.
     */
    private Point createPoint(Address.Coordinates coordinates) {
        return geometryFactory.createPoint(new Coordinate(
            coordinates.longitude().doubleValue(),
            coordinates.latitude().doubleValue()
        ));
    }
}