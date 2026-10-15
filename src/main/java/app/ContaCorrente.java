package app;

public class ContaCorrente extends Conta{

    public ContaCorrente(Usuario usuario, String chave){
        super(usuario, TipoConta.CORRENTE, chave);
    }
}
