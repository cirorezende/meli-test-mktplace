package br.com.ml.mktplace.orders.domain.service;

import br.com.ml.mktplace.orders.domain.model.*;
import br.com.ml.mktplace.orders.domain.port.CreateOrderUseCase;
import br.com.ml.mktplace.orders.domain.port.OrderRepository;
import br.com.ml.mktplace.orders.domain.port.EventPublisher;
import br.com.ml.mktplace.orders.domain.port.IDGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of CreateOrderUseCase following hexagonal architecture principles.
 * Handles order creation, validation, persistence, and event publishing.
 */
@Service
@Transactional
public class CreateOrderUseCaseImpl implements CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;
    private final IDGenerator idGenerator;

    public CreateOrderUseCaseImpl(
            OrderRepository orderRepository,
            EventPublisher eventPublisher,
            IDGenerator idGenerator) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.idGenerator = idGenerator;
    }

    @Override
    public Order createOrder(String customerId, List<OrderItem> items, Address deliveryAddress) {
        // Validate inputs
        validateInputs(customerId, items, deliveryAddress);
        
        // Generate order ID  
        String orderId = idGenerator.generate();
        
        // Create order
        Order order = new Order(orderId, customerId, items, deliveryAddress, OrderStatus.RECEIVED, java.time.Instant.now());
        
        // Persist order
        Order savedOrder = orderRepository.save(order);
        
        // Publish order created event
        eventPublisher.publishOrderCreated(savedOrder);
        
        return savedOrder;
    }
    
    private void validateInputs(String customerId, List<OrderItem> items, Address deliveryAddress) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        
        if (items.size() > 100) {
            throw new IllegalArgumentException("Order cannot have more than 100 items");
        }
        
        if (deliveryAddress == null) {
            throw new IllegalArgumentException("Delivery address is required");
        }
        
        // Validate no duplicate products
        long distinctProducts = items.stream()
            .map(OrderItem::getItemId)
            .distinct()
            .count();
            
        if (distinctProducts != items.size()) {
            throw new IllegalArgumentException("Order cannot have duplicate products");
        }
    }
}