# Docker Containerization Guide

This document provides comprehensive instructions for running the Orders Processing System using Docker containers.

## 🐳 Quick Start

### Prerequisites

- Docker Desktop (Windows/Mac) or Docker Engine (Linux)
- Docker Compose v2.0+
- At least 4GB of available RAM
- Ports 3000, 5050, 5432, 5540, 6379, 8080, 8081, 9092, 2181 available

### Running the Application

#### IMPORTANT: Build WireMock Extension (Random Distribution Centers)

O mock de Centros de Distribuição usa uma extensão WireMock customizada (`RandomDistributionCentersTransformer`) para retornar entre 1 e 5 CDs aleatórios a cada requisição.

Antes de subir os containers, gere o jar da extensão para que o container `distribution-centers-api` consiga carregá-la.

Passos rápidos:

```bash
# 1. Gerar somente a extensão (mais rápido)
mvn -pl wiremock-extensions -am package

# (Opcional) build completo
mvn clean package

# 2. Verificar se o jar existe
ls wiremock-extensions/target/wiremock-extensions-0.0.1-SNAPSHOT.jar

# 3. Subir apenas o mock (ou tudo)
docker compose up -d distribution-centers-api

# 4. Testar (executar várias vezes e observar tamanhos diferentes 1..5)
curl "http://localhost:3000/distribuitioncenters?itemId=TEST"
```

Se esquecer de gerar o jar:

- WireMock pode subir sem o transformer (lista fixa ou erro de extensão)
- Solução: gerar jar e reiniciar: `docker compose restart distribution-centers-api`

Debug rápido:

```bash
docker logs -f distribution-centers-api | grep random-distribution-centers || true
```

Critérios de sucesso:

- Arrays variam entre 1 e 5 elementos
- Nunca vazio
- Sem duplicados na mesma resposta

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

- **WireMock**: Distribution Centers API mock
  - URL: <http://localhost:3000>
  - Mock data includes distribution centers for São Paulo, Rio de Janeiro, and Belo Horizonte

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
│   └── wiremock/
│       └── mappings/              # API mock definitions
├── scripts/
│   ├── build.sh / build.ps1       # Build scripts
│   └── run.sh / run.ps1           # Runtime management
└── src/main/resources/
    └── application-docker.properties  # Container-specific config
```

## 🔧 Configuration

### Environment Variables

The application supports the following environment variables for configuration:

```bash
# Database
DB_HOST=postgres
DB_PORT=5432
DB_NAME=orders_db
DB_USER=orders_user
DB_PASSWORD=orders_pass

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=redis_pass

# Kafka
KAFKA_BROKERS=kafka:9092

# External APIs
DISTRIBUTION_CENTERS_API_URL=http://distribution-centers-api:8080

# Application
SPRING_PROFILES_ACTIVE=docker
SERVER_PORT=8080
MANAGEMENT_SERVER_PORT=8080
```

### Application Profiles

- **docker**: Optimized for container deployment
- **dev**: Development with external database
- **prod**: Production configuration
- **staging**: Staging environment

## 🚀 Deployment Commands

### Build Commands

```bash
# Build application image
docker-compose build orders-app

# Build with no cache
docker-compose build --no-cache orders-app

# Pull latest base images
docker-compose pull
```

### Runtime Commands

```bash
# Start all services
docker-compose up -d

# Start specific service
docker-compose up -d orders-app

# Scale application instances
docker-compose up -d --scale orders-app=3

# View logs
docker-compose logs -f orders-app
docker-compose logs --tail=100 postgres

# Execute commands in containers
docker-compose exec orders-app bash
docker-compose exec postgres psql -U orders_user -d orders_db
docker-compose exec redis redis-cli -a redis_pass
```

### Maintenance Commands

```bash
# Restart services
docker-compose restart orders-app

# Stop all services
docker-compose down

# Remove volumes (data loss!)
docker-compose down --volumes

# Clean up system
docker system prune -f
docker volume prune -f
```

## 📊 Monitoring & Health Checks

### Application Health

```bash
# Health check endpoint
curl http://localhost:8080/api/actuator/health

# Application info
curl http://localhost:8080/api/actuator/info

# Metrics
curl http://localhost:8080/api/actuator/metrics
```

### Database Health

```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U orders_user -d orders_db

# Check PostGIS extension
SELECT PostGIS_Version();
```

### Cache Health

```bash
# Connect to Redis
docker-compose exec redis redis-cli -a redis_pass

# Check cache status
INFO memory
```

## 🔍 Troubleshooting

### Common Issues

#### Port Conflicts

```bash
# Check port usage
netstat -tulpn | grep :8080

# Use different ports in docker-compose.yml
services:
  orders-app:
    ports:
      - "8081:8080"  # Map to different host port
```

#### Memory Issues

```bash
# Increase Docker memory limit (4GB+)
# Add to docker-compose.yml
services:
  orders-app:
    deploy:
      resources:
        limits:
          memory: 2G
```

#### Database Connection Issues

```bash
# Check database logs
docker-compose logs postgres

# Verify database initialization
docker-compose exec postgres psql -U orders_user -d orders_db -c "\dt"
```

#### Application Startup Issues

```bash
# Check application logs
docker-compose logs -f orders-app

# Debug with interactive session
docker-compose run --rm orders-app bash
```

### Log Analysis

```bash
# Application logs with timestamps
docker-compose logs -f -t orders-app

# Search for errors
docker-compose logs orders-app 2>&1 | grep ERROR

# Export logs to file
docker-compose logs orders-app > app.log 2>&1
```

## 🧪 Testing

### Integration Tests

```bash
# Run tests against containers
./scripts/run.sh start
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"items":[{"productId":1,"quantity":2}]}'
```

### Performance Testing

```bash
# Load test with curl
for i in {1..100}; do
  curl -X GET http://localhost:8080/api/orders &
done
wait
```

## 📈 Performance Optimization

### JVM Tuning

The Dockerfile includes optimized JVM settings:

```dockerfile
ENV JAVA_OPTS="-Xmx1536m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"
```

### Database Optimization

- Connection pooling (HikariCP)
- Prepared statement caching
- PostGIS spatial indexing

### Cache Strategy

- Redis for session storage
- Application-level caching
- Database query result caching

## 🔒 Security

### Container Security

- Non-root user execution
- Minimal base image (Amazon Corretto)
- Security scanning with Trivy

### Network Security

- Internal Docker network
- No unnecessary port exposure
- Environment-based secrets

### Data Security

- Database credentials via environment variables
- Redis password protection
- TLS/SSL termination at load balancer

## � Troubleshooting

### Application Issues

**Symptoms**: HTTP 500 errors on all endpoints, including health checks
**Root Cause**: Application treating REST endpoints as static resources
**Current Status**: Under investigation

```bash
# Check application logs for errors
docker logs orders-app --tail 50

# Verify container health status
docker ps --format "table {{.Names}}\t{{.Status}}"

# Restart application container if needed
docker-compose restart orders-app
```

**Known Issues**:

- All API endpoints (including `/api/actuator/*`) return HTTP 500
- Spring MVC not properly routing requests to controllers
- Requests being handled by ResourceHttpRequestHandler instead of REST controllers

**Workaround**: Container infrastructure is healthy, application requires code-level debugging

### Infrastructure Issues

**Database Connection**:

```bash
# Test database connectivity
docker-compose exec postgres pg_isready -U orders_user -d orders_db
```

**Redis Connection**:

```bash
# Test Redis connectivity  
docker-compose exec redis redis-cli ping
```

**Kafka Connection**:

```bash
# Check Kafka status
docker-compose exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```

## �📚 Additional Resources

- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)
- [Docker Compose Best Practices](https://docs.docker.com/compose/production/)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)
- [Redis Docker Hub](https://hub.docker.com/_/redis)

## 🆘 Support

For issues related to containerization:

1. Check this documentation
2. Review Docker Compose logs
3. Verify system requirements
4. Check port availability
5. Validate Docker installation

---

**Note**: This containerized environment is designed for development and testing. For production deployment, consider using Kubernetes or similar container orchestration platforms with appropriate security, monitoring, and scaling configurations.
