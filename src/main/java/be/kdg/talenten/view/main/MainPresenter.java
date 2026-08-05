package be.kdg.talenten.view.main;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.verdeling.VerdelingPresenter;
import be.kdg.talenten.view.verdeling.VerdelingView;

public class MainPresenter {
    private final ApplicationConfig config;
    private final MainView view;
    private final SceneManager sceneManager;

    public MainPresenter(ApplicationConfig config, MainView view, SceneManager sceneManager) {
        if (config == null || view == null || sceneManager == null) {
            throw new IllegalArgumentException("Config, view en sceneManager mogen niet null zijn");
        }
        this.config = config;
        this.view = view;
        this.sceneManager = sceneManager;
        addEventHandlers();
    }

    private void addEventHandlers() {
        view.getVerdelingBekijkenButton().setOnAction(event -> toonVerdeling());
        view.getAfsluitenButton().setOnAction(event -> sceneManager.sluit());
    }

    private void toonVerdeling() {
        VerdelingView verdelingView = new VerdelingView();
        new VerdelingPresenter(config, verdelingView, sceneManager, this::toonHoofdmenu);
        sceneManager.toon(verdelingView);
    }

    private void toonHoofdmenu() {
        MainView mainView = new MainView();
        new MainPresenter(config, mainView, sceneManager);
        sceneManager.toon(mainView);
    }
}
