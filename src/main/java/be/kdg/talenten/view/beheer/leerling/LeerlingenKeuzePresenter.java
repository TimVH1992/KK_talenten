package be.kdg.talenten.view.beheer.leerling;

import be.kdg.talenten.service.beheer.*;
import be.kdg.talenten.service.leerling.LeerlingenPlakService;
import be.kdg.talenten.service.leerling.LeerlingHistoriekService;
import be.kdg.talenten.view.beheer.leerling.historiek.LeerlingHistoriekPresenter;
import be.kdg.talenten.view.beheer.leerling.historiek.LeerlingHistoriekView;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.beheer.BeheerView;
import be.kdg.talenten.view.beheer.leerling.bulk.LeerlingenBulkPresenter;
import be.kdg.talenten.view.beheer.leerling.bulk.LeerlingenBulkView;
import be.kdg.talenten.view.theme.ThemeManager;
import javafx.scene.Scene;

public class LeerlingenKeuzePresenter {
    private final LeerlingenKeuzeView view;
    private final BeheerView beheerView;
    private final SceneManager sceneManager;
    private final ThemeManager themeManager;
    private final Scene scene;
    private final SchooljaarService schooljaarService;
    private final LeerlingService leerlingService;
    private final KlasService klasService;
    private final LeerlingenPlakService plakService;
    private final LeerlingHistoriekService historiekService;

    public LeerlingenKeuzePresenter(LeerlingenKeuzeView view, BeheerView beheerView,
                                    SceneManager sceneManager, ThemeManager themeManager, Scene scene,
                                    SchooljaarService schooljaarService, LeerlingService leerlingService,
                                    KlasService klasService, LeerlingenPlakService plakService,
                                    LeerlingHistoriekService historiekService) {
        if (view == null || beheerView == null || sceneManager == null || themeManager == null || scene == null
                || schooljaarService == null || leerlingService == null || klasService == null || plakService == null
                || historiekService == null) {
            throw new IllegalArgumentException("LeerlingenKeuzePresenter kreeg een null-afhankelijkheid");
        }
        this.view = view;
        this.beheerView = beheerView;
        this.sceneManager = sceneManager;
        this.themeManager = themeManager;
        this.scene = scene;
        this.schooljaarService = schooljaarService;
        this.leerlingService = leerlingService;
        this.klasService = klasService;
        this.plakService = plakService;
        this.historiekService = historiekService;
        configureer();
    }

    private void configureer() {
        view.updateThemeIcon(themeManager.isDark());
        view.getTerugButton().setOnAction(event -> sceneManager.toon(beheerView));
        view.getThemeButton().setOnAction(event -> toggleTheme());
        view.getBulkButton().setOnAction(event -> toonBulk());
        view.getIndividueelButton().setOnAction(event -> toonIndividueel());
        view.getHistoriekButton().setOnAction(event -> toonHistoriek());
    }

    private void toonBulk() {
        LeerlingenBulkView bulkView = new LeerlingenBulkView();
        new LeerlingenBulkPresenter(bulkView, view, sceneManager, themeManager, scene,
                schooljaarService, klasService, plakService);
        sceneManager.toon(bulkView);
    }

    private void toonIndividueel() {
        LeerlingView leerlingView = new LeerlingView();
        new LeerlingPresenter(leerlingView, beheerView, sceneManager, themeManager, scene,
                schooljaarService, leerlingService, klasService, plakService);
        leerlingView.getTerugButton().setOnAction(event -> sceneManager.toon(view));
        leerlingView.getThemeButton().setOnAction(event -> {
            themeManager.toggle(scene);
            boolean dark = themeManager.isDark();
            leerlingView.updateThemeIcon(dark);
            view.updateThemeIcon(dark);
        });
        sceneManager.toon(leerlingView);
    }

    private void toonHistoriek() {
        LeerlingHistoriekView historiekView = new LeerlingHistoriekView();
        new LeerlingHistoriekPresenter(historiekView, view, sceneManager, themeManager, scene,
                schooljaarService, leerlingService, historiekService);
        sceneManager.toon(historiekView);
    }

    private void toggleTheme() {
        themeManager.toggle(scene);
        boolean dark = themeManager.isDark();
        view.updateThemeIcon(dark);
        beheerView.updateThemeIcon(dark);
    }
}
