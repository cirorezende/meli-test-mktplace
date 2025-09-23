package br.com.ml.mktplace.orders.adapter.outbound.http;

import br.com.ml.mktplace.orders.domain.model.Address;
import br.com.ml.mktplace.orders.domain.model.DistributionCenter;
import br.com.ml.mktplace.orders.domain.port.DistributionCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Fallback in-memory implementation for development profile to avoid external HTTP dependency
 * when processing orders. Provides a small static list of distribution centers.
 */
@Component
@Profile("dev")
public class DevFallbackDistributionCenterService implements DistributionCenterService {

    private static final Logger log = LoggerFactory.getLogger(DevFallbackDistributionCenterService.class);

    private final List<DistributionCenter> staticCenters;

    public DevFallbackDistributionCenterService() {
        Address spAddress = new Address(
                "Rua Exemplo",
                "Sao Paulo",
                "SP",
                "Brasil",
                "01000-000",
                new Address.Coordinates(new BigDecimal("-23.5505"), new BigDecimal("-46.6333"))
        );
        Address rjAddress = new Address(
                "Av Atlântica",
                "Rio de Janeiro",
                "RJ",
                "Brasil",
                "22010-000",
                new Address.Coordinates(new BigDecimal("-22.9707"), new BigDecimal("-43.1824"))
        );
        this.staticCenters = List.of(
                new DistributionCenter("DC-SP-01", "Centro SP 01", spAddress),
                new DistributionCenter("DC-RJ-01", "Centro RJ 01", rjAddress)
        );
    }

    @Override
    public List<DistributionCenter> findDistributionCentersByItem(String itemId) {
        log.debug("[dev-fallback] Returning static centers for item {}", itemId);
        return staticCenters;
    }

    @Override
    public List<DistributionCenter> findDistributionCentersByItems(List<String> itemIds) {
        log.debug("[dev-fallback] Returning static centers for {} items", itemIds.size());
        return staticCenters;
    }

    @Override
    public List<DistributionCenter> findAllDistributionCenters() {
        log.debug("[dev-fallback] Returning static centers ({} centers)", staticCenters.size());
        return staticCenters;
    }
}
