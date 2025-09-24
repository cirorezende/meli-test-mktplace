package br.com.ml.mktplace.orders.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DistributionCenterTest {

    private Address createValidAddress() {
        var coordinates = new Address.Coordinates(
            new BigDecimal("-23.5505"), 
            new BigDecimal("-46.6333")
        );
        
        return new Address(
            "Rua da Consolação",
            "247",
            "São Paulo",
            "SP",
            "Brazil",
            "01301-000",
            coordinates
        );
    }

    @Test
    void shouldCreateValidDistributionCenter() {
        // Given
        var address = createValidAddress();
        
        // When
        var distributionCenter = new DistributionCenter("SP01", "Centro SP", address);
        
        // Then
        assertNotNull(distributionCenter);
        assertEquals("SP01", distributionCenter.code());
        assertEquals("Centro SP", distributionCenter.name());
        assertEquals(address, distributionCenter.address());
    }

    @Test
    void shouldThrowExceptionWhenCodeIsNull() {
        // Given
        var address = createValidAddress();
        
        // When & Then
        assertThrows(NullPointerException.class, () ->
            new DistributionCenter(null, "Centro SP", address)
        );
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        // Given
        var address = createValidAddress();
        
        // When & Then
        assertThrows(NullPointerException.class, () ->
            new DistributionCenter("SP01", null, address)
        );
    }

    @Test
    void shouldThrowExceptionWhenAddressIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () ->
            new DistributionCenter("SP01", "Centro SP", null)
        );
    }

    @Test
    void shouldReturnCoordinatesFromAddress() {
        // Given
        var coordinates = new Address.Coordinates(
            new BigDecimal("-23.5505"), 
            new BigDecimal("-46.6333")
        );
        var address = new Address(
            "Rua da Consolação",
            "247",
            "São Paulo",
            "SP",
            "Brazil",
            "01301-000",
            coordinates
        );
        var distributionCenter = new DistributionCenter("SP01", "Centro SP", address);
        
        // When
        var result = distributionCenter.getCoordinates();
        
        // Then
        assertEquals(coordinates, result);
        assertEquals(new BigDecimal("-23.5505"), result.latitude());
        assertEquals(new BigDecimal("-46.6333"), result.longitude());
    }

    @Test
    void shouldBeEqualWhenAllFieldsAreEqual() {
        // Given
        var address = createValidAddress();
        var dc1 = new DistributionCenter("SP01", "Centro SP", address);
        var dc2 = new DistributionCenter("SP01", "Centro SP", address);
        
        // When & Then
        assertEquals(dc1, dc2);
        assertEquals(dc1.hashCode(), dc2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenCodeIsDifferent() {
        // Given
        var address = createValidAddress();
        var dc1 = new DistributionCenter("SP01", "Centro SP", address);
        var dc2 = new DistributionCenter("SP02", "Centro SP", address);
        
        // When & Then
        assertNotEquals(dc1, dc2);
    }

    @Test
    void shouldNotBeEqualWhenNameIsDifferent() {
        // Given
        var address = createValidAddress();
        var dc1 = new DistributionCenter("SP01", "Centro SP", address);
        var dc2 = new DistributionCenter("SP01", "Centro RJ", address);
        
        // When & Then
        assertNotEquals(dc1, dc2);
    }

    @Test
    void shouldHaveValidStringRepresentation() {
        // Given
        var address = createValidAddress();
        var distributionCenter = new DistributionCenter("SP01", "Centro SP", address);
        
        // When
        var result = distributionCenter.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("SP01"));
        assertTrue(result.contains("Centro SP"));
    }
}