package be.kdg.talenten.view.beheer.leerkracht;

import be.kdg.talenten.domain.Leerkracht;
import be.kdg.talenten.service.beheer.LeerkrachtService;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.beheer.BeheerView;
import be.kdg.talenten.view.theme.ThemeManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LeerkrachtPresenter {
    private final LeerkrachtView view;
    private final BeheerView beheerView;
    private final SceneManager sceneManager;
    private final ThemeManager themeManager;
    private final Scene scene;
    private final LeerkrachtService leerkrachtService;
    private List<Leerkracht> alleLeerkrachten = List.of();

    public LeerkrachtPresenter(
            LeerkrachtView view,
            BeheerView beheerView,
            SceneManager sceneManager,
            ThemeManager themeManager,
            Scene scene,
            LeerkrachtService leerkrachtService
    ) {
        if (view == null || beheerView == null || sceneManager == null || themeManager == null
                || scene == null || leerkrachtService == null) {
            throw new IllegalArgumentException("LeerkrachtPresenter kreeg een null-afhankelijkheid");
        }
        this.view = view;
        this.beheerView = beheerView;
        this.sceneManager = sceneManager;
        this.themeManager = themeManager;
        this.scene = scene;
        this.leerkrachtService = leerkrachtService;

        configureer();
        laadLeerkrachten();
    }

    private void configureer() {
        view.updateThemeIcon(themeManager.isDark());
        view.getTerugButton().setOnAction(event -> sceneManager.toon(beheerView));
        view.getThemeButton().setOnAction(event -> toggleTheme());
        view.getZoekVeld().textProperty().addListener(
                (observable, oud, nieuw) -> filterLeerkrachten()
        );
        view.getTabel().getSelectionModel().selectedItemProperty().addListener(
                (observable, oud, geselecteerd) -> view.wijzigSelectieActies(
                        geselecteerd != null,
                        geselecteerd == null || geselecteerd.isActief()
                )
        );
        view.getToevoegenButton().setOnAction(event -> voegLeerkrachtToe());
        view.getWijzigenButton().setOnAction(event -> wijzigLeerkracht());
        view.getActiefWijzigenButton().setOnAction(event -> wijzigActieveStatus());
    }

    private void laadLeerkrachten() {
        view.verbergStatus();
        try {
            alleLeerkrachten = leerkrachtService.geefAlleLeerkrachten().stream()
                    .sorted(Comparator.comparing(Leerkracht::getAchternaam)
                            .thenComparing(Leerkracht::getVoornaam))
                    .toList();
            filterLeerkrachten();
        } catch (RuntimeException exception) {
            alleLeerkrachten = List.of();
            view.getTabel().setItems(FXCollections.observableArrayList());
            view.toonAantalLeerkrachten(0);
            view.toonStatus(
                    "De leerkrachten konden niet geladen worden. Controleer de databankverbinding.",
                    true
            );
        }
    }

    private void filterLeerkrachten() {
        String zoekterm = view.getZoekVeld().getText() == null
                ? ""
                : view.getZoekVeld().getText().trim().toLowerCase();

        List<Leerkracht> gefilterd = alleLeerkrachten.stream()
                .filter(leerkracht -> zoekterm.isBlank()
                        || leerkracht.getVoornaam().toLowerCase().contains(zoekterm)
                        || leerkracht.getAchternaam().toLowerCase().contains(zoekterm)
                        || (leerkracht.getVoornaam() + " " + leerkracht.getAchternaam())
                                .toLowerCase().contains(zoekterm))
                .toList();

        view.getTabel().setItems(FXCollections.observableArrayList(gefilterd));
        view.toonAantalLeerkrachten(gefilterd.size());
        view.getTabel().refresh();
    }

    private void voegLeerkrachtToe() {
        toonLeerkrachtDialoog("Leerkracht toevoegen", "Nieuwe leerkracht toevoegen", null)
                .ifPresent(invoer -> {
                    try {
                        Leerkracht toegevoegd = leerkrachtService.maakLeerkracht(
                                invoer.voornaam(), invoer.achternaam()
                        );
                        herlaadEnSelecteer(toegevoegd);
                        view.toonStatus(toegevoegd.getVoornaam() + " " + toegevoegd.getAchternaam()
                                + " is toegevoegd.", false);
                    } catch (RuntimeException exception) {
                        view.toonStatus(boodschap(exception, "De leerkracht kon niet toegevoegd worden."), true);
                    }
                });
    }

    private void wijzigLeerkracht() {
        Leerkracht leerkracht = view.getTabel().getSelectionModel().getSelectedItem();
        if (leerkracht == null) return;
        toonLeerkrachtDialoog("Leerkracht wijzigen", "Gegevens van " + volledigeNaam(leerkracht) + " wijzigen", leerkracht)
                .ifPresent(invoer -> {
                    try {
                        leerkrachtService.wijzigLeerkracht(leerkracht, invoer.voornaam(), invoer.achternaam());
                        herlaadEnSelecteer(leerkracht);
                        view.toonStatus("De gegevens van " + volledigeNaam(leerkracht) + " zijn gewijzigd.", false);
                    } catch (RuntimeException exception) {
                        view.toonStatus(boodschap(exception, "De leerkracht kon niet gewijzigd worden."), true);
                    }
                });
    }

    private Optional<LeerkrachtInvoer> toonLeerkrachtDialoog(
            String titel, String header, Leerkracht leerkracht
    ) {
        Dialog<LeerkrachtInvoer> dialoog = new Dialog<>();
        dialoog.setTitle(titel);
        dialoog.setHeaderText(header);
        dialoog.initOwner(scene.getWindow());
        dialoog.getDialogPane().getStyleClass().add("app-dialog");
        dialoog.getDialogPane().getStylesheets().setAll(scene.getStylesheets());
        ButtonType opslaan = new ButtonType("Opslaan", ButtonBar.ButtonData.OK_DONE);
        dialoog.getDialogPane().getButtonTypes().addAll(opslaan, ButtonType.CANCEL);

        TextField voornaam = new TextField(leerkracht == null ? "" : leerkracht.getVoornaam());
        voornaam.setPromptText("Voornaam");
        TextField achternaam = new TextField(leerkracht == null ? "" : leerkracht.getAchternaam());
        achternaam.setPromptText("Achternaam");
        GridPane formulier = new GridPane();
        formulier.setHgap(12);
        formulier.setVgap(12);
        formulier.setPadding(new Insets(8, 0, 4, 0));
        formulier.addRow(0, new Label("Voornaam"), voornaam);
        formulier.addRow(1, new Label("Achternaam"), achternaam);
        dialoog.getDialogPane().setContent(formulier);
        dialoog.setResultConverter(knop -> knop == opslaan
                ? new LeerkrachtInvoer(voornaam.getText(), achternaam.getText()) : null);
        return dialoog.showAndWait();
    }

    private void wijzigActieveStatus() {
        Leerkracht leerkracht = view.getTabel().getSelectionModel().getSelectedItem();
        if (leerkracht == null) return;
        String actie = leerkracht.isActief() ? "deactiveren" : "activeren";
        Alert bevestiging = new Alert(Alert.AlertType.CONFIRMATION,
                "Wilt u " + volledigeNaam(leerkracht) + " " + actie + "?",
                ButtonType.OK, ButtonType.CANCEL);
        bevestiging.setTitle("Leerkracht " + actie);
        bevestiging.setHeaderText("Actieve status wijzigen");
        bevestiging.initOwner(scene.getWindow());
        if (bevestiging.showAndWait().filter(ButtonType.OK::equals).isEmpty()) return;
        try {
            if (leerkracht.isActief()) leerkrachtService.deactiveerLeerkracht(leerkracht);
            else leerkrachtService.activeerLeerkracht(leerkracht);
            herlaadEnSelecteer(leerkracht);
            view.toonStatus(volledigeNaam(leerkracht) + " is "
                    + (leerkracht.isActief() ? "geactiveerd." : "gedeactiveerd."), false);
        } catch (RuntimeException exception) {
            view.toonStatus(boodschap(exception, "De status kon niet gewijzigd worden."), true);
        }
    }

    private void herlaadEnSelecteer(Leerkracht teSelecteren) {
        alleLeerkrachten = leerkrachtService.geefAlleLeerkrachten().stream()
                .sorted(Comparator.comparing(Leerkracht::getAchternaam)
                        .thenComparing(Leerkracht::getVoornaam))
                .toList();
        view.getZoekVeld().clear();
        filterLeerkrachten();
        view.getTabel().getItems().stream()
                .filter(leerkracht -> leerkracht.getId() != null
                        && leerkracht.getId().equals(teSelecteren.getId()))
                .findFirst()
                .ifPresent(leerkracht -> view.getTabel().getSelectionModel().select(leerkracht));
    }

    private String volledigeNaam(Leerkracht leerkracht) {
        return leerkracht.getVoornaam() + " " + leerkracht.getAchternaam();
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

    private record LeerkrachtInvoer(String voornaam, String achternaam) {
    }
}
