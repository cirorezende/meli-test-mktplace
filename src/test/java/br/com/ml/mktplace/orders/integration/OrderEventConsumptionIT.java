package br.com.ml.mktplace.orders.integration;

import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.AddressDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderItemDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderRequest;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderResponse;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de consumo de evento Kafka: cria pedido, consome evento de criação/processed.
 * Usa o KafkaContainer configurado em BaseIntegrationTest e um consumer manual.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderEventConsumptionIT extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Deve publicar evento de pedido e consumidor lê do tópico")
    void shouldPublishAndConsumeOrderEvent() {
        // Arrange
        String itemId = "EVT-CONSUME-1";
        stubFor(get(urlPathMatching("/distribution-centers/item/" + itemId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"code\":\"CDZ\",\"name\":\"CD Z\",\"street\":\"Rua Z\",\"city\":\"SP\",\"state\":\"SP\",\"country\":\"BR\",\"zipCode\":\"01000-000\",\"latitude\":-23.51,\"longitude\":-46.61}]")
                ));

        OrderRequest request = new OrderRequest();
        request.setCustomerId("customer-event-consumer");
        request.setItems(List.of(new OrderItemDto(itemId, 1)));
        AddressDto addr = new AddressDto(
                "Rua Consumo",
                "São Paulo",
                "SP",
                "BR",
                "01000-000",
                new AddressDto.CoordinatesDto(new BigDecimal("-23.5"), new BigDecimal("-46.6"))
        );
        request.setDeliveryAddress(addr);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Act - cria pedido (deverá disparar eventos)
        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity("/api/v1/orders", new HttpEntity<>(request, headers), OrderResponse.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(createResponse.getBody()).isNotNull();
        String orderId = createResponse.getBody().getId();
        assertThat(orderId).isNotBlank();

        // Kafka consumer manual para ler eventos (default topic configurado em KafkaConfig: orders.events)
        String topic = "orders.events"; // assumindo defaultTopic configurado
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-events-test-group-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        List<ConsumerRecord<String, String>> received = new ArrayList<>();
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of(topic));
            long timeoutMs = 5000;
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < timeoutMs && received.isEmpty()) {
                consumer.poll(Duration.ofMillis(500)).forEach(received::add);
            }
        }

        // Assert - deve ter pelo menos um evento contendo o aggregateId do pedido
        assertThat(received).isNotEmpty();
        boolean found = received.stream().anyMatch(r -> r.value() != null && r.value().contains(orderId));
        assertThat(found)
                .as("Evento relacionado ao pedido criado deve estar presente no tópico")
                .isTrue();
    }
}
