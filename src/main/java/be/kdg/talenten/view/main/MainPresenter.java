package be.kdg.talenten.view.main;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.beheer.BeheerPresenter;
import be.kdg.talenten.view.beheer.BeheerView;
import be.kdg.talenten.view.shared.SectionView;
import be.kdg.talenten.view.theme.ThemeManager;
import javafx.scene.Scene;

public class MainPresenter {
    private final MainView view;
    private final SceneManager sceneManager;
    private final ThemeManager themeManager;
    private final Scene scene;
    private final ApplicationConfig config;

    public MainPresenter(
            MainView view,
            SceneManager sceneManager,
            ThemeManager themeManager,
            Scene scene,
            ApplicationConfig config
    ) {
        if (view == null || sceneManager == null || themeManager == null || scene == null || config == null) {
            throw new IllegalArgumentException("MainPresenter kreeg een null-afhankelijkheid");
        }

        this.view = view;
        this.sceneManager = sceneManager;
        this.themeManager = themeManager;
        this.scene = scene;
        this.config = config;

        configureer();
    }

    private void configureer() {
        view.updateThemeIcon(themeManager.isDark());
        view.getThemeButton().setOnAction(event -> toggleTheme(view::updateThemeIcon));
        view.getBeheerButton().setOnAction(event -> toonBeheer());
        view.getVerdelingButton().setOnAction(event -> toonSectie(
                "Verdeling opstellen",
                "Kies later hier schooljaar, periode en doelgroep. Van hieruit kunt u de ingerichte talenten beheren, automatisch verdelen, de verdeling bekijken en manueel aanpassen."
        ));
        view.getKeuzelijstButton().setOnAction(event -> toonSectie(
                "Keuzelijst aanmaken",
                "Maak hier de Excel-keuzelijst voor één talentenperiode en één doelgroep."
        ));
        view.getExportButton().setOnAction(event -> toonSectie(
                "Lijsten exporteren",
                "Exporteer hier de definitieve verdeling per klas of per ingericht talent, steeds voor één periode en doelgroep."
        ));
    }

    private void toonBeheer() {
        BeheerView beheerView = new BeheerView();
        new BeheerPresenter(
                beheerView,
                view,
                sceneManager,
                themeManager,
                scene,
                config
        );
        sceneManager.toon(beheerView);
    }

    private void toonSectie(String titel, String uitleg) {
        SectionView sectionView = new SectionView(titel, uitleg);
        sectionView.updateThemeIcon(themeManager.isDark());
        sectionView.getTerugButton().setOnAction(event -> sceneManager.toon(view));
        sectionView.getThemeButton().setOnAction(event -> toggleTheme(sectionView::updateThemeIcon));
        sceneManager.toon(sectionView);
    }

    private void toggleTheme(java.util.function.Consumer<Boolean> updateCurrentIcon) {
        themeManager.toggle(scene);
        boolean dark = themeManager.isDark();
        updateCurrentIcon.accept(dark);
        view.updateThemeIcon(dark);
    }
}
