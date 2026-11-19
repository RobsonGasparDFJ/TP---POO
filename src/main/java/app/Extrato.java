package app;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Extrato {

    private Conta conta;
    private List<Transacao> transacoes;

    public Extrato(Conta conta) {
        if (conta == null) {
            throw new IllegalArgumentException("O extrato precisa estar associado a uma conta válida.");
        }
        this.conta = conta;
        this.transacoes = new ArrayList<>();
    }

    public Extrato(Conta conta, List<Transacao> transacoes) {
        if (conta == null) {
            throw new IllegalArgumentException("O extrato precisa estar associado a uma conta válida.");
        }
        this.conta = conta;
        this.transacoes = (transacoes != null) ? transacoes : new ArrayList<>();
    }

    public void adicionarTransacao(Transacao transacao) {
        if (transacao != null) {
            this.transacoes.add(transacao);
        }
    }

    public Conta getConta() {
        return this.conta;
    }

    public List<Transacao> getTransacoes() {
        return Collections.unmodifiableList(transacoes);
    }

    public void exibirExtrato(){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        System.out.println("===== EXTRATO - Conta " + conta.getIdConta() + " =====");

        if (transacoes.isEmpty()) {
            System.out.println("Nenhuma transação encontrada.");
            return;
        }

        for (Transacao t : transacoes) {
            System.out.printf("%s | %s | R$ %.2f%n",
                    t.getDataHora().format(fmt),
                    t.getTipo(),
                    t.getValor());
        }
        System.out.println("==================================");
    }

}