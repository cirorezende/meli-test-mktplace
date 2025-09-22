package br.com.ml.mktplace.orders.adapter.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Testes unitários para KafkaConfig.
 * 
 * Verifica a configuração do Kafka Producer, templates e serialização.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaConfig Tests")
class KafkaConfigTest {

    private KafkaConfig kafkaConfig;

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaConfig();
        
        // Set test properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(kafkaConfig, "retries", 3);
        ReflectionTestUtils.setField(kafkaConfig, "batchSize", 16384);
        ReflectionTestUtils.setField(kafkaConfig, "lingerMs", 10);
        ReflectionTestUtils.setField(kafkaConfig, "bufferMemory", 33554432L);
        ReflectionTestUtils.setField(kafkaConfig, "acks", "all");
        ReflectionTestUtils.setField(kafkaConfig, "enableIdempotence", true);
    }

    @Test
    @DisplayName("Should create ProducerFactory with correct configuration")
    void shouldCreateProducerFactoryWithCorrectConfiguration() {
        // When
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();
        
        // Then
        assertNotNull(producerFactory);
        assertThat(producerFactory).isInstanceOf(DefaultKafkaProducerFactory.class);
        
        // Verify configuration properties
        Map<String, Object> configProps = producerFactory.getConfigurationProperties();
        assertThat(configProps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");
        assertThat(configProps.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)).isEqualTo(StringSerializer.class);
        assertThat(configProps.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)).isEqualTo(JsonSerializer.class);
        assertThat(configProps.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
        assertThat(configProps.get(ProducerConfig.RETRIES_CONFIG)).isEqualTo(3);
        assertThat(configProps.get(ProducerConfig.BATCH_SIZE_CONFIG)).isEqualTo(16384);
        assertThat(configProps.get(ProducerConfig.LINGER_MS_CONFIG)).isEqualTo(10);
        assertThat(configProps.get(ProducerConfig.BUFFER_MEMORY_CONFIG)).isEqualTo(33554432L);
        assertThat(configProps.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo(true);
    }

    @Test
    @DisplayName("Should create ProducerFactory with additional configurations")
    void shouldCreateProducerFactoryWithAdditionalConfigurations() {
        // When
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();
        
        // Then
        Map<String, Object> configProps = producerFactory.getConfigurationProperties();
        assertThat(configProps.get(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG)).isEqualTo(30000);
        assertThat(configProps.get(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG)).isEqualTo(120000);
        assertThat(configProps.get(ProducerConfig.COMPRESSION_TYPE_CONFIG)).isEqualTo("snappy");
        assertThat(configProps.get(ProducerConfig.CLIENT_ID_CONFIG)).isEqualTo("orders-service-producer");
    }

    @Test
    @DisplayName("Should create KafkaTemplate")
    void shouldCreateKafkaTemplate() {
        // When
        KafkaTemplate<String, Object> kafkaTemplate = kafkaConfig.kafkaTemplate();
        
        // Then
        assertNotNull(kafkaTemplate);
        assertNotNull(kafkaTemplate.getProducerFactory());
    }

    @Test
    @DisplayName("Should create Order Events KafkaTemplate")
    void shouldCreateOrderEventsKafkaTemplate() {
        // When
        KafkaTemplate<String, Object> kafkaTemplate = kafkaConfig.orderEventsKafkaTemplate();
        
        // Then
        assertNotNull(kafkaTemplate);
        assertNotNull(kafkaTemplate.getProducerFactory());
        assertThat(kafkaTemplate.getDefaultTopic()).isEqualTo("orders.events");
    }

    @Test
    @DisplayName("Should create Processing Events KafkaTemplate")
    void shouldCreateProcessingEventsKafkaTemplate() {
        // When
        KafkaTemplate<String, Object> kafkaTemplate = kafkaConfig.processingEventsKafkaTemplate();
        
        // Then
        assertNotNull(kafkaTemplate);
        assertNotNull(kafkaTemplate.getProducerFactory());
        assertThat(kafkaTemplate.getDefaultTopic()).isEqualTo("orders.processing");
    }

    @Test
    @DisplayName("Should create development ProducerFactory with relaxed settings")
    void shouldCreateDevelopmentProducerFactory() {
        // Given
        KafkaConfig.DevelopmentKafkaConfig devConfig = new KafkaConfig.DevelopmentKafkaConfig();
        
        // When
        ProducerFactory<String, Object> producerFactory = devConfig.developmentProducerFactory("localhost:9092");
        
        // Then
        assertNotNull(producerFactory);
        assertThat(producerFactory).isInstanceOf(DefaultKafkaProducerFactory.class);
        
        // Verify development-specific configurations
        Map<String, Object> configProps = producerFactory.getConfigurationProperties();
        assertThat(configProps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");
        assertThat(configProps.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)).isEqualTo(StringSerializer.class);
        assertThat(configProps.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)).isEqualTo(JsonSerializer.class);
        assertThat(configProps.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("1");
        assertThat(configProps.get(ProducerConfig.RETRIES_CONFIG)).isEqualTo(1);
        assertThat(configProps.get(ProducerConfig.BATCH_SIZE_CONFIG)).isEqualTo(1000);
        assertThat(configProps.get(ProducerConfig.LINGER_MS_CONFIG)).isEqualTo(5);
        assertThat(configProps.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo(false);
        assertThat(configProps.get(ProducerConfig.CLIENT_ID_CONFIG)).isEqualTo("orders-service-dev-producer");
    }

    @Test
    @DisplayName("Should create staging ProducerFactory with security settings")
    void shouldCreateStagingProducerFactory() {
        // Given
        KafkaConfig.StagingKafkaConfig stagingConfig = new KafkaConfig.StagingKafkaConfig();
        
        // When
        ProducerFactory<String, Object> producerFactory = stagingConfig.stagingProducerFactory(
            "staging-kafka:9092", "SASL_SSL", "AWS_MSK_IAM");
        
        // Then
        assertNotNull(producerFactory);
        assertThat(producerFactory).isInstanceOf(DefaultKafkaProducerFactory.class);
        
        // Verify staging-specific configurations
        Map<String, Object> configProps = producerFactory.getConfigurationProperties();
        assertThat(configProps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("staging-kafka:9092");
        assertThat(configProps.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)).isEqualTo(StringSerializer.class);
        assertThat(configProps.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)).isEqualTo(JsonSerializer.class);
        assertThat(configProps.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
        assertThat(configProps.get(ProducerConfig.RETRIES_CONFIG)).isEqualTo(3);
        assertThat(configProps.get(ProducerConfig.BATCH_SIZE_CONFIG)).isEqualTo(8192);
        assertThat(configProps.get(ProducerConfig.LINGER_MS_CONFIG)).isEqualTo(10);
        assertThat(configProps.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo(true);
        assertThat(configProps.get(ProducerConfig.CLIENT_ID_CONFIG)).isEqualTo("orders-service-staging-producer");
        
        // Verify security configurations
        assertThat(configProps.get("security.protocol")).isEqualTo("SASL_SSL");
        assertThat(configProps.get("sasl.mechanism")).isEqualTo("AWS_MSK_IAM");
        assertThat(configProps.get("sasl.jaas.config")).isEqualTo("software.amazon.msk.auth.iam.IAMLoginModule required;");
        assertThat(configProps.get("sasl.client.callback.handler.class"))
            .isEqualTo("software.amazon.msk.auth.iam.IAMClientCallbackHandler");
    }

    @Test
    @DisplayName("Should create production ProducerFactory with optimized settings")
    void shouldCreateProductionProducerFactory() {
        // Given
        KafkaConfig.ProductionKafkaConfig prodConfig = new KafkaConfig.ProductionKafkaConfig();
        
        // When
        ProducerFactory<String, Object> producerFactory = prodConfig.productionProducerFactory(
            "prod-kafka:9092", "SASL_SSL", "AWS_MSK_IAM");
        
        // Then
        assertNotNull(producerFactory);
        assertThat(producerFactory).isInstanceOf(DefaultKafkaProducerFactory.class);
        
        // Verify production-specific configurations
        Map<String, Object> configProps = producerFactory.getConfigurationProperties();
        assertThat(configProps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("prod-kafka:9092");
        assertThat(configProps.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)).isEqualTo(StringSerializer.class);
        assertThat(configProps.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)).isEqualTo(JsonSerializer.class);
        assertThat(configProps.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
        assertThat(configProps.get(ProducerConfig.RETRIES_CONFIG)).isEqualTo(5);
        assertThat(configProps.get(ProducerConfig.BATCH_SIZE_CONFIG)).isEqualTo(32768);
        assertThat(configProps.get(ProducerConfig.LINGER_MS_CONFIG)).isEqualTo(20);
        assertThat(configProps.get(ProducerConfig.BUFFER_MEMORY_CONFIG)).isEqualTo(67108864L);
        assertThat(configProps.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo(true);
        assertThat(configProps.get(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG)).isEqualTo(60000);
        assertThat(configProps.get(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG)).isEqualTo(300000);
        assertThat(configProps.get(ProducerConfig.COMPRESSION_TYPE_CONFIG)).isEqualTo("lz4");
        assertThat(configProps.get(ProducerConfig.CLIENT_ID_CONFIG)).isEqualTo("orders-service-prod-producer");
    }

    @Test
    @DisplayName("Should have different configurations for different environments")
    void shouldHaveDifferentConfigurationsForDifferentEnvironments() {
        // Given
        KafkaConfig.DevelopmentKafkaConfig devConfig = new KafkaConfig.DevelopmentKafkaConfig();
        KafkaConfig.ProductionKafkaConfig prodConfig = new KafkaConfig.ProductionKafkaConfig();
        KafkaConfig.StagingKafkaConfig stagingConfig = new KafkaConfig.StagingKafkaConfig();
        
        // When
        ProducerFactory<String, Object> devFactory = devConfig.developmentProducerFactory("localhost:9092");
        ProducerFactory<String, Object> prodFactory = prodConfig.productionProducerFactory(
            "prod-kafka:9092", "SASL_SSL", "AWS_MSK_IAM");
        ProducerFactory<String, Object> stagingFactory = stagingConfig.stagingProducerFactory(
            "staging-kafka:9092", "SASL_SSL", "AWS_MSK_IAM");
        ProducerFactory<String, Object> defaultFactory = kafkaConfig.producerFactory();
        
        // Then
        Map<String, Object> devProps = devFactory.getConfigurationProperties();
        Map<String, Object> prodProps = prodFactory.getConfigurationProperties();
        Map<String, Object> stagingProps = stagingFactory.getConfigurationProperties();
        Map<String, Object> defaultProps = defaultFactory.getConfigurationProperties();
        
        // Development should have minimal reliability
        assertThat(devProps.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("1");
        assertThat(devProps.get(ProducerConfig.RETRIES_CONFIG)).isEqualTo(1);
        assertThat(devProps.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo(false);
        
        // Production should have maximum reliability
        assertThat(prodProps.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
        assertThat(prodProps.get(ProducerConfig.RETRIES_CONFIG)).isEqualTo(5);
        assertThat(prodProps.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo(true);
        
        // Staging should be balanced
        assertThat(stagingProps.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
        assertThat(stagingProps.get(ProducerConfig.RETRIES_CONFIG)).isEqualTo(3);
        assertThat(stagingProps.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo(true);
        
        // Default should be moderate
        assertThat(defaultProps.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
        assertThat(defaultProps.get(ProducerConfig.RETRIES_CONFIG)).isEqualTo(3);
        assertThat(defaultProps.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo(true);
    }

    @Test
    @DisplayName("Should create different bean instances")
    void shouldCreateDifferentBeanInstances() {
        // When
        KafkaTemplate<String, Object> defaultTemplate = kafkaConfig.kafkaTemplate();
        KafkaTemplate<String, Object> orderEventsTemplate = kafkaConfig.orderEventsKafkaTemplate();
        KafkaTemplate<String, Object> processingEventsTemplate = kafkaConfig.processingEventsKafkaTemplate();
        
        // Then
        assertThat(defaultTemplate).isNotSameAs(orderEventsTemplate);
        assertThat(defaultTemplate).isNotSameAs(processingEventsTemplate);
        assertThat(orderEventsTemplate).isNotSameAs(processingEventsTemplate);
    }

    @Test
    @DisplayName("Should have proper serializer configuration")
    void shouldHaveProperSerializerConfiguration() {
        // When
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();
        
        // Then
        Map<String, Object> configProps = producerFactory.getConfigurationProperties();
        assertThat(configProps.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)).isEqualTo(StringSerializer.class);
        assertThat(configProps.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)).isEqualTo(JsonSerializer.class);
    }

    @Test
    @DisplayName("Should have proper performance configuration")
    void shouldHaveProperPerformanceConfiguration() {
        // When
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();
        
        // Then
        Map<String, Object> configProps = producerFactory.getConfigurationProperties();
        assertThat(configProps.get(ProducerConfig.BATCH_SIZE_CONFIG)).isEqualTo(16384);
        assertThat(configProps.get(ProducerConfig.LINGER_MS_CONFIG)).isEqualTo(10);
        assertThat(configProps.get(ProducerConfig.BUFFER_MEMORY_CONFIG)).isEqualTo(33554432L);
    }

    @Test
    @DisplayName("Should have proper reliability configuration")
    void shouldHaveProperReliabilityConfiguration() {
        // When
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();
        
        // Then
        Map<String, Object> configProps = producerFactory.getConfigurationProperties();
        assertThat(configProps.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
        assertThat(configProps.get(ProducerConfig.RETRIES_CONFIG)).isEqualTo(3);
        assertThat(configProps.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo(true);
        assertThat(configProps.get(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG)).isEqualTo(30000);
        assertThat(configProps.get(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG)).isEqualTo(120000);
    }

    @Test
    @DisplayName("Should have environment-specific client IDs")
    void shouldHaveEnvironmentSpecificClientIds() {
        // Given
        KafkaConfig.DevelopmentKafkaConfig devConfig = new KafkaConfig.DevelopmentKafkaConfig();
        KafkaConfig.ProductionKafkaConfig prodConfig = new KafkaConfig.ProductionKafkaConfig();
        KafkaConfig.StagingKafkaConfig stagingConfig = new KafkaConfig.StagingKafkaConfig();
        
        // When
        ProducerFactory<String, Object> devFactory = devConfig.developmentProducerFactory("localhost:9092");
        ProducerFactory<String, Object> prodFactory = prodConfig.productionProducerFactory(
            "prod-kafka:9092", "SASL_SSL", "AWS_MSK_IAM");
        ProducerFactory<String, Object> stagingFactory = stagingConfig.stagingProducerFactory(
            "staging-kafka:9092", "SASL_SSL", "AWS_MSK_IAM");
        ProducerFactory<String, Object> defaultFactory = kafkaConfig.producerFactory();
        
        // Then
        assertThat(devFactory.getConfigurationProperties().get(ProducerConfig.CLIENT_ID_CONFIG))
            .isEqualTo("orders-service-dev-producer");
        assertThat(prodFactory.getConfigurationProperties().get(ProducerConfig.CLIENT_ID_CONFIG))
            .isEqualTo("orders-service-prod-producer");
        assertThat(stagingFactory.getConfigurationProperties().get(ProducerConfig.CLIENT_ID_CONFIG))
            .isEqualTo("orders-service-staging-producer");
        assertThat(defaultFactory.getConfigurationProperties().get(ProducerConfig.CLIENT_ID_CONFIG))
            .isEqualTo("orders-service-producer");
    }

    @Test
    @DisplayName("Should have environment-specific compression settings")
    void shouldHaveEnvironmentSpecificCompressionSettings() {
        // Given
        KafkaConfig.ProductionKafkaConfig prodConfig = new KafkaConfig.ProductionKafkaConfig();
        
        // When
        ProducerFactory<String, Object> prodFactory = prodConfig.productionProducerFactory(
            "prod-kafka:9092", "SASL_SSL", "AWS_MSK_IAM");
        ProducerFactory<String, Object> defaultFactory = kafkaConfig.producerFactory();
        
        // Then
        assertThat(prodFactory.getConfigurationProperties().get(ProducerConfig.COMPRESSION_TYPE_CONFIG))
            .isEqualTo("lz4");
        assertThat(defaultFactory.getConfigurationProperties().get(ProducerConfig.COMPRESSION_TYPE_CONFIG))
            .isEqualTo("snappy");
    }
}