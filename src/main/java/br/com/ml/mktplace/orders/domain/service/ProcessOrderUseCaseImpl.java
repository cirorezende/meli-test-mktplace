package br.com.ml.mktplace.orders.domain.service;

import br.com.ml.mktplace.orders.domain.model.*;
import br.com.ml.mktplace.orders.adapter.config.metrics.ObservabilityMetrics;
import br.com.ml.mktplace.orders.domain.port.ProcessOrderUseCase;
import br.com.ml.mktplace.orders.domain.port.OrderRepository;
import br.com.ml.mktplace.orders.domain.port.DistributionCenterService;
import br.com.ml.mktplace.orders.domain.port.CacheService;
import br.com.ml.mktplace.orders.domain.port.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of ProcessOrderUseCase following hexagonal architecture principles.
 * Handles order processing, distribution center selection, and order updating.
 */
@Service
@Transactional
public class ProcessOrderUseCaseImpl implements ProcessOrderUseCase {

    private final OrderRepository orderRepository;
    private final DistributionCenterService distributionCenterService;
    private final CacheService cacheService;
    private final EventPublisher eventPublisher;
    private final DistributionCenterSelectionService selectionService;
    private final ObservabilityMetrics observabilityMetrics;
    private static final Logger log = LoggerFactory.getLogger(ProcessOrderUseCaseImpl.class);

    public ProcessOrderUseCaseImpl(
            OrderRepository orderRepository,
            DistributionCenterService distributionCenterService,
            CacheService cacheService,
            EventPublisher eventPublisher,
            DistributionCenterSelectionService selectionService,
            ObservabilityMetrics observabilityMetrics) {
        this.orderRepository = orderRepository;
        this.distributionCenterService = distributionCenterService;
        this.cacheService = cacheService;
        this.eventPublisher = eventPublisher;
        this.selectionService = selectionService;
        this.observabilityMetrics = observabilityMetrics;
    }

    @Override
    public Order processOrder(String orderId) {
        validateOrderId(orderId);
        
        try {
            // Find order
            Order order = findOrder(orderId);
            
            // Check if already processed
            if (order.isInFinalState()) {
                throw new ProcessOrderException(orderId, "Order is already in final state: " + order.getStatus());
            }
            
            // Process order
            return observabilityMetrics.recordProcessing(() -> performOrderProcessing(order));
            
        } catch (OrderNotFoundException | ProcessOrderException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = "Unexpected error during processing";
            eventPublisher.publishOrderFailed(findOrderSafely(orderId), errorMessage, e);
            throw new ProcessOrderException(orderId, errorMessage, e);
        }
    }

    @Override
    public Order reprocessOrder(String orderId) {
        validateOrderId(orderId);
        
        try {
            // Find order
            Order order = findOrder(orderId);
            
            // Check if can be reprocessed
            if (order.getStatus() != OrderStatus.FAILED) {
                throw new ProcessOrderException(orderId, "Order cannot be reprocessed. Current status: " + order.getStatus());
            }
            
            // Reset order status
            order.changeStatus(OrderStatus.RECEIVED);
            Order updatedOrder = orderRepository.save(order);
            
            // Process order
            return observabilityMetrics.recordProcessing(() -> performOrderProcessing(updatedOrder));
            
        } catch (OrderNotFoundException | ProcessOrderException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = "Unexpected error during reprocessing";
            eventPublisher.publishOrderFailed(findOrderSafely(orderId), errorMessage, e);
            throw new ProcessOrderException(orderId, errorMessage, e);
        }
    }
    
    private Order performOrderProcessing(Order order) {
        try {
            // Change status to processing
            order.changeStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
            
            // Get distribution centers (with cache)
            List<DistributionCenter> availableCenters = getAvailableDistributionCenters(order.getDeliveryAddress());
            
            if (availableCenters.isEmpty()) {
                throw new ExternalServiceException("DistributionCenterService", "No distribution centers available for processing");
            }
            
            // Assign distribution centers to items
            int itemsProcessed = 0;
            int itemsFailed = 0;
            observabilityMetrics.recordItemsPerOrder(order.getItems().size());

            for (OrderItem item : order.getItems()) {
                try {
                    DistributionCenter selectedCenter = selectionService.selectDistributionCenter(
                        availableCenters, 
                        order.getDeliveryAddress()
                    );
                    item.assignDistributionCenter(selectedCenter);
                    observabilityMetrics.incrementDcSelection(selectedCenter.code());
                    log.info("CD selecionado para item {} pedido {} -> {}", item.getItemId(), order.getId(), selectedCenter.code());
                    itemsProcessed++;
                } catch (Exception e) {
                    // Log error but continue with other items
                    itemsFailed++;
                    log.warn("Falha ao atribuir CD para item {} do pedido {}: {}", item.getItemId(), order.getId(), e.getMessage());
                }
            }
            
            // Update order status based on results
            OrderStatus finalStatus;
            if (itemsProcessed == 0) {
                finalStatus = OrderStatus.FAILED;
            } else if (itemsFailed > 0) {
                finalStatus = OrderStatus.PROCESSING; // Partial success
            } else {
                finalStatus = OrderStatus.PROCESSED; // Full success
            }
            
            order.changeStatus(finalStatus);
            Order processedOrder = orderRepository.save(order);
            
            // Publish events
            if (finalStatus == OrderStatus.PROCESSED) {
                eventPublisher.publishOrderProcessed(processedOrder);
            } else if (finalStatus == OrderStatus.FAILED) {
                eventPublisher.publishOrderFailed(processedOrder, "Failed to process any items");
            }
            
            return processedOrder;
            
        } catch (ExternalServiceException e) {
            // Handle external service failures
            order.changeStatus(OrderStatus.FAILED);
            Order failedOrder = orderRepository.save(order);
            eventPublisher.publishOrderFailed(failedOrder, "Distribution center service unavailable", e);
            throw new ProcessOrderException(order.getId(), "Distribution center service unavailable", e);
        }
    }
    
    private List<DistributionCenter> getAvailableDistributionCenters(Address deliveryAddress) {
        // Try cache first
        String cacheKey = "distribution-centers:" + deliveryAddress.state();
        
        @SuppressWarnings("rawtypes")
        Optional<List> cachedCentersOpt = cacheService.get(cacheKey, List.class);
        @SuppressWarnings("unchecked")
        List<DistributionCenter> cachedCenters = cachedCentersOpt.map(list -> (List<DistributionCenter>) list).orElse(null);
        
        if (cachedCenters != null) {
            return cachedCenters;
        }
        
        // Get from service and cache result
        List<DistributionCenter> centers = distributionCenterService.findAllDistributionCenters();
        cacheService.put(cacheKey, centers, java.time.Duration.ofMinutes(5));
        
        return centers;
    }
    
    private void validateOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
    }
    
    private Order findOrder(String orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new OrderNotFoundException(orderId);
        }
        return orderOpt.get();
    }
    
    private Order findOrderSafely(String orderId) {
        try {
            return findOrder(orderId);
        } catch (OrderNotFoundException e) {
            // Return null if order not found (for event publishing)
            return null;
        }
    }
}