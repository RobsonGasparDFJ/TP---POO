package app.ui;

import app.Usuario;

/**
 * Guarda o usuário que fez login, para as próximas telas (dashboard, extrato, etc.)
 * poderem consultar quem está logado.
 */
public final class Sessao {

    private static Usuario usuarioLogado;

    private Sessao() {
    }

    public static void entrar(Usuario usuario) {
        usuarioLogado = usuario;
    }

    public static void sair() {
        usuarioLogado = null;
    }

    public static Usuario getUsuarioLogado() {
        return usuarioLogado;
    }
}
