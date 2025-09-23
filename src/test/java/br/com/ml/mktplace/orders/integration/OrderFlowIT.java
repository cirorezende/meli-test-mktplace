package br.com.ml.mktplace.orders.integration;

import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderRequest;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderRequestItem;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
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

    private static WireMockServer wireMockServer;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) wireMockServer.stop();
    }

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
        request.setItems(List.of(new OrderRequestItem("ABC123", 2, new BigDecimal("10.00"))));
        request.setCity("São Paulo");
        request.setState("SP");
        request.setCountry("BR");
        request.setStreet("Rua Teste");
        request.setZipCode("01000-000");
        request.setLatitude(new BigDecimal("-23.5"));
        request.setLongitude(new BigDecimal("-46.6"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Correlation-ID", correlationId);

        HttpEntity<OrderRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity("/api/v1/orders", entity, OrderResponse.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        String orderId = createResponse.getBody().getId();
        assertThat(orderId).isNotBlank();

        ResponseEntity<OrderResponse> getResponse = restTemplate.getForEntity("/api/v1/orders/" + orderId, OrderResponse.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getId()).isEqualTo(orderId);
    }
}
