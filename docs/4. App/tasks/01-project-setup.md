# Tarefa 01 - Setup Inicial do Projeto

## Objetivo

Configurar a estrutura base do projeto Spring Boot seguindo a Arquitetura Hexagonal definida no ADR-017.

## Descrição

Criar a estrutura de diretórios, configurar dependências Maven e estabelecer a organização de pacotes conforme o padrão Ports & Adapters.

## Critérios de Aceitação

- [ ] Projeto Spring Boot 3.x configurado com Java 21
- [ ] Estrutura de pacotes seguindo Arquitetura Hexagonal
- [ ] Dependências básicas configuradas (Spring Web, Spring Data JPA, PostgreSQL, Redis)
- [ ] Profiles de configuração (dev, staging, prod) definidos
- [ ] Arquivos de propriedades por ambiente criados

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
