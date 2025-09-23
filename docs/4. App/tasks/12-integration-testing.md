# Tarefa 12 - Testes de Integração com Mock da API Externa

## 🚧 Status: EM PROGRESSO (Atualizado: 23/09/2025)

Implementação iniciada. Primeiro fluxo end-to-end (criação + recuperação de pedido) validado com infraestrutura dinâmica via Testcontainers e WireMock básico.

## Objetivo

Implementar testes de integração focados na validação da integração com a API externa de CDs, utilizando mocks conforme mencionado nos requisitos.

## Descrição

Criar testes que validem o comportamento completo do sistema com a API de CDs mockada, garantindo que todos os cenários de integração funcionem corretamente.

## Critérios de Aceitação

- [x] Mock server para API de CDs configurado (WireMock dinâmico por teste)
- [x] Primeiro cenário de sucesso (criação + consulta de pedido)
- [ ] Testes de múltiplos itens / seleção de CD
- [ ] Testes de cenários de falha (timeout, 500, 404)
- [ ] Validação do comportamento do cache
- [ ] Testes de retry e circuit breaker
- [ ] Validação de eventos publicados (Kafka)
- [ ] Testes end-to-end completos (fluxos de erro e recuperação)

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

## Próximos Passos Imediatos

1. Adicionar stubs de falha (500, timeout, lista vazia) e asserts de fallback/resiliência
2. Validar publicação de eventos Kafka (inspecionar tópicos ou usar consumer test harness)
3. Adicionar cenários de cache hit/miss (duas chamadas consecutivas para mesmo item)
4. Introduzir testes de retry (simulando falha temporária seguida de sucesso)
5. Adicionar simulação de circuit breaker (se configuração já exposta; caso contrário planejar impl)

## Observações

- Uso de porta estática 9999 placeholder removido posteriormente quando WireMock registrar URL dinâmica nas propriedades (ajuste futuro: Property override em `@DynamicPropertySource` após start do WireMock)
- Considerar criar utilitário para construir DTOs repetidos (builder de OrderRequest)

## ADRs Relacionados

- ADR-003: Processamento individual por item
- ADR-010: Cache distribuído
- ADR-011: Tratamento de erros e resiliência
- ADR-014: Estratégia de testes (foco em mocks)
