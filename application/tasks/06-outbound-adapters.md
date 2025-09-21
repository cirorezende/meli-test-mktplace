# Tarefa 06 - Implementação dos Adaptadores de Saída

## Objetivo
Implementar os adaptadores que conectam o core de negócio com sistemas externos (banco, cache, APIs).

## Descrição
Criar as implementações concretas das portas de saída, incluindo repositórios JPA, cliente HTTP para API de CDs, cache Redis e gerador ULID.

## Critérios de Aceitação
- [ ] JpaOrderRepository implementando OrderRepository
- [ ] HttpDistributionCenterService para API externa
- [ ] RedisCache implementando CacheService
- [ ] UlidGeneratorImpl para geração de IDs
- [ ] KafkaEventPublisher para publicação de eventos
- [ ] Configuração de retry e circuit breaker
- [ ] Mapeamento entre entidades JPA e domínio

## Adaptadores a Implementar

### JpaOrderRepository
- Entidades JPA (OrderEntity, OrderItemEntity)
- Mapeamento para/de objetos de domínio
- Consultas com PostGIS para cálculos geográficos

### HttpDistributionCenterService
- Cliente HTTP com RestTemplate/WebClient
- URL configurável via properties
- Tratamento de timeouts e erros
- Integração com cache

### RedisCache
- Configuração Redis com Spring Data Redis
- TTL de 8 horas conforme ADR-010
- Serialização JSON dos dados
- Limpeza automática diária

### UlidGeneratorImpl
- Biblioteca ULID Java
- Thread-safe para uso concorrente

### KafkaEventPublisher
- Configuração Kafka/MSK
- Tópicos para eventos de pedidos
- Serialização de eventos

## Configurações
- Connection pools otimizados
- Timeouts apropriados
- Retry policies com backoff exponencial

## ADRs Relacionados
- ADR-001: PostgreSQL com PostGIS
- ADR-007: Kafka (AWS MSK)
- ADR-008: ULID
- ADR-010: Cache Redis
- ADR-011: Tratamento de erros
- ADR-017: Arquitetura Hexagonal (Outbound Adapters)