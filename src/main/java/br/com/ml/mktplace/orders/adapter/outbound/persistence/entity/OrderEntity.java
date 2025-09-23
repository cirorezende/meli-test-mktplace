package br.com.ml.mktplace.orders.adapter.outbound.persistence.entity;

import br.com.ml.mktplace.orders.domain.model.OrderStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * JPA Entity representing an Order in the database.
 * Maps to the 'orders' table with PostGIS geometry support for delivery coordinates.
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_customer_id", columnList = "customer_id"),
    @Index(name = "idx_orders_status", columnList = "status"),
    @Index(name = "idx_orders_created_at", columnList = "created_at")
})
public class OrderEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "customer_id", length = 26, nullable = false)
    private String customerId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "delivery_address", columnDefinition = "jsonb", nullable = false)
    private String deliveryAddressJson;

    @Column(name = "delivery_coordinates", columnDefinition = "geometry(Point,4326)")
    private Point deliveryCoordinates;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
        mappedBy = "order", 
        cascade = CascadeType.ALL, 
        orphanRemoval = true, 
        fetch = FetchType.LAZY
    )
    private List<OrderItemEntity> items;

    // Default constructor for JPA
    protected OrderEntity() {
    }

    public OrderEntity(String id, String customerId, String deliveryAddressJson, 
                      Point deliveryCoordinates, OrderStatus status, Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.deliveryAddressJson = deliveryAddressJson;
        this.deliveryCoordinates = deliveryCoordinates;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getDeliveryAddressJson() {
        return deliveryAddressJson;
    }

    public void setDeliveryAddressJson(String deliveryAddressJson) {
        this.deliveryAddressJson = deliveryAddressJson;
    }

    public Point getDeliveryCoordinates() {
        return deliveryCoordinates;
    }

    public void setDeliveryCoordinates(Point deliveryCoordinates) {
        this.deliveryCoordinates = deliveryCoordinates;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OrderItemEntity> getItems() {
        return items;
    }

    public void setItems(List<OrderItemEntity> items) {
        this.items = items;
        // Ensure bidirectional relationship
        if (items != null) {
            items.forEach(item -> item.setOrder(this));
        }
    }

    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItemEntity item) {
        items.remove(item);
        item.setOrder(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderEntity that = (OrderEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OrderEntity{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", itemsCount=" + (items != null ? items.size() : 0) +
                '}';
    }
}