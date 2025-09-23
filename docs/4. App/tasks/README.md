# Plano de Implementação - Sistema de Processamento de Pedidos

## Visão Geral

Este plano de implementação detalha as tarefas necessárias para desenvolver o sistema de processamento de pedidos com roteamento inteligente para centros de distribuição, seguindo todas as decisões arquiteturais documentadas nos ADRs.

## Arquitetura Base

- **Estilo Arquitetural**: Arquitetura Hexagonal (Ports & Adapters) - ADR-017
- **Estrutura**: Monolito Modular - ADR-005
- **Tecnologia**: Java 21 + Spring Boot 3.x
- **Banco de Dados**: PostgreSQL com PostGIS - ADR-001
- **Cache**: Redis distribuído - ADR-010
- **Deploy**: AWS Fargate - ADR-006

## Sequência de Execução

### Fase 1: Fundação (Tarefas 1-3)

1. **Setup Inicial do Projeto** - Estrutura base e dependências
2. **Modelagem do Domínio** - Entidades e value objects
3. **Definição das Portas** - Interfaces e contratos

### Fase 2: Core de Negócio (Tarefas 4-5)

1. **Implementação dos Casos de Uso** - Lógica de negócio e algoritmos
2. **Configuração do Banco de Dados** - PostgreSQL com PostGIS

### Fase 3: Adaptadores (Tarefas 6-8)

1. **Adaptadores de Saída** - Repositórios, clientes HTTP, cache
2. **Adaptadores de Entrada** - Controllers REST e APIs
3. **Configuração e Wiring** - Injeção de dependências

### Fase 4: Qualidade e Observabilidade (Tarefas 9-10)

1. **Testes Unitários** - Cobertura do core de negócio
2. **Observabilidade** - Logging, métricas e monitoramento

### Fase 5: Deploy e Integração (Tarefas 11-12)

1. **Containerização** - Docker e AWS Fargate
2. **Testes de Integração** - Validação com mocks

## Principais Decisões Arquiteturais Aplicadas

### Simplicidade para Primeira Versão

- **Algoritmo de CD**: Proximidade geográfica simples (ADR-009)
- **Cache**: Redis distribuído apenas (ADR-010)
- **Testes**: Unitários apenas (ADR-014)
- **Configuração**: Arquivos de propriedades (ADR-016)

### Qualidade e Evolução

- **Arquitetura Hexagonal**: Core isolado e testável
- **Observabilidade**: Logs estruturados e métricas
- **Resiliência**: Retry, circuit breaker e fallbacks (planejado)
- **Versionamento**: APIs versionadas para evolução

## Critérios de Sucesso

- [x] Sistema processa pedidos com até 100 itens
- [x] Algoritmo seleciona CD mais próximo geograficamente  
- [x] **Core de negócio implementado** *(implementações cases de uso prontas)*
- [x] **Database configurado** *(PostgreSQL + PostGIS + Flyway)*
- [x] Cache otimiza chamadas à API externa *(interfaces definidas)*
- [x] **Testes unitários com 100% passando (251 testes)**
- [x] **APIs REST funcionais para processamento e consulta** *(Controllers, DTOs, validação, error handling)*
- [x] **Configuração completa por ambiente** *(ApplicationConfig, DatabaseConfig, CacheConfig, HttpClientConfig, KafkaConfig)*
- [x] **Sistema conteinerizado e funcional** *(Docker + docker-compose com 8 serviços)*
- 🚧 Observabilidade completa implementada *(dashboards + métricas externas pendentes)*
- ⏳ Testes de integração com mocks *(pendente)*

## Status das Tarefas

### Fase 1: Fundação

- ✅ **Tarefa 01** - Setup Inicial do Projeto (Concluída - 21/09/2025)
- ✅ **Tarefa 02** - Modelagem do Domínio (Concluída - 21/09/2025)
- ✅ **Tarefa 03** - Definição das Portas (Concluída - 21/09/2025)

### Fase 2: Core de Negócio

- ✅ **Tarefa 04** - Implementação dos Casos de Uso (Concluída - 21/09/2025)
- ✅ **Tarefa 05** - Configuração do Banco de Dados (Concluída - 21/09/2025)

### Fase 3: Adaptadores

- ✅ **Tarefa 06** - Adaptadores de Saída (Concluída - 21/09/2025)
- ✅ **Tarefa 07** - Adaptadores de Entrada (Concluída - 21/09/2025)
- ✅ **Tarefa 08** - Configuração e Wiring (Concluída - 21/09/2025)

### Fase 4: Qualidade e Observabilidade

- ✅ **Tarefa 09** - Testes Unitários (Concluída - 21/09/2025 / Atualizada 22/09 com Testcontainers)
- 🚧 **Tarefa 10** - Observabilidade (Em Progresso - 22/09/2025)

### Fase 5: Deploy e Integração

- ✅ **Tarefa 11** - Containerização (Concluída - 21/09/2025)
- ⏳ **Tarefa 12** - Testes de Integração (Pendente)

## Tabela Consolidada de Status (Atualizado: 22/09/2025)

| Tarefa | Título | Status | Última Atualização |
|--------|--------|--------|--------------------|
| 01 | Setup Inicial do Projeto | ✅ Concluída | 21/09/2025 |
| 02 | Modelagem do Domínio | ✅ Concluída | 21/09/2025 |
| 03 | Definição das Portas | ✅ Concluída | 21/09/2025 |
| 04 | Implementação dos Casos de Uso | ✅ Concluída | 21/09/2025 |
| 05 | Configuração do Banco de Dados | ✅ Concluída | 21/09/2025 |
| 06 | Adaptadores de Saída | ✅ Concluída | 21/09/2025 |
| 07 | Adaptadores de Entrada | ✅ Concluída | 21/09/2025 |
| 08 | Configuração e Wiring | ✅ Concluída | 21/09/2025 |
| 09 | Testes Unitários | ✅ Concluída | 22/09/2025 |
| 10 | Observabilidade | 🚧 Em Progresso | 22/09/2025 |
| 11 | Containerização | ✅ Concluída | 21/09/2025 |
| 12 | Testes de Integração | ⏳ Pendente | — |

### Próximas Entregas Prioritárias

1. Tarefa 10 - Finalizar dashboards e métricas externas (API CDs / DB).  
2. Tarefa 12 - Testes de Integração: WireMock + Testcontainers (PostgreSQL, Redis, Kafka).

### Notas de Progresso (22/09/2025)

- Testes 100% passando com adoção de Testcontainers para configuração de banco.  
- Observabilidade avançada: métricas de pedidos, cache e seleção de CD implementadas.  
- Falta instrumentar cliente HTTP externo para métricas de latência e status.  
- Preparação para iniciar testes de integração após fechamento da parte visual de observabilidade.

## Próximos Passos

Após completar todas as tarefas, o sistema estará pronto para:

- Deploy em ambiente de produção
- Evolução incremental dos algoritmos
- Migração para configurações mais sofisticadas
- Expansão para microserviços se necessário

## 📊 Resumo do Progresso

### Tarefas Concluídas: 10/12 (83%)

✅ **Fase 1 - Fundação**: 100% concluída (3/3 tarefas)  
✅ **Fase 2 - Core de Negócio**: 100% concluída (2/2 tarefas)  
✅ **Fase 3 - Adaptadores**: 100% concluída (3/3 tarefas)  
✅ **Fase 4 - Qualidade**: 50% concluída (1/2 tarefas)  
✅ **Fase 5 - Deploy**: 50% concluída (1/2 tarefas)

### Estado Atual

- **Core de Domínio**: ✅ Completo
- **Database**: ✅ Configurado (PostgreSQL + PostGIS + migrations)
- **Testes**: ✅ 251 testes (100% passando) – incluindo config com Testcontainers
- **Adaptadores**: ✅ Completo
- **Configurações**: ✅ Completo
- **Containerização**: ✅ Completo
- **Observabilidade**: 🚧 Parcial (dashboards e métricas externas faltantes)

## ADRs de Referência

Consulte os ADRs 001-017 para detalhes completos das decisões arquiteturais que fundamentam este plano de implementação.
