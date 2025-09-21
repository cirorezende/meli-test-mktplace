package br.com.ml.mktplace.orders.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Value Object representando um centro de distribuição
 */
public record DistributionCenter(
        @NotBlank(message = "Code is required")
        String code,
        
        @NotBlank(message = "Name is required")
        String name,
        
        @NotNull(message = "Address is required")
        Address address
) {
    
    public DistributionCenter {
        Objects.requireNonNull(code, "Code cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(address, "Address cannot be null");
    }
    
    /**
     * Obtém as coordenadas geográficas do centro de distribuição
     */
    public Address.Coordinates getCoordinates() {
        return address.coordinates();
    }
}