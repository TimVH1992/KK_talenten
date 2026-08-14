package be.kdg.talenten.view.talentenperiode;

import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.view.navigation.AppSidebar;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class TalentenPeriodeView extends BorderPane {
    private static final DateTimeFormatter DATUM_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AppSidebar sidebar;

    private final Label actiefSchooljaarLabel;
    private final TableView<Schooljaar> schooljarenTable;
    private final Button volgendSchooljaarButton;
    private final Button actiefMakenButton;

    private final Label geselecteerdSchooljaarLabel;
    private final Button nieuwePeriodeButton;
    private final TableView<TalentenPeriode> periodesTable;

    private final Label formulierTitelLabel;
    private final Label formulierSchooljaarLabel;
    private final TextField naamField;
    private final DatePicker startDatumPicker;
    private final DatePicker eindDatumPicker;
    private final Button verwijderenPeriodeButton;
    private final Button annulerenPeriodeButton;
    private final Button opslaanPeriodeButton;

    private final Label statusLabel;

    public TalentenPeriodeView() {
        sidebar = new AppSidebar(AppSidebar.Sectie.TALENTENPERIODES, "Schooljaren & periodes");

        actiefSchooljaarLabel = new Label("Actief schooljaar wordt geladen...");
        actiefSchooljaarLabel.getStyleClass().add("context-pill");

        schooljarenTable = maakSchooljarenTable();

        volgendSchooljaarButton = new Button("+ Volgend schooljaar");
        volgendSchooljaarButton.getStyleClass().add("primary-outline-button");

        actiefMakenButton = new Button("Actief maken");
        actiefMakenButton.getStyleClass().add("primary-button");
        actiefMakenButton.setDisable(true);

        geselecteerdSchooljaarLabel = new Label("Selecteer een schooljaar");
        geselecteerdSchooljaarLabel.getStyleClass().add("section-title");

        nieuwePeriodeButton = new Button("+ Nieuwe periode");
        nieuwePeriodeButton.getStyleClass().add("primary-button");
        nieuwePeriodeButton.setDisable(true);

        periodesTable = maakPeriodesTable();

        formulierTitelLabel = new Label("Nieuwe talentenperiode");
        formulierTitelLabel.getStyleClass().add("section-title");

        formulierSchooljaarLabel = new Label("-");
        formulierSchooljaarLabel.getStyleClass().add("muted-label");

        naamField = new TextField();
        naamField.setPromptText("Bijvoorbeeld Herfst");

        startDatumPicker = new DatePicker();
        startDatumPicker.setPromptText("Startdatum");
        startDatumPicker.setMaxWidth(Double.MAX_VALUE);

        eindDatumPicker = new DatePicker();
        eindDatumPicker.setPromptText("Einddatum");
        eindDatumPicker.setMaxWidth(Double.MAX_VALUE);

        verwijderenPeriodeButton = new Button("Verwijderen");
        verwijderenPeriodeButton.getStyleClass().add("secondary-button");
        verwijderenPeriodeButton.setVisible(false);
        verwijderenPeriodeButton.setManaged(false);

        annulerenPeriodeButton = new Button("Annuleren");
        annulerenPeriodeButton.getStyleClass().add("secondary-button");

        opslaanPeriodeButton = new Button("Opslaan");
        opslaanPeriodeButton.getStyleClass().add("primary-button");
        opslaanPeriodeButton.setDisable(true);

        statusLabel = new Label("Selecteer een schooljaar om de talentenperiodes te beheren.");
        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-label");

        layoutNodes();
    }

    private TableView<Schooljaar> maakSchooljarenTable() {
        TableView<Schooljaar> table = new TableView<>();
        table.setPlaceholder(new Label("Nog geen schooljaren beschikbaar."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Schooljaar, String> naamKolom = new TableColumn<>("Schooljaar");
        naamKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNaam()));
        naamKolom.setPrefWidth(115);

        TableColumn<Schooljaar, String> periodeKolom = new TableColumn<>("Periode");
        periodeKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getStartDatum().format(DATUM_FORMATTER) + " – " + data.getValue().getEindDatum().format(DATUM_FORMATTER)
        ));
        periodeKolom.setPrefWidth(180);

        TableColumn<Schooljaar, String> statusKolom = new TableColumn<>("Status");
        statusKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().isActief() ? "ACTIEF" : ""));
        statusKolom.setPrefWidth(80);

        table.getColumns().addAll(naamKolom, periodeKolom, statusKolom);
        return table;
    }

    private TableView<TalentenPeriode> maakPeriodesTable() {
        TableView<TalentenPeriode> table = new TableView<>();
        table.setPlaceholder(new Label("Nog geen talentenperiodes voor dit schooljaar."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(300);

        TableColumn<TalentenPeriode, String> naamKolom = new TableColumn<>("Naam");
        naamKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNaam()));
        naamKolom.setPrefWidth(180);

        TableColumn<TalentenPeriode, String> startKolom = new TableColumn<>("Startdatum");
        startKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStartDatum().format(DATUM_FORMATTER)));
        startKolom.setPrefWidth(125);

        TableColumn<TalentenPeriode, String> eindKolom = new TableColumn<>("Einddatum");
        eindKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEindDatum().format(DATUM_FORMATTER)));
        eindKolom.setPrefWidth(125);

        table.getColumns().addAll(naamKolom, startKolom, eindKolom);
        return table;
    }

    private void layoutNodes() {
        setLeft(sidebar);

        Label titel = new Label("Talentenperiodes & schooljaren");
        titel.getStyleClass().add("page-title");

        Label ondertitel = new Label("Plan schooljaren en beheer de periodes waarin talenten worden ingericht en verdeeld.");
        ondertitel.getStyleClass().add("page-subtitle");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox header = new HBox(14, new VBox(4, titel, ondertitel), headerSpacer, actiefSchooljaarLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox schooljarenCard = maakSchooljarenCard();
        VBox periodesCard = maakPeriodesCard();

        HBox hoofdInhoud = new HBox(18, schooljarenCard, periodesCard);
        HBox.setHgrow(periodesCard, Priority.ALWAYS);
        VBox.setVgrow(hoofdInhoud, Priority.ALWAYS);

        VBox inhoud = new VBox(18, header, hoofdInhoud, statusLabel);
        inhoud.setPadding(new Insets(28));
        VBox.setVgrow(hoofdInhoud, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(inhoud);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("app-background");

        setCenter(scrollPane);
        getStyleClass().add("app-background");
    }

    private VBox maakSchooljarenCard() {
        Label titel = new Label("Schooljaren");
        titel.getStyleClass().add("section-title");

        Label uitleg = new Label("Schooljaren worden automatisch opeenvolgend aangemaakt. Je hoeft naam of datums niet zelf in te geven.");
        uitleg.setWrapText(true);
        uitleg.getStyleClass().add("muted-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox knoppen = new HBox(10, actiefMakenButton, spacer, volgendSchooljaarButton);
        knoppen.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12, titel, uitleg, schooljarenTable, knoppen);
        card.getStyleClass().add("content-card");
        card.setPadding(new Insets(20));
        card.setPrefWidth(410);
        card.setMinWidth(365);
        VBox.setVgrow(schooljarenTable, Priority.ALWAYS);
        return card;
    }

    private VBox maakPeriodesCard() {
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox cardHeader = new HBox(12, geselecteerdSchooljaarLabel, headerSpacer, nieuwePeriodeButton);
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        VBox formulier = maakPeriodeFormulier();

        VBox card = new VBox(14, cardHeader, periodesTable, new Separator(), formulier);
        card.getStyleClass().add("content-card");
        card.setPadding(new Insets(20));
        card.setMinWidth(520);
        VBox.setVgrow(periodesTable, Priority.ALWAYS);
        return card;
    }

    private VBox maakPeriodeFormulier() {
        Label schooljaarTitel = maakVeldLabel("Schooljaar");
        Label naamLabel = maakVeldLabel("Naam");
        Label startLabel = maakVeldLabel("Startdatum");
        Label eindLabel = maakVeldLabel("Einddatum");

        VBox startBox = new VBox(5, startLabel, startDatumPicker);
        VBox eindBox = new VBox(5, eindLabel, eindDatumPicker);
        HBox.setHgrow(startBox, Priority.ALWAYS);
        HBox.setHgrow(eindBox, Priority.ALWAYS);
        startDatumPicker.setMaxWidth(Double.MAX_VALUE);
        eindDatumPicker.setMaxWidth(Double.MAX_VALUE);
        HBox datums = new HBox(12, startBox, eindBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox knoppen = new HBox(10, verwijderenPeriodeButton, spacer, annulerenPeriodeButton, opslaanPeriodeButton);
        knoppen.setAlignment(Pos.CENTER_RIGHT);

        return new VBox(9,
                formulierTitelLabel,
                schooljaarTitel, formulierSchooljaarLabel,
                naamLabel, naamField,
                datums,
                knoppen
        );
    }

    private Label maakVeldLabel(String tekst) {
        Label label = new Label(tekst);
        label.getStyleClass().add("field-label");
        return label;
    }

    public void setSchooljaren(List<Schooljaar> schooljaren) {
        schooljarenTable.setItems(FXCollections.observableArrayList(schooljaren == null ? List.of() : schooljaren));
        schooljarenTable.refresh();
    }

    public void setActiefSchooljaar(Schooljaar schooljaar) {
        actiefSchooljaarLabel.setText(schooljaar == null ? "Geen actief schooljaar" : "Actief: " + schooljaar.getNaam());
    }

    public void setGeselecteerdSchooljaar(Schooljaar schooljaar) {
        if (schooljaar == null) {
            geselecteerdSchooljaarLabel.setText("Selecteer een schooljaar");
            actiefMakenButton.setDisable(true);
            nieuwePeriodeButton.setDisable(true);
            opslaanPeriodeButton.setDisable(true);
            formulierSchooljaarLabel.setText("-");
            return;
        }

        geselecteerdSchooljaarLabel.setText("Talentenperiodes · " + schooljaar.getNaam());
        actiefMakenButton.setDisable(schooljaar.isActief());
        nieuwePeriodeButton.setDisable(false);
        opslaanPeriodeButton.setDisable(false);
        formulierSchooljaarLabel.setText(schooljaar.getNaam());
    }

    public void setPeriodes(Schooljaar schooljaar, List<TalentenPeriode> periodes) {
        setGeselecteerdSchooljaar(schooljaar);
        periodesTable.setItems(FXCollections.observableArrayList(periodes == null ? List.of() : periodes));
        periodesTable.refresh();
    }

    public void toonNieuwePeriodeFormulier(Schooljaar schooljaar) {
        formulierTitelLabel.setText("Nieuwe talentenperiode");
        formulierSchooljaarLabel.setText(schooljaar == null ? "-" : schooljaar.getNaam());
        naamField.clear();
        startDatumPicker.setValue(null);
        eindDatumPicker.setValue(null);
        verwijderenPeriodeButton.setVisible(false);
        verwijderenPeriodeButton.setManaged(false);
        opslaanPeriodeButton.setDisable(schooljaar == null);
        periodesTable.getSelectionModel().clearSelection();
    }

    public void toonWijzigPeriodeFormulier(TalentenPeriode periode) {
        if (periode == null) return;

        formulierTitelLabel.setText("Talentenperiode wijzigen");
        formulierSchooljaarLabel.setText(periode.getSchooljaar().getNaam());
        naamField.setText(periode.getNaam());
        startDatumPicker.setValue(periode.getStartDatum());
        eindDatumPicker.setValue(periode.getEindDatum());
        verwijderenPeriodeButton.setVisible(true);
        verwijderenPeriodeButton.setManaged(true);
        opslaanPeriodeButton.setDisable(false);
    }

    public boolean vraagBevestiging(String titel, String bericht) {
        Alert alert = maakAlert(Alert.AlertType.CONFIRMATION, titel, bericht);
        ButtonType resultaat = alert.showAndWait().orElse(ButtonType.CANCEL);
        return resultaat == ButtonType.OK;
    }

    public void toonFout(String bericht) {
        setStatus(bericht);
        statusLabel.getStyleClass().removeAll("status-success", "status-warning", "status-error");
        statusLabel.getStyleClass().add("status-error");
        maakAlert(Alert.AlertType.ERROR, "Fout", bericht).showAndWait();
    }

    public void toonSucces(String bericht) {
        setStatus(bericht);
        statusLabel.getStyleClass().removeAll("status-success", "status-warning", "status-error");
        statusLabel.getStyleClass().add("status-success");
    }

    public void setStatus(String bericht) {
        statusLabel.setText(bericht == null ? "" : bericht);
        statusLabel.getStyleClass().removeAll("status-success", "status-warning", "status-error");
    }

    private Alert maakAlert(Alert.AlertType type, String titel, String bericht) {
        Alert alert = new Alert(type);
        alert.setTitle(titel);
        alert.setHeaderText(null);

        Label label = new Label(bericht == null ? "" : bericht);
        label.setWrapText(true);
        label.setMaxWidth(500);
        label.setPrefWidth(460);
        alert.getDialogPane().setContent(label);
        alert.getDialogPane().setMinWidth(520);
        return alert;
    }

    public AppSidebar getSidebar() { return sidebar; }
    public TableView<Schooljaar> getSchooljarenTable() { return schooljarenTable; }
    public Button getVolgendSchooljaarButton() { return volgendSchooljaarButton; }
    public Button getActiefMakenButton() { return actiefMakenButton; }
    public Button getNieuwePeriodeButton() { return nieuwePeriodeButton; }
    public TableView<TalentenPeriode> getPeriodesTable() { return periodesTable; }
    public TextField getNaamField() { return naamField; }
    public DatePicker getStartDatumPicker() { return startDatumPicker; }
    public DatePicker getEindDatumPicker() { return eindDatumPicker; }
    public Button getVerwijderenPeriodeButton() { return verwijderenPeriodeButton; }
    public Button getAnnulerenPeriodeButton() { return annulerenPeriodeButton; }
    public Button getOpslaanPeriodeButton() { return opslaanPeriodeButton; }
}
