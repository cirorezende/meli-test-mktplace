package br.com.ml.mktplace.orders.adapter.outbound.persistence.repository;

import br.com.ml.mktplace.orders.adapter.outbound.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for OrderEntity.
 * Provides database access with PostGIS spatial queries.
 */
@Repository
public interface JpaOrderEntityRepository extends JpaRepository<OrderEntity, String> {

    /**
     * Find orders by customer ID.
     */
    List<OrderEntity> findByCustomerId(String customerId);

    /**
     * Check if an order exists by ID.
     */
    boolean existsById(String orderId);

    /**
     * Find orders within a certain distance from a point (using PostGIS).
     * This is an example of how to use spatial queries.
     */
    @Query(value = """
        SELECT o.* FROM orders o 
        WHERE ST_DWithin(
            o.delivery_coordinates,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326),
            :radiusMeters
        )
        ORDER BY ST_Distance_Sphere(
            o.delivery_coordinates,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)
        )
        """, nativeQuery = true)
    List<OrderEntity> findOrdersNearLocation(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("radiusMeters") Double radiusMeters
    );

    /**
     * Find orders by status using derived query method.
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status")
    List<OrderEntity> findByStatus(@Param("status") String status);

    /**
     * Retorna códigos de CDs e distâncias (em km) para um conjunto de códigos dado,
     * calculadas a partir de um ponto (latitude/longitude) usando PostGIS, ordenadas do mais próximo ao mais distante.
     */
    @Query(value = """
        SELECT dc.code as code,
               ST_DistanceSphere(
                   dc.coordinates,
                   ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)
               ) / 1000.0 as distance_km
        FROM distribution_centers dc
        WHERE dc.code = ANY(:codes)
        ORDER BY distance_km ASC
        """, nativeQuery = true)
    List<Object[]> findDistancesForDcCodesOrdered(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("codes") String[] codes
    );
}