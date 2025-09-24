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
    
    // Response-only fields
    @Schema(description = "DC code selected during processing; not required on requests", readOnly = true)
    @JsonProperty("distributionCenterCode")
    private String distributionCenterCode;

    @Schema(description = "Available DCs for this item ordered by distance (most distant first as per requirement)", readOnly = true)
    @JsonProperty("availableDistributionCenters")
    private java.util.List<NearbyDcDto> availableDistributionCenters;

    public static class NearbyDcDto {
        @JsonProperty("code")
        public String code;
        @JsonProperty("distanceKm")
        public double distanceKm;
        public NearbyDcDto() {}
        public NearbyDcDto(String code, double distanceKm) { this.code = code; this.distanceKm = distanceKm; }
    }
    
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
    public OrderItemDto(String itemId, int quantity, String distributionCenterCode, java.util.List<NearbyDcDto> availableDistributionCenters) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.distributionCenterCode = distributionCenterCode;
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
    
    public String getDistributionCenterCode() {
        return distributionCenterCode;
    }
    
    public void setDistributionCenterCode(String distributionCenterCode) {
        this.distributionCenterCode = distributionCenterCode;
    }

    public java.util.List<NearbyDcDto> getAvailableDistributionCenters() {
        return availableDistributionCenters;
    }

    public void setAvailableDistributionCenters(java.util.List<NearbyDcDto> availableDistributionCenters) {
        this.availableDistributionCenters = availableDistributionCenters;
    }
}