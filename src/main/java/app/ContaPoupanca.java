package app;

public class ContaPoupanca extends Conta{

    public ContaPoupanca(Usuario usuario, String chave){
        super(usuario, TipoConta.POUPANCA, chave);
    }
}
