package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ContaDAO {

    private static final String SELECT_BASE = """
        SELECT c.PK_idConta AS conta_id, c.tipo_conta, c.saldo, c.chave,
               u.PK_idUsuario AS usuario_id, u.nome, u.cpf, u.telefone, u.endereco,
               u.senhaHash AS senha_hash, u.salt
        FROM TB_Conta c
        JOIN TB_Usuario u ON u.PK_idUsuario = c.FK_idUsuario
    """;

    public void inserir(Connection conn, Conta conta) throws SQLException {
        if (conta.getUsuario().getIdUsuario() == null) {
            throw new IllegalArgumentException("Salve o usuário no banco antes de salvar a conta dele.");
        }
        if (conta.getTipoConta() == null) {
            throw new IllegalArgumentException("A conta precisa ter um tipo (corrente ou poupança).");
        }

        String sql = "INSERT INTO TB_Conta (FK_idUsuario, tipo_conta, saldo, chave) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, conta.getUsuario().getIdUsuario());
            stmt.setString(2, conta.getTipoConta().name());
            stmt.setDouble(3, conta.getSaldo());
            stmt.setString(4, conta.getChave());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    conta.setIdConta(keys.getLong(1));
                }
            }
        }

        inserirEspecializacao(conn, conta);
    }

    private void inserirEspecializacao(Connection conn, Conta conta) throws SQLException {
        String sql = (conta.getTipoConta() == TipoConta.CORRENTE)
                ? "INSERT INTO TB_Corrente (PK_idConta_Corrente) VALUES (?)"
                : "INSERT INTO TB_Poupanca (PK_idConta_Poupanca) VALUES (?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, conta.getIdConta());
            stmt.executeUpdate();
        }
    }

    public Conta buscarPorId(Connection conn, Long idConta) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + " WHERE c.PK_idConta = ?")) {
            stmt.setLong(1, idConta);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Conta buscarPorChave(Connection conn, String chave) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + " WHERE c.chave = ?")) {
            stmt.setString(1, chave);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Todas as contas de um usuário (um usuário pode ter corrente e poupança). */
    public List<Conta> buscarPorUsuario(Connection conn, Long idUsuario) throws SQLException {
        List<Conta> contas = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(
                SELECT_BASE + " WHERE c.FK_idUsuario = ? ORDER BY c.PK_idConta")) {
            stmt.setLong(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    contas.add(mapear(rs));
                }
            }
        }
        return contas;
    }

    public void atualizarSaldo(Connection conn, Conta conta) throws SQLException {
        String sql = "UPDATE TB_Conta SET saldo = ? WHERE PK_idConta = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, conta.getSaldo());
            stmt.setLong(2, conta.getIdConta());
            stmt.executeUpdate();
        }
    }

    private Conta mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(
            rs.getLong("usuario_id"),
            rs.getString("nome"),
            rs.getString("cpf"),
            rs.getString("telefone"),
            rs.getString("endereco"),
            rs.getString("senha_hash"),
            rs.getString("salt")
        );

        return new Conta(
            rs.getLong("conta_id"),
            usuario,
            TipoConta.valueOf(rs.getString("tipo_conta")),
            rs.getDouble("saldo"),
            rs.getString("chave")
        );
    }
}
