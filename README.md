# Orders Service (meli-test-mktplace)

Serviço de processamento de pedidos com pipeline assíncrono (event-driven) utilizando Postgres, Redis e Kafka. A infraestrutura sobe via Docker Compose e a aplicação roda diretamente via IDE ou Maven.

## Modelo Assíncrono Resumido

1. `POST /api/v1/orders` cria um pedido, publica evento `ORDER_CREATED` e retorna `202 Accepted` (status inicial `RECEIVED`).
2. Processamento ocorre assíncronamente, atualizando estados: `RECEIVED -> PROCESSING -> PROCESSED` (ou `FAILED`).
3. Client da API acompanha via `GET /api/v1/orders/{id}` ou observando eventos Kafka (`order.created` / `order.processed`).

### Idempotência

Processamento só executa lógica pesada quando o pedido está em `RECEIVED`. Novas chamadas ou reentregas de eventos em estados intermediários não introduzem efeitos colaterais extra.

### Cache

Redis armazena dados (ex.: disponibilidade de itens por centro de distribuição) usando chaves versionadas como `item-dc-availability:v2:{ITEM_ID}`. Falhas de cache não quebram o fluxo.

## Como Executar Localmente

Pre-requisitos:

- Docker + Docker Compose
- JDK 21
- Maven 3.9+

### 1. Subir Infraestrutura

No diretório raiz do projeto:

```bash
docker compose up -d
```

Isso disponibiliza:

- Postgres (localhost:5432)
- Redis (localhost:6379)
- Kafka (localhost:9092)
- Kafka UI ([http://localhost:8081](http://localhost:8081))
- Redis Insight ([http://localhost:5540](http://localhost:5540))
- pgAdmin ([http://localhost:5050](http://localhost:5050))

### 2. Rodar a Aplicação

Via Maven (hot reload simples para dev):

```bash
mvn spring-boot:run
```

Ou execute a classe `br.com.ml.mktplace.orders.OrdersApplication` pela IDE.

Também é possível executar o JAR gerado pelo build (disponibilizado na pasta 'dist'):

```bash
java -jar orders-0.0.1-SNAPSHOT.jar
```

### 3. Testar Fluxo Básico

Criar pedido:

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H 'Content-Type: application/json' \
  -d '{
        "customerId": "customer-1",
        "items": [{"itemId": "SKU123", "quantity": 1}],
        "deliveryAddress": {
          "street": "Rua A",
          "number": "100",
          "city": "São Paulo",
          "state": "SP",
          "country": "BR",
          "zipCode": "01000-000"
        }
      }'
```

Consultar status:

```bash
curl http://localhost:8080/api/v1/orders/{ORDER_ID}
```

### Parar Infraestrutura

```bash
docker compose down
```

## Estrutura do Projeto

Projeto single-module (`orders`). Código fonte: `src/main/java`, testes: `src/test/java`.

## Testes

```bash
mvn clean verify
```

Utiliza Testcontainers para Kafka/Postgres/Redis – não requer docker compose rodando para testes de integração (os containers são gerenciados pelos testes). Para rodar a aplicação manual + compose simultaneamente, não há conflito de portas com os containers de teste (eles sobem em portas dinâmicas).

## Observabilidade

- Actuator: `http://localhost:8080/actuator/health`
- Métricas (Prometheus): `http://localhost:8080/actuator/prometheus`
- Logs estruturados (Logstash encoder)
