package br.com.ml.mktplace.orders.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

/**
 * Entidade representando um item dentro de um pedido
 */
public class OrderItem {
    
    @NotBlank(message = "Item ID is required")
    private final String itemId;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private final Integer quantity;
    
    private DistributionCenter assignedDistributionCenter;
    // Lista de CDs disponíveis para este item ordenada por distância (mais próximo -> mais distante)
    private java.util.List<NearbyDistributionCenter> availableDistributionCenters;
    
    public OrderItem(String itemId, Integer quantity) {
        this.itemId = Objects.requireNonNull(itemId, "Item ID cannot be null");
        this.quantity = Objects.requireNonNull(quantity, "Quantity cannot be null");
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
    
    /**
     * Atribui um centro de distribuição para este item
     */
    public void assignDistributionCenter(DistributionCenter distributionCenter) {
        this.assignedDistributionCenter = Objects.requireNonNull(
            distributionCenter, 
            "Distribution center cannot be null"
        );
    }

    /**
     * Define a lista de CDs disponíveis para este item, já ordenada pela distância.
     */
    public void setAvailableDistributionCenters(java.util.List<NearbyDistributionCenter> nearby) {
        this.availableDistributionCenters = java.util.List.copyOf(Objects.requireNonNull(nearby));
    }
    
    /**
     * Verifica se o item já foi atribuído a um centro de distribuição
     */
    public boolean isAssigned() {
        return assignedDistributionCenter != null;
    }
    
    // Getters
    public String getItemId() {
        return itemId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public DistributionCenter getAssignedDistributionCenter() {
        return assignedDistributionCenter;
    }

    public java.util.List<NearbyDistributionCenter> getAvailableDistributionCenters() {
        return availableDistributionCenters == null ? java.util.List.of() : java.util.List.copyOf(availableDistributionCenters);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(itemId, orderItem.itemId) && 
               Objects.equals(quantity, orderItem.quantity);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(itemId, quantity);
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "itemId='" + itemId + '\'' +
                ", quantity=" + quantity +
                ", assignedDistributionCenter=" + assignedDistributionCenter +
                ", availableDistributionCenters=" + (availableDistributionCenters == null ? 0 : availableDistributionCenters.size()) +
                '}';
    }
}