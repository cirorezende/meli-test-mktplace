package br.com.ml.mktplace.orders.integration;

import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderRequest;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderItemDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderResponse;
import com.github.tomakehurst.wiremock.client.WireMock;
import br.com.ml.mktplace.orders.integration.support.SharedWireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida comportamento de cache em arquitetura 100% assíncrona (POST retorna 202 + status RECEIVED):
 * Duas criações de pedidos com mesmo item e mesmo estado devem resultar em apenas
 * UMA chamada HTTP ao endpoint externo /distribution-centers na primeira execução.
 * A segunda deve reutilizar o cache (chave versionada por estado) e não gerar nova chamada.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DistributionCenterCachingIT extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String ITEM = "CACHE1";

    @BeforeEach
    void ensureServer() { SharedWireMock.startIfNeeded(); }

    @BeforeEach
    void resetWireMock() {
        WireMock.reset();
    // Production code currently calls findAllDistributionCenters() -> GET /distribution-centers
    stubFor(get(urlPathEqualTo("/distribution-centers"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"code\":\"CDY\",\"name\":\"CD Y\",\"street\":\"Rua Y\",\"city\":\"SP\",\"state\":\"SP\",\"country\":\"BR\",\"zipCode\":\"01000-000\",\"latitude\":-23.55,\"longitude\":-46.61}]")
                ));
    }

    @Test
    void segundaChamadaReutilizaCache() {
        OrderRequest req1 = buildRequest();
        OrderRequest req2 = buildRequest();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<OrderResponse> r1 = restTemplate.postForEntity("/v1/orders", new HttpEntity<>(req1, headers), OrderResponse.class);
    assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    // Aguarda primeiro pedido atingir estado final para evitar condições de concorrência
    OrderResponse firstBody = r1.getBody();
    assertThat(firstBody).as("Resposta de criação do primeiro pedido não deve ser nula").isNotNull();
    String firstOrderId = java.util.Objects.requireNonNull(firstBody).getId();
    assertThat(firstOrderId).isNotBlank();
    awaitOrderProcessed(firstOrderId);

    ResponseEntity<OrderResponse> r2 = restTemplate.postForEntity("/v1/orders", new HttpEntity<>(req2, headers), OrderResponse.class);
    assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // Verifica que o endpoint externo foi chamado apenas uma vez
    verify(1, getRequestedFor(urlPathEqualTo("/distribution-centers")));
    }

    private void awaitOrderProcessed(String orderId) {
        long timeoutMs = 10_000; // 10s deve ser suficiente para processamento async local
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            ResponseEntity<OrderResponse> resp = restTemplate.getForEntity("/v1/orders/" + orderId, OrderResponse.class);
        OrderResponse body = resp.getBody();
        if (resp.getStatusCode().is2xxSuccessful() && body != null &&
            "PROCESSED".equals(body.getStatus())) {
                return;
            }
            try { Thread.sleep(200); } catch (InterruptedException ignored) { }
        }
        throw new AssertionError("Pedido não chegou a PROCESSED dentro do timeout: " + orderId);
    }

    private OrderRequest buildRequest() {
        OrderRequest request = new OrderRequest();
    request.setCustomerId("customer-cache");
    request.setItems(List.of(new OrderItemDto(ITEM, 1)));
    // deliveryAddress is no longer part of the request; it will be resolved later in processing
        return request;
    }
}
