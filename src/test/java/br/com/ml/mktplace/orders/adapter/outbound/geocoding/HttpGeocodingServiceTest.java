package br.com.ml.mktplace.orders.adapter.outbound.geocoding;

import br.com.ml.mktplace.orders.domain.model.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpGeocodingServiceTest {

    RestTemplate restTemplate;
    HttpGeocodingService service;

    @BeforeEach
    void setup() {
        restTemplate = mock(RestTemplate.class);
        service = new HttpGeocodingService(restTemplate, "API_KEY", "https://api.api-ninjas.com/v1/geocoding");
    }

    @Test
    void geocode_returnsCoordinatesOnSuccess() {
        // given
        List<Map<String,Object>> body = List.of(Map.of(
                "latitude", -23.5505,
                "longitude", -46.6333
        ));
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(java.util.List.class)))
        .thenReturn(ResponseEntity.ok(body));

        // when
        Address.Coordinates coords = service.geocode("Rua", "10", "Sao Paulo", "SP", "BR", "01000-000");

        // then
        assertNotNull(coords);
        assertEquals(BigDecimal.valueOf(-23.5505), coords.latitude());
        assertEquals(BigDecimal.valueOf(-46.6333), coords.longitude());
    }

    @Test
    void geocode_returnsNullWhenEmpty() {
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(java.util.List.class)))
        .thenReturn(ResponseEntity.ok(List.of()));
        Address.Coordinates coords = service.geocode("Rua", "10", "Cidade", "ST", "BR", "00000-000");
        assertNull(coords);
    }

    @Test
    void geocode_returnsNullOnException() {
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(java.util.List.class)))
        .thenThrow(new RuntimeException("boom"));
        Address.Coordinates coords = service.geocode("Rua", "10", "Cidade", "ST", "BR", "00000-000");
        assertNull(coords);
    }
}
