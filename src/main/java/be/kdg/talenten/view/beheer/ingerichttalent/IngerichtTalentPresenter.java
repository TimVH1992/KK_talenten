package be.kdg.talenten.view.beheer.ingerichttalent;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.service.beheer.*;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.beheer.BeheerView;
import be.kdg.talenten.view.theme.ThemeManager;
import javafx.collections.FXCollections;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.*;

public class IngerichtTalentPresenter {
    private final IngerichtTalentView view;
    private final BeheerView beheerView;
    private final SceneManager sceneManager;
    private final ThemeManager themeManager;
    private final Scene scene;
    private final SchooljaarService schooljaarService;
    private final TalentenPeriodeService periodeService;
    private final IngerichtTalentService ingerichtTalentService;
    private final TalentService talentService;
    private final LeerkrachtService leerkrachtService;

    public IngerichtTalentPresenter(
            IngerichtTalentView view, BeheerView beheerView, SceneManager sceneManager,
            ThemeManager themeManager, Scene scene, SchooljaarService schooljaarService,
            TalentenPeriodeService periodeService, IngerichtTalentService ingerichtTalentService,
            TalentService talentService, LeerkrachtService leerkrachtService
    ) {
        if (view == null || beheerView == null || sceneManager == null || themeManager == null || scene == null
                || schooljaarService == null || periodeService == null || ingerichtTalentService == null
                || talentService == null || leerkrachtService == null) {
            throw new IllegalArgumentException("IngerichtTalentPresenter kreeg een null-afhankelijkheid");
        }
        this.view = view;
        this.beheerView = beheerView;
        this.sceneManager = sceneManager;
        this.themeManager = themeManager;
        this.scene = scene;
        this.schooljaarService = schooljaarService;
        this.periodeService = periodeService;
        this.ingerichtTalentService = ingerichtTalentService;
        this.talentService = talentService;
        this.leerkrachtService = leerkrachtService;
        configureer();
        laadSchooljaren();
    }

    private void configureer() {
        view.updateThemeIcon(themeManager.isDark());
        view.getTerugButton().setOnAction(event -> sceneManager.toon(beheerView));
        view.getThemeButton().setOnAction(event -> toggleTheme());
        configureerDoelgroepKeuze();
        view.getSchooljaarComboBox().valueProperty().addListener((o, oud, nieuw) -> laadPeriodes(nieuw));
        view.getPeriodeComboBox().valueProperty().addListener((o, oud, nieuw) -> laadIngerichteTalenten(null));
        view.getDoelgroepComboBox().valueProperty().addListener((o, oud, nieuw) -> laadIngerichteTalenten(null));
        view.getTabel().getSelectionModel().selectedItemProperty().addListener(
                (o, oud, nieuw) -> view.wijzigSelectieActies(nieuw));
        view.getToevoegenButton().setOnAction(event -> voegToe());
        view.getWijzigenButton().setOnAction(event -> wijzig());
        view.getActiefWijzigenButton().setOnAction(event -> wijzigActieveStatus());
    }

    private void configureerDoelgroepKeuze() {
        view.getDoelgroepComboBox().setConverter(new StringConverter<>() {
            @Override public String toString(Doelgroep doelgroep) {
                if (doelgroep == null) return "";
                return doelgroep == Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                        ? "Observatie / opleidingsfase / 1e graad A-B"
                        : "Kwalificatiefase / 2e graad A-B";
            }
            @Override public Doelgroep fromString(String string) { return null; }
        });
        view.getDoelgroepComboBox().setItems(FXCollections.observableArrayList(Doelgroep.values()));
        view.getDoelgroepComboBox().setValue(Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB);
    }

    private void laadSchooljaren() {
        view.verbergStatus();
        try {
            List<Schooljaar> jaren = schooljaarService.zoekAlleSchooljaren().stream()
                    .sorted(Comparator.comparing(Schooljaar::getStartDatum).reversed()).toList();
            view.getSchooljaarComboBox().setItems(FXCollections.observableArrayList(jaren));
            Schooljaar actief = schooljaarService.zoekActiefSchooljaar().orElse(null);
            Schooljaar selectie = actief == null ? jaren.stream().findFirst().orElse(null)
                    : jaren.stream().filter(j -> zelfdeSchooljaar(j, actief)).findFirst().orElse(actief);
            view.getSchooljaarComboBox().setValue(selectie);
            updateBeschikbaarheid();
        } catch (RuntimeException exception) {
            view.getSchooljaarComboBox().setItems(FXCollections.observableArrayList());
            view.toonStatus("De schooljaren konden niet geladen worden.", true);
            updateBeschikbaarheid();
        }
    }

    private void laadPeriodes(Schooljaar schooljaar) {
        view.getPeriodeComboBox().getSelectionModel().clearSelection();
        if (schooljaar == null) {
            view.getPeriodeComboBox().setItems(FXCollections.observableArrayList());
            laadIngerichteTalenten(null);
            updateBeschikbaarheid();
            return;
        }
        try {
            List<TalentenPeriode> periodes = periodeService.geefPeriodesVoorSchooljaar(schooljaar).stream()
                    .sorted(Comparator.comparing(TalentenPeriode::getStartDatum)).toList();
            view.getPeriodeComboBox().setItems(FXCollections.observableArrayList(periodes));
            view.getPeriodeComboBox().setValue(kiesPassendePeriode(periodes, LocalDate.now()));
            if (periodes.isEmpty()) laadIngerichteTalenten(null);
        } catch (RuntimeException exception) {
            view.getPeriodeComboBox().setItems(FXCollections.observableArrayList());
            view.toonStatus("De talentenperiodes konden niet geladen worden.", true);
        }
        updateBeschikbaarheid();
    }

    static TalentenPeriode kiesPassendePeriode(List<TalentenPeriode> periodes, LocalDate vandaag) {
        return periodes.stream()
                .filter(p -> !vandaag.isBefore(p.getStartDatum()) && !vandaag.isAfter(p.getEindDatum()))
                .findFirst()
                .orElseGet(() -> periodes.stream().filter(p -> p.getStartDatum().isAfter(vandaag))
                        .findFirst().orElse(periodes.isEmpty() ? null : periodes.getLast()));
    }

    private void laadIngerichteTalenten(IngerichtTalent teSelecteren) {
        view.verbergStatus();
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        Doelgroep doelgroep = view.getDoelgroepComboBox().getValue();
        if (periode == null || doelgroep == null) {
            view.getTabel().setItems(FXCollections.observableArrayList());
            view.toonAantal(0);
            updateBeschikbaarheid();
            return;
        }
        try {
            List<IngerichtTalent> gefilterd = filterOpDoelgroep(
                    ingerichtTalentService.geefIngerichteTalentenVoorPeriode(periode), doelgroep);
            view.getTabel().setItems(FXCollections.observableArrayList(gefilterd));
            view.toonAantal(gefilterd.size());
            if (teSelecteren != null) gefilterd.stream()
                    .filter(t -> t.getId() != null && t.getId().equals(teSelecteren.getId()))
                    .findFirst().ifPresent(t -> view.getTabel().getSelectionModel().select(t));
            view.getTabel().refresh();
        } catch (RuntimeException exception) {
            view.getTabel().setItems(FXCollections.observableArrayList());
            view.toonAantal(0);
            view.toonStatus("De ingerichte talenten konden niet geladen worden.", true);
        }
        updateBeschikbaarheid();
    }

    static List<IngerichtTalent> filterOpDoelgroep(List<IngerichtTalent> talenten, Doelgroep doelgroep) {
        return talenten.stream().filter(t -> t.getDoelgroep() == doelgroep)
                .sorted(Comparator.comparing(IngerichtTalent::getNaam)).toList();
    }

    private void updateBeschikbaarheid() {
        view.getToevoegenButton().setDisable(
                view.getPeriodeComboBox().getValue() == null || view.getDoelgroepComboBox().getValue() == null);
    }

    private void voegToe() {
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        Doelgroep doelgroep = view.getDoelgroepComboBox().getValue();
        if (periode == null || doelgroep == null) return;
        try {
            List<Talent> talenten = talentService.geefAlleTalenten().stream()
                    .sorted(Comparator.comparing(Talent::getNaam)).toList();
            List<Leerkracht> leerkrachten = beschikbareLeerkrachten(
                    leerkrachtService.geefActieveLeerkrachten(),
                    ingerichtTalentService.geefIngerichteTalentenVoorPeriode(periode),
                    null
            );
            toonDialoog("Talent inrichten", null, talenten, leerkrachten).ifPresent(invoer -> {
                try {
                    IngerichtTalent toegevoegd = ingerichtTalentService.maakIngerichtTalent(
                            invoer.talent(), periode, invoer.naam(), invoer.omschrijving(),
                            leesMaximumCapaciteit(invoer.capaciteit()), doelgroep, invoer.leerkrachten());
                    laadIngerichteTalenten(toegevoegd);
                    view.toonStatus(toegevoegd.getNaam() + " is ingericht.", false);
                } catch (RuntimeException exception) {
                    view.toonStatus(boodschap(exception, "Het talent kon niet ingericht worden."), true);
                }
            });
        } catch (RuntimeException exception) {
            view.toonStatus(boodschap(exception, "De formuliergegevens konden niet geladen worden."), true);
        }
    }

    private void wijzig() {
        IngerichtTalent geselecteerd = view.getTabel().getSelectionModel().getSelectedItem();
        if (geselecteerd == null) return;
        try {
            List<Leerkracht> leerkrachten = beschikbareLeerkrachten(
                    leerkrachtService.geefActieveLeerkrachten(),
                    ingerichtTalentService.geefIngerichteTalentenVoorPeriode(
                            geselecteerd.getTalentenPeriode()),
                    geselecteerd
            );
            toonDialoog("Ingericht talent wijzigen", geselecteerd, List.of(geselecteerd.getTalent()), leerkrachten)
                    .ifPresent(invoer -> {
                        try {
                            ingerichtTalentService.wijzigIngerichtTalent(geselecteerd, invoer.naam(),
                                    invoer.omschrijving(), leesMaximumCapaciteit(invoer.capaciteit()));
                            synchroniseerLeerkrachten(geselecteerd, invoer.leerkrachten());
                            laadIngerichteTalenten(geselecteerd);
                            view.toonStatus(geselecteerd.getNaam() + " is gewijzigd.", false);
                        } catch (RuntimeException exception) {
                            view.toonStatus(boodschap(exception, "Het ingerichte talent kon niet gewijzigd worden."), true);
                        }
                    });
        } catch (RuntimeException exception) {
            view.toonStatus(boodschap(exception, "De leerkrachten konden niet geladen worden."), true);
        }
    }

    private void synchroniseerLeerkrachten(IngerichtTalent talent, List<Leerkracht> gewenst) {
        for (Leerkracht bestaand : new ArrayList<>(talent.getLeerkrachten())) {
            if (!gewenst.contains(bestaand)) ingerichtTalentService.verwijderLeerkracht(talent, bestaand);
        }
        for (Leerkracht leerkracht : gewenst) {
            if (!talent.getLeerkrachten().contains(leerkracht))
                ingerichtTalentService.voegLeerkrachtToe(talent, leerkracht);
        }
    }

    static List<Leerkracht> beschikbareLeerkrachten(List<Leerkracht> actieveLeerkrachten,
                                                     List<IngerichtTalent> talentenInPeriode,
                                                     IngerichtTalent huidigTalent) {
        Set<Leerkracht> bezetBijAnderTalent = talentenInPeriode.stream()
                .filter(talent -> huidigTalent == null || !zelfdeTalent(talent, huidigTalent))
                .flatMap(talent -> talent.getLeerkrachten().stream())
                .collect(java.util.stream.Collectors.toSet());

        LinkedHashSet<Leerkracht> beschikbaar = actieveLeerkrachten.stream()
                .filter(leerkracht -> !bezetBijAnderTalent.contains(leerkracht))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (huidigTalent != null) beschikbaar.addAll(huidigTalent.getLeerkrachten());

        return beschikbaar.stream()
                .sorted(Comparator.comparing(Leerkracht::getAchternaam, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Leerkracht::getVoornaam, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private static boolean zelfdeTalent(IngerichtTalent eerste, IngerichtTalent tweede) {
        return eerste.getId() != null && tweede.getId() != null
                ? eerste.getId().equals(tweede.getId())
                : eerste == tweede;
    }

    static List<Leerkracht> filterEnSorteerLeerkrachten(List<Leerkracht> leerkrachten,
                                                        Set<Leerkracht> geselecteerd,
                                                        String zoekterm) {
        String term = zoekterm == null ? "" : zoekterm.trim().toLowerCase(Locale.ROOT);
        return leerkrachten.stream()
                .filter(leerkracht -> geselecteerd.contains(leerkracht)
                        || volledigeNaam(leerkracht).toLowerCase(Locale.ROOT).contains(term))
                .sorted(Comparator.<Leerkracht, Boolean>comparing(
                                leerkracht -> !geselecteerd.contains(leerkracht))
                        .thenComparing(Leerkracht::getAchternaam, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Leerkracht::getVoornaam, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private static String volledigeNaam(Leerkracht leerkracht) {
        return leerkracht.getVoornaam() + " " + leerkracht.getAchternaam();
    }

    private Optional<FormulierInvoer> toonDialoog(String titel, IngerichtTalent bestaand,
                                                    List<Talent> talenten, List<Leerkracht> leerkrachten) {
        Dialog<FormulierInvoer> dialoog = new Dialog<>();
        dialoog.setTitle(titel);
        dialoog.setHeaderText(titel + " in " + view.getPeriodeComboBox().getValue().getNaam());
        dialoog.initOwner(scene.getWindow());
        dialoog.getDialogPane().getStyleClass().add("app-dialog");
        dialoog.getDialogPane().getStylesheets().setAll(scene.getStylesheets());
        ButtonType opslaan = new ButtonType("Opslaan", ButtonBar.ButtonData.OK_DONE);
        dialoog.getDialogPane().getButtonTypes().addAll(opslaan, ButtonType.CANCEL);

        ComboBox<Talent> basis = new ComboBox<>(FXCollections.observableArrayList(talenten));
        basis.setMaxWidth(Double.MAX_VALUE);
        basis.setValue(bestaand == null ? talenten.stream().findFirst().orElse(null) : bestaand.getTalent());
        basis.setDisable(bestaand != null);
        TextField naam = new TextField(bestaand == null ? "" : bestaand.getNaam());
        naam.setPromptText(bestaand == null
                ? "Optioneel — automatisch OBS_ of KWA_ indien leeg"
                : "Naam van het ingerichte talent");
        TextArea omschrijving = new TextArea(bestaand == null ? "" : bestaand.getOmschrijving());
        omschrijving.setPrefRowCount(3);
        omschrijving.setWrapText(true);
        TextField capaciteit = new TextField(bestaand == null ? "" : Integer.toString(bestaand.getMaxCapaciteit()));
        Map<Leerkracht, BooleanProperty> geselecteerdeLeerkrachten = new LinkedHashMap<>();
        for (Leerkracht leerkracht : leerkrachten) {
            geselecteerdeLeerkrachten.put(
                    leerkracht,
                    new SimpleBooleanProperty(bestaand != null && bestaand.getLeerkrachten().contains(leerkracht))
            );
        }
        ListView<Leerkracht> leerkrachtLijst = new ListView<>(FXCollections.observableArrayList(leerkrachten));
        leerkrachtLijst.setPrefHeight(190);
        leerkrachtLijst.setCellFactory(CheckBoxListCell.forListView(
                geselecteerdeLeerkrachten::get,
                new StringConverter<>() {
                    @Override public String toString(Leerkracht leerkracht) {
                        return leerkracht == null ? "" : leerkracht.getVoornaam() + " " + leerkracht.getAchternaam();
                    }
                    @Override public Leerkracht fromString(String string) { return null; }
                }
        ));

        TextField leerkrachtZoeken = new TextField();
        leerkrachtZoeken.setPromptText("Zoek een leerkracht op naam");
        Runnable vernieuwLeerkrachten = () -> {
            Set<Leerkracht> aangevinkt = geselecteerdeLeerkrachten.entrySet().stream()
                    .filter(entry -> entry.getValue().get())
                    .map(Map.Entry::getKey)
                    .collect(java.util.stream.Collectors.toSet());
            leerkrachtLijst.getItems().setAll(filterEnSorteerLeerkrachten(
                    leerkrachten, aangevinkt, leerkrachtZoeken.getText()));
        };
        leerkrachtZoeken.textProperty().addListener((observable, oud, nieuw) -> vernieuwLeerkrachten.run());
        geselecteerdeLeerkrachten.values().forEach(property ->
                property.addListener((observable, oud, nieuw) -> vernieuwLeerkrachten.run()));
        vernieuwLeerkrachten.run();

        GridPane formulier = new GridPane();
        formulier.setHgap(12); formulier.setVgap(10); formulier.setPadding(new Insets(6));
        formulier.addRow(0, new Label("Basistalent"), basis);
        formulier.addRow(1, new Label("Naam"), naam);
        formulier.addRow(2, new Label("Omschrijving"), omschrijving);
        formulier.addRow(3, new Label("Maximumcapaciteit"), capaciteit);
        formulier.addRow(4, new Label("Doelgroep"), new Label(doelgroepTekst(view.getDoelgroepComboBox().getValue())));
        VBox leerkrachtKeuze = new VBox(5,
                new Label("Vink de gewenste leerkrachten aan. Leeg laten is toegestaan."),
                leerkrachtZoeken,
                leerkrachtLijst
        );
        formulier.addRow(5, new Label("Leerkracht(en), maximaal 2"), leerkrachtKeuze);
        dialoog.getDialogPane().setContent(formulier);
        dialoog.setResultConverter(knop -> knop == opslaan
                ? new FormulierInvoer(basis.getValue(), naam.getText(), omschrijving.getText(),
                capaciteit.getText(), geselecteerdeLeerkrachten.entrySet().stream()
                        .filter(entry -> entry.getValue().get())
                        .map(Map.Entry::getKey)
                        .toList()) : null);
        return dialoog.showAndWait();
    }

    private void wijzigActieveStatus() {
        IngerichtTalent talent = view.getTabel().getSelectionModel().getSelectedItem();
        if (talent == null) return;
        if (talent.isActief()) {
            Alert bevestiging = new Alert(Alert.AlertType.CONFIRMATION,
                    "Wilt u " + talent.getNaam() + " niet actief maken?", ButtonType.OK, ButtonType.CANCEL);
            bevestiging.setTitle("Ingericht talent deactiveren");
            bevestiging.setHeaderText("Bestaande toewijzingen worden volgens de huidige regels behandeld.");
            bevestiging.initOwner(scene.getWindow());
            if (bevestiging.showAndWait().filter(ButtonType.OK::equals).isEmpty()) return;
        }
        try {
            if (talent.isActief()) ingerichtTalentService.deactiveer(talent);
            else ingerichtTalentService.activeer(talent);
            laadIngerichteTalenten(talent);
            view.toonStatus(talent.getNaam() + " is " + (talent.isActief() ? "actief." : "niet actief."), false);
        } catch (RuntimeException exception) {
            view.toonStatus(boodschap(exception, "De status kon niet gewijzigd worden."), true);
        }
    }

    private String doelgroepTekst(Doelgroep doelgroep) {
        return doelgroep == Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                ? "Observatie / opleidingsfase / 1e graad A-B" : "Kwalificatiefase / 2e graad A-B";
    }
    private boolean zelfdeSchooljaar(Schooljaar a, Schooljaar b) {
        return a.getId() != null && b.getId() != null ? a.getId().equals(b.getId()) : a.equals(b);
    }
    private String boodschap(RuntimeException e, String standaard) {
        return e.getMessage() == null || e.getMessage().isBlank() ? standaard : e.getMessage();
    }
    static int leesMaximumCapaciteit(String invoer) {
        if (invoer == null || invoer.isBlank()) {
            throw new IllegalArgumentException("Geef een maximum_capaciteit mee");
        }
        try {
            return Integer.parseInt(invoer.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Geef een geldige maximum_capaciteit mee");
        }
    }
    private void toggleTheme() {
        themeManager.toggle(scene);
        boolean dark = themeManager.isDark();
        view.updateThemeIcon(dark);
        beheerView.updateThemeIcon(dark);
    }
    private record FormulierInvoer(Talent talent, String naam, String omschrijving,
                                    String capaciteit, List<Leerkracht> leerkrachten) {}
}
