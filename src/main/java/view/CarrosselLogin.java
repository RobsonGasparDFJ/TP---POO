package view;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public class CarrosselLogin {

    private record Slide(
        String titulo,
        String texto,
        String textoBotao,
        String iconeBotao,
        String tagImagem,
        String imagem,
        String reserva
    ) {}

    // Ordem sincronizada com os 4 cards inferiores:
    // 0: Transferências | 1: Depósitos | 2: Cassino (slide inicial!) | 3: Desafios
    private static final List<Slide> SLIDES = List.of(
        new Slide("Pix na velocidade da luz!",
                  "Envie Pix e transferências entre contas em segundos, 24 horas por dia, sem taxas.",
                  "FAZER PIX AGORA",
                  "fas-bolt",
                  "PIX INSTANTÂNEO / ZERO TAXAS",
                  "pix.png",
                  "linear-gradient(to bottom right, #0D573B, #073B27, #042418)"),
        new Slide("Sua Poupança rendendo de verdade!",
                  "Guarde dinheiro na sua conta poupança e veja seu saldo multiplicar todo mês.",
                  "ABRIR MINHA CONTA",
                  "fas-piggy-bank",
                  "RENDIMENTO DIÁRIO / CONTA LYPE",
                  "poupanca.png",
                  "linear-gradient(to bottom right, #09472F, #073B27, #031F14)"),
        new Slide("Multiplique seu saldo no Cassino Lype!",
                  "O único banco digital com Foguetinho, Roleta e muito mais. Jogue direto com sua conta!",
                  "CRIAR CONTA & JOGAR",
                  "fas-gamepad",
                  "CRASH GAME / JOGUE AGORA!",
                  "foguete.png",
                  "linear-gradient(to bottom right, #0A4D33, #073B27, #042418)"),
        new Slide("Cumpra desafios e controle seus gastos!",
                  "Acompanhe gráficos em tempo real, cumpra missões e ganhe cashback direto na conta.",
                  "CONHECER O PAINEL",
                  "fas-trophy",
                  "GRÁFICOS & CASHBACK / AO VIVO",
                  "graficos.png",
                  "linear-gradient(to bottom right, #0B5237, #073B27, #042418)")
    );

    private static final int SEGUNDOS_POR_SLIDE = 5;

    private final Region fundoA;
    private final Region fundoB;
    private final VBox blocoTexto;
    private final Label slideTitulo;
    private final Label slideTexto;
    private final Button btnCarrosselCta;
    private final FontIcon iconCarrosselCta;
    private final ImageView slideImagem;
    private final Label lblTagFlutuante;
    private final HBox pontos;
    private final List<VBox> cardsInferiores;
    private final Timeline relogio;

    // Começa no índice 2 (Cassino / Foguetinho)
    private int atual = 2;
    private boolean bVisivel = false;
    private boolean emTransicao = false;

    public CarrosselLogin(Region fundoA, Region fundoB, VBox blocoTexto, Label slideTitulo,
                          Label slideTexto, Button btnCarrosselCta, FontIcon iconCarrosselCta,
                          ImageView slideImagem, Label lblTagFlutuante,
                          HBox pontos, List<VBox> cardsInferiores) {
        this.fundoA = fundoA;
        this.fundoB = fundoB;
        this.blocoTexto = blocoTexto;
        this.slideTitulo = slideTitulo;
        this.slideTexto = slideTexto;
        this.btnCarrosselCta = btnCarrosselCta;
        this.iconCarrosselCta = iconCarrosselCta;
        this.slideImagem = slideImagem;
        this.lblTagFlutuante = lblTagFlutuante;
        this.pontos = pontos;
        this.cardsInferiores = cardsInferiores;

        relogio = new Timeline(new KeyFrame(Duration.seconds(SEGUNDOS_POR_SLIDE),
                e -> ir((atual + 1) % SLIDES.size())));
        relogio.setCycleCount(Animation.INDEFINITE);
    }

    public void iniciar() {
        Slide inicial = SLIDES.get(atual);
        fundoA.setStyle(estilo(inicial));
        fundoA.setOpacity(1);
        fundoB.setOpacity(0);
        aplicarConteudo(inicial);
        montarPontos();
        configurarCliqueCards();
        marcarAtivo(atual);
        iniciarFlutuacaoImagem();

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

    private void iniciarFlutuacaoImagem() {
        if (slideImagem == null) return;
        TranslateTransition flutuar = new TranslateTransition(Duration.seconds(2.3), slideImagem);
        flutuar.setByY(-12);
        flutuar.setCycleCount(Animation.INDEFINITE);
        flutuar.setAutoReverse(true);
        flutuar.setInterpolator(Interpolator.EASE_BOTH);
        flutuar.play();
    }

    private void ir(int indice) {
        if (indice == atual || emTransicao) {
            return;
        }
        emTransicao = true;
        atual = indice;
        Slide slide = SLIDES.get(indice);

        FadeTransition fade = new FadeTransition(Duration.millis(800), fundoB);
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

        trocarConteudo(slide);
        marcarAtivo(indice);
    }

    private void trocarConteudo(Slide slide) {
        FadeTransition saiTexto = new FadeTransition(Duration.millis(240), blocoTexto);
        saiTexto.setToValue(0);

        if (slideImagem != null) {
            FadeTransition saiImg = new FadeTransition(Duration.millis(240), slideImagem);
            saiImg.setToValue(0);
            saiImg.play();
        }
        if (lblTagFlutuante != null) {
            FadeTransition saiTag = new FadeTransition(Duration.millis(240), lblTagFlutuante);
            saiTag.setToValue(0);
            saiTag.play();
        }

        saiTexto.setOnFinished(e -> {
            aplicarConteudo(slide);

            FadeTransition entraTexto = new FadeTransition(Duration.millis(420), blocoTexto);
            entraTexto.setToValue(1);
            entraTexto.play();

            if (slideImagem != null) {
                FadeTransition entraImg = new FadeTransition(Duration.millis(420), slideImagem);
                entraImg.setToValue(1);
                entraImg.play();
            }
            if (lblTagFlutuante != null) {
                FadeTransition entraTag = new FadeTransition(Duration.millis(420), lblTagFlutuante);
                entraTag.setToValue(1);
                entraTag.play();
            }
        });
        saiTexto.play();
    }

    private void aplicarConteudo(Slide slide) {
        slideTitulo.setText(slide.titulo());
        slideTexto.setText(slide.texto());
        if (btnCarrosselCta != null) {
            btnCarrosselCta.setText(slide.textoBotao());
        }
        if (iconCarrosselCta != null) {
            iconCarrosselCta.setIconLiteral(slide.iconeBotao());
        }
        if (lblTagFlutuante != null) {
            lblTagFlutuante.setText(slide.tagImagem());
        }
        if (slideImagem != null) {
            URL url = CarrosselLogin.class.getResource("/view/img/" + slide.imagem());
            if (url == null) {
                url = CarrosselLogin.class.getResource("/view/" + slide.imagem());
            }
            if (url != null) {
                Image original = new Image(url.toExternalForm());
                slideImagem.setImage(removerFundoQuadrado(original));
            } else {
                slideImagem.setImage(null);
            }
        }
    }

    private String estilo(Slide slide) {
        return "-fx-background-color: " + slide.reserva() + ";";
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

    private void configurarCliqueCards() {
        if (cardsInferiores == null) return;
        for (int i = 0; i < cardsInferiores.size(); i++) {
            final int indice = i;
            cardsInferiores.get(i).setOnMouseClicked(e -> {
                ir(indice);
                relogio.playFromStart();
            });
        }
    }

    private void marcarAtivo(int ativo) {
        for (int i = 0; i < pontos.getChildren().size(); i++) {
            var estilos = pontos.getChildren().get(i).getStyleClass();
            estilos.remove("login-ponto-ativo");
            if (i == ativo) {
                estilos.add("login-ponto-ativo");
            }
        }

        if (cardsInferiores != null) {
            for (int i = 0; i < cardsInferiores.size(); i++) {
                var estilosCard = cardsInferiores.get(i).getStyleClass();
                estilosCard.remove("login-mini-card-ativo");
                if (i == ativo) {
                    estilosCard.add("login-mini-card-ativo");
                }
            }
        }
    }

    public static Image removerFundoQuadrado(Image img) {
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        PixelReader reader = img.getPixelReader();
        WritableImage saida = new WritableImage(w, h);
        PixelWriter writer = saida.getPixelWriter();

        Color fundo = reader.getColor(0, 0);
        boolean[][] visitado = new boolean[w][h];
        ArrayDeque<int[]> fila = new ArrayDeque<>();

        int[][] cantos = {{0, 0}, {w - 1, 0}, {0, h - 1}, {w - 1, h - 1}};
        for (int[] c : cantos) {
            visitado[c[0]][c[1]] = true;
            fila.add(c);
        }

        while (!fila.isEmpty()) {
            int[] p = fila.poll();
            int x = p[0], y = p[1];
            Color cor = reader.getColor(x, y);

            double dist = Math.sqrt(
                Math.pow(cor.getRed() - fundo.getRed(), 2) +
                Math.pow(cor.getGreen() - fundo.getGreen(), 2) +
                Math.pow(cor.getBlue() - fundo.getBlue(), 2)
            );

            if (dist < 0.14) {
                writer.setColor(x, y, Color.TRANSPARENT);
                int[][] vizinhos = {{x + 1, y}, {x - 1, y}, {x, y + 1}, {x, y - 1}};
                for (int[] v : vizinhos) {
                    if (v[0] >= 0 && v[0] < w && v[1] >= 0 && v[1] < h && !visitado[v[0]][v[1]]) {
                        visitado[v[0]][v[1]] = true;
                        fila.add(v);
                    }
                }
            } else {
                double alfa = Math.min(1.0, (dist - 0.14) / 0.08);
                writer.setColor(x, y, new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), cor.getOpacity() * alfa));
            }
        }

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!visitado[x][y]) {
                    writer.setColor(x, y, reader.getColor(x, y));
                }
            }
        }
        return saida;
    }
}