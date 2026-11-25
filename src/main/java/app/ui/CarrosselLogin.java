package app.ui;

import java.net.URL;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;


public class CarrosselLogin {

    
    private record Slide(String titulo, String texto, String imagem, String reserva) {}

    private static final List<Slide> SLIDES = List.of(
        new Slide("Seu dinheiro, sem complicação.",
                  "Conta, extrato e transferências num só lugar.",
                  "slide1.jpg",
                  "linear-gradient(to bottom right, #4A3AFF, #14104F)"),
        new Slide("Transfira em segundos.",
                  "Envie dinheiro para outras contas com poucos cliques.",
                  "slide2.jpg",
                  "linear-gradient(to bottom right, #2F6BFF, #121A5C)"),
        new Slide("Entenda pra onde vai.",
                  "Extrato e gráficos para acompanhar cada entrada e saída.",
                  "slide3.jpg",
                  "linear-gradient(to bottom right, #7A3CFF, #241270)"),
        new Slide("Seus dados, protegidos.",
                  "Sua conta é só sua. Abra e comece agora.",
                  "slide4.jpg",
                  "linear-gradient(to bottom right, #3A4BFF, #0F0D3A)")
    );

    private static final int SEGUNDOS_POR_SLIDE = 6;

    private final Region fundoA;
    private final Region fundoB;
    private final VBox blocoTexto;
    private final Label slideTitulo;
    private final Label slideTexto;
    private final HBox pontos;
    private final Timeline relogio;

    private int atual = 0;
    private boolean bVisivel = false;
    private boolean emTransicao = false;

    public CarrosselLogin(Region fundoA, Region fundoB, VBox blocoTexto,
                          Label slideTitulo, Label slideTexto, HBox pontos) {
        this.fundoA = fundoA;
        this.fundoB = fundoB;
        this.blocoTexto = blocoTexto;
        this.slideTitulo = slideTitulo;
        this.slideTexto = slideTexto;
        this.pontos = pontos;

        relogio = new Timeline(new KeyFrame(Duration.seconds(SEGUNDOS_POR_SLIDE),
                e -> ir((atual + 1) % SLIDES.size())));
        relogio.setCycleCount(Animation.INDEFINITE);
    }

    public void iniciar() {
        Slide primeiro = SLIDES.get(0);
        fundoA.setStyle(estilo(primeiro));
        fundoA.setOpacity(1);
        fundoB.setOpacity(0);
        aplicarTexto(primeiro);
        montarPontos();
        marcarPonto(0);

        // quando o login sai da tela (setRoot do dashboard), para o relógio
        fundoA.sceneProperty().addListener((obs, antiga, nova) -> {
            if (nova == null) {
                parar();
            }
        });
        relogio.play();
    }

    public void parar() {
        relogio.stop();
    }

    // ------------------------------------------------------------------

    private void ir(int indice) {
        if (indice == atual || emTransicao) {
            return;
        }
        emTransicao = true;
        atual = indice;
        Slide slide = SLIDES.get(indice);

        // A fica sempre por baixo e opaca. B fica por cima e aparece/some.
        FadeTransition fade = new FadeTransition(Duration.millis(900), fundoB);
        if (!bVisivel) {
            fundoB.setStyle(estilo(slide));
            fade.setFromValue(0);
            fade.setToValue(1);
        } else {
            fundoA.setStyle(estilo(slide));
            fade.setFromValue(1);
            fade.setToValue(0);
        }
        bVisivel = !bVisivel;
        fade.setOnFinished(e -> emTransicao = false);
        fade.play();

        trocarTexto(slide);
        marcarPonto(indice);
    }

    private void trocarTexto(Slide slide) {
        FadeTransition sai = new FadeTransition(Duration.millis(250), blocoTexto);
        sai.setToValue(0);
        sai.setOnFinished(e -> {
            aplicarTexto(slide);
            FadeTransition entra = new FadeTransition(Duration.millis(450), blocoTexto);
            entra.setToValue(1);
            entra.play();
        });
        sai.play();
    }

    private void aplicarTexto(Slide slide) {
        slideTitulo.setText(slide.titulo());
        slideTexto.setText(slide.texto());
    }

    private String estilo(Slide slide) {
        URL url = CarrosselLogin.class.getResource("/app/ui/img/" + slide.imagem());
        String base = "-fx-background-color: " + slide.reserva() + ";";
        if (url == null) {
            return base;
        }
        return base
                + " -fx-background-image: url('" + url.toExternalForm() + "');"
                + " -fx-background-size: cover;"
                + " -fx-background-position: center center;"
                + " -fx-background-repeat: no-repeat;";
    }

    private void montarPontos() {
        pontos.getChildren().clear();
        for (int i = 0; i < SLIDES.size(); i++) {
            final int indice = i;
            Region ponto = new Region();
            ponto.getStyleClass().add("login-ponto");
            ponto.setOnMouseClicked(e -> {
                ir(indice);
                relogio.playFromStart();
            });
            pontos.getChildren().add(ponto);
        }
    }

    private void marcarPonto(int ativo) {
        for (int i = 0; i < pontos.getChildren().size(); i++) {
            var estilos = pontos.getChildren().get(i).getStyleClass();
            estilos.remove("login-ponto-ativo");
            if (i == ativo) {
                estilos.add("login-ponto-ativo");
            }
        }
    }
}