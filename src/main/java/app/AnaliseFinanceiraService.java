package app;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** Centraliza os cálculos usados pelo dashboard e pelo relatório mensal. */
public class AnaliseFinanceiraService {

    public record Resumo(double entradas, double saidas) {
        public double getSaldoLiquido() {
            return entradas - saidas;
        }
    }

    public boolean ehEntrada(Transacao transacao, Long idConta) {
        if (transacao == null || idConta == null) {
            return false;
        }
        Conta destinatario = transacao.getDestinatario();
        return destinatario != null
                && destinatario.getIdConta() != null
                && destinatario.getIdConta().equals(idConta);
    }

    public Resumo resumir(Long idConta, List<Transacao> transacoes) {
        if (idConta == null) {
            throw new IllegalArgumentException("Informe o identificador da conta.");
        }

        double entradas = 0;
        double saidas = 0;
        for (Transacao transacao : transacoes == null ? List.<Transacao>of() : transacoes) {
            if (ehEntrada(transacao, idConta)) {
                entradas += transacao.getValor();
            } else {
                saidas += transacao.getValor();
            }
        }
        return new Resumo(arredondar(entradas), arredondar(saidas));
    }

    public Resumo resumirMes(Long idConta, YearMonth mes, List<Transacao> transacoes) {
        if (mes == null) {
            throw new IllegalArgumentException("Informe o mês do relatório.");
        }

        List<Transacao> transacoesDoMes = new ArrayList<>();
        for (Transacao transacao : transacoes == null ? List.<Transacao>of() : transacoes) {
            if (transacao.getDataHora() != null
                    && YearMonth.from(transacao.getDataHora()).equals(mes)) {
                transacoesDoMes.add(transacao);
            }
        }
        return resumir(idConta, transacoesDoMes);
    }

    public List<Transacao> limitarRecentes(List<Transacao> transacoes, int quantidade) {
        if (transacoes == null || transacoes.isEmpty() || quantidade <= 0) {
            return List.of();
        }
        return transacoes.stream().limit(quantidade).toList();
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
