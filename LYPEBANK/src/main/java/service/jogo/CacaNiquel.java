package service.jogo;

import java.util.List;
import java.util.Random;

import model.TipoJogo;
import model.ResultadoJogo;

/**
 * Três rolos com cinco símbolos, todos com a mesma chance.
 * Três iguais pagam conforme o símbolo; dois iguais pagam 1,3x; o resto perde.
 */
public class CacaNiquel extends Jogo {

    public static final double MULTIPLICADOR_PAR = 1.3;

    public enum Simbolo {
        CEREJA("CEREJA", 3),
        LIMAO("LIMÃO", 4),
        SINO("SINO", 6),
        ESTRELA("ESTRELA", 8),
        SETE("SETE", 20);

        private final String rotulo;
        private final double multiplicadorTrinca;

        Simbolo(String rotulo, double multiplicadorTrinca) {
            this.rotulo = rotulo;
            this.multiplicadorTrinca = multiplicadorTrinca;
        }

        public String getRotulo() {
            return rotulo;
        }

        public double getMultiplicadorTrinca() {
            return multiplicadorTrinca;
        }
    }

    private static final Simbolo[] SIMBOLOS = Simbolo.values();

    public CacaNiquel(Random random) {
        super(TipoJogo.CACA_NIQUEL, random);
    }

    @Override
    public String getRegras() {
        return "Gire os três rolos. Três símbolos iguais pagam de 3x (cereja) a 20x (sete) a aposta; "
                + "dois iguais pagam 1,3x.";
    }

    @Override
    public List<String> getOpcoes() {
        return List.of(); // não há escolha: é só girar
    }

    /** Média sobre as 125 combinações possíveis dos rolos. */
    @Override
    public double getRetornoEsperado() {
        double soma = 0;
        for (Simbolo a : SIMBOLOS) {
            for (Simbolo b : SIMBOLOS) {
                for (Simbolo c : SIMBOLOS) {
                    soma += multiplicador(a, b, c);
                }
            }
        }
        return soma / (SIMBOLOS.length * SIMBOLOS.length * SIMBOLOS.length);
    }

    /** Quanto a combinação paga, em múltiplos da aposta (0 se perder). */
    public static double multiplicador(Simbolo a, Simbolo b, Simbolo c) {
        if (a == b && b == c) {
            return a.getMultiplicadorTrinca();
        }
        if (a == b || a == c || b == c) {
            return MULTIPLICADOR_PAR;
        }
        return 0.0;
    }

    @Override
    protected ResultadoJogo sortear(double aposta, String escolha) {
        Simbolo a = sortearSimbolo();
        Simbolo b = sortearSimbolo();
        Simbolo c = sortearSimbolo();
        String descricao = a.getRotulo() + " | " + b.getRotulo() + " | " + c.getRotulo();

        double multiplicador = multiplicador(a, b, c);
        if (multiplicador > 0) {
            return new ResultadoJogo(true, arredondar(aposta * multiplicador), descricao);
        }
        return new ResultadoJogo(false, 0.0, descricao);
    }

    private Simbolo sortearSimbolo() {
        return SIMBOLOS[random.nextInt(SIMBOLOS.length)];
    }
}
