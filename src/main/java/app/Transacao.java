package app;

import java.time.LocalDateTime;

public class Transacao {

    private Long id;
    private Conta remetente;
    private Conta destinatario;
    private double valor;
    private LocalDateTime dataHora;
    private TipoTransacao tipo;

    //construtor para recuperar registros do banco
    public Transacao(Long id, Conta remetente, Conta destinatario, double valor, LocalDateTime dataHora, TipoTransacao tipo) {
        this.id = id;
        this.remetente = remetente;
        this.destinatario = destinatario;
        this.valor = valor;
        this.dataHora = dataHora;
        this.tipo = tipo;
    }

    //construtor para criar uma nova transação
    public Transacao(Conta remetente, Conta destinatario, double valor, TipoTransacao tipo) {
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor da transação deve ser maior que zero.");
        }
        this.remetente = remetente;
        this.destinatario = destinatario;
        this.valor = valor;
        this.dataHora = LocalDateTime.now();
        this.tipo = tipo;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Conta getRemetente() {
        return this.remetente;
    }

    public Conta getDestinatario() {
        return this.destinatario;
    }

    public double getValor() {
        return this.valor;
    }

    public LocalDateTime getDataHora() {
        return this.dataHora;
    }

    public TipoTransacao getTipo() {
        return this.tipo;
    }
}