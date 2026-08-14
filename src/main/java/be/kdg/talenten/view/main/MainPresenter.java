package be.kdg.talenten.view.main;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.navigation.AppNavigator;

public class MainPresenter {
    private final ApplicationConfig config;
    private final MainView view;

    public MainPresenter(ApplicationConfig config, MainView view, SceneManager sceneManager) {
        if (config == null || view == null || sceneManager == null) {
            throw new IllegalArgumentException("Config, view en sceneManager mogen niet null zijn");
        }

        this.config = config;
        this.view = view;

        new AppNavigator(config, sceneManager).koppelSidebar(view.getSidebar());
        laadContext();
    }

    private void laadContext() {
        try {
            String schooljaar = config.getSchooljaarService()
                    .zoekActiefSchooljaar()
                    .map(Schooljaar::getNaam)
                    .orElse(null);

            view.setActiefSchooljaar(schooljaar);
        } catch (RuntimeException exception) {
            view.setActiefSchooljaar(null);
        }
    }
}
