package app;

/** Totais das apostas de uma conta. */
public record ResumoApostas(int quantidade, double totalApostado, double totalPremios) {

    /** Positivo se o jogador está no lucro, negativo se está no prejuízo. */
    public double saldoLiquido() {
        return totalPremios - totalApostado;
    }
}
