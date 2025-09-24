package br.com.ml.mktplace.orders.adapter.config.metrics.http;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Micrometer-based interceptor to time external HTTP calls
 * and record status/outcome per service and endpoint path.
 */
public class HttpClientMetricsInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HttpClientMetricsInterceptor.class);
    private static final String METRIC_NAME = "external.http.client.requests";

    private final MeterRegistry registry;
    private final String serviceName;

    public HttpClientMetricsInterceptor(MeterRegistry registry, String serviceName) {
        this.registry = registry;
        this.serviceName = serviceName == null ? "default" : serviceName;
    }

    @Override
    public @org.springframework.lang.NonNull ClientHttpResponse intercept(
        @org.springframework.lang.NonNull HttpRequest request,
        @org.springframework.lang.NonNull byte[] body,
        @org.springframework.lang.NonNull ClientHttpRequestExecution execution) throws IOException {
        long start = System.nanoTime();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        URI uri = request.getURI();
        String path = uri != null ? uri.getPath() : "unknown";
        String exception = "none";
        int status = 0;
        try {
            ClientHttpResponse response = execution.execute(request, body);
            try {
                status = response.getStatusCode().value();
            } catch (Exception ignored) {
                status = 0;
            }
            return response;
        } catch (IOException ex) {
            exception = ex.getClass().getSimpleName();
            throw ex;
        } finally {
            long end = System.nanoTime();
            recordTimer(Duration.ofNanos(end - start), method, path, status, exception);
        }
    }

    private void recordTimer(Duration duration, String method, String path, int status, String exception) {
        String outcome = outcomeForStatus(status);
        List<Tag> tags = new ArrayList<>();
        tags.add(Tag.of("service", serviceName));
        tags.add(Tag.of("method", method));
        tags.add(Tag.of("uri", path));
        tags.add(Tag.of("status", status == 0 ? "IO_ERROR" : Integer.toString(status)));
        tags.add(Tag.of("outcome", outcome));
        tags.add(Tag.of("exception", exception));
        try {
            Timer.builder(METRIC_NAME)
                    .tags(tags)
                    .publishPercentileHistogram()
                    .register(registry)
                    .record(duration);
        } catch (Exception e) {
            log.debug("Failed to record HTTP client metric: {} {} -> {}", method, path, status, e);
        }
    }

    private String outcomeForStatus(int status) {
        if (status >= 100 && status < 200) return "INFORMATIONAL";
        if (status >= 200 && status < 300) return "SUCCESS";
        if (status >= 300 && status < 400) return "REDIRECTION";
        if (status >= 400 && status < 500) return "CLIENT_ERROR";
        if (status >= 500 && status < 600) return "SERVER_ERROR";
        return status == 0 ? "IO_ERROR" : "UNKNOWN";
    }
}
