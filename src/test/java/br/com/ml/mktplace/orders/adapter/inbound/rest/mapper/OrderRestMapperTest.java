package br.com.ml.mktplace.orders.adapter.inbound.rest.mapper;

import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.*;
import br.com.ml.mktplace.orders.domain.model.Address;
import br.com.ml.mktplace.orders.domain.model.Order;
import br.com.ml.mktplace.orders.domain.model.OrderItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderRestMapperTest {

    private final OrderRestMapper mapper = new OrderRestMapper();

    @Test
    void toDomain_shouldCreateOrderWithZeroCoordinatesWhenMissing() {
        // given request without coordinates
        AddressDto.CoordinatesDto coords = null; // intentionally null
        AddressDto dto = new AddressDto("Rua A", "123", "Cidade", "ST", "BR", "12345-000", coords);
        OrderItemDto itemDto = new OrderItemDto("ITEM-1", 2, List.of());
        OrderRequest request = new OrderRequest("CUST-1", List.of(itemDto), dto);

        // when
        Order order = mapper.toDomain(request, null);

        // then
        assertNotNull(order.getDeliveryAddress());
        assertEquals("Rua A", order.getDeliveryAddress().street());
        assertEquals("123", order.getDeliveryAddress().number());
        assertEquals(BigDecimal.ZERO, order.getDeliveryAddress().coordinates().latitude());
        assertEquals(BigDecimal.ZERO, order.getDeliveryAddress().coordinates().longitude());
    }

    @Test
    void toResponse_shouldIncludeNumberAndCoordinates() {
        // given domain order with full address
    Address address = new Address(
        "Av. Central", "500", "Metropolis", "ST", "BR", "54321-000",
                new Address.Coordinates(BigDecimal.valueOf(-10.1234), BigDecimal.valueOf(50.9876))
        );
        OrderItem item = new OrderItem("ITEM-9", 1);
        Order domain = new Order("CUST-X", List.of(item), address);

        // when
        OrderResponse response = mapper.toResponse(domain);

        // then
        assertNotNull(response.getDeliveryAddress());
        assertEquals("500", response.getDeliveryAddress().getNumber());
        assertEquals(BigDecimal.valueOf(-10.1234), response.getDeliveryAddress().getCoordinates().getLatitude());
    }
}
