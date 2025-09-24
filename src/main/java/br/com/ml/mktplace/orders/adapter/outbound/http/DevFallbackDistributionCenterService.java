package br.com.ml.mktplace.orders.adapter.outbound.http;

import br.com.ml.mktplace.orders.domain.port.DistributionCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fallback in-memory implementation for development profile to avoid external HTTP dependency
 * when processing orders. Provides a small static list of distribution centers.
 */
@Component
@Profile("dev")
public class DevFallbackDistributionCenterService implements DistributionCenterService {

    private static final Logger log = LoggerFactory.getLogger(DevFallbackDistributionCenterService.class);

    private final List<String> staticCodes;

    public DevFallbackDistributionCenterService() {
    this.staticCodes = List.of("SP-001", "RJ-001", "MG-001", "PR-001", "BA-001");
    }

    @Override
    public List<String> findDistributionCentersByItem(String itemId) {
        log.debug("[dev-fallback] Returning codes for item {}", itemId);
        // simple randomized subset between 1 and size
        int max = Math.min(5, staticCodes.size());
        int count = 1 + new java.util.Random().nextInt(max);
        java.util.List<String> shuffled = new java.util.ArrayList<>(staticCodes);
        java.util.Collections.shuffle(shuffled);
        java.util.List<String> picked = new java.util.ArrayList<>(shuffled.subList(0, count));
        picked.sort(java.util.Comparator.naturalOrder());
        return picked;
    }

    // Métodos de múltiplos itens e de "todos os CDs" foram removidos por regra de negócio.
}
