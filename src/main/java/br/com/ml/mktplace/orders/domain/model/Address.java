package br.com.ml.mktplace.orders.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representando um endereço de entrega
 */
public record Address(
        @NotBlank(message = "Street is required")
        String street,

    @NotBlank(message = "Number is required")
    String number,
        
        @NotBlank(message = "City is required") 
        String city,
        
        @NotBlank(message = "State is required")
        String state,
        
        @NotBlank(message = "Country is required")
        String country,
        
        @NotBlank(message = "Zip code is required")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "Invalid zip code format")
        String zipCode,
        
        @NotNull(message = "Coordinates are required")
        Coordinates coordinates
) {
    
    public Address {
        Objects.requireNonNull(street, "Street cannot be null");
        Objects.requireNonNull(number, "Number cannot be null");
        Objects.requireNonNull(city, "City cannot be null");
        Objects.requireNonNull(state, "State cannot be null");
        Objects.requireNonNull(country, "Country cannot be null");
        Objects.requireNonNull(zipCode, "Zip code cannot be null");
        Objects.requireNonNull(coordinates, "Coordinates cannot be null");
    }
    
    /**
     * Coordenadas geográficas
     */
    public record Coordinates(
            @NotNull(message = "Latitude is required")
            BigDecimal latitude,
            
            @NotNull(message = "Longitude is required") 
            BigDecimal longitude
    ) {
        
        public Coordinates {
            Objects.requireNonNull(latitude, "Latitude cannot be null");
            Objects.requireNonNull(longitude, "Longitude cannot be null");
            
            if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 || 
                latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
                throw new IllegalArgumentException("Latitude must be between -90 and 90");
            }
            
            if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 || 
                longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
                throw new IllegalArgumentException("Longitude must be between -180 and 180");
            }
        }
    }
}