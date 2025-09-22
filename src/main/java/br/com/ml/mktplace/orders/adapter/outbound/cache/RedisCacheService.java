package br.com.ml.mktplace.orders.adapter.outbound.cache;

import br.com.ml.mktplace.orders.domain.port.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * Redis implementation of CacheService.
 * Uses Redis for caching generic objects to improve performance.
 */
@Component
public class RedisCacheService implements CacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheService.class);
    private static final Duration DEFAULT_TTL = Duration.ofHours(8); // ADR-010
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public RedisCacheService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public <T> Optional<T> get(String key, Class<T> valueType) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache key cannot be null or empty");
        }
        if (valueType == null) {
            throw new IllegalArgumentException("Value type cannot be null");
        }
        
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            
            if (cached == null) {
                logger.debug("Cache miss for key: {}", key);
                return Optional.empty();
            }
            
            T value;
            if (valueType.isInstance(cached)) {
                value = valueType.cast(cached);
            } else if (cached instanceof String) {
                // Try to deserialize from JSON
                value = objectMapper.readValue((String) cached, valueType);
            } else {
                // Convert using ObjectMapper
                value = objectMapper.convertValue(cached, valueType);
            }
            
            logger.debug("Cache hit for key: {}", key);
            return Optional.of(value);
            
        } catch (Exception e) {
            logger.error("Failed to get value from cache for key: {}", key, e);
            return Optional.empty();
        }
    }
    
    @Override
    public <T> void put(String key, T value, Duration ttl) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache key cannot be null or empty");
        }
        if (ttl != null && ttl.isNegative()) {
            throw new IllegalArgumentException("TTL cannot be negative");
        }
        
        if (value == null) {
            logger.debug("Skipping cache put for null value with key: {}", key);
            return;
        }
        
        try {
            Duration actualTtl = ttl != null ? ttl : DEFAULT_TTL;
            redisTemplate.opsForValue().set(key, value, actualTtl);
            logger.debug("Cached value for key: {} with TTL: {}", key, actualTtl);
            
        } catch (Exception e) {
            logger.error("Failed to cache value for key: {}", key, e);
            // Don't throw exception - cache failures should not break the flow
        }
    }
    
    @Override
    public <T> void put(String key, T value) {
        put(key, value, DEFAULT_TTL);
    }
    
    @Override
    public void evict(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache key cannot be null or empty");
        }
        
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                logger.debug("Evicted key from cache: {}", key);
            } else {
                logger.debug("Key not found in cache for eviction: {}", key);
            }
            
        } catch (Exception e) {
            logger.error("Failed to evict key from cache: {}", key, e);
            // Don't throw exception - cache failures should not break the flow
        }
    }
    
    @Override
    public void evictByPattern(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache pattern cannot be null or empty");
        }
        
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                logger.info("Evicted {} keys matching pattern: {}", deleted, pattern);
            } else {
                logger.debug("No keys found matching pattern: {}", pattern);
            }
            
        } catch (Exception e) {
            logger.error("Failed to evict keys by pattern: {}", pattern, e);
            // Don't throw exception - cache failures should not break the flow
        }
    }
    
    @Override
    public void clear() {
        try {
            Set<String> keys = redisTemplate.keys("*");
            
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                logger.info("Cleared {} keys from cache", deleted);
            } else {
                logger.debug("Cache is already empty");
            }
            
        } catch (Exception e) {
            logger.error("Failed to clear cache", e);
            // Don't throw exception - cache failures should not break the flow
        }
    }
    
    @Override
    public boolean exists(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache key cannot be null or empty");
        }
        
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
            
        } catch (Exception e) {
            logger.error("Failed to check if key exists in cache: {}", key, e);
            return false;
        }
    }
}