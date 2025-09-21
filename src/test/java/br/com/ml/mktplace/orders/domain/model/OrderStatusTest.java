package br.com.ml.mktplace.orders.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    void shouldHaveAllExpectedValues() {
        // When
        var values = OrderStatus.values();
        
        // Then
        assertEquals(4, values.length);
        
        // Verifica se todos os valores esperados existem
        assertTrue(containsValue(values, OrderStatus.RECEIVED));
        assertTrue(containsValue(values, OrderStatus.PROCESSING));
        assertTrue(containsValue(values, OrderStatus.PROCESSED));
        assertTrue(containsValue(values, OrderStatus.FAILED));
    }

    @Test
    void shouldReturnCorrectStringRepresentation() {
        // When & Then
        assertEquals("RECEIVED", OrderStatus.RECEIVED.toString());
        assertEquals("PROCESSING", OrderStatus.PROCESSING.toString());
        assertEquals("PROCESSED", OrderStatus.PROCESSED.toString());
        assertEquals("FAILED", OrderStatus.FAILED.toString());
    }

    @Test
    void shouldSupportValueOfMethod() {
        // When & Then
        assertEquals(OrderStatus.RECEIVED, OrderStatus.valueOf("RECEIVED"));
        assertEquals(OrderStatus.PROCESSING, OrderStatus.valueOf("PROCESSING"));
        assertEquals(OrderStatus.PROCESSED, OrderStatus.valueOf("PROCESSED"));
        assertEquals(OrderStatus.FAILED, OrderStatus.valueOf("FAILED"));
    }

    @Test
    void shouldThrowExceptionForInvalidValue() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            OrderStatus.valueOf("INVALID_STATUS")
        );
    }

    @Test
    void shouldBeComparable() {
        // When & Then - Enums são comparáveis por ordem de declaração
        assertTrue(OrderStatus.RECEIVED.compareTo(OrderStatus.PROCESSING) < 0);
        assertTrue(OrderStatus.PROCESSING.compareTo(OrderStatus.PROCESSED) < 0);
        assertTrue(OrderStatus.PROCESSED.compareTo(OrderStatus.FAILED) < 0);
        
        assertEquals(0, OrderStatus.RECEIVED.compareTo(OrderStatus.RECEIVED));
    }

    @Test
    void shouldHaveCorrectOrdinalValues() {
        // When & Then
        assertEquals(0, OrderStatus.RECEIVED.ordinal());
        assertEquals(1, OrderStatus.PROCESSING.ordinal());
        assertEquals(2, OrderStatus.PROCESSED.ordinal());
        assertEquals(3, OrderStatus.FAILED.ordinal());
    }

    private boolean containsValue(OrderStatus[] values, OrderStatus target) {
        for (OrderStatus value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}