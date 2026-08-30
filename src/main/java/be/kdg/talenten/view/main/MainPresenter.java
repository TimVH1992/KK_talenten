package be.kdg.talenten.view.main;

import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.beheer.BeheerView;
import be.kdg.talenten.view.shared.SectionView;
import be.kdg.talenten.view.theme.ThemeManager;
import javafx.scene.Scene;

public class MainPresenter {
    private final MainView view;
    private final SceneManager sceneManager;
    private final ThemeManager themeManager;
    private final Scene scene;

    public MainPresenter(MainView view, SceneManager sceneManager, ThemeManager themeManager, Scene scene) {
        if (view == null || sceneManager == null || themeManager == null || scene == null) {
            throw new IllegalArgumentException("MainPresenter kreeg een null-afhankelijkheid");
        }

        this.view = view;
        this.sceneManager = sceneManager;
        this.themeManager = themeManager;
        this.scene = scene;

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
        beheerView.updateThemeIcon(themeManager.isDark());
        beheerView.getTerugButton().setOnAction(event -> sceneManager.toon(view));
        beheerView.getThemeButton().setOnAction(event -> toggleTheme(beheerView::updateThemeIcon));

        beheerView.getSchooljarenButton().setOnAction(event -> toonBeheerSectie(beheerView, "Schooljaren", "Maak het volgende schooljaar aan en beheer welk schooljaar actief is."));
        beheerView.getPeriodesButton().setOnAction(event -> toonBeheerSectie(beheerView, "Talentenperiodes", "Beheer de talentenperiodes binnen een schooljaar."));
        beheerView.getKlassenButton().setOnAction(event -> toonBeheerSectie(beheerView, "Klassen", "Beheer klassen, leerjaren en doelgroepen."));
        beheerView.getLeerlingenButton().setOnAction(event -> toonBeheerSectie(beheerView, "Leerlingen", "Beheer leerlingen, actieve status en klaswissels met historiek."));
        beheerView.getLeerkrachtenButton().setOnAction(event -> toonBeheerSectie(beheerView, "Leerkrachten", "Beheer leerkrachten en hun actieve status."));
        beheerView.getTalentenButton().setOnAction(event -> toonBeheerSectie(beheerView, "Talenten", "Beheer de basistalenten die later per periode ingericht kunnen worden."));

        sceneManager.toon(beheerView);
    }

    private void toonBeheerSectie(BeheerView beheerView, String titel, String uitleg) {
        SectionView sectionView = new SectionView(titel, uitleg);
        sectionView.updateThemeIcon(themeManager.isDark());
        sectionView.getTerugButton().setOnAction(event -> sceneManager.toon(beheerView));
        sectionView.getThemeButton().setOnAction(event -> toggleTheme(sectionView::updateThemeIcon));
        sceneManager.toon(sectionView);
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
