package br.com.ml.mktplace.orders.integration;

import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderRequest;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderItemDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.AddressDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderResponse;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa comportamento de cache: duas criações de pedidos com mesmo item devem
 * resultar em apenas uma chamada ao endpoint externo de Distribution Centers.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DistributionCenterCachingIT extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String ITEM = "CACHE1";

    @BeforeEach
    void resetWireMock() {
        WireMock.reset();
        stubFor(get(urlPathMatching("/distribution-centers/item/" + ITEM))
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

        ResponseEntity<OrderResponse> r1 = restTemplate.postForEntity("/api/v1/orders", new HttpEntity<>(req1, headers), OrderResponse.class);
        assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<OrderResponse> r2 = restTemplate.postForEntity("/api/v1/orders", new HttpEntity<>(req2, headers), OrderResponse.class);
        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Verifica que o endpoint externo foi chamado apenas uma vez
        verify(1, getRequestedFor(urlPathMatching("/distribution-centers/item/" + ITEM)));
    }

    private OrderRequest buildRequest() {
        OrderRequest request = new OrderRequest();
    request.setCustomerId("customer-cache");
    request.setItems(List.of(new OrderItemDto(ITEM, 1)));
    AddressDto address = new AddressDto(
        "Rua Cache",
        "São Paulo",
        "SP",
        "BR",
        "01000-000",
        new AddressDto.CoordinatesDto(new BigDecimal("-23.5"), new BigDecimal("-46.6"))
    );
    request.setDeliveryAddress(address);
        return request;
    }
}
