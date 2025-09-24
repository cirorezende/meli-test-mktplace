package br.com.ml.mktplace.orders.adapter.outbound.http;

import br.com.ml.mktplace.orders.domain.model.ExternalServiceException;
import br.com.ml.mktplace.orders.domain.port.DistributionCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * HTTP implementation of DistributionCenterService.
 * Integrates with external service to fetch distribution center information.
 */
@Component
@org.springframework.context.annotation.Profile("!dev")
public class HttpDistributionCenterService implements DistributionCenterService {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpDistributionCenterService.class);
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    
    @Autowired
    public HttpDistributionCenterService(
            RestTemplate restTemplate,
            @Value("${app.distribution-center.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }
    
    @Override
    public List<String> findDistributionCentersByItem(String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) {
            throw new IllegalArgumentException("Item ID cannot be null or empty");
        }

        try {
            // New single-item-only endpoint: GET /distribuitioncenters?itemId={id}
            String url = baseUrl + "/distribuitioncenters?itemId=" + itemId;

            logger.debug("Fetching distribution centers for item: {} via {}", itemId, url);
            String[] codes = restTemplate.getForObject(url, String[].class);

            if (codes == null) {
                logger.debug("No distribution centers found for item: {}", itemId);
                return List.of();
            }

            List<String> list = Arrays.stream(codes).toList();
            logger.debug("Found {} distribution centers for item: {}", list.size(), itemId);
            return list;

        } catch (RestClientException e) {
            logger.error("Failed to fetch distribution centers for item: {}", itemId, e);
            throw new ExternalServiceException("DistributionCenterService", "Failed to fetch distribution centers for item: " + itemId, e);
        }
    }
}