package br.com.ml.mktplace.orders.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExternalServiceExceptionTest {

    @Test
    void shouldCreateExceptionWithServiceNameAndMessage() {
        // Given
        String serviceName = "DistributionCenterAPI";
        String message = "Connection timeout";
        
        // When
        var exception = new ExternalServiceException(serviceName, message);
        
        // Then
        assertNotNull(exception);
        assertEquals(serviceName, exception.getServiceName());
        assertTrue(exception.getMessage().contains(serviceName));
        assertTrue(exception.getMessage().contains(message));
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithServiceNameMessageAndCause() {
        // Given
        String serviceName = "DistributionCenterAPI";
        String message = "Connection timeout";
        var cause = new RuntimeException("Network error");
        
        // When
        var exception = new ExternalServiceException(serviceName, message, cause);
        
        // Then
        assertNotNull(exception);
        assertEquals(serviceName, exception.getServiceName());
        assertTrue(exception.getMessage().contains(serviceName));
        assertTrue(exception.getMessage().contains(message));
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldHandleNullServiceName() {
        // Given
        String message = "Some error";
        
        // When
        var exception = new ExternalServiceException(null, message);
        
        // Then
        assertNotNull(exception);
        assertNull(exception.getServiceName());
        assertTrue(exception.getMessage().contains("null"));
        assertTrue(exception.getMessage().contains(message));
    }

    @Test
    void shouldHandleNullMessage() {
        // Given
        String serviceName = "TestService";
        
        // When
        var exception = new ExternalServiceException(serviceName, null);
        
        // Then
        assertNotNull(exception);
        assertEquals(serviceName, exception.getServiceName());
        assertTrue(exception.getMessage().contains(serviceName));
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    void shouldHandleEmptyServiceName() {
        // Given
        String serviceName = "";
        String message = "Error occurred";
        
        // When
        var exception = new ExternalServiceException(serviceName, message);
        
        // Then
        assertNotNull(exception);
        assertEquals(serviceName, exception.getServiceName());
        assertTrue(exception.getMessage().contains("External service error"));
        assertTrue(exception.getMessage().contains(message));
    }

    @Test
    void shouldHandleEmptyMessage() {
        // Given
        String serviceName = "TestService";
        String message = "";
        
        // When
        var exception = new ExternalServiceException(serviceName, message);
        
        // Then
        assertNotNull(exception);
        assertEquals(serviceName, exception.getServiceName());
        assertTrue(exception.getMessage().contains(serviceName));
    }

    @Test
    void shouldBeRuntimeException() {
        // Given
        String serviceName = "TestService";
        String message = "Test error";
        
        // When
        var exception = new ExternalServiceException(serviceName, message);
        
        // Then
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void shouldHaveConsistentMessageFormat() {
        // Given
        String serviceName = "CacheService";
        String message = "Redis connection failed";
        
        // When
        var exception = new ExternalServiceException(serviceName, message);
        
        // Then
        assertEquals("External service error [" + serviceName + "]: " + message, exception.getMessage());
    }

    @Test
    void shouldPreserveCauseChain() {
        // Given
        String serviceName = "TestService";
        String message = "Error message";
        var rootCause = new IllegalStateException("Root cause");
        var intermediateCause = new RuntimeException("Intermediate", rootCause);
        
        // When
        var exception = new ExternalServiceException(serviceName, message, intermediateCause);
        
        // Then
        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }

    @Test
    void shouldWorkWithCommonServiceNames() {
        // Test with typical service names
        String[] serviceNames = {
            "DistributionCenterAPI",
            "CacheService", 
            "EventPublisher",
            "DatabaseService",
            "external-api"
        };
        
        for (String serviceName : serviceNames) {
            // When
            var exception = new ExternalServiceException(serviceName, "Test error");
            
            // Then
            assertEquals(serviceName, exception.getServiceName());
            assertTrue(exception.getMessage().contains(serviceName));
        }
    }
}