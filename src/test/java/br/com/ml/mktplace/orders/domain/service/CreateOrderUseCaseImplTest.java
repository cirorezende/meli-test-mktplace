package br.com.ml.mktplace.orders.domain.service;

import br.com.ml.mktplace.orders.domain.model.*;
import br.com.ml.mktplace.orders.domain.port.OrderRepository;
import br.com.ml.mktplace.orders.domain.port.EventPublisher;
import br.com.ml.mktplace.orders.domain.port.IDGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateOrderUseCaseImpl Unit Tests")
class CreateOrderUseCaseImplTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @Mock
    private IDGenerator idGenerator;
    
    private CreateOrderUseCaseImpl useCase;
    
    private Address validAddress;
    private OrderItem validOrderItem;
    private List<OrderItem> validItems;
    
    @BeforeEach
    void setUp() {
        useCase = new CreateOrderUseCaseImpl(orderRepository, eventPublisher, idGenerator);
        
        validAddress = new Address(
            "123 Main St",
            "Springfield",
            "IL",
            "USA", 
            "12345-678",
            new Address.Coordinates(
                BigDecimal.valueOf(39.7817),
                BigDecimal.valueOf(-89.6501)
            )
        );
        
        validOrderItem = new OrderItem("ITEM-001", 2);
        validItems = List.of(validOrderItem);
    }
    
    @Test
    @DisplayName("Should create order successfully with valid inputs")
    void shouldCreateOrderSuccessfully() {
        // Given
        String customerId = "CUSTOMER-123";
        String generatedId = "ORDER-001";
        Order expectedOrder = new Order(generatedId, customerId, validItems, validAddress, OrderStatus.RECEIVED, java.time.Instant.now());
        
        when(idGenerator.generate()).thenReturn(generatedId);
        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);
        
        // When
        Order result = useCase.createOrder(customerId, validItems, validAddress);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(generatedId);
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.RECEIVED);
        
        verify(idGenerator).generate();
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publishOrderCreated(expectedOrder);
    }
    
    @Test
    @DisplayName("Should throw exception when customer ID is null")
    void shouldThrowExceptionWhenCustomerIdIsNull() {
        // When/Then
        assertThatThrownBy(() -> useCase.createOrder(null, validItems, validAddress))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Customer ID is required");
        
        verifyNoInteractions(idGenerator, orderRepository, eventPublisher);
    }
    
    @Test
    @DisplayName("Should throw exception when customer ID is empty")
    void shouldThrowExceptionWhenCustomerIdIsEmpty() {
        // When/Then
        assertThatThrownBy(() -> useCase.createOrder("", validItems, validAddress))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Customer ID is required");
        
        verifyNoInteractions(idGenerator, orderRepository, eventPublisher);
    }
    
    @Test
    @DisplayName("Should throw exception when customer ID is blank")
    void shouldThrowExceptionWhenCustomerIdIsBlank() {
        // When/Then
        assertThatThrownBy(() -> useCase.createOrder("   ", validItems, validAddress))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Customer ID is required");
        
        verifyNoInteractions(idGenerator, orderRepository, eventPublisher);
    }
    
    @Test
    @DisplayName("Should throw exception when items list is null")
    void shouldThrowExceptionWhenItemsIsNull() {
        // When/Then
        assertThatThrownBy(() -> useCase.createOrder("CUSTOMER-123", null, validAddress))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Order must have at least one item");
        
        verifyNoInteractions(idGenerator, orderRepository, eventPublisher);
    }
    
    @Test
    @DisplayName("Should throw exception when items list is empty")
    void shouldThrowExceptionWhenItemsIsEmpty() {
        // When/Then
        assertThatThrownBy(() -> useCase.createOrder("CUSTOMER-123", List.of(), validAddress))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Order must have at least one item");
        
        verifyNoInteractions(idGenerator, orderRepository, eventPublisher);
    }
    
    @Test
    @DisplayName("Should throw exception when items list has more than 100 items")
    void shouldThrowExceptionWhenTooManyItems() {
        // Given
        List<OrderItem> tooManyItems = java.util.stream.IntStream.range(0, 101)
            .mapToObj(i -> new OrderItem("ITEM-" + String.format("%03d", i), 1))
            .toList();
        
        // When/Then
        assertThatThrownBy(() -> useCase.createOrder("CUSTOMER-123", tooManyItems, validAddress))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Order cannot have more than 100 items");
        
        verifyNoInteractions(idGenerator, orderRepository, eventPublisher);
    }
    
    @Test
    @DisplayName("Should throw exception when delivery address is null")
    void shouldThrowExceptionWhenAddressIsNull() {
        // When/Then
        assertThatThrownBy(() -> useCase.createOrder("CUSTOMER-123", validItems, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Delivery address is required");
        
        verifyNoInteractions(idGenerator, orderRepository, eventPublisher);
    }
    
    @Test
    @DisplayName("Should throw exception when items contain duplicates")
    void shouldThrowExceptionWhenItemsContainDuplicates() {
        // Given
        List<OrderItem> duplicateItems = List.of(
            new OrderItem("ITEM-001", 1),
            new OrderItem("ITEM-002", 2),
            new OrderItem("ITEM-001", 3) // Duplicate
        );
        
        // When/Then
        assertThatThrownBy(() -> useCase.createOrder("CUSTOMER-123", duplicateItems, validAddress))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Order cannot have duplicate products");
        
        verifyNoInteractions(idGenerator, orderRepository, eventPublisher);
    }
    
    @Test
    @DisplayName("Should handle repository save failure gracefully")
    void shouldHandleRepositorySaveFailure() {
        // Given
        String customerId = "CUSTOMER-123";
        String generatedId = "ORDER-001";
        
        when(idGenerator.generate()).thenReturn(generatedId);
        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("Database error"));
        
        // When/Then
        assertThatThrownBy(() -> useCase.createOrder(customerId, validItems, validAddress))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database error");
        
        verify(idGenerator).generate();
        verify(orderRepository).save(any(Order.class));
        verifyNoInteractions(eventPublisher);
    }
    
    @Test
    @DisplayName("Should create order with single item at boundary (1 item)")
    void shouldCreateOrderWithSingleItem() {
        // Given
        String customerId = "CUSTOMER-123";
        String generatedId = "ORDER-001";
        List<OrderItem> singleItem = List.of(new OrderItem("ITEM-001", 1));
        Order expectedOrder = new Order(generatedId, customerId, singleItem, validAddress, OrderStatus.RECEIVED, java.time.Instant.now());
        
        when(idGenerator.generate()).thenReturn(generatedId);
        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);
        
        // When
        Order result = useCase.createOrder(customerId, singleItem, validAddress);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        
        verify(idGenerator).generate();
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publishOrderCreated(expectedOrder);
    }
    
    @Test
    @DisplayName("Should create order with maximum items at boundary (100 items)")
    void shouldCreateOrderWithMaximumItems() {
        // Given
        String customerId = "CUSTOMER-123";
        String generatedId = "ORDER-001";
        List<OrderItem> maxItems = java.util.stream.IntStream.range(0, 100)
            .mapToObj(i -> new OrderItem("ITEM-" + String.format("%03d", i), 1))
            .toList();
        Order expectedOrder = new Order(generatedId, customerId, maxItems, validAddress, OrderStatus.RECEIVED, java.time.Instant.now());
        
        when(idGenerator.generate()).thenReturn(generatedId);
        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);
        
        // When
        Order result = useCase.createOrder(customerId, maxItems, validAddress);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(100);
        
        verify(idGenerator).generate();
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publishOrderCreated(expectedOrder);
    }
}