package be.kdg.talenten.view.verdeling;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.overzicht.IngerichtTalentOverzicht;
import be.kdg.talenten.overzicht.NietToegewezenLeerlingOverzicht;
import be.kdg.talenten.overzicht.LeerlingToewijzingOverzicht;
import be.kdg.talenten.overzicht.LeerlingDetailsOverzicht;
import be.kdg.talenten.service.beheer.KlasService;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.service.beheer.TalentenPeriodeService;
import be.kdg.talenten.service.leerling.LeerlingDetailsService;
import be.kdg.talenten.service.verdeling.AutomatischeVerdelingService;
import be.kdg.talenten.service.verdeling.ManueleToewijzingService;
import be.kdg.talenten.service.verdeling.VerdelingBekijkenService;
import be.kdg.talenten.verdeling.VerdelingsResultaat;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.main.MainView;
import be.kdg.talenten.view.theme.ThemeManager;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class VerdelingPresenter {
    private final VerdelingView view;
    private final MainView mainView;
    private final SceneManager sceneManager;
    private final ThemeManager themeManager;
    private final Scene scene;
    private final SchooljaarService schooljaarService;
    private final TalentenPeriodeService periodeService;
    private final KlasService klasService;
    private final AutomatischeVerdelingService automatischeService;
    private final ManueleToewijzingService manueleService;
    private final VerdelingBekijkenService bekijkenService;
    private final LeerlingDetailsService leerlingDetailsService;
    private List<IngerichtTalentOverzicht> talentOverzichten = List.of();
    private final AtomicLong laadVersie = new AtomicLong();

    public VerdelingPresenter(VerdelingView view, MainView mainView, SceneManager sceneManager,
                              ThemeManager themeManager, Scene scene, SchooljaarService schooljaarService,
                              TalentenPeriodeService periodeService, KlasService klasService,
                              AutomatischeVerdelingService automatischeService,
                              ManueleToewijzingService manueleService, VerdelingBekijkenService bekijkenService,
                              LeerlingDetailsService leerlingDetailsService) {
        if (view == null || mainView == null || sceneManager == null || themeManager == null || scene == null
                || schooljaarService == null || periodeService == null || klasService == null || automatischeService == null
                || manueleService == null || bekijkenService == null || leerlingDetailsService == null) {
            throw new IllegalArgumentException("VerdelingPresenter kreeg een null-afhankelijkheid");
        }
        this.view = view; this.mainView = mainView; this.sceneManager = sceneManager;
        this.themeManager = themeManager; this.scene = scene; this.schooljaarService = schooljaarService;
        this.periodeService = periodeService; this.automatischeService = automatischeService;
        this.klasService = klasService;
        this.manueleService = manueleService; this.bekijkenService = bekijkenService;
        this.leerlingDetailsService = leerlingDetailsService;
        configureer();
        laadSchooljaren();
    }

    private void configureer() {
        view.updateThemeIcon(themeManager.isDark());
        view.getTerugButton().setOnAction(e -> sceneManager.toon(mainView));
        view.getThemeButton().setOnAction(e -> toggleTheme());
        configureerDoelgroep();
        view.getSchooljaarComboBox().valueProperty().addListener((o, oud, nieuw) -> laadPeriodes(nieuw));
        view.getPeriodeComboBox().valueProperty().addListener((o, oud, nieuw) -> laadOverzicht());
        view.getDoelgroepComboBox().valueProperty().addListener((o, oud, nieuw) -> laadOverzicht());
        view.getTalentTabel().getSelectionModel().selectedItemProperty().addListener((o, oud, nieuw) -> toonTalentDetail(nieuw));
        view.getKlasComboBox().valueProperty().addListener((o, oud, nieuw) -> laadKlasOverzicht(nieuw));
        view.getLeerlingTabel().getSelectionModel().selectedItemProperty().addListener((o, oud, nieuw) -> {
            if (nieuw != null) {
                view.getNietToegewezenTabel().getSelectionModel().clearSelection();
                view.getKlasTabel().getSelectionModel().clearSelection();
            }
            updateHandmatigKnop();
        });
        view.getNietToegewezenTabel().getSelectionModel().selectedItemProperty().addListener((o, oud, nieuw) -> {
            if (nieuw != null) {
                view.getLeerlingTabel().getSelectionModel().clearSelection();
                view.getKlasTabel().getSelectionModel().clearSelection();
            }
            updateHandmatigKnop();
        });
        view.getKlasTabel().getSelectionModel().selectedItemProperty().addListener((o, oud, nieuw) -> {
            if (nieuw != null) {
                view.getLeerlingTabel().getSelectionModel().clearSelection();
                view.getNietToegewezenTabel().getSelectionModel().clearSelection();
            }
            updateHandmatigKnop();
        });
        view.getAutomatischButton().setOnAction(e -> voerAutomatischeVerdelingUit());
        view.getHandmatigButton().setOnAction(e -> wijsHandmatigToe());
    }

    private void configureerDoelgroep() {
        view.getDoelgroepComboBox().setConverter(new StringConverter<>() {
            @Override public String toString(Doelgroep d) {
                if (d == null) return "";
                return d == Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                        ? "Observatie / opleidingsfase / 1e graad A-B"
                        : "Kwalificatiefase / 2e graad A-B";
            }
            @Override public Doelgroep fromString(String s) { return null; }
        });
        view.getDoelgroepComboBox().setItems(FXCollections.observableArrayList(Doelgroep.values()));
        view.getDoelgroepComboBox().setValue(Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB);
    }

    private void laadSchooljaren() {
        try {
            List<Schooljaar> jaren = schooljaarService.zoekAlleSchooljaren().stream()
                    .sorted(Comparator.comparing(Schooljaar::getStartDatum).reversed()).toList();
            view.getSchooljaarComboBox().setItems(FXCollections.observableArrayList(jaren));
            Schooljaar actief = schooljaarService.zoekActiefSchooljaar().orElse(jaren.isEmpty() ? null : jaren.getFirst());
            view.getSchooljaarComboBox().setValue(actief);
        } catch (RuntimeException e) { view.toonStatus(boodschap(e, "De schooljaren konden niet geladen worden."), true); }
    }

    private void laadPeriodes(Schooljaar schooljaar) {
        view.getPeriodeComboBox().getSelectionModel().clearSelection();
        if (schooljaar == null) { view.getPeriodeComboBox().getItems().clear(); laadOverzicht(); return; }
        try {
            List<TalentenPeriode> periodes = periodeService.geefPeriodesVoorSchooljaar(schooljaar).stream()
                    .sorted(Comparator.comparing(TalentenPeriode::getStartDatum)).toList();
            view.getPeriodeComboBox().setItems(FXCollections.observableArrayList(periodes));
            view.getPeriodeComboBox().setValue(kiesPassendePeriode(periodes, LocalDate.now()));
        } catch (RuntimeException e) { view.toonStatus(boodschap(e, "De periodes konden niet geladen worden."), true); }
    }

    static TalentenPeriode kiesPassendePeriode(List<TalentenPeriode> periodes, LocalDate vandaag) {
        return periodes.stream().filter(p -> !vandaag.isBefore(p.getStartDatum()) && !vandaag.isAfter(p.getEindDatum()))
                .findFirst().orElseGet(() -> periodes.stream().filter(p -> p.getStartDatum().isAfter(vandaag))
                        .findFirst().orElse(periodes.isEmpty() ? null : periodes.getLast()));
    }

    private void laadOverzicht() {
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        Doelgroep doelgroep = view.getDoelgroepComboBox().getValue();
        view.getAutomatischButton().setDisable(periode == null || doelgroep == null);
        if (periode == null || doelgroep == null) { maakOverzichtLeeg(); return; }
        long huidigeVersie = laadVersie.incrementAndGet();
        Task<OverzichtData> taak = new Task<>() {
            @Override protected OverzichtData call() {
                List<IngerichtTalentOverzicht> talenten = bekijkenService.bekijkPerIngerichtTalent(periode, doelgroep);
                List<NietToegewezenLeerlingOverzicht> nietToegewezen =
                        bekijkenService.bekijkNietToegewezenLeerlingen(periode, doelgroep);
                List<Klas> klassen = zoekKlassen(periode.getSchooljaar(), doelgroep);
                return new OverzichtData(talenten, nietToegewezen, klassen);
            }
        };
        taak.setOnSucceeded(event -> {
            if (huidigeVersie != laadVersie.get()) return;
            OverzichtData data = taak.getValue();
            talentOverzichten = data.talenten();
            view.getTalentTabel().setItems(FXCollections.observableArrayList(data.talenten()));
            view.getNietToegewezenTabel().setItems(FXCollections.observableArrayList(data.nietToegewezen()));
            toonKlassen(data.klassen(), periode);
            int toegewezen = data.talenten().stream().mapToInt(IngerichtTalentOverzicht::aantalToegewezen).sum();
            view.toonAantallen(toegewezen + data.nietToegewezen().size(), toegewezen,
                    data.nietToegewezen().size(), data.talenten().size());
            if (!data.talenten().isEmpty()) view.getTalentTabel().getSelectionModel().selectFirst();
            else toonTalentDetail(null);
            updateHandmatigKnop();
        });
        taak.setOnFailed(event -> {
            if (huidigeVersie != laadVersie.get()) return;
            maakOverzichtLeeg();
            Throwable oorzaak = taak.getException();
            view.toonStatus(boodschap(oorzaak instanceof RuntimeException runtime
                    ? runtime : new IllegalStateException(oorzaak), "De verdeling kon niet geladen worden."), true);
        });
        Thread thread = new Thread(taak, "verdeling-overzicht-laden");
        thread.setDaemon(true);
        thread.start();
    }

    private void maakOverzichtLeeg() {
        talentOverzichten = List.of();
        view.getTalentTabel().getItems().clear(); view.getLeerlingTabel().getItems().clear();
        view.getNietToegewezenTabel().getItems().clear(); view.getKlasTabel().getItems().clear();
        view.getKlasComboBox().getItems().clear(); view.toonAantallen(0, 0, 0, 0);
        toonTalentDetail(null); updateHandmatigKnop();
    }

    private void toonTalentDetail(IngerichtTalentOverzicht overzicht) {
        view.getDetailTitel().setText(overzicht == null ? "Selecteer een talent"
                : overzicht.ingerichtTalent().getNaam() + " — " + overzicht.aantalToegewezen()
                + "/" + overzicht.ingerichtTalent().getMaxCapaciteit());
        view.getLeerlingTabel().setItems(FXCollections.observableArrayList(
                overzicht == null ? List.of() : overzicht.toewijzingen()));
    }

    private List<Klas> zoekKlassen(Schooljaar schooljaar, Doelgroep doelgroep) {
        return klasService.geefAlleKlassen().stream()
                .filter(klas -> zelfdeSchooljaar(klas.getSchooljaar(), schooljaar))
                .filter(klas -> klas.getDoelgroep() == doelgroep)
                .sorted(Comparator.comparing(Klas::getNaam, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private void toonKlassen(List<Klas> klassen, TalentenPeriode periode) {
        Klas huidigeSelectie = view.getKlasComboBox().getValue();
        view.getKlasComboBox().setItems(FXCollections.observableArrayList(klassen));
        Klas selectie = klassen.stream().filter(klas -> zelfdeKlas(klas, huidigeSelectie))
                .findFirst().orElse(klassen.isEmpty() ? null : klassen.getFirst());
        view.getKlasComboBox().setValue(selectie);
        if (selectie == null) view.getKlasTabel().getItems().clear();
        else laadKlasOverzicht(selectie);
    }

    private void laadKlasOverzicht(Klas klas) {
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        if (klas == null || periode == null) { view.getKlasTabel().getItems().clear(); return; }
        try {
            view.getKlasTabel().setItems(FXCollections.observableArrayList(
                    bekijkenService.bekijkVoorKlas(periode, klas).leerlingen()));
        } catch (RuntimeException e) {
            view.getKlasTabel().getItems().clear();
            view.toonStatus(boodschap(e, "Het klasoverzicht kon niet geladen worden."), true);
        }
    }

    private boolean zelfdeSchooljaar(Schooljaar eerste, Schooljaar tweede) {
        return eerste != null && tweede != null && eerste.getId() != null && tweede.getId() != null
                ? eerste.getId().equals(tweede.getId()) : eerste == tweede;
    }

    private boolean zelfdeKlas(Klas eerste, Klas tweede) {
        return eerste != null && tweede != null && eerste.getId() != null && tweede.getId() != null
                ? eerste.getId().equals(tweede.getId()) : eerste == tweede;
    }

    private void voerAutomatischeVerdelingUit() {
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        Doelgroep doelgroep = view.getDoelgroepComboBox().getValue();
        if (periode == null || doelgroep == null) return;
        try {
            AutomatischeVerdelingService.VoorkeurenDekking dekking =
                    automatischeService.bepaalVoorkeurenDekking(periode, doelgroep);
            boolean bestaandeToewijzingen = automatischeService
                    .heeftBestaandeToewijzingen(periode, doelgroep);
            StringBuilder melding = new StringBuilder()
                    .append(dekking.leerlingenMetVolledigeVoorkeuren())
                    .append(" van de ")
                    .append(dekking.totaalLeerlingen())
                    .append(" leerlingen hebben drie opgeslagen voorkeuren.");
            if (dekking.leerlingenZonderVolledigeVoorkeuren() > 0) {
                melding.append(System.lineSeparator())
                        .append(dekking.leerlingenZonderVolledigeVoorkeuren())
                        .append(dekking.leerlingenZonderVolledigeVoorkeuren() == 1
                                ? " leerling heeft geen of onvolledige voorkeuren."
                                : " leerlingen hebben geen of onvolledige voorkeuren.");
            }
            if (bestaandeToewijzingen) {
                melding.append(System.lineSeparator()).append(System.lineSeparator())
                        .append("Bestaande automatische toewijzingen voor deze doelgroep worden vervangen.");
            }
            Alert bevestiging = new Alert(Alert.AlertType.CONFIRMATION,
                    melding.toString(), ButtonType.OK, ButtonType.CANCEL);
            bevestiging.setTitle("Automatische verdeling uitvoeren");
            bevestiging.setHeaderText("Wilt u de automatische verdeling uitvoeren?");
            bevestiging.initOwner(scene.getWindow());
            bevestiging.getDialogPane().getStylesheets().setAll(scene.getStylesheets());
            if (bevestiging.showAndWait().filter(ButtonType.OK::equals).isEmpty()) return;

            VerdelingsResultaat resultaat = automatischeService.voerAutomatischeVerdelingUit(periode, doelgroep);
            laadOverzicht();
            view.toonStatus(resultaat.getAantalToewijzingen() + " leerlingen automatisch toegewezen · "
                    + resultaat.getNietToegewezenLeerlingen().size() + " niet toegewezen · "
                    + resultaat.getImportProblemen().size() + " importproblemen", false);
        } catch (RuntimeException e) { view.toonStatus(boodschap(e, "De automatische verdeling is niet uitgevoerd."), true); }
    }

    private void wijsHandmatigToe() {
        Leerling leerling = geselecteerdeLeerling();
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        if (leerling == null || periode == null) return;

        LeerlingDetailsOverzicht details;
        try {
            details = leerlingDetailsService.bekijk(leerling, periode);
        } catch (RuntimeException e) {
            view.toonStatus(boodschap(e, "De voorkeuren en historiek konden niet geladen worden."), true);
            return;
        }

        Dialog<IngerichtTalentOverzicht> dialoog = new Dialog<>();
        dialoog.setTitle("Handmatig toewijzen");
        dialoog.setHeaderText(leerling.getVoornaam() + " " + leerling.getAchternaam()
                + " · " + leerling.getKlas().getNaam());
        dialoog.initOwner(scene.getWindow());
        dialoog.getDialogPane().getStyleClass().add("app-dialog");
        dialoog.getDialogPane().getStylesheets().setAll(scene.getStylesheets());
        ButtonType opslaan = new ButtonType("Toewijzen", ButtonBar.ButtonData.OK_DONE);
        dialoog.getDialogPane().getButtonTypes().addAll(opslaan, ButtonType.CANCEL);

        Label voorkeurTitel = new Label("Opgeslagen voorkeuren voor " + periode.getNaam());
        voorkeurTitel.getStyleClass().add("field-label");
        ListView<String> voorkeurLijst = new ListView<>();
        voorkeurLijst.setMaxHeight(112);
        voorkeurLijst.setPlaceholder(new Label("Geen voorkeuren opgeslagen voor deze periode."));
        voorkeurLijst.setItems(FXCollections.observableArrayList(details.voorkeuren().stream()
                .sorted(Comparator.comparingInt(Voorkeur::getVoorkeurNummer))
                .map(voorkeur -> "Voorkeur " + voorkeur.getVoorkeurNummer() + " — "
                        + voorkeur.getIngerichtTalent().getNaam())
                .toList()));

        Label historiekTitel = new Label("Eerdere toewijzingen in " + periode.getSchooljaar().getNaam());
        historiekTitel.getStyleClass().add("field-label");
        ListView<String> historiekLijst = new ListView<>();
        historiekLijst.setMaxHeight(140);
        historiekLijst.setPlaceholder(new Label("Geen eerdere toewijzingen in dit schooljaar."));
        historiekLijst.setItems(FXCollections.observableArrayList(details.historischeToewijzingen().stream()
                .map(toewijzing -> {
                    String voorkeur = toewijzing.getVoorkeurNummer() == null
                            ? "geen voorkeur"
                            : "voorkeur " + toewijzing.getVoorkeurNummer();
                    String type = toewijzing.getToewijzingsType() == ToewijzingsType.MANUEEL
                            ? "manueel" : "automatisch";
                    return toewijzing.getIngerichtTalent().getTalentenPeriode().getNaam()
                            + " — " + toewijzing.getIngerichtTalent().getNaam()
                            + " · " + type + " · " + voorkeur;
                })
                .toList()));

        Label talentTitel = new Label("Nieuw ingericht talent");
        talentTitel.getStyleClass().add("field-label");
        ComboBox<IngerichtTalentOverzicht> talent = new ComboBox<>(FXCollections.observableArrayList(talentOverzichten));
        talent.setMaxWidth(Double.MAX_VALUE);
        talent.setPromptText("Selecteer een ingericht talent");
        talent.setConverter(new StringConverter<>() {
            @Override public String toString(IngerichtTalentOverzicht o) {
                return o == null ? "" : o.ingerichtTalent().getNaam() + " — " + o.aantalToegewezen()
                        + "/" + o.ingerichtTalent().getMaxCapaciteit();
            }
            @Override public IngerichtTalentOverzicht fromString(String s) { return null; }
        });

        VBox inhoud = new VBox(8,
                voorkeurTitel, voorkeurLijst,
                historiekTitel, historiekLijst,
                talentTitel, talent
        );
        inhoud.setPrefWidth(680);
        dialoog.getDialogPane().setContent(inhoud);
        Button toewijzenButton = (Button) dialoog.getDialogPane().lookupButton(opslaan);
        toewijzenButton.disableProperty().bind(talent.valueProperty().isNull());
        dialoog.setResultConverter(knop -> knop == opslaan ? talent.getValue() : null);
        dialoog.showAndWait().ifPresent(keuze -> {
            try {
                manueleService.wijzigToewijzing(periode, leerling, keuze.ingerichtTalent());
                laadOverzicht();
                view.toonStatus(leerling.getVoornaam() + " " + leerling.getAchternaam()
                        + " is toegewezen aan " + keuze.ingerichtTalent().getNaam() + ".", false);
            } catch (RuntimeException e) { view.toonStatus(boodschap(e, "De leerling kon niet toegewezen worden."), true); }
        });
    }

    private Leerling geselecteerdeLeerling() {
        Toewijzing toewijzing = view.getLeerlingTabel().getSelectionModel().getSelectedItem();
        if (toewijzing != null) return toewijzing.getLeerling();
        LeerlingToewijzingOverzicht klasLeerling = view.getKlasTabel().getSelectionModel().getSelectedItem();
        if (klasLeerling != null) return klasLeerling.leerling();
        NietToegewezenLeerlingOverzicht niet = view.getNietToegewezenTabel().getSelectionModel().getSelectedItem();
        return niet == null ? null : niet.leerling();
    }

    private void updateHandmatigKnop() { view.getHandmatigButton().setDisable(geselecteerdeLeerling() == null); }
    private String boodschap(RuntimeException e, String standaard) {
        return e.getMessage() == null || e.getMessage().isBlank() ? standaard : e.getMessage();
    }
    private void toggleTheme() {
        themeManager.toggle(scene); boolean dark = themeManager.isDark();
        view.updateThemeIcon(dark); mainView.updateThemeIcon(dark);
    }

    private record OverzichtData(List<IngerichtTalentOverzicht> talenten,
                                 List<NietToegewezenLeerlingOverzicht> nietToegewezen,
                                 List<Klas> klassen) { }
}
