# Tarefa 03 - Definição das Portas (Interfaces)

## Objetivo
Definir as interfaces (ports) que estabelecem os contratos entre o core de negócio e o mundo externo.

## Descrição
Criar as interfaces que representam as portas de entrada e saída da Arquitetura Hexagonal, definindo os contratos que serão implementados pelos adaptadores.

## Critérios de Aceitação
- [ ] Interface OrderRepository para persistência de pedidos
- [ ] Interface DistributionCenterService para consulta de CDs por item
- [ ] Interface CacheService para operações de cache
- [ ] Interface EventPublisher para publicação de eventos
- [ ] Interface IDGenerator para geração de identificadores
- [ ] Todas as interfaces no pacote domain.port

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