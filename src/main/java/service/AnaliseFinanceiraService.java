package service;

import model.Conta;
import model.Transacao;

import java.time.YearMonth;
import java.util.List;

/** Regras de cálculo usadas pelo fluxo financeiro do dashboard. */
public class AnaliseFinanceiraService {

    public Resumo resumir(Long idConta, List<Transacao> transacoes) {
        validarConta(idConta);
        if (transacoes == null) {
            throw new IllegalArgumentException("A lista de transações não pode ser nula.");
        }

        double entradas = 0.0;
        double saidas = 0.0;
        for (Transacao transacao : transacoes) {
            if (transacao == null || !pertenceAConta(transacao, idConta)) {
                continue;
            }
            if (ehEntrada(transacao, idConta)) {
                entradas += transacao.getValor();
            } else {
                saidas += transacao.getValor();
            }
        }
        return new Resumo(entradas, saidas);
    }

    public Resumo resumirMes(Long idConta, YearMonth mes, List<Transacao> transacoes) {
        validarConta(idConta);
        if (mes == null) {
            throw new IllegalArgumentException("O mês consultado não pode ser nulo.");
        }
        if (transacoes == null) {
            throw new IllegalArgumentException("A lista de transações não pode ser nula.");
        }

        List<Transacao> doMes = transacoes.stream()
                .filter(transacao -> transacao != null && transacao.getDataHora() != null)
                .filter(transacao -> YearMonth.from(transacao.getDataHora()).equals(mes))
                .toList();
        return resumir(idConta, doMes);
    }

    public boolean ehEntrada(Transacao transacao, Long idConta) {
        validarConta(idConta);
        if (transacao == null) {
            throw new IllegalArgumentException("A transação não pode ser nula.");
        }
        Conta destinatario = transacao.getDestinatario();
        return destinatario != null && idConta.equals(destinatario.getIdConta());
    }

    /** Limita uma lista já ordenada da mais recente para a mais antiga, como a consulta do DAO. */
    public List<Transacao> limitarRecentes(List<Transacao> transacoes, int limite) {
        if (transacoes == null) {
            throw new IllegalArgumentException("A lista de transações não pode ser nula.");
        }
        if (limite < 0) {
            throw new IllegalArgumentException("O limite não pode ser negativo.");
        }
        return List.copyOf(transacoes.subList(0, Math.min(limite, transacoes.size())));
    }

    private boolean pertenceAConta(Transacao transacao, Long idConta) {
        Conta remetente = transacao.getRemetente();
        Conta destinatario = transacao.getDestinatario();
        return (remetente != null && idConta.equals(remetente.getIdConta()))
                || (destinatario != null && idConta.equals(destinatario.getIdConta()));
    }

    private void validarConta(Long idConta) {
        if (idConta == null) {
            throw new IllegalArgumentException("O identificador da conta não pode ser nulo.");
        }
    }

    public record Resumo(double entradas, double saidas) {
        public double getSaldoLiquido() {
            return entradas - saidas;
        }
    }
}
