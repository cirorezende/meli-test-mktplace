package br.com.ml.mktplace.orders.adapter.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Testes unitários para CacheConfig.
 * 
 * Verifica a configuração do Redis, RedisTemplate e TTL.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CacheConfig Tests")
class CacheConfigTest {

    private CacheConfig cacheConfig;

    @BeforeEach
    void setUp() {
        cacheConfig = new CacheConfig();
        
        // Set test properties using reflection
        ReflectionTestUtils.setField(cacheConfig, "redisHost", "localhost");
        ReflectionTestUtils.setField(cacheConfig, "redisPort", 6379);
        ReflectionTestUtils.setField(cacheConfig, "redisPassword", "");
        ReflectionTestUtils.setField(cacheConfig, "redisDatabase", 0);
        ReflectionTestUtils.setField(cacheConfig, "cacheTtlHours", 8);
        ReflectionTestUtils.setField(cacheConfig, "maxTotal", 20);
        ReflectionTestUtils.setField(cacheConfig, "maxIdle", 8);
        ReflectionTestUtils.setField(cacheConfig, "minIdle", 2);
        ReflectionTestUtils.setField(cacheConfig, "timeout", 5000L);
    }

    @Test
    @DisplayName("Should create RedisConnectionFactory with correct configuration")
    void shouldCreateRedisConnectionFactoryWithCorrectConfiguration() {
        // When
        RedisConnectionFactory connectionFactory = cacheConfig.redisConnectionFactory();
        
        // Then
        assertNotNull(connectionFactory);
        assertThat(connectionFactory).isInstanceOf(LettuceConnectionFactory.class);
        
        LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) connectionFactory;
        RedisStandaloneConfiguration config = lettuceFactory.getStandaloneConfiguration();
        
        assertThat(config.getHostName()).isEqualTo("localhost");
        assertThat(config.getPort()).isEqualTo(6379);
        assertThat(config.getDatabase()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should create RedisTemplate with correct serializers")
    void shouldCreateRedisTemplateWithCorrectSerializers() {
        // Given
        RedisConnectionFactory connectionFactory = cacheConfig.redisConnectionFactory();
        
        // When
        RedisTemplate<String, Object> redisTemplate = cacheConfig.redisTemplate(connectionFactory);
        
        // Then
        assertNotNull(redisTemplate);
        assertThat(redisTemplate.getConnectionFactory()).isSameAs(connectionFactory);
        assertThat(redisTemplate.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(redisTemplate.getValueSerializer()).isInstanceOf(GenericJackson2JsonRedisSerializer.class);
        assertThat(redisTemplate.getHashKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(redisTemplate.getHashValueSerializer()).isInstanceOf(GenericJackson2JsonRedisSerializer.class);
    }

    @Test
    @DisplayName("Should create String RedisTemplate with String serializers")
    void shouldCreateStringRedisTemplateWithStringSerializers() {
        // Given
        RedisConnectionFactory connectionFactory = cacheConfig.redisConnectionFactory();
        
        // When
        RedisTemplate<String, String> stringTemplate = cacheConfig.stringRedisTemplate(connectionFactory);
        
        // Then
        assertNotNull(stringTemplate);
        assertThat(stringTemplate.getConnectionFactory()).isSameAs(connectionFactory);
        assertThat(stringTemplate.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(stringTemplate.getValueSerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(stringTemplate.getHashKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(stringTemplate.getHashValueSerializer()).isInstanceOf(StringRedisSerializer.class);
    }

    @Test
    @DisplayName("Should create default cache TTL")
    void shouldCreateDefaultCacheTtl() {
        // When
        Duration ttl = cacheConfig.defaultCacheTtl();
        
        // Then
        assertNotNull(ttl);
        assertThat(ttl).isEqualTo(Duration.ofHours(8));
    }

    @Test
    @DisplayName("Should handle empty password gracefully")
    void shouldHandleEmptyPasswordGracefully() {
        // Given
        ReflectionTestUtils.setField(cacheConfig, "redisPassword", "");
        
        // When
        RedisConnectionFactory connectionFactory = cacheConfig.redisConnectionFactory();
        
        // Then
        assertNotNull(connectionFactory);
        LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) connectionFactory;
        RedisStandaloneConfiguration config = lettuceFactory.getStandaloneConfiguration();
        // For empty password, Spring creates a RedisPassword.none()
        assertThat(config.getPassword()).isEqualTo(RedisPassword.none());
    }

    @Test
    @DisplayName("Should handle null password gracefully")
    void shouldHandleNullPasswordGracefully() {
        // Given
        ReflectionTestUtils.setField(cacheConfig, "redisPassword", null);
        
        // When
        RedisConnectionFactory connectionFactory = cacheConfig.redisConnectionFactory();
        
        // Then
        assertNotNull(connectionFactory);
    }

    @Test
    @DisplayName("Should configure Redis with password when provided")
    void shouldConfigureRedisWithPasswordWhenProvided() {
        // Given
        ReflectionTestUtils.setField(cacheConfig, "redisPassword", "mypassword");
        
        // When
        RedisConnectionFactory connectionFactory = cacheConfig.redisConnectionFactory();
        
        // Then
        assertNotNull(connectionFactory);
        LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) connectionFactory;
        RedisStandaloneConfiguration config = lettuceFactory.getStandaloneConfiguration();
        assertThat(config.getPassword()).isNotNull();
    }

    @Test
    @DisplayName("Should configure connection validation")
    void shouldConfigureConnectionValidation() {
        // When
        RedisConnectionFactory connectionFactory = cacheConfig.redisConnectionFactory();
        
        // Then
        assertNotNull(connectionFactory);
        LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) connectionFactory;
        assertThat(lettuceFactory.getValidateConnection()).isTrue();
    }

    @Test
    @DisplayName("Should create development cache TTL")
    void shouldCreateDevelopmentCacheTtl() {
        // Given
        CacheConfig.DevelopmentCacheConfig devConfig = new CacheConfig.DevelopmentCacheConfig();
        
        // When
        Duration ttl = devConfig.developmentCacheTtl();
        
        // Then
        assertNotNull(ttl);
        assertThat(ttl).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    @DisplayName("Should create production cache TTL")
    void shouldCreateProductionCacheTtl() {
        // Given
        CacheConfig.ProductionCacheConfig prodConfig = new CacheConfig.ProductionCacheConfig();
        
        // When
        Duration ttl = prodConfig.productionCacheTtl();
        
        // Then
        assertNotNull(ttl);
        assertThat(ttl).isEqualTo(Duration.ofHours(8));
    }

    @Test
    @DisplayName("Should configure RedisTemplate with default serializer enabled")
    void shouldConfigureRedisTemplateWithDefaultSerializerEnabled() {
        // Given
        RedisConnectionFactory connectionFactory = cacheConfig.redisConnectionFactory();
        
        // When
        RedisTemplate<String, Object> redisTemplate = cacheConfig.redisTemplate(connectionFactory);
        
        // Then
        assertThat(redisTemplate.isEnableDefaultSerializer()).isTrue();
    }

    @Test
    @DisplayName("Should configure different TTL values for different environments")
    void shouldConfigureDifferentTtlValuesForDifferentEnvironments() {
        // Given
        CacheConfig.DevelopmentCacheConfig devConfig = new CacheConfig.DevelopmentCacheConfig();
        CacheConfig.ProductionCacheConfig prodConfig = new CacheConfig.ProductionCacheConfig();
        
        // When
        Duration devTtl = devConfig.developmentCacheTtl();
        Duration prodTtl = prodConfig.productionCacheTtl();
        Duration defaultTtl = cacheConfig.defaultCacheTtl();
        
        // Then
        assertThat(devTtl).isEqualTo(Duration.ofMinutes(30));
        assertThat(prodTtl).isEqualTo(Duration.ofHours(8));
        assertThat(defaultTtl).isEqualTo(Duration.ofHours(8));
        
        // Development should have shorter TTL than production
        assertThat(devTtl).isLessThan(prodTtl);
    }
}