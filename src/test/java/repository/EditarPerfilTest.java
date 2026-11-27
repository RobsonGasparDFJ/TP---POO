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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditarPerfilTest {

    private static final String CPF = "12345678901";

    @TempDir
    Path pastaTemporaria;

    private UsuarioDAO usuarioDAO;

    @BeforeEach
    void prepararBanco() throws SQLException {
        String url = "jdbc:sqlite:" + pastaTemporaria.resolve("perfil.db").toAbsolutePath();
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
                "11999999999", "Rua Antiga", "senha123"));
    }

    @Test
    void editarPerfilPersisteNomeTelefoneEEndereco() {
        Usuario usuario = usuarioDAO.buscarPorCpf(CPF);
        usuario.setNome("Nome Atualizado");
        usuario.setTelefone("11988887777");
        usuario.setEndereco("Rua Nova, 123");
        usuario.setFotoPerfil("file:/foto-perfil.png");

        usuarioDAO.atualizarPerfil(usuario);

        Usuario perfilAtualizado = usuarioDAO.buscarPorCpf(CPF);
        assertEquals("Nome Atualizado", perfilAtualizado.getNome());
        assertEquals("11988887777", perfilAtualizado.getTelefone());
        assertEquals("Rua Nova, 123", perfilAtualizado.getEndereco());
        assertEquals("file:/foto-perfil.png", perfilAtualizado.getFotoPerfil());
        assertTrue(perfilAtualizado.verificarSenha("senha123"));
    }
}
