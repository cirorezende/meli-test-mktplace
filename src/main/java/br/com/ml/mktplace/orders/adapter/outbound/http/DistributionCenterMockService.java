package br.com.ml.mktplace.orders.adapter.outbound.http;

import br.com.ml.mktplace.orders.domain.port.DistributionCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * ÚNICA implementação mock de {@link DistributionCenterService}.
 * Remove totalmente a dependência de HTTP/WireMock para centros de distribuição.
 * Retorna um subconjunto aleatório (1..N, limitado a 5) de códigos estáticos a cada chamada.
 */
@Component
public class DistributionCenterMockService implements DistributionCenterService {

    private static final Logger log = LoggerFactory.getLogger(DistributionCenterMockService.class);
    private static final List<String> STATIC_CODES = List.of("SP-001", "RJ-001", "MG-001", "PR-001", "BA-001");
    private static final Random RANDOM = new Random();

    @Override
    public List<String> findDistributionCentersByItem(String itemId) {
        int max = Math.min(5, STATIC_CODES.size());
        int count = 1 + RANDOM.nextInt(max); // 1..max
        ArrayList<String> shuffled = new ArrayList<>(STATIC_CODES);
        Collections.shuffle(shuffled, RANDOM);
        ArrayList<String> picked = new ArrayList<>(shuffled.subList(0, count));
        picked.sort(Comparator.naturalOrder());
        log.debug("[dc-mock] itemId={} -> returning {} codes {}", itemId, picked.size(), picked);
        return picked;
    }
}
