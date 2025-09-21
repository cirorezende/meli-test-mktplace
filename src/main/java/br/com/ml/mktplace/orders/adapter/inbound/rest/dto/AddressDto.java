package br.com.ml.mktplace.orders.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * DTO for address information in API requests/responses
 */
public class AddressDto {
    
    @NotBlank(message = "Street is required")
    @JsonProperty("street")
    private String street;
    
    @NotBlank(message = "City is required")
    @JsonProperty("city")
    private String city;
    
    @NotBlank(message = "State is required")
    @JsonProperty("state")
    private String state;
    
    @NotBlank(message = "Country is required")
    @JsonProperty("country")
    private String country;
    
    @NotBlank(message = "ZIP code is required")
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "Invalid ZIP code format. Use XXXXX-XXX or XXXXXXXX")
    @JsonProperty("zipCode")
    private String zipCode;
    
    @Valid
    @NotNull(message = "Coordinates are required")
    @JsonProperty("coordinates")
    private CoordinatesDto coordinates;
    
    // Constructors
    public AddressDto() {}
    
    public AddressDto(String street, String city, String state, String country, 
                     String zipCode, CoordinatesDto coordinates) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zipCode = zipCode;
        this.coordinates = coordinates;
    }
    
    // Getters and Setters
    public String getStreet() {
        return street;
    }
    
    public void setStreet(String street) {
        this.street = street;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getZipCode() {
        return zipCode;
    }
    
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    public CoordinatesDto getCoordinates() {
        return coordinates;
    }
    
    public void setCoordinates(CoordinatesDto coordinates) {
        this.coordinates = coordinates;
    }
    
    public static class CoordinatesDto {
        
        @NotNull(message = "Latitude is required")
        @JsonProperty("latitude")
        private BigDecimal latitude;
        
        @NotNull(message = "Longitude is required")
        @JsonProperty("longitude")
        private BigDecimal longitude;
        
        // Constructors
        public CoordinatesDto() {}
        
        public CoordinatesDto(BigDecimal latitude, BigDecimal longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        
        // Getters and Setters
        public BigDecimal getLatitude() {
            return latitude;
        }
        
        public void setLatitude(BigDecimal latitude) {
            this.latitude = latitude;
        }
        
        public BigDecimal getLongitude() {
            return longitude;
        }
        
        public void setLongitude(BigDecimal longitude) {
            this.longitude = longitude;
        }
    }
}