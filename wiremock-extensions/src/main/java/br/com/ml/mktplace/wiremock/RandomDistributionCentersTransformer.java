package br.com.ml.mktplace.wiremock;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WireMock transformer que devolve subconjunto aleatório de centros de distribuição.
 * - Tamanho aleatório entre 1 e 5
 * - Elementos aleatórios sem repetição da lista base.
 */
public class RandomDistributionCentersTransformer extends ResponseDefinitionTransformer {

    private static final Logger log = LoggerFactory.getLogger(RandomDistributionCentersTransformer.class);

    private static final List<String> BASE = List.of("SP-001","RJ-001","MG-001","RS-001","PR-001");
    private static final Random RANDOM = new Random();

    @Override
    public String getName() {
        return "random-distribution-centers";
    }

    @Override
    public boolean applyGlobally() { return false; }

    @Override
    public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters, Admin admin) {
        try {
            int size = 1 + RANDOM.nextInt(BASE.size()); // 1..5
            List<String> shuffled = new ArrayList<>(BASE);
            Collections.shuffle(shuffled, RANDOM);
            List<String> subset = shuffled.stream().limit(size).collect(Collectors.toList());
            String body = toJsonArray(subset);
            return new ResponseDefinitionBuilder()
                    .withStatus(200)
                    .withHeader("Content-Type","application/json")
                    .withBody(body.getBytes(StandardCharsets.UTF_8))
                    .build();
        } catch (Exception e) {
            log.warn("Falling back to static list due to error generating random subset", e);
            return new ResponseDefinitionBuilder()
                    .withStatus(200)
                    .withHeader("Content-Type","application/json")
                    .withBody("[\"SP-001\",\"RJ-001\",\"MG-001\",\"RS-001\",\"PR-001\"]")
                    .build();
        }
    }

    private String toJsonArray(List<String> values) {
        return values.stream().map(v -> "\"" + v + "\"")
                .collect(Collectors.joining(",","[","]"));
    }
}
