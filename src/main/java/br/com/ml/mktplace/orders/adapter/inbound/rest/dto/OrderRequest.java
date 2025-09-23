package br.com.ml.mktplace.orders.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for order creation requests
 */
@JsonIgnoreProperties(ignoreUnknown = true) // tolerate extra fields like legacy deliveryAddress
@Schema(name = "OrderRequest", description = "Order creation payload")
public class OrderRequest {
    
    @Schema(description = "Identifier of the customer placing the order", example = "CUST-12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Customer ID is required")
    @JsonProperty("customerId")
    private String customerId;
    
    @Schema(description = "Items being ordered (1-100)")
    @NotEmpty(message = "Order must have at least one item")
    @Size(min = 1, max = 100, message = "Order must have between 1 and 100 items")
    @Valid
    @JsonProperty("items")
    private List<OrderItemDto> items;
    
    // Constructors
    public OrderRequest() {}
    
    public OrderRequest(String customerId, List<OrderItemDto> items) {
        this.customerId = customerId;
        this.items = items;
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
}