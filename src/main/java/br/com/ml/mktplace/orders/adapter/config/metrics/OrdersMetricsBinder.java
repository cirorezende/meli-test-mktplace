package br.com.ml.mktplace.orders.adapter.config.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

/**
 * Registers legacy business metrics related to order processing.
 * New aggregations and timers are managed by {@link ObservabilityMetrics}.
 * Counters remain here for backwards compatibility; can be merged later.
 */
@Component
public class OrdersMetricsBinder implements MeterBinder {

    public static final String ORDERS_PROCESSED_TOTAL = "orders.processed.total";
    public static final String ORDERS_FAILED_TOTAL = "orders.failed.total";

    private Counter processedCounter;
    private Counter failedCounter;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.processedCounter = Counter.builder(ORDERS_PROCESSED_TOTAL)
                .description("Total de pedidos processados com sucesso")
                .register(registry);

        this.failedCounter = Counter.builder(ORDERS_FAILED_TOTAL)
                .description("Total de pedidos que falharam no processamento")
                .register(registry);
    }

    public void incrementProcessed() {
        if (processedCounter != null) {
            processedCounter.increment();
        }
    }

    public void incrementFailed() {
        if (failedCounter != null) {
            failedCounter.increment();
        }
    }
}
