package be.kdg.talenten;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class TalentenApplication extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("KK Talenten");

        StackPane root =
                new StackPane();

        Scene scene =
                new Scene(
                        root,
                        1200,
                        800
                );

        stage.setScene(
                scene
        );

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}