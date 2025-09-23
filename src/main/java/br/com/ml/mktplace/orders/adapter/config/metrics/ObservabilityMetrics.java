package br.com.ml.mktplace.orders.adapter.config.metrics;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Centraliza métricas de observabilidade do domínio de pedidos.
 */
@Component
public class ObservabilityMetrics {

    private final MeterRegistry registry;
    private final Timer processingTimer;
    private final DistributionSummary itemsPerOrder;

    public ObservabilityMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.processingTimer = Timer.builder("orders.processing.duration")
                .description("Tempo de processamento de pedidos")
                .publishPercentileHistogram()
                .register(registry);

        this.itemsPerOrder = DistributionSummary.builder("orders.items.per.order")
                .description("Distribuição de itens por pedido")
                .publishPercentileHistogram()
                .register(registry);
    }

    public <T> T recordProcessing(Supplier<T> supplier) {
        long start = System.nanoTime();
        try {
            return supplier.get();
        } finally {
            long end = System.nanoTime();
            processingTimer.record(end - start, TimeUnit.NANOSECONDS);
        }
    }

    public void recordItemsPerOrder(int items) {
        if (items >= 0) {
            itemsPerOrder.record(items);
        }
    }

    public void incrementDcSelection(String dcCode) {
        registry.counter("distribution.centers.selected", List.of(Tag.of("code", dcCode == null ? "UNKNOWN" : dcCode))).increment();
    }

    public void cacheHit(String cacheName) {
        registry.counter("cache.operations.total", List.of(Tag.of("cache", cacheName), Tag.of("op", "hit"))).increment();
    }

    public void cacheMiss(String cacheName) {
        registry.counter("cache.operations.total", List.of(Tag.of("cache", cacheName), Tag.of("op", "miss"))).increment();
    }
}
