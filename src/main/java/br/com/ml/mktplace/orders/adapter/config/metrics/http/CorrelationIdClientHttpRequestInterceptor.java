package br.com.ml.mktplace.orders.adapter.config.metrics.http;

import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Propagates correlationId from MDC to HTTP outbound calls as X-Correlation-Id header.
 */
public class CorrelationIdClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    @Override
    public @org.springframework.lang.NonNull ClientHttpResponse intercept(
            @org.springframework.lang.NonNull HttpRequest request,
            @org.springframework.lang.NonNull byte[] body,
            @org.springframework.lang.NonNull ClientHttpRequestExecution execution) throws IOException {
        String correlationId = MDC.get(MDC_KEY);
        if (correlationId != null && !correlationId.isBlank()) {
            request.getHeaders().set(CORRELATION_ID_HEADER, correlationId);
        }
        return execution.execute(request, body);
    }
}
