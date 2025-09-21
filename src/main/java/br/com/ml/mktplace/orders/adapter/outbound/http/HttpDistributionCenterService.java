package br.com.ml.mktplace.orders.adapter.outbound.http;

import br.com.ml.mktplace.orders.adapter.outbound.http.dto.DistributionCenterDto;
import br.com.ml.mktplace.orders.domain.model.Address;
import br.com.ml.mktplace.orders.domain.model.DistributionCenter;
import br.com.ml.mktplace.orders.domain.model.ExternalServiceException;
import br.com.ml.mktplace.orders.domain.port.DistributionCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * HTTP implementation of DistributionCenterService.
 * Integrates with external service to fetch distribution center information.
 */
@Component
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
    public List<DistributionCenter> findDistributionCentersByItem(String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) {
            throw new IllegalArgumentException("Item ID cannot be null or empty");
        }
        
        try {
            String url = baseUrl + "/distribution-centers/item/" + itemId;
            
            logger.debug("Fetching distribution centers for item: {}", itemId);
            DistributionCenterDto[] dtos = restTemplate.getForObject(url, DistributionCenterDto[].class);
            
            if (dtos == null) {
                logger.debug("No distribution centers found for item: {}", itemId);
                return List.of();
            }
            
            List<DistributionCenter> distributionCenters = Arrays.stream(dtos)
                    .map(this::mapToDomain)
                    .toList();
            
            logger.debug("Found {} distribution centers for item: {}", distributionCenters.size(), itemId);
            return distributionCenters;
            
        } catch (RestClientException e) {
            logger.error("Failed to fetch distribution centers for item: {}", itemId, e);
            throw new ExternalServiceException("DistributionCenterService", "Failed to fetch distribution centers for item: " + itemId, e);
        }
    }
    
    @Override
    public List<DistributionCenter> findDistributionCentersByItems(List<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            throw new IllegalArgumentException("Item IDs list cannot be null or empty");
        }
        
        try {
            String url = baseUrl + "/distribution-centers/items";
            
            logger.debug("Fetching distribution centers for {} items", itemIds.size());
            
            // Send POST request with item IDs in the body
            DistributionCenterDto[] dtos = restTemplate.postForObject(url, itemIds, DistributionCenterDto[].class);
            
            if (dtos == null) {
                logger.debug("No distribution centers found for items");
                return List.of();
            }
            
            List<DistributionCenter> distributionCenters = Arrays.stream(dtos)
                    .map(this::mapToDomain)
                    .toList();
            
            logger.debug("Found {} distribution centers for items", distributionCenters.size());
            return distributionCenters;
            
        } catch (RestClientException e) {
            logger.error("Failed to fetch distribution centers for items", e);
            throw new ExternalServiceException("DistributionCenterService", "Failed to fetch distribution centers for items", e);
        }
    }
    
    @Override
    public List<DistributionCenter> findAllDistributionCenters() {
        try {
            String url = baseUrl + "/distribution-centers";
            
            logger.debug("Fetching all distribution centers");
            DistributionCenterDto[] dtos = restTemplate.getForObject(url, DistributionCenterDto[].class);
            
            if (dtos == null) {
                logger.debug("No distribution centers found");
                return List.of();
            }
            
            List<DistributionCenter> distributionCenters = Arrays.stream(dtos)
                    .map(this::mapToDomain)
                    .toList();
            
            logger.debug("Found {} distribution centers", distributionCenters.size());
            return distributionCenters;
            
        } catch (RestClientException e) {
            logger.error("Failed to fetch all distribution centers", e);
            throw new ExternalServiceException("DistributionCenterService", "Failed to fetch all distribution centers", e);
        }
    }
    
    private DistributionCenter mapToDomain(DistributionCenterDto dto) {
        Address address = new Address(
                dto.getStreet(),
                dto.getCity(),
                dto.getState(),
                dto.getCountry(),
                dto.getZipCode(),
                new Address.Coordinates(
                        BigDecimal.valueOf(dto.getLatitude()),
                        BigDecimal.valueOf(dto.getLongitude())
                )
        );
        
        return new DistributionCenter(
                dto.getCode(),
                dto.getName(),
                address
        );
    }
}