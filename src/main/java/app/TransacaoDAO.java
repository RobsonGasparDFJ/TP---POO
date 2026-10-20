package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransacaoDAO {

    private final ContaDAO contaDAO = new ContaDAO();

    public void salvar(Connection conn, Transacao transacao) throws SQLException {
        String sql = """
            INSERT INTO TB_Transacao (FK_idConta_Remetente, FK_idConta_Destinatario, valor, data_hora, tipo)
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

    public List<Transacao> listarPorConta(Connection conn, Long idConta) throws SQLException {
        String sql = """
            SELECT PK_idTransacao, FK_idConta_Remetente, FK_idConta_Destinatario,
                   valor, data_hora, tipo
            FROM TB_Transacao
            WHERE FK_idConta_Remetente = ? OR FK_idConta_Destinatario = ?
            ORDER BY data_hora DESC, PK_idTransacao DESC
        """;

        record Linha(long id, Long remetenteId, Long destinatarioId,
                     double valor, String dataHora, String tipo) { }

        List<Linha> linhas = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idConta);
            stmt.setLong(2, idConta);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long remetente = rs.getLong("FK_idConta_Remetente");
                    Long remetenteId = rs.wasNull() ? null : remetente;
                    long destinatario = rs.getLong("FK_idConta_Destinatario");
                    Long destinatarioId = rs.wasNull() ? null : destinatario;

                    linhas.add(new Linha(
                        rs.getLong("PK_idTransacao"),
                        remetenteId,
                        destinatarioId,
                        rs.getDouble("valor"),
                        rs.getString("data_hora"),
                        rs.getString("tipo")
                    ));
                }
            }
        }

        Map<Long, Conta> cache = new HashMap<>();
        List<Transacao> transacoes = new ArrayList<>();
        for (Linha l : linhas) {
            transacoes.add(new Transacao(
                l.id(),
                resolverConta(conn, cache, l.remetenteId()),
                resolverConta(conn, cache, l.destinatarioId()),
                l.valor(),
                LocalDateTime.parse(l.dataHora()),
                TipoTransacao.valueOf(l.tipo())
            ));
        }
        return transacoes;
    }

    private Conta resolverConta(Connection conn, Map<Long, Conta> cache, Long idConta) throws SQLException {
        if (idConta == null) {
            return null;
        }
        Conta conta = cache.get(idConta);
        if (conta == null) {
            conta = contaDAO.buscarPorId(conn, idConta);
            cache.put(idConta, conta);
        }
        return conta;
    }
}
