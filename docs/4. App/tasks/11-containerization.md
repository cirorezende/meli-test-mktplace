# Tarefa 11 - Containerização e Deploy

## Objetivo

Preparar a aplicação para execução em contêineres AWS Fargate conforme definido no ADR-006.

## Descrição

Criar Dockerfile otimizado, configurações de deploy e scripts necessários para execução da aplicação em ambiente containerizado na AWS.

## Critérios de Aceitação

- [ ] Dockerfile multi-stage otimizado
- [ ] docker-compose para desenvolvimento local
- [ ] Scripts de build e deploy
- [ ] Configuração de health checks
- [ ] Variáveis de ambiente documentadas
- [ ] Imagem Docker otimizada (< 200MB)
- [ ] Configuração para AWS Fargate

## Dockerfile Multi-stage

### Stage 1: Build

- Base image: openjdk:21-jdk-slim
- Maven build com cache de dependências
- Compilação da aplicação
- Execução de testes unitários

### Stage 2: Runtime

- Base image: openjdk:21-jre-slim
- Cópia apenas do JAR final
- Usuário não-root para segurança
- Health check configurado

## docker-compose.yml (Desenvolvimento)

```yaml
services:
  orders-app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    depends_on:
      - postgres
      - redis
  
  postgres:
    image: postgis/postgis:15-3.3
    environment:
      POSTGRES_DB: orders
      POSTGRES_USER: orders
      POSTGRES_PASSWORD: orders
    ports:
      - "5432:5432"
  
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

## Variáveis de Ambiente

- `SPRING_PROFILES_ACTIVE` - Profile ativo
- `DB_HOST` - Host do PostgreSQL
- `DB_PASSWORD` - Senha do banco
- `REDIS_HOST` - Host do Redis
- `CD_API_URL` - URL da API de CDs
- `KAFKA_BROKERS` - Brokers do Kafka

## Scripts de Deploy

### build.sh

- Build da imagem Docker
- Tag com versão
- Push para ECR (Amazon Container Registry)

### deploy.sh

- Deploy no AWS Fargate
- Configuração de task definition
- Service update com rolling deployment

## Configuração AWS Fargate

- Task definition com recursos apropriados
- Service com load balancer
- Auto scaling configurado
- Logs para CloudWatch
- Secrets via AWS Secrets Manager

## Health Checks

- Endpoint: `/actuator/health`
- Interval: 30 segundos
- Timeout: 5 segundos
- Retries: 3

## Otimizações

- Imagem base slim
- Layers cacheable
- .dockerignore configurado
- JVM tuning para containers

## ADRs Relacionados

- ADR-006: AWS Fargate
- ADR-016: Configuração via properties/env vars
