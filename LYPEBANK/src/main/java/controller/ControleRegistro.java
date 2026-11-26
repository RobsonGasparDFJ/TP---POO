package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import model.Conta;
import repository.ContaDAO;
import repository.DatabaseSetup;
import model.TipoConta;
import model.Usuario;
import repository.UsuarioDAO;
import util.Mascara;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

public class ControleRegistro {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ContaDAO contaDAO = new ContaDAO();

    @FXML
    private Button BotaoCriarConta;

    @FXML
    private ComboBox<TipoConta> SelecionarTipoConta;

    @FXML
    private Button botaoVoltar;

    @FXML
    private TextField campoCPFR;

    @FXML
    private TextField campoChavePix;

    @FXML
    private TextField campoEndereco;

    @FXML
    private TextField campoNome;

    @FXML
    private PasswordField campoSenhaR;

    @FXML
    private TextField campoTelefone;

    @FXML
    public void initialize() {
        Mascara.cpf(campoCPFR);
        Mascara.telefone(campoTelefone);

        SelecionarTipoConta.getItems().setAll(TipoConta.values());
        SelecionarTipoConta.setConverter(new StringConverter<TipoConta>() {
            @Override
            public String toString(TipoConta tipo) {
                if (tipo == null) {
                    return "";
                }
                return tipo == TipoConta.CORRENTE ? "Corrente" : "Poupança";
            }

            @Override
            public TipoConta fromString(String texto) {
                return null; // o combo não é editável
            }
        });
    }

    @FXML
    void criarConta(MouseEvent event) {
        try {
            String nome = campoNome.getText() == null ? "" : campoNome.getText().trim();
            String cpf = campoCPFR.getText() == null ? "" : campoCPFR.getText().replaceAll("\\D", "");
            String telefone = campoTelefone.getText() == null ? "" : campoTelefone.getText().replaceAll("\\D", "");
            String endereco = campoEndereco.getText() == null ? "" : campoEndereco.getText().trim();
            String senha = campoSenhaR.getText();
            String chave = campoChavePix.getText() == null ? "" : campoChavePix.getText().trim();
            TipoConta tipo = SelecionarTipoConta.getValue();

            // as próprias classes de domínio validam nome, CPF (11 dígitos), senha (mín. 6) e chave
            Usuario usuario = new Usuario(nome, cpf, telefone, endereco, senha);
            if (!telefone.isEmpty() && telefone.length() != 10 && telefone.length() != 11) {
                throw new IllegalArgumentException("O telefone deve ter DDD + 8 ou 9 dígitos.");
            }
            Conta conta = new Conta(usuario, tipo, chave);

            if (tipo == null) {
                throw new IllegalArgumentException("Selecione o tipo de conta.");
            }
            if (usuarioDAO.buscarPorCpf(cpf) != null) {
                mostrar(Alert.AlertType.WARNING, "Já existe um usuário cadastrado com esse CPF.");
                return;
            }

            // usuário + conta gravados juntos: ou salva os dois, ou nenhum
            try (Connection conn = DatabaseSetup.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    usuarioDAO.salvar(conn, usuario);
                    contaDAO.inserir(conn, conta);
                    conn.commit();
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
            }

            mostrar(Alert.AlertType.INFORMATION,
                    "Conta criada com sucesso!\nNúmero da sua conta: " + conta.getIdConta());
            irParaLogin();

        } catch (IllegalArgumentException e) {
            mostrar(Alert.AlertType.WARNING, e.getMessage());
        } catch (SQLException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("TB_Conta.chave")) {
                mostrar(Alert.AlertType.WARNING, "Essa chave Pix já está em uso. Tente outra.");
            } else if (msg.contains("TB_Usuario.cpf")) {
                mostrar(Alert.AlertType.WARNING, "Já existe um usuário cadastrado com esse CPF.");
            } else {
                mostrar(Alert.AlertType.ERROR, "Erro ao cadastrar: " + msg);
            }
        } catch (RuntimeException e) {
            mostrar(Alert.AlertType.ERROR, "Erro: " + e.getMessage());
        }
    }

    @FXML
    void voltarAba(MouseEvent event) {
        irParaLogin();
    }

    private void irParaLogin() {
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            botaoVoltar.getScene().setRoot(login);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Não foi possível voltar: " + e.getMessage()).show();
        }
    }

    private void mostrar(Alert.AlertType tipo, String mensagem) {
        Alert alerta = new Alert(tipo, mensagem);
        alerta.setHeaderText(null);
        alerta.initOwner(botaoVoltar.getScene().getWindow());
        alerta.showAndWait();
    }
}