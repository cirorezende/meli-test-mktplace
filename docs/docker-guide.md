# Docker Containerization Guide

This document provides comprehensive instructions for running the Orders Processing System using Docker containers.

## 🐳 Quick Start

### Prerequisites

- Docker Desktop (Windows/Mac) or Docker Engine (Linux)
- Docker Compose v2.0+
- At least 4GB of available RAM
- Ports 5050, 5432, 5540, 6379, 8080, 8081, 9092, 2181 available (3000 removed – no external DC mock)

### Running the Application

#### Distribution Centers Mock

O mock de Centros de Distribuição agora é interno (bean Spring). A cada requisição a lista é gerada embaralhando a base `[SP-001,RJ-001,MG-001,RS-001,PR-001]` e retornando um subconjunto aleatório (1..5 elementos) ordenado alfabeticamente. Nenhum container ou passo extra é necessário.

#### Windows (PowerShell)

```powershell
# Build and start all services
.\scripts\build.ps1
.\scripts\run.ps1 start

# Check status
.\scripts\run.ps1 status

# View application logs
.\scripts\run.ps1 logs orders-app
```

#### Linux/Mac (Bash)

```bash
# Build and start all services
./scripts/build.sh
./scripts/run.sh start

# Check status
./scripts/run.sh status

# View application logs
./scripts/run.sh logs orders-app
```

## 🏗️ Architecture

The containerized environment includes:

### Core Application

- **orders-app**: Main Spring Boot application
  - Port: 8080
  - Health check: `/api/actuator/health`
  - API docs: `/api/swagger-ui.html`

### Infrastructure Services

- **PostgreSQL 15 + PostGIS**: Primary database
  - Port: 5432
  - Database: `orders_db`
  - User: `orders_user`
  - Password: `orders_pass`

- **Redis 7**: Caching and session store
  - Port: 6379
  - Password: `redis_pass`

- **Apache Kafka**: Message broker
  - Port: 9092
  - Zookeeper: 2181

### Management Interfaces

- **pgAdmin**: PostgreSQL management
  - URL: <http://localhost:5050>
  - Email: <admin@orders.com>
  - Password: admin123

- **Redis Insight**: Redis management
  - URL: <http://localhost:5540>

- **Kafka UI**: Kafka monitoring
  - URL: <http://localhost:8081>

### External Service Mocks

Nenhum serviço externo para Centros de Distribuição (in-process mock).

## 📁 Container Structure

``` java
├── Dockerfile                    # Multi-stage application build
├── docker-compose.yml            # Service orchestration
├── .dockerignore                 # Build context optimization
├── config/
│   ├── postgres/
│   │   └── 01-init-database.sql   # Database initialization
│   ├── redis/
│   │   └── redis.conf             # Redis configuration
│   └── (wiremock/)                # Removido – mock interno
├── scripts/
│   ├── build.sh / build.ps1       # Build scripts
│   └── run.sh / run.ps1           # Runtime management
└── src/main/resources/ (arquivo histórico `application-docker.properties` foi removido em 2025)
```

## 🔧 Configuration

### Environment Variables

The application supports the following environment variables for configuration:
 
## Deprecated: Full Application Containerization Guide

This document used to describe how to run the application itself inside Docker (service `orders-app` + supporting infra). The project has since moved to a simpler developer workflow:

1. Start ONLY infrastructure services with `docker compose` (PostgreSQL, Redis, Kafka, pgAdmin, Redis Insight, Kafka UI).
2. Run the Spring Boot application locally via: `mvn spring-boot:run` (or your IDE).

The application image / Dockerfile and helper scripts were removed. This file is kept for historical reference and has been MINIMIZED to only list currently relevant container services. Obsolete sections were removed.

For up‑to‑date instructions see the root `README.md` (section "Como Executar").

---

## Infraestrutura Ativa (Compose)

| Serviço       | Porta | Uso Principal | UI / Observabilidade |
|---------------|-------|---------------|----------------------|
| PostgreSQL    | 5432  | Banco + PostGIS | pgAdmin (5050) |
| Redis         | 6379  | Cache          | Redis Insight (5540) |
| Kafka         | 9092  | Mensageria     | Kafka UI (8081) |
| Zookeeper     | 2181  | Coordenação Kafka | - |
| pgAdmin       | 5050  | Admin Postgres | [http://localhost:5050](http://localhost:5050) |
| Redis Insight | 5540  | Admin Redis    | [http://localhost:5540](http://localhost:5540) |
| Kafka UI      | 8081  | Monitor Kafka  | [http://localhost:8081](http://localhost:8081) |

Credenciais padrão (dev):

Postgres: user `orders_user` / pass `orders_pass` / db `orders_db`  
Redis: password `redis_pass`

Nota: O antigo arquivo `application-docker.properties` foi removido (não há mais perfil `docker` ativo).

---

## Como Subir Infraestrutura

```bash
docker compose up -d            # sobe todos os serviços de infraestrutura
docker compose ps               # lista status
docker compose logs -f postgres # exemplo de logs
```

Parar e (opcional) remover volumes:

```bash
docker compose down             # para serviços preservando dados
docker compose down --volumes   # ATENÇÃO: apaga dados (Postgres/Redis)
```

---

## Executando a Aplicação (Local JVM)

Em outro terminal:

```bash
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
```

Ou rode a classe principal via IDE. A aplicação expõe:

- API REST: [http://localhost:8080/api/orders](http://localhost:8080/api/orders) (exemplo)
- Actuator: [http://localhost:8080/api/actuator/health](http://localhost:8080/api/actuator/health)
- (Se habilitado) Swagger UI: /api/swagger-ui.html

---

## Verificações Rápidas

```bash
curl http://localhost:8080/api/actuator/health
curl http://localhost:8080/api/actuator/info
```

Ver banco:

```bash
docker compose exec postgres psql -U orders_user -d orders_db -c "SELECT NOW();"
```

Ver Redis:

```bash
docker compose exec redis redis-cli -a redis_pass INFO memory | head -n 15
```

Tópicos Kafka (após subir aplicação que cria/usa tópicos):

```bash
docker compose exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```

---

## Variáveis de Ambiente Relevantes (Perfil dev)

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=orders_db
DB_USER=orders_user
DB_PASSWORD=orders_pass
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis_pass
KAFKA_BROKERS=localhost:9092
SPRING_PROFILES_ACTIVE=dev
```

Quando executado com Testcontainers (testes de integração) essas variáveis não são necessárias: os containers efêmeros são gerenciados automaticamente pelos testes.

---

## Histórico (Resumo)

Versões anteriores deste documento descreviam:  
- Dockerfile multi-stage para a aplicação  
- Scripts `build.sh` / `run.sh`  
- Serviço `orders-app` no compose  
Todos removidos para simplificar o fluxo local (infra compartilhada + execução direta na JVM).

---

Se encontrar referência antiga a `orders-app` ou `application-docker.properties`, considerar remover ou atualizar. Abra um PR com a limpeza adicional.

