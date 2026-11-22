package app;

/**
 * Resultado de uma rodada.
 *
 * @param ganhou    se o jogador recebeu algum prêmio
 * @param premio    total que volta para a conta (aposta × multiplicador); 0 quando perdeu
 * @param descricao o que foi sorteado, por exemplo "Deu CARA" ou "CEREJA | SINO | SETE"
 */
public record ResultadoJogo(boolean ganhou, double premio, String descricao) {
}
