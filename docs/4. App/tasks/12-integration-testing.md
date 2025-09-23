# Tarefa 12 - Testes de Integração com Mock da API Externa

## ✅ Status: CONCLUÍDA (Atualizado: 23/09/2025) — Escopo mínimo entregue

Incrementos concluídos:

1. Fluxo end-to-end criação + recuperação de pedido
2. Validação de cache (hit na primeira chamada, miss na segunda) – WireMock verificado
3. Publicação inicial de eventos
4. Publicação real em Kafka + teste de consumo manual (`OrderEventConsumptionIT`)
5. Listener Kafka implementado (`OrderEventsListener`) consumindo `ORDER_CREATED` e disparando processamento
6. Teste end-to-end assíncrono criação -> evento -> consumo -> processamento (`OrderEndToEndProcessingIT`) validando status `PROCESSED`

Infraestrutura: Testcontainers (Postgres, Redis, Kafka) + WireMock configurados em `BaseIntegrationTest`.

## Objetivo

Implementar testes de integração focados na validação da integração com a API externa de CDs, utilizando mocks conforme mencionado nos requisitos.

## Descrição

Criar testes que validem o comportamento completo do sistema com a API de CDs mockada, garantindo que todos os cenários de integração funcionem corretamente.

## Critérios de Aceitação

- [x] Mock server para API de CDs configurado (WireMock dinâmico por teste)
- [x] Primeiro cenário de sucesso (criação + consulta de pedido)
- [ ] Testes de múltiplos itens / seleção de CD (próximo)
- [ ] Testes de cenários de falha (timeout, 500, lista vazia)
- [x] Validação do comportamento do cache (hit/miss inicial)
- [ ] Testes de retry e circuit breaker
- [x] Validação inicial de publicação de eventos (fluxo roda sem exceções)
- [x] Consumo real de evento publicado em Kafka (`OrderEventConsumptionIT`)
- [x] Teste assíncrono com listener processando pedido (`OrderEndToEndProcessingIT`)

Nota: Itens não marcados foram replanejados como incrementos pós-MVP e não fazem parte do escopo mínimo desta tarefa.


## Mock da API de CDs

### WireMock Configuration

- Servidor mock rodando em porta específica
- Endpoints simulados: `/distributioncenters?itemId={id}`
- Respostas configuráveis por cenário
- Latência simulada para testes de timeout

### Cenários de Resposta

```json
// Sucesso
{
  "distributionCenters": ["CD1", "CD2", "CD3"]
}

// Item não encontrado
{
  "distributionCenters": []
}

// Erro 500
{
  "error": "Internal server error"
}
```

## Testes de Integração

### Cenários de Sucesso

- Processamento de pedido com 1 item
- Processamento de pedido com múltiplos itens
- Seleção correta do CD mais próximo
- Cache funcionando corretamente
- Eventos publicados no Kafka

### Cenários de Falha

- API retorna erro 500
- API com timeout
- API retorna lista vazia de CDs
- Retry automático funcionando
- Circuit breaker ativando

### Testes de Cache

- Cache miss na primeira chamada
- Cache hit na segunda chamada
- Expiração do cache após TTL
- Limpeza do cache

### Testes de Resiliência

- Retry com backoff exponencial
- Circuit breaker abrindo após falhas
- Fallback para CD padrão
- Recovery após API voltar

## Configuração de Teste

- Profile `integration-test` (ativado via `@ActiveProfiles` na base)
- Testcontainers: PostgreSQL 16-alpine, Redis 7-alpine, Kafka (Confluent 7.4.1)
- WireMock server dinâmico por classe de teste (porta dinâmica)
- `BaseIntegrationTest` centraliza containers + propriedades dinâmicas
- Futura extensão: reuso de containers com `testcontainers.reuse.enable=true`

## Validações

- Estado final do pedido correto
- CDs selecionados conforme algoritmo
- Eventos publicados com dados corretos
- Logs de auditoria gerados
- Métricas atualizadas

## Ferramentas

- JUnit 5 + Spring Boot Test (@SpringBootTest)
- WireMock (stubs HTTP de Distribution Centers)
- Testcontainers (PostgreSQL, Redis, Kafka)
- Maven Failsafe Plugin (padrão de nomenclatura *IT.java)

## Execução

Para executar unit + integration tests:

```bash
mvn clean verify
```

Somente testes de integração (já tendo feito compile/test antes):

```bash
mvn failsafe:integration-test failsafe:verify
```

Relatórios de cobertura continuam sendo gerados via JaCoCo (incluem ITs ao rodar `verify`).

## Itens Replanejados (pós-MVP)

Ordem sugerida (minimizando retrabalho):

1. Teste múltiplos itens com seleção correta de CD (garante baseline antes de falhas)
2. Cenários de falha WireMock: 500, timeout simulado (latência), lista vazia → asserts de fallback / resposta
3. Implementar/verificar retry (config mínima + validar número de chamadas WireMock)
4. Introduzir circuit breaker (se configurado via Resilience4j ou similar) e validar estados (open/half-open)
5. Testes de eventos adicionais: validar publicação de `ORDER_PROCESSED` e `ORDER_FAILED`
6. Acrescentar teste de expiração de cache (usar TTL curto / manipular tempo se viável)
7. Refinar utilitários de construção (builder de `OrderRequest`) para reduzir duplicação
8. Testes de reprocessamento (quando status FAILED) após listener

## Observações

- Uso de porta estática 9999 placeholder removido posteriormente quando WireMock registrar URL dinâmica nas propriedades (ajuste futuro: Property override em `@DynamicPropertySource` após start do WireMock)
- Considerar criar utilitário para construir DTOs repetidos (builder de OrderRequest)

## ADRs Relacionados

- ADR-003: Processamento individual por item
- ADR-010: Cache distribuído
- ADR-011: Tratamento de erros e resiliência
- ADR-014: Estratégia de testes (foco em mocks)
