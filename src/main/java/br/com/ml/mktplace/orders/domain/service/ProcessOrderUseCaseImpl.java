package br.com.ml.mktplace.orders.domain.service;

import br.com.ml.mktplace.orders.domain.model.*;
import br.com.ml.mktplace.orders.adapter.config.metrics.ObservabilityMetrics;
import br.com.ml.mktplace.orders.domain.port.ProcessOrderUseCase;
import br.com.ml.mktplace.orders.domain.port.OrderRepository;
import br.com.ml.mktplace.orders.domain.port.DistributionCenterService;
import br.com.ml.mktplace.orders.domain.port.CacheService;
import br.com.ml.mktplace.orders.domain.port.EventPublisher;
import br.com.ml.mktplace.orders.domain.port.GeocodingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of ProcessOrderUseCase following hexagonal architecture principles.
 * Handles order processing, distribution center selection, and order updating.
 */
@Service
@Transactional
public class ProcessOrderUseCaseImpl implements ProcessOrderUseCase {

    private final OrderRepository orderRepository;
    private final DistributionCenterService distributionCenterService;
    private final CacheService cacheService;
    private final EventPublisher eventPublisher;
    private final DistributionCenterSelectionService selectionService;
    private final GeocodingService geocodingService;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private br.com.ml.mktplace.orders.adapter.outbound.persistence.JpaOrderRepository jpaOrderRepository;
    private final ObservabilityMetrics observabilityMetrics;
    private static final Logger log = LoggerFactory.getLogger(ProcessOrderUseCaseImpl.class);

    public ProcessOrderUseCaseImpl(
            OrderRepository orderRepository,
            DistributionCenterService distributionCenterService,
            CacheService cacheService,
            EventPublisher eventPublisher,
            DistributionCenterSelectionService selectionService,
            GeocodingService geocodingService,
            ObservabilityMetrics observabilityMetrics) {
        this.orderRepository = orderRepository;
        this.distributionCenterService = distributionCenterService;
        this.cacheService = cacheService;
        this.eventPublisher = eventPublisher;
        this.selectionService = selectionService;
        this.geocodingService = geocodingService;
        this.observabilityMetrics = observabilityMetrics;
    }

    @Override
    public Order processOrder(String orderId) {
        validateOrderId(orderId);
        
        try {
            // Find order
            Order order = findOrder(orderId);
            
            // Check if already processed
            if (order.isInFinalState()) {
                throw new ProcessOrderException(orderId, "Order is already in final state: " + order.getStatus());
            }

            // Idempotency guard (async pipeline):
            // Only process orders when status == RECEIVED. For any other non-final status,
            // the invocation likely results from retries/duplicate events and must not trigger
            // external side-effects (e.g., fetching distribution centers again). Returning the
            // current state keeps the pipeline idempotent and ensures exactly-one external call
            // for a given order lifecycle.
            if (order.getStatus() != OrderStatus.RECEIVED) {
                log.debug("Skipping processing for order {} with status {} (idempotency guard)", orderId, order.getStatus());
                return order; // return current state without side-effects
            }
            
            // Process order
            return observabilityMetrics.recordProcessing(() -> performOrderProcessing(order));
            
        } catch (OrderNotFoundException | ProcessOrderException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = "Unexpected error during processing";
            eventPublisher.publishOrderFailed(findOrderSafely(orderId), errorMessage, e);
            throw new ProcessOrderException(orderId, errorMessage, e);
        }
    }

    @Override
    public Order reprocessOrder(String orderId) {
        validateOrderId(orderId);
        
        try {
            // Find order
            Order order = findOrder(orderId);
            
            // Check if can be reprocessed
            if (order.getStatus() != OrderStatus.FAILED) {
                throw new ProcessOrderException(orderId, "Order cannot be reprocessed. Current status: " + order.getStatus());
            }
            
            // Reset order status
            order.changeStatus(OrderStatus.RECEIVED);
            Order updatedOrder = orderRepository.save(order);
            
            // Process order
            return observabilityMetrics.recordProcessing(() -> performOrderProcessing(updatedOrder));
            
        } catch (OrderNotFoundException | ProcessOrderException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = "Unexpected error during reprocessing";
            eventPublisher.publishOrderFailed(findOrderSafely(orderId), errorMessage, e);
            throw new ProcessOrderException(orderId, errorMessage, e);
        }
    }
    
    private Order performOrderProcessing(Order order) {
        try {
            // Resolve geocoding if needed (coordinates zero -> attempt fetch)
            if (needsGeocoding(order.getDeliveryAddress())) {
                var coords = geocodingService.geocode(
                        order.getDeliveryAddress().street(),
                        order.getDeliveryAddress().number(),
                        order.getDeliveryAddress().city(),
                        order.getDeliveryAddress().state(),
                        order.getDeliveryAddress().country(),
                        order.getDeliveryAddress().zipCode()
                );
                if (coords != null) {
                    // Create new order instance with enriched address (immutable pattern)
                    order = rebuildOrderWithAddress(order, new Address(
                            order.getDeliveryAddress().street(),
                            order.getDeliveryAddress().number(),
                            order.getDeliveryAddress().city(),
                            order.getDeliveryAddress().state(),
                            order.getDeliveryAddress().country(),
                            order.getDeliveryAddress().zipCode(),
                            coords
                    ));
                }
            }

            // Change status to processing
            order.changeStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
            
            // Para cada item: buscar CDs disponíveis para o item (com cache por itemId),
            // calcular distâncias usando PostGIS e armazenar lista ordenada por proximidade.
            int itemsProcessed = 0;
            int itemsFailed = 0;
            observabilityMetrics.recordItemsPerOrder(order.getItems().size());

            for (OrderItem item : order.getItems()) {
                try {
                    List<DistributionCenter> itemCenters = getAvailableDistributionCentersForItem(item.getItemId());
                    if (itemCenters.isEmpty()) {
                        throw new ExternalServiceException("DistributionCenterService", "No distribution centers available for item " + item.getItemId());
                    }

                    // Coordenadas do endereço de entrega
                    double lat = order.getDeliveryAddress().coordinates().latitude().doubleValue();
                    double lon = order.getDeliveryAddress().coordinates().longitude().doubleValue();

                    // Ordenar por distância via banco (PostGIS) para os códigos retornados pela API
                    List<String> codes = itemCenters.stream().map(DistributionCenter::code).toList();
                    java.util.List<br.com.ml.mktplace.orders.domain.model.NearbyDistributionCenter> nearby;
                    if (jpaOrderRepository != null) {
                        nearby = jpaOrderRepository.findNearbyDistributionCentersOrdered(lat, lon, codes);
                    } else {
                        // Fallback: ordenar em memória usando Haversine
                        nearby = new java.util.ArrayList<>();
                        for (DistributionCenter dc : itemCenters) {
                            double d = estimateDistanceKm(order.getDeliveryAddress(), dc);
                            nearby.add(new br.com.ml.mktplace.orders.domain.model.NearbyDistributionCenter(dc.code(), d));
                        }
                        nearby.sort(java.util.Comparator.comparingDouble(br.com.ml.mktplace.orders.domain.model.NearbyDistributionCenter::distanceKm));
                    }
                    // Persistimos no agregado em memória (será refletido na resposta via mapeadores/DTO se necessário)
                    item.setAvailableDistributionCenters(nearby);

                    // Seleciona o mais próximo entre os disponíveis (fallback: serviço local de seleção)
                    DistributionCenter selectedCenter = selectionService.selectDistributionCenter(itemCenters, order.getDeliveryAddress());
                    item.assignDistributionCenter(selectedCenter);
                    observabilityMetrics.incrementDcSelection(selectedCenter.code());
                    log.info("CD selecionado para item {} pedido {} -> {}", item.getItemId(), order.getId(), selectedCenter.code());
                    itemsProcessed++;
                } catch (ExternalServiceException e) {
                    // Preserve legacy behavior: bubble up external service failures to fail the whole order
                    throw e;
                } catch (Exception e) {
                    // Unexpected exceptions should bubble up and be handled at a higher level
                    log.warn("Erro inesperado ao processar item {} do pedido {}: {}", item.getItemId(), order.getId(), e.getMessage());
                    throw e;
                }
            }
            
            // Update order status based on results
            OrderStatus finalStatus;
            if (itemsProcessed == 0) {
                finalStatus = OrderStatus.FAILED;
            } else if (itemsFailed > 0) {
                finalStatus = OrderStatus.PROCESSING; // Partial success
            } else {
                finalStatus = OrderStatus.PROCESSED; // Full success
            }
            
            order.changeStatus(finalStatus);
            Order processedOrder = orderRepository.save(order);
            
            // Publish events
            if (finalStatus == OrderStatus.PROCESSED) {
                eventPublisher.publishOrderProcessed(processedOrder);
            } else if (finalStatus == OrderStatus.FAILED) {
                eventPublisher.publishOrderFailed(processedOrder, "Failed to process any items");
            }
            
            return processedOrder;
            
        } catch (ExternalServiceException e) {
            // Handle external service failures
            order.changeStatus(OrderStatus.FAILED);
            Order failedOrder = orderRepository.save(order);
            eventPublisher.publishOrderFailed(failedOrder, "Distribution center service unavailable", e);
            throw new ProcessOrderException(order.getId(), "Distribution center service unavailable", e);
        }
    }

    private boolean needsGeocoding(Address address) {
        return address.coordinates().latitude().compareTo(java.math.BigDecimal.ZERO) == 0 &&
               address.coordinates().longitude().compareTo(java.math.BigDecimal.ZERO) == 0;
    }

    private Order rebuildOrderWithAddress(Order original, Address newAddress) {
        return new Order(
                original.getId(),
                original.getCustomerId(),
                original.getItems(),
                newAddress,
                original.getStatus(),
                original.getCreatedAt()
        );
    }

    private double estimateDistanceKm(Address address, DistributionCenter dc) {
        double lat1 = address.coordinates().latitude().doubleValue();
        double lon1 = address.coordinates().longitude().doubleValue();
        double lat2 = dc.getCoordinates().latitude().doubleValue();
        double lon2 = dc.getCoordinates().longitude().doubleValue();
        final double R = 6371.0088; // Earth radius km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    private List<DistributionCenter> getAvailableDistributionCentersForItem(String itemId) {
        // Cache por item: códigos de CDs que possuem o item disponível
        String cacheKey = "item-dc-availability:v2:" + itemId;
        Optional<String[]> cachedArrayOpt = cacheService.get(cacheKey, String[].class);
        List<String> codes;
        if (cachedArrayOpt.isPresent() && cachedArrayOpt.get().length > 0) {
            codes = java.util.Arrays.asList(cachedArrayOpt.get());
        } else {
            // Tenta por item (única forma permitida pela API externa)
            List<String> byItem = distributionCenterService.findDistributionCentersByItem(itemId);
            codes = (byItem == null) ? java.util.List.of() : byItem;
            cacheService.put(cacheKey, codes.toArray(String[]::new), java.time.Duration.ofMinutes(5));
        }

        // Carrega os detalhes completos na base local
        if (jpaOrderRepository != null) {
            return ((br.com.ml.mktplace.orders.adapter.outbound.persistence.JpaOrderRepository) jpaOrderRepository)
                    .findDistributionCentersByCodes(codes);
        }
        // Fallback: materializa mínimos se repositório JPA não disponível (ex.: testes unitários puros)
        java.util.List<DistributionCenter> minimal = new java.util.ArrayList<>();
        for (String code : codes) {
        Address placeholder = new Address(
            "Unknown", "0", "Unknown", "Unknown", "Unknown", "00000-000",
            new Address.Coordinates(java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO)
        );
            minimal.add(new DistributionCenter(code, "DC " + code, placeholder));
        }
        return minimal;
    }
    
    private void validateOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
    }
    
    private Order findOrder(String orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new OrderNotFoundException(orderId);
        }
        return orderOpt.get();
    }
    
    private Order findOrderSafely(String orderId) {
        try {
            return findOrder(orderId);
        } catch (OrderNotFoundException e) {
            // Return null if order not found (for event publishing)
            return null;
        }
    }
}