package service.jogo;

import java.util.List;
import java.util.Random;
import java.util.Set;

import model.TipoJogo;
import model.ResultadoJogo;

/**
 * Roleta europeia: números de 0 a 36, todos com a mesma chance. O zero é verde e não
 * conta como par nem ímpar; é dele que vem a vantagem da casa.
 */
public class Roleta extends Jogo {

    public static final String VERMELHO = "VERMELHO";
    public static final String PRETO = "PRETO";
    public static final String VERDE = "VERDE";
    public static final String PAR = "PAR";
    public static final String IMPAR = "ÍMPAR";

    public static final int CASAS = 37;

    /** Cor, par e ímpar: 18 chances em 37 pagando 2x. */
    public static final double MULTIPLICADOR_SIMPLES = 2.0;
    /** Verde: 1 chance em 37 pagando 36x. Todas as opções têm o mesmo retorno (36/37). */
    public static final double MULTIPLICADOR_VERDE = 36.0;

    private static final Set<Integer> VERMELHOS =
            Set.of(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36);

    public Roleta(Random random) {
        super(TipoJogo.ROLETA, random);
    }

    @Override
    public String getRegras() {
        return "Aposte numa cor ou em par/ímpar. Vermelho, preto, par e ímpar pagam 2x; "
                + "verde (o zero) paga 36x.";
    }

    @Override
    public List<String> getOpcoes() {
        return List.of(VERMELHO, PRETO, VERDE, PAR, IMPAR);
    }

    @Override
    public double getRetornoEsperado() {
        return MULTIPLICADOR_VERDE / CASAS;
    }

    /** Cor da casa sorteada. */
    public static String cor(int numero) {
        if (numero == 0) {
            return VERDE;
        }
        return VERMELHOS.contains(numero) ? VERMELHO : PRETO;
    }

    /** Quanto a opção paga quando sai o número, em múltiplos da aposta (0 se perder). */
    public static double multiplicador(int numero, String escolha) {
        boolean acertou = switch (escolha) {
            case VERMELHO, PRETO, VERDE -> cor(numero).equals(escolha);
            case PAR -> numero != 0 && numero % 2 == 0;
            case IMPAR -> numero % 2 == 1;
            default -> false;
        };
        if (!acertou) {
            return 0.0;
        }
        return VERDE.equals(escolha) ? MULTIPLICADOR_VERDE : MULTIPLICADOR_SIMPLES;
    }

    @Override
    protected ResultadoJogo sortear(double aposta, String escolha) {
        int numero = random.nextInt(CASAS);
        String descricao = "Saiu " + numero + " " + cor(numero);

        double multiplicador = multiplicador(numero, escolha);
        return new ResultadoJogo(multiplicador > 0, arredondar(aposta * multiplicador), descricao);
    }
}
