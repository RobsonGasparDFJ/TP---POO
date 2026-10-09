package app;

import jakarta.persistence.Entity;

@Entity
public class ContaCorrente extends Conta{

    protected ContaCorrente() {}

    public ContaCorrente(Usuario usuario, String chave){
        super(usuario, TipoConta.CORRENTE, chave);
    }
}
