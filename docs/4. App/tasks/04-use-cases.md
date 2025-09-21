# Tarefa 04 - Implementação dos Casos de Uso

## Objetivo

Implementar a lógica de negócio através dos casos de uso, incluindo o algoritmo de seleção de centro de distribuição.

## Descrição

Criar os serviços de domínio que orquestram as regras de negócio, implementando o algoritmo de proximidade geográfica e o processamento de pedidos.

## Critérios de Aceitação

- [ ] ProcessOrderUseCase implementado com algoritmo de seleção de CD
- [ ] QueryOrderUseCase para consulta de pedidos
- [ ] DistributionCenterSelectionService com lógica de proximidade
- [ ] Tratamento de erros e cenários de falha
- [ ] Publicação de eventos de sucesso e falha
- [ ] Uso de cache para otimizar consultas de CD

## Casos de Uso Principais

- **CreateOrderUseCase**: Recebe pedido, persiste resultado, publica evento de novo pedido criado.
- **ProcessOrderUseCase**: Recebe evento de novo pedido, seleciona CDs, persiste resultado
- **QueryOrderUseCase**: Consulta pedidos por ID ou lista todos do cliente
- **DistributionCenterSelectionService**: Algoritmo de proximidade geográfica

## Algoritmo de Seleção de CD (ADR-009)

1. Para cada item do pedido:
   - Consultar CDs disponíveis via API externa (com cache)
   - Calcular distância geográfica de cada CD para endereço de entrega (usando PostGIS)
   - Selecionar CD mais próximo (usando PostGIS)
   - Atribuir CD ao item

## Tratamento de Erros

- API de CDs indisponível: usar CD padrão da região
- Timeout: retry com backoff exponencial
- Falha total: marcar pedido como FAILED

## ADRs Relacionados

- ADR-009: Proximidade geográfica simples
- ADR-010: Cache distribuído (Redis)
- ADR-011: Estratégia de tratamento de erros
- ADR-017: Arquitetura Hexagonal (Use Cases no core)
