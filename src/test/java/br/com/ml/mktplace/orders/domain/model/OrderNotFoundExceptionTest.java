package br.com.ml.mktplace.orders.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithOrderId() {
        // Given
        String orderId = "01234567890123456789012345";
        
        // When
        var exception = new OrderNotFoundException(orderId);
        
        // Then
        assertNotNull(exception);
        assertEquals(orderId, exception.getOrderId());
        assertTrue(exception.getMessage().contains(orderId));
        assertTrue(exception.getMessage().contains("Order not found"));
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithOrderIdAndCause() {
        // Given
        String orderId = "01234567890123456789012345";
        var cause = new RuntimeException("Database connection failed");
        
        // When
        var exception = new OrderNotFoundException(orderId, cause);
        
        // Then
        assertNotNull(exception);
        assertEquals(orderId, exception.getOrderId());
        assertTrue(exception.getMessage().contains(orderId));
        assertTrue(exception.getMessage().contains("Order not found"));
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldHandleNullOrderId() {
        // When
        var exception = new OrderNotFoundException(null);
        
        // Then
        assertNotNull(exception);
        assertNull(exception.getOrderId());
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    void shouldHandleEmptyOrderId() {
        // Given
        String emptyOrderId = "";
        
        // When
        var exception = new OrderNotFoundException(emptyOrderId);
        
        // Then
        assertNotNull(exception);
        assertEquals(emptyOrderId, exception.getOrderId());
        assertTrue(exception.getMessage().contains("Order not found"));
    }

    @Test
    void shouldBeRuntimeException() {
        // Given
        String orderId = "01234567890123456789012345";
        
        // When
        var exception = new OrderNotFoundException(orderId);
        
        // Then
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void shouldHaveConsistentMessage() {
        // Given
        String orderId = "test-order-123";
        
        // When
        var exception = new OrderNotFoundException(orderId);
        
        // Then
        assertEquals("Order not found with ID: " + orderId, exception.getMessage());
    }

    @Test
    void shouldPreserveCauseChain() {
        // Given
        String orderId = "test-order";
        var rootCause = new IllegalStateException("Root cause");
        var intermediateCause = new RuntimeException("Intermediate", rootCause);
        
        // When
        var exception = new OrderNotFoundException(orderId, intermediateCause);
        
        // Then
        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }
}