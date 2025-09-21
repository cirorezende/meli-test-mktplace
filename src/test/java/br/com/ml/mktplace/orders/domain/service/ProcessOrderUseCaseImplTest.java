package br.com.ml.mktplace.orders.domain.service;

import br.com.ml.mktplace.orders.domain.model.*;
import br.com.ml.mktplace.orders.domain.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessOrderUseCaseImpl Unit Tests")
class ProcessOrderUseCaseImplTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private DistributionCenterService distributionCenterService;
    
    @Mock
    private CacheService cacheService;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @Mock
    private DistributionCenterSelectionService selectionService;
    
    private ProcessOrderUseCaseImpl useCase;
    
    private Order validOrder;
    private List<DistributionCenter> availableCenters;
    private DistributionCenter selectedCenter;
    
    @BeforeEach
    void setUp() {
        useCase = new ProcessOrderUseCaseImpl(
            orderRepository,
            distributionCenterService,
            cacheService,
            eventPublisher,
            selectionService
        );
        
        // Create test data
        Address address = new Address(
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
        
        OrderItem orderItem = new OrderItem("ITEM-001", 2);
        List<OrderItem> items = List.of(orderItem);
        
        validOrder = new Order(
            "ORDER-001",
            "CUSTOMER-123", 
            items,
            address,
            OrderStatus.RECEIVED,
            Instant.now()
        );
        
        selectedCenter = new DistributionCenter(
            "DC-001",
            "Main Distribution Center",
            new Address(
                "456 Warehouse Ave",
                "Springfield",
                "IL", 
                "USA",
                "12345-000",
                new Address.Coordinates(
                    BigDecimal.valueOf(39.7900),
                    BigDecimal.valueOf(-89.6400)
                )
            )
        );
        
        availableCenters = List.of(selectedCenter);
    }
    
    @Test
    @DisplayName("Should process order successfully")
    void shouldProcessOrderSuccessfully() {
        // Given
        when(orderRepository.findById("ORDER-001")).thenReturn(Optional.of(validOrder));
        when(cacheService.get(any(String.class), eq(List.class))).thenReturn(Optional.empty());
        when(distributionCenterService.findAllDistributionCenters()).thenReturn(availableCenters);
        when(selectionService.selectDistributionCenter(availableCenters, validOrder.getDeliveryAddress()))
            .thenReturn(selectedCenter);
        
        Order processedOrder = new Order(
            validOrder.getId(),
            validOrder.getCustomerId(),
            validOrder.getItems(),
            validOrder.getDeliveryAddress(),
            OrderStatus.PROCESSED,
            validOrder.getCreatedAt()
        );
        when(orderRepository.save(any(Order.class))).thenReturn(processedOrder);
        
        // When
        Order result = useCase.processOrder("ORDER-001");
        
        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESSED);
        verify(orderRepository, times(2)).save(any(Order.class)); // Processing + Final save
        verify(distributionCenterService).findAllDistributionCenters();
        verify(cacheService).put(any(String.class), eq(availableCenters), eq(Duration.ofMinutes(5)));
        verify(selectionService).selectDistributionCenter(availableCenters, validOrder.getDeliveryAddress());
        verify(eventPublisher).publishOrderProcessed(result);
    }
    
    @Test
    @DisplayName("Should throw exception when order ID is null")
    void shouldThrowExceptionWhenOrderIdIsNull() {
        // When/Then
        assertThatThrownBy(() -> useCase.processOrder(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Order ID cannot be null or empty");
        
        verifyNoInteractions(orderRepository, distributionCenterService, cacheService, eventPublisher, selectionService);
    }
    
    @Test
    @DisplayName("Should throw exception when order ID is empty")
    void shouldThrowExceptionWhenOrderIdIsEmpty() {
        // When/Then
        assertThatThrownBy(() -> useCase.processOrder(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Order ID cannot be null or empty");
        
        verifyNoInteractions(orderRepository, distributionCenterService, cacheService, eventPublisher, selectionService);
    }
    
    @Test
    @DisplayName("Should throw exception when order ID is blank")
    void shouldThrowExceptionWhenOrderIdIsBlank() {
        // When/Then
        assertThatThrownBy(() -> useCase.processOrder("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Order ID cannot be null or empty");
        
        verifyNoInteractions(orderRepository, distributionCenterService, cacheService, eventPublisher, selectionService);
    }
    
    @Test
    @DisplayName("Should throw OrderNotFoundException when order does not exist")
    void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist() {
        // Given
        when(orderRepository.findById("NON-EXISTENT")).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> useCase.processOrder("NON-EXISTENT"))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessage("Order not found with ID: NON-EXISTENT");
        
        verify(orderRepository).findById("NON-EXISTENT");
        verifyNoInteractions(distributionCenterService, cacheService, eventPublisher, selectionService);
    }
    
    @Test
    @DisplayName("Should throw ProcessOrderException when order is already processed")
    void shouldThrowProcessOrderExceptionWhenOrderIsAlreadyProcessed() {
        // Given
        Order processedOrder = new Order(
            "ORDER-001",
            "CUSTOMER-123",
            validOrder.getItems(),
            validOrder.getDeliveryAddress(),
            OrderStatus.PROCESSED,
            validOrder.getCreatedAt()
        );
        when(orderRepository.findById("ORDER-001")).thenReturn(Optional.of(processedOrder));
        
        // When/Then
        assertThatThrownBy(() -> useCase.processOrder("ORDER-001"))
            .isInstanceOf(ProcessOrderUseCase.ProcessOrderException.class)
            .hasMessage("Failed to process order [ORDER-001]: Order is already in final state: PROCESSED");
        
        verify(orderRepository).findById("ORDER-001");
        verifyNoInteractions(distributionCenterService, cacheService, eventPublisher, selectionService);
    }
    
    @Test
    @DisplayName("Should throw ProcessOrderException when order is failed")
    void shouldThrowProcessOrderExceptionWhenOrderIsFailed() {
        // Given
        Order failedOrder = new Order(
            "ORDER-001",
            "CUSTOMER-123",
            validOrder.getItems(),
            validOrder.getDeliveryAddress(),
            OrderStatus.FAILED,
            validOrder.getCreatedAt()
        );
        when(orderRepository.findById("ORDER-001")).thenReturn(Optional.of(failedOrder));
        
        // When/Then
        assertThatThrownBy(() -> useCase.processOrder("ORDER-001"))
            .isInstanceOf(ProcessOrderUseCase.ProcessOrderException.class)
            .hasMessage("Failed to process order [ORDER-001]: Order is already in final state: FAILED");
        
        verify(orderRepository).findById("ORDER-001");
        verifyNoInteractions(distributionCenterService, cacheService, eventPublisher, selectionService);
    }
    
    @Test
    @DisplayName("Should use cached distribution centers when available")
    void shouldUseCachedDistributionCentersWhenAvailable() {
        // Given
        when(orderRepository.findById("ORDER-001")).thenReturn(Optional.of(validOrder));
        @SuppressWarnings("rawtypes")
        Optional<List> cachedCenters = Optional.of(availableCenters);
        when(cacheService.get(any(String.class), eq(List.class))).thenReturn(cachedCenters);
        when(selectionService.selectDistributionCenter(availableCenters, validOrder.getDeliveryAddress()))
            .thenReturn(selectedCenter);
        
        Order processedOrder = new Order(
            validOrder.getId(),
            validOrder.getCustomerId(),
            validOrder.getItems(),
            validOrder.getDeliveryAddress(),
            OrderStatus.PROCESSED,
            validOrder.getCreatedAt()
        );
        when(orderRepository.save(any(Order.class))).thenReturn(processedOrder);
        
        // When
        useCase.processOrder("ORDER-001");
        
        // Then
        verify(cacheService).get(any(String.class), eq(List.class));
        verify(distributionCenterService, never()).findAllDistributionCenters();
        verify(cacheService, never()).put(any(String.class), any(), any(Duration.class));
    }
    
    @Test
    @DisplayName("Should handle ExternalServiceException and mark order as failed")
    void shouldHandleExternalServiceExceptionAndMarkOrderAsFailed() {
        // Given
        when(orderRepository.findById("ORDER-001")).thenReturn(Optional.of(validOrder));
        when(cacheService.get(any(String.class), eq(List.class))).thenReturn(Optional.empty());
        when(distributionCenterService.findAllDistributionCenters())
            .thenThrow(new ExternalServiceException("DistributionCenterService", "Service unavailable"));
        
        Order failedOrder = new Order(
            validOrder.getId(),
            validOrder.getCustomerId(),
            validOrder.getItems(),
            validOrder.getDeliveryAddress(),
            OrderStatus.FAILED,
            validOrder.getCreatedAt()
        );
        when(orderRepository.save(any(Order.class))).thenReturn(validOrder, failedOrder);
        
        // When/Then
        assertThatThrownBy(() -> useCase.processOrder("ORDER-001"))
            .isInstanceOf(ProcessOrderUseCase.ProcessOrderException.class)
            .hasMessage("Failed to process order [ORDER-001]: Distribution center service unavailable");
        
        verify(orderRepository, times(2)).save(any(Order.class)); // Processing + Failed save
        verify(eventPublisher).publishOrderFailed(eq(failedOrder), eq("Distribution center service unavailable"), any(ExternalServiceException.class));
    }
    
    @Test
    @DisplayName("Should handle empty distribution centers list")
    void shouldHandleEmptyDistributionCentersList() {
        // Given
        when(orderRepository.findById("ORDER-001")).thenReturn(Optional.of(validOrder));
        when(cacheService.get(any(String.class), eq(List.class))).thenReturn(Optional.empty());
        when(distributionCenterService.findAllDistributionCenters()).thenReturn(List.of());
        
        Order failedOrder = new Order(
            validOrder.getId(),
            validOrder.getCustomerId(),
            validOrder.getItems(),
            validOrder.getDeliveryAddress(),
            OrderStatus.FAILED,
            validOrder.getCreatedAt()
        );
        when(orderRepository.save(any(Order.class))).thenReturn(validOrder, failedOrder);
        
        // When/Then
        assertThatThrownBy(() -> useCase.processOrder("ORDER-001"))
            .isInstanceOf(ProcessOrderUseCase.ProcessOrderException.class)
            .hasMessage("Failed to process order [ORDER-001]: Distribution center service unavailable");
        
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(eventPublisher).publishOrderFailed(eq(failedOrder), eq("Distribution center service unavailable"), any(ExternalServiceException.class));
    }
    
    @Test
    @DisplayName("Should reprocess failed order successfully")
    void shouldReprocessFailedOrderSuccessfully() {
        // Given
        Order failedOrder = new Order(
            "ORDER-001",
            "CUSTOMER-123",
            validOrder.getItems(),
            validOrder.getDeliveryAddress(),
            OrderStatus.FAILED,
            validOrder.getCreatedAt()
        );
        
        when(orderRepository.findById("ORDER-001")).thenReturn(Optional.of(failedOrder));
        when(cacheService.get(any(String.class), eq(List.class))).thenReturn(Optional.empty());
        when(distributionCenterService.findAllDistributionCenters()).thenReturn(availableCenters);
        when(selectionService.selectDistributionCenter(availableCenters, failedOrder.getDeliveryAddress()))
            .thenReturn(selectedCenter);
        
        Order processedOrder = new Order(
            failedOrder.getId(),
            failedOrder.getCustomerId(),
            failedOrder.getItems(),
            failedOrder.getDeliveryAddress(),
            OrderStatus.PROCESSED,
            failedOrder.getCreatedAt()
        );
        when(orderRepository.save(any(Order.class))).thenReturn(failedOrder, processedOrder);
        
        // When
        Order result = useCase.reprocessOrder("ORDER-001");
        
        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESSED);
        verify(orderRepository, times(3)).save(any(Order.class)); // Reset + Processing + Final
        verify(eventPublisher).publishOrderProcessed(result);
    }
    
    @Test
    @DisplayName("Should throw ProcessOrderException when trying to reprocess non-failed order")
    void shouldThrowProcessOrderExceptionWhenTryingToReprocessNonFailedOrder() {
        // Given
        when(orderRepository.findById("ORDER-001")).thenReturn(Optional.of(validOrder));
        
        // When/Then
        assertThatThrownBy(() -> useCase.reprocessOrder("ORDER-001"))
            .isInstanceOf(ProcessOrderUseCase.ProcessOrderException.class)
            .hasMessage("Failed to process order [ORDER-001]: Order cannot be reprocessed. Current status: RECEIVED");
        
        verify(orderRepository).findById("ORDER-001");
        verifyNoInteractions(distributionCenterService, cacheService, eventPublisher, selectionService);
    }
    
    @Test
    @DisplayName("Should handle unexpected exception during processing")
    void shouldHandleUnexpectedExceptionDuringProcessing() {
        // Given
        when(orderRepository.findById("ORDER-001")).thenReturn(Optional.of(validOrder));
        when(cacheService.get(any(String.class), eq(List.class)))
            .thenThrow(new RuntimeException("Unexpected cache error"));
        
        // When/Then
        assertThatThrownBy(() -> useCase.processOrder("ORDER-001"))
            .isInstanceOf(ProcessOrderUseCase.ProcessOrderException.class)
            .hasMessage("Failed to process order [ORDER-001]: Unexpected error during processing");
        
        verify(eventPublisher).publishOrderFailed(eq(validOrder), eq("Unexpected error during processing"), any(RuntimeException.class));
    }
}