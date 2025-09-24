# Tarefa 10 - Implementação de Observabilidade

## 🚧 Status: EM PROGRESSO (Atualizado: 23/09/2025)

Implementação avançada: logging estruturado (Logback + JSON), filtro Correlation ID ativo, métricas Prometheus expostas, contadores de pedidos processados/falhos, tempo de processamento, distribuição de itens, seleção de CDs e métricas de cache (hit/miss) adicionadas. Cliente HTTP externo instrumentado com métricas (latência/status) e correlação propagada; contadores Kafka (publicados/erros) adicionados. Falta apenas definição de dashboards e, opcionalmente, métricas de banco.

## Objetivo

Implementar logging estruturado, métricas e tracing leve para monitoramento e troubleshooting do sistema.

## Descrição

Configurar observabilidade completa seguindo as definições do ADR-013, com logs estruturados, métricas de negócio e técnicas, health checks e correlação fim‑a‑fim via correlation IDs.

## Critérios de Aceitação

- [x] Logging estruturado em JSON com correlation ID (não-dev em arquivo + console)
- [x] Métricas customizadas com Micrometer (counters básicos)
- [x] Correlation ID propagado em todas as operações HTTP (filtro servlet)
- [x] Logs de auditoria para decisões de CD (INFO + DEBUG na seleção e distâncias)
- [x] Métricas de performance e negócio adicionais (latências, seleção de CD, cache hit/miss)
- [x] Health checks configurados (actuator + probes)
- [ ] Dashboards básicos definidos (Grafana / Kibana – pendente)
- [x] Métricas externas API de CDs (latência / status codes)
- [ ] Métricas de operações de banco (latência agregada por tipo) / opcional

## Logging Estruturado

### Configuração Logback (Implementado)

- Formato JSON (RollingFile + JSON encoder) para ambientes não-dev
- Formato legível (pattern) para `dev`
- Inclusão de `correlationId` via MDC
- Rolling diário com retenção de 7 dias (simplificado)

### Campos Obrigatórios

```json
{
  "timestamp": "2025-09-21T10:30:00Z",
  "level": "INFO",
  "service": "orders-system",
  "correlationId": "01ARZ3NDEKTSV4RRFFQ69G5FAV",
  "message": "Order processed successfully",
  "orderId": "01ARZ3NDEKTSV4RRFFQ69G5FAV",
  "processingTimeMs": 1250
}
```

### Eventos de Log Importantes

- Início/fim de processamento de pedidos
- Seleção de CD para cada item
- Chamadas à API externa (sucesso/falha) – LOGAR (pendente detalhe de status/latência)
- Cache hit/miss
- Publicação de eventos

## Métricas com Micrometer

### Métricas de Negócio (Atuais)

- `orders.processed.total`  
- `orders.failed.total`  
- `orders.processing.duration` (Timer)  
- `orders.items.per.order` (DistributionSummary)  
- `distribution.centers.selected` (counter com tag code)  

### Métricas Técnicas (Atualizadas)

- `cache.operations.total` (hit/miss por cache/op)  
- `external.http.client.requests` (Timer) — tags: `service`, `method`, `uri`, `status`, `outcome`, `exception`  
  - `service=distribution-centers-api` para a API de CDs; `service=external-generic` para demais clientes  
- `kafka.events.published.total{type}` e `kafka.events.errors.total{type}`  
- (Opcional) `database.operations.duration` (Timer por operação)  

### Métricas de Sistema (Spring / Actuator)

- JVM (heap, GC, threads)  
- HTTP server (latência, códigos)  
- Connection pool (Hikari)  

## Correlation ID

- Geração automática por request
- Propagação em MDC (`correlationId`)
- Header de saída `X-Correlation-Id`
- Propagado para eventos publicados (payload inclui correlationId)
- Propagado para chamadas HTTP externas via interceptor (`X-Correlation-Id`)

## Health Checks

- `/actuator/health` com readiness/liveness básico
- Banco / Redis / Kafka (quando aplicável) / API externa (pendente implementação de check ativo)

## Roadmap de Dashboards (Pendente)

| Painel | Gráfico | Métrica | Insight |
|--------|---------|---------|---------|
| Pedidos | Throughput | rate(sum(orders_processed_total[5m])) | Volume processado |
| Pedidos | Taxa Falhas | (increase(orders_failed_total[5m]) / increase(orders_processed_total[5m])) | Qualidade do processamento |
| Latência | P95/P99 | histogram_quantile(0.95, sum by (le)(rate(orders_processing_duration_seconds_bucket[5m]))) | Desempenho |
| Itens | Distribuição | orders_items_per_order_count / sum(orders_items_per_order_count) | Perfil dos pedidos |
| CDs | Top CDs | topk(5, increase(distribution_centers_selected_total[1h])) | Carga por CD |
| Cache | Hit Ratio | sum(increase(cache_operations_total{op="hit"}[5m])) / sum(increase(cache_operations_total[5m])) | Efetividade do cache |
| API Externa | Latência | (Timer a definir) | Performance integração |
| API Externa | Taxa Erros | increase(external_http_client_requests_seconds_count{service="distribution-centers-api",status!~"2.."}[5m]) / increase(external_http_client_requests_seconds_count{service="distribution-centers-api"}[5m]) | Saúde integração |

## Como verificar rapidamente (dev)

1. Listar métricas: `GET /actuator/metrics`
2. Inspecionar HTTP externo: `GET /actuator/metrics/external.http.client.requests`
3. Inspecionar Kafka publish: `GET /actuator/metrics/kafka.events.published.total`
4. Scrape geral (Prometheus): `GET /actuator/prometheus`

Observação: execute um fluxo que faça chamadas à API de CDs para popular as séries (`service="distribution-centers-api"`).

## Próximas Ações (Para concluir a tarefa)

1. Instrumentar cliente HTTP de CDs com Timer + counter por status.  
2. Adicionar Timer opcional para operações críticas de repositório.  
3. Criar documento `observability-dashboards.md` com queries sugeridas (Grafana).  
4. Adicionar exemplo de log de erro enriquecido.  
5. Marcar tarefa como concluída após dashboard + métricas externas.  

## ADRs Relacionados

- ADR-013: Observabilidade e monitoramento  
- ADR-008: ULID para correlation IDs  

## Notas

Esta tarefa já habilita diagnósticos de performance e comportamento de pedidos. A conclusão final depende apenas da camada de visualização (dashboards) e métricas da API externa.
