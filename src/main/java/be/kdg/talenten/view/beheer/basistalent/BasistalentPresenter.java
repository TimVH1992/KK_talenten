package be.kdg.talenten.view.beheer.basistalent;

import be.kdg.talenten.domain.Talent;
import be.kdg.talenten.service.beheer.TalentService;
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
import java.util.Locale;
import java.util.Optional;

public class BasistalentPresenter {
    private final BasistalentView view;
    private final BeheerView beheerView;
    private final SceneManager sceneManager;
    private final ThemeManager themeManager;
    private final Scene scene;
    private final TalentService talentService;
    private List<Talent> alleTalenten = List.of();

    public BasistalentPresenter(BasistalentView view, BeheerView beheerView,
                               SceneManager sceneManager, ThemeManager themeManager,
                               Scene scene, TalentService talentService) {
        if (view == null || beheerView == null || sceneManager == null || themeManager == null
                || scene == null || talentService == null) {
            throw new IllegalArgumentException("BasistalentPresenter kreeg een null-afhankelijkheid");
        }
        this.view = view;
        this.beheerView = beheerView;
        this.sceneManager = sceneManager;
        this.themeManager = themeManager;
        this.scene = scene;
        this.talentService = talentService;
        configureer();
        laadTalenten();
    }

    private void configureer() {
        view.updateThemeIcon(themeManager.isDark());
        view.getTerugButton().setOnAction(event -> sceneManager.toon(beheerView));
        view.getThemeButton().setOnAction(event -> toggleTheme());
        view.getZoekVeld().textProperty().addListener((observable, oud, nieuw) -> filterTalenten());
        view.getTabel().getSelectionModel().selectedItemProperty().addListener(
                (observable, oud, geselecteerd) -> view.getWijzigenButton().setDisable(geselecteerd == null));
        view.getToevoegenButton().setOnAction(event -> voegTalentToe());
        view.getWijzigenButton().setOnAction(event -> wijzigTalent());
    }

    private void laadTalenten() {
        view.verbergStatus();
        try {
            alleTalenten = talentService.geefAlleTalenten().stream()
                    .sorted(Comparator.comparing(Talent::getNaam, String.CASE_INSENSITIVE_ORDER))
                    .toList();
            filterTalenten();
        } catch (RuntimeException exception) {
            alleTalenten = List.of();
            view.getTabel().setItems(FXCollections.observableArrayList());
            view.toonAantal(0);
            view.toonStatus("De basistalenten konden niet geladen worden. Controleer de databankverbinding.", true);
        }
    }

    private void filterTalenten() {
        List<Talent> gefilterd = filter(alleTalenten, view.getZoekVeld().getText());
        view.getTabel().setItems(FXCollections.observableArrayList(gefilterd));
        view.toonAantal(gefilterd.size());
        view.getTabel().refresh();
    }

    static List<Talent> filter(List<Talent> talenten, String zoekterm) {
        String term = zoekterm == null ? "" : zoekterm.trim().toLowerCase(Locale.ROOT);
        return talenten.stream().filter(talent -> term.isBlank()
                || talent.getNaam().toLowerCase(Locale.ROOT).contains(term)
                || talent.getBeschrijving().toLowerCase(Locale.ROOT).contains(term)).toList();
    }

    private void voegTalentToe() {
        toonDialoog("Basistalent toevoegen", "Nieuw basistalent toevoegen", null).ifPresent(invoer -> {
            try {
                Talent toegevoegd = talentService.maakTalent(invoer.naam(), invoer.beschrijving());
                herlaadEnSelecteer(toegevoegd);
                view.toonStatus(gepoetsteNaam(toegevoegd) + " is toegevoegd.", false);
            } catch (RuntimeException exception) {
                view.toonStatus(boodschap(exception, "Het basistalent kon niet toegevoegd worden."), true);
            }
        });
    }

    private void wijzigTalent() {
        Talent geselecteerd = view.getTabel().getSelectionModel().getSelectedItem();
        if (geselecteerd == null) return;
        toonDialoog("Basistalent wijzigen", "Gegevens van " + geselecteerd.getNaam() + " wijzigen", geselecteerd)
                .ifPresent(invoer -> {
                    try {
                        talentService.wijzigTalent(geselecteerd, invoer.naam(), invoer.beschrijving());
                        herlaadEnSelecteer(geselecteerd);
                        view.toonStatus(gepoetsteNaam(geselecteerd) + " is gewijzigd.", false);
                    } catch (RuntimeException exception) {
                        view.toonStatus(boodschap(exception, "Het basistalent kon niet gewijzigd worden."), true);
                    }
                });
    }

    private Optional<TalentInvoer> toonDialoog(String titel, String header, Talent talent) {
        Dialog<TalentInvoer> dialoog = new Dialog<>();
        dialoog.setTitle(titel);
        dialoog.setHeaderText(header);
        dialoog.initOwner(scene.getWindow());
        dialoog.getDialogPane().getStyleClass().add("app-dialog");
        dialoog.getDialogPane().getStylesheets().setAll(scene.getStylesheets());
        ButtonType opslaan = new ButtonType("Opslaan", ButtonBar.ButtonData.OK_DONE);
        dialoog.getDialogPane().getButtonTypes().addAll(opslaan, ButtonType.CANCEL);

        TextField naam = new TextField(talent == null ? "" : talent.getNaam());
        naam.setPromptText("Naam van het basistalent");
        TextArea beschrijving = new TextArea(talent == null ? "" : talent.getBeschrijving());
        beschrijving.setPromptText("Beschrijving");
        beschrijving.setWrapText(true);
        beschrijving.setPrefRowCount(5);

        GridPane formulier = new GridPane();
        formulier.setHgap(12);
        formulier.setVgap(12);
        formulier.setPadding(new Insets(8, 0, 4, 0));
        formulier.addRow(0, new Label("Naam"), naam);
        formulier.addRow(1, new Label("Beschrijving"), beschrijving);
        dialoog.getDialogPane().setContent(formulier);
        dialoog.setResultConverter(knop -> knop == opslaan
                ? new TalentInvoer(naam.getText(), beschrijving.getText()) : null);
        return dialoog.showAndWait();
    }

    private void herlaadEnSelecteer(Talent talent) {
        alleTalenten = talentService.geefAlleTalenten().stream()
                .sorted(Comparator.comparing(Talent::getNaam, String.CASE_INSENSITIVE_ORDER))
                .toList();
        view.getZoekVeld().clear();
        filterTalenten();
        view.getTabel().getItems().stream()
                .filter(item -> item.getId() != null && item.getId().equals(talent.getId()))
                .findFirst().ifPresent(item -> view.getTabel().getSelectionModel().select(item));
    }

    private String gepoetsteNaam(Talent talent) {
        return talent.getNaam() == null ? "Het basistalent" : talent.getNaam();
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

    private record TalentInvoer(String naam, String beschrijving) { }
}
