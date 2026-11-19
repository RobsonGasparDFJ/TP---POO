package app.ui;

import javafx.scene.input.MouseEvent;

import app.DatabaseSetup;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseSetup.inicializarBanco();

        Parent raiz = FXMLLoader.load(getClass().getResource("/app/ui/login.fxml"));
        Scene cena = new Scene(raiz, 1280, 720);
        cena.getStylesheets().add(getClass().getResource("/app/ui/estilo.css").toExternalForm());

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(cena);
        stage.setTitle("Sistema Bancário");
        stage.show();
        
        final double[] offset = new double[2];
        final double alturaBarra = 40;          // altura da faixa de arrasto, em px
        final boolean[] arrastando = {false};

        cena.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            arrastando[0] = e.getSceneY() <= alturaBarra;
            offset[0] = e.getSceneX();
            offset[1] = e.getSceneY();
        });

        cena.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (arrastando[0]) {
                stage.setX(e.getScreenX() - offset[0]);
                stage.setY(e.getScreenY() - offset[1]);
            }
        });
    }
}