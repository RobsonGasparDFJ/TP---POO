package app;

import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {

    private static final String URL = "jdbc:sqlite:banco-de-dados.db";

    public static Connection getConnection() throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        return DriverManager.getConnection(URL, config.toProperties());
    }

    public static void inicializarBanco() {
        String sqlUsuario = """
            CREATE TABLE IF NOT EXISTS TB_Usuario (
                PK_idUsuario INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                cpf TEXT NOT NULL UNIQUE,
                telefone TEXT,
                endereco TEXT,
                senhaHash TEXT NOT NULL,
                salt TEXT NOT NULL
            );
        """;

        String sqlConta = """
            CREATE TABLE IF NOT EXISTS TB_Conta (
                PK_idConta INTEGER PRIMARY KEY AUTOINCREMENT,
                FK_idUsuario INTEGER NOT NULL,
                tipo_conta TEXT NOT NULL CHECK (tipo_conta IN ('CORRENTE', 'POUPANCA')),
                saldo REAL NOT NULL DEFAULT 0.0,
                chave TEXT NOT NULL UNIQUE,
                FOREIGN KEY (FK_idUsuario) REFERENCES TB_Usuario (PK_idUsuario)
            );
        """;

        String sqlTransacao = """
            CREATE TABLE IF NOT EXISTS TB_Transacao (
                PK_idTransacao INTEGER PRIMARY KEY AUTOINCREMENT,
                FK_idConta_Remetente INTEGER,
                FK_idConta_Destinatario INTEGER,
                valor REAL NOT NULL,
                data_hora TEXT NOT NULL,
                tipo TEXT NOT NULL CHECK (tipo IN ('PIX', 'DEPOSITO', 'SAQUE', 'TRANSFERENCIA')),
                FOREIGN KEY (FK_idConta_Remetente) REFERENCES TB_Conta (PK_idConta),
                FOREIGN KEY (FK_idConta_Destinatario) REFERENCES TB_Conta (PK_idConta)
            );
        """;

        String sqlPoupanca = """
            CREATE TABLE IF NOT EXISTS TB_Poupanca (
                PK_idConta_Poupanca INTEGER PRIMARY KEY,
                FOREIGN KEY (PK_idConta_Poupanca) REFERENCES TB_Conta (PK_idConta)
            );
        """;

        String sqlCorrente = """
            CREATE TABLE IF NOT EXISTS TB_Corrente (
                PK_idConta_Corrente INTEGER PRIMARY KEY,
                FOREIGN KEY (PK_idConta_Corrente) REFERENCES TB_Conta (PK_idConta)
            );
        """;

        String[] indices = {
            "CREATE INDEX IF NOT EXISTS fk_contas_usuarios ON TB_Conta (FK_idUsuario);",
            "CREATE INDEX IF NOT EXISTS fk_TB_Transacao_TB_Conta1_idx ON TB_Transacao (FK_idConta_Remetente);",
            "CREATE INDEX IF NOT EXISTS fk_TB_Transacao_TB_Conta2_idx ON TB_Transacao (FK_idConta_Destinatario);"
        };

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sqlUsuario);
            stmt.execute(sqlConta);
            stmt.execute(sqlTransacao);
            stmt.execute(sqlPoupanca);
            stmt.execute(sqlCorrente);
            for (String indice : indices) {
                stmt.execute(indice);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
