package be.kdg.talenten.view.beheer.klas;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.service.beheer.KlasService;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.beheer.BeheerView;
import be.kdg.talenten.view.theme.ThemeManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class KlasPresenter {
    private final KlasView view;
    private final BeheerView beheerView;
    private final SceneManager sceneManager;
    private final ThemeManager themeManager;
    private final Scene scene;
    private final SchooljaarService schooljaarService;
    private final KlasService klasService;

    public KlasPresenter(
            KlasView view,
            BeheerView beheerView,
            SceneManager sceneManager,
            ThemeManager themeManager,
            Scene scene,
            SchooljaarService schooljaarService,
            KlasService klasService
    ) {
        if (view == null || beheerView == null || sceneManager == null || themeManager == null
                || scene == null || schooljaarService == null || klasService == null) {
            throw new IllegalArgumentException("KlasPresenter kreeg een null-afhankelijkheid");
        }

        this.view = view;
        this.beheerView = beheerView;
        this.sceneManager = sceneManager;
        this.themeManager = themeManager;
        this.scene = scene;
        this.schooljaarService = schooljaarService;
        this.klasService = klasService;

        configureer();
        laadSchooljaren();
    }

    private void configureer() {
        view.updateThemeIcon(themeManager.isDark());
        view.getTerugButton().setOnAction(event -> sceneManager.toon(beheerView));
        view.getThemeButton().setOnAction(event -> toggleTheme());

        view.getSchooljaarComboBox().valueProperty().addListener(
                (observable, oud, nieuw) -> laadKlassen(nieuw, null)
        );

        view.getTabel().getSelectionModel().selectedItemProperty().addListener(
                (observable, oud, geselecteerd) -> {
                    boolean nietsGeselecteerd = geselecteerd == null;
                    view.getWijzigenButton().setDisable(nietsGeselecteerd);
                    view.getVerwijderenButton().setDisable(nietsGeselecteerd);
                }
        );

        view.getToevoegenButton().setOnAction(event -> voegKlasToe());
        view.getWijzigenButton().setOnAction(event -> wijzigKlas());
        view.getVerwijderenButton().setOnAction(event -> verwijderKlas());
    }

    private void laadSchooljaren() {
        view.verbergStatus();

        try {
            List<Schooljaar> schooljaren = schooljaarService
                    .zoekAlleSchooljaren()
                    .stream()
                    .sorted(Comparator.comparing(Schooljaar::getStartDatum).reversed())
                    .toList();

            view.getSchooljaarComboBox().setItems(
                    FXCollections.observableArrayList(schooljaren)
            );

            Schooljaar actief = schooljaarService
                    .zoekActiefSchooljaar()
                    .orElse(null);

            Schooljaar selectie = actief == null
                    ? schooljaren.stream().findFirst().orElse(null)
                    : schooljaren.stream()
                            .filter(schooljaar -> zelfdeSchooljaar(schooljaar, actief))
                            .findFirst()
                            .orElse(actief);

            view.getSchooljaarComboBox().setValue(selectie);
            view.getToevoegenButton().setDisable(selectie == null);

            if (selectie == null) {
                laadKlassen(null, null);
            }
        } catch (RuntimeException exception) {
            view.getSchooljaarComboBox().setItems(FXCollections.observableArrayList());
            view.getToevoegenButton().setDisable(true);
            laadKlassen(null, null);
            view.toonStatus(
                    "De schooljaren konden niet geladen worden. Controleer de databankverbinding.",
                    true
            );
        }
    }

    private void laadKlassen(Schooljaar schooljaar, Klas teSelecteren) {
        view.verbergStatus();
        view.getTabel().getSelectionModel().clearSelection();

        if (schooljaar == null) {
            view.getTabel().setItems(FXCollections.observableArrayList());
            return;
        }

        try {
            List<Klas> klassen = klasService
                    .geefAlleKlassen()
                    .stream()
                    .filter(klas -> zelfdeSchooljaar(klas.getSchooljaar(), schooljaar))
                    .sorted(Comparator.comparingInt(Klas::getLeerjaar).thenComparing(Klas::getNaam))
                    .toList();

            view.getTabel().setItems(FXCollections.observableArrayList(klassen));

            if (teSelecteren != null) {
                klassen.stream()
                        .filter(klas -> klas.getId() != null && klas.getId().equals(teSelecteren.getId()))
                        .findFirst()
                        .ifPresent(klas -> view.getTabel().getSelectionModel().select(klas));
            }

            view.getTabel().refresh();
        } catch (RuntimeException exception) {
            view.getTabel().setItems(FXCollections.observableArrayList());
            view.toonStatus(
                    "De klassen konden niet geladen worden. Controleer de databankverbinding.",
                    true
            );
        }
    }

    private boolean zelfdeSchooljaar(Schooljaar eerste, Schooljaar tweede) {
        if (eerste.getId() != null && tweede.getId() != null) {
            return eerste.getId().equals(tweede.getId());
        }
        return eerste.equals(tweede);
    }

    private void voegKlasToe() {
        Schooljaar schooljaar = view.getSchooljaarComboBox().getValue();
        if (schooljaar == null) {
            return;
        }

        toonKlasDialoog("Klas toevoegen", null).ifPresent(invoer -> {
            try {
                Klas toegevoegd = klasService.maakKlas(
                        invoer.naam(),
                        schooljaar,
                        invoer.leerjaar(),
                        invoer.doelgroep()
                );
                laadKlassen(schooljaar, toegevoegd);
                view.toonStatus("Klas " + toegevoegd.getNaam() + " is toegevoegd.", false);
            } catch (RuntimeException exception) {
                view.toonStatus(
                        boodschap(exception, "De klas kon niet toegevoegd worden."),
                        true
                );
            }
        });
    }

    private void wijzigKlas() {
        Klas geselecteerd = view.getTabel().getSelectionModel().getSelectedItem();
        if (geselecteerd == null) {
            return;
        }

        toonKlasDialoog("Klas wijzigen", geselecteerd).ifPresent(invoer -> {
            try {
                klasService.wijzigKlas(
                        geselecteerd,
                        invoer.naam(),
                        invoer.leerjaar(),
                        invoer.doelgroep()
                );
                laadKlassen(view.getSchooljaarComboBox().getValue(), geselecteerd);
                view.toonStatus("Klas " + geselecteerd.getNaam() + " is gewijzigd.", false);
            } catch (RuntimeException exception) {
                view.toonStatus(
                        boodschap(exception, "De klas kon niet gewijzigd worden."),
                        true
                );
            }
        });
    }

    private Optional<KlasInvoer> toonKlasDialoog(String titel, Klas klas) {
        Dialog<KlasInvoer> dialoog = new Dialog<>();
        dialoog.setTitle(titel);
        dialoog.setHeaderText(
                titel + " voor " + view.getSchooljaarComboBox().getValue().getNaam()
        );
        dialoog.initOwner(scene.getWindow());
        dialoog.getDialogPane().getStyleClass().add("app-dialog");
        dialoog.getDialogPane().getStylesheets().setAll(scene.getStylesheets());

        ButtonType opslaan = new ButtonType("Opslaan", ButtonBar.ButtonData.OK_DONE);
        dialoog.getDialogPane().getButtonTypes().addAll(opslaan, ButtonType.CANCEL);

        TextField naam = new TextField(klas == null ? "" : klas.getNaam());
        naam.setPromptText("Naam van de klas");

        ComboBox<Integer> leerjaar = new ComboBox<>(
                FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7)
        );
        leerjaar.setEditable(false);
        leerjaar.setMaxWidth(Double.MAX_VALUE);
        leerjaar.setPromptText("Selecteer een leerjaar");
        if (klas != null) {
            leerjaar.setValue(klas.getLeerjaar());
        }

        ComboBox<Doelgroep> doelgroep = new ComboBox<>(
                FXCollections.observableArrayList(Doelgroep.values())
        );
        doelgroep.setEditable(false);
        doelgroep.setMaxWidth(Double.MAX_VALUE);
        doelgroep.setPromptText("Selecteer een doelgroep");
        doelgroep.setConverter(new StringConverter<>() {
            @Override
            public String toString(Doelgroep waarde) {
                return waarde == null ? "" : leesbareDoelgroep(waarde);
            }

            @Override
            public Doelgroep fromString(String tekst) {
                return null;
            }
        });
        if (klas != null) {
            doelgroep.setValue(klas.getDoelgroep());
        }

        GridPane formulier = new GridPane();
        formulier.setHgap(12);
        formulier.setVgap(12);
        formulier.setPadding(new Insets(8, 0, 4, 0));
        formulier.addRow(0, new Label("Klasnaam"), naam);
        formulier.addRow(1, new Label("Leerjaar"), leerjaar);
        formulier.addRow(2, new Label("Doelgroep"), doelgroep);
        dialoog.getDialogPane().setContent(formulier);

        dialoog.setResultConverter(knop -> knop == opslaan
                ? new KlasInvoer(naam.getText(), leerjaar.getValue(), doelgroep.getValue())
                : null
        );

        return dialoog.showAndWait();
    }

    private String leesbareDoelgroep(Doelgroep doelgroep) {
        return switch (doelgroep) {
            case OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB ->
                    "Observatie-/opleidingsfase en eerste graad A/B";
            case KWALIFICATIEFASE_TWEEDEGRAAD_AB ->
                    "Kwalificatiefase en tweede graad A/B";
        };
    }

    private void verwijderKlas() {
        Klas geselecteerd = view.getTabel().getSelectionModel().getSelectedItem();
        if (geselecteerd == null) {
            return;
        }

        Alert bevestiging = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Wilt u klas " + geselecteerd.getNaam() + " verwijderen?",
                ButtonType.OK,
                ButtonType.CANCEL
        );
        bevestiging.setTitle("Klas verwijderen");
        bevestiging.setHeaderText("Deze actie kan niet ongedaan gemaakt worden.");
        bevestiging.initOwner(scene.getWindow());

        if (bevestiging.showAndWait().filter(ButtonType.OK::equals).isEmpty()) {
            return;
        }

        try {
            klasService.verwijderKlas(geselecteerd);
            laadKlassen(view.getSchooljaarComboBox().getValue(), null);
            view.toonStatus("Klas " + geselecteerd.getNaam() + " is verwijderd.", false);
        } catch (RuntimeException exception) {
            view.toonStatus(
                    boodschap(exception, "De klas kon niet verwijderd worden."),
                    true
            );
        }
    }

    private String boodschap(RuntimeException exception, String standaard) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? standaard
                : exception.getMessage();
    }

    private void toggleTheme() {
        themeManager.toggle(scene);
        boolean dark = themeManager.isDark();
        view.updateThemeIcon(dark);
        beheerView.updateThemeIcon(dark);
    }

    private record KlasInvoer(String naam, Integer leerjaar, Doelgroep doelgroep) {
    }
}
