# Testes: dashboard de KPIs e relatório mensal de gastos

## Objetivo e escopo

Este documento organiza os testes da parte atribuída ao Robson. A massa usada é
fictícia e está em [dados-teste-dashboard.csv](./dados-teste-dashboard.csv).

Na versão do código analisada, a aba **Visão geral** apresenta saldo, evolução
do saldo e fluxo financeiro com os totais de entradas e saídas. Os valores do
fluxo são calculados sobre todas as transações da conta, sem filtro mensal.
Não foi encontrada uma tela ou rotina de relatório mensal de gastos no código
atual; os cenários desse relatório ficam bloqueados até a funcionalidade ser
integrada.

## Massa e resultados esperados

Use `2026-10` como mês consultado. A conta fictícia `1` começa com saldo de
R$ 590,00 antes das movimentações da massa.

| Recorte | Entradas | Saídas | Saldo líquido |
|---|---:|---:|---:|
| Outubro/2026 | R$ 1.260,00 | R$ 240,00 | R$ 1.020,00 |
| Todo o CSV | R$ 1.330,00 | R$ 330,00 | R$ 1.000,00 |

O saldo final esperado da conta `1`, após todas as linhas, é R$ 1.590,00.
Em outubro, o prêmio de R$ 10,00 é entrada e a aposta de R$ 30,00 é saída.
As linhas de setembro e novembro servem para verificar que o relatório mensal
não mistura meses adjacentes.

## Casos de teste

| ID | Funcionalidade / cenário | Resultado esperado | Situação |
|---|---|---|---|
| KPI-01 | Conta sem movimentações | Entradas e saídas em R$ 0,00; evolução mostra o saldo atual sem pontos de transação. | Preparado; não executado na interface |
| KPI-02 | Carregar a conta `1` com todo o CSV | Dashboard mostra entradas de R$ 1.330,00 e saídas de R$ 330,00; saldo da conta R$ 1.590,00. | Preparado; não executado na interface |
| KPI-03 | Verificar direção das transações | Depósito, PIX recebido e prêmio contam como entrada; PIX enviado, aposta, transferência enviada e saque contam como saída. | Preparado; não executado na interface |
| KPI-04 | Trocar para outra conta | O saldo, os totais e as movimentações exibidos pertencem somente à conta selecionada. | Preparado; não executado na interface |
| KPI-05 | Conta com mais de seis movimentações | O gráfico de evolução exibe no máximo as seis movimentações mais recentes, em ordem cronológica. | Preparado; não executado na interface |
| REL-01 | Gerar relatório de outubro/2026 para conta `1` | Entradas R$ 1.260,00; gastos/saídas R$ 240,00; saldo líquido R$ 1.020,00. | Bloqueado: funcionalidade não encontrada no código analisado |
| REL-02 | Conferir limites do mês | A saída de R$ 90,00 de setembro e a entrada de R$ 70,00 de novembro não entram no relatório de outubro. | Bloqueado: funcionalidade não encontrada no código analisado |
| REL-03 | Classificar prêmio e aposta | O prêmio de R$ 10,00 é entrada, não gasto; a aposta de R$ 30,00 é gasto. | Bloqueado: funcionalidade não encontrada no código analisado |
| REL-04 | Consultar mês sem movimentações | O relatório apresenta valores zerados e uma indicação clara de ausência de movimentações. | Bloqueado: funcionalidade não encontrada no código analisado |

## Registro de execução

Os resultados acima são valores esperados calculados a partir da massa, não
evidência de execução do aplicativo. A execução visual dos casos de KPI e dos
casos de relatório ainda precisa ser feita e registrada após a integração das
funcionalidades. Antes de aprovar REL-01 a REL-04, confirmar com o responsável
pela implementação qual é o formato final do relatório e se ele usa exatamente
as mesmas regras de classificação de entradas e saídas do dashboard.

## Referência de implementação observada

O cálculo atual do dashboard classifica uma transação como entrada quando a
conta selecionada é seu destinatário; as demais transações listadas para essa
conta são somadas como saídas. Os totais não são filtrados por data. A interface
exibe esses valores em **Fluxo financeiro**. Não foi identificada implementação
de relatório mensal no código-fonte consultado.
