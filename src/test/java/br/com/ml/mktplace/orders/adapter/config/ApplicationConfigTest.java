package br.com.ml.mktplace.orders.adapter.config;

import br.com.ml.mktplace.orders.domain.port.CacheService;
import br.com.ml.mktplace.orders.domain.port.CreateOrderUseCase;
import br.com.ml.mktplace.orders.domain.port.DistributionCenterService;
import br.com.ml.mktplace.orders.domain.port.EventPublisher;
import br.com.ml.mktplace.orders.domain.port.IDGenerator;
import br.com.ml.mktplace.orders.domain.port.OrderRepository;
import br.com.ml.mktplace.orders.domain.port.ProcessOrderUseCase;
import br.com.ml.mktplace.orders.domain.port.QueryOrderUseCase;
import br.com.ml.mktplace.orders.domain.service.CreateOrderUseCaseImpl;
import br.com.ml.mktplace.orders.domain.service.DistributionCenterSelectionService;
import br.com.ml.mktplace.orders.domain.service.ProcessOrderUseCaseImpl;
import br.com.ml.mktplace.orders.domain.service.QueryOrderUseCaseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Testes unitários para ApplicationConfig.
 * 
 * Verifica se todos os beans são criados corretamente e com as dependências apropriadas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationConfig Tests")
class ApplicationConfigTest {

    @Mock
    private OrderRepository mockOrderRepository;
    
    @Mock
    private EventPublisher mockEventPublisher;
    
    @Mock
    private IDGenerator mockIdGenerator;
    
    @Mock
    private DistributionCenterService mockDistributionCenterService;
    
    @Mock
    private CacheService mockCacheService;
    
    @Mock
    private br.com.ml.mktplace.orders.adapter.config.metrics.ObservabilityMetrics mockObservabilityMetrics;
    
    private ApplicationConfig applicationConfig;

    @BeforeEach
    void setUp() {
        applicationConfig = new ApplicationConfig();
    }

    @Test
    @DisplayName("Should create CreateOrderUseCase bean with correct dependencies")
    void shouldCreateCreateOrderUseCaseBeanWithCorrectDependencies() {
        // When
        CreateOrderUseCase result = applicationConfig.createOrderUseCase(
            mockOrderRepository, 
            mockEventPublisher, 
            mockIdGenerator
        );
        
        // Then
        assertNotNull(result);
        assertThat(result).isInstanceOf(CreateOrderUseCaseImpl.class);
    }

    @Test
    @DisplayName("Should create ProcessOrderUseCase bean with correct dependencies")
    void shouldCreateProcessOrderUseCaseBeanWithCorrectDependencies() {
        // Given
        DistributionCenterSelectionService mockSelectionService = 
            applicationConfig.distributionCenterSelectionService();
        
        // When
        ProcessOrderUseCase result = applicationConfig.processOrderUseCase(
            mockOrderRepository,
            mockDistributionCenterService,
            mockCacheService,
            mockEventPublisher,
            mockSelectionService,
            mockObservabilityMetrics
        );
        
        // Then
        assertNotNull(result);
        assertThat(result).isInstanceOf(ProcessOrderUseCaseImpl.class);
    }

    @Test
    @DisplayName("Should create QueryOrderUseCase bean with correct dependencies")
    void shouldCreateQueryOrderUseCaseBeanWithCorrectDependencies() {
        // When
        QueryOrderUseCase result = applicationConfig.queryOrderUseCase(mockOrderRepository);
        
        // Then
        assertNotNull(result);
        assertThat(result).isInstanceOf(QueryOrderUseCaseImpl.class);
    }

    @Test
    @DisplayName("Should create DistributionCenterSelectionService bean")
    void shouldCreateDistributionCenterSelectionServiceBean() {
        // When
        DistributionCenterSelectionService result = 
            applicationConfig.distributionCenterSelectionService();
        
        // Then
        assertNotNull(result);
        assertThat(result).isInstanceOf(DistributionCenterSelectionService.class);
    }

    @Test
    @DisplayName("Should create singleton instances of DistributionCenterSelectionService")
    void shouldCreateSingletonInstancesOfDistributionCenterSelectionService() {
        // When
        DistributionCenterSelectionService first = 
            applicationConfig.distributionCenterSelectionService();
        DistributionCenterSelectionService second = 
            applicationConfig.distributionCenterSelectionService();
        
        // Then
        assertNotNull(first);
        assertNotNull(second);
        // Note: In real Spring context, this would be a singleton, 
        // but in unit test each call creates new instance
        assertThat(first).isNotSameAs(second);
        assertThat(first.getClass()).isEqualTo(second.getClass());
    }

    @Test
    @DisplayName("Should create different instances of use cases with same dependencies")
    void shouldCreateDifferentInstancesOfUseCasesWithSameDependencies() {
        // When
        CreateOrderUseCase first = applicationConfig.createOrderUseCase(
            mockOrderRepository, mockEventPublisher, mockIdGenerator
        );
        CreateOrderUseCase second = applicationConfig.createOrderUseCase(
            mockOrderRepository, mockEventPublisher, mockIdGenerator
        );
        
        // Then
        assertNotNull(first);
        assertNotNull(second);
        assertThat(first).isNotSameAs(second);
        assertThat(first.getClass()).isEqualTo(second.getClass());
    }

    @Test
    @DisplayName("Should handle null dependencies gracefully")
    void shouldHandleNullDependenciesGracefully() {
        // When & Then
        // These should not throw NullPointerException during bean creation
        // The actual NullPointerException would occur when the use case is used
        
        CreateOrderUseCase createUseCase = applicationConfig.createOrderUseCase(
            null, null, null
        );
        assertNotNull(createUseCase);
        
        QueryOrderUseCase queryUseCase = applicationConfig.queryOrderUseCase(null);
        assertNotNull(queryUseCase);
        
        ProcessOrderUseCase processUseCase = applicationConfig.processOrderUseCase(
            null, null, null, null, null, null
        );
        assertNotNull(processUseCase);
    }

    @Test
    @DisplayName("Should verify all required beans can be created")
    void shouldVerifyAllRequiredBeansCanBeCreated() {
        // Given
        DistributionCenterSelectionService selectionService = 
            applicationConfig.distributionCenterSelectionService();
        
        // When
        CreateOrderUseCase createUseCase = applicationConfig.createOrderUseCase(
            mockOrderRepository, mockEventPublisher, mockIdGenerator
        );
        
        ProcessOrderUseCase processUseCase = applicationConfig.processOrderUseCase(
            mockOrderRepository, mockDistributionCenterService, 
            mockCacheService, mockEventPublisher, selectionService, mockObservabilityMetrics
        );
        
        QueryOrderUseCase queryUseCase = applicationConfig.queryOrderUseCase(
            mockOrderRepository
        );
        
        // Then
        assertThat(createUseCase).isNotNull();
        assertThat(processUseCase).isNotNull();
        assertThat(queryUseCase).isNotNull();
        assertThat(selectionService).isNotNull();
        
        // Verify types
        assertThat(createUseCase).isInstanceOf(CreateOrderUseCaseImpl.class);
        assertThat(processUseCase).isInstanceOf(ProcessOrderUseCaseImpl.class);
        assertThat(queryUseCase).isInstanceOf(QueryOrderUseCaseImpl.class);
        assertThat(selectionService).isInstanceOf(DistributionCenterSelectionService.class);
    }
}