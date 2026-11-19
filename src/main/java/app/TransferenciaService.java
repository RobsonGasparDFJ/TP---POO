package app;

import java.sql.Connection;
import java.sql.SQLException;

public class TransferenciaService {

    private final ContaDAO contaDAO = new ContaDAO();
    private final TransacaoDAO transacaoDAO = new TransacaoDAO();

    /**
     * Transferência via Pix: o destinatário é localizado pela chave Pix.
     */
    public Transacao transferirPorPix(Long idContaOrigem, String chavePixDestino, double valor) {
        if (chavePixDestino == null || chavePixDestino.isBlank()) {
            throw new IllegalArgumentException("A chave Pix do destinatário é obrigatória.");
        }
        return executar(idContaOrigem, null, chavePixDestino, valor, TipoTransacao.PIX);
    }

    /**
     * Transferência comum: o destinatário é localizado pelo número (id) da conta.
     */
    public Transacao transferirPorConta(Long idContaOrigem, Long idContaDestino, double valor) {
        if (idContaDestino == null) {
            throw new IllegalArgumentException("O número da conta de destino é obrigatório.");
        }
        return executar(idContaOrigem, idContaDestino, null, valor, TipoTransacao.TRANSFERENCIA);
    }

    private Transacao executar(Long idOrigem, Long idDestino, String chaveDestino,
                               double valor, TipoTransacao tipo) {
        if (idOrigem == null) {
            throw new IllegalArgumentException("O número da conta de origem é obrigatório.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor da transferência deve ser maior que zero.");
        }

        try (Connection conn = DatabaseSetup.getConnection()) {
            conn.setAutoCommit(false); // tudo ou nada: ou grava as 3 coisas, ou nenhuma

            try {
                Conta origem = contaDAO.buscarPorId(conn, idOrigem);
                if (origem == null) {
                    throw new IllegalArgumentException("Conta de origem não encontrada.");
                }

                Conta destino = (tipo == TipoTransacao.PIX)
                        ? contaDAO.buscarPorChave(conn, chaveDestino)
                        : contaDAO.buscarPorId(conn, idDestino);
                if (destino == null) {
                    throw new IllegalArgumentException(tipo == TipoTransacao.PIX
                            ? "Chave Pix não encontrada."
                            : "Conta de destino não encontrada.");
                }

                if (origem.getIdConta().equals(destino.getIdConta())) {
                    throw new IllegalArgumentException("Origem e destino não podem ser a mesma conta.");
                }

                // sacar() já valida saldo insuficiente e valor inválido
                origem.sacar(valor);
                destino.depositar(valor);

                contaDAO.atualizarSaldo(conn, origem);
                contaDAO.atualizarSaldo(conn, destino);

                Transacao transacao = new Transacao(origem, destino, valor, tipo);
                transacaoDAO.salvar(conn, transacao);

                conn.commit();
                return transacao;

            } catch (Exception e) {
                conn.rollback(); // desfaz qualquer alteração parcial
                if (e instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException("Erro ao realizar a transferência: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão com o banco: " + e.getMessage(), e);
        }
    }
}
