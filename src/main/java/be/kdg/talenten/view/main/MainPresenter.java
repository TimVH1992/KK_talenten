package be.kdg.talenten.view.main;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.beheer.BeheerPresenter;
import be.kdg.talenten.view.beheer.BeheerView;
import be.kdg.talenten.view.shared.SectionView;
import be.kdg.talenten.view.theme.ThemeManager;
import be.kdg.talenten.view.verdeling.VerdelingPresenter;
import be.kdg.talenten.view.verdeling.VerdelingView;
import be.kdg.talenten.view.keuzelijst.KeuzelijstPresenter;
import be.kdg.talenten.view.keuzelijst.KeuzelijstView;
import be.kdg.talenten.view.export.ExportPresenter;
import be.kdg.talenten.view.export.ExportView;
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
        view.getVerdelingButton().setOnAction(event -> toonVerdeling());
        view.getKeuzelijstButton().setOnAction(event -> toonKeuzelijst());
        view.getExportButton().setOnAction(event -> toonExport());
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

    private void toonVerdeling() {
        VerdelingView verdelingView = new VerdelingView();
        new VerdelingPresenter(
                verdelingView, view, sceneManager, themeManager, scene,
                config.getSchooljaarService(), config.getTalentenPeriodeService(), config.getKlasService(),
                config.getAutomatischeVerdelingService(), config.getManueleToewijzingService(),
                config.getVerdelingBekijkenService()
        );
        sceneManager.toon(verdelingView);
    }

    private void toonSectie(String titel, String uitleg) {
        SectionView sectionView = new SectionView(titel, uitleg);
        sectionView.updateThemeIcon(themeManager.isDark());
        sectionView.getTerugButton().setOnAction(event -> sceneManager.toon(view));
        sectionView.getThemeButton().setOnAction(event -> toggleTheme(sectionView::updateThemeIcon));
        sceneManager.toon(sectionView);
    }

    private void toonKeuzelijst() {
        KeuzelijstView keuzelijstView = new KeuzelijstView();
        new KeuzelijstPresenter(keuzelijstView, view, sceneManager, themeManager, scene,
                config.getSchooljaarService(), config.getTalentenPeriodeService(),
                config.getVoorkeurenExcelService());
        sceneManager.toon(keuzelijstView);
    }

    private void toonExport() {
        ExportView exportView = new ExportView();
        new ExportPresenter(exportView, view, sceneManager, themeManager, scene,
                config.getSchooljaarService(), config.getTalentenPeriodeService(),
                config.getVerdelingExcelService());
        sceneManager.toon(exportView);
    }

    private void toggleTheme(java.util.function.Consumer<Boolean> updateCurrentIcon) {
        themeManager.toggle(scene);
        boolean dark = themeManager.isDark();
        updateCurrentIcon.accept(dark);
        view.updateThemeIcon(dark);
    }
}
