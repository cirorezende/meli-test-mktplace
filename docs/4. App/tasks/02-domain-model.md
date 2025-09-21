# Tarefa 02 - Modelagem do Domínio

## Objetivo

Implementar as entidades de domínio e value objects que representam os conceitos centrais do sistema de processamento de pedidos.

## Descrição

Criar as classes de domínio seguindo os princípios de Domain-Driven Design, mantendo o core de negócio isolado de detalhes técnicos conforme Arquitetura Hexagonal.

## Critérios de Aceitação

- [x] Entidade Order com identificador ULID
- [x] Entidade OrderItem com referência ao produto
- [x] Value Object Address para endereço de entrega
- [x] Value Object DistributionCenter com coordenadas geográficas
- [x] Enum OrderStatus para controle de estados
- [x] Validações de negócio implementadas nas entidades
- [x] Máximo de 100 itens por pedido validado

## Status: ✅ CONCLUÍDA

**Data de Conclusão**: 21/09/2025

### Implementações Realizadas

1. **OrderStatus (Enum)**: Estados RECEIVED, PROCESSING, PROCESSED, FAILED
2. **Address (Value Object)**: Record com street, city, state, country, zipCode e coordenadas geográficas com validações
3. **DistributionCenter (Value Object)**: Record com code, name e address, incluindo método para obter coordenadas
4. **OrderItem (Entidade)**: Classe com itemId, quantity e assignedDistributionCenter, com validações de negócio
5. **Order (Entidade Principal)**: Classe com ULID, items, deliveryAddress, status e createdAt, incluindo:
   - Geração automática de ULID para identificação
   - Validação de máximo 100 itens por pedido
   - Status inicial sempre RECEIVED
   - Métodos de negócio para verificação de estados
   - Validações completas com Bean Validation

### Dependências Adicionadas

- **ULID Creator**: Biblioteca para geração de ULIDs conforme ADR-008

## Entidades e Value Objects

- **Order**: ID (ULID), items, deliveryAddress, status, createdAt
- **OrderItem**: itemId, quantity, assignedDistributionCenter
- **Address**: street, city, state, country, zipCode, coordinates (lat/lng)
- **DistributionCenter**: code, name, address, coordinates
- **OrderStatus**: RECEIVED, PROCESSING, PROCESSED, FAILED

## Regras de Negócio

- Pedido deve ter pelo menos 1 item
- Máximo 100 itens por pedido (conforme PRD)
- Endereço de entrega obrigatório
- Status inicial sempre RECEIVED

## ADRs Relacionados

- ADR-008: Geração de IDs com ULID
- ADR-017: Arquitetura Hexagonal (Domain no core)
