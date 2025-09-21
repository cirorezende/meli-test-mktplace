package br.com.ml.mktplace.orders.domain.model;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidade principal representando um pedido no sistema
 */
public class Order {
    
    public static final int MAX_ITEMS = 100;
    
    @NotNull(message = "Order ID is required")
    private final String id;
    
    @NotNull(message = "Customer ID is required")
    private final String customerId;
    
    @NotEmpty(message = "Order must have at least one item")
    @Size(max = MAX_ITEMS, message = "Order cannot have more than " + MAX_ITEMS + " items")
    @Valid
    private final List<OrderItem> items;
    
    @NotNull(message = "Delivery address is required")
    @Valid
    private final Address deliveryAddress;
    
    @NotNull(message = "Order status is required")
    private OrderStatus status;
    
    @NotNull(message = "Created timestamp is required")
    private final Instant createdAt;
    
    /**
     * Construtor para criar um novo pedido
     */
    public Order(String customerId, List<OrderItem> items, Address deliveryAddress) {
        this.id = UlidCreator.getUlid().toString();
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.items = validateAndCopyItems(items);
        this.deliveryAddress = Objects.requireNonNull(deliveryAddress, "Delivery address cannot be null");
        this.status = OrderStatus.RECEIVED; // Status inicial sempre RECEIVED
        this.createdAt = Instant.now();
    }
    
    /**
     * Construtor para criar um novo pedido (para compatibilidade com testes)
     */
    public Order(List<OrderItem> items, Address deliveryAddress) {
        this("CUSTOMER-DEFAULT", items, deliveryAddress);
    }
    
    /**
     * Construtor para reconstrução (ex: vindo do banco de dados)
     */
    public Order(String id, String customerId, List<OrderItem> items, Address deliveryAddress, 
                 OrderStatus status, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "Order ID cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.items = validateAndCopyItems(items);
        this.deliveryAddress = Objects.requireNonNull(deliveryAddress, "Delivery address cannot be null");
        this.status = Objects.requireNonNull(status, "Order status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created timestamp cannot be null");
    }
    
    /**
     * Construtor para reconstrução (para compatibilidade com testes)
     */
    public Order(String id, List<OrderItem> items, Address deliveryAddress, 
                 OrderStatus status, Instant createdAt) {
        this(id, "CUSTOMER-DEFAULT", items, deliveryAddress, status, createdAt);
    }
    
    /**
     * Valida e cria uma cópia defensiva da lista de itens
     */
    private List<OrderItem> validateAndCopyItems(List<OrderItem> items) {
        Objects.requireNonNull(items, "Items list cannot be null");
        
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        
        if (items.size() > MAX_ITEMS) {
            throw new IllegalArgumentException("Order cannot have more than " + MAX_ITEMS + " items");
        }
        
        // Cria uma cópia defensiva
        return new ArrayList<>(items);
    }
    
    /**
     * Altera o status do pedido
     */
    public void changeStatus(OrderStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "Status cannot be null");
    }
    
    /**
     * Verifica se todos os itens foram atribuídos a centros de distribuição
     */
    public boolean areAllItemsAssigned() {
        return items.stream().allMatch(OrderItem::isAssigned);
    }
    
    /**
     * Obtém o número total de itens no pedido (considerando quantidades)
     */
    public int getTotalItemsCount() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }
    
    /**
     * Verifica se o pedido está em um estado final
     */
    public boolean isInFinalState() {
        return status == OrderStatus.PROCESSED || status == OrderStatus.FAILED;
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
    
    public Address getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", itemsCount=" + items.size() +
                ", totalItemsCount=" + getTotalItemsCount() +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}