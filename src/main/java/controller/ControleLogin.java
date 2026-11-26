package controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Random;

import model.Usuario;
import repository.UsuarioDAO;
import util.*;
import view.CarrosselLogin;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public class ControleLogin {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML private Button BotaoEntrar;
    @FXML private Hyperlink HyperLinkCriarConta;
    @FXML private Button botaoFechar;
    @FXML private Button botaoMinimizar;
    @FXML private Button botaoMaximizar;
    @FXML private TextField campoCPF;
    @FXML private PasswordField campoSenha;

    @FXML private StackPane logoContainer;
    @FXML private SVGPath logoPecaSuperior;
    @FXML private SVGPath logoPecaInferior;

    @FXML private Region fundoA;
    @FXML private Region fundoB;
    @FXML private VBox blocoTexto;
    @FXML private Label slideTitulo;
    @FXML private Label slideTexto;
    @FXML private Button btnCarrosselCta;
    @FXML private FontIcon iconCarrosselCta;
    @FXML private ImageView slideImagem;
    @FXML private Label lblTagFlutuante;
    @FXML private ImageView imgPromoFoguete;
    @FXML private HBox pontos;
    @FXML private Circle pontoOnline;
    @FXML private Label lblJogadoresOnline;

    @FXML private VBox cardTransferencias;
    @FXML private VBox cardDepositos;
    @FXML private VBox cardCassino;
    @FXML private VBox cardDesafios;

    private Timeline timelineOnline;
    private int jogadoresAtuais = 1452;

    @FXML
    public void initialize() {
        Mascara.cpf(campoCPF);

        List<VBox> cards = List.of(cardTransferencias, cardDepositos, cardCassino, cardDesafios);
        new CarrosselLogin(
            fundoA, fundoB, blocoTexto, slideTitulo, slideTexto,
            btnCarrosselCta, iconCarrosselCta, slideImagem, lblTagFlutuante,
            pontos, cards
        ).iniciar();

        animarEntradaLogo();
        iniciarContadorJogadores();
        carregarFoguetePromo();
    }

    private void carregarFoguetePromo() {
        if (imgPromoFoguete == null) return;
        URL urlFoguete = getClass().getResource("/view/img/foguete.png");
        if (urlFoguete == null) {
            urlFoguete = getClass().getResource("/view/foguete.png");
        }
        if (urlFoguete != null) {
            Image img = new Image(urlFoguete.toExternalForm());
            imgPromoFoguete.setImage(CarrosselLogin.removerFundoQuadrado(img));
        }
    }

    private void animarEntradaLogo() {
        if (logoPecaSuperior == null || logoPecaInferior == null) return;

        logoPecaSuperior.setTranslateY(-18);
        logoPecaSuperior.setOpacity(0);
        logoPecaInferior.setTranslateX(18);
        logoPecaInferior.setOpacity(0);

        TranslateTransition desceSup = new TranslateTransition(Duration.millis(650), logoPecaSuperior);
        desceSup.setToY(0);
        desceSup.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeSup = new FadeTransition(Duration.millis(650), logoPecaSuperior);
        fadeSup.setToValue(1);

        TranslateTransition entraInf = new TranslateTransition(Duration.millis(650), logoPecaInferior);
        entraInf.setToX(0);
        entraInf.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeInf = new FadeTransition(Duration.millis(650), logoPecaInferior);
        fadeInf.setToValue(1);

        new ParallelTransition(desceSup, fadeSup, entraInf, fadeInf).play();
    }

    private void iniciarContadorJogadores() {
        if (pontoOnline == null || lblJogadoresOnline == null) return;

        FadeTransition pulsarPonto = new FadeTransition(Duration.seconds(1.1), pontoOnline);
        pulsarPonto.setFromValue(1.0);
        pulsarPonto.setToValue(0.25);
        pulsarPonto.setCycleCount(Animation.INDEFINITE);
        pulsarPonto.setAutoReverse(true);
        pulsarPonto.play();

        Random random = new Random();
        timelineOnline = new Timeline(new KeyFrame(Duration.seconds(3.5), e -> {
            int variacao = random.nextInt(11) - 4;
            jogadoresAtuais = Math.max(1200, jogadoresAtuais + variacao);
            lblJogadoresOnline.setText(String.format("%,d jogadores online agora", jogadoresAtuais).replace(',', '.'));
        }));
        timelineOnline.setCycleCount(Animation.INDEFINITE);
        timelineOnline.play();

        lblJogadoresOnline.sceneProperty().addListener((obs, antiga, nova) -> {
            if (nova == null && timelineOnline != null) {
                timelineOnline.stop();
            }
        });
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

            if (usuario == null || !usuario.verificarSenha(senha)) {
                mostrar(Alert.AlertType.ERROR, "CPF ou senha inválidos.");
                campoSenha.clear();
                return;
            }

            Sessao.entrar(usuario);
            campoSenha.clear();

            animarLoginEEntrar();

        } catch (RuntimeException e) {
            mostrar(Alert.AlertType.ERROR, "Erro ao entrar: " + e.getMessage());
        }
    }

    private void animarLoginEEntrar() {
        BotaoEntrar.setDisable(true);
        BotaoEntrar.setText("Entrando...");

        if (logoContainer != null) {
            RotateTransition girar = new RotateTransition(Duration.millis(750), logoContainer);
            girar.setByAngle(360);
            girar.setInterpolator(Interpolator.EASE_BOTH);

            ScaleTransition pulsar = new ScaleTransition(Duration.millis(375), logoContainer);
            pulsar.setToX(1.16);
            pulsar.setToY(1.16);
            pulsar.setCycleCount(2);
            pulsar.setAutoReverse(true);

            ParallelTransition animacao = new ParallelTransition(girar, pulsar);
            animacao.setOnFinished(e -> abrirDashboard());
            animacao.play();
        } else {
            abrirDashboard();
        }
    }

    private void abrirDashboard() {
        try {
            Parent dashboard = FXMLLoader.load(getClass().getResource("/view/dashboard.fxml"));
            BotaoEntrar.getScene().setRoot(dashboard);
        } catch (IOException e) {
            BotaoEntrar.setDisable(false);
            BotaoEntrar.setText("Entrar");
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            e.printStackTrace();
            mostrar(Alert.AlertType.ERROR, "Não foi possível abrir o dashboard: " + causa.getClass().getSimpleName()
                    + " - " + (causa.getMessage() == null ? "erro desconhecido" : causa.getMessage()));
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
            Parent registro = FXMLLoader.load(getClass().getResource("/view/registro.fxml"));
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

    @FXML
    void maximizarAba(MouseEvent event) {
        Stage stage = (Stage) botaoMaximizar.getScene().getWindow();
        boolean maximizada = JanelaUtil.alternarMaximizada(stage);
        ((FontIcon) botaoMaximizar.getGraphic())
                .setIconLiteral(maximizada ? "fas-window-restore" : "fas-window-maximize");
    }

    private void mostrar(Alert.AlertType tipo, String mensagem) {
        Alert alerta = new Alert(tipo, mensagem);
        alerta.setHeaderText(null);
        alerta.initOwner(BotaoEntrar.getScene().getWindow());
        alerta.showAndWait();
    }
}