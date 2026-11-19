package app.ui;

import java.io.IOException;

import app.Usuario;
import app.UsuarioDAO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ControleLogin {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    private Button BotaoEntrar;

    @FXML
    private Hyperlink HyperLinkCriarConta;

    @FXML
    private Button botaoFechar;

    @FXML
    private Button botaoMinimizar;

    @FXML
    private TextField campoCPF;

    @FXML
    private PasswordField campoSenha;

    @FXML
    public void initialize() {
        Mascara.cpf(campoCPF);
    }

    @FXML
    void entrar(ActionEvent event) {
        String cpf = campoCPF.getText() == null ? "" : campoCPF.getText().replaceAll("\\D", "");
        String senha = campoSenha.getText();

        if (cpf.isEmpty() || senha == null || senha.isEmpty()) {
            mostrar(Alert.AlertType.WARNING, "Informe o CPF e a senha.");
            return;
        }

        try {
            Usuario usuario = usuarioDAO.buscarPorCpf(cpf);

            // mesma mensagem para CPF inexistente e senha errada, para não revelar quais CPFs existem
            if (usuario == null || !usuario.verificarSenha(senha)) {
                mostrar(Alert.AlertType.ERROR, "CPF ou senha inválidos.");
                campoSenha.clear();
                return;
            }

            Sessao.entrar(usuario);
            campoSenha.clear();

            // TODO: trocar este aviso pela tela principal (dashboard) quando ela existir
            mostrar(Alert.AlertType.INFORMATION, "Bem-vindo(a), " + usuario.getNome() + "!");
        } catch (RuntimeException e) {
            mostrar(Alert.AlertType.ERROR, "Erro ao entrar: " + e.getMessage());
        }
    }

    @FXML
    void fecharAba(MouseEvent event) {
        Platform.exit();
    }

    @FXML
    void janela_registro(MouseEvent event) {
        try {
            BorderPane raiz = (BorderPane) HyperLinkCriarConta.getScene().getRoot();
            Parent registro = FXMLLoader.load(getClass().getResource("/app/ui/registro.fxml"));
            raiz.setLeft(registro);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Não foi possível abrir o registro: " + e.getMessage()).show();
        }
    }

    @FXML
    void minimizarAba(MouseEvent event) {
        Stage stage = (Stage) botaoMinimizar.getScene().getWindow();
        stage.setIconified(true);
    }

    private void mostrar(Alert.AlertType tipo, String mensagem) {
        Alert alerta = new Alert(tipo, mensagem);
        alerta.setHeaderText(null);
        alerta.initOwner(BotaoEntrar.getScene().getWindow());
        alerta.showAndWait();
    }
}