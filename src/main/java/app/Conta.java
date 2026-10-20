package app;

public class Conta {

    private Long idConta;
    private Usuario usuario;
    private TipoConta tipoConta;
    private double saldo;
    private String chave;

    // Construtor para criar uma conta nova
    public Conta(Usuario usuario, TipoConta tipoConta, String chave){
        if(usuario == null){
           throw new IllegalArgumentException("A conta precisa ter um usuário");
        }

        this.usuario = usuario;
        this.tipoConta = tipoConta;
        this.saldo = 0.0;
        setChave(chave);
    }

    // NOVO: construtor para recuperar uma conta do banco de dados
    public Conta(Long idConta, Usuario usuario, TipoConta tipoConta, double saldo, String chave){
        this(usuario, tipoConta, chave);
        this.idConta = idConta;
        this.saldo = saldo;
    }

    public void depositar(double valor){
        if(valor <= 0) {
            throw new IllegalArgumentException("O valor de depósito não pode ser menor ou igual a 0");
        }

        this.saldo +=valor;
    }

    public void sacar(double valor){
        if(valor <= 0){
            throw new IllegalArgumentException("O valor do saque deve ser maior que 0");
        }

        if (this.saldo < valor){
            throw new IllegalArgumentException("Não há saldo suficiente na conta para realizar esse saque");
        }

        this.saldo -= valor;
    }

    // Getters e Setters

    public Long getIdConta() {
        return this.idConta;
    }

    // NOVO: necessário para preencher o id depois de inserir a conta no banco
    public void setIdConta(Long idConta) {
        this.idConta = idConta;
    }

    public Usuario getUsuario() {
        return this.usuario;
    }

    public TipoConta getTipoConta() {
        return this.tipoConta;
    }

    public double getSaldo() {
        return this.saldo;
    }

    public String getChave() {
        return this.chave;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setTipoConta(TipoConta tipoConta) {
        this.tipoConta = tipoConta;
    }

    public void setChave(String chave) {
        if(chave == null || chave.isBlank()){
            throw new IllegalArgumentException("A chave da conta não pode ser nula ou vazia");
        }

        this.chave = chave;
    }
}
