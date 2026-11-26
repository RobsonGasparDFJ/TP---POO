package service.jogo;

import java.util.List;
import java.util.Random;

import model.TipoJogo;
import model.ResultadoJogo;

public class CaraOuCoroa extends Jogo {

    public static final String CARA = "CARA";
    public static final String COROA = "COROA";

    /** Chance de 1/2 pagando 1,9x: de cada R$ 1,00 apostado voltam R$ 0,95 em média. */
    public static final double MULTIPLICADOR = 1.9;

    public CaraOuCoroa(Random random) {
        super(TipoJogo.CARA_COROA, random);
    }

    @Override
    public String getRegras() {
        return "Escolha cara ou coroa. Se acertar, você recebe 1,9x o valor apostado.";
    }

    @Override
    public List<String> getOpcoes() {
        return List.of(CARA, COROA);
    }

    @Override
    public double getRetornoEsperado() {
        return 0.5 * MULTIPLICADOR;
    }

    @Override
    protected ResultadoJogo sortear(double aposta, String escolha) {
        String sorteado = random.nextBoolean() ? CARA : COROA;
        String descricao = "Deu " + sorteado;

        if (sorteado.equals(escolha)) {
            return new ResultadoJogo(true, arredondar(aposta * MULTIPLICADOR), descricao);
        }
        return new ResultadoJogo(false, 0.0, descricao);
    }
}
