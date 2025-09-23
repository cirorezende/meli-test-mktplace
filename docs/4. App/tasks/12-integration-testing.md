# Tarefa 12 - Testes de Integração com Mock da API Externa

## ⏳ Status: PENDENTE (Atualizado: 22/09/2025)

Escopo definido. Implementação aguardando finalização da Tarefa 10 (Observabilidade) para instrumentar métricas e logs que auxiliarão na validação dos testes.

## Objetivo

Implementar testes de integração focados na validação da integração com a API externa de CDs, utilizando mocks conforme mencionado nos requisitos.

## Descrição

Criar testes que validem o comportamento completo do sistema com a API de CDs mockada, garantindo que todos os cenários de integração funcionem corretamente.

## Critérios de Aceitação

- [ ] Mock server para API de CDs configurado
- [ ] Testes de cenários de sucesso da API
- [ ] Testes de cenários de falha (timeout, 500, 404)
- [ ] Validação do comportamento do cache
- [ ] Testes de retry e circuit breaker
- [ ] Validação de eventos publicados
- [ ] Testes end-to-end com mock completo

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

- Profile `integration-test`
- TestContainers para PostgreSQL e Redis
- WireMock para API externa
- Kafka embedded para eventos

## Validações

- Estado final do pedido correto
- CDs selecionados conforme algoritmo
- Eventos publicados com dados corretos
- Logs de auditoria gerados
- Métricas atualizadas

## Ferramentas

- WireMock para mock da API
- TestContainers para infraestrutura
- Spring Boot Test para contexto
- Testcontainers Kafka para eventos

## ADRs Relacionados

- ADR-003: Processamento individual por item
- ADR-010: Cache distribuído
- ADR-011: Tratamento de erros e resiliência
- ADR-014: Estratégia de testes (foco em mocks)
