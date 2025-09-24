package br.com.ml.mktplace.orders.adapter.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Testes unitários para HttpClientConfig.
 * 
 * Verifica a configuração do RestTemplate, retry policies e timeouts.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HttpClientConfig Tests")
class HttpClientConfigTest {

    private HttpClientConfig httpClientConfig;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        httpClientConfig = new HttpClientConfig();
        meterRegistry = new SimpleMeterRegistry();
        
        // Set test properties using reflection
        ReflectionTestUtils.setField(httpClientConfig, "connectionTimeout", 5000);
        ReflectionTestUtils.setField(httpClientConfig, "readTimeout", 10000);
        ReflectionTestUtils.setField(httpClientConfig, "maxRetryAttempts", 3);
        ReflectionTestUtils.setField(httpClientConfig, "initialRetryInterval", 1000L);
        ReflectionTestUtils.setField(httpClientConfig, "retryMultiplier", 2.0);
        ReflectionTestUtils.setField(httpClientConfig, "maxRetryInterval", 10000L);
    }

    @Test
    @DisplayName("Should create RestTemplate with correct configuration")
    void shouldCreateRestTemplateWithCorrectConfiguration() {
        // When
        RestTemplate restTemplate = httpClientConfig.restTemplate(meterRegistry);
        
        // Then
        assertNotNull(restTemplate);
        ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
        if (factory instanceof InterceptingClientHttpRequestFactory) {
            Object delegate = ReflectionTestUtils.getField(factory, "requestFactory");
            assertThat(delegate).isInstanceOf(SimpleClientHttpRequestFactory.class);
        } else {
            assertThat(factory).isInstanceOf(SimpleClientHttpRequestFactory.class);
        }
    }

    @Test
    @DisplayName("Should create ClientHttpRequestFactory")
    void shouldCreateClientHttpRequestFactory() {
        // When
        ClientHttpRequestFactory factory = httpClientConfig.clientHttpRequestFactory();
        
        // Then
        assertNotNull(factory);
        assertThat(factory).isInstanceOf(SimpleClientHttpRequestFactory.class);
    }

    @Test
    @DisplayName("Should create RetryTemplate")
    void shouldCreateRetryTemplate() {
        // When
        RetryTemplate retryTemplate = httpClientConfig.httpRetryTemplate();
        
        // Then
        assertNotNull(retryTemplate);
    }

    @Test
    @DisplayName("Should create Distribution Center RestTemplate")
    void shouldCreateDistributionCenterRestTemplate() {
        // When
        RestTemplate restTemplate = httpClientConfig.distributionCenterRestTemplate(3000, 5000, meterRegistry);
        
        // Then
        assertNotNull(restTemplate);
        ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
        if (factory instanceof InterceptingClientHttpRequestFactory) {
            Object delegate = ReflectionTestUtils.getField(factory, "requestFactory");
            assertThat(delegate).isInstanceOf(SimpleClientHttpRequestFactory.class);
        } else {
            assertThat(factory).isInstanceOf(SimpleClientHttpRequestFactory.class);
        }
    }

    @Test
    @DisplayName("Should create Distribution Center RetryTemplate")
    void shouldCreateDistributionCenterRetryTemplate() {
        // When
        RetryTemplate retryTemplate = httpClientConfig.distributionCenterRetryTemplate(5, 500L);
        
        // Then
        assertNotNull(retryTemplate);
    }

    @Test
    @DisplayName("Should create development RestTemplate")
    void shouldCreateDevelopmentRestTemplate() {
        // Given
        HttpClientConfig.DevelopmentHttpConfig devConfig = new HttpClientConfig.DevelopmentHttpConfig();
        
        // When
        RestTemplate restTemplate = devConfig.developmentRestTemplate();
        
        // Then
        assertNotNull(restTemplate);
        
        ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
        assertThat(factory).isInstanceOf(SimpleClientHttpRequestFactory.class);
    }

    @Test
    @DisplayName("Should create development RetryTemplate")
    void shouldCreateDevelopmentRetryTemplate() {
        // Given
        HttpClientConfig.DevelopmentHttpConfig devConfig = new HttpClientConfig.DevelopmentHttpConfig();
        
        // When
        RetryTemplate retryTemplate = devConfig.developmentRetryTemplate();
        
        // Then
        assertNotNull(retryTemplate);
    }

    @Test
    @DisplayName("Should create production RestTemplate")
    void shouldCreateProductionRestTemplate() {
        // Given
        HttpClientConfig.ProductionHttpConfig prodConfig = new HttpClientConfig.ProductionHttpConfig();
        
        // When
        RestTemplate restTemplate = prodConfig.productionRestTemplate();
        
        // Then
        assertNotNull(restTemplate);
        
        ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
        assertThat(factory).isInstanceOf(SimpleClientHttpRequestFactory.class);
    }

    @Test
    @DisplayName("Should create production RetryTemplate")
    void shouldCreateProductionRetryTemplate() {
        // Given
        HttpClientConfig.ProductionHttpConfig prodConfig = new HttpClientConfig.ProductionHttpConfig();
        
        // When
        RetryTemplate retryTemplate = prodConfig.productionRetryTemplate();
        
        // Then
        assertNotNull(retryTemplate);
    }

    @Test
    @DisplayName("Should create different beans for different methods")
    void shouldCreateDifferentBeansForDifferentMethods() {
        // When
        RestTemplate defaultTemplate = httpClientConfig.restTemplate(meterRegistry);
        RestTemplate dcTemplate = httpClientConfig.distributionCenterRestTemplate(3000, 5000, meterRegistry);
        
        HttpClientConfig.DevelopmentHttpConfig devConfig = new HttpClientConfig.DevelopmentHttpConfig();
        HttpClientConfig.ProductionHttpConfig prodConfig = new HttpClientConfig.ProductionHttpConfig();
        
        RestTemplate devTemplate = devConfig.developmentRestTemplate();
        RestTemplate prodTemplate = prodConfig.productionRestTemplate();
        
        // Then
        assertNotNull(defaultTemplate);
        assertNotNull(dcTemplate);
        assertNotNull(devTemplate);
        assertNotNull(prodTemplate);
        
        // Verify they are different instances
        assertThat(defaultTemplate).isNotSameAs(dcTemplate);
        assertThat(defaultTemplate).isNotSameAs(devTemplate);
        assertThat(defaultTemplate).isNotSameAs(prodTemplate);
        assertThat(dcTemplate).isNotSameAs(devTemplate);
        assertThat(dcTemplate).isNotSameAs(prodTemplate);
        assertThat(devTemplate).isNotSameAs(prodTemplate);
    }

    @Test
    @DisplayName("Should create different retry templates for different methods")
    void shouldCreateDifferentRetryTemplatesForDifferentMethods() {
        // When
        RetryTemplate defaultRetry = httpClientConfig.httpRetryTemplate();
        RetryTemplate dcRetry = httpClientConfig.distributionCenterRetryTemplate(5, 500L);
        
        HttpClientConfig.DevelopmentHttpConfig devConfig = new HttpClientConfig.DevelopmentHttpConfig();
        HttpClientConfig.ProductionHttpConfig prodConfig = new HttpClientConfig.ProductionHttpConfig();
        
        RetryTemplate devRetry = devConfig.developmentRetryTemplate();
        RetryTemplate prodRetry = prodConfig.productionRetryTemplate();
        
        // Then
        assertNotNull(defaultRetry);
        assertNotNull(dcRetry);
        assertNotNull(devRetry);
        assertNotNull(prodRetry);
        
        // Verify they are different instances
        assertThat(defaultRetry).isNotSameAs(dcRetry);
        assertThat(defaultRetry).isNotSameAs(devRetry);
        assertThat(defaultRetry).isNotSameAs(prodRetry);
        assertThat(dcRetry).isNotSameAs(devRetry);
        assertThat(dcRetry).isNotSameAs(prodRetry);
        assertThat(devRetry).isNotSameAs(prodRetry);
    }

    @Test
    @DisplayName("Should have proper configuration for all environments")
    void shouldHaveProperConfigurationForAllEnvironments() {
        // Given
        HttpClientConfig.DevelopmentHttpConfig devConfig = new HttpClientConfig.DevelopmentHttpConfig();
        HttpClientConfig.ProductionHttpConfig prodConfig = new HttpClientConfig.ProductionHttpConfig();
        
        // When & Then - All should create beans without throwing exceptions
        assertNotNull(httpClientConfig.restTemplate(meterRegistry));
        assertNotNull(httpClientConfig.clientHttpRequestFactory());
        assertNotNull(httpClientConfig.httpRetryTemplate());
        assertNotNull(httpClientConfig.distributionCenterRestTemplate(3000, 5000, meterRegistry));
        assertNotNull(httpClientConfig.distributionCenterRetryTemplate(5, 500L));
        
        assertNotNull(devConfig.developmentRestTemplate());
        assertNotNull(devConfig.developmentRetryTemplate());
        
        assertNotNull(prodConfig.productionRestTemplate());
        assertNotNull(prodConfig.productionRetryTemplate());
    }
}