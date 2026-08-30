package be.kdg.talenten;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.main.MainPresenter;
import be.kdg.talenten.view.main.MainView;
import be.kdg.talenten.view.theme.ThemeManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TalentenApplication extends Application {
    private ApplicationConfig config;

    @Override
    public void init() {
        config = new ApplicationConfig();
    }

    @Override
    public void start(Stage stage) {
        MainView mainView = new MainView();
        Scene scene = new Scene(mainView, 1200, 800);

        ThemeManager themeManager = new ThemeManager();
        themeManager.apply(scene);

        SceneManager sceneManager = new SceneManager(scene);
        new MainPresenter(mainView, sceneManager, themeManager, scene, config);

        stage.setTitle("Talenten KK");
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
