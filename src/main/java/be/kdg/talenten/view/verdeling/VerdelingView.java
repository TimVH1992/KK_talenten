package be.kdg.talenten.view.verdeling;

import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.overzicht.IngerichtTalentOverzicht;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class VerdelingView extends BorderPane {
    private final DateTimeFormatter datumFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Button terugButton;
    private ComboBox<TalentenPeriode> periodeComboBox;
    private Button overzichtLadenButton;
    private Button automatischeVerdelingButton;
    private TableView<IngerichtTalentOverzicht> talentenTable;
    private TableView<Toewijzing> leerlingenTable;
    private ComboBox<IngerichtTalent> doelTalentComboBox;
    private Button verplaatsLeerlingButton;
    private Label statusLabel;

    public VerdelingView() {
        initialiseNodes();
        layoutNodes();
    }

    private void initialiseNodes() {
        terugButton = new Button("← Hoofdmenu");
        terugButton.getStyleClass().add("secondary-button");

        periodeComboBox = new ComboBox<>();
        periodeComboBox.setPromptText("Selecteer een talentenperiode");
        periodeComboBox.setPrefWidth(330);
        periodeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TalentenPeriode periode) {
                if (periode == null) return "";
                return periode.getNaam() + " — " + datumFormatter.format(periode.getStartDatum()) + " t.e.m. " + datumFormatter.format(periode.getEindDatum());
            }

            @Override
            public TalentenPeriode fromString(String string) {
                return null;
            }
        });

        overzichtLadenButton = new Button("Overzicht laden");
        overzichtLadenButton.getStyleClass().add("primary-button");

        automatischeVerdelingButton = new Button("Automatische verdeling uitvoeren");
        automatischeVerdelingButton.getStyleClass().add("warning-button");

        talentenTable = maakTalentenTable();
        leerlingenTable = maakLeerlingenTable();

        doelTalentComboBox = new ComboBox<>();
        doelTalentComboBox.setPromptText("Kies het nieuwe talent");
        doelTalentComboBox.setPrefWidth(300);
        doelTalentComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(IngerichtTalent ingerichtTalent) {
                if (ingerichtTalent == null) return "";
                return ingerichtTalent.getTalent().getNaam() + " — capaciteit " + ingerichtTalent.getMaxCapaciteit();
            }

            @Override
            public IngerichtTalent fromString(String string) {
                return null;
            }
        });

        verplaatsLeerlingButton = new Button("Leerling verplaatsen");
        verplaatsLeerlingButton.getStyleClass().add("primary-button");

        statusLabel = new Label("Selecteer een periode om de verdeling te bekijken.");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setWrapText(true);
    }

    private TableView<IngerichtTalentOverzicht> maakTalentenTable() {
        TableView<IngerichtTalentOverzicht> table = new TableView<>();
        table.setPlaceholder(new Label("Geen ingerichte talenten gevonden."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<IngerichtTalentOverzicht, String> talentKolom = new TableColumn<>("Talent");
        talentKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().ingerichtTalent().getTalent().getNaam()));

        TableColumn<IngerichtTalentOverzicht, String> doelgroepKolom = new TableColumn<>("Doelgroep");
        doelgroepKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatteerDoelgroep(data.getValue().ingerichtTalent())));

        TableColumn<IngerichtTalentOverzicht, Number> toegewezenKolom = new TableColumn<>("Toegewezen");
        toegewezenKolom.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().aantalToegewezen()));

        TableColumn<IngerichtTalentOverzicht, Number> capaciteitKolom = new TableColumn<>("Capaciteit");
        capaciteitKolom.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().ingerichtTalent().getMaxCapaciteit()));

        TableColumn<IngerichtTalentOverzicht, Number> vrijKolom = new TableColumn<>("Vrij");
        vrijKolom.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().aantalVrijePlaatsen()));

        table.getColumns().addAll(talentKolom, doelgroepKolom, toegewezenKolom, capaciteitKolom, vrijKolom);
        return table;
    }

    private TableView<Toewijzing> maakLeerlingenTable() {
        TableView<Toewijzing> table = new TableView<>();
        table.setPlaceholder(new Label("Selecteer links een talent."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Toewijzing, String> leerlingKolom = new TableColumn<>("Leerling");
        leerlingKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getLeerling().toString()));

        TableColumn<Toewijzing, String> klasKolom = new TableColumn<>("Klas");
        klasKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getLeerling().getKlas().getNaam()));

        TableColumn<Toewijzing, String> typeKolom = new TableColumn<>("Type");
        typeKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getToewijzingsType().name()));

        TableColumn<Toewijzing, String> voorkeurKolom = new TableColumn<>("Voorkeur");
        voorkeurKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getVoorkeurNummer() == null ? "—" : data.getValue().getVoorkeurNummer().toString()));

        table.getColumns().addAll(leerlingKolom, klasKolom, typeKolom, voorkeurKolom);
        return table;
    }

    private void layoutNodes() {
        Label titel = new Label("Talentenverdeling");
        titel.getStyleClass().add("screen-title");

        HBox titelBalk = new HBox(18, terugButton, titel);
        titelBalk.setAlignment(Pos.CENTER_LEFT);

        HBox selectieBalk = new HBox(12, new Label("Periode:"), periodeComboBox, overzichtLadenButton, automatischeVerdelingButton);
        selectieBalk.setAlignment(Pos.CENTER_LEFT);
        selectieBalk.getStyleClass().add("toolbar");

        VBox bovenkant = new VBox(16, titelBalk, selectieBalk);
        bovenkant.setPadding(new Insets(24, 24, 12, 24));
        setTop(bovenkant);

        VBox talentenPaneel = maakPaneel("Ingerichte talenten", talentenTable);
        VBox leerlingenPaneel = maakPaneel("Toegewezen leerlingen", leerlingenTable);

        SplitPane splitPane = new SplitPane(talentenPaneel, leerlingenPaneel);
        splitPane.setDividerPositions(0.48);
        splitPane.setPadding(new Insets(0, 24, 12, 24));
        setCenter(splitPane);

        Label handmatigeTitel = new Label("Manuele wijziging");
        handmatigeTitel.getStyleClass().add("section-title");

        Label uitleg = new Label("Selecteer een leerling in de rechtertabel en kies daarna een nieuw ingericht talent.");
        uitleg.getStyleClass().add("muted-label");

        HBox wijzigingBalk = new HBox(12, doelTalentComboBox, verplaatsLeerlingButton);
        wijzigingBalk.setAlignment(Pos.CENTER_LEFT);

        VBox onderkant = new VBox(8, handmatigeTitel, uitleg, wijzigingBalk, statusLabel);
        onderkant.setPadding(new Insets(12, 24, 24, 24));
        onderkant.getStyleClass().add("bottom-panel");
        setBottom(onderkant);

        getStyleClass().add("app-background");
    }

    private <T> VBox maakPaneel(String titelTekst, TableView<T> table) {
        Label titel = new Label(titelTekst);
        titel.getStyleClass().add("section-title");
        VBox paneel = new VBox(10, titel, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        paneel.setPadding(new Insets(16));
        paneel.getStyleClass().add("content-card");
        return paneel;
    }

    private String formatteerDoelgroep(IngerichtTalent ingerichtTalent) {
        return switch (ingerichtTalent.getDoelgroep()) {
            case OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB -> "Observatie / opleidingsfase";
            case KWALIFICATIEFASE_TWEEDEGRAAD_AB -> "Kwalificatiefase";
        };
    }

    public void setPeriodes(List<TalentenPeriode> periodes) {
        periodeComboBox.setItems(FXCollections.observableArrayList(periodes));
    }

    public void setOverzichten(List<IngerichtTalentOverzicht> overzichten) {
        talentenTable.setItems(FXCollections.observableArrayList(overzichten));
        doelTalentComboBox.setItems(FXCollections.observableArrayList(overzichten.stream().map(IngerichtTalentOverzicht::ingerichtTalent).toList()));
    }

    public void setToewijzingen(List<Toewijzing> toewijzingen) {
        leerlingenTable.setItems(FXCollections.observableArrayList(toewijzingen));
    }

    public void toonMelding(String melding) {
        statusLabel.setText(melding);
        statusLabel.getStyleClass().removeAll("status-error", "status-success");
    }

    public void toonSucces(String melding) {
        statusLabel.setText(melding);
        statusLabel.getStyleClass().remove("status-error");
        if (!statusLabel.getStyleClass().contains("status-success")) statusLabel.getStyleClass().add("status-success");
    }

    public void toonFout(String melding) {
        statusLabel.setText(melding);
        statusLabel.getStyleClass().remove("status-success");
        if (!statusLabel.getStyleClass().contains("status-error")) statusLabel.getStyleClass().add("status-error");
    }

    public boolean vraagBevestiging(String titel, String boodschap) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titel);
        alert.setHeaderText(titel);
        alert.setContentText(boodschap);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    public Button getTerugButton() {
        return terugButton;
    }

    public ComboBox<TalentenPeriode> getPeriodeComboBox() {
        return periodeComboBox;
    }

    public Button getOverzichtLadenButton() {
        return overzichtLadenButton;
    }

    public Button getAutomatischeVerdelingButton() {
        return automatischeVerdelingButton;
    }

    public TableView<IngerichtTalentOverzicht> getTalentenTable() {
        return talentenTable;
    }

    public TableView<Toewijzing> getLeerlingenTable() {
        return leerlingenTable;
    }

    public ComboBox<IngerichtTalent> getDoelTalentComboBox() {
        return doelTalentComboBox;
    }

    public Button getVerplaatsLeerlingButton() {
        return verplaatsLeerlingButton;
    }
}
