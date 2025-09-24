package br.com.ml.mktplace.orders.adapter.config;

import br.com.ml.mktplace.orders.domain.port.CreateOrderUseCase;
import br.com.ml.mktplace.orders.domain.port.ProcessOrderUseCase;
import br.com.ml.mktplace.orders.domain.port.QueryOrderUseCase;
import br.com.ml.mktplace.orders.domain.port.CacheService;
import br.com.ml.mktplace.orders.domain.port.DistributionCenterService;
import br.com.ml.mktplace.orders.domain.port.EventPublisher;
import br.com.ml.mktplace.orders.domain.port.IDGenerator;
import br.com.ml.mktplace.orders.domain.port.OrderRepository;
import br.com.ml.mktplace.orders.domain.service.CreateOrderUseCaseImpl;
import br.com.ml.mktplace.orders.domain.service.DistributionCenterSelectionService;
import br.com.ml.mktplace.orders.domain.service.ProcessOrderUseCaseImpl;
import br.com.ml.mktplace.orders.adapter.config.metrics.ObservabilityMetrics;
import br.com.ml.mktplace.orders.domain.service.QueryOrderUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração principal da aplicação - Application Config
 * 
 * Responsável pelo wiring entre as portas (interfaces) e os adaptadores (implementações)
 * seguindo os princípios da Arquitetura Hexagonal.
 * 
 * Esta classe conecta:
 * - Use Cases (implementações de serviços do domínio)
 * - Portas de saída (outbound ports) com seus adaptadores
 * - Componentes de domínio (serviços especializados)
 */
@Configuration
public class ApplicationConfig {

    /**
     * Configura o caso de uso para criação de pedidos.
     * 
     * @param orderRepository Repositório de pedidos
     * @param eventPublisher Publicador de eventos
     * @param idGenerator Gerador de IDs únicos (ULID)
     * @return Use case configurado
     */
    @Bean
    public CreateOrderUseCase createOrderUseCase(
            OrderRepository orderRepository,
            EventPublisher eventPublisher,
            IDGenerator idGenerator) {
        
        return new CreateOrderUseCaseImpl(orderRepository, eventPublisher, idGenerator);
    }

    /**
     * Configura o caso de uso para processamento de pedidos.
     * 
     * @param orderRepository Repositório de pedidos
     * @param distributionCenterService Serviço de consulta de CDs
     * @param cacheService Serviço de cache
     * @param eventPublisher Publicador de eventos
     * @param distributionCenterSelectionService Serviço de seleção de CDs
     * @return Use case configurado
     */
    @Bean
    public ProcessOrderUseCase processOrderUseCase(
        OrderRepository orderRepository,
        DistributionCenterService distributionCenterService,
        CacheService cacheService,
        EventPublisher eventPublisher,
        DistributionCenterSelectionService distributionCenterSelectionService,
        br.com.ml.mktplace.orders.domain.port.GeocodingService geocodingService,
        ObservabilityMetrics observabilityMetrics) {
        
        return new ProcessOrderUseCaseImpl(
            orderRepository,
            distributionCenterService, 
            cacheService,
            eventPublisher,
            distributionCenterSelectionService,
            geocodingService,
            observabilityMetrics
        );
    }

    /**
     * Configura o caso de uso para consulta de pedidos.
     * 
     * @param orderRepository Repositório de pedidos
     * @return Use case configurado
     */
    @Bean
    public QueryOrderUseCase queryOrderUseCase(OrderRepository orderRepository) {
        return new QueryOrderUseCaseImpl(orderRepository);
    }

    /**
     * Configura o serviço de seleção de centros de distribuição.
     * Implementa o algoritmo de proximidade geográfica definido no ADR-009.
     * 
     * @return Serviço de seleção configurado
     */
    @Bean
    public DistributionCenterSelectionService distributionCenterSelectionService() {
        return new DistributionCenterSelectionService();
    }
}