# Testes: dashboard de KPIs e resumo mensal

## Objetivo e escopo

Este documento descreve os cenários de análise financeira. A massa fictícia
está em [dados-teste-dashboard.csv](./dados-teste-dashboard.csv) e também é
recriada nos testes automatizados em `src/test/java/service`.

A aba **Visão geral** apresenta saldo, evolução do saldo e fluxo financeiro.
O serviço `AnaliseFinanceiraService` calcula entradas e saídas por conta e por
mês. O dashboard usa o resumo sem filtro de período. Ainda não existe uma tela
de relatório mensal; os cenários mensais abaixo verificam o cálculo do serviço,
não uma tela.

## Massa e resultados esperados

Use `2026-10` como mês consultado. A conta fictícia `1` começa com saldo de
R$ 590,00 antes das movimentações da massa.

| Recorte | Entradas | Saídas | Saldo líquido |
|---|---:|---:|---:|
| Outubro/2026 | R$ 1.260,00 | R$ 240,00 | R$ 1.020,00 |
| Todas as datas | R$ 1.330,00 | R$ 330,00 | R$ 1.000,00 |

O saldo final esperado da conta `1`, após todas as linhas, é R$ 1.590,00.
Em outubro, o prêmio de R$ 10,00 é entrada e a aposta de R$ 30,00 é saída.
As linhas de setembro e novembro verificam que o resumo mensal não mistura
meses adjacentes.

## Casos de teste

| ID | Funcionalidade / cenário | Resultado esperado | Cobertura |
|---|---|---|---|
| KPI-01 | Conta sem movimentações | Entradas, saídas e saldo líquido zerados. | Automatizado no serviço |
| KPI-02 | Conta `1` com todas as movimentações | Entradas R$ 1.330,00; saídas R$ 330,00; saldo líquido R$ 1.000,00. | Automatizado no serviço |
| KPI-03 | Direção das transações | Depósito, PIX recebido e prêmio são entradas; PIX enviado, aposta, transferência enviada e saque são saídas. | Automatizado no serviço |
| KPI-04 | Resumo de outra conta | Movimentações que envolvem somente outras contas não entram nos totais. | Automatizado no serviço |
| KPI-05 | Mais de seis movimentações | O limite do gráfico não excede seis registros recentes, preservando a ordem recebida do DAO. | Limite automatizado; interface não exercitada |
| REL-01 | Resumir outubro/2026 para conta `1` | Entradas R$ 1.260,00; saídas R$ 240,00; saldo líquido R$ 1.020,00. | Automatizado no serviço; sem tela mensal |
| REL-02 | Conferir limites do mês | A saída de setembro e a entrada de novembro não entram no resumo de outubro. | Automatizado no serviço; sem tela mensal |
| REL-03 | Classificar prêmio e aposta | O prêmio é entrada e a aposta é saída. | Automatizado no serviço; sem tela mensal |
| REL-04 | Mês sem movimentações | Entradas, saídas e saldo líquido zerados. | Automatizado no serviço; sem tela mensal |

## Execução

Execute todos os testes automatizados na raiz do repositório com:

    mvn test

Os testes verificam as regras do serviço e não abrem a interface JavaFX. A
interface mensal continua pendente. Quando for criada, será necessário
verificar também a apresentação visual do relatório.

## Regra de classificação

Uma transação é entrada quando a conta consultada é seu destinatário; uma
transação enviada pela conta é saída. O dashboard usa essa regra sem filtro de
data. O método `resumirMes` aplica o filtro mensal, mas ainda não há uma tela
que apresente esse resumo.
