package app;

import jakarta.persistence.Entity;

@Entity
public class ContaPoupanca extends Conta{

    protected ContaPoupanca() {}

    public ContaPoupanca(Usuario usuario, String chave){
        super(usuario, TipoConta.POUPANCA, chave);
    }
}
