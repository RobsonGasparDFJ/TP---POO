package app;

public class ContaPoupanca extends Conta{
    public ContaPoupanca(int id, Usuario usuario, String chave){
        super(id, usuario, TipoConta.POUPANCA, chave);
    }
}
