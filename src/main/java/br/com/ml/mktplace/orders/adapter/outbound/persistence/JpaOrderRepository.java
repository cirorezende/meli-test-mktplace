package br.com.ml.mktplace.orders.adapter.outbound.persistence;

import br.com.ml.mktplace.orders.adapter.outbound.persistence.entity.OrderEntity;
import br.com.ml.mktplace.orders.adapter.outbound.persistence.mapper.OrderEntityMapper;
import br.com.ml.mktplace.orders.adapter.outbound.persistence.repository.JpaOrderEntityRepository;
import br.com.ml.mktplace.orders.domain.model.Address;
import br.com.ml.mktplace.orders.domain.model.DistributionCenter;
import br.com.ml.mktplace.orders.domain.model.Order;
import br.com.ml.mktplace.orders.domain.model.OrderNotFoundException;
import br.com.ml.mktplace.orders.domain.model.NearbyDistributionCenter;
import br.com.ml.mktplace.orders.domain.port.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of OrderRepository port.
 * Translates between domain objects and JPA entities.
 */
@Component
public class JpaOrderRepository implements OrderRepository {

    private final JpaOrderEntityRepository jpaRepository;
    private final OrderEntityMapper mapper;

    public JpaOrderRepository(JpaOrderEntityRepository jpaRepository, OrderEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        OrderEntity entity = mapper.toEntity(order);
        OrderEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> findById(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        
        return jpaRepository.findById(orderId)
                .map(mapper::toDomain);
    }

    @Override
    public Order getById(String orderId) {
        return findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        
        List<OrderEntity> entities = jpaRepository.findByCustomerId(customerId);
        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findAll() {
        List<OrderEntity> entities = jpaRepository.findAll();
        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        
        return jpaRepository.existsById(orderId);
    }

    /**
     * Additional method using PostGIS spatial queries.
     * Find orders near a specific location within a radius.
     */
    public List<Order> findOrdersNearLocation(Double latitude, Double longitude, Double radiusMeters) {
        List<OrderEntity> entities = jpaRepository.findOrdersNearLocation(latitude, longitude, radiusMeters);
        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * Calcula e retorna a lista ordenada de centros de distribuição próximos
     * para os códigos informados, considerando o ponto de entrega (lat/lng).
     */
    public List<NearbyDistributionCenter> findNearbyDistributionCentersOrdered(double latitude, double longitude, List<String> dcCodes) {
        if (dcCodes == null || dcCodes.isEmpty()) return List.of();
        String[] codes = dcCodes.toArray(new String[0]);
        List<Object[]> rows = jpaRepository.findDistancesForDcCodesOrdered(latitude, longitude, codes);
        return rows.stream()
                .map(r -> new NearbyDistributionCenter((String) r[0], ((Number) r[1]).doubleValue()))
                .toList();
    }

    /**
     * Carrega os detalhes completos dos CDs a partir dos códigos informados usando o catálogo local (tabela distribution_centers).
     */
    public List<DistributionCenter> findDistributionCentersByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) return List.of();
        String[] arr = codes.toArray(new String[0]);
        List<Object[]> rows = jpaRepository.findDistributionCentersByCodes(arr);
        return rows.stream().map(r -> {
            String code = (String) r[0];
            String name = (String) r[1];
            String addressJson = (String) r[2];
            double longitude = ((Number) r[3]).doubleValue();
            double latitude = ((Number) r[4]).doubleValue();
            // Reconstruir Address a partir de JSON armazenado e coordenadas
            try {
                com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode node = om.readTree(addressJson);
                String street = node.get("street").asText();
                String city = node.get("city").asText();
                String state = node.get("state").asText();
                String country = node.get("country").asText();
                com.fasterxml.jackson.databind.JsonNode postalOrZipNode = node.get("postalCode");
                if (postalOrZipNode == null) postalOrZipNode = node.get("zipCode");
                String postal = postalOrZipNode != null ? postalOrZipNode.asText() : "00000-000";
                Address.Coordinates coords = new Address.Coordinates(
                        java.math.BigDecimal.valueOf(latitude),
                        java.math.BigDecimal.valueOf(longitude)
                );
                Address addr = new Address(street, city, state, country, postal, coords);
                return new DistributionCenter(code, name, addr);
            } catch (Exception e) {
                // Fallback mínimo em caso de JSON inesperado
                Address addr = new Address("Unknown", "Unknown", "Unknown", "Unknown", "00000-000",
                        new Address.Coordinates(java.math.BigDecimal.valueOf(latitude), java.math.BigDecimal.valueOf(longitude)));
                return new DistributionCenter(code, name, addr);
            }
        }).toList();
    }
}