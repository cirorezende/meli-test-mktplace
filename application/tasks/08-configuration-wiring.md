# Tarefa 08 - Configuração e Wiring dos Componentes

## Objetivo
Configurar a injeção de dependências e wiring entre portas e adaptadores, estabelecendo a configuração completa da aplicação.

## Descrição
Criar as classes de configuração Spring que conectam os adaptadores às portas, configurar beans e estabelecer a estrutura de dependências da Arquitetura Hexagonal.

## Critérios de Aceitação
- [ ] ApplicationConfig com wiring de use cases
- [ ] DatabaseConfig para configuração JPA/PostGIS
- [ ] CacheConfig para configuração Redis
- [ ] HttpClientConfig para cliente da API de CDs
- [ ] KafkaConfig para publicação de eventos
- [ ] Profiles de configuração (dev, staging, prod)
- [ ] Properties externalizadas por ambiente

## Classes de Configuração

### ApplicationConfig
- Beans dos use cases (ProcessOrderUseCase, QueryOrderUseCase)
- Wiring das portas com adaptadores
- Configuração de componentes de domínio

### DatabaseConfig
- DataSource configurado por ambiente
- JPA properties otimizadas
- Configuração PostGIS
- Connection pool settings

### CacheConfig
- RedisTemplate configurado
- Serialização JSON
- TTL padrão de 8 horas
- Connection pool Redis

### HttpClientConfig
- RestTemplate/WebClient configurado
- Timeouts e retry policies
- Circuit breaker configuration
- Headers padrão

### KafkaConfig
- Producer configuration
- Tópicos e partições
- Serialização de eventos
- Error handling

## Arquivos de Properties

### application.properties (base)
```properties
app.name=orders-system
app.max-items-per-order=100
cache.ttl-hours=8
api.cd-service.timeout=5000
api.cd-service.retry-attempts=3
```

### application-dev.properties
- Configurações de desenvolvimento
- Banco de dados PostgreSQL com PostGIS em Docker
- Redis em Docker
- Mock da API de CDs


### application-prod.properties
- Configurações de produção
- PostgreSQL com PostGIS
- Redis cluster
- URLs reais das APIs

## ADRs Relacionados
- ADR-016: Arquivos de propriedades
- ADR-017: Arquitetura Hexagonal (Configuration)