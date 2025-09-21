package br.com.ml.mktplace.orders.domain.port;

import br.com.ml.mktplace.orders.domain.model.Address;
import br.com.ml.mktplace.orders.domain.model.Order;
import br.com.ml.mktplace.orders.domain.model.OrderItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProcessOrderUseCaseTest {

    private Order createValidOrder() {
        var coordinates = new Address.Coordinates(
            new BigDecimal("-23.5505"), 
            new BigDecimal("-46.6333")
        );
        
        var address = new Address(
            "Rua da Consolação, 247",
            "São Paulo",
            "SP",
            "Brazil",
            "01301-000",
            coordinates
        );
        
        var items = List.of(
            new OrderItem("ITEM001", 2),
            new OrderItem("ITEM002", 1)
        );
        
        return new Order(items, address);
    }

    @Test
    void shouldCreateValidProcessOrderResult() {
        // Given
        var order = createValidOrder();
        boolean success = true;
        String message = "Order processed successfully";
        int itemsProcessed = 2;
        int itemsFailed = 0;
        
        // When
        var result = new ProcessOrderUseCase.ProcessOrderResult(
            order, success, message, itemsProcessed, itemsFailed
        );
        
        // Then
        assertNotNull(result);
        assertEquals(order, result.order());
        assertTrue(result.success());
        assertEquals(message, result.message());
        assertEquals(itemsProcessed, result.itemsProcessed());
        assertEquals(itemsFailed, result.itemsFailed());
    }

    @Test
    void shouldThrowExceptionWhenOrderIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new ProcessOrderUseCase.ProcessOrderResult(null, true, "message", 1, 0)
        );
    }

    @Test
    void shouldReturnFalseWhenNoFailures() {
        // Given
        var order = createValidOrder();
        var result = new ProcessOrderUseCase.ProcessOrderResult(
            order, true, "Success", 2, 0
        );
        
        // When & Then
        assertFalse(result.hasFailures());
    }

    @Test
    void shouldReturnTrueWhenHasFailures() {
        // Given
        var order = createValidOrder();
        var result = new ProcessOrderUseCase.ProcessOrderResult(
            order, false, "Partial failure", 1, 1
        );
        
        // When & Then
        assertTrue(result.hasFailures());
    }

    @Test
    void shouldReturnTrueForPartialSuccess() {
        // Given
        var order = createValidOrder();
        var result = new ProcessOrderUseCase.ProcessOrderResult(
            order, true, "Partial success", 1, 1
        );
        
        // When & Then
        assertTrue(result.isPartialSuccess());
        assertTrue(result.hasFailures());
    }

    @Test
    void shouldReturnFalseForPartialSuccessWhenNoFailures() {
        // Given
        var order = createValidOrder();
        var result = new ProcessOrderUseCase.ProcessOrderResult(
            order, true, "Complete success", 2, 0
        );
        
        // When & Then
        assertFalse(result.isPartialSuccess());
        assertFalse(result.hasFailures());
    }

    @Test
    void shouldReturnFalseForPartialSuccessWhenNotSuccessful() {
        // Given
        var order = createValidOrder();
        var result = new ProcessOrderUseCase.ProcessOrderResult(
            order, false, "Complete failure", 0, 2
        );
        
        // When & Then
        assertFalse(result.isPartialSuccess());
        assertTrue(result.hasFailures());
    }

    @Test
    void shouldHandleNullMessage() {
        // Given
        var order = createValidOrder();
        
        // When & Then
        assertDoesNotThrow(() ->
            new ProcessOrderUseCase.ProcessOrderResult(order, true, null, 2, 0)
        );
    }

    @Test
    void shouldHandleEmptyMessage() {
        // Given
        var order = createValidOrder();
        
        // When
        var result = new ProcessOrderUseCase.ProcessOrderResult(order, true, "", 2, 0);
        
        // Then
        assertEquals("", result.message());
    }

    @Test
    void shouldHandleZeroItemsProcessed() {
        // Given
        var order = createValidOrder();
        
        // When
        var result = new ProcessOrderUseCase.ProcessOrderResult(order, false, "Failed", 0, 2);
        
        // Then
        assertEquals(0, result.itemsProcessed());
        assertEquals(2, result.itemsFailed());
    }

    @Test
    void shouldHandleNegativeValues() {
        // Given
        var order = createValidOrder();
        
        // When & Then - Should accept negative values (could represent error states)
        assertDoesNotThrow(() ->
            new ProcessOrderUseCase.ProcessOrderResult(order, false, "Error", -1, -1)
        );
    }

    @Test
    void shouldBeEqualWhenAllFieldsAreEqual() {
        // Given
        var order = createValidOrder();
        var result1 = new ProcessOrderUseCase.ProcessOrderResult(order, true, "Success", 2, 0);
        var result2 = new ProcessOrderUseCase.ProcessOrderResult(order, true, "Success", 2, 0);
        
        // When & Then
        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenFieldsDiffer() {
        // Given
        var order = createValidOrder();
        var result1 = new ProcessOrderUseCase.ProcessOrderResult(order, true, "Success", 2, 0);
        var result2 = new ProcessOrderUseCase.ProcessOrderResult(order, false, "Success", 2, 0);
        
        // When & Then
        assertNotEquals(result1, result2);
    }

    @Test
    void shouldHaveValidStringRepresentation() {
        // Given
        var order = createValidOrder();
        var result = new ProcessOrderUseCase.ProcessOrderResult(order, true, "Success", 2, 0);
        
        // When
        String stringRepresentation = result.toString();
        
        // Then
        assertNotNull(stringRepresentation);
        assertTrue(stringRepresentation.contains("ProcessOrderResult"));
        assertTrue(stringRepresentation.contains("true"));
        assertTrue(stringRepresentation.contains("Success"));
    }
}