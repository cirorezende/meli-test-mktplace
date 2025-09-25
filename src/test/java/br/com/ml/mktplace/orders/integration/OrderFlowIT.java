package br.com.ml.mktplace.orders.integration;

import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderRequest;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.AddressDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderItemDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Primeiro teste de integração end-to-end validando fluxo básico de criação e recuperação de pedido
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderFlowIT extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // WireMock removed: in-process distribution center mock returns random subset automatically

    @Test
    @org.junit.jupiter.api.Order(1)
    void deveCriarERecuperarPedidoComSucesso() {
        String correlationId = UUID.randomUUID().toString();

    OrderRequest request = new OrderRequest();
    request.setCustomerId("customer-1");
    request.setItems(List.of(new OrderItemDto("ABC123", 2)));
    // Provide minimal valid delivery address (was previously removed causing 400 BAD_REQUEST)
    AddressDto addr = new AddressDto();
    addr.setStreet("Rua Teste");
    addr.setNumber("123");
    addr.setCity("São Paulo");
    addr.setState("SP");
    addr.setCountry("BR");
    addr.setZipCode("01000-000");
    request.setDeliveryAddress(addr);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Correlation-ID", correlationId);

        HttpEntity<OrderRequest> entity = new HttpEntity<>(request, headers);

    ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity("/v1/orders", entity, OrderResponse.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    OrderResponse createdBody = createResponse.getBody();
    assertThat(createdBody).isNotNull();
    String orderId = createdBody != null ? createdBody.getId() : null;
        assertThat(orderId).isNotBlank();

    ResponseEntity<OrderResponse> getResponse = restTemplate.getForEntity("/v1/orders/" + orderId, OrderResponse.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    OrderResponse fetched = getResponse.getBody();
    assertThat(fetched).isNotNull();
    assertThat(fetched != null ? fetched.getId() : null).isEqualTo(orderId);
    }
}
