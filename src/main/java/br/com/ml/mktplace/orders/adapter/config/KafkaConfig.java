package br.com.ml.mktplace.orders.adapter.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração do Kafka - Kafka Config
 * 
 * Configura:
 * - Producer para publicação de eventos
 * - Serialização JSON para eventos
 * - Configurações específicas por ambiente
 * - Topics para diferentes tipos de eventos
 * 
 * ADRs relacionados:
 * - ADR-007: Apache Kafka (AWS MSK) para eventos
 * - ADR-016: Configuração por arquivos de propriedades
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${app.kafka.producer.retries:3}")
    private int retries;

    @Value("${app.kafka.producer.batch-size:16384}")
    private int batchSize;

    @Value("${app.kafka.producer.linger-ms:10}")
    private int lingerMs;

    @Value("${app.kafka.producer.buffer-memory:33554432}")
    private long bufferMemory;

    @Value("${app.kafka.producer.acks:all}")
    private String acks;

    @Value("${app.kafka.producer.enable-idempotence:true}")
    private boolean enableIdempotence;

    /**
     * Configura a factory de producers Kafka.
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Configurações básicas
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Configurações de performance e confiabilidade
        configProps.put(ProducerConfig.ACKS_CONFIG, acks);
        configProps.put(ProducerConfig.RETRIES_CONFIG, retries);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);
        
        // Configurações de timeout
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        
        // Configurações de compressão
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        // Client ID para identificação
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, "orders-service-producer");
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Configura o template Kafka principal.
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());
        
        // Configurar default topic se necessário
        // template.setDefaultTopic("orders-events");
        
        return template;
    }

    /**
     * Template específico para eventos de pedidos.
     */
    @Bean("orderEventsKafkaTemplate")
    public KafkaTemplate<String, Object> orderEventsKafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());
        template.setDefaultTopic("orders.events");
        return template;
    }

    /**
     * Template específico para eventos de processamento.
     */
    @Bean("processingEventsKafkaTemplate")
    public KafkaTemplate<String, Object> processingEventsKafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());
        template.setDefaultTopic("orders.processing");
        return template;
    }

    /**
     * Configurações específicas para ambiente de desenvolvimento.
     */
    @Configuration
    @Profile("dev")
    static class DevelopmentKafkaConfig {
        
        @Bean
        public ProducerFactory<String, Object> developmentProducerFactory(
                @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers) {
            
            Map<String, Object> configProps = new HashMap<>();
            
            // Configurações simplificadas para desenvolvimento
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            
            // Configurações menos rigorosas para desenvolvimento
            configProps.put(ProducerConfig.ACKS_CONFIG, "1");
            configProps.put(ProducerConfig.RETRIES_CONFIG, 1);
            configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 1000);
            configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);
            configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
            
            configProps.put(ProducerConfig.CLIENT_ID_CONFIG, "orders-service-dev-producer");
            
            return new DefaultKafkaProducerFactory<>(configProps);
        }
    }

    /**
     * Configurações específicas para ambiente de staging.
     */
    @Configuration
    @Profile("staging")
    static class StagingKafkaConfig {
        
        @Bean
        public ProducerFactory<String, Object> stagingProducerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
                @Value("${spring.kafka.security.protocol:SASL_SSL}") String securityProtocol,
                @Value("${spring.kafka.sasl.mechanism:AWS_MSK_IAM}") String saslMechanism) {
            
            Map<String, Object> configProps = new HashMap<>();
            
            // Configurações básicas
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            
            // Configurações de segurança para AWS MSK
            configProps.put("security.protocol", securityProtocol);
            configProps.put("sasl.mechanism", saslMechanism);
            configProps.put("sasl.jaas.config", "software.amazon.msk.auth.iam.IAMLoginModule required;");
            configProps.put("sasl.client.callback.handler.class", 
                           "software.amazon.msk.auth.iam.IAMClientCallbackHandler");
            
            // Configurações de performance
            configProps.put(ProducerConfig.ACKS_CONFIG, "all");
            configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
            configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 8192);
            configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10);
            configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
            
            configProps.put(ProducerConfig.CLIENT_ID_CONFIG, "orders-service-staging-producer");
            
            return new DefaultKafkaProducerFactory<>(configProps);
        }
    }

    /**
     * Configurações específicas para ambiente de produção.
     */
    @Configuration
    @Profile("prod")
    static class ProductionKafkaConfig {
        
        @Bean
        public ProducerFactory<String, Object> productionProducerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
                @Value("${spring.kafka.security.protocol:SASL_SSL}") String securityProtocol,
                @Value("${spring.kafka.sasl.mechanism:AWS_MSK_IAM}") String saslMechanism) {
            
            Map<String, Object> configProps = new HashMap<>();
            
            // Configurações básicas
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            
            // Configurações de segurança para AWS MSK
            configProps.put("security.protocol", securityProtocol);
            configProps.put("sasl.mechanism", saslMechanism);
            configProps.put("sasl.jaas.config", "software.amazon.msk.auth.iam.IAMLoginModule required;");
            configProps.put("sasl.client.callback.handler.class", 
                           "software.amazon.msk.auth.iam.IAMClientCallbackHandler");
            
            // Configurações otimizadas para produção
            configProps.put(ProducerConfig.ACKS_CONFIG, "all");
            configProps.put(ProducerConfig.RETRIES_CONFIG, 5);
            configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768);
            configProps.put(ProducerConfig.LINGER_MS_CONFIG, 20);
            configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 67108864L);
            configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
            
            // Configurações de timeout otimizadas
            configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 60000);
            configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 300000);
            
            // Compressão mais eficiente para produção
            configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
            
            configProps.put(ProducerConfig.CLIENT_ID_CONFIG, "orders-service-prod-producer");
            
            return new DefaultKafkaProducerFactory<>(configProps);
        }
    }
}