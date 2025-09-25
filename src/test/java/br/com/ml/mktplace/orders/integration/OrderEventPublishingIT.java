package br.com.ml.mktplace.orders.integration;

import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderRequest;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.AddressDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderItemDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderResponse;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de publicação de eventos. Neste estágio o publisher apenas loga; esse teste serve
 * como ponto de extensão futuro: quando Kafka real for integrado, basta remover comentários
 * do envio e validar consumo. Por ora validamos que o fluxo de pedido roda sem exceções.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderEventPublishingIT extends BaseIntegrationTest {


    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fluxoPublicaEventosSemErro() {
        // Arrange
        OrderRequest request = new OrderRequest();
        request.setCustomerId("customer-events");
        request.setItems(List.of(new OrderItemDto("EVT123", 1)));
        AddressDto addr = new AddressDto();
        addr.setStreet("Rua Eventos");
        addr.setNumber("10");
        addr.setCity("São Paulo");
        addr.setState("SP");
        addr.setCountry("BR");
        addr.setZipCode("02000-000");
        request.setDeliveryAddress(addr);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WireMock removed: internal mock provides CD list automatically

        // Act
    ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity("/v1/orders", new HttpEntity<>(request, headers), OrderResponse.class);

        // Assert
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    OrderResponse body = createResponse.getBody();
    assertThat(body).isNotNull();
    assertThat(body != null ? body.getId() : null).isNotBlank();
    }
}
