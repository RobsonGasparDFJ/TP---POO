package service;

import model.ResumoApostas;
import model.TipoJogo;
import model.Aposta;
import repository.*;
import service.jogo.*;
import model.Transacao;
import model.ResultadoJogo;
import model.Conta;
import model.RodadaFoguete;
import model.TipoTransacao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Regras de uma aposta: debita, sorteia, paga o prêmio e registra tudo no extrato e em TB_Aposta. */
public class CassinoService {

    private final ContaDAO contaDAO = new ContaDAO();
    private final TransacaoDAO transacaoDAO = new TransacaoDAO();
    private final ApostaDAO apostaDAO = new ApostaDAO();
    private final Map<TipoJogo, Jogo> jogos = new EnumMap<>(TipoJogo.class);

    public CassinoService() {
        this(new Random());
    }

    /** Permite informar o gerador de sorteios (os testes usam uma semente fixa). */
    public CassinoService(Random random) {
        registrar(new CaraOuCoroa(random));
        registrar(new Dado(random));
        registrar(new CacaNiquel(random));
        registrar(new Roleta(random));
        registrar(new Foguete(random));
    }

    private void registrar(Jogo jogo) {
        jogos.put(jogo.getTipo(), jogo);
    }

    public List<Jogo> listarJogos() {
        return List.copyOf(jogos.values());
    }

    public Jogo getJogo(TipoJogo tipo) {
        return jogos.get(tipo);
    }

    /**
     * Faz uma aposta. Tudo acontece numa única transação do banco: ou o saldo, o extrato e o
     * registro da aposta são gravados juntos, ou nada é gravado.
     *
     * @param escolha a opção do jogador (cara, número do dado...); nula nos jogos sem escolha
     * @return a aposta já registrada, com o que foi sorteado e o prêmio
     */
    public Aposta apostar(Long idConta, TipoJogo tipo, String escolha, double valor) {
        if (idConta == null) {
            throw new IllegalArgumentException("Selecione uma conta para apostar.");
        }
        Jogo jogo = (tipo == null) ? null : jogos.get(tipo);
        if (jogo == null) {
            throw new IllegalArgumentException("Selecione um jogo.");
        }

        double aposta = Math.round(valor * 100.0) / 100.0;
        jogo.validar(aposta, escolha); // falha antes de abrir o banco

        try (Connection conn = DatabaseSetup.getConnection()) {
            conn.setAutoCommit(false); // tudo ou nada

            try {
                Conta conta = contaDAO.buscarPorId(conn, idConta);
                if (conta == null) {
                    throw new IllegalArgumentException("Conta não encontrada.");
                }
                if (conta.getSaldo() < aposta) {
                    throw new IllegalArgumentException("Saldo insuficiente para essa aposta.");
                }

                ResultadoJogo resultado = jogo.jogar(aposta, escolha);

                // a saída é gravada antes do prêmio: no extrato, o prêmio aparece acima da aposta
                conta.sacar(aposta);
                Transacao saida = new Transacao(conta, null, aposta, TipoTransacao.APOSTA);
                transacaoDAO.salvar(conn, saida);

                Transacao entrada = null;
                if (resultado.premio() > 0) {
                    conta.depositar(resultado.premio());
                    entrada = new Transacao(null, conta, resultado.premio(), TipoTransacao.PREMIO);
                    transacaoDAO.salvar(conn, entrada);
                }
                contaDAO.atualizarSaldo(conn, conta);

                Aposta registro = new Aposta(conta.getIdConta(), tipo, aposta, escolha,
                        resultado.descricao(), resultado.ganhou(), resultado.premio(), saida.getDataHora());
                apostaDAO.salvar(conn, registro, saida.getId(), entrada == null ? null : entrada.getId());

                conn.commit();
                return registro;

            } catch (Exception e) {
                conn.rollback(); // desfaz qualquer alteração parcial
                if (e instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException("Erro ao realizar a aposta: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão com o banco: " + e.getMessage(), e);
        }
    }

    /**
     * Primeira etapa do foguete: debita a aposta, sorteia a explosão e já grava a rodada como
     * perdida. Se o programa fechar durante o voo, o banco continua consistente.
     */
    public RodadaFoguete iniciarFoguete(Long idConta, double valor) {
        if (idConta == null) {
            throw new IllegalArgumentException("Selecione uma conta para apostar.");
        }
        Foguete foguete = (Foguete) jogos.get(TipoJogo.FOGUETE);
        double aposta = Math.round(valor * 100.0) / 100.0;
        foguete.validar(aposta, null);

        return emTransacao(conn -> {
            Conta conta = contaDAO.buscarPorId(conn, idConta);
            if (conta == null) {
                throw new IllegalArgumentException("Conta não encontrada.");
            }
            if (conta.getSaldo() < aposta) {
                throw new IllegalArgumentException("Saldo insuficiente para essa aposta.");
            }

            double explosao = foguete.sortearExplosao();
            conta.sacar(aposta);
            Transacao saida = new Transacao(conta, null, aposta, TipoTransacao.APOSTA);
            transacaoDAO.salvar(conn, saida);
            contaDAO.atualizarSaldo(conn, conta);

            Aposta registro = new Aposta(idConta, TipoJogo.FOGUETE, aposta, null,
                    "Explodiu em " + Foguete.formatar(explosao), false, 0.0, saida.getDataHora());
            apostaDAO.salvar(conn, registro, saida.getId(), null);

            return new RodadaFoguete(registro, explosao);
        });
    }

    /**
     * Segunda etapa do foguete: paga a aposta no multiplicador informado, se o foguete ainda
     * não tinha explodido. Cada rodada só pode ser retirada uma vez.
     *
     * @return a aposta atualizada, com o prêmio
     */
    public Aposta retirarFoguete(RodadaFoguete rodada, double multiplicador) {
        Foguete.validarRetirada(multiplicador);
        if (rodada.isRetirada()) {
            throw new IllegalArgumentException("Essa rodada já foi retirada.");
        }
        if (multiplicador > rodada.getExplosao()) {
            throw new IllegalArgumentException("O foguete já explodiu.");
        }

        Aposta inicial = rodada.getAposta();
        double premio = Math.round(inicial.getValor() * multiplicador * 100.0) / 100.0;
        String descricao = "Saiu " + Foguete.formatar(multiplicador)
                + " de " + Foguete.formatar(rodada.getExplosao());

        Aposta paga = emTransacao(conn -> {
            Conta conta = contaDAO.buscarPorId(conn, inicial.getIdConta());
            if (conta == null) {
                throw new IllegalArgumentException("Conta não encontrada.");
            }
            conta.depositar(premio);
            Transacao entrada = new Transacao(null, conta, premio, TipoTransacao.PREMIO);
            transacaoDAO.salvar(conn, entrada);
            contaDAO.atualizarSaldo(conn, conta);
            apostaDAO.registrarRetirada(conn, inicial.getId(), premio, descricao, entrada.getId());

            return new Aposta(inicial.getId(), inicial.getIdConta(), TipoJogo.FOGUETE, inicial.getValor(),
                    null, descricao, true, premio, inicial.getDataHora());
        });
        rodada.marcarRetirada();
        return paga;
    }

    private interface Operacao<T> {
        T executar(Connection conn) throws SQLException;
    }

    /** Executa a operação numa única transação do banco: grava tudo ou nada. */
    private <T> T emTransacao(Operacao<T> operacao) {
        try (Connection conn = DatabaseSetup.getConnection()) {
            conn.setAutoCommit(false);
            try {
                T resultado = operacao.executar(conn);
                conn.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro no banco: " + e.getMessage(), e);
        }
    }

    /** Apostas mais recentes da conta. */
    public List<Aposta> listarRecentes(Long idConta, int limite) {
        try (Connection conn = DatabaseSetup.getConnection()) {
            return apostaDAO.listarRecentes(conn, idConta, limite);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar as apostas: " + e.getMessage(), e);
        }
    }

    public ResumoApostas resumir(Long idConta) {
        try (Connection conn = DatabaseSetup.getConnection()) {
            return apostaDAO.resumir(conn, idConta);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao resumir as apostas: " + e.getMessage(), e);
        }
    }
}