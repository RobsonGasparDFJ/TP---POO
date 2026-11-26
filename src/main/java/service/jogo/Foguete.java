package service.jogo;

import model.TipoJogo;
import model.ResultadoJogo;

import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Jogo do foguete (crash): o multiplicador sobe até o foguete explodir num ponto sorteado.
 * Quem retira antes recebe a aposta vezes o multiplicador do momento.
 *
 * A chance de o foguete chegar a um multiplicador m é RETORNO / m, então o retorno
 * esperado é o mesmo (97%) para qualquer ponto de retirada.
 */
public class Foguete extends Jogo {

    public static final double RETORNO = 0.97;
    public static final double RETIRADA_MINIMA = 1.01;
    public static final double MULTIPLICADOR_MAXIMO = 100.0;
    /** Velocidade da subida: o multiplicador é e^(VELOCIDADE * segundos). */
    private static final double VELOCIDADE = 0.18;

    public Foguete(Random random) {
        super(TipoJogo.FOGUETE, random);
    }

    @Override
    public String getRegras() {
        return "O multiplicador sobe até o foguete explodir. Retire antes para ganhar a aposta "
                + "vezes o multiplicador (máximo de 100x).";
    }

    @Override
    public List<String> getOpcoes() {
        return List.of();
    }

    @Override
    public double getRetornoEsperado() {
        return RETORNO;
    }

    /** Sorteia onde o foguete explode, entre 1,00x e o máximo, com duas casas. */
    public double sortearExplosao() {
        double ponto = Math.floor(100 * RETORNO / (1 - random.nextDouble())) / 100;
        return Math.max(1.0, Math.min(ponto, MULTIPLICADOR_MAXIMO));
    }

    /** Multiplicador mostrado depois de certo tempo de voo. */
    public static double multiplicadorEm(double segundos) {
        return Math.floor(100 * Math.exp(VELOCIDADE * segundos)) / 100;
    }

    public static void validarRetirada(double multiplicador) {
        if (!Double.isFinite(multiplicador)
                || multiplicador < RETIRADA_MINIMA || multiplicador > MULTIPLICADOR_MAXIMO) {
            throw new IllegalArgumentException("A retirada deve ficar entre 1,01x e 100x.");
        }
    }

    public static String formatar(double multiplicador) {
        return String.format(Locale.forLanguageTag("pt-BR"), "%.2fx", multiplicador);
    }

    /** Rodada resolvida de uma vez: a escolha é o multiplicador da retirada automática. */
    @Override
    protected ResultadoJogo sortear(double aposta, String escolha) {
        if (escolha == null) {
            throw new IllegalArgumentException("Informe o multiplicador da retirada.");
        }
        double alvo = Double.parseDouble(escolha.replace(',', '.'));
        validarRetirada(alvo);

        double explosao = sortearExplosao();
        boolean ganhou = alvo <= explosao;
        return new ResultadoJogo(ganhou, ganhou ? arredondar(aposta * alvo) : 0.0,
                "Explodiu em " + formatar(explosao));
    }
}