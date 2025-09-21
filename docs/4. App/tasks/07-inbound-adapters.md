# Tarefa 07 - Implementação dos Adaptadores de Entrada

## Objetivo

Implementar os controllers REST que expõem as APIs do sistema para processamento e consulta de pedidos.

## Descrição

Criar os controllers REST seguindo as melhores práticas de API design, com validação de entrada, tratamento de erros e documentação adequada.

## Status: ✅ CONCLUÍDA (21/09/2025)

## Critérios de Aceitação

- [x] OrderController com endpoints de processamento e consulta
- [x] DTOs para request/response
- [x] Validação de entrada com Bean Validation
- [x] Tratamento global de exceções
- [x] Documentação OpenAPI/Swagger
- [x] Versionamento de API (v1)
- [x] Headers de resposta apropriados

## Endpoints da API

### POST /api/v1/orders

- Processar novo pedido
- Request: OrderRequest (items, deliveryAddress, customerId)
- Response: OrderResponse (id, status, items com CDs)
- Status: 201 Created (processamento assíncrono)

### GET /api/v1/orders/{id}

- Consultar pedido por ID
- Response: OrderResponse completo
- Status: 200 OK ou 404 Not Found

### GET /api/v1/orders

- Listar todos os pedidos
- Paginação opcional
- Response: Lista de OrderResponse
- Status: 200 OK

## DTOs e Validação

- **OrderRequest**: validação de itens (1-100), endereço obrigatório
- **OrderResponse**: dados completos do pedido processado
- **AddressDto**: validação de campos obrigatórios
- **OrderItemDto**: validação de quantidade positiva

## Tratamento de Erros

- GlobalExceptionHandler para exceções do domínio
- Respostas padronizadas com ErrorResponse
- Logs estruturados para troubleshooting

## Headers de Resposta

- API-Version: versão da API
- Correlation-ID: para rastreamento
- Content-Type: application/json

## ADRs Relacionados

- ADR-002: RESTful API
- ADR-015: Versionamento de APIs
- ADR-017: Arquitetura Hexagonal (Inbound Adapters)
