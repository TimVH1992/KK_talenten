package be.kdg.talenten;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.main.MainPresenter;
import be.kdg.talenten.view.main.MainView;
import javafx.application.Application;
import javafx.stage.Stage;

public class TalentenApplication extends Application {
    private ApplicationConfig config;

    @Override
    public void init() {
        config = new ApplicationConfig();
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("KK Talenten");

        SceneManager sceneManager = new SceneManager(stage);
        MainView mainView = new MainView();
        new MainPresenter(config, mainView, sceneManager);
        sceneManager.toon(mainView);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
