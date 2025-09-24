package br.com.ml.mktplace.orders.adapter.config;

import org.springframework.beans.factory.annotation.Value;
import io.micrometer.core.instrument.MeterRegistry;
import br.com.ml.mktplace.orders.adapter.config.metrics.http.HttpClientMetricsInterceptor;
import br.com.ml.mktplace.orders.adapter.config.metrics.http.CorrelationIdClientHttpRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * Configuração do cliente HTTP - Http Client Config
 * 
 * Configura:
 * - RestTemplate com timeouts otimizados
 * - Retry policy com backoff exponencial
 * - Circuit breaker pattern (simplificado)
 * - Configurações específicas por ambiente
 * 
 * ADRs relacionados:
 * - ADR-011: Estratégia de tratamento de erros
 * - ADR-016: Configuração por arquivos de propriedades
 */
@Configuration
@EnableRetry
public class HttpClientConfig {

    @Value("${app.http.timeout.connection:5000}")
    private int connectionTimeout;

    @Value("${app.http.timeout.read:10000}")
    private int readTimeout;

    @Value("${app.http.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${app.http.retry.initial-interval:1000}")
    private long initialRetryInterval;

    @Value("${app.http.retry.multiplier:2.0}")
    private double retryMultiplier;

    @Value("${app.http.retry.max-interval:10000}")
    private long maxRetryInterval;

    /**
     * Configura o RestTemplate principal com timeouts otimizados.
     * 
     * Usado para chamadas HTTP para a API de Distribution Centers
     * e outros serviços externos.
     */
    @Bean
    public RestTemplate restTemplate(MeterRegistry registry) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(clientHttpRequestFactory());
    // Interceptors: correlação e métricas
    restTemplate.getInterceptors().add(new CorrelationIdClientHttpRequestInterceptor());
    restTemplate.getInterceptors().add(new HttpClientMetricsInterceptor(registry, "external-generic"));
        
        return restTemplate;
    }

    /**
     * Configura a factory para requisições HTTP com timeouts.
     */
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        
        return factory;
    }

    /**
     * Configura o template de retry para chamadas HTTP.
     * 
     * Implementa:
     * - Retry com backoff exponencial
     * - Máximo de tentativas configurável
     * - Multiplier para aumentar intervalo entre tentativas
     */
    @Bean
    public RetryTemplate httpRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Política de retry - quantas vezes tentar
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxRetryAttempts);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // Política de backoff - intervalo entre tentativas
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialRetryInterval);
        backOffPolicy.setMultiplier(retryMultiplier);
        backOffPolicy.setMaxInterval(maxRetryInterval);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        return retryTemplate;
    }

    /**
     * RestTemplate específico para API de Distribution Centers.
     * Com configurações otimizadas para esse serviço específico.
     */
    @Bean("distributionCenterRestTemplate")
    public RestTemplate distributionCenterRestTemplate(
            @Value("${app.api.distribution-center.timeout.connection:3000}") int dcConnectionTimeout,
        @Value("${app.api.distribution-center.timeout.read:5000}") int dcReadTimeout,
        MeterRegistry registry) {
        
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(dcConnectionTimeout);
        factory.setReadTimeout(dcReadTimeout);
        
        RestTemplate restTemplate = new RestTemplate(factory);
        // Interceptors: correlação e métricas específico para a API de CDs
        restTemplate.getInterceptors().add(new CorrelationIdClientHttpRequestInterceptor());
        restTemplate.getInterceptors().add(new HttpClientMetricsInterceptor(registry, "distribution-centers-api"));
        
        return restTemplate;
    }

    /**
     * Template de retry específico para API de Distribution Centers.
     * Com configurações mais agressivas devido à criticidade.
     */
    @Bean("distributionCenterRetryTemplate")
    public RetryTemplate distributionCenterRetryTemplate(
            @Value("${app.api.distribution-center.retry.max-attempts:5}") int dcMaxAttempts,
            @Value("${app.api.distribution-center.retry.initial-interval:500}") long dcInitialInterval) {
        
        RetryTemplate retryTemplate = new RetryTemplate();
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(dcMaxAttempts);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(dcInitialInterval);
        backOffPolicy.setMultiplier(1.5); // Backoff mais suave para API crítica
        backOffPolicy.setMaxInterval(5000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        return retryTemplate;
    }

    /**
     * Configurações específicas para ambiente de desenvolvimento.
     */
    @Configuration
    @Profile("dev")
    static class DevelopmentHttpConfig {
        
        @Bean
        public RestTemplate developmentRestTemplate() {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            // Timeouts maiores para desenvolvimento (facilitar debug)
            factory.setConnectTimeout(30000);
            factory.setReadTimeout(60000);
            
            return new RestTemplate(factory);
        }
        
        @Bean
        public RetryTemplate developmentRetryTemplate() {
            RetryTemplate retryTemplate = new RetryTemplate();
            
            // Menos tentativas para desenvolvimento
            SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
            retryPolicy.setMaxAttempts(2);
            retryTemplate.setRetryPolicy(retryPolicy);
            
            // Backoff mais curto
            ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
            backOffPolicy.setInitialInterval(500);
            backOffPolicy.setMultiplier(2.0);
            backOffPolicy.setMaxInterval(2000);
            retryTemplate.setBackOffPolicy(backOffPolicy);
            
            return retryTemplate;
        }
    }

    /**
     * Configurações específicas para ambiente de produção.
     */
    @Configuration
    @Profile("prod")
    static class ProductionHttpConfig {
        
        @Bean
        public RestTemplate productionRestTemplate() {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            // Timeouts otimizados para produção
            factory.setConnectTimeout(3000);
            factory.setReadTimeout(8000);
            
            RestTemplate restTemplate = new RestTemplate(factory);
            
            // Em produção, adicionar interceptors de monitoramento
            // restTemplate.setInterceptors(List.of(
            //     new MetricsClientHttpRequestInterceptor(),
            //     new TracingClientHttpRequestInterceptor()
            // ));
            
            return restTemplate;
        }
        
        @Bean
        public RetryTemplate productionRetryTemplate() {
            RetryTemplate retryTemplate = new RetryTemplate();
            
            // Mais tentativas em produção
            SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
            retryPolicy.setMaxAttempts(4);
            retryTemplate.setRetryPolicy(retryPolicy);
            
            // Backoff otimizado para produção
            ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
            backOffPolicy.setInitialInterval(800);
            backOffPolicy.setMultiplier(2.0);
            backOffPolicy.setMaxInterval(8000);
            retryTemplate.setBackOffPolicy(backOffPolicy);
            
            return retryTemplate;
        }
    }
}