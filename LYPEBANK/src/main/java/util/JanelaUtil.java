package util;

import java.util.List;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;


public final class JanelaUtil {

    private static final String CHAVE = "janela.tamanhoAnterior";

    private JanelaUtil() {
    }

    /** Alterna entre maximizada e normal. Retorna true se ficou maximizada. */
    public static boolean alternarMaximizada(Stage stage) {
        double[] anterior = (double[]) stage.getProperties().get(CHAVE);

        if (anterior == null) {
            stage.getProperties().put(CHAVE, new double[] {
                    stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()
            });
            Rectangle2D area = areaUtilDaTela(stage);
            stage.setX(area.getMinX());
            stage.setY(area.getMinY());
            stage.setWidth(area.getWidth());
            stage.setHeight(area.getHeight());
            return true;
        }

        stage.setX(anterior[0]);
        stage.setY(anterior[1]);
        stage.setWidth(anterior[2]);
        stage.setHeight(anterior[3]);
        stage.getProperties().remove(CHAVE);
        return false;
    }

    public static boolean estaMaximizada(Stage stage) {
        return stage.getProperties().containsKey(CHAVE);
    }

    public static void sincronizarIcone(Button botao) {
        Platform.runLater(() -> {
            if (botao.getScene() == null || !(botao.getGraphic() instanceof FontIcon)) {
                return;
            }
            Stage stage = (Stage) botao.getScene().getWindow();
            ((FontIcon) botao.getGraphic()).setIconLiteral(
                    estaMaximizada(stage) ? "fas-window-restore" : "fas-window-maximize");
        });
    }

    private static Rectangle2D areaUtilDaTela(Stage stage) {
        List<Screen> telas = Screen.getScreensForRectangle(
                stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
        Screen tela = telas.isEmpty() ? Screen.getPrimary() : telas.get(0);
        return tela.getVisualBounds();
    }
}