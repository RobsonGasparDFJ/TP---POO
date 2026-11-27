package repository;

import model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginTest {

    private static final String CPF = "12345678901";

    @TempDir
    Path pastaTemporaria;

    private UsuarioDAO usuarioDAO;

    @BeforeEach
    void prepararBanco() throws SQLException {
        String url = "jdbc:sqlite:" + pastaTemporaria.resolve("login.db").toAbsolutePath();
        usuarioDAO = new UsuarioDAO(() -> DriverManager.getConnection(url));

        try (Connection conexao = DriverManager.getConnection(url);
                Statement statement = conexao.createStatement()) {
            statement.execute("""
                    CREATE TABLE TB_Usuario (
                        PK_idUsuario INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        cpf TEXT NOT NULL UNIQUE,
                        telefone TEXT,
                        endereco TEXT,
                        senhaHash TEXT NOT NULL,
                        salt TEXT NOT NULL,
                        fotoPerfil TEXT
                    )
                    """);
        }

        usuarioDAO.salvar(new Usuario("Usuário Teste", CPF,
                "11999999999", "Rua de Teste", "senha123"));
    }

    @Test
    void loginAceitaCredenciaisCorretasERejeitaCredenciaisInvalidas() {
        assertTrue(usuarioDAO.autenticar(CPF, "senha123"));
        assertFalse(usuarioDAO.autenticar(CPF, "senhaerrada"));
        assertFalse(usuarioDAO.autenticar("00000000000", "senha123"));
    }
}
