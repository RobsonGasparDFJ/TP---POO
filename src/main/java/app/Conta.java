package app;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name = "conta")
@Inheritance(strategy = InheritanceType.JOINED)
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConta;
    
    @OneToOne
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private TipoConta tipoConta;

    private double saldo;
    private String chave;

    protected Conta() {} //construtor sem argumento obrigatório pro JPA

    public Conta(Usuario usuario, TipoConta tipoConta, String chave){
        if(usuario == null){
           throw new IllegalArgumentException("A conta precisa ter um usuário");
        }

        this.usuario = usuario;
        this.tipoConta = tipoConta;
        this.saldo = 0.0;
        setChave(chave);
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
        return idConta;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public TipoConta getTipoConta() {
        return tipoConta;
    }

    public double getSaldo() {
        return saldo;
    }

    public String getChave() {
        return chave;
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
