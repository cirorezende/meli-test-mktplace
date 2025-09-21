# Tarefa 03 - Definição das Portas (Interfaces)

## Objetivo

Definir as interfaces (ports) que estabelecem os contratos entre o core de negócio e o mundo externo.

## Descrição

Criar as interfaces que representam as portas de entrada e saída da Arquitetura Hexagonal, definindo os contratos que serão implementados pelos adaptadores.

## Critérios de Aceitação

- [x] Interface OrderRepository para persistência de pedidos
- [x] Interface DistributionCenterService para consulta de CDs por item
- [x] Interface CacheService para operações de cache
- [x] Interface EventPublisher para publicação de eventos
- [x] Interface IDGenerator para geração de identificadores
- [x] Todas as interfaces no pacote domain.port

## Status: ✅ CONCLUÍDA

**Data de Conclusão**: 21/09/2025

### Implementações Realizadas

**Outbound Ports (Portas de Saída)**:
- **OrderRepository**: Interface completa para persistência com métodos save, find, exists
- **DistributionCenterService**: Interface para consulta de CDs via API externa
- **CacheService**: Interface genérica para operações de cache distribuído
- **EventPublisher**: Interface para publicação de eventos de domínio
- **IDGenerator**: Interface para geração de identificadores únicos (ULID)

**Inbound Ports (Portas de Entrada)**:
- **CreateOrderUseCase**: Interface para criação de pedidos com DTOs
- **ProcessOrderUseCase**: Interface para processamento com algoritmo de roteamento
- **QueryOrderUseCase**: Interface completa para consultas e buscas com paginação

**Exceções de Domínio**:
- **OrderNotFoundException**: Para pedidos não encontrados
- **ExternalServiceException**: Para falhas em serviços externos
- **ProcessOrderException**: Para falhas no processamento

**Documentação**:
- README completo no pacote domain.port com todos os contratos
- JavaDoc detalhado em todas as interfaces
- DTOs com validações integradas (records)

## Portas de Saída (Outbound Ports)

- **OrderRepository**: save, findById, findAll, existsById
- **DistributionCenterService**: findDistributionCentersByItem
- **CacheService**: get, put, evict, clear
- **EventPublisher**: publishOrderProcessed, publishOrderFailed
- **IDGenerator**: generate

## Portas de Entrada (Inbound Ports)

- **CreateOrderUseCase**: createOrder
- **ProcessOrderUseCase**: processOrder
- **QueryOrderUseCase**: getOrderById, getOrdersByCustomerId

## Contratos das Interfaces

- Métodos devem usar apenas tipos do domínio
- Não devem ter dependências de frameworks
- Exceções específicas do domínio quando necessário
- Documentação clara dos contratos

## ADRs Relacionados

- ADR-008: ULID para identificadores
- ADR-010: Cache distribuído (Redis)
- ADR-017: Arquitetura Hexagonal (Ports)
