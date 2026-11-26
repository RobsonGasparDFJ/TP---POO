package model;

/** Um voo em andamento: a aposta já foi debitada e ainda pode ser retirada. */
public class RodadaFoguete {

    private final Aposta aposta;
    private final double explosao;
    private boolean retirada = false;

    public RodadaFoguete(Aposta aposta, double explosao) {
        this.aposta = aposta;
        this.explosao = explosao;
    }

    /** O registro feito no início do voo (conta como perdido até haver retirada). */
    public Aposta getAposta() {
        return aposta;
    }

    public double getExplosao() {
        return explosao;
    }

    public boolean isRetirada() {
        return retirada;
    }

    public void marcarRetirada() {
        retirada = true;
    }
}