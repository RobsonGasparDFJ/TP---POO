package app;

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

    public void exibirExtrato(){};

}