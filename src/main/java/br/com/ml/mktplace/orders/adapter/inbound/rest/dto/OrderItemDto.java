package br.com.ml.mktplace.orders.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
    
    // Response-only field
    @Schema(description = "Available DCs for this item ordered by distance", readOnly = true)
    @JsonProperty("availableDistributionCenters")
    private java.util.List<NearbyDcDto> availableDistributionCenters;

    public static class NearbyDcDto {
        @JsonProperty("code")
        public String code;
        @JsonProperty("distanceKm")
        @Schema(description = "Distância em quilômetros com duas casas decimais", example = "12.34")
        public java.math.BigDecimal distanceKm;
        public NearbyDcDto() {}
        public NearbyDcDto(String code, double distanceKm) { 
            this.code = code; 
            this.distanceKm = java.math.BigDecimal.valueOf(distanceKm).setScale(2, java.math.RoundingMode.HALF_UP); 
        }
        public NearbyDcDto(String code, java.math.BigDecimal distanceKm) {
            this.code = code;
            this.distanceKm = distanceKm == null ? null : distanceKm.setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }
    
    // Constructors
    public OrderItemDto() {}
    
    public OrderItemDto(String itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }
    
    public OrderItemDto(String itemId, int quantity, java.util.List<NearbyDcDto> availableDistributionCenters) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.availableDistributionCenters = availableDistributionCenters;
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
    
    public java.util.List<NearbyDcDto> getAvailableDistributionCenters() {
        return availableDistributionCenters;
    }

    public void setAvailableDistributionCenters(java.util.List<NearbyDcDto> availableDistributionCenters) {
        this.availableDistributionCenters = availableDistributionCenters;
    }
}