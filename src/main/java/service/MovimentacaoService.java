package service;

import model.Transacao;
import repository.*;
import model.Conta;
import model.TipoTransacao;

import java.sql.Connection;
import java.sql.SQLException;

/** Operações de saldo que precisam registrar a movimentação no extrato. */
public class MovimentacaoService {

    private final ContaDAO contaDAO = new ContaDAO();
    private final TransacaoDAO transacaoDAO = new TransacaoDAO();

    public Transacao depositar(Long idConta, double valor) {
        if (idConta == null) {
            throw new IllegalArgumentException("Selecione uma conta para adicionar saldo.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor do depósito deve ser maior que zero.");
        }

        try (Connection conexao = DatabaseSetup.getConnection()) {
            conexao.setAutoCommit(false);
            try {
                Conta conta = contaDAO.buscarPorId(conexao, idConta);
                if (conta == null) {
                    throw new IllegalArgumentException("Conta não encontrada.");
                }
                conta.depositar(valor);
                contaDAO.atualizarSaldo(conexao, conta);

                Transacao deposito = new Transacao(null, null, conta, valor,
                        java.time.LocalDateTime.now(), TipoTransacao.DEPOSITO);
                transacaoDAO.salvar(conexao, deposito);
                conexao.commit();
                return deposito;
            } catch (Exception e) {
                conexao.rollback();
                if (e instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException("Erro ao adicionar saldo: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão com o banco: " + e.getMessage(), e);
        }
    }
}
