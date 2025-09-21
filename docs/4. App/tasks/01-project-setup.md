# Tarefa 01 - Setup Inicial do Projeto

## Objetivo

Configurar a estrutura base do projeto Spring Boot seguindo a Arquitetura Hexagonal definida no ADR-017.

## Descrição

Criar a estrutura de diretórios, configurar dependências Maven e estabelecer a organização de pacotes conforme o padrão Ports & Adapters.

## Critérios de Aceitação

- [x] Projeto Spring Boot 3.x configurado com Java 21
- [x] Estrutura de pacotes seguindo Arquitetura Hexagonal
- [x] Dependências básicas configuradas (Spring Web, Spring Data JPA, PostgreSQL, Redis)
- [x] Profiles de configuração (dev, staging, prod) definidos
- [x] Arquivos de propriedades por ambiente criados

## Status: ✅ CONCLUÍDA

**Data de Conclusão**: 21/09/2025

### Implementações Realizadas

1. **pom.xml**: Configurado com Spring Boot 3.2.0, Java 21 e todas as dependências necessárias
2. **Estrutura de Pacotes**: Criada seguindo Arquitetura Hexagonal:
   - `br.com.ml.mktplace.orders.domain` (model, service, port)
   - `br.com.ml.mktplace.orders.adapter` (inbound, outbound, config)
3. **OrdersApplication.java**: Classe principal da aplicação Spring Boot criada
4. **Arquivos de Configuração**: Criados para todos os ambientes:
   - `application.properties` (configurações gerais)
   - `application-dev.properties` (desenvolvimento)
   - `application-staging.properties` (staging)  
   - `application-prod.properties` (produção)

## Estrutura de Pacotes Esperada

``` java
br.com.ml.mktplace.orders
├── domain/
│   ├── model/          # Entidades de negócio
│   ├── service/        # Casos de uso
│   └── port/           # Interfaces (portas)
├── adapter/
│   ├── inbound/        # Controllers, handlers
│   ├── outbound/       # Repositórios, clientes
│   └── config/         # Configuração e wiring
└── OrdersApplication.java
```

## Dependências Principais

- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Data Redis
- PostgreSQL Driver
- Spring Boot Starter Test
- Spring Boot Starter Validation

## ADRs Relacionados

- ADR-005: Monolito Modular
- ADR-016: Arquivos de propriedades
- ADR-017: Arquitetura Hexagonal
