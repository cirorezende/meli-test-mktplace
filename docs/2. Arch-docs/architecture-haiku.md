# Architecture Haiku – Sistema de Processamento de Pedidos com Roteamento Inteligente para CDs

## Descrição do sistema

O sistema de processamento de pedidos do e-commerce otimiza a alocação de itens em centros de distribuição (CDs) de forma inteligente, escalável e modular. Utiliza APIs modernas, lógica geoespacial e integração com múltiplos CDs, visando reduzir custos logísticos, melhorar prazos de entrega e garantir flexibilidade para expansão, operando em ambiente cloud-native.

## Principais objetivos de negócio

- Reduzir custos logísticos e prazos de entrega
- Suportar a expansão planejada de CDs
- Garantir rastreabilidade das decisões logísticas
- Facilitar integração com sistemas internos e externos
- Prover experiência superior ao cliente final

## Principais restrições

- Processamento de pedidos com até 100 itens
- Integração obrigatória com API de consulta de CDs por item (chamada unitária)
- Uso de Java + Spring Boot
- Implantação em nuvem (AWS Fargate)
- Persistência relacional (PostgreSQL + PostGIS)
- APIs RESTful, sem frontend

## Principais assunções

- Apesar de a API de consulta de CDs não informar a localização de cada CD, assume-se que os seus endereços (e por consequencia suas coordenadas geoespaciais) são conhecidos pelo time de desenvolvimento (e até publicamente).

- A API de CDs por item é chamada unitariamente, não sendo possível fazer batch de chamadas. Assume-se que a partir do momento em que recebemos um retorno da API sobre quais CDs possuem determinado produto, este produto continuará disponível pelo menos até o fim do dia. Desta forma poderemos salvar as informações em cache para evitar requisições repetidas à API de CDs. Tal cache será limpo ao final do dia.

## Atributos de qualidade priorizados

Escalabilidade > Disponibilidade > Performance > Manutenibilidade

## Principais decisões de design

- Arquitetura monolito modular, com segmentação interna clara
- Persistência em PostgreSQL com PostGIS para dados e lógica geoespacial
- APIs RESTful para processamento e consulta de pedidos
- Integração com API externa de CDs (chamada por item)
- Geração de IDs com ULID
- Execução em contêineres gerenciados via AWS Fargate
- Publicação de eventos assíncronos via Apache Kafka (AWS MSK)
- Documentação e versionamento em GitHub privado
- Testabilidade via mocks para APIs externas

## Recomendações adicionais

### Performance e custo-eficiência nas chamadas unitárias à API de CDs

- Utilizar cache remoto (ex: Redis) para armazenar resultados de CDs por item e reduzir chamadas repetidas à API externa.
- Implementar mecanismo de retry exponencial e backoff para lidar com falhas temporárias e garantir resiliência.
- Garantir autenticação robusta nas integrações com a API de CDs.
- Implementar rate limiting para evitar sobrecarga e uso indevido da API externa.

### Crescimento do volume de dados e performance do banco PostgreSQL/PostGIS

- Normalizar dados de pedidos e CDs, evitando redundância.
- Utilizar índices geoespaciais e de busca para acelerar consultas.
- Planejar particionamento de tabelas para grandes volumes.
- Monitorar tamanho das tabelas e planejar rotinas de manutenção e particionamento para garantir performance contínua.
- Implementar arquivamento de pedidos antigos em storage frio (S3, Glacier) para manter o banco enxuto.
- Avaliar uso de read replicas para consultas analíticas.

### Segurança: práticas e ferramentas

- Utilizar AWS Shield para proteção contra ataques DDoS.
- Implementar AWS WAF para filtragem e proteção de APIs contra ameaças conhecidas (ex: SQL Injection, XSS).
- Utilizar AWS API Gateway para expor e gerenciar APIs, com autenticação, autorização e limitação de taxa (rate limiting).
- Garantir que dados sensíveis e eventos sejam acessíveis apenas via rede interna (VPC), sem exposição ao mundo exterior.
- Aplicar criptografia em trânsito (TLS) e em repouso para todos os dados sensíveis.
- Segregar ambientes (produção, homologação, desenvolvimento) e aplicar políticas de least privilege em IAM.
- Monitorar e auditar acessos e eventos de segurança (ex: AWS CloudTrail, CloudWatch Logs).
