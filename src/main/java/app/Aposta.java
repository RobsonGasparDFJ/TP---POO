package app;

import java.time.LocalDateTime;

/** Registro de uma rodada do cassino: o que foi apostado, o que saiu e quanto voltou. */
public class Aposta {

    private Long id;
    private final Long idConta;
    private final TipoJogo jogo;
    private final double valor;
    private final String escolha;
    private final String descricao;
    private final boolean ganhou;
    private final double premio;
    private final LocalDateTime dataHora;

    // construtor para recuperar registros do banco
    public Aposta(Long id, Long idConta, TipoJogo jogo, double valor, String escolha,
                  String descricao, boolean ganhou, double premio, LocalDateTime dataHora) {
        this.id = id;
        this.idConta = idConta;
        this.jogo = jogo;
        this.valor = valor;
        this.escolha = escolha;
        this.descricao = descricao;
        this.ganhou = ganhou;
        this.premio = premio;
        this.dataHora = dataHora;
    }

    // construtor para criar uma nova aposta
    public Aposta(Long idConta, TipoJogo jogo, double valor, String escolha,
                  String descricao, boolean ganhou, double premio, LocalDateTime dataHora) {
        this(null, idConta, jogo, valor, escolha, descricao, ganhou, premio, dataHora);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdConta() {
        return idConta;
    }

    public TipoJogo getJogo() {
        return jogo;
    }

    public double getValor() {
        return valor;
    }

    /** O que o jogador escolheu (cara, um número do dado...); nulo quando o jogo não pede escolha. */
    public String getEscolha() {
        return escolha;
    }

    /** O que foi sorteado. */
    public String getDescricao() {
        return descricao;
    }

    public boolean isGanhou() {
        return ganhou;
    }

    /** Total que voltou para a conta; 0 quando perdeu. */
    public double getPremio() {
        return premio;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    /** Ganho (positivo) ou perda (negativo) da rodada. */
    public double getLiquido() {
        return premio - valor;
    }
}
