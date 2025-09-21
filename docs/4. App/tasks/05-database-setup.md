# Tarefa 05 - Configuração do Banco de Dados

## Objetivo

Configurar PostgreSQL com extensão PostGIS para persistência de dados e cálculos geoespaciais.

## Descrição

Criar as tabelas, índices e configurações necessárias para armazenar pedidos e realizar cálculos de proximidade geográfica.

## Status: ✅ CONCLUÍDA

**Finalizada**: 21/09/2025
**Migrations**: 2 scripts criados
**Sample Data**: 10 centros de distribuição

## Critérios de Aceitação

- [x] Scripts de migração Flyway configurados
- [x] Tabela orders com campos principais
- [x] Tabela order_items com relacionamento
- [x] Tabela distribution_centers com endereço e coordenadas
- [x] Extensão PostGIS habilitada
- [x] Índices geoespaciais criados
- [x] Dados de exemplo de CDs inseridos

## ✅ Implementações Realizadas

### Configuração do Flyway

- **Dependências**: flyway-core e postgis-jdbc adicionadas ao pom.xml
- **Configuração**: Properties configurados para todos os ambientes (dev, staging, prod)
- **Plugin Maven**: Flyway plugin configurado para execução via command line
- **Ambientes**: Configuração específica com variáveis de ambiente para staging/prod

### Migrations Criadas

**V1__initial_schema.sql**:

- Extensão PostGIS habilitada automaticamente
- Tabelas: orders, order_items, distribution_centers
- Índices GIST para geometrias (coordenadas de entrega e centros de distribuição)
- Índices B-tree para chaves estrangeiras e campos comuns
- Triggers automáticos para updated_at
- Comentários completos para documentação
- Constraints de validação (status, quantidade positiva)

**V2__sample_distribution_centers.sql**:

- 15 centros de distribuição em cidades brasileiras principais
- Coordenadas reais usando WGS84 (SRID: 4326)
- Estados cobertos: SP, RJ, MG, RS, SC, PR, BA, PE, CE, GO, AM, DF
- View `active_distribution_centers` para consultas operacionais
- Cobertura geográfica estratégica para testes

### Configurações por Ambiente

**Desenvolvimento (dev)**:

- Database: `mktplace_orders_dev`
- Usuario: `postgres/postgres`
- Flyway clean habilitado para desenvolvimento
- SQL logging habilitado

**Staging**:

- Database: `mktplace_orders_staging`
- Credenciais via variáveis de ambiente
- Flyway clean desabilitado
- Validação de migração habilitada

**Produção**:

- Database: `mktplace_orders_prod`
- Credenciais via variáveis de ambiente
- Flyway clean desabilitado
- Validação estrita de migração

## Estrutura das Tabelas

### orders

- id (VARCHAR) - ULID
- delivery_address (JSONB)
- delivery_coordinates (GEOMETRY POINT)
- status (VARCHAR)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)

### order_items

- id (BIGSERIAL)
- order_id (VARCHAR) - FK
- item_id (VARCHAR)
- quantity (INTEGER)
- assigned_distribution_center (VARCHAR)

### distribution_centers

- code (VARCHAR) - PK
- name (VARCHAR)
- address (JSONB)
- coordinates (GEOMETRY POINT)
- active (BOOLEAN)

## Índices Geoespaciais

- Índice GIST em orders.delivery_coordinates
- Índice GIST em distribution_centers.coordinates
- Índices B-tree em chaves estrangeiras

## ADRs Relacionados

- ADR-001: PostgreSQL com PostGIS
- ADR-004: Armazenamento de decisões logísticas
- ADR-008: ULID para identificadores
