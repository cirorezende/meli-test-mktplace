package br.com.ml.mktplace.orders.domain.port;

import br.com.ml.mktplace.orders.domain.model.Address;
import br.com.ml.mktplace.orders.domain.model.OrderItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CreateOrderUseCaseTest {

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

    private List<OrderItem> createValidItems() {
        return List.of(
            new OrderItem("ITEM001", 2),
            new OrderItem("ITEM002", 1)
        );
    }

    @Test
    void shouldCreateValidCreateOrderCommand() {
        // Given
        String customerId = "customer123";
        var items = createValidItems();
        var address = createValidAddress();
        
        // When
        var command = new CreateOrderUseCase.CreateOrderCommand(customerId, items, address);
        
        // Then
        assertNotNull(command);
        assertEquals(customerId, command.customerId());
        assertEquals(items, command.items());
        assertEquals(address, command.deliveryAddress());
    }

    @Test
    void shouldThrowExceptionWhenCustomerIdIsNull() {
        // Given
        var items = createValidItems();
        var address = createValidAddress();
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new CreateOrderUseCase.CreateOrderCommand(null, items, address)
        );
    }

    @Test
    void shouldThrowExceptionWhenCustomerIdIsEmpty() {
        // Given
        var items = createValidItems();
        var address = createValidAddress();
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new CreateOrderUseCase.CreateOrderCommand("", items, address)
        );
    }

    @Test
    void shouldThrowExceptionWhenCustomerIdIsBlank() {
        // Given
        var items = createValidItems();
        var address = createValidAddress();
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new CreateOrderUseCase.CreateOrderCommand("   ", items, address)
        );
    }

    @Test
    void shouldThrowExceptionWhenItemsIsNull() {
        // Given
        String customerId = "customer123";
        var address = createValidAddress();
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new CreateOrderUseCase.CreateOrderCommand(customerId, null, address)
        );
    }

    @Test
    void shouldThrowExceptionWhenItemsIsEmpty() {
        // Given
        String customerId = "customer123";
        List<OrderItem> emptyItems = Collections.emptyList();
        var address = createValidAddress();
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new CreateOrderUseCase.CreateOrderCommand(customerId, emptyItems, address)
        );
    }

    @Test
    void shouldThrowExceptionWhenDeliveryAddressIsNull() {
        // Given
        String customerId = "customer123";
        var items = createValidItems();
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new CreateOrderUseCase.CreateOrderCommand(customerId, items, null)
        );
    }

    @Test
    void shouldAcceptValidCustomerIdWithSpaces() {
        // Given
        String customerId = "customer 123";
        var items = createValidItems();
        var address = createValidAddress();
        
        // When & Then
        assertDoesNotThrow(() ->
            new CreateOrderUseCase.CreateOrderCommand(customerId, items, address)
        );
    }

    @Test
    void shouldAcceptSingleItem() {
        // Given
        String customerId = "customer123";
        List<OrderItem> singleItem = List.of(new OrderItem("ITEM001", 1));
        var address = createValidAddress();
        
        // When
        var command = new CreateOrderUseCase.CreateOrderCommand(customerId, singleItem, address);
        
        // Then
        assertEquals(1, command.items().size());
        assertEquals("ITEM001", command.items().get(0).getItemId());
    }

    @Test
    void shouldAcceptMultipleItems() {
        // Given
        String customerId = "customer123";
        var items = new ArrayList<OrderItem>();
        for (int i = 1; i <= 10; i++) {
            items.add(new OrderItem("ITEM" + String.format("%03d", i), i));
        }
        var address = createValidAddress();
        
        // When
        var command = new CreateOrderUseCase.CreateOrderCommand(customerId, items, address);
        
        // Then
        assertEquals(10, command.items().size());
    }

    @Test
    void shouldBeEqualWhenAllFieldsAreEqual() {
        // Given
        String customerId = "customer123";
        var items = createValidItems();
        var address = createValidAddress();
        
        var command1 = new CreateOrderUseCase.CreateOrderCommand(customerId, items, address);
        var command2 = new CreateOrderUseCase.CreateOrderCommand(customerId, items, address);
        
        // When & Then
        assertEquals(command1, command2);
        assertEquals(command1.hashCode(), command2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenCustomerIdIsDifferent() {
        // Given
        var items = createValidItems();
        var address = createValidAddress();
        
        var command1 = new CreateOrderUseCase.CreateOrderCommand("customer1", items, address);
        var command2 = new CreateOrderUseCase.CreateOrderCommand("customer2", items, address);
        
        // When & Then
        assertNotEquals(command1, command2);
    }

    @Test
    void shouldHaveValidStringRepresentation() {
        // Given
        String customerId = "customer123";
        var items = createValidItems();
        var address = createValidAddress();
        var command = new CreateOrderUseCase.CreateOrderCommand(customerId, items, address);
        
        // When
        String result = command.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains(customerId));
        assertTrue(result.contains("CreateOrderCommand"));
    }
}