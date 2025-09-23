# PRD – Sistema de Processamento de Pedidos com Roteamento Inteligente para CDs

## Nome do Produto ou Funcionalidade

Sistema de processamento de pedidos com roteamento inteligente para centros de distribuição (CDs)

## Objetivo Principal

Desenvolver uma solução que processe pedidos e determine qual centro de distribuição (CD) deve ser usado para o envio de cada item, com base nas informações fornecidas pela API de centros de distribuição.

## Time Envolvido

Líder técnico de uma empresa de e-commerce (outros membros não especificados)

## Visão Geral

Com a expansão dos centros de distribuição, o sistema atual não consegue otimizar o envio dos itens, gerando custos logísticos elevados e atrasos.
A ausência de inteligência para roteamento de pedidos dificulta a escalabilidade e a experiência do cliente.
Queremos garantir que cada item seja enviado do CD mais adequado, reduzindo prazos e custos.

## Escopo (In / Out)

**In:**

- Processamento de pedidos com até 100 itens
- API para processar pedidos e vincular CDs
- API para consultar pedidos e CDs vinculados
- Integração com API de consulta de CDs por item
- Inteligência para determinar o melhor CD a partir de dados de geolocalização

**Out:**

- Desenvolvimento de frontend
- Integração com sistemas de pagamento
- Sistema de notificações de status do pedido
- Quaisquer outras funcionalidades não informadas

## Personas

- **Cliente Final:** Indiretamente beneficiado, pois recebe os pedidos de forma mais rápida e eficiente.
- **Operador Logístico:** Usuário responsável por acompanhar e gerenciar o processamento dos pedidos e a alocação dos CDs.
- **Desenvolvedor Backend:** Profissional que irá manter, evoluir e dar suporte à solução técnica.

## Requisitos Funcionais

- O sistema deve permitir o processamento de pedidos com até 100 itens.
- Deve disponibilizar uma API para processar pedidos e retornar os CDs vinculados a cada item.
- Deve disponibilizar uma API para consultar pedidos, retornando os itens e os CDs vinculados.
- O sistema deve integrar e consumir a API de consulta de CDs por item para determinar o CD ideal.
- O sistema deve registrar o vínculo de cada item do pedido ao CD selecionado.

## Requisitos Não Funcionais

- O sistema deve ser desenvolvido em Java, utilizando o framework Springboot (especialização técnica do time de desenvolvimento).
- O sistema deve ser documentado e versionado em repositório GitHub privado.
- Não é necessário desenvolver frontend.
- O sistema deve ser implantado em nuvem desde o início, aproveitando benefícios como setup simplificado, escalabilidade, elasticidade e modelo de custo 'pay as you go' (baixo investimento inicial).
- O sistema deve ser capaz de processar pedidos de forma eficiente, mesmo com aumento do número de CDs.
- APIs devem seguir boas práticas de REST e retornar respostas em formato JSON.
- O sistema deve ser facilmente testável, permitindo uso de mocks para a API de CDs.

## Fluxo Principal de Usuário

1. O cliente final realiza uma compra na plataforma de e-commerce, incluindo diversos itens em seu pedido.
2. O pedido é recebido pelo sistema de backend, que devolve o identificador do pedido gerado e salva o pedido para processamento automático.
3. Para cada item do pedido, o sistema consulta a API de CDs para identificar quais centros podem atender à entrega.
4. A inteligência do sistema seleciona o CD mais adequado para cada item, considerando proximidade, disponibilidade e regras logísticas.
5. O sistema registra o vínculo de cada item ao CD selecionado e envia os detalhes do pedido para o operador logístico, que prepara o pedido para expedição.
6. O cliente final recebe notificações sobre o andamento do pedido e, posteriormente, recebe os itens de forma otimizada, com menor prazo e custo logístico.

## Métricas de Sucesso

- Redução do tempo médio de entrega dos pedidos ao cliente final.
- Percentual de pedidos entregues dentro do prazo estimado.
- Redução de custos logísticos por pedido.
- Taxa de sucesso na alocação automática de CDs para itens do pedido.
- Satisfação do cliente (NPS ou avaliações pós-entrega).
- Melhoria de disponibilidade e tempo de resposta das APIs.
