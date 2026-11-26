package repository;

import model.Aposta;
import model.ResumoApostas;
import model.TipoJogo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApostaDAO {

    /**
     * Grava a aposta ligada às transações que movimentaram o saldo.
     * O prêmio é nulo quando o jogador perdeu (não existe transação de prêmio).
     */
    public void salvar(Connection conn, Aposta aposta, Long idTransacaoAposta, Long idTransacaoPremio)
            throws SQLException {
        if (idTransacaoAposta == null) {
            throw new IllegalArgumentException("Salve a transação da aposta antes de salvar a aposta.");
        }

        String sql = """
            INSERT INTO TB_Aposta (FK_idConta, jogo, valor_apostado, escolha, resultado, ganhou, premio, data_hora, FK_idTransacao_Aposta, FK_idTransacao_Premio)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, aposta.getIdConta());
            stmt.setString(2, aposta.getJogo().name());
            stmt.setDouble(3, aposta.getValor());
            stmt.setString(4, aposta.getEscolha());
            stmt.setString(5, aposta.getDescricao());
            stmt.setInt(6, aposta.isGanhou() ? 1 : 0);
            stmt.setDouble(7, aposta.getPremio());
            stmt.setString(8, aposta.getDataHora().toString());
            stmt.setLong(9, idTransacaoAposta);
            if (idTransacaoPremio != null) {
                stmt.setLong(10, idTransacaoPremio);
            } else {
                stmt.setNull(10, Types.INTEGER);
            }
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    aposta.setId(keys.getLong(1));
                }
            }
        }
    }

    /** Transforma em vitória uma rodada do foguete que estava gravada como perdida. */
    public void registrarRetirada(Connection conn, Long idAposta, double premio, String resultado, Long idTransacaoPremio) throws SQLException {
        String sql = """
            UPDATE TB_Aposta SET ganhou = 1, premio = ?, resultado = ?, FK_idTransacao_Premio = ?
            WHERE PK_idAposta = ? AND ganhou = 0
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, premio);
            stmt.setString(2, resultado);
            stmt.setLong(3, idTransacaoPremio);
            stmt.setLong(4, idAposta);
            if (stmt.executeUpdate() != 1) {
                throw new SQLException("A rodada não pode ser paga de novo.");
            }
        }
    }

    /** Apostas mais recentes da conta, da mais nova para a mais antiga. */
    public List<Aposta> listarRecentes(Connection conn, Long idConta, int limite) throws SQLException {
        String sql = """
            SELECT PK_idAposta, FK_idConta, jogo, valor_apostado, escolha, resultado, ganhou, premio, data_hora
            FROM TB_Aposta
            WHERE FK_idConta = ?
            ORDER BY data_hora DESC, PK_idAposta DESC
            LIMIT ?
        """;

        List<Aposta> apostas = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idConta);
            stmt.setInt(2, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    apostas.add(new Aposta(
                        rs.getLong("PK_idAposta"),
                        rs.getLong("FK_idConta"),
                        TipoJogo.valueOf(rs.getString("jogo")),
                        rs.getDouble("valor_apostado"),
                        rs.getString("escolha"),
                        rs.getString("resultado"),
                        rs.getInt("ganhou") == 1,
                        rs.getDouble("premio"),
                        LocalDateTime.parse(rs.getString("data_hora"))
                    ));
                }
            }
        }
        return apostas;
    }

    /** Quantidade de apostas, total apostado e total de prêmios da conta. */
    public ResumoApostas resumir(Connection conn, Long idConta) throws SQLException {
        String sql = """
            SELECT COUNT(*) AS quantidade,
                    COALESCE(SUM(valor_apostado), 0) AS total_apostado,
                    COALESCE(SUM(premio), 0) AS total_premios
            FROM TB_Aposta
            WHERE FK_idConta = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idConta);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return new ResumoApostas(
                    rs.getInt("quantidade"),
                    rs.getDouble("total_apostado"),
                    rs.getDouble("total_premios")
                );
            }
        }
    }
}