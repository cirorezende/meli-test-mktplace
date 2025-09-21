# Tarefa 02 - Modelagem do Domínio

## Objetivo
Implementar as entidades de domínio e value objects que representam os conceitos centrais do sistema de processamento de pedidos.

## Descrição
Criar as classes de domínio seguindo os princípios de Domain-Driven Design, mantendo o core de negócio isolado de detalhes técnicos conforme Arquitetura Hexagonal.

## Critérios de Aceitação
- [ ] Entidade Order com identificador ULID
- [ ] Entidade OrderItem com referência ao produto
- [ ] Value Object Address para endereço de entrega
- [ ] Value Object DistributionCenter com coordenadas geográficas
- [ ] Enum OrderStatus para controle de estados
- [ ] Validações de negócio implementadas nas entidades
- [ ] Máximo de 100 itens por pedido validado

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