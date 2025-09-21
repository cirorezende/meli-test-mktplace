package br.com.ml.mktplace.orders.domain.port;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProcessOrderExceptionTest {

    @Test
    void shouldCreateExceptionWithOrderIdAndMessage() {
        // Given
        String orderId = "01234567890123456789012345";
        String message = "Failed to process items";
        
        // When
        var exception = new ProcessOrderUseCase.ProcessOrderException(orderId, message);
        
        // Then
        assertNotNull(exception);
        assertEquals(orderId, exception.getOrderId());
        assertTrue(exception.getMessage().contains(orderId));
        assertTrue(exception.getMessage().contains(message));
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithOrderIdMessageAndCause() {
        // Given
        String orderId = "01234567890123456789012345";
        String message = "Distribution center service unavailable";
        var cause = new RuntimeException("Network timeout");
        
        // When
        var exception = new ProcessOrderUseCase.ProcessOrderException(orderId, message, cause);
        
        // Then
        assertNotNull(exception);
        assertEquals(orderId, exception.getOrderId());
        assertTrue(exception.getMessage().contains(orderId));
        assertTrue(exception.getMessage().contains(message));
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldHandleNullOrderId() {
        // Given
        String message = "Processing failed";
        
        // When
        var exception = new ProcessOrderUseCase.ProcessOrderException(null, message);
        
        // Then
        assertNotNull(exception);
        assertNull(exception.getOrderId());
        assertTrue(exception.getMessage().contains("null"));
        assertTrue(exception.getMessage().contains(message));
    }

    @Test
    void shouldHandleNullMessage() {
        // Given
        String orderId = "test-order-123";
        
        // When
        var exception = new ProcessOrderUseCase.ProcessOrderException(orderId, null);
        
        // Then
        assertNotNull(exception);
        assertEquals(orderId, exception.getOrderId());
        assertTrue(exception.getMessage().contains(orderId));
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    void shouldHandleEmptyOrderId() {
        // Given
        String orderId = "";
        String message = "Processing failed";
        
        // When
        var exception = new ProcessOrderUseCase.ProcessOrderException(orderId, message);
        
        // Then
        assertNotNull(exception);
        assertEquals(orderId, exception.getOrderId());
        assertTrue(exception.getMessage().contains(message));
    }

    @Test
    void shouldHandleEmptyMessage() {
        // Given
        String orderId = "test-order-123";
        String message = "";
        
        // When
        var exception = new ProcessOrderUseCase.ProcessOrderException(orderId, message);
        
        // Then
        assertNotNull(exception);
        assertEquals(orderId, exception.getOrderId());
        assertTrue(exception.getMessage().contains(orderId));
    }

    @Test
    void shouldBeRuntimeException() {
        // Given
        String orderId = "test-order";
        String message = "Test failure";
        
        // When
        var exception = new ProcessOrderUseCase.ProcessOrderException(orderId, message);
        
        // Then
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void shouldHaveConsistentMessageFormat() {
        // Given
        String orderId = "order-123";
        String message = "Algorithm failed";
        
        // When
        var exception = new ProcessOrderUseCase.ProcessOrderException(orderId, message);
        
        // Then
        assertEquals("Failed to process order [" + orderId + "]: " + message, exception.getMessage());
    }

    @Test
    void shouldPreserveCauseChain() {
        // Given
        String orderId = "test-order";
        String message = "Processing error";
        var rootCause = new IllegalStateException("Root cause");
        var intermediateCause = new RuntimeException("Intermediate", rootCause);
        
        // When
        var exception = new ProcessOrderUseCase.ProcessOrderException(orderId, message, intermediateCause);
        
        // Then
        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }

    @Test
    void shouldWorkWithTypicalProcessingErrors() {
        // Test with typical processing error scenarios
        String[][] testCases = {
            {"order-001", "No distribution centers available"},
            {"order-002", "External API timeout"},
            {"order-003", "Invalid item configuration"},
            {"order-004", "Cache service unavailable"},
            {"order-005", "Algorithm execution failed"}
        };
        
        for (String[] testCase : testCases) {
            // When
            var exception = new ProcessOrderUseCase.ProcessOrderException(testCase[0], testCase[1]);
            
            // Then
            assertEquals(testCase[0], exception.getOrderId());
            assertTrue(exception.getMessage().contains(testCase[0]));
            assertTrue(exception.getMessage().contains(testCase[1]));
        }
    }

    @Test
    void shouldBeUsableInExceptionChain() {
        // Given
        String orderId = "chain-test";
        String message = "Chain test error";
        
        // When & Then - Should be usable in try-catch and throw chains
        assertDoesNotThrow(() -> {
            try {
                throw new ProcessOrderUseCase.ProcessOrderException(orderId, message);
            } catch (ProcessOrderUseCase.ProcessOrderException e) {
                assertEquals(orderId, e.getOrderId());
                assertEquals("Failed to process order [" + orderId + "]: " + message, e.getMessage());
            }
        });
    }
}