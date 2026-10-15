package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {

    private static final String URL = "jdbc:sqlite:banco-de-dados.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void inicializarBanco() {
        String sqlUsuarios = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                cpf TEXT NOT NULL UNIQUE,
                telefone TEXT,
                endereco TEXT,
                senha_hash TEXT NOT NULL,
                salt TEXT NOT NULL
            );
        """;

        String sqlContas = """
            CREATE TABLE IF NOT EXISTS contas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER NOT NULL,
                tipo_conta TEXT NOT NULL,
                saldo REAL NOT NULL DEFAULT 0.0,
                chave TEXT NOT NULL,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
            );
        """;

        String sqlTransacoes = """
            CREATE TABLE IF NOT EXISTS transacoes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                remetente_id INTEGER,
                destinatario_id INTEGER,
                valor REAL NOT NULL,
                data_hora TEXT NOT NULL,
                tipo TEXT NOT NULL,
                FOREIGN KEY (remetente_id) REFERENCES contas(id),
                FOREIGN KEY (destinatario_id) REFERENCES contas(id)
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlContas);
            stmt.execute(sqlTransacoes);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}