package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContaDAO {

    private static final String SELECT_BASE = """
        SELECT c.id AS conta_id, c.tipo_conta, c.saldo, c.chave,
               u.id AS usuario_id, u.nome, u.cpf, u.telefone, u.endereco, u.senha_hash, u.salt
        FROM contas c
        JOIN usuarios u ON u.id = c.usuario_id
    """;

    // Os métodos recebem a Connection para participar da mesma transação da transferência.

    public Conta buscarPorId(Connection conn, Long idConta) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + " WHERE c.id = ?")) {
            stmt.setLong(1, idConta);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Conta buscarPorChave(Connection conn, String chave) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + " WHERE c.chave = ? LIMIT 1")) {
            stmt.setString(1, chave);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public void atualizarSaldo(Connection conn, Conta conta) throws SQLException {
        String sql = "UPDATE contas SET saldo = ? WHERE id = ?";
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
