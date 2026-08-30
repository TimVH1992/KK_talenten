package be.kdg.talenten.view.beheer.schooljaar;

import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.beheer.BeheerView;
import be.kdg.talenten.view.theme.ThemeManager;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SchooljaarPresenter {
    private final SchooljaarView view;
    private final BeheerView beheerView;
    private final SceneManager sceneManager;
    private final ThemeManager themeManager;
    private final Scene scene;
    private final SchooljaarService schooljaarService;

    public SchooljaarPresenter(
            SchooljaarView view,
            BeheerView beheerView,
            SceneManager sceneManager,
            ThemeManager themeManager,
            Scene scene,
            SchooljaarService schooljaarService
    ) {
        if (view == null || beheerView == null || sceneManager == null || themeManager == null || scene == null || schooljaarService == null) {
            throw new IllegalArgumentException("SchooljaarPresenter kreeg een null-afhankelijkheid");
        }

        this.view = view;
        this.beheerView = beheerView;
        this.sceneManager = sceneManager;
        this.themeManager = themeManager;
        this.scene = scene;
        this.schooljaarService = schooljaarService;

        configureer();
        laadSchooljaren();
    }

    private void configureer() {
        view.updateThemeIcon(themeManager.isDark());
        view.getTerugButton().setOnAction(event -> sceneManager.toon(beheerView));
        view.getThemeButton().setOnAction(event -> toggleTheme());

        view.getTabel()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oud, geselecteerd) ->
                        view.getMaakActiefButton().setDisable(
                                geselecteerd == null
                                        || view.isActiefSchooljaar(geselecteerd)
                        )
                );

        view.getVolgendSchooljaarButton().setOnAction(event -> voegVolgendSchooljaarToe());
        view.getMaakActiefButton().setOnAction(event -> maakGeselecteerdSchooljaarActief());
    }

    private void laadSchooljaren() {
        view.verbergStatus();

        try {
            List<Schooljaar> schooljaren = schooljaarService
                    .zoekAlleSchooljaren()
                    .stream()
                    .sorted(Comparator.comparing(Schooljaar::getStartDatum).reversed())
                    .toList();

            view.getTabel().setItems(
                    FXCollections.observableArrayList(schooljaren)
            );

            Schooljaar actief = schooljaarService
                    .zoekActiefSchooljaar()
                    .orElse(null);

            view.toonActiefSchooljaar(actief);

            if (actief != null) {
                schooljaren.stream()
                        .filter(view::isActiefSchooljaar)
                        .findFirst()
                        .ifPresent(actiefInTabel -> {
                            view.getTabel()
                                    .getSelectionModel()
                                    .select(actiefInTabel);
                            view.getTabel().scrollTo(actiefInTabel);
                        });
            }

            view.getTabel().refresh();
            view.getVolgendSchooljaarButton().setDisable(schooljaren.isEmpty());
            view.getMaakActiefButton().setDisable(true);

        } catch (RuntimeException exception) {
            view.getTabel().setItems(FXCollections.observableArrayList());
            view.toonActiefSchooljaar(null);
            view.getVolgendSchooljaarButton().setDisable(true);
            view.getMaakActiefButton().setDisable(true);
            view.toonStatus(
                    "De schooljaren konden niet geladen worden. Controleer de databankverbinding.",
                    true
            );
        }
    }

    private void voegVolgendSchooljaarToe() {
        Optional<ButtonType> antwoord = toonBevestiging(
                "Volgend schooljaar toevoegen",
                "Wilt u het volgende opeenvolgende schooljaar toevoegen?",
                "Het schooljaar wordt automatisch aangemaakt met 1 september als startdatum en 30 juni als einddatum."
        );

        if (antwoord.isEmpty() || antwoord.get() != ButtonType.OK) {
            return;
        }

        try {
            Schooljaar nieuwSchooljaar = schooljaarService.voegVolgendSchooljaarToe();
            laadSchooljaren();
            view.toonStatus(
                    "Schooljaar " + nieuwSchooljaar.getNaam() + " is toegevoegd.",
                    false
            );
        } catch (RuntimeException exception) {
            view.toonStatus(
                    exception.getMessage() == null
                            ? "Het schooljaar kon niet toegevoegd worden."
                            : exception.getMessage(),
                    true
            );
        }
    }

    private void maakGeselecteerdSchooljaarActief() {
        Schooljaar geselecteerd = view.getTabel()
                .getSelectionModel()
                .getSelectedItem();

        if (geselecteerd == null || view.isActiefSchooljaar(geselecteerd)) {
            return;
        }

        Optional<ButtonType> antwoord = toonBevestiging(
                "Actief schooljaar wijzigen",
                geselecteerd.getNaam() + " actief maken?",
                "Het huidige actieve schooljaar wordt automatisch gedeactiveerd."
        );

        if (antwoord.isEmpty() || antwoord.get() != ButtonType.OK) {
            return;
        }

        try {
            schooljaarService.maakActief(geselecteerd);
            laadSchooljaren();
            view.toonStatus(
                    geselecteerd.getNaam() + " is nu het actieve schooljaar.",
                    false
            );
        } catch (RuntimeException exception) {
            view.toonStatus(
                    exception.getMessage() == null
                            ? "Het actieve schooljaar kon niet gewijzigd worden."
                            : exception.getMessage(),
                    true
            );
        }
    }

    private Optional<ButtonType> toonBevestiging(
            String titel,
            String header,
            String inhoud
    ) {
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                inhoud,
                ButtonType.OK,
                ButtonType.CANCEL
        );
        alert.setTitle(titel);
        alert.setHeaderText(header);
        alert.initOwner(scene.getWindow());
        return alert.showAndWait();
    }

    private void toggleTheme() {
        themeManager.toggle(scene);
        boolean dark = themeManager.isDark();
        view.updateThemeIcon(dark);
        beheerView.updateThemeIcon(dark);
    }
}
