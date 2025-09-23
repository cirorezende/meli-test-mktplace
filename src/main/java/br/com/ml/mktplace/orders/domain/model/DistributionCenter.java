package br.com.ml.mktplace.orders.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Value Object representando um centro de distribuição
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonIgnore // derived attribute - avoid adding extra JSON property in cache serialization
    public Address.Coordinates getCoordinates() {
        return address.coordinates();
    }
}