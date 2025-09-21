package br.com.ml.mktplace.orders.adapter.outbound.persistence;

import br.com.ml.mktplace.orders.adapter.outbound.persistence.entity.OrderEntity;
import br.com.ml.mktplace.orders.adapter.outbound.persistence.mapper.OrderEntityMapper;
import br.com.ml.mktplace.orders.adapter.outbound.persistence.repository.JpaOrderEntityRepository;
import br.com.ml.mktplace.orders.domain.model.Order;
import br.com.ml.mktplace.orders.domain.model.OrderNotFoundException;
import br.com.ml.mktplace.orders.domain.port.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of OrderRepository port.
 * Translates between domain objects and JPA entities.
 */
@Component
public class JpaOrderRepository implements OrderRepository {

    private final JpaOrderEntityRepository jpaRepository;
    private final OrderEntityMapper mapper;

    public JpaOrderRepository(JpaOrderEntityRepository jpaRepository, OrderEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        OrderEntity entity = mapper.toEntity(order);
        OrderEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> findById(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        
        return jpaRepository.findById(orderId)
                .map(mapper::toDomain);
    }

    @Override
    public Order getById(String orderId) {
        return findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        
        List<OrderEntity> entities = jpaRepository.findByCustomerId(customerId);
        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findAll() {
        List<OrderEntity> entities = jpaRepository.findAll();
        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        
        return jpaRepository.existsById(orderId);
    }

    /**
     * Additional method using PostGIS spatial queries.
     * Find orders near a specific location within a radius.
     */
    public List<Order> findOrdersNearLocation(Double latitude, Double longitude, Double radiusMeters) {
        List<OrderEntity> entities = jpaRepository.findOrdersNearLocation(latitude, longitude, radiusMeters);
        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }
}