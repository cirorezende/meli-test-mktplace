package br.com.ml.mktplace.orders.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for order creation requests
 */
public class OrderRequest {
    
    @NotBlank(message = "Customer ID is required")
    @JsonProperty("customerId")
    private String customerId;
    
    @NotEmpty(message = "Order must have at least one item")
    @Size(min = 1, max = 100, message = "Order must have between 1 and 100 items")
    @Valid
    @JsonProperty("items")
    private List<OrderItemDto> items;
    
    @NotNull(message = "Delivery address is required")
    @Valid
    @JsonProperty("deliveryAddress")
    private AddressDto deliveryAddress;
    
    // Constructors
    public OrderRequest() {}
    
    public OrderRequest(String customerId, List<OrderItemDto> items, AddressDto deliveryAddress) {
        this.customerId = customerId;
        this.items = items;
        this.deliveryAddress = deliveryAddress;
    }
    
    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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
}