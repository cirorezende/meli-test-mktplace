package br.com.ml.mktplace.orders.adapter.inbound.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter responsável por garantir a presença de um correlationId
 * em cada requisição HTTP para rastreabilidade ponta a ponta.
 *
 * Estratégia: reutiliza header existente (X-Correlation-Id ou Correlation-Id)
 * ou gera um novo ULID-like fallback (UUID simplificado aqui para evitar nova dependência).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements Filter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String correlationId = extractOrGenerate(httpRequest);
        MDC.put(MDC_KEY, correlationId);
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private String extractOrGenerate(HttpServletRequest request) {
        String id = request.getHeader(CORRELATION_ID_HEADER);
        if (id == null || id.isBlank()) {
            id = request.getHeader("Correlation-Id");
        }
        if (id == null || id.isBlank()) {
            // UUID usado como fallback rápido. Pode ser substituído por ULID se necessário.
            id = UUID.randomUUID().toString();
        }
        return id;
    }
}
