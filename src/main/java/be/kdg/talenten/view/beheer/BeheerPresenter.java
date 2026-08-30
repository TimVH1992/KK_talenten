package be.kdg.talenten.view.beheer;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.beheer.basistalent.BasistalentPresenter;
import be.kdg.talenten.view.beheer.basistalent.BasistalentView;
import be.kdg.talenten.view.beheer.klas.KlasPresenter;
import be.kdg.talenten.view.beheer.klas.KlasView;
import be.kdg.talenten.view.beheer.ingerichttalent.IngerichtTalentPresenter;
import be.kdg.talenten.view.beheer.ingerichttalent.IngerichtTalentView;
import be.kdg.talenten.view.beheer.leerkracht.LeerkrachtPresenter;
import be.kdg.talenten.view.beheer.leerkracht.LeerkrachtView;
import be.kdg.talenten.view.beheer.leerling.LeerlingPresenter;
import be.kdg.talenten.view.beheer.leerling.LeerlingView;
import be.kdg.talenten.view.beheer.leerling.LeerlingenKeuzePresenter;
import be.kdg.talenten.view.beheer.leerling.LeerlingenKeuzeView;
import be.kdg.talenten.view.beheer.schooljaar.SchooljaarPresenter;
import be.kdg.talenten.view.beheer.schooljaar.SchooljaarView;
import be.kdg.talenten.view.beheer.talentenperiode.TalentenPeriodePresenter;
import be.kdg.talenten.view.beheer.talentenperiode.TalentenPeriodeView;
import be.kdg.talenten.view.main.MainView;
import be.kdg.talenten.view.theme.ThemeManager;
import javafx.scene.Scene;

public class BeheerPresenter {
    private final BeheerView view;
    private final MainView mainView;
    private final SceneManager sceneManager;
    private final ThemeManager themeManager;
    private final Scene scene;
    private final ApplicationConfig config;

    public BeheerPresenter(
            BeheerView view,
            MainView mainView,
            SceneManager sceneManager,
            ThemeManager themeManager,
            Scene scene,
            ApplicationConfig config
    ) {
        if (view == null || mainView == null || sceneManager == null || themeManager == null || scene == null || config == null) {
            throw new IllegalArgumentException("BeheerPresenter kreeg een null-afhankelijkheid");
        }

        this.view = view;
        this.mainView = mainView;
        this.sceneManager = sceneManager;
        this.themeManager = themeManager;
        this.scene = scene;
        this.config = config;

        configureer();
    }

    private void configureer() {
        view.updateThemeIcon(themeManager.isDark());
        view.getTerugButton().setOnAction(event -> sceneManager.toon(mainView));
        view.getThemeButton().setOnAction(event -> toggleTheme());

        view.getSchooljarenButton().setOnAction(event -> toonSchooljaren());
        view.getPeriodesButton().setOnAction(event -> toonTalentenperiodes());
        view.getKlassenButton().setOnAction(event -> toonKlassen());
        view.getLeerlingenButton().setOnAction(event -> toonLeerlingen());
        view.getLeerkrachtenButton().setOnAction(event -> toonLeerkrachten());
        view.getIngerichteTalentenButton().setOnAction(event -> toonIngerichteTalenten());
        view.getBasisTalentenButton().setOnAction(event -> toonBasistalenten());
    }

    private void toonSchooljaren() {
        SchooljaarView schooljaarView = new SchooljaarView();
        new SchooljaarPresenter(
                schooljaarView,
                view,
                sceneManager,
                themeManager,
                scene,
                config.getSchooljaarService()
        );
        sceneManager.toon(schooljaarView);
    }

    private void toonTalentenperiodes() {
        TalentenPeriodeView periodeView = new TalentenPeriodeView();
        new TalentenPeriodePresenter(
                periodeView,
                view,
                sceneManager,
                themeManager,
                scene,
                config.getSchooljaarService(),
                config.getTalentenPeriodeService()
        );
        sceneManager.toon(periodeView);
    }

    private void toonKlassen() {
        KlasView klasView = new KlasView();
        new KlasPresenter(
                klasView,
                view,
                sceneManager,
                themeManager,
                scene,
                config.getSchooljaarService(),
                config.getKlasService()
        );
        sceneManager.toon(klasView);
    }

    private void toonLeerlingen() {
        LeerlingenKeuzeView keuzeView = new LeerlingenKeuzeView();
        new LeerlingenKeuzePresenter(
                keuzeView,
                view,
                sceneManager,
                themeManager,
                scene,
                config.getSchooljaarService(),
                config.getLeerlingService(),
                config.getKlasService(),
                config.getLeerlingenPlakService()
        );
        sceneManager.toon(keuzeView);
    }

    private void toonLeerkrachten() {
        LeerkrachtView leerkrachtView = new LeerkrachtView();
        new LeerkrachtPresenter(
                leerkrachtView,
                view,
                sceneManager,
                themeManager,
                scene,
                config.getLeerkrachtService()
        );
        sceneManager.toon(leerkrachtView);
    }

    private void toonIngerichteTalenten() {
        IngerichtTalentView ingerichtTalentView = new IngerichtTalentView();
        new IngerichtTalentPresenter(
                ingerichtTalentView, view, sceneManager, themeManager, scene,
                config.getSchooljaarService(), config.getTalentenPeriodeService(),
                config.getIngerichtTalentService(), config.getTalentService(),
                config.getLeerkrachtService()
        );
        sceneManager.toon(ingerichtTalentView);
    }

    private void toonBasistalenten() {
        BasistalentView basistalentView = new BasistalentView();
        new BasistalentPresenter(
                basistalentView, view, sceneManager, themeManager, scene,
                config.getTalentService()
        );
        sceneManager.toon(basistalentView);
    }

    private void toggleTheme() {
        themeManager.toggle(scene);
        boolean dark = themeManager.isDark();
        view.updateThemeIcon(dark);
        mainView.updateThemeIcon(dark);
    }
}
