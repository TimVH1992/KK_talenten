package be.kdg.talenten.view.beheer.leerling.bulk;

import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.service.beheer.KlasService;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.service.leerling.LeerlingenPlakResultaat;
import be.kdg.talenten.service.leerling.LeerlingenPlakService;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.beheer.leerling.LeerlingenKeuzeView;
import be.kdg.talenten.view.theme.ThemeManager;
import javafx.collections.FXCollections;
import javafx.scene.Scene;

import java.util.Comparator;
import java.util.List;

public class LeerlingenBulkPresenter {
    private final LeerlingenBulkView view;
    private final LeerlingenKeuzeView keuzeView;
    private final SceneManager sceneManager;
    private final ThemeManager themeManager;
    private final Scene scene;
    private final SchooljaarService schooljaarService;
    private final KlasService klasService;
    private final LeerlingenPlakService plakService;
    private LeerlingenPlakResultaat resultaat;

    public LeerlingenBulkPresenter(LeerlingenBulkView view, LeerlingenKeuzeView keuzeView,
                                   SceneManager sceneManager, ThemeManager themeManager, Scene scene,
                                   SchooljaarService schooljaarService, KlasService klasService,
                                   LeerlingenPlakService plakService) {
        if (view == null || keuzeView == null || sceneManager == null || themeManager == null || scene == null
                || schooljaarService == null || klasService == null || plakService == null) {
            throw new IllegalArgumentException("LeerlingenBulkPresenter kreeg een null-afhankelijkheid");
        }
        this.view = view;
        this.keuzeView = keuzeView;
        this.sceneManager = sceneManager;
        this.themeManager = themeManager;
        this.scene = scene;
        this.schooljaarService = schooljaarService;
        this.klasService = klasService;
        this.plakService = plakService;
        configureer();
        laadSchooljaren();
    }

    private void configureer() {
        view.updateThemeIcon(themeManager.isDark());
        view.getTerugButton().setOnAction(event -> sceneManager.toon(keuzeView));
        view.getThemeButton().setOnAction(event -> toggleTheme());
        view.getSchooljaarComboBox().valueProperty().addListener((o, oud, nieuw) -> laadKlassen(nieuw));
        view.getPlakVeld().textProperty().addListener((o, oud, nieuw) -> {
            resultaat = null;
            view.toonValidatie("", false, false);
        });
        view.getValiderenButton().setOnAction(event -> valideer());
        view.getToevoegenButton().setOnAction(event -> voegToe());
    }

    private void laadSchooljaren() {
        try {
            List<Schooljaar> jaren = schooljaarService.zoekAlleSchooljaren().stream()
                    .sorted(Comparator.comparing(Schooljaar::getStartDatum).reversed()).toList();
            view.getSchooljaarComboBox().setItems(FXCollections.observableArrayList(jaren));
            Schooljaar actief = schooljaarService.zoekActiefSchooljaar().orElse(null);
            Schooljaar selectie = actief == null ? jaren.stream().findFirst().orElse(null)
                    : jaren.stream().filter(j -> zelfdeSchooljaar(j, actief)).findFirst().orElse(actief);
            view.getSchooljaarComboBox().setValue(selectie);
        } catch (RuntimeException e) {
            view.toonStatus("De schooljaren konden niet geladen worden.", true);
        }
    }

    private void laadKlassen(Schooljaar jaar) {
        if (jaar == null) {
            view.getKlasComboBox().setItems(FXCollections.observableArrayList());
            return;
        }
        try {
            List<Klas> klassen = klasService.geefAlleKlassen().stream()
                    .filter(k -> zelfdeSchooljaar(k.getSchooljaar(), jaar))
                    .sorted(Comparator.comparingInt(Klas::getLeerjaar).thenComparing(Klas::getNaam)).toList();
            view.getKlasComboBox().setItems(FXCollections.observableArrayList(klassen));
            view.getKlasComboBox().setValue(klassen.stream().findFirst().orElse(null));
        } catch (RuntimeException e) {
            view.toonStatus("De klassen konden niet geladen worden.", true);
        }
    }

    private void valideer() {
        if (view.getKlasComboBox().getValue() == null) {
            view.toonValidatie("Selecteer eerst een klas.", true, false);
            return;
        }
        try {
            resultaat = plakService.analyseer(view.getPlakVeld().getText());
            if (resultaat.heeftProblemen()) {
                view.toonValidatie(String.join(" | ", resultaat.getProblemen()), true, false);
            } else {
                view.toonValidatie(resultaat.getAantalGeldigeLeerlingen()
                        + " leerlingen zijn klaar om toe te voegen.", false, true);
            }
        } catch (RuntimeException e) {
            resultaat = null;
            view.toonValidatie(e.getMessage(), true, false);
        }
    }

    private void voegToe() {
        Klas klas = view.getKlasComboBox().getValue();
        if (klas == null || resultaat == null || resultaat.heeftProblemen()) return;
        try {
            int aantal = plakService.slaLeerlingenOp(klas, resultaat).size();
            view.getPlakVeld().clear();
            resultaat = null;
            view.toonStatus(aantal + " leerlingen zijn toegevoegd aan klas " + klas.getNaam() + ".", false);
        } catch (RuntimeException e) {
            view.toonStatus(e.getMessage() == null ? "De leerlingen konden niet toegevoegd worden." : e.getMessage(), true);
        }
    }

    private boolean zelfdeSchooljaar(Schooljaar a, Schooljaar b) {
        return a.getId() != null && b.getId() != null ? a.getId().equals(b.getId()) : a.equals(b);
    }

    private void toggleTheme() {
        themeManager.toggle(scene);
        boolean dark = themeManager.isDark();
        view.updateThemeIcon(dark);
        keuzeView.updateThemeIcon(dark);
    }
}
