package br.com.ml.mktplace.orders.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    void shouldCreateValidAddress() {
        // Given
        var coordinates = new Address.Coordinates(
            new BigDecimal("-23.5505"), 
            new BigDecimal("-46.6333")
        );
        
        // When
        var address = new Address(
            "Rua da Consolação, 247",
            "São Paulo",
            "SP",
            "Brazil",
            "01301-000",
            coordinates
        );
        
        // Then
        assertNotNull(address);
        assertEquals("Rua da Consolação, 247", address.street());
        assertEquals("São Paulo", address.city());
        assertEquals("SP", address.state());
        assertEquals("Brazil", address.country());
        assertEquals("01301-000", address.zipCode());
        assertEquals(coordinates, address.coordinates());
    }

    @Test
    void shouldThrowExceptionWhenStreetIsNull() {
        // Given
        var coordinates = new Address.Coordinates(
            new BigDecimal("-23.5505"), 
            new BigDecimal("-46.6333")
        );
        
        // When & Then
        assertThrows(NullPointerException.class, () ->
            new Address(null, "São Paulo", "SP", "Brazil", "01301-000", coordinates)
        );
    }

    @Test
    void shouldThrowExceptionWhenCityIsNull() {
        // Given
        var coordinates = new Address.Coordinates(
            new BigDecimal("-23.5505"), 
            new BigDecimal("-46.6333")
        );
        
        // When & Then
        assertThrows(NullPointerException.class, () ->
            new Address("Rua da Consolação, 247", null, "SP", "Brazil", "01301-000", coordinates)
        );
    }

    @Test
    void shouldThrowExceptionWhenStateIsNull() {
        // Given
        var coordinates = new Address.Coordinates(
            new BigDecimal("-23.5505"), 
            new BigDecimal("-46.6333")
        );
        
        // When & Then
        assertThrows(NullPointerException.class, () ->
            new Address("Rua da Consolação, 247", "São Paulo", null, "Brazil", "01301-000", coordinates)
        );
    }

    @Test
    void shouldThrowExceptionWhenCountryIsNull() {
        // Given
        var coordinates = new Address.Coordinates(
            new BigDecimal("-23.5505"), 
            new BigDecimal("-46.6333")
        );
        
        // When & Then
        assertThrows(NullPointerException.class, () ->
            new Address("Rua da Consolação, 247", "São Paulo", "SP", null, "01301-000", coordinates)
        );
    }

    @Test
    void shouldThrowExceptionWhenZipCodeIsNull() {
        // Given
        var coordinates = new Address.Coordinates(
            new BigDecimal("-23.5505"), 
            new BigDecimal("-46.6333")
        );
        
        // When & Then
        assertThrows(NullPointerException.class, () ->
            new Address("Rua da Consolação, 247", "São Paulo", "SP", "Brazil", null, coordinates)
        );
    }

    @Test
    void shouldThrowExceptionWhenCoordinatesIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () ->
            new Address("Rua da Consolação, 247", "São Paulo", "SP", "Brazil", "01301-000", null)
        );
    }

    @Test
    void shouldCreateValidCoordinates() {
        // When
        var coordinates = new Address.Coordinates(
            new BigDecimal("-23.5505"), 
            new BigDecimal("-46.6333")
        );
        
        // Then
        assertNotNull(coordinates);
        assertEquals(new BigDecimal("-23.5505"), coordinates.latitude());
        assertEquals(new BigDecimal("-46.6333"), coordinates.longitude());
    }

    @Test
    void shouldThrowExceptionWhenLatitudeIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () ->
            new Address.Coordinates(null, new BigDecimal("-46.6333"))
        );
    }

    @Test
    void shouldThrowExceptionWhenLongitudeIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () ->
            new Address.Coordinates(new BigDecimal("-23.5505"), null)
        );
    }

    @Test
    void shouldThrowExceptionWhenLatitudeIsTooHigh() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new Address.Coordinates(new BigDecimal("91"), new BigDecimal("-46.6333"))
        );
    }

    @Test
    void shouldThrowExceptionWhenLatitudeIsTooLow() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new Address.Coordinates(new BigDecimal("-91"), new BigDecimal("-46.6333"))
        );
    }

    @Test
    void shouldThrowExceptionWhenLongitudeIsTooHigh() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new Address.Coordinates(new BigDecimal("-23.5505"), new BigDecimal("181"))
        );
    }

    @Test
    void shouldThrowExceptionWhenLongitudeIsTooLow() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new Address.Coordinates(new BigDecimal("-23.5505"), new BigDecimal("-181"))
        );
    }

    @Test
    void shouldAcceptValidLatitudeBoundaries() {
        // When & Then
        assertDoesNotThrow(() -> {
            new Address.Coordinates(new BigDecimal("90"), new BigDecimal("0"));
            new Address.Coordinates(new BigDecimal("-90"), new BigDecimal("0"));
        });
    }

    @Test
    void shouldAcceptValidLongitudeBoundaries() {
        // When & Then
        assertDoesNotThrow(() -> {
            new Address.Coordinates(new BigDecimal("0"), new BigDecimal("180"));
            new Address.Coordinates(new BigDecimal("0"), new BigDecimal("-180"));
        });
    }
}