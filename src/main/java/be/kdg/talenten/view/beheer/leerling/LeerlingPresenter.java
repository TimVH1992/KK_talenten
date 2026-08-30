package be.kdg.talenten.view.beheer.leerling;

import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.service.beheer.KlasService;
import be.kdg.talenten.service.beheer.LeerlingService;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.service.leerling.LeerlingenPlakResultaat;
import be.kdg.talenten.service.leerling.LeerlingenPlakService;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.beheer.BeheerView;
import be.kdg.talenten.view.theme.ThemeManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public class LeerlingPresenter {
    private final LeerlingView view;
    private final BeheerView beheerView;
    private final SceneManager sceneManager;
    private final ThemeManager themeManager;
    private final Scene scene;
    private final SchooljaarService schooljaarService;
    private final LeerlingService leerlingService;
    private final KlasService klasService;
    private final LeerlingenPlakService plakService;
    private List<Leerling> leerlingenVoorSchooljaar = List.of();
    private LeerlingenPlakResultaat gevalideerdResultaat;

    public LeerlingPresenter(
            LeerlingView view,
            BeheerView beheerView,
            SceneManager sceneManager,
            ThemeManager themeManager,
            Scene scene,
            SchooljaarService schooljaarService,
            LeerlingService leerlingService,
            KlasService klasService,
            LeerlingenPlakService plakService
    ) {
        if (view == null || beheerView == null || sceneManager == null || themeManager == null
                || scene == null || schooljaarService == null || leerlingService == null
                || klasService == null || plakService == null) {
            throw new IllegalArgumentException("LeerlingPresenter kreeg een null-afhankelijkheid");
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

        configureer();
        laadSchooljaren();
    }

    private void configureer() {
        view.updateThemeIcon(themeManager.isDark());
        view.getTerugButton().setOnAction(event -> sceneManager.toon(beheerView));
        view.getThemeButton().setOnAction(event -> toggleTheme());
        view.getSchooljaarComboBox().valueProperty().addListener(
                (observable, oud, nieuw) -> laadKlassenEnLeerlingen(nieuw)
        );
        view.getKlasComboBox().valueProperty().addListener((observable, oud, nieuw) -> filterLeerlingen());
        view.getZoekVeld().textProperty().addListener((observable, oud, nieuw) -> filterLeerlingen());
        view.getPlakVeld().textProperty().addListener((observable, oud, nieuw) -> {
            gevalideerdResultaat = null;
            view.toonPlakValidatie("", false, false);
        });
        view.getValiderenButton().setOnAction(event -> valideerPlakGegevens());
        view.getBulkToevoegenButton().setOnAction(event -> voegLeerlingenInBulkToe());
        view.getToevoegenButton().setOnAction(event -> voegLeerlingToe());
        view.getWijzigenButton().setOnAction(event -> wijzigLeerling());
        view.getKlasWijzigenButton().setOnAction(event -> wijzigKlas());
        view.getActiefWijzigenButton().setOnAction(event -> wijzigActieveStatus());
        view.getTabel().getSelectionModel().selectedItemProperty().addListener(
                (observable, oud, geselecteerd) -> view.wijzigSelectieActies(
                        geselecteerd != null,
                        geselecteerd == null || geselecteerd.isActief()
                )
        );
    }

    private void laadSchooljaren() {
        view.verbergStatus();
        try {
            List<Schooljaar> schooljaren = schooljaarService.zoekAlleSchooljaren().stream()
                    .sorted(Comparator.comparing(Schooljaar::getStartDatum).reversed())
                    .toList();
            view.getSchooljaarComboBox().setItems(FXCollections.observableArrayList(schooljaren));
            Schooljaar actief = schooljaarService.zoekActiefSchooljaar().orElse(null);
            Schooljaar selectie = actief == null
                    ? schooljaren.stream().findFirst().orElse(null)
                    : schooljaren.stream().filter(s -> zelfdeSchooljaar(s, actief)).findFirst().orElse(actief);
            view.getSchooljaarComboBox().setValue(selectie);
            boolean geenSchooljaar = selectie == null;
            view.getToevoegenButton().setDisable(geenSchooljaar);
            if (geenSchooljaar) laadKlassenEnLeerlingen(null);
        } catch (RuntimeException exception) {
            view.getSchooljaarComboBox().setItems(FXCollections.observableArrayList());
            view.getToevoegenButton().setDisable(true);
            laadKlassenEnLeerlingen(null);
            view.toonStatus("De schooljaren konden niet geladen worden. Controleer de databankverbinding.", true);
        }
    }

    private void laadKlassenEnLeerlingen(Schooljaar schooljaar) {
        view.verbergStatus();
        view.getTabel().getSelectionModel().clearSelection();
        if (schooljaar == null) {
            view.getKlasComboBox().setItems(FXCollections.observableArrayList());
            leerlingenVoorSchooljaar = List.of();
            view.getTabel().setItems(FXCollections.observableArrayList());
            view.toonAantalLeerlingen(0);
            return;
        }
        try {
            List<Klas> klassen = klasService.geefAlleKlassen().stream()
                    .filter(klas -> zelfdeSchooljaar(klas.getSchooljaar(), schooljaar))
                    .sorted(Comparator.comparingInt(Klas::getLeerjaar).thenComparing(Klas::getNaam))
                    .toList();
            view.getKlasComboBox().setItems(FXCollections.observableArrayList(klassen));
            view.getKlasComboBox().setValue(klassen.stream().findFirst().orElse(null));
            leerlingenVoorSchooljaar = leerlingService.geefLeerlingenVoorSchooljaar(schooljaar).stream()
                    .sorted(Comparator.comparing((Leerling leerling) -> leerling.getKlas().getNaam())
                            .thenComparing(Leerling::getAchternaam)
                            .thenComparing(Leerling::getVoornaam))
                    .toList();
            filterLeerlingen();
        } catch (RuntimeException exception) {
            leerlingenVoorSchooljaar = List.of();
            view.getTabel().setItems(FXCollections.observableArrayList());
            view.toonAantalLeerlingen(0);
            view.toonStatus("De leerlingen konden niet geladen worden. Controleer de databankverbinding.", true);
        }
    }

    private void filterLeerlingen() {
        Klas klas = view.getKlasComboBox().getValue();
        String zoekterm = view.getZoekVeld().getText() == null
                ? "" : view.getZoekVeld().getText().trim().toLowerCase();
        List<Leerling> gefilterd = leerlingenVoorSchooljaar.stream()
                .filter(leerling -> klas == null || leerling.getKlas().equals(klas))
                .filter(leerling -> zoekterm.isBlank()
                        || leerling.getVoornaam().toLowerCase().contains(zoekterm)
                        || leerling.getAchternaam().toLowerCase().contains(zoekterm)
                        || (leerling.getVoornaam() + " " + leerling.getAchternaam()).toLowerCase().contains(zoekterm))
                .toList();
        view.getTabel().setItems(FXCollections.observableArrayList(gefilterd));
        view.toonAantalLeerlingen(gefilterd.size());
        view.getTabel().refresh();
    }

    private void voegLeerlingToe() {
        Klas klas = view.getKlasComboBox().getValue();
        if (klas == null) {
            view.toonStatus("Selecteer eerst een klas.", true);
            return;
        }

        toonLeerlingDialoog(klas).ifPresent(invoer -> {
            try {
                Leerling toegevoegd = leerlingService.maakLeerling(
                        invoer.voornaam(),
                        invoer.achternaam(),
                        klas
                );
                herlaadLeerlingenEnSelecteer(toegevoegd);
                view.toonStatus(
                        toegevoegd.getVoornaam() + " " + toegevoegd.getAchternaam() + " is toegevoegd.",
                        false
                );
            } catch (RuntimeException exception) {
                view.toonStatus(
                        exception.getMessage() == null || exception.getMessage().isBlank()
                                ? "De leerling kon niet toegevoegd worden."
                                : exception.getMessage(),
                        true
                );
            }
        });
    }

    private Optional<LeerlingInvoer> toonLeerlingDialoog(Klas klas) {
        Dialog<LeerlingInvoer> dialoog = new Dialog<>();
        dialoog.setTitle("Leerling toevoegen");
        dialoog.setHeaderText("Leerling toevoegen aan klas " + klas.getNaam());
        dialoog.initOwner(scene.getWindow());
        dialoog.getDialogPane().getStyleClass().add("app-dialog");
        dialoog.getDialogPane().getStylesheets().setAll(scene.getStylesheets());

        ButtonType opslaan = new ButtonType("Opslaan", ButtonBar.ButtonData.OK_DONE);
        dialoog.getDialogPane().getButtonTypes().addAll(opslaan, ButtonType.CANCEL);

        TextField voornaam = new TextField();
        voornaam.setPromptText("Voornaam");
        TextField achternaam = new TextField();
        achternaam.setPromptText("Achternaam");

        GridPane formulier = new GridPane();
        formulier.setHgap(12);
        formulier.setVgap(12);
        formulier.setPadding(new Insets(8, 0, 4, 0));
        formulier.addRow(0, new Label("Voornaam"), voornaam);
        formulier.addRow(1, new Label("Achternaam"), achternaam);
        dialoog.getDialogPane().setContent(formulier);
        dialoog.setResultConverter(knop -> knop == opslaan
                ? new LeerlingInvoer(voornaam.getText(), achternaam.getText())
                : null
        );
        return dialoog.showAndWait();
    }

    private void wijzigLeerling() {
        Leerling leerling = view.getTabel().getSelectionModel().getSelectedItem();
        if (leerling == null) return;

        toonWijzigLeerlingDialoog(leerling).ifPresent(invoer -> {
            try {
                leerlingService.wijzigLeerling(leerling, invoer.voornaam(), invoer.achternaam());
                herlaadLeerlingenEnSelecteer(leerling);
                view.toonStatus("De gegevens van " + leerling + " zijn gewijzigd.", false);
            } catch (RuntimeException exception) {
                view.toonStatus(boodschap(exception, "De leerling kon niet gewijzigd worden."), true);
            }
        });
    }

    private Optional<LeerlingInvoer> toonWijzigLeerlingDialoog(Leerling leerling) {
        Dialog<LeerlingInvoer> dialoog = new Dialog<>();
        configureerDialoog(dialoog, "Leerling wijzigen", "Gegevens van " + leerling + " wijzigen");
        ButtonType opslaan = new ButtonType("Opslaan", ButtonBar.ButtonData.OK_DONE);
        dialoog.getDialogPane().getButtonTypes().addAll(opslaan, ButtonType.CANCEL);
        TextField voornaam = new TextField(leerling.getVoornaam());
        TextField achternaam = new TextField(leerling.getAchternaam());
        GridPane formulier = maakFormulier();
        formulier.addRow(0, new Label("Voornaam"), voornaam);
        formulier.addRow(1, new Label("Achternaam"), achternaam);
        dialoog.getDialogPane().setContent(formulier);
        dialoog.setResultConverter(knop -> knop == opslaan
                ? new LeerlingInvoer(voornaam.getText(), achternaam.getText()) : null);
        return dialoog.showAndWait();
    }

    private void wijzigKlas() {
        Leerling leerling = view.getTabel().getSelectionModel().getSelectedItem();
        if (leerling == null) return;

        toonKlasWijzigDialoog(leerling).ifPresent(invoer -> {
            try {
                leerlingService.wijzigKlas(leerling, invoer.klas(), invoer.wisseldatum());
                herlaadLeerlingenEnSelecteer(leerling);
                view.toonStatus(leerling + " is verplaatst naar klas " + invoer.klas().getNaam() + ".", false);
            } catch (RuntimeException exception) {
                view.toonStatus(boodschap(exception, "De klas kon niet gewijzigd worden."), true);
            }
        });
    }

    private Optional<KlasWijzigInvoer> toonKlasWijzigDialoog(Leerling leerling) {
        Dialog<KlasWijzigInvoer> dialoog = new Dialog<>();
        configureerDialoog(dialoog, "Klas wijzigen", "Klas van " + leerling + " wijzigen");
        ButtonType opslaan = new ButtonType("Opslaan", ButtonBar.ButtonData.OK_DONE);
        dialoog.getDialogPane().getButtonTypes().addAll(opslaan, ButtonType.CANCEL);

        ComboBox<Klas> klas = new ComboBox<>(view.getKlasComboBox().getItems());
        klas.setEditable(false);
        klas.setMaxWidth(Double.MAX_VALUE);
        klas.setValue(leerling.getKlas());
        DatePicker wisseldatum = new DatePicker(LocalDate.now());
        wisseldatum.setMaxWidth(Double.MAX_VALUE);

        GridPane formulier = maakFormulier();
        formulier.addRow(0, new Label("Nieuwe klas"), klas);
        formulier.addRow(1, new Label("Wisseldatum"), wisseldatum);
        dialoog.getDialogPane().setContent(formulier);
        dialoog.setResultConverter(knop -> knop == opslaan
                ? new KlasWijzigInvoer(klas.getValue(), wisseldatum.getValue()) : null);
        return dialoog.showAndWait();
    }

    private void wijzigActieveStatus() {
        Leerling leerling = view.getTabel().getSelectionModel().getSelectedItem();
        if (leerling == null) return;
        String actie = leerling.isActief() ? "deactiveren" : "activeren";
        Alert bevestiging = new Alert(Alert.AlertType.CONFIRMATION,
                "Wilt u " + leerling + " " + actie + "?", ButtonType.OK, ButtonType.CANCEL);
        bevestiging.setTitle("Leerling " + actie);
        bevestiging.setHeaderText("Actieve status wijzigen");
        bevestiging.initOwner(scene.getWindow());
        if (bevestiging.showAndWait().filter(ButtonType.OK::equals).isEmpty()) return;

        try {
            if (leerling.isActief()) leerlingService.deactiveerLeerling(leerling);
            else leerlingService.activeerLeerling(leerling);
            herlaadLeerlingenEnSelecteer(leerling);
            view.toonStatus(leerling + " is " + (leerling.isActief() ? "geactiveerd." : "gedeactiveerd."), false);
        } catch (RuntimeException exception) {
            view.toonStatus(boodschap(exception, "De status kon niet gewijzigd worden."), true);
        }
    }

    private <T> void configureerDialoog(Dialog<T> dialoog, String titel, String header) {
        dialoog.setTitle(titel);
        dialoog.setHeaderText(header);
        dialoog.initOwner(scene.getWindow());
        dialoog.getDialogPane().getStyleClass().add("app-dialog");
        dialoog.getDialogPane().getStylesheets().setAll(scene.getStylesheets());
    }

    private GridPane maakFormulier() {
        GridPane formulier = new GridPane();
        formulier.setHgap(12);
        formulier.setVgap(12);
        formulier.setPadding(new Insets(8, 0, 4, 0));
        return formulier;
    }

    private void herlaadLeerlingenEnSelecteer(Leerling teSelecteren) {
        Schooljaar schooljaar = view.getSchooljaarComboBox().getValue();
        if (schooljaar == null) return;
        leerlingenVoorSchooljaar = leerlingService.geefLeerlingenVoorSchooljaar(schooljaar).stream()
                .sorted(Comparator.comparing((Leerling leerling) -> leerling.getKlas().getNaam())
                        .thenComparing(Leerling::getAchternaam)
                        .thenComparing(Leerling::getVoornaam))
                .toList();
        view.getKlasComboBox().getItems().stream()
                .filter(klas -> klas.equals(teSelecteren.getKlas()))
                .findFirst()
                .ifPresent(klas -> view.getKlasComboBox().setValue(klas));
        view.getZoekVeld().clear();
        filterLeerlingen();
        view.getTabel().getItems().stream()
                .filter(leerling -> leerling.getId() != null && leerling.getId().equals(teSelecteren.getId()))
                .findFirst()
                .ifPresent(leerling -> view.getTabel().getSelectionModel().select(leerling));
    }

    private void valideerPlakGegevens() {
        if (view.getKlasComboBox().getValue() == null) {
            view.toonPlakValidatie("Selecteer eerst een klas.", true, false);
            return;
        }
        try {
            gevalideerdResultaat = plakService.analyseer(view.getPlakVeld().getText());
            if (gevalideerdResultaat.heeftProblemen()) {
                view.toonPlakValidatie(String.join(" | ", gevalideerdResultaat.getProblemen()), true, false);
            } else {
                int aantal = gevalideerdResultaat.getAantalGeldigeLeerlingen();
                view.toonPlakValidatie(aantal + " leerlingen zijn klaar om toe te voegen.", false, true);
            }
        } catch (RuntimeException exception) {
            gevalideerdResultaat = null;
            view.toonPlakValidatie(exception.getMessage(), true, false);
        }
    }

    private void voegLeerlingenInBulkToe() {
        Klas klas = view.getKlasComboBox().getValue();
        if (klas == null || gevalideerdResultaat == null || gevalideerdResultaat.heeftProblemen()) return;
        try {
            int aantal = plakService.slaLeerlingenOp(klas, gevalideerdResultaat).size();
            view.getPlakVeld().clear();
            gevalideerdResultaat = null;
            laadKlassenEnLeerlingen(view.getSchooljaarComboBox().getValue());
            view.getKlasComboBox().getItems().stream()
                    .filter(item -> item.equals(klas)).findFirst()
                    .ifPresent(item -> view.getKlasComboBox().setValue(item));
            view.toonStatus(aantal + " leerlingen zijn toegevoegd aan klas " + klas.getNaam() + ".", false);
        } catch (RuntimeException exception) {
            view.toonStatus(exception.getMessage() == null
                    ? "De leerlingen konden niet toegevoegd worden." : exception.getMessage(), true);
        }
    }

    private boolean zelfdeSchooljaar(Schooljaar eerste, Schooljaar tweede) {
        if (eerste.getId() != null && tweede.getId() != null) return eerste.getId().equals(tweede.getId());
        return eerste.equals(tweede);
    }

    private String boodschap(RuntimeException exception, String standaard) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? standaard : exception.getMessage();
    }

    private void toggleTheme() {
        themeManager.toggle(scene);
        boolean dark = themeManager.isDark();
        view.updateThemeIcon(dark);
        beheerView.updateThemeIcon(dark);
    }

    private record LeerlingInvoer(String voornaam, String achternaam) {
    }

    private record KlasWijzigInvoer(Klas klas, LocalDate wisseldatum) {
    }
}
