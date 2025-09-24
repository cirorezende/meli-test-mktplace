package br.com.ml.mktplace.orders.adapter.outbound.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

/**
 * JPA Entity representing an OrderItem in the database.
 * Maps to the 'order_items' table with foreign key relationship to Order.
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_items_order_id", columnList = "order_id"),
    @Index(name = "idx_order_items_item_id", columnList = "item_id")
})
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "item_id", length = 50, nullable = false)
    private String itemId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "assigned_distribution_center", length = 20)
    private String assignedDistributionCenter;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "available_distribution_centers", columnDefinition = "jsonb")
    private String availableDistributionCentersJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Default constructor for JPA
    protected OrderItemEntity() {
        this.createdAt = Instant.now();
    }

    public OrderItemEntity(OrderEntity order, String itemId, Integer quantity) {
        this();
        this.order = order;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public OrderItemEntity(OrderEntity order, String itemId, Integer quantity, 
                          String assignedDistributionCenter) {
        this(order, itemId, quantity);
        this.assignedDistributionCenter = assignedDistributionCenter;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getAssignedDistributionCenter() {
        return assignedDistributionCenter;
    }

    public void setAssignedDistributionCenter(String assignedDistributionCenter) {
        this.assignedDistributionCenter = assignedDistributionCenter;
    }

    public String getAvailableDistributionCentersJson() {
        return availableDistributionCentersJson;
    }

    public void setAvailableDistributionCentersJson(String availableDistributionCentersJson) {
        this.availableDistributionCentersJson = availableDistributionCentersJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemEntity that = (OrderItemEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OrderItemEntity{" +
                "id=" + id +
                ", itemId='" + itemId + '\'' +
                ", quantity=" + quantity +
                ", assignedDistributionCenter='" + assignedDistributionCenter + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}