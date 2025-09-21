package br.com.ml.mktplace.orders.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * DTO for order response in API calls
 */
public class OrderResponse {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("customerId")
    private String customerId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("items")
    private List<OrderItemDto> items;
    
    @JsonProperty("deliveryAddress")
    private AddressDto deliveryAddress;
    
    @JsonProperty("createdAt")
    private Instant createdAt;
    
    @JsonProperty("updatedAt")
    private Instant updatedAt;
    
    @JsonProperty("totalItemsCount")
    private int totalItemsCount;
    
    // Constructors
    public OrderResponse() {}
    
    public OrderResponse(String id, String customerId, String status, List<OrderItemDto> items, 
                        AddressDto deliveryAddress, Instant createdAt, Instant updatedAt, 
                        int totalItemsCount) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.items = items;
        this.deliveryAddress = deliveryAddress;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.totalItemsCount = totalItemsCount;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public List<OrderItemDto> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }
    
    public AddressDto getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public void setDeliveryAddress(AddressDto deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
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
    
    public int getTotalItemsCount() {
        return totalItemsCount;
    }
    
    public void setTotalItemsCount(int totalItemsCount) {
        this.totalItemsCount = totalItemsCount;
    }
}