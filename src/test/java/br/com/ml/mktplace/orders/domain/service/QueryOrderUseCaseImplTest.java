package br.com.ml.mktplace.orders.domain.service;

import br.com.ml.mktplace.orders.domain.model.*;
import br.com.ml.mktplace.orders.domain.port.OrderRepository;
import br.com.ml.mktplace.orders.domain.port.QueryOrderUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QueryOrderUseCaseImpl Unit Tests")
class QueryOrderUseCaseImplTest {

    @Mock
    private OrderRepository orderRepository;
    
    private QueryOrderUseCaseImpl useCase;
    
    private Order testOrder1;
    private Order testOrder2;
    private Order testOrder3;
    private List<Order> allOrders;
    
    @BeforeEach
    void setUp() {
        useCase = new QueryOrderUseCaseImpl(orderRepository);
        
        Address address1 = new Address(
            "123 Main St",
            "1",
            "Springfield",
            "IL",
            "USA",
            "12345-678",
            new Address.Coordinates(
                BigDecimal.valueOf(39.7817),
                BigDecimal.valueOf(-89.6501)
            )
        );
        
        Address address2 = new Address(
            "456 Oak Ave",
            "10",
            "Chicago",
            "IL",
            "USA",
            "60601-123",
            new Address.Coordinates(
                BigDecimal.valueOf(41.8781),
                BigDecimal.valueOf(-87.6298)
            )
        );
        
        OrderItem item1 = new OrderItem("ITEM-001", 2);
        OrderItem item2 = new OrderItem("ITEM-002", 1);
        OrderItem item3 = new OrderItem("ITEM-003", 3);
        
        testOrder1 = new Order(
            "ORDER-001",
            "CUSTOMER-123",
            List.of(item1),
            address1,
            OrderStatus.RECEIVED,
            Instant.now().minusSeconds(3600)
        );
        
        testOrder2 = new Order(
            "ORDER-002",
            "CUSTOMER-123",
            List.of(item2),
            address1,
            OrderStatus.PROCESSED,
            Instant.now().minusSeconds(1800)
        );
        
        testOrder3 = new Order(
            "ORDER-003",
            "CUSTOMER-456",
            List.of(item3),
            address2,
            OrderStatus.FAILED,
            Instant.now()
        );
        
        allOrders = List.of(testOrder1, testOrder2, testOrder3);
    }
    
    @Test
    @DisplayName("Should find order by ID when exists")
    void shouldFindOrderByIdWhenExists() {
        // Given
        when(orderRepository.findById("ORDER-001")).thenReturn(Optional.of(testOrder1));
        
        // When
        Optional<Order> result = useCase.getOrderById("ORDER-001");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("ORDER-001");
        assertThat(result.get().getCustomerId()).isEqualTo("CUSTOMER-123");
        verify(orderRepository).findById("ORDER-001");
    }
    
    @Test
    @DisplayName("Should return empty when order ID does not exist")
    void shouldReturnEmptyWhenOrderIdDoesNotExist() {
        // Given
        when(orderRepository.findById("NON-EXISTENT")).thenReturn(Optional.empty());
        
        // When
        Optional<Order> result = useCase.getOrderById("NON-EXISTENT");
        
        // Then
        assertThat(result).isEmpty();
        verify(orderRepository).findById("NON-EXISTENT");
    }
    
    @Test
    @DisplayName("Should throw exception when order ID is null for getOrderById")
    void shouldThrowExceptionWhenOrderIdIsNullForGetOrderById() {
        // When/Then
        assertThatThrownBy(() -> useCase.getOrderById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Order ID cannot be null or empty");
        
        verifyNoInteractions(orderRepository);
    }
    
    @Test
    @DisplayName("Should throw exception when order ID is empty for getOrderById")
    void shouldThrowExceptionWhenOrderIdIsEmptyForGetOrderById() {
        // When/Then
        assertThatThrownBy(() -> useCase.getOrderById(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Order ID cannot be null or empty");
        
        verifyNoInteractions(orderRepository);
    }
    
    @Test
    @DisplayName("Should find required order by ID when exists")
    void shouldFindRequiredOrderByIdWhenExists() {
        // Given
        when(orderRepository.findById("ORDER-001")).thenReturn(Optional.of(testOrder1));
        
        // When
        Order result = useCase.getOrderByIdRequired("ORDER-001");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("ORDER-001");
        verify(orderRepository).findById("ORDER-001");
    }
    
    @Test
    @DisplayName("Should throw OrderNotFoundException for getOrderByIdRequired when not found")
    void shouldThrowOrderNotFoundExceptionForGetOrderByIdRequiredWhenNotFound() {
        // Given
        when(orderRepository.findById("NON-EXISTENT")).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> useCase.getOrderByIdRequired("NON-EXISTENT"))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessage("Order not found with ID: NON-EXISTENT");
        
        verify(orderRepository).findById("NON-EXISTENT");
    }
    
    @Test
    @DisplayName("Should find orders by customer ID")
    void shouldFindOrdersByCustomerId() {
        // Given
        List<Order> customerOrders = List.of(testOrder1, testOrder2);
        when(orderRepository.findByCustomerId("CUSTOMER-123")).thenReturn(customerOrders);
        
        // When
        List<Order> result = useCase.getOrdersByCustomerId("CUSTOMER-123");
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(testOrder1, testOrder2);
        verify(orderRepository).findByCustomerId("CUSTOMER-123");
    }
    
    @Test
    @DisplayName("Should return empty list when customer has no orders")
    void shouldReturnEmptyListWhenCustomerHasNoOrders() {
        // Given
        when(orderRepository.findByCustomerId("NON-EXISTENT-CUSTOMER")).thenReturn(List.of());
        
        // When
        List<Order> result = useCase.getOrdersByCustomerId("NON-EXISTENT-CUSTOMER");
        
        // Then
        assertThat(result).isEmpty();
        verify(orderRepository).findByCustomerId("NON-EXISTENT-CUSTOMER");
    }
    
    @Test
    @DisplayName("Should throw exception when customer ID is null")
    void shouldThrowExceptionWhenCustomerIdIsNull() {
        // When/Then
        assertThatThrownBy(() -> useCase.getOrdersByCustomerId(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Customer ID cannot be null or empty");
        
        verifyNoInteractions(orderRepository);
    }
    
    @Test
    @DisplayName("Should find orders by status")
    void shouldFindOrdersByStatus() {
        // Given
        when(orderRepository.findAll()).thenReturn(allOrders);
        
        // When
        List<Order> result = useCase.getOrdersByStatus(OrderStatus.RECEIVED);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.RECEIVED);
        verify(orderRepository).findAll();
    }
    
    @Test
    @DisplayName("Should return empty list when no orders match status")
    void shouldReturnEmptyListWhenNoOrdersMatchStatus() {
        // Given
        when(orderRepository.findAll()).thenReturn(allOrders);
        
        // When
        List<Order> result = useCase.getOrdersByStatus(OrderStatus.PROCESSING);
        
        // Then
        assertThat(result).isEmpty();
        verify(orderRepository).findAll();
    }
    
    @Test
    @DisplayName("Should throw exception when status is null")
    void shouldThrowExceptionWhenStatusIsNull() {
        // When/Then
        assertThatThrownBy(() -> useCase.getOrdersByStatus(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Status cannot be null");
        
        verifyNoInteractions(orderRepository);
    }
    
    @Test
    @DisplayName("Should get all orders")
    void shouldGetAllOrders() {
        // Given
        when(orderRepository.findAll()).thenReturn(allOrders);
        
        // When
        List<Order> result = useCase.getAllOrders();
        
        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(testOrder1, testOrder2, testOrder3);
        verify(orderRepository).findAll();
    }
    
    @Test
    @DisplayName("Should return true when order exists")
    void shouldReturnTrueWhenOrderExists() {
        // Given
        when(orderRepository.existsById("ORDER-001")).thenReturn(true);
        
        // When
        boolean result = useCase.orderExists("ORDER-001");
        
        // Then
        assertThat(result).isTrue();
        verify(orderRepository).existsById("ORDER-001");
    }
    
    @Test
    @DisplayName("Should return false when order does not exist")
    void shouldReturnFalseWhenOrderDoesNotExist() {
        // Given
        when(orderRepository.existsById("NON-EXISTENT")).thenReturn(false);
        
        // When
        boolean result = useCase.orderExists("NON-EXISTENT");
        
        // Then
        assertThat(result).isFalse();
        verify(orderRepository).existsById("NON-EXISTENT");
    }
    
    @Test
    @DisplayName("Should search orders with customer ID filter")
    void shouldSearchOrdersWithCustomerIdFilter() {
        // Given
        when(orderRepository.findAll()).thenReturn(allOrders);
        QueryOrderUseCase.OrderSearchCriteria criteria = new QueryOrderUseCase.OrderSearchCriteria(
            "CUSTOMER-123", null, null, 0, 10
        );
        
        // When
        QueryOrderUseCase.OrderSearchResult result = useCase.searchOrders(criteria);
        
        // Then
        assertThat(result.orders()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.currentPage()).isEqualTo(0);
        assertThat(result.isEmpty()).isFalse();
    }
    
    @Test
    @DisplayName("Should search orders with status filter")
    void shouldSearchOrdersWithStatusFilter() {
        // Given
        when(orderRepository.findAll()).thenReturn(allOrders);
        QueryOrderUseCase.OrderSearchCriteria criteria = new QueryOrderUseCase.OrderSearchCriteria(
            null, OrderStatus.PROCESSED, null, 0, 10
        );
        
        // When
        QueryOrderUseCase.OrderSearchResult result = useCase.searchOrders(criteria);
        
        // Then
        assertThat(result.orders()).hasSize(1);
        assertThat(result.orders().get(0).getStatus()).isEqualTo(OrderStatus.PROCESSED);
        assertThat(result.totalElements()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Should search orders with item ID filter")
    void shouldSearchOrdersWithItemIdFilter() {
        // Given
        when(orderRepository.findAll()).thenReturn(allOrders);
        QueryOrderUseCase.OrderSearchCriteria criteria = new QueryOrderUseCase.OrderSearchCriteria(
            null, null, "ITEM-001", 0, 10
        );
        
        // When
        QueryOrderUseCase.OrderSearchResult result = useCase.searchOrders(criteria);
        
        // Then
        assertThat(result.orders()).hasSize(1);
        assertThat(result.orders().get(0).getId()).isEqualTo("ORDER-001");
        assertThat(result.totalElements()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Should search orders with pagination")
    void shouldSearchOrdersWithPagination() {
        // Given
        when(orderRepository.findAll()).thenReturn(allOrders);
        QueryOrderUseCase.OrderSearchCriteria criteria = new QueryOrderUseCase.OrderSearchCriteria(
            null, null, null, 0, 2
        );
        
        // When
        QueryOrderUseCase.OrderSearchResult result = useCase.searchOrders(criteria);
        
        // Then
        assertThat(result.orders()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.currentPage()).isEqualTo(0);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.hasPrevious()).isFalse();
    }
    
    @Test
    @DisplayName("Should search orders with second page pagination")
    void shouldSearchOrdersWithSecondPagePagination() {
        // Given
        when(orderRepository.findAll()).thenReturn(allOrders);
        QueryOrderUseCase.OrderSearchCriteria criteria = new QueryOrderUseCase.OrderSearchCriteria(
            null, null, null, 1, 2
        );
        
        // When
        QueryOrderUseCase.OrderSearchResult result = useCase.searchOrders(criteria);
        
        // Then
        assertThat(result.orders()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.currentPage()).isEqualTo(1);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.hasPrevious()).isTrue();
    }
    
    @Test
    @DisplayName("Should return empty result when no orders match criteria")
    void shouldReturnEmptyResultWhenNoOrdersMatchCriteria() {
        // Given
        when(orderRepository.findAll()).thenReturn(allOrders);
        QueryOrderUseCase.OrderSearchCriteria criteria = new QueryOrderUseCase.OrderSearchCriteria(
            "NON-EXISTENT-CUSTOMER", null, null, 0, 10
        );
        
        // When
        QueryOrderUseCase.OrderSearchResult result = useCase.searchOrders(criteria);
        
        // Then
        assertThat(result.orders()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);
        assertThat(result.totalPages()).isEqualTo(0);
        assertThat(result.isEmpty()).isTrue();
    }
    
    @Test
    @DisplayName("Should throw exception when search criteria is null")
    void shouldThrowExceptionWhenSearchCriteriaIsNull() {
        // When/Then
        assertThatThrownBy(() -> useCase.searchOrders(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Search criteria cannot be null");
        
        verifyNoInteractions(orderRepository);
    }
    
    @Test
    @DisplayName("Should search orders with multiple filters combined")
    void shouldSearchOrdersWithMultipleFiltersCombined() {
        // Given
        when(orderRepository.findAll()).thenReturn(allOrders);
        QueryOrderUseCase.OrderSearchCriteria criteria = new QueryOrderUseCase.OrderSearchCriteria(
            "CUSTOMER-123", OrderStatus.RECEIVED, "ITEM-001", 0, 10
        );
        
        // When
        QueryOrderUseCase.OrderSearchResult result = useCase.searchOrders(criteria);
        
        // Then
        assertThat(result.orders()).hasSize(1);
        assertThat(result.orders().get(0).getId()).isEqualTo("ORDER-001");
        assertThat(result.orders().get(0).getCustomerId()).isEqualTo("CUSTOMER-123");
        assertThat(result.orders().get(0).getStatus()).isEqualTo(OrderStatus.RECEIVED);
    }
}