# Tarefa 09 - Implementação dos Testes Unitários

## Objetivo
Implementar testes unitários abrangentes para o core de negócio, seguindo a estratégia definida no ADR-014.

## Descrição
Criar testes unitários focados na lógica de negócio, com cobertura mínima de 85% e execução rápida, utilizando mocks para todas as dependências externas.

## Critérios de Aceitação
- [ ] Testes para todas as entidades de domínio
- [ ] Testes para todos os casos de uso
- [ ] Testes para algoritmo de seleção de CD
- [ ] Mocks para todas as portas (interfaces)
- [ ] Cobertura de código >= 85%
- [ ] Execução completa < 30 segundos
- [ ] Testes de cenários de erro e edge cases

## Testes de Domínio

### Entidades (Order, OrderItem, etc.)
- Validações de negócio
- Criação de objetos válidos/inválidos
- Comportamentos específicos
- Edge cases (limites, valores nulos)

### Value Objects (Address, DistributionCenter)
- Imutabilidade
- Validações
- Equality e hashCode

## Testes de Casos de Uso

### CreateOrderUseCase
- Criação de pedido válido
- Criação de pedido com itens inválidos
- Criação de pedido com itens duplicados
- Criação de pedido com itens fora de estoque
- Criação de pedido com itens inválidos (limites, valores nulos)

### ProcessOrderUseCase
- Processamento de pedido válido
- Seleção correta de CDs por proximidade
- Tratamento de falhas da API de CDs
- Uso correto do cache
- Publicação de eventos

### QueryOrderUseCase
- Consulta por ID existente/inexistente
- Listagem de pedidos
- Mapeamento correto de dados

### DistributionCenterSelectionService
- Algoritmo de proximidade geográfica
- Seleção do CD mais próximo
- Tratamento de lista vazia de CDs
- Fallback para CD padrão

## Mocks e Stubs
- **OrderRepository**: MockitoMock com comportamentos simulados
- **DistributionCenterService**: Respostas simuladas da API
- **CacheService**: Simulação de hit/miss
- **EventPublisher**: Verificação de publicação
- **UlidGenerator**: IDs determinísticos para testes

## Cenários de Teste
- **Sucesso**: Fluxos normais de processamento
- **Falhas**: APIs indisponíveis, timeouts, dados inválidos
- **Limites**: Pedidos com 1 e 100 itens
- **Edge Cases**: Coordenadas inválidas, CDs duplicados

## Ferramentas
- JUnit 5 para estrutura de testes
- Mockito para mocks e stubs
- AssertJ para assertions fluentes
- TestContainers apenas se necessário (mínimo)

## ADRs Relacionados
- ADR-014: Testes unitários apenas
- ADR-017: Arquitetura Hexagonal (testabilidade do core)