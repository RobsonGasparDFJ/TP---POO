package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public Usuario buscarPorCpf(String cpf) {
        String sql = "SELECT * FROM usuarios WHERE cpf = ?";

        try (Connection conn = DatabaseSetup.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                        rs.getLong("id"),
                        rs.getString("nome"),
                        rs.getString("cpf"),
                        rs.getString("telefone"),
                        rs.getString("endereco"),
                        rs.getString("senha_hash"),
                        rs.getString("salt")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por CPF: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean autenticar(String cpf, String senhaDigitada) {
        Usuario usuario = buscarPorCpf(cpf);
        
        if (usuario == null) {
            return false; // Usuário não encontrado
        }

        // Utiliza o método verificarSenha da classe Usuario (que aplica o Salt + Hash SHA-256)
        return usuario.verificarSenha(senhaDigitada);
    }
}