package br.com.ml.mktplace.orders.adapter.inbound.rest.mapper;

import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.AddressDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderItemDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderRequest;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderResponse;
import br.com.ml.mktplace.orders.domain.model.Address;
import br.com.ml.mktplace.orders.domain.model.Order;
import br.com.ml.mktplace.orders.domain.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between REST DTOs and domain objects
 */
@Component
public class OrderRestMapper {
    
    /**
     * Converts OrderRequest DTO to Order domain object
     */
    public Order toDomain(OrderRequest request, String orderId) {
        List<OrderItem> orderItems = request.getItems().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());

        // Since delivery address is no longer provided in the request, we create
        // a temporary placeholder address. The real address should be resolved
        // later during processing (e.g., from customer profile or another service).
        Address placeholderAddress = new Address(
                "TBD Street",
                "TBD City",
                "TBD",
                "TBD",
                "00000-000",
                new Address.Coordinates(
                        java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO
                )
        );

        return new Order(request.getCustomerId(), orderItems, placeholderAddress);
    }
    
    /**
     * Converts Order domain object to OrderResponse DTO
     */
    public OrderResponse toResponse(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        AddressDto addressDto = toDto(order.getDeliveryAddress());
        
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus().name(),
                itemDtos,
                addressDto,
                order.getCreatedAt(),
                order.getCreatedAt(), // Using createdAt as updatedAt since we don't have updatedAt method
                order.getTotalItemsCount()
        );
    }
    
    /**
     * Converts OrderItemDto to OrderItem domain object
     */
    private OrderItem toDomain(OrderItemDto dto) {
        return new OrderItem(dto.getItemId(), dto.getQuantity());
    }
    
    /**
     * Converts OrderItem domain object to OrderItemDto
     */
    private OrderItemDto toDto(OrderItem orderItem) {
        // Mapear lista de CDs disponíveis mantendo a ordem do menos distante para o mais distante
        java.util.List<OrderItemDto.NearbyDcDto> nearbyDtos = orderItem.getAvailableDistributionCenters().stream()
                .map(n -> new OrderItemDto.NearbyDcDto(n.code(), n.distanceKm()))
                .toList();

        return new OrderItemDto(orderItem.getItemId(), orderItem.getQuantity(), nearbyDtos);
    }
    
    /**
     * Converts AddressDto to Address domain object
     */
    private Address toDomain(AddressDto dto) {
        Address.Coordinates coordinates = new Address.Coordinates(
                dto.getCoordinates().getLatitude(),
                dto.getCoordinates().getLongitude()
        );
        
        return new Address(
                dto.getStreet(),
                dto.getCity(),
                dto.getState(),
                dto.getCountry(),
                dto.getZipCode(),
                coordinates
        );
    }
    
    /**
     * Converts Address domain object to AddressDto
     */
    private AddressDto toDto(Address address) {
        AddressDto.CoordinatesDto coordinatesDto = new AddressDto.CoordinatesDto(
                address.coordinates().latitude(),
                address.coordinates().longitude()
        );
        
        return new AddressDto(
                address.street(),
                address.city(),
                address.state(),
                address.country(),
                address.zipCode(),
                coordinatesDto
        );
    }
}