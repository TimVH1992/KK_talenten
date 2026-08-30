package be.kdg.talenten.view.beheer.talentenperiode;

import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.service.beheer.TalentenPeriodeService;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.beheer.BeheerView;
import be.kdg.talenten.view.theme.ThemeManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TalentenPeriodePresenter {
    private final TalentenPeriodeView view;
    private final BeheerView beheerView;
    private final SceneManager sceneManager;
    private final ThemeManager themeManager;
    private final Scene scene;
    private final SchooljaarService schooljaarService;
    private final TalentenPeriodeService periodeService;

    public TalentenPeriodePresenter(TalentenPeriodeView view, BeheerView beheerView,
                                    SceneManager sceneManager, ThemeManager themeManager, Scene scene,
                                    SchooljaarService schooljaarService, TalentenPeriodeService periodeService) {
        if (view == null || beheerView == null || sceneManager == null || themeManager == null || scene == null
                || schooljaarService == null || periodeService == null) {
            throw new IllegalArgumentException("TalentenPeriodePresenter kreeg een null-afhankelijkheid");
        }
        this.view = view;
        this.beheerView = beheerView;
        this.sceneManager = sceneManager;
        this.themeManager = themeManager;
        this.scene = scene;
        this.schooljaarService = schooljaarService;
        this.periodeService = periodeService;

        configureer();
        laadSchooljaren();
    }

    private void configureer() {
        view.updateThemeIcon(themeManager.isDark());
        view.getTerugButton().setOnAction(event -> sceneManager.toon(beheerView));
        view.getThemeButton().setOnAction(event -> toggleTheme());
        view.getSchooljaarComboBox().valueProperty().addListener((observable, oud, nieuw) -> laadPeriodes(nieuw, null));
        view.getTabel().getSelectionModel().selectedItemProperty().addListener((observable, oud, geselecteerd) -> {
            boolean nietsGeselecteerd = geselecteerd == null;
            view.getWijzigenButton().setDisable(nietsGeselecteerd);
            view.getVerwijderenButton().setDisable(nietsGeselecteerd);
        });
        view.getToevoegenButton().setOnAction(event -> voegPeriodeToe());
        view.getWijzigenButton().setOnAction(event -> wijzigPeriode());
        view.getVerwijderenButton().setOnAction(event -> verwijderPeriode());
    }

    private void laadSchooljaren() {
        view.verbergStatus();
        try {
            List<Schooljaar> schooljaren = schooljaarService.zoekAlleSchooljaren().stream()
                    .sorted(Comparator.comparing(Schooljaar::getStartDatum).reversed()).toList();
            view.getSchooljaarComboBox().setItems(FXCollections.observableArrayList(schooljaren));
            Schooljaar actief = schooljaarService.zoekActiefSchooljaar().orElse(null);
            Schooljaar selectie = actief == null ? schooljaren.stream().findFirst().orElse(null) :
                    schooljaren.stream().filter(s -> zelfdeSchooljaar(s, actief)).findFirst().orElse(actief);
            view.getSchooljaarComboBox().setValue(selectie);
            boolean geenSchooljaar = selectie == null;
            view.getToevoegenButton().setDisable(geenSchooljaar);
            if (geenSchooljaar) laadPeriodes(null, null);
        } catch (RuntimeException exception) {
            view.getSchooljaarComboBox().setItems(FXCollections.observableArrayList());
            view.getToevoegenButton().setDisable(true);
            laadPeriodes(null, null);
            view.toonStatus("De schooljaren konden niet geladen worden. Controleer de databankverbinding.", true);
        }
    }

    private boolean zelfdeSchooljaar(Schooljaar eerste, Schooljaar tweede) {
        if (eerste.getId() != null && tweede.getId() != null) return eerste.getId().equals(tweede.getId());
        return eerste.equals(tweede);
    }

    private void laadPeriodes(Schooljaar schooljaar, TalentenPeriode teSelecteren) {
        view.verbergStatus();
        view.getTabel().getSelectionModel().clearSelection();
        if (schooljaar == null) {
            view.getTabel().setItems(FXCollections.observableArrayList());
            return;
        }
        try {
            List<TalentenPeriode> periodes = periodeService.geefPeriodesVoorSchooljaar(schooljaar).stream()
                    .sorted(Comparator.comparing(TalentenPeriode::getStartDatum)).toList();
            view.getTabel().setItems(FXCollections.observableArrayList(periodes));
            if (teSelecteren != null) periodes.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(teSelecteren.getId()))
                    .findFirst().ifPresent(p -> view.getTabel().getSelectionModel().select(p));
            view.getTabel().refresh();
        } catch (RuntimeException exception) {
            view.getTabel().setItems(FXCollections.observableArrayList());
            view.toonStatus("De talentenperiodes konden niet geladen worden. Controleer de databankverbinding.", true);
        }
    }

    private void voegPeriodeToe() {
        Schooljaar schooljaar = view.getSchooljaarComboBox().getValue();
        if (schooljaar == null) return;
        toonPeriodeDialoog("Periode toevoegen", null).ifPresent(invoer -> {
            try {
                TalentenPeriode toegevoegd = periodeService.maakPeriode(invoer.naam(), invoer.startDatum(), invoer.eindDatum(), schooljaar);
                laadPeriodes(schooljaar, toegevoegd);
                view.toonStatus("Talentenperiode " + toegevoegd.getNaam() + " is toegevoegd.", false);
            } catch (RuntimeException exception) {
                view.toonStatus(boodschap(exception, "De talentenperiode kon niet toegevoegd worden."), true);
            }
        });
    }

    private void wijzigPeriode() {
        TalentenPeriode geselecteerd = view.getTabel().getSelectionModel().getSelectedItem();
        if (geselecteerd == null) return;
        toonPeriodeDialoog("Periode wijzigen", geselecteerd).ifPresent(invoer -> {
            try {
                periodeService.wijzigPeriode(geselecteerd, invoer.naam(), invoer.startDatum(), invoer.eindDatum());
                laadPeriodes(view.getSchooljaarComboBox().getValue(), geselecteerd);
                view.toonStatus("Talentenperiode " + geselecteerd.getNaam() + " is gewijzigd.", false);
            } catch (RuntimeException exception) {
                view.toonStatus(boodschap(exception, "De talentenperiode kon niet gewijzigd worden."), true);
            }
        });
    }

    private Optional<PeriodeInvoer> toonPeriodeDialoog(String titel, TalentenPeriode periode) {
        Dialog<PeriodeInvoer> dialoog = new Dialog<>();
        dialoog.setTitle(titel);
        dialoog.setHeaderText(titel + " voor " + view.getSchooljaarComboBox().getValue().getNaam());
        dialoog.initOwner(scene.getWindow());
        dialoog.getDialogPane().getStyleClass().add("app-dialog");
        dialoog.getDialogPane().getStylesheets().setAll(scene.getStylesheets());
        ButtonType opslaan = new ButtonType("Opslaan", ButtonBar.ButtonData.OK_DONE);
        dialoog.getDialogPane().getButtonTypes().addAll(opslaan, ButtonType.CANCEL);

        TextField naam = new TextField(periode == null ? "" : periode.getNaam());
        DatePicker start = new DatePicker(periode == null ? null : periode.getStartDatum());
        DatePicker einde = new DatePicker(periode == null ? null : periode.getEindDatum());
        naam.setPromptText("Naam van de periode");
        start.setPromptText("Startdatum");
        einde.setPromptText("Einddatum");

        GridPane formulier = new GridPane();
        formulier.setHgap(12);
        formulier.setVgap(12);
        formulier.setPadding(new Insets(8, 0, 4, 0));
        formulier.addRow(0, new Label("Naam"), naam);
        formulier.addRow(1, new Label("Startdatum"), start);
        formulier.addRow(2, new Label("Einddatum"), einde);
        dialoog.getDialogPane().setContent(formulier);
        dialoog.setResultConverter(knop -> knop == opslaan ? new PeriodeInvoer(naam.getText(), start.getValue(), einde.getValue()) : null);
        return dialoog.showAndWait();
    }

    private void verwijderPeriode() {
        TalentenPeriode geselecteerd = view.getTabel().getSelectionModel().getSelectedItem();
        if (geselecteerd == null) return;
        Alert bevestiging = new Alert(Alert.AlertType.CONFIRMATION,
                "Wilt u talentenperiode " + geselecteerd.getNaam() + " verwijderen?", ButtonType.OK, ButtonType.CANCEL);
        bevestiging.setTitle("Talentenperiode verwijderen");
        bevestiging.setHeaderText("Deze actie kan niet ongedaan gemaakt worden.");
        bevestiging.initOwner(scene.getWindow());
        if (bevestiging.showAndWait().filter(ButtonType.OK::equals).isEmpty()) return;
        try {
            periodeService.verwijderPeriode(geselecteerd);
            laadPeriodes(view.getSchooljaarComboBox().getValue(), null);
            view.toonStatus("Talentenperiode " + geselecteerd.getNaam() + " is verwijderd.", false);
        } catch (RuntimeException exception) {
            view.toonStatus(boodschap(exception, "De talentenperiode kon niet verwijderd worden."), true);
        }
    }

    private String boodschap(RuntimeException exception, String standaard) {
        return exception.getMessage() == null || exception.getMessage().isBlank() ? standaard : exception.getMessage();
    }

    private void toggleTheme() {
        themeManager.toggle(scene);
        boolean dark = themeManager.isDark();
        view.updateThemeIcon(dark);
        beheerView.updateThemeIcon(dark);
    }

    private record PeriodeInvoer(String naam, java.time.LocalDate startDatum, java.time.LocalDate eindDatum) { }
}
