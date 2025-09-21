package br.com.ml.mktplace.orders.adapter.outbound.cache;

import br.com.ml.mktplace.orders.domain.model.Order;
import br.com.ml.mktplace.orders.domain.port.OrderCacheRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis implementation of OrderCacheRepository.
 * Uses Redis for caching orders to improve performance.
 */
@Component
public class RedisOrderCacheRepository implements OrderCacheRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisOrderCacheRepository.class);
    private static final String ORDER_CACHE_KEY_PREFIX = "order:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(15);
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public RedisOrderCacheRepository(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void cache(Order order) {
        if (order == null || order.getId() == null) {
            logger.warn("Cannot cache order: order or order ID is null");
            return;
        }
        
        try {
            String key = buildKey(order.getId());
            String serializedOrder = objectMapper.writeValueAsString(order);
            
            redisTemplate.opsForValue().set(key, serializedOrder, DEFAULT_TTL);
            logger.debug("Cached order with ID: {}", order.getId());
            
        } catch (Exception e) {
            logger.error("Failed to cache order with ID: {}", order.getId(), e);
            // Don't throw exception - cache failures should not break the flow
        }
    }
    
    @Override
    public Optional<Order> getCachedOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            logger.warn("Cannot retrieve cached order: order ID is null or empty");
            return Optional.empty();
        }
        
        try {
            String key = buildKey(orderId);
            String cachedOrder = redisTemplate.opsForValue().get(key);
            
            if (cachedOrder == null) {
                logger.debug("Order not found in cache: {}", orderId);
                return Optional.empty();
            }
            
            Order order = objectMapper.readValue(cachedOrder, Order.class);
            logger.debug("Retrieved order from cache: {}", orderId);
            return Optional.of(order);
            
        } catch (Exception e) {
            logger.error("Failed to retrieve cached order with ID: {}", orderId, e);
            return Optional.empty();
        }
    }
    
    @Override
    public void evict(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            logger.warn("Cannot evict order: order ID is null or empty");
            return;
        }
        
        try {
            String key = buildKey(orderId);
            Boolean deleted = redisTemplate.delete(key);
            
            if (Boolean.TRUE.equals(deleted)) {
                logger.debug("Evicted order from cache: {}", orderId);
            } else {
                logger.debug("Order not found in cache for eviction: {}", orderId);
            }
            
        } catch (Exception e) {
            logger.error("Failed to evict order from cache: {}", orderId, e);
            // Don't throw exception - cache failures should not break the flow
        }
    }
    
    @Override
    public void evictAll() {
        try {
            String pattern = ORDER_CACHE_KEY_PREFIX + "*";
            var keys = redisTemplate.keys(pattern);
            
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.info("Evicted {} orders from cache", keys.size());
            } else {
                logger.debug("No orders found in cache for eviction");
            }
            
        } catch (Exception e) {
            logger.error("Failed to evict all orders from cache", e);
            // Don't throw exception - cache failures should not break the flow
        }
    }
    
    @Override
    public boolean exists(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return false;
        }
        
        try {
            String key = buildKey(orderId);
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
            
        } catch (Exception e) {
            logger.error("Failed to check if order exists in cache: {}", orderId, e);
            return false;
        }
    }
    
    private String buildKey(String orderId) {
        return ORDER_CACHE_KEY_PREFIX + orderId;
    }
}