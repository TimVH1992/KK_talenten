package be.kdg.talenten.view;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public final class SceneManager {
    private static final double STANDAARD_BREEDTE = 1440;
    private static final double STANDAARD_HOOGTE = 900;

    private final Stage stage;

    public SceneManager(Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Stage mag niet null zijn");
        }
        this.stage = stage;
    }

    public void toon(Parent root) {
        if (root == null) {
            throw new IllegalArgumentException("Root mag niet null zijn");
        }

        if (stage.getScene() == null) {
            Scene scene = new Scene(root, STANDAARD_BREEDTE, STANDAARD_HOOGTE);
            URL stylesheet = getClass().getResource("/css/application.css");
            if (stylesheet != null) {
                scene.getStylesheets().add(stylesheet.toExternalForm());
            }
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(root);
        }

        stage.setMinWidth(1180);
        stage.setMinHeight(720);
        stage.show();
    }

    public void sluit() {
        stage.close();
    }
}
