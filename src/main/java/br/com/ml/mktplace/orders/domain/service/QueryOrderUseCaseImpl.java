package br.com.ml.mktplace.orders.domain.service;

import br.com.ml.mktplace.orders.domain.model.Order;
import br.com.ml.mktplace.orders.domain.model.OrderNotFoundException;
import br.com.ml.mktplace.orders.domain.model.OrderStatus;
import br.com.ml.mktplace.orders.domain.port.QueryOrderUseCase;
import br.com.ml.mktplace.orders.domain.port.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of QueryOrderUseCase following hexagonal architecture principles.
 * Handles order queries and search operations.
 */
@Service
@Transactional(readOnly = true)
public class QueryOrderUseCaseImpl implements QueryOrderUseCase {

    private final OrderRepository orderRepository;

    public QueryOrderUseCaseImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Optional<Order> getOrderById(String orderId) {
        validateOrderId(orderId);
        return orderRepository.findById(orderId);
    }

    @Override
    public Order getOrderByIdRequired(String orderId) {
        validateOrderId(orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }


    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        return orderRepository.findAll().stream()
                .filter(order -> status.equals(order.getStatus()))
                .toList();
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public boolean orderExists(String orderId) {
        validateOrderId(orderId);
        return orderRepository.existsById(orderId);
    }

    @Override
    public OrderSearchResult searchOrders(OrderSearchCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Search criteria cannot be null");
        }

        // For simplicity in this implementation, we'll simulate pagination
        // In a real implementation, this would use proper database pagination
        List<Order> allOrders = orderRepository.findAll();
        
        // Apply filters
        List<Order> filteredOrders = allOrders.stream()
                .filter(order -> matchesCriteria(order, criteria))
                .toList();
        
        // Apply pagination
        int totalElements = filteredOrders.size();
        int totalPages = (int) Math.ceil((double) totalElements / criteria.size());
        int startIndex = criteria.page() * criteria.size();
        int endIndex = Math.min(startIndex + criteria.size(), totalElements);
        
        List<Order> pageOrders = filteredOrders.subList(startIndex, endIndex);
        
        return new OrderSearchResult(
                pageOrders,
                totalElements,
                totalPages,
                criteria.page(),
                criteria.size()
        );
    }
    
    private boolean matchesCriteria(Order order, OrderSearchCriteria criteria) {
        // Check customer ID filter
        if (criteria.customerId() != null && !criteria.customerId().equals(order.getCustomerId())) {
            return false;
        }
        
        // Check status filter
        if (criteria.status() != null && !criteria.status().equals(order.getStatus())) {
            return false;
        }
        
        // Check item ID filter
        if (criteria.itemId() != null) {
            boolean hasItem = order.getItems().stream()
                    .anyMatch(item -> criteria.itemId().equals(item.getItemId()));
            if (!hasItem) {
                return false;
            }
        }
        
        return true;
    }
    
    private void validateOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
    }
    
}