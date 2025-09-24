package br.com.ml.mktplace.orders.integration;

import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderItemDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderRequest;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderResponse;
import br.com.ml.mktplace.orders.integration.support.SharedWireMock;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test validating asynchronous flow: create order -> ORDER_CREATED event -> listener consumes -> processes order -> status PROCESSED
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderEndToEndProcessingIT extends BaseIntegrationTest {

    // Shared server managed centrally

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void startWireMock() { SharedWireMock.startIfNeeded(); }

    @AfterAll
    static void stopWireMock() { }

    @BeforeEach
    void setupStubs() {
        String itemId = "ASYNC-PROC-1";
    stubFor(get(urlPathMatching("/distribution-centers/item/" + itemId))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("[\"SP-001\"]")
        ));
        stubFor(get(urlPathMatching("/distribution-centers"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
            .withBody("[\"SP-001\"]")
                ));
    }

    @Test
    @DisplayName("Deve processar pedido assincronamente via evento ORDER_CREATED")
    void shouldProcessOrderAsynchronouslyFromCreatedEvent() {
        // Arrange
        String itemId = "ASYNC-PROC-1";
        OrderRequest request = new OrderRequest();
        request.setCustomerId("customer-async-proc");
        request.setItems(List.of(new OrderItemDto(itemId, 1)));
    // deliveryAddress removed from request; will be resolved later during async processing

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Correlation-ID", UUID.randomUUID().toString());

        // Act - create order
    ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity("/v1/orders", new HttpEntity<>(request, headers), OrderResponse.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    OrderResponse createdBody = createResponse.getBody();
    assertThat(createdBody).isNotNull();
    String orderId = createdBody != null ? createdBody.getId() : null;
        assertThat(orderId).isNotBlank();

        // Await processing -> polling the GET endpoint until status PROCESSED or timeout
        Instant start = Instant.now();
    OrderResponse current = createdBody;
        OrderResponse lastNonNull = current;
        OrderResponse finalState = null;
        long maxWaitMs = 15_000; // generous for container spin latency
        while (Duration.between(start, Instant.now()).toMillis() < maxWaitMs) {
            ResponseEntity<OrderResponse> get = restTemplate.getForEntity("/v1/orders/" + orderId, OrderResponse.class);
            if (get.getStatusCode().is2xxSuccessful() && get.getBody() != null) {
                current = get.getBody();
                lastNonNull = current;
                if (current != null && ("PROCESSED".equals(current.getStatus()) || "FAILED".equals(current.getStatus()))) {
                    finalState = current;
                    break;
                }
            }
            try { Thread.sleep(500); } catch (InterruptedException ignored) { }
        }

        assertThat(finalState != null ? finalState : lastNonNull).is(new Condition<>(
                or -> {
                    if (or == null) return false;
                    // Accept processed; failed would indicate an issue - we assert processed specifically
                    return "PROCESSED".equals(or.getStatus());
                }, "status should become PROCESSED"
        ));
    }
}
