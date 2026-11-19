public class ContaCorrente extends Conta{
    public ContaCorrente(int id, Usuario usuario, String chave){
        super(id, usuario, TipoConta.CORRENTE, chave);
    }
}
