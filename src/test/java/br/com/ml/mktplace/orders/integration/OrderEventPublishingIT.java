package br.com.ml.mktplace.orders.integration;

import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderRequest;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderItemDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.AddressDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderResponse;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import com.github.tomakehurst.wiremock.client.WireMock;
import br.com.ml.mktplace.orders.integration.support.SharedWireMock;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de publicação de eventos. Neste estágio o publisher apenas loga; esse teste serve
 * como ponto de extensão futuro: quando Kafka real for integrado, basta remover comentários
 * do envio e validar consumo. Por ora validamos que o fluxo de pedido roda sem exceções.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderEventPublishingIT extends BaseIntegrationTest {

    @BeforeEach
    void ensureWireMock() { SharedWireMock.startIfNeeded(); }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fluxoPublicaEventosSemErro() {
        // Arrange
        OrderRequest request = new OrderRequest();
    request.setCustomerId("customer-events");
    request.setItems(List.of(new OrderItemDto("EVT123", 1)));
    AddressDto addr = new AddressDto(
        "Rua Eventos",
        "São Paulo",
        "SP",
        "BR",
        "01000-000",
        new AddressDto.CoordinatesDto(new BigDecimal("-23.5"), new BigDecimal("-46.6"))
    );
    request.setDeliveryAddress(addr);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Stub WireMock para item EVT123
    WireMock.stubFor(
        WireMock.get(
                WireMock.urlPathMatching("/distribution-centers/item/EVT123"))
            .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("[{\"code\":\"CDX\",\"name\":\"CD X\",\"street\":\"Rua X\",\"city\":\"SP\",\"state\":\"SP\",\"country\":\"BR\",\"zipCode\":\"01000-000\",\"latitude\":-23.6,\"longitude\":-46.7}]")
                        )
        );

        // Act
        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity("/api/v1/orders", new HttpEntity<>(request, headers), OrderResponse.class);

        // Assert
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    OrderResponse body = createResponse.getBody();
    assertThat(body).isNotNull();
    assertThat(body != null ? body.getId() : null).isNotBlank();
    }
}
