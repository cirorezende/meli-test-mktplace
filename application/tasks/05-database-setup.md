# Tarefa 05 - Configuração do Banco de Dados

## Objetivo
Configurar PostgreSQL com extensão PostGIS para persistência de dados e cálculos geoespaciais.

## Descrição
Criar as tabelas, índices e configurações necessárias para armazenar pedidos e realizar cálculos de proximidade geográfica.

## Critérios de Aceitação
- [ ] Scripts de migração Flyway configurados
- [ ] Tabela orders com campos principais
- [ ] Tabela order_items com relacionamento
- [ ] Tabela distribution_centers com endereço e coordenadas
- [ ] Extensão PostGIS habilitada
- [ ] Índices geoespaciais criados
- [ ] Dados de exemplo de CDs inseridos

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