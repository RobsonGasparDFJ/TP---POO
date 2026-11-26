package view;

import java.util.Random;

import service.jogo.Foguete;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/** Desenho do voo: estrelas, rastro, foguete e o multiplicador. Não decide nada do jogo. */
public class VooFoguete extends Pane {

    private static final double LARGURA = 500;
    private static final double ALTURA = 180;

    private final Random aleatorio = new Random();
    private final Polyline rastro = new Polyline();
    private final Group foguete = criarFoguete();
    private final Group chama = (Group) foguete.getChildren().get(0);
    private final Text multiplicador = new Text();
    private final Text aviso = new Text();

    public VooFoguete() {
        setMinSize(LARGURA, ALTURA);
        setPrefSize(LARGURA, ALTURA);
        setMaxSize(LARGURA, ALTURA);

        for (int i = 0; i < 45; i++) {
            Circle estrela = new Circle(aleatorio.nextDouble() * LARGURA, aleatorio.nextDouble() * ALTURA,
                    0.6 + aleatorio.nextDouble(), Color.web("#FFFFFF", 0.45));
            getChildren().add(estrela);
        }

        rastro.setStroke(Color.web("#E8B84A"));
        rastro.setStrokeWidth(3);
        multiplicador.setFont(Font.font("System", FontWeight.BOLD, 40));
        aviso.setFont(Font.font("System", FontWeight.BOLD, 13));
        getChildren().addAll(rastro, foguete, multiplicador, aviso);

        atualizar(0, 1.0);
        chama.setVisible(false);
    }

    /** Posiciona o foguete conforme o tempo de voo e o multiplicador atual. */
    public void atualizar(double segundos, double valor) {
        // sobe numa curva que se aproxima do canto superior direito sem sair da tela
        double avanco = 1 - Math.exp(-segundos / 4.0);
        double x = 36 + avanco * (LARGURA - 90);
        double y = ALTURA - 24 - Math.pow(avanco, 1.8) * (ALTURA - 62);

        rastro.getPoints().clear();
        for (int i = 0; i <= 24; i++) {
            double p = avanco * i / 24;
            rastro.getPoints().addAll(36 + p * (LARGURA - 90), ALTURA - 24 - Math.pow(p, 1.8) * (ALTURA - 62));
        }

        double tremor = segundos > 0 ? aleatorio.nextDouble() * 2 - 1 : 0;
        foguete.setTranslateX(x);
        foguete.setTranslateY(y + tremor);
        foguete.setRotate(75 - 40 * avanco); // 0 grau aponta para cima

        chama.setVisible(segundos > 0);
        chama.setScaleY(0.8 + aleatorio.nextDouble() * 0.5);

        mostrarMultiplicador(valor, Color.WHITE);
    }

    /** Marca que o jogador já saiu; o foguete continua até explodir. */
    public void marcarRetirada(double valor) {
        aviso.setText("RETIRADO EM " + Foguete.formatar(valor));
        aviso.setFill(Color.web("#6FD0A4"));
        centralizar(aviso, 86);
    }

    public void explodir(double valor) {
        mostrarMultiplicador(valor, Color.web("#F08A72"));
        foguete.setVisible(false);
        rastro.setStroke(Color.web("#C8402F"));

        double x = foguete.getTranslateX();
        double y = foguete.getTranslateY();
        Color[] cores = {Color.web("#E8B84A"), Color.web("#C8402F"), Color.web("#F08A72")};
        for (int i = 0; i < 40; i++) {
            Circle estilhaco = new Circle(x, y, 2 + aleatorio.nextDouble() * 5, cores[i % cores.length]);
            getChildren().add(estilhaco);

            double angulo = aleatorio.nextDouble() * 2 * Math.PI;
            double distancia = 20 + aleatorio.nextDouble() * 60;
            Duration duracao = Duration.millis(500 + aleatorio.nextInt(400));
            TranslateTransition voo = new TranslateTransition(duracao, estilhaco);
            voo.setByX(distancia * Math.cos(angulo));
            voo.setByY(distancia * Math.sin(angulo));
            FadeTransition some = new FadeTransition(duracao, estilhaco);
            some.setToValue(0);
            ScaleTransition encolhe = new ScaleTransition(duracao, estilhaco);
            encolhe.setToX(0.3);
            encolhe.setToY(0.3);

            ParallelTransition explosao = new ParallelTransition(voo, some, encolhe);
            explosao.setOnFinished(e -> getChildren().remove(estilhaco));
            explosao.play();
        }
    }

    private void mostrarMultiplicador(double valor, Color cor) {
        multiplicador.setText(Foguete.formatar(valor));
        multiplicador.setFill(cor);
        centralizar(multiplicador, 62);
    }

    private static void centralizar(Text texto, double y) {
        texto.setX((LARGURA - texto.getLayoutBounds().getWidth()) / 2);
        texto.setY(y);
    }

    /** Foguete desenhado em torno da origem, apontando para cima. O primeiro filho é a chama. */
    private static Group criarFoguete() {
        Polygon fogoFora = new Polygon(-7, 16, 7, 16, 0, 36);
        fogoFora.setFill(Color.web("#E8702F"));
        Polygon fogoDentro = new Polygon(-3, 16, 3, 16, 0, 27);
        fogoDentro.setFill(Color.web("#FFE27A"));
        Group chama = new Group(fogoFora, fogoDentro);

        Polygon aletas = new Polygon(-17, 18, -8, 0, 8, 0, 17, 18);
        aletas.setFill(Color.web("#C8402F"));
        Rectangle corpo = new Rectangle(-9, -12, 18, 29);
        corpo.setArcWidth(6);
        corpo.setArcHeight(6);
        corpo.setFill(Color.web("#F1EFEA"));
        Polygon bico = new Polygon(-9, -11, 0, -28, 9, -11);
        bico.setFill(Color.web("#C8402F"));
        Circle janela = new Circle(0, -2, 4.5, Color.web("#4A6FD0"));

        return new Group(chama, aletas, corpo, bico, janela);
    }
}