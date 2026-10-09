package app;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "conta")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idConta;
    
    private Usuario usuario;
    private TipoConta tipoConta;
    private double saldo;
    private String chave;

    protected Conta(int id, Usuario usuario, TipoConta tipoConta, String chave){
        if(usuario == null){
            System.out.println("A conta precisa ser associada a um Usuario.");
            return;
        }
        this.idConta = id;
        this.usuario = usuario;
        this.tipoConta = tipoConta;
        this.saldo = 0.0;
        setChave(chave);
        usuario.setConta(this);

    }

    public void depositar(double valor){
        if(valor > 0) {
            this.saldo += valor;
        }
        else{
            System.out.println("Não é possível depositar um valor negativo");
        }
    }

    public boolean sacar(double valor){
        if(this.getSaldo() - valor < 0){
            System.out.println("Saldo insuficiente");
            return false;
        }
        else if(valor <= 0){
            System.out.println("Não é possível sacar um valor negativo");
            return false;
        }
        else{
            this.saldo -= valor;
            return true;
        }
    }

    // Getters e Setters

    public int getIdConta() {
        return idConta;
    }
    public void setIdConta(int idConta) {
        this.idConta = idConta;
    }

    public Usuario getUsuario() {
        return usuario;
    }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public TipoConta getTipoConta() {
        return tipoConta;
    }
    public void setTipoConta(TipoConta tipoConta) {
        this.tipoConta = tipoConta;
    }

    public double getSaldo() {
        return saldo;
    }

    public String getChave() {
        return chave;
    }
    public void setChave(String chave) {
        if(chave != null){
            System.out.println("A chave pix não pode ser vazia");
        }
        this.chave = chave;
    }
}
