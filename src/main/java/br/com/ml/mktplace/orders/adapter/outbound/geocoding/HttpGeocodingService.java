package br.com.ml.mktplace.orders.adapter.outbound.geocoding;

import br.com.ml.mktplace.orders.domain.model.Address;
import br.com.ml.mktplace.orders.domain.port.GeocodingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * HTTP adapter calling api-ninjas geocoding API.
 * API docs: https://api-ninjas.com/api/geocoding
 */
@Service
public class HttpGeocodingService implements GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(HttpGeocodingService.class);
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    public HttpGeocodingService(RestTemplate restTemplate,
                                @Value("${app.geocoding.api-key:}") String apiKey,
                                @Value("${app.geocoding.base-url:https://api.api-ninjas.com/v1/geocoding}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @Override
    public Address.Coordinates geocode(String street, String number, String city, String state, String country, String zipCode) {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                log.warn("Geocoding disabled: missing API key");
                return null;
            }
            StringBuilder sb = new StringBuilder(baseUrl)
                    .append("?city=").append(encode(city))
                    .append("&country=").append(encode(country));
            // API supports optional parameters; include state if provided.
            if (state != null && !state.isBlank()) {
                sb.append("&state=").append(encode(state));
            }
            // Street/number not always used by API, but we log for context.
            URI uri = URI.create(sb.toString());
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Api-Key", apiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
        @SuppressWarnings("rawtypes")
        ResponseEntity<java.util.List> resp = restTemplate.exchange(
            uri, HttpMethod.GET, entity, java.util.List.class);
            @SuppressWarnings("unchecked")
            java.util.List<java.util.Map<String,Object>> body = resp.getBody();
            if (!resp.getStatusCode().is2xxSuccessful() || body == null || body.isEmpty()) {
                log.warn("Geocoding not found for {} {} - {} / {} {}", street, number, city, state, country);
                return null;
            }
            // Response is an array of objects with latitude & longitude
            Object first = body.get(0);
            if (first instanceof java.util.Map<?,?> map) {
                Object lat = map.get("latitude");
                Object lon = map.get("longitude");
                if (lat != null && lon != null) {
                    return new Address.Coordinates(new BigDecimal(lat.toString()), new BigDecimal(lon.toString()));
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("Geocoding call failed: {}", e.getMessage());
            return null; // Fail soft; processing can continue with zero coords (distances may degrade)
        }
    }

    private String encode(String v) { return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8); }
}
