# Tarefa 10 - Implementação de Observabilidade

## ⏳ Status: PENDENTE (Atualizado: 22/09/2025)

Planejado. Implementação ainda não iniciada no código. Próximo passo imediato após estabilização do fluxo de pedidos.

## Objetivo

Implementar logging estruturado, métricas e tracing para monitoramento e troubleshooting do sistema.

## Descrição

Configurar observabilidade completa seguindo as definições do ADR-013, com logs estruturados, métricas de negócio e técnicas, e correlation IDs para rastreamento.

## Critérios de Aceitação

- [ ] Logging estruturado em JSON com correlation ID
- [ ] Métricas customizadas com Micrometer
- [ ] Correlation ID propagado em todas as operações
- [ ] Logs de auditoria para decisões de CD
- [ ] Métricas de performance e negócio
- [ ] Health checks configurados
- [ ] Dashboards básicos definidos

## Logging Estruturado

### Configuração Logback

- Formato JSON para produção
- Formato legível para desenvolvimento
- Níveis apropriados por ambiente
- Rotation e retention policies

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
- Chamadas à API externa (sucesso/falha)
- Cache hit/miss
- Publicação de eventos

## Métricas com Micrometer

### Métricas de Negócio

- `orders.processed.total` - Total de pedidos processados
- `orders.processing.duration` - Tempo de processamento
- `orders.items.per.order` - Distribuição de itens por pedido
- `distribution.centers.selected` - CDs mais utilizados

### Métricas Técnicas

- `api.cd.service.calls.total` - Chamadas à API de CDs
- `api.cd.service.duration` - Latência da API externa
- `cache.operations.total` - Operações de cache (hit/miss)
- `database.operations.duration` - Performance do banco

### Métricas de Sistema

- JVM metrics (heap, GC, threads)
- HTTP metrics (requests, responses, latency)
- Database connection pool metrics

## Correlation ID

- Geração automática para cada request
- Propagação via MDC (Mapped Diagnostic Context)
- Inclusão em headers de resposta
- Rastreamento através de chamadas assíncronas

## Health Checks

- `/actuator/health` - Status geral da aplicação
- Checks customizados:
  - Database connectivity
  - Redis connectivity
  - External API availability
  - Kafka connectivity

## Configuração por Ambiente

- **Dev**: Logs readable, métricas básicas
- **Staging**: Logs JSON, métricas completas
- **Prod**: Logs JSON, métricas + alertas

## ADRs Relacionados

- ADR-013: Observabilidade e monitoramento
- ADR-008: ULID para correlation IDs
