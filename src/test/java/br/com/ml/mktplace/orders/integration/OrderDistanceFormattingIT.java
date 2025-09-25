package br.com.ml.mktplace.orders.integration;

import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.AddressDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderItemDto;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderRequest;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que distanceKm é serializado com exatamente duas casas decimais no JSON de resposta.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderDistanceFormattingIT extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void distanceDeveTerDuasCasasDecimais() {
        OrderRequest request = new OrderRequest();
        request.setCustomerId("customer-distance");
        request.setItems(List.of(new OrderItemDto("FMT-001", 1)));
        AddressDto addr = new AddressDto();
        addr.setStreet("Rua Format");
        addr.setNumber("1");
        addr.setCity("São Paulo");
        addr.setState("SP");
        addr.setCountry("BR");
        addr.setZipCode("06000-000");
        request.setDeliveryAddress(addr);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<OrderResponse> resp = restTemplate.postForEntity("/v1/orders", new HttpEntity<>(request, headers), OrderResponse.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    OrderResponse body = resp.getBody();
    assertThat(body).as("Corpo da resposta não deve ser nulo").isNotNull();
    if (body == null) return; // safety for static analysis
    assertThat(body.getItems()).as("Lista de itens não deve ser vazia").isNotNull().isNotEmpty();
    OrderItemDto item = body.getItems().get(0);
        if (item.getAvailableDistributionCenters() != null && !item.getAvailableDistributionCenters().isEmpty()) {
            var dc = item.getAvailableDistributionCenters().get(0);
            // Serialização BigDecimal com 2 casas implica string JSON com duas casas; aqui verificamos valor escala
            java.math.BigDecimal distance = dc.distanceKm;
            assertThat(distance.scale()).isEqualTo(2);
        }
    }
}
