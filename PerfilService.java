package app;

public class PerfilService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Atualiza nome, telefone e endereço.
     * O usuário é recarregado do banco, alterado e só então salvo: se alguma
     * validação falhar, nada é gravado e o objeto que a tela usa não fica "meio editado".
     * O CPF é o identificador de login e não pode ser alterado.
     *
     * @return o usuário já atualizado
     */
    public Usuario atualizarDados(Long idUsuario, String nome, String telefone, String endereco) {
        Usuario usuario = carregar(idUsuario);
        usuario.atualizarDadosCadastrais(nome, telefone, endereco);
        usuarioDAO.atualizar(usuario);
        return usuario;
    }

    /**
     * Troca a senha. Exige a senha atual e grava um novo salt junto com o novo hash.
     *
     * @return o usuário já atualizado
     */
    public Usuario alterarSenha(Long idUsuario, String senhaAtual, String novaSenha) {
        Usuario usuario = carregar(idUsuario);
        usuario.alterarSenha(senhaAtual, novaSenha);
        usuarioDAO.atualizar(usuario);
        return usuario;
    }

    private Usuario carregar(Long idUsuario) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("O id do usuário é obrigatório.");
        }
        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        return usuario;
    }
}
