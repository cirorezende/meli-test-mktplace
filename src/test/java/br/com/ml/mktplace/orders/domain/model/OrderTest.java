package br.com.ml.mktplace.orders.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Address createValidAddress() {
        var coordinates = new Address.Coordinates(
            new BigDecimal("-23.5505"), 
            new BigDecimal("-46.6333")
        );
        
        return new Address(
            "Rua da Consolação, 247",
            "São Paulo",
            "SP",
            "Brazil",
            "01301-000",
            coordinates
        );
    }

    private List<OrderItem> createValidItems() {
        return List.of(
            new OrderItem("ITEM001", 2),
            new OrderItem("ITEM002", 1),
            new OrderItem("ITEM003", 3)
        );
    }

    private DistributionCenter createValidDistributionCenter() {
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
        
        return new DistributionCenter("SP01", "Centro SP", address);
    }

    @Test
    void shouldCreateValidOrderWithNewConstructor() {
        // Given
        var items = createValidItems();
        var address = createValidAddress();
        
        // When
        var order = new Order(items, address);
        
        // Then
        assertNotNull(order);
        assertNotNull(order.getId());
        assertFalse(order.getId().isEmpty()); // ULID deve ser gerado
        assertEquals(3, order.getItems().size());
        assertEquals(address, order.getDeliveryAddress());
        assertEquals(OrderStatus.RECEIVED, order.getStatus()); // Status inicial
        assertNotNull(order.getCreatedAt());
        assertTrue(order.getCreatedAt().isBefore(Instant.now().plusSeconds(1))); // Criado recentemente
    }

    @Test
    void shouldCreateOrderWithReconstructionConstructor() {
        // Given
        var id = "01234567890123456789012345";
        var items = createValidItems();
        var address = createValidAddress();
        var status = OrderStatus.PROCESSING;
        var createdAt = Instant.now().minusSeconds(3600);
        
        // When
        var order = new Order(id, items, address, status, createdAt);
        
        // Then
        assertNotNull(order);
        assertEquals(id, order.getId());
        assertEquals(3, order.getItems().size());
        assertEquals(address, order.getDeliveryAddress());
        assertEquals(status, order.getStatus());
        assertEquals(createdAt, order.getCreatedAt());
    }

    @Test
    void shouldThrowExceptionWhenItemsListIsNull() {
        // Given
        var address = createValidAddress();
        
        // When & Then
        assertThrows(NullPointerException.class, () ->
            new Order(null, address)
        );
    }

    @Test
    void shouldThrowExceptionWhenItemsListIsEmpty() {
        // Given
        var items = Collections.<OrderItem>emptyList();
        var address = createValidAddress();
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new Order(items, address)
        );
    }

    @Test
    void shouldThrowExceptionWhenDeliveryAddressIsNull() {
        // Given
        var items = createValidItems();
        
        // When & Then
        assertThrows(NullPointerException.class, () ->
            new Order(items, null)
        );
    }

    @Test
    void shouldThrowExceptionWhenItemsExceedMaximum() {
        // Given
        var items = new ArrayList<OrderItem>();
        for (int i = 1; i <= 101; i++) { // Mais que o máximo permitido (100)
            items.add(new OrderItem("ITEM" + String.format("%03d", i), 1));
        }
        var address = createValidAddress();
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new Order(items, address)
        );
    }

    @Test
    void shouldAcceptMaximumNumberOfItems() {
        // Given
        var items = new ArrayList<OrderItem>();
        for (int i = 1; i <= 100; i++) { // Exatamente o máximo permitido
            items.add(new OrderItem("ITEM" + String.format("%03d", i), 1));
        }
        var address = createValidAddress();
        
        // When & Then
        assertDoesNotThrow(() -> {
            var order = new Order(items, address);
            assertEquals(100, order.getItems().size());
        });
    }

    @Test
    void shouldChangeStatus() {
        // Given
        var items = createValidItems();
        var address = createValidAddress();
        var order = new Order(items, address);
        
        // When
        order.changeStatus(OrderStatus.PROCESSING);
        
        // Then
        assertEquals(OrderStatus.PROCESSING, order.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenChangingToNullStatus() {
        // Given
        var items = createValidItems();
        var address = createValidAddress();
        var order = new Order(items, address);
        
        // When & Then
        assertThrows(NullPointerException.class, () ->
            order.changeStatus(null)
        );
    }

    @Test
    void shouldReturnTrueWhenAllItemsAreAssigned() {
        // Given
        var items = createValidItems();
        var address = createValidAddress();
        var order = new Order(items, address);
        var distributionCenter = createValidDistributionCenter();
        
        // When
        order.getItems().forEach(item -> item.assignDistributionCenter(distributionCenter));
        
        // Then
        assertTrue(order.areAllItemsAssigned());
    }

    @Test
    void shouldReturnFalseWhenNotAllItemsAreAssigned() {
        // Given
        var items = createValidItems();
        var address = createValidAddress();
        var order = new Order(items, address);
        var distributionCenter = createValidDistributionCenter();
        
        // When - Atribui apenas o primeiro item
        order.getItems().get(0).assignDistributionCenter(distributionCenter);
        
        // Then
        assertFalse(order.areAllItemsAssigned());
    }

    @Test
    void shouldCalculateTotalItemsCount() {
        // Given
        var items = Arrays.asList(
            new OrderItem("ITEM001", 2),
            new OrderItem("ITEM002", 1),
            new OrderItem("ITEM003", 3),
            new OrderItem("ITEM004", 4)
        );
        var address = createValidAddress();
        var order = new Order(items, address);
        
        // When
        var totalCount = order.getTotalItemsCount();
        
        // Then
        assertEquals(10, totalCount); // 2 + 1 + 3 + 4 = 10
    }

    @Test
    void shouldReturnTrueForFinalStates() {
        // Given
        var items = createValidItems();
        var address = createValidAddress();
        var order = new Order(items, address);
        
        // When & Then - PROCESSED
        order.changeStatus(OrderStatus.PROCESSED);
        assertTrue(order.isInFinalState());
        
        // When & Then - FAILED
        order.changeStatus(OrderStatus.FAILED);
        assertTrue(order.isInFinalState());
    }

    @Test
    void shouldReturnFalseForNonFinalStates() {
        // Given
        var items = createValidItems();
        var address = createValidAddress();
        var order = new Order(items, address);
        
        // When & Then - RECEIVED (inicial)
        assertEquals(OrderStatus.RECEIVED, order.getStatus());
        assertFalse(order.isInFinalState());
        
        // When & Then - PROCESSING
        order.changeStatus(OrderStatus.PROCESSING);
        assertFalse(order.isInFinalState());
    }

    @Test
    void shouldReturnUnmodifiableItemsList() {
        // Given
        var items = createValidItems();
        var address = createValidAddress();
        var order = new Order(items, address);
        
        // When
        var returnedItems = order.getItems();
        
        // Then
        assertThrows(UnsupportedOperationException.class, () ->
            returnedItems.add(new OrderItem("ITEM999", 1))
        );
    }

    @Test
    void shouldNotModifyOriginalItemsList() {
        // Given
        var items = new ArrayList<>(createValidItems());
        var address = createValidAddress();
        var order = new Order(items, address);
        var originalSize = items.size();
        
        // When - Modifica a lista original
        items.add(new OrderItem("ITEM999", 1));
        
        // Then - Order não deve ser afetada
        assertEquals(originalSize, order.getItems().size());
        assertFalse(order.getItems().stream()
            .anyMatch(item -> "ITEM999".equals(item.getItemId())));
    }

    @Test
    void shouldBeEqualWhenIdIsEqual() {
        // Given
        var id = "01234567890123456789012345";
        var items = createValidItems();
        var address = createValidAddress();
        var createdAt = Instant.now();
        
        var order1 = new Order(id, items, address, OrderStatus.RECEIVED, createdAt);
        var order2 = new Order(id, items, address, OrderStatus.PROCESSING, createdAt);
        
        // When & Then
        assertEquals(order1, order2);
        assertEquals(order1.hashCode(), order2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenIdIsDifferent() {
        // Given
        var items = createValidItems();
        var address = createValidAddress();
        var createdAt = Instant.now();
        
        var order1 = new Order("01234567890123456789012345", items, address, OrderStatus.RECEIVED, createdAt);
        var order2 = new Order("01234567890123456789012346", items, address, OrderStatus.RECEIVED, createdAt);
        
        // When & Then
        assertNotEquals(order1, order2);
    }

    @Test
    void shouldHaveValidStringRepresentation() {
        // Given
        var items = createValidItems();
        var address = createValidAddress();
        var order = new Order(items, address);
        
        // When
        var result = order.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains(order.getId()));
        assertTrue(result.contains("3")); // itemsCount
        assertTrue(result.contains("6")); // totalItemsCount (2+1+3)
        assertTrue(result.contains("RECEIVED"));
    }

    @Test
    void shouldThrowExceptionWhenReconstructionParametersAreNull() {
        // Given
        var items = createValidItems();
        var address = createValidAddress();
        var createdAt = Instant.now();
        
        // When & Then
        assertThrows(NullPointerException.class, () ->
            new Order(null, items, address, OrderStatus.RECEIVED, createdAt)
        );
        
        assertThrows(NullPointerException.class, () ->
            new Order("123", items, address, null, createdAt)
        );
        
        assertThrows(NullPointerException.class, () ->
            new Order("123", items, address, OrderStatus.RECEIVED, null)
        );
    }
}