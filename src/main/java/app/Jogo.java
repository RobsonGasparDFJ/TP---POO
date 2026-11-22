package app;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Base de todos os jogos do cassino (Template Method): {@link #jogar} valida a aposta e
 * chama {@link #sortear}, que cada jogo implementa com as suas regras e pagamentos.
 *
 * O jogo só sorteia e calcula o prêmio; quem mexe no saldo é o {@link CassinoService}.
 * O {@link Random} é recebido no construtor para os testes poderem usar uma semente fixa.
 */
public abstract class Jogo {

    public static final double APOSTA_MINIMA = 1.00;
    public static final double APOSTA_MAXIMA = 1000.00;

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    private final TipoJogo tipo;
    protected final Random random;

    protected Jogo(TipoJogo tipo, Random random) {
        if (tipo == null || random == null) {
            throw new IllegalArgumentException("O jogo precisa de um tipo e de um gerador de sorteios.");
        }
        this.tipo = tipo;
        this.random = random;
    }

    public TipoJogo getTipo() {
        return tipo;
    }

    public String getNome() {
        return tipo.getNome();
    }

    /** Explicação curta das regras e dos pagamentos, para mostrar ao jogador. */
    public abstract String getRegras();

    /** Opções que o jogador pode escolher antes de apostar; vazia se o jogo não pede escolha. */
    public abstract List<String> getOpcoes();

    /** Quanto de cada R$ 1,00 apostado volta em média para o jogador (abaixo de 1: a casa ganha). */
    public abstract double getRetornoEsperado();

    /** Sorteia e calcula o prêmio. Recebe uma aposta e uma escolha já validadas. */
    protected abstract ResultadoJogo sortear(double aposta, String escolha);

    public final ResultadoJogo jogar(double aposta, String escolha) {
        validar(aposta, escolha);
        return sortear(aposta, escolha);
    }

    public void validar(double aposta, String escolha) {
        if (!Double.isFinite(aposta) || aposta < APOSTA_MINIMA || aposta > APOSTA_MAXIMA) {
            throw new IllegalArgumentException("A aposta deve ficar entre "
                    + MOEDA.format(APOSTA_MINIMA) + " e " + MOEDA.format(APOSTA_MAXIMA) + ".");
        }
        if (!getOpcoes().isEmpty() && (escolha == null || !getOpcoes().contains(escolha))) {
            throw new IllegalArgumentException("Escolha uma opção válida para " + getNome() + ".");
        }
    }

    /** Arredonda para centavos. */
    protected static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
