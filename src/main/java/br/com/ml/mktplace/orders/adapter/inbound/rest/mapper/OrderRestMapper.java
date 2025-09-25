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

                if (request.getDeliveryAddress() == null) {
                        throw new IllegalArgumentException("deliveryAddress is required");
                }
                AddressDto addr = request.getDeliveryAddress();
                Address address;
                if (addr.getCoordinates() != null) {
                        address = toDomain(addr);
                } else {
                        address = new Address(
                                        addr.getStreet(),
                                        addr.getNumber(),
                                        addr.getCity(),
                                        addr.getState(),
                                        addr.getCountry(),
                                        addr.getZipCode(),
                                        new Address.Coordinates(java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO)
                        );
                }
                return new Order(request.getCustomerId(), orderItems, address);
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
                .map(n -> new OrderItemDto.NearbyDcDto(n.code(), java.math.BigDecimal.valueOf(n.distanceKm()).setScale(2, java.math.RoundingMode.HALF_UP)))
                .toList();

        return new OrderItemDto(orderItem.getItemId(), orderItem.getQuantity(), nearbyDtos);
    }
    
    /**
     * Converts AddressDto to Address domain object
     */
    private Address toDomain(AddressDto dto) {
        if (dto.getCoordinates() == null) {
            throw new IllegalArgumentException("Coordinates missing when attempting domain conversion (should be set at this stage)");
        }
        Address.Coordinates coordinates = new Address.Coordinates(dto.getCoordinates().getLatitude(), dto.getCoordinates().getLongitude());
        return new Address(dto.getStreet(), dto.getNumber(), dto.getCity(), dto.getState(), dto.getCountry(), dto.getZipCode(), coordinates);
    }
    
    /**
     * Converts Address domain object to AddressDto
     */
        private AddressDto toDto(Address address) {
                AddressDto.CoordinatesDto coordinatesDto = new AddressDto.CoordinatesDto(
                                address.coordinates().latitude(),
                                address.coordinates().longitude()
                );
                return new AddressDto(address.street(), address.number(), address.city(), address.state(), address.country(), address.zipCode(), coordinatesDto);
        }
}