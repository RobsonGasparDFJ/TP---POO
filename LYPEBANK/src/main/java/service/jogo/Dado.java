package service.jogo;

import java.util.List;
import java.util.Random;

import model.TipoJogo;
import model.ResultadoJogo;

public class Dado extends Jogo {

    /** Chance de 1/6 pagando 5,5x: de cada R$ 1,00 apostado voltam cerca de R$ 0,92 em média. */
    public static final double MULTIPLICADOR = 5.5;

    public Dado(Random random) {
        super(TipoJogo.DADO, random);
    }

    @Override
    public String getRegras() {
        return "Escolha um número de 1 a 6 e role o dado. Se acertar, você recebe 5,5x o valor apostado.";
    }

    @Override
    public List<String> getOpcoes() {
        return List.of("1", "2", "3", "4", "5", "6");
    }

    @Override
    public double getRetornoEsperado() {
        return MULTIPLICADOR / 6.0;
    }

    @Override
    protected ResultadoJogo sortear(double aposta, String escolha) {
        int sorteado = random.nextInt(6) + 1;
        String descricao = "Caiu " + sorteado;

        if (sorteado == Integer.parseInt(escolha)) {
            return new ResultadoJogo(true, arredondar(aposta * MULTIPLICADOR), descricao);
        }
        return new ResultadoJogo(false, 0.0, descricao);
    }
}
