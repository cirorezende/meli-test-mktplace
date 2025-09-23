package br.com.ml.mktplace.orders.adapter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Configuração do cache Redis - Cache Config
 * 
 * Configura:
 * - RedisTemplate com serialização JSON
 * - TTL padrão de 8 horas conforme ADR-010
 * - Connection pool otimizado
 * - Configurações específicas por ambiente
 * 
 * ADRs relacionados:
 * - ADR-010: Cache distribuído (Redis)
 * - ADR-016: Configuração por arquivos de propriedades
 */
@Configuration
public class CacheConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @Value("${app.cache.ttl-hours:8}")
    private int cacheTtlHours;

    @Value("${app.cache.pool.max-total:20}")
    private int maxTotal;

    @Value("${app.cache.pool.max-idle:8}")
    private int maxIdle;

    @Value("${app.cache.pool.min-idle:2}")
    private int minIdle;

    @Value("${app.cache.timeout:5000}")
    private long timeout;

    /**
     * Configura a connection factory para Redis usando Lettuce.
     * Lettuce é thread-safe e assíncrono, melhor que Jedis para aplicações Spring Boot.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setDatabase(redisDatabase);
        
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            config.setPassword(redisPassword);
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.setValidateConnection(true);
        
        return factory;
    }

    /**
     * Configura o RedisTemplate principal com serialização JSON.
     * 
     * Configurações:
     * - Chaves: String (para facilitar debug e monitoramento)
     * - Valores: JSON (para flexibilidade e legibilidade)
     * - Hash keys/values: String/JSON
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Serializers
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        
        // Key serializers (sempre String para facilitar debug)
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // Value serializers (JSON para flexibilidade)
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        // Configurações adicionais
        template.setEnableTransactionSupport(false); // Cache não precisa de transações
        template.setExposeConnection(false); // Segurança
        template.setEnableDefaultSerializer(true);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Bean com TTL padrão configurável.
     * Retorna a duração padrão para cache conforme ADR-010 (8 horas).
     */
    @Bean
    public Duration defaultCacheTtl() {
        return Duration.ofHours(cacheTtlHours);
    }

    /**
     * Configurações específicas para ambiente de desenvolvimento.
     */
    @Configuration
    @Profile("dev")
    static class DevelopmentCacheConfig {
        
        @Bean
        public Duration developmentCacheTtl() {
            // TTL menor para desenvolvimento (30 minutos)
            return Duration.ofMinutes(30);
        }
    }

    /**
     * Configurações específicas para ambiente de produção.
     */
    @Configuration
    @Profile("prod")
    static class ProductionCacheConfig {
        
        @Bean
        public RedisConnectionFactory productionRedisConnectionFactory(
                @Value("${spring.redis.cluster.nodes:}") String clusterNodes,
                @Value("${spring.redis.host}") String host,
                @Value("${spring.redis.port}") int port,
                @Value("${spring.redis.password}") String password) {
            
            // Em produção, configurar cluster se disponível
            if (clusterNodes != null && !clusterNodes.trim().isEmpty()) {
                // TODO: Implementar RedisClusterConfiguration quando necessário
                // Para agora, usar standalone mesmo em produção
            }
            
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(host);
            config.setPort(port);
            config.setPassword(password);
            
            LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
            factory.setValidateConnection(true);
            
            return factory;
        }
        
        @Bean
        public Duration productionCacheTtl() {
            // TTL completo para produção (8 horas)
            return Duration.ofHours(8);
        }
    }
}