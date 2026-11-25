package app.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import app.Aposta;
import app.CacaNiquel.Simbolo;
import app.CaraOuCoroa;
import app.Roleta;
import app.TipoJogo;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Área de animação do cassino. Só desenha: o resultado já chega sorteado e gravado
 * pelo CassinoService, e aqui ele é apenas revelado.
 */
public class PalcoCassino extends StackPane {

    private static final Color DOURADO = Color.web("#E8B84A");
    private static final Color DOURADO_ESCURO = Color.web("#B4852A");
    private static final Color ESCURO = Color.web("#2A2825");
    private static final Color VERMELHO = Color.web("#C8402F");

    private static final double RAIO_ROLETA = 84;

    /** Ordem dos números na roda europeia, no sentido horário. */
    private static final int[] RODA = {
        0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10,
        5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26
    };

    /** Usado só nos quadros intermediários; não influencia o resultado da aposta. */
    private final Random quadros = new Random();

    public PalcoCassino() {
        setMinHeight(190);
        setPrefHeight(190);
    }

    /** Mostra o jogo parado, antes da primeira rodada. */
    public void preparar(TipoJogo jogo) {
        switch (jogo) {
            case CARA_COROA -> getChildren().setAll(moeda(true));
            case DADO -> getChildren().setAll(dado(5));
            case CACA_NIQUEL -> getChildren().setAll(
                    rolos(List.of(rolo(Simbolo.SETE), rolo(Simbolo.SETE), rolo(Simbolo.SETE))));
            case ROLETA -> getChildren().setAll(mesaRoleta(roda()));
        }
    }

    /** Anima a rodada e chama {@code aoTerminar} quando o resultado já está na tela. */
    public void animar(Aposta aposta, Runnable aoTerminar) {
        Animation animacao = switch (aposta.getJogo()) {
            case CARA_COROA -> animarMoeda(aposta.getDescricao().endsWith(CaraOuCoroa.CARA));
            case DADO -> animarDado(Integer.parseInt(
                    aposta.getDescricao().substring(aposta.getDescricao().length() - 1)));
            case CACA_NIQUEL -> animarRolos(lerSimbolos(aposta.getDescricao()));
            case ROLETA -> animarRoleta(Integer.parseInt(aposta.getDescricao().split(" ")[1]));
        };

        SequentialTransition rodada = new SequentialTransition(animacao);
        if (aposta.isGanhou()) {
            Node desenho = getChildren().get(0); // só o desenho pulsa, não o confete
            PauseTransition comemoracao = new PauseTransition(Duration.millis(1));
            comemoracao.setOnFinished(e -> {
                destacar(aposta);
                soltarConfete();
            });
            rodada.getChildren().add(comemoracao);

            ScaleTransition pulo = new ScaleTransition(Duration.millis(180), desenho);
            pulo.setToX(1.08);
            pulo.setToY(1.08);
            pulo.setAutoReverse(true);
            pulo.setCycleCount(4);
            rodada.getChildren().add(pulo);
        }
        rodada.getChildren().add(new PauseTransition(Duration.millis(150)));
        rodada.setOnFinished(e -> aoTerminar.run());
        rodada.play();
    }

    // ---------------------------------------------------------------- vitória

    /** Brilho dourado no que premiou; no caça-níquel, só nos rolos que se repetiram. */
    private void destacar(Aposta aposta) {
        Node desenho = getChildren().get(0);
        if (aposta.getJogo() != TipoJogo.CACA_NIQUEL) {
            desenho.setEffect(brilho());
            return;
        }
        Simbolo[] sorteados = lerSimbolos(aposta.getDescricao());
        HBox caixa = (HBox) desenho;
        for (int i = 0; i < sorteados.length; i++) {
            int iguais = 0;
            for (Simbolo outro : sorteados) {
                if (outro == sorteados[i]) {
                    iguais++;
                }
            }
            if (iguais > 1) {
                StackPane rolo = (StackPane) caixa.getChildren().get(i);
                Rectangle fundo = (Rectangle) rolo.getChildren().get(0);
                fundo.setStroke(DOURADO);
                fundo.setStrokeWidth(4);
                rolo.setEffect(brilho());
            }
        }
    }

    private static DropShadow brilho() {
        DropShadow brilho = new DropShadow(28, DOURADO);
        brilho.setSpread(0.35);
        return brilho;
    }

    /** Papéis coloridos caindo sobre o palco; somem sozinhos. */
    private void soltarConfete() {
        Color[] cores = {DOURADO, VERMELHO, Color.web("#4A6FD0"), Color.web("#6FD0A4"), Color.WHITE};
        Pane camada = new Pane();
        camada.setMouseTransparent(true);
        camada.setManaged(false); // não interfere no layout
        double largura = getWidth();
        double altura = getHeight();
        getChildren().add(camada);

        for (int i = 0; i < 90; i++) {
            Rectangle papel = new Rectangle(5, 9, cores[quadros.nextInt(cores.length)]);
            papel.setX(quadros.nextDouble() * largura);
            papel.setY(-12);
            papel.setVisible(false);
            camada.getChildren().add(papel);

            Duration duracao = Duration.millis(900 + quadros.nextInt(900));
            TranslateTransition queda = new TranslateTransition(duracao, papel);
            queda.setByY(altura + 10);
            queda.setByX(quadros.nextInt(81) - 40);
            RotateTransition giro = new RotateTransition(duracao, papel);
            giro.setByAngle(quadros.nextInt(1081) - 540);

            ParallelTransition voo = new ParallelTransition(queda, giro);
            PauseTransition atraso = new PauseTransition(Duration.millis(quadros.nextInt(500)));
            atraso.setOnFinished(e -> {
                papel.setVisible(true);
                voo.play();
            });
            voo.setOnFinished(e -> camada.getChildren().remove(papel));
            atraso.play();
        }
    }

    // ---------------------------------------------------------------- roleta

    /** A roda com as 37 casas; o zero começa no topo. */
    private Group roda() {
        Group roda = new Group(new Circle(RAIO_ROLETA + 5, DOURADO_ESCURO));
        double passo = 360.0 / RODA.length;

        for (int i = 0; i < RODA.length; i++) {
            // o Arc mede ângulos no sentido anti-horário a partir das 3 horas
            Arc casa = new Arc(0, 0, RAIO_ROLETA, RAIO_ROLETA, 90 - i * passo - passo / 2, passo);
            casa.setType(ArcType.ROUND);
            casa.setFill(corDaCasa(RODA[i]));

            Text numero = texto(String.valueOf(RODA[i]), 8, Color.WHITE);
            double angulo = Math.toRadians(i * passo);
            double raio = RAIO_ROLETA - 10;
            numero.setX(raio * Math.sin(angulo) - numero.getLayoutBounds().getWidth() / 2);
            numero.setY(-raio * Math.cos(angulo) + 3);
            numero.setRotate(i * passo);

            roda.getChildren().addAll(casa, numero);
        }
        roda.getChildren().add(new Circle(RAIO_ROLETA - 22, Color.web("#3A2E1C")));
        return roda;
    }

    /** Roda, número central e o ponteiro fixo no topo. */
    private StackPane mesaRoleta(Group roda) {
        Polygon ponteiro = new Polygon(-8, 0, 8, 0, 0, 16);
        ponteiro.setFill(Color.WHITE);
        ponteiro.setTranslateY(-RAIO_ROLETA - 1);
        StackPane mesa = new StackPane(roda, ponteiro);
        mesa.setMaxSize(2 * RAIO_ROLETA + 10, 2 * RAIO_ROLETA + 10);
        return mesa;
    }

    private Animation animarRoleta(int sorteado) {
        Group roda = roda();
        StackPane mesa = mesaRoleta(roda);
        getChildren().setAll(mesa);

        int posicao = 0;
        while (RODA[posicao] != sorteado) {
            posicao++;
        }
        RotateTransition giro = new RotateTransition(Duration.seconds(3.2), roda);
        giro.setFromAngle(0);
        // cinco voltas e para com a casa sorteada embaixo do ponteiro
        giro.setToAngle(5 * 360 - posicao * 360.0 / RODA.length);
        giro.setInterpolator(Interpolator.SPLINE(0.15, 0.6, 0.25, 1));
        giro.setOnFinished(e -> mesa.getChildren().add(texto(String.valueOf(sorteado), 30, Color.WHITE)));
        return giro;
    }

    private static Color corDaCasa(int numero) {
        return switch (Roleta.cor(numero)) {
            case Roleta.VERMELHO -> VERMELHO;
            case Roleta.PRETO -> Color.web("#111111");
            default -> Color.web("#398364");
        };
    }

    // ---------------------------------------------------------------- moeda

    private Node moeda(boolean cara) {
        Circle borda = new Circle(52, DOURADO_ESCURO);
        Circle centro = new Circle(44, DOURADO);
        Text face = texto(cara ? "CARA" : "COROA", cara ? 22 : 18, Color.web("#5C4210"));
        return new StackPane(borda, centro, face);
    }

    private Animation animarMoeda(boolean caiuCara) {
        StackPane suporte = new StackPane(moeda(true));
        getChildren().setAll(suporte);

        int meiasVoltas = 11;
        SequentialTransition giros = new SequentialTransition();
        for (int i = 0; i < meiasVoltas; i++) {
            // a última face é sempre a sorteada; as anteriores se alternam
            boolean face = ((meiasVoltas - 1 - i) % 2 == 0) == caiuCara;
            double duracao = 55 + i * 8; // vai desacelerando

            ScaleTransition fecha = new ScaleTransition(Duration.millis(duracao), suporte);
            fecha.setToX(0.05);
            fecha.setOnFinished(e -> suporte.getChildren().setAll(moeda(face)));
            ScaleTransition abre = new ScaleTransition(Duration.millis(duracao), suporte);
            abre.setToX(1);
            giros.getChildren().addAll(fecha, abre);
        }

        TranslateTransition salto = new TranslateTransition(giros.getTotalDuration().divide(2), suporte);
        salto.setToY(-22);
        salto.setAutoReverse(true);
        salto.setCycleCount(2);
        salto.setInterpolator(Interpolator.EASE_OUT);

        return new ParallelTransition(giros, salto);
    }

    // ---------------------------------------------------------------- dado

    private Node dado(int numero) {
        Rectangle corpo = new Rectangle(100, 100, Color.WHITE);
        corpo.setArcWidth(24);
        corpo.setArcHeight(24);
        Group grupo = new Group(corpo);

        // posições dos pontos numa grade 3x3 (coluna, linha)
        int[][] pontos = switch (numero) {
            case 1 -> new int[][] {{1, 1}};
            case 2 -> new int[][] {{0, 0}, {2, 2}};
            case 3 -> new int[][] {{0, 0}, {1, 1}, {2, 2}};
            case 4 -> new int[][] {{0, 0}, {2, 0}, {0, 2}, {2, 2}};
            case 5 -> new int[][] {{0, 0}, {2, 0}, {1, 1}, {0, 2}, {2, 2}};
            default -> new int[][] {{0, 0}, {2, 0}, {0, 1}, {2, 1}, {0, 2}, {2, 2}};
        };
        for (int[] p : pontos) {
            grupo.getChildren().add(new Circle(24 + p[0] * 26, 24 + p[1] * 26, 9, ESCURO));
        }
        return grupo;
    }

    private Animation animarDado(int sorteado) {
        StackPane suporte = new StackPane(dado(1 + quadros.nextInt(6)));
        suporte.setMaxSize(100, 100);
        getChildren().setAll(suporte);

        Timeline faces = new Timeline();
        double instante = 0;
        for (int i = 0; i < 14; i++) {
            instante += 50 + i * 9;
            int numero = (i == 13) ? sorteado : 1 + quadros.nextInt(6);
            faces.getKeyFrames().add(new KeyFrame(Duration.millis(instante),
                    e -> suporte.getChildren().setAll(dado(numero))));
        }

        RotateTransition giro = new RotateTransition(Duration.millis(instante), suporte);
        giro.setFromAngle(0);
        giro.setToAngle(720);
        giro.setInterpolator(Interpolator.EASE_OUT);

        return new ParallelTransition(faces, giro);
    }

    // ---------------------------------------------------------------- caça-níquel

    private StackPane rolo(Simbolo simbolo) {
        Rectangle fundo = new Rectangle(84, 100, Color.WHITE);
        fundo.setArcWidth(14);
        fundo.setArcHeight(14);
        StackPane rolo = new StackPane(fundo, desenhar(simbolo));
        rolo.setMaxSize(84, 100);
        return rolo;
    }

    private HBox rolos(List<StackPane> lista) {
        HBox caixa = new HBox(10);
        caixa.getChildren().addAll(lista);
        caixa.setAlignment(Pos.CENTER);
        return caixa;
    }

    private Animation animarRolos(Simbolo[] sorteados) {
        List<StackPane> lista = new ArrayList<>();
        ParallelTransition todos = new ParallelTransition();

        for (int r = 0; r < sorteados.length; r++) {
            Simbolo finalDoRolo = sorteados[r];
            StackPane rolo = rolo(Simbolo.values()[quadros.nextInt(Simbolo.values().length)]);
            lista.add(rolo);

            Timeline giro = new Timeline();
            int passos = 12 + r * 6; // cada rolo para um pouco depois do anterior
            for (int i = 1; i <= passos; i++) {
                boolean ultimo = i == passos;
                Simbolo s = ultimo ? finalDoRolo : Simbolo.values()[(i + r * 2) % Simbolo.values().length];
                giro.getKeyFrames().add(new KeyFrame(Duration.millis(i * 80.0), e -> {
                    Node desenho = desenhar(s);
                    desenho.setTranslateY(-24);
                    rolo.getChildren().set(1, desenho);
                    new Timeline(new KeyFrame(Duration.millis(ultimo ? 160 : 70),
                            new KeyValue(desenho.translateYProperty(), 0, Interpolator.EASE_OUT))).play();
                }));
            }
            giro.getKeyFrames().add(new KeyFrame(Duration.millis(passos * 80.0 + 180)));
            todos.getChildren().add(giro);
        }

        getChildren().setAll(rolos(lista));
        return todos;
    }

    private static Simbolo[] lerSimbolos(String descricao) {
        String[] partes = descricao.split(" \\| ");
        Simbolo[] simbolos = new Simbolo[partes.length];
        for (int i = 0; i < partes.length; i++) {
            for (Simbolo s : Simbolo.values()) {
                if (s.getRotulo().equals(partes[i])) {
                    simbolos[i] = s;
                }
            }
        }
        return simbolos;
    }

    private Node desenhar(Simbolo simbolo) {
        Group desenho = switch (simbolo) {
            case CEREJA -> {
                Line caboA = new Line(14, 36, 30, 6);
                Line caboB = new Line(38, 40, 30, 6);
                caboA.setStroke(Color.web("#398364"));
                caboB.setStroke(Color.web("#398364"));
                caboA.setStrokeWidth(3);
                caboB.setStrokeWidth(3);
                yield new Group(caboA, caboB, new Circle(14, 40, 11, VERMELHO), new Circle(38, 44, 11, VERMELHO));
            }
            case LIMAO -> {
                Ellipse corpo = new Ellipse(26, 26, 24, 17);
                corpo.setFill(Color.web("#EBCB2D"));
                yield new Group(corpo,
                        new Circle(3, 26, 4, Color.web("#EBCB2D")),
                        new Circle(49, 26, 4, Color.web("#EBCB2D")));
            }
            case SINO -> new Group(
                    new Polygon(18, 12, 34, 12, 40, 38, 48, 44, 4, 44, 12, 38),
                    new Circle(26, 12, 8, DOURADO_ESCURO),
                    new Circle(26, 49, 5, DOURADO_ESCURO));
            case ESTRELA -> new Group(new Polygon(
                    26, 0, 33, 18, 52, 19, 37, 31, 42, 50, 26, 39, 10, 50, 15, 31, 0, 19, 19, 18));
            case SETE -> new Group(texto("7", 56, VERMELHO));
        };
        if (simbolo == Simbolo.SINO) {
            ((Polygon) desenho.getChildren().get(0)).setFill(DOURADO);
        }
        if (simbolo == Simbolo.ESTRELA) {
            ((Polygon) desenho.getChildren().get(0)).setFill(Color.web("#4A6FD0"));
        }
        return desenho;
    }

    private static Text texto(String conteudo, double tamanho, Color cor) {
        Text texto = new Text(conteudo);
        texto.setFont(Font.font("System", FontWeight.BOLD, tamanho));
        texto.setFill(cor);
        return texto;
    }
}
