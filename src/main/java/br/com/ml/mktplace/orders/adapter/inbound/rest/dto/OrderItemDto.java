package br.com.ml.mktplace.orders.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * DTO for order item information in API requests/responses
 */
public class OrderItemDto {
    
    @NotBlank(message = "Item ID is required")
    @JsonProperty("itemId")
    private String itemId;
    
    @Positive(message = "Quantity must be positive")
    @JsonProperty("quantity")
    private int quantity;
    
    // Response-only fields
    @JsonProperty("distributionCenterCode")
    private String distributionCenterCode;
    
    // Constructors
    public OrderItemDto() {}
    
    public OrderItemDto(String itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }
    
    public OrderItemDto(String itemId, int quantity, String distributionCenterCode) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.distributionCenterCode = distributionCenterCode;
    }
    
    // Getters and Setters
    public String getItemId() {
        return itemId;
    }
    
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public String getDistributionCenterCode() {
        return distributionCenterCode;
    }
    
    public void setDistributionCenterCode(String distributionCenterCode) {
        this.distributionCenterCode = distributionCenterCode;
    }
}