package br.com.ml.mktplace.orders.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    private DistributionCenter createValidDistributionCenter() {
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
        
        return new DistributionCenter("SP01", "Centro SP", address);
    }

    @Test
    void shouldCreateValidOrderItem() {
        // When
        var orderItem = new OrderItem("ITEM001", 5);
        
        // Then
        assertNotNull(orderItem);
        assertEquals("ITEM001", orderItem.getItemId());
        assertEquals(5, orderItem.getQuantity());
        assertNull(orderItem.getAssignedDistributionCenter());
        assertFalse(orderItem.isAssigned());
    }

    @Test
    void shouldThrowExceptionWhenItemIdIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () ->
            new OrderItem(null, 5)
        );
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () ->
            new OrderItem("ITEM001", null)
        );
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsZero() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new OrderItem("ITEM001", 0)
        );
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsNegative() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new OrderItem("ITEM001", -1)
        );
    }

    @Test
    void shouldAssignDistributionCenter() {
        // Given
        var orderItem = new OrderItem("ITEM001", 5);
        var distributionCenter = createValidDistributionCenter();
        
        // When
        orderItem.assignDistributionCenter(distributionCenter);
        
        // Then
        assertEquals(distributionCenter, orderItem.getAssignedDistributionCenter());
        assertTrue(orderItem.isAssigned());
    }

    @Test
    void shouldThrowExceptionWhenAssigningNullDistributionCenter() {
        // Given
        var orderItem = new OrderItem("ITEM001", 5);
        
        // When & Then
        assertThrows(NullPointerException.class, () ->
            orderItem.assignDistributionCenter(null)
        );
    }

    @Test
    void shouldOverrideDistributionCenterAssignment() {
        // Given
        var orderItem = new OrderItem("ITEM001", 5);
        var dc1 = createValidDistributionCenter();
        var coordinates2 = new Address.Coordinates(
            new BigDecimal("-22.9068"), 
            new BigDecimal("-43.1729")
        );
        var address2 = new Address(
            "Rua das Flores",
            "123",
            "Rio de Janeiro",
            "RJ",
            "Brazil",
            "20040-000",
            coordinates2
        );
        var dc2 = new DistributionCenter("RJ01", "Centro RJ", address2);
        
        // When
        orderItem.assignDistributionCenter(dc1);
        orderItem.assignDistributionCenter(dc2);
        
        // Then
        assertEquals(dc2, orderItem.getAssignedDistributionCenter());
        assertTrue(orderItem.isAssigned());
    }

    @Test
    void shouldBeEqualWhenItemIdAndQuantityAreEqual() {
        // Given
        var item1 = new OrderItem("ITEM001", 5);
        var item2 = new OrderItem("ITEM001", 5);
        
        // When & Then
        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenItemIdIsDifferent() {
        // Given
        var item1 = new OrderItem("ITEM001", 5);
        var item2 = new OrderItem("ITEM002", 5);
        
        // When & Then
        assertNotEquals(item1, item2);
    }

    @Test
    void shouldNotBeEqualWhenQuantityIsDifferent() {
        // Given
        var item1 = new OrderItem("ITEM001", 5);
        var item2 = new OrderItem("ITEM001", 3);
        
        // When & Then
        assertNotEquals(item1, item2);
    }

    @Test
    void shouldEqualityIgnoreDistributionCenterAssignment() {
        // Given
        var item1 = new OrderItem("ITEM001", 5);
        var item2 = new OrderItem("ITEM001", 5);
        var distributionCenter = createValidDistributionCenter();
        
        item1.assignDistributionCenter(distributionCenter);
        // item2 não tem centro de distribuição atribuído
        
        // When & Then
        assertEquals(item1, item2); // Igualdade baseada apenas em itemId e quantity
    }

    @Test
    void shouldHaveValidStringRepresentation() {
        // Given
        var orderItem = new OrderItem("ITEM001", 5);
        
        // When
        var result = orderItem.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("ITEM001"));
        assertTrue(result.contains("5"));
    }

    @Test
    void shouldIncludeDistributionCenterInStringRepresentation() {
        // Given
        var orderItem = new OrderItem("ITEM001", 5);
        var distributionCenter = createValidDistributionCenter();
        orderItem.assignDistributionCenter(distributionCenter);
        
        // When
        var result = orderItem.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("ITEM001"));
        assertTrue(result.contains("5"));
        assertTrue(result.contains("SP01"));
    }
}