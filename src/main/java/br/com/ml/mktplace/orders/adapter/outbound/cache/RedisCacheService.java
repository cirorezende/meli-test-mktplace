package br.com.ml.mktplace.orders.adapter.outbound.cache;

import br.com.ml.mktplace.orders.domain.port.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import br.com.ml.mktplace.orders.adapter.config.metrics.ObservabilityMetrics;
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
    private final ObservabilityMetrics observabilityMetrics;
    
    @Autowired
    public RedisCacheService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper, ObservabilityMetrics observabilityMetrics) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.observabilityMetrics = observabilityMetrics;
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
                observabilityMetrics.cacheMiss("redis");
                return Optional.empty();
            } else {
                observabilityMetrics.cacheHit("redis");
            }
            
            T value;
            // Specialized handling for collection / array structures to avoid Jackson default typing pitfalls
            if (valueType == java.util.List.class) {
                // Special handling for List values (we serialize them explicitly as JSON String to avoid polymorphic typing issues)
                if (cached instanceof java.util.List<?>) {
                    @SuppressWarnings("unchecked")
                    T casted = (T) cached;
                    value = casted;
                } else if (cached instanceof String s) {
                    // Parse JSON array -> List
                    @SuppressWarnings("unchecked")
                    T parsed = (T) objectMapper.readValue(s, java.util.List.class);
                    value = parsed;
                } else {
                    @SuppressWarnings("unchecked")
                    T converted = (T) objectMapper.convertValue(cached, java.util.List.class);
                    value = converted;
                }
            } else if (valueType.isArray()) {
                Class<?> componentType = valueType.getComponentType();
                Object arrayValue;
                if (cached instanceof String s) {
                    // Direct JSON -> array
                    arrayValue = objectMapper.readValue(s, valueType);
                } else if (cached != null && cached.getClass().isArray()) {
                    arrayValue = cached; // Already an array of something
                } else if (cached instanceof java.util.Collection<?> coll) {
                    // Convert collection elements to desired component type
                    Object[] intermediate = coll.toArray();
                    Object newArray = java.lang.reflect.Array.newInstance(componentType, intermediate.length);
                    for (int i = 0; i < intermediate.length; i++) {
                        Object elem = intermediate[i];
                        if (elem != null && !componentType.isInstance(elem)) {
                            elem = objectMapper.convertValue(elem, componentType);
                        }
                        java.lang.reflect.Array.set(newArray, i, elem);
                    }
                    arrayValue = newArray;
                } else {
                    // Fallback convert
                    arrayValue = objectMapper.convertValue(cached, valueType);
                }
                @SuppressWarnings("unchecked")
                T casted = (T) arrayValue;
                value = casted;
            } else if (valueType.isInstance(cached)) {
                value = valueType.cast(cached);
            } else if (cached instanceof String s) {
                value = objectMapper.readValue(s, valueType);
            } else {
                value = objectMapper.convertValue(cached, valueType);
            }

            logger.debug("Cache hit for key: {} (type={})", key, valueType.getSimpleName());
            return Optional.ofNullable(value);
            
        } catch (Exception e) {
            logger.warn("Cache deserialization failed for key {} - evicting and returning empty. Cause: {}", key, e.getMessage());
            try { redisTemplate.delete(key); } catch (Exception ignored) { }
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

            if (value instanceof java.util.List<?> list) {
                // Serialize list explicitly as JSON String to avoid GenericJackson2JsonRedisSerializer polymorphic array issues
                String json = objectMapper.writeValueAsString(list);
                redisTemplate.opsForValue().set(key, json, actualTtl);
                logger.debug("Cached LIST value for key: {} ({} elements) with TTL: {}", key, list.size(), actualTtl);
            } else if (value.getClass().isArray()) {
                // Serialize arrays explicitly as JSON as well (e.g., String[])
                String json = objectMapper.writeValueAsString(value);
                int length = java.lang.reflect.Array.getLength(value);
                redisTemplate.opsForValue().set(key, json, actualTtl);
                logger.debug("Cached ARRAY value for key: {} ({} elements, type={}[]) with TTL: {}", key, length, value.getClass().getComponentType().getSimpleName(), actualTtl);
            } else {
                redisTemplate.opsForValue().set(key, value, actualTtl);
                logger.debug("Cached value for key: {} with TTL: {}", key, actualTtl);
            }
            
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