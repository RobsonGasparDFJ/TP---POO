package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UsuarioDAO {

    public void salvar(Connection conn, Usuario usuario) throws SQLException {
        String sql = """
            INSERT INTO TB_Usuario (nome, cpf, telefone, endereco, senhaHash, salt)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getCpf());
            stmt.setString(3, usuario.getTelefone());
            stmt.setString(4, usuario.getEndereco());
            stmt.setString(5, usuario.getSenhaHash());
            stmt.setString(6, usuario.getSalt());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    usuario.setIdUsuario(keys.getLong(1));
                }
            }
        }
    }

    public void salvar(Usuario usuario) {
        try (Connection conn = DatabaseSetup.getConnection()) {
            salvar(conn, usuario);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar usuário: " + e.getMessage(), e);
        }
    }

    public Usuario buscarPorCpf(String cpf) {
        String sql = "SELECT * FROM TB_Usuario WHERE cpf = ?";

        try (Connection conn = DatabaseSetup.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por CPF: " + e.getMessage(), e);
        }
        return null;
    }

    // NOVO (editar perfil): busca pelo id, usado para recarregar o usuário do banco
    public Usuario buscarPorId(Long idUsuario) {
        String sql = "SELECT * FROM TB_Usuario WHERE PK_idUsuario = ?";

        try (Connection conn = DatabaseSetup.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por id: " + e.getMessage(), e);
        }
        return null;
    }

    // NOVO (editar perfil): atualiza os dados editáveis. O CPF nunca é alterado.
    public void atualizar(Usuario usuario) {
        if (usuario.getIdUsuario() == null) {
            throw new IllegalArgumentException("Salve o usuário no banco antes de atualizá-lo.");
        }

        String sql = """
            UPDATE TB_Usuario
            SET nome = ?, telefone = ?, endereco = ?, senhaHash = ?, salt = ?
            WHERE PK_idUsuario = ?
        """;

        try (Connection conn = DatabaseSetup.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getTelefone());
            stmt.setString(3, usuario.getEndereco());
            stmt.setString(4, usuario.getSenhaHash());
            stmt.setString(5, usuario.getSalt());
            stmt.setLong(6, usuario.getIdUsuario());

            if (stmt.executeUpdate() == 0) {
                throw new IllegalArgumentException("Usuário não encontrado para atualização.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar usuário: " + e.getMessage(), e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
            rs.getLong("PK_idUsuario"),
            rs.getString("nome"),
            rs.getString("cpf"),
            rs.getString("telefone"),
            rs.getString("endereco"),
            rs.getString("senhaHash"),
            rs.getString("salt")
        );
    }

    public boolean autenticar(String cpf, String senhaDigitada) {
        Usuario usuario = buscarPorCpf(cpf);

        if (usuario == null) {
            return false;
        }

        return usuario.verificarSenha(senhaDigitada);
    }
}
