package br.com.ml.mktplace.orders.domain.port;

import br.com.ml.mktplace.orders.domain.model.Address;
import br.com.ml.mktplace.orders.domain.model.Order;
import br.com.ml.mktplace.orders.domain.model.OrderItem;
import br.com.ml.mktplace.orders.domain.model.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryOrderUseCaseTest {

    private Order createValidOrder() {
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
        
        var items = List.of(
            new OrderItem("ITEM001", 2),
            new OrderItem("ITEM002", 1)
        );
        
        return new Order(items, address);
    }

    @Test
    void shouldCreateValidOrderSearchCriteria() {
        // Given
        String customerId = "customer123";
        OrderStatus status = OrderStatus.PROCESSING;
        String itemId = "ITEM001";
        int page = 0;
        int size = 10;
        
        // When
        var criteria = new QueryOrderUseCase.OrderSearchCriteria(
            customerId, status, itemId, page, size
        );
        
        // Then
        assertNotNull(criteria);
        assertEquals(customerId, criteria.customerId());
        assertEquals(status, criteria.status());
        assertEquals(itemId, criteria.itemId());
        assertEquals(page, criteria.page());
        assertEquals(size, criteria.size());
    }

    @Test
    void shouldCreateOrderSearchCriteriaWithNullValues() {
        // When
        var criteria = new QueryOrderUseCase.OrderSearchCriteria(
            null, null, null, 0, 10
        );
        
        // Then
        assertNotNull(criteria);
        assertNull(criteria.customerId());
        assertNull(criteria.status());
        assertNull(criteria.itemId());
        assertEquals(0, criteria.page());
        assertEquals(10, criteria.size());
    }

    @Test
    void shouldThrowExceptionWhenPageIsNegative() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new QueryOrderUseCase.OrderSearchCriteria(null, null, null, -1, 10)
        );
    }

    @Test
    void shouldThrowExceptionWhenSizeIsZero() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new QueryOrderUseCase.OrderSearchCriteria(null, null, null, 0, 0)
        );
    }

    @Test
    void shouldThrowExceptionWhenSizeIsNegative() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new QueryOrderUseCase.OrderSearchCriteria(null, null, null, 0, -1)
        );
    }

    @Test
    void shouldCreateDefaultCriteria() {
        // When
        var criteria = QueryOrderUseCase.OrderSearchCriteria.defaultCriteria();
        
        // Then
        assertNotNull(criteria);
        assertNull(criteria.customerId());
        assertNull(criteria.status());
        assertNull(criteria.itemId());
        assertEquals(0, criteria.page());
        assertEquals(10, criteria.size());
    }

    @Test
    void shouldCreateValidOrderSearchResult() {
        // Given
        var orders = List.of(createValidOrder());
        int totalElements = 1;
        int totalPages = 1;
        int currentPage = 0;
        int pageSize = 10;
        
        // When
        var result = new QueryOrderUseCase.OrderSearchResult(
            orders, totalElements, totalPages, currentPage, pageSize
        );
        
        // Then
        assertNotNull(result);
        assertEquals(orders, result.orders());
        assertEquals(totalElements, result.totalElements());
        assertEquals(totalPages, result.totalPages());
        assertEquals(currentPage, result.currentPage());
        assertEquals(pageSize, result.pageSize());
    }

    @Test
    void shouldThrowExceptionWhenOrdersIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new QueryOrderUseCase.OrderSearchResult(null, 0, 0, 0, 10)
        );
    }

    @Test
    void shouldThrowExceptionWhenTotalElementsIsNegative() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new QueryOrderUseCase.OrderSearchResult(Collections.emptyList(), -1, 0, 0, 10)
        );
    }

    @Test
    void shouldThrowExceptionWhenTotalPagesIsNegative() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new QueryOrderUseCase.OrderSearchResult(Collections.emptyList(), 0, -1, 0, 10)
        );
    }

    @Test
    void shouldThrowExceptionWhenCurrentPageIsNegative() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new QueryOrderUseCase.OrderSearchResult(Collections.emptyList(), 0, 0, -1, 10)
        );
    }

    @Test
    void shouldThrowExceptionWhenPageSizeIsZero() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new QueryOrderUseCase.OrderSearchResult(Collections.emptyList(), 0, 0, 0, 0)
        );
    }

    @Test
    void shouldThrowExceptionWhenPageSizeIsNegative() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new QueryOrderUseCase.OrderSearchResult(Collections.emptyList(), 0, 0, 0, -1)
        );
    }

    @Test
    void shouldReturnTrueForHasNextWhenNotLastPage() {
        // Given
        var result = new QueryOrderUseCase.OrderSearchResult(
            Collections.emptyList(), 20, 2, 0, 10
        );
        
        // When & Then
        assertTrue(result.hasNext());
    }

    @Test
    void shouldReturnFalseForHasNextWhenLastPage() {
        // Given
        var result = new QueryOrderUseCase.OrderSearchResult(
            Collections.emptyList(), 10, 1, 0, 10
        );
        
        // When & Then
        assertFalse(result.hasNext());
    }

    @Test
    void shouldReturnTrueForHasPreviousWhenNotFirstPage() {
        // Given
        var result = new QueryOrderUseCase.OrderSearchResult(
            Collections.emptyList(), 20, 2, 1, 10
        );
        
        // When & Then
        assertTrue(result.hasPrevious());
    }

    @Test
    void shouldReturnFalseForHasPreviousWhenFirstPage() {
        // Given
        var result = new QueryOrderUseCase.OrderSearchResult(
            Collections.emptyList(), 20, 2, 0, 10
        );
        
        // When & Then
        assertFalse(result.hasPrevious());
    }

    @Test
    void shouldReturnTrueForIsEmptyWhenNoOrders() {
        // Given
        var result = new QueryOrderUseCase.OrderSearchResult(
            Collections.emptyList(), 0, 0, 0, 10
        );
        
        // When & Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnFalseForIsEmptyWhenHasOrders() {
        // Given
        var orders = List.of(createValidOrder());
        var result = new QueryOrderUseCase.OrderSearchResult(
            orders, 1, 1, 0, 10
        );
        
        // When & Then
        assertFalse(result.isEmpty());
    }

    @Test
    void shouldBeEqualWhenAllFieldsAreEqual() {
        // Given
        var orders = List.of(createValidOrder());
        var result1 = new QueryOrderUseCase.OrderSearchResult(orders, 1, 1, 0, 10);
        var result2 = new QueryOrderUseCase.OrderSearchResult(orders, 1, 1, 0, 10);
        
        // When & Then
        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenFieldsDiffer() {
        // Given
        var orders = List.of(createValidOrder());
        var result1 = new QueryOrderUseCase.OrderSearchResult(orders, 1, 1, 0, 10);
        var result2 = new QueryOrderUseCase.OrderSearchResult(orders, 1, 1, 1, 10);
        
        // When & Then
        assertNotEquals(result1, result2);
    }

    @Test
    void shouldHaveValidStringRepresentation() {
        // Given
        var orders = List.of(createValidOrder());
        var result = new QueryOrderUseCase.OrderSearchResult(orders, 1, 1, 0, 10);
        
        // When
        String stringRepresentation = result.toString();
        
        // Then
        assertNotNull(stringRepresentation);
        assertTrue(stringRepresentation.contains("OrderSearchResult"));
    }
}