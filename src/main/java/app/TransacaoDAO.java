package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class TransacaoDAO {

    public void salvar(Connection conn, Transacao transacao) throws SQLException {
        String sql = """
            INSERT INTO transacoes (remetente_id, destinatario_id, valor, data_hora, tipo)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (transacao.getRemetente() != null) {
                stmt.setLong(1, transacao.getRemetente().getIdConta());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            if (transacao.getDestinatario() != null) {
                stmt.setLong(2, transacao.getDestinatario().getIdConta());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            stmt.setDouble(3, transacao.getValor());
            stmt.setString(4, transacao.getDataHora().toString());
            stmt.setString(5, transacao.getTipo().name());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    transacao.setId(keys.getLong(1));
                }
            }
        }
    }
}
