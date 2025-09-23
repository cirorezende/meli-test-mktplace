package br.com.ml.mktplace.orders.integration;

import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderRequest;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderItemDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.AddressDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderResponse;
import br.com.ml.mktplace.orders.integration.support.SharedWireMock;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Primeiro teste de integração end-to-end validando fluxo básico de criação e recuperação de pedido
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderFlowIT extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void startWireMock() { SharedWireMock.startIfNeeded(); }

    @AfterAll
    static void stopWireMock() { }

    @BeforeEach
    void setupStubs() {
        // Sucesso para item ABC123
        stubFor(get(urlPathMatching("/distribution-centers/item/ABC123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"code\":\"CD1\",\"name\":\"CD One\",\"street\":\"Rua A\",\"city\":\"SP\",\"state\":\"SP\",\"country\":\"BR\",\"zipCode\":\"01000-000\",\"latitude\":-23.5,\"longitude\":-46.6}]")
                ));
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void deveCriarERecuperarPedidoComSucesso() {
        String correlationId = UUID.randomUUID().toString();

    OrderRequest request = new OrderRequest();
    request.setCustomerId("customer-1");
    request.setItems(List.of(new OrderItemDto("ABC123", 2)));
    AddressDto deliveryAddress = new AddressDto(
        "Rua Teste",
        "São Paulo",
        "SP",
        "BR",
        "01000-000",
        new AddressDto.CoordinatesDto(new BigDecimal("-23.5"), new BigDecimal("-46.6"))
    );
    request.setDeliveryAddress(deliveryAddress);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Correlation-ID", correlationId);

        HttpEntity<OrderRequest> entity = new HttpEntity<>(request, headers);

    ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity("/api/v1/orders", entity, OrderResponse.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    OrderResponse createdBody = createResponse.getBody();
    assertThat(createdBody).isNotNull();
    String orderId = createdBody != null ? createdBody.getId() : null;
        assertThat(orderId).isNotBlank();

    ResponseEntity<OrderResponse> getResponse = restTemplate.getForEntity("/api/v1/orders/" + orderId, OrderResponse.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    OrderResponse fetched = getResponse.getBody();
    assertThat(fetched).isNotNull();
    assertThat(fetched != null ? fetched.getId() : null).isEqualTo(orderId);
    }
}
