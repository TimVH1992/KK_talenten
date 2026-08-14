package be.kdg.talenten.view.klas;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.view.navigation.AppSidebar;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Map;

public class KlasView extends BorderPane {
    private final AppSidebar sidebar;

    private final Label schooljaarLabel;
    private final Button nieuweKlasButton;
    private final TableView<Klas> klassenTable;
    private Map<Long, Integer> leerlingAantallen = Map.of();

    private final Label formulierTitelLabel;
    private final TextField naamField;
    private final ComboBox<Integer> leerjaarComboBox;
    private final ComboBox<Doelgroep> doelgroepComboBox;
    private final Label formulierSchooljaarLabel;
    private final Button verwijderenButton;
    private final Button annulerenButton;
    private final Button opslaanButton;

    private final Label leerlingenTitelLabel;
    private final TableView<Leerling> leerlingenTable;
    private final TextArea plakArea;
    private final Button previewButton;
    private final Button leerlingenToevoegenButton;
    private final TextArea previewArea;
    private final Label statusLabel;

    public KlasView() {
        sidebar = new AppSidebar(AppSidebar.Sectie.KLASSEN, "Klassen en leerlingen");

        schooljaarLabel = new Label("Actief schooljaar wordt geladen...");
        schooljaarLabel.getStyleClass().add("context-pill");

        nieuweKlasButton = new Button("+ Nieuwe klas");
        nieuweKlasButton.getStyleClass().add("primary-button");

        klassenTable = maakKlassenTable();

        formulierTitelLabel = new Label("Nieuwe klas");
        formulierTitelLabel.getStyleClass().add("section-title");

        naamField = new TextField();
        naamField.setPromptText("Bijvoorbeeld 1AA");

        leerjaarComboBox = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7));
        leerjaarComboBox.setPromptText("Kies leerjaar");
        leerjaarComboBox.setMaxWidth(Double.MAX_VALUE);

        doelgroepComboBox = new ComboBox<>(FXCollections.observableArrayList(Doelgroep.values()));
        doelgroepComboBox.setPromptText("Kies doelgroep");
        doelgroepComboBox.setMaxWidth(Double.MAX_VALUE);
        doelgroepComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Doelgroep doelgroep) {
                return doelgroep == null ? "" : toonDoelgroep(doelgroep);
            }

            @Override
            public Doelgroep fromString(String string) {
                return null;
            }
        });

        formulierSchooljaarLabel = new Label("-");
        formulierSchooljaarLabel.getStyleClass().add("muted-label");

        verwijderenButton = new Button("Verwijderen");
        verwijderenButton.getStyleClass().add("secondary-button");
        annulerenButton = new Button("Annuleren");
        annulerenButton.getStyleClass().add("secondary-button");
        opslaanButton = new Button("Opslaan");
        opslaanButton.getStyleClass().add("primary-button");

        leerlingenTitelLabel = new Label("Leerlingen");
        leerlingenTitelLabel.getStyleClass().add("section-title");
        leerlingenTable = maakLeerlingenTable();

        plakArea = new TextArea();
        plakArea.setPromptText("Plak hier twee kolommen uit Excel:\nVoornaam    Achternaam\nJan         Peeters\nSofie       Janssens");
        plakArea.setWrapText(false);
        plakArea.setPrefRowCount(6);

        previewButton = new Button("Voorbeeld bekijken");
        previewButton.getStyleClass().add("secondary-button");
        leerlingenToevoegenButton = new Button("Leerlingen toevoegen");
        leerlingenToevoegenButton.getStyleClass().add("primary-button");
        leerlingenToevoegenButton.setDisable(true);

        previewArea = new TextArea();
        previewArea.setEditable(false);
        previewArea.setWrapText(true);
        previewArea.setPrefRowCount(5);
        previewArea.setPromptText("De controle van de geplakte leerlingen verschijnt hier.");

        statusLabel = new Label("Selecteer een klas of maak een nieuwe klas aan.");
        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-label");

        layoutNodes();
    }

    private TableView<Klas> maakKlassenTable() {
        TableView<Klas> table = new TableView<>();
        table.setPlaceholder(new Label("Nog geen klassen voor het actieve schooljaar."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Klas, String> naamKolom = new TableColumn<>("Klas");
        naamKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNaam()));
        naamKolom.setPrefWidth(110);

        TableColumn<Klas, Number> leerjaarKolom = new TableColumn<>("Leerjaar");
        leerjaarKolom.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().getLeerjaar()));
        leerjaarKolom.setPrefWidth(90);

        TableColumn<Klas, String> doelgroepKolom = new TableColumn<>("Doelgroep");
        doelgroepKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(toonDoelgroep(data.getValue().getDoelgroep())));
        doelgroepKolom.setPrefWidth(190);

        TableColumn<Klas, Number> leerlingenKolom = new TableColumn<>("Leerlingen");
        leerlingenKolom.setCellValueFactory(data -> {
            Long id = data.getValue().getId();
            return new ReadOnlyIntegerWrapper(id == null ? 0 : leerlingAantallen.getOrDefault(id, 0));
        });
        leerlingenKolom.setPrefWidth(95);

        table.getColumns().addAll(naamKolom, leerjaarKolom, doelgroepKolom, leerlingenKolom);
        return table;
    }

    private TableView<Leerling> maakLeerlingenTable() {
        TableView<Leerling> table = new TableView<>();
        table.setPlaceholder(new Label("Deze klas bevat nog geen leerlingen."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(210);

        TableColumn<Leerling, String> voornaamKolom = new TableColumn<>("Voornaam");
        voornaamKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getVoornaam()));

        TableColumn<Leerling, String> achternaamKolom = new TableColumn<>("Achternaam");
        achternaamKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAchternaam()));

        table.getColumns().addAll(voornaamKolom, achternaamKolom);
        return table;
    }

    private void layoutNodes() {
        setLeft(sidebar);

        Label titel = new Label("Klassen beheren");
        titel.getStyleClass().add("page-title");
        Label ondertitel = new Label("Maak klassen aan, pas ze aan en voeg leerlingen rechtstreeks toe vanuit Excel.");
        ondertitel.getStyleClass().add("page-subtitle");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox header = new HBox(14, new VBox(4, titel, ondertitel), headerSpacer, schooljaarLabel, nieuweKlasButton);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox overzichtCard = new VBox(12, maakSectieTitel("Klassen in het actieve schooljaar"), klassenTable);
        overzichtCard.getStyleClass().add("content-card");
        overzichtCard.setPadding(new Insets(20));
        VBox.setVgrow(klassenTable, Priority.ALWAYS);

        VBox klasFormulier = maakKlasFormulier();
        VBox leerlingenCard = maakLeerlingenCard();

        VBox rechterKolom = new VBox(16, klasFormulier, leerlingenCard);
        rechterKolom.setPrefWidth(530);
        VBox.setVgrow(leerlingenCard, Priority.ALWAYS);

        HBox hoofdInhoud = new HBox(18, overzichtCard, rechterKolom);
        HBox.setHgrow(overzichtCard, Priority.ALWAYS);
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

    private VBox maakKlasFormulier() {
        Label naamLabel = maakVeldLabel("Naam");
        Label leerjaarLabel = maakVeldLabel("Leerjaar");
        Label doelgroepLabel = maakVeldLabel("Doelgroep");
        Label schooljaarTitel = maakVeldLabel("Schooljaar");

        HBox knoppen = new HBox(10, verwijderenButton, maakSpacer(), annulerenButton, opslaanButton);
        knoppen.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(knoppen.getChildren().get(1), Priority.ALWAYS);

        VBox card = new VBox(10,
                formulierTitelLabel,
                naamLabel, naamField,
                leerjaarLabel, leerjaarComboBox,
                doelgroepLabel, doelgroepComboBox,
                schooljaarTitel, formulierSchooljaarLabel,
                new Separator(),
                knoppen
        );
        card.getStyleClass().add("content-card");
        card.setPadding(new Insets(20));
        return card;
    }

    private VBox maakLeerlingenCard() {
        Label uitleg = new Label("Selecteer in Excel de kolommen Voornaam en Achternaam, kopieer ze en plak ze hieronder. Een eventuele koprij wordt automatisch overgeslagen.");
        uitleg.setWrapText(true);
        uitleg.getStyleClass().add("muted-label");

        HBox actieKnoppen = new HBox(10, previewButton, leerlingenToevoegenButton);
        actieKnoppen.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(10,
                leerlingenTitelLabel,
                leerlingenTable,
                maakVeldLabel("Leerlingen uit Excel plakken"),
                uitleg,
                plakArea,
                actieKnoppen,
                previewArea
        );
        card.getStyleClass().add("content-card");
        card.setPadding(new Insets(20));
        VBox.setVgrow(leerlingenTable, Priority.ALWAYS);
        return card;
    }

    private Label maakSectieTitel(String tekst) {
        Label label = new Label(tekst);
        label.getStyleClass().add("section-title");
        return label;
    }

    private Label maakVeldLabel(String tekst) {
        Label label = new Label(tekst);
        label.getStyleClass().add("field-label");
        return label;
    }

    private Region maakSpacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private String toonDoelgroep(Doelgroep doelgroep) {
        if (doelgroep == null) return "";
        return switch (doelgroep) {
            case OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB -> "Observatie / opleidingsfase / 1e graad A-B";
            case KWALIFICATIEFASE_TWEEDEGRAAD_AB -> "Kwalificatiefase / 2e graad A-B";
        };
    }

    public void setActiefSchooljaar(Schooljaar schooljaar) {
        String tekst = schooljaar == null ? "Geen actief schooljaar" : "Schooljaar " + schooljaar.getNaam();
        schooljaarLabel.setText(tekst);
        formulierSchooljaarLabel.setText(schooljaar == null ? "-" : schooljaar.getNaam() + " (actief)");
    }

    public void setKlassen(List<Klas> klassen, Map<Long, Integer> aantallen) {
        leerlingAantallen = aantallen == null ? Map.of() : Map.copyOf(aantallen);
        klassenTable.getItems().setAll(klassen == null ? List.of() : klassen);
        klassenTable.refresh();
    }

    public void setLeerlingen(Klas klas, List<Leerling> leerlingen) {
        leerlingenTitelLabel.setText(klas == null ? "Leerlingen" : "Leerlingen — " + klas.getNaam());
        leerlingenTable.getItems().setAll(leerlingen == null ? List.of() : leerlingen);
        leerlingenTable.refresh();
    }

    public void toonNieuweKlasFormulier(Schooljaar schooljaar) {
        formulierTitelLabel.setText("Nieuwe klas");
        naamField.clear();
        leerjaarComboBox.getSelectionModel().clearSelection();
        doelgroepComboBox.getSelectionModel().clearSelection();
        formulierSchooljaarLabel.setText(schooljaar == null ? "-" : schooljaar.getNaam() + " (actief)");
        verwijderenButton.setVisible(false);
        verwijderenButton.setManaged(false);
        klassenTable.getSelectionModel().clearSelection();
        setLeerlingImportBeschikbaar(false);
        setLeerlingen(null, List.of());
        wisPlakInvoer();
        naamField.requestFocus();
    }

    public void toonKlasFormulier(Klas klas) {
        formulierTitelLabel.setText("Klas wijzigen");
        naamField.setText(klas.getNaam());
        leerjaarComboBox.getSelectionModel().select(Integer.valueOf(klas.getLeerjaar()));
        doelgroepComboBox.getSelectionModel().select(klas.getDoelgroep());
        formulierSchooljaarLabel.setText(klas.getSchooljaar().getNaam());
        verwijderenButton.setVisible(true);
        verwijderenButton.setManaged(true);
        setLeerlingImportBeschikbaar(true);
        wisPlakInvoer();
    }

    public void setLeerlingImportBeschikbaar(boolean beschikbaar) {
        plakArea.setDisable(!beschikbaar);
        previewButton.setDisable(!beschikbaar);
        if (!beschikbaar) {
            leerlingenToevoegenButton.setDisable(true);
        }
    }

    public void setPreview(String tekst, boolean opslaanToegestaan) {
        previewArea.setText(tekst == null ? "" : tekst);
        leerlingenToevoegenButton.setDisable(!opslaanToegestaan);
    }

    public void wisPlakInvoer() {
        plakArea.clear();
        previewArea.clear();
        leerlingenToevoegenButton.setDisable(true);
    }

    public void setStatus(String tekst) {
        statusLabel.setText(tekst == null ? "" : tekst);
        statusLabel.getStyleClass().removeAll("status-success", "status-warning", "status-error");
    }

    public void toonSucces(String tekst) {
        setStatus(tekst);
        statusLabel.getStyleClass().add("status-success");
    }

    public void toonFout(String tekst) {
        setStatus(tekst);
        statusLabel.getStyleClass().add("status-error");

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Klassen beheren");
        alert.setHeaderText("De actie kon niet uitgevoerd worden");
        alert.setContentText(tekst);
        alert.showAndWait();
    }

    public boolean vraagBevestiging(String titel, String tekst) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titel);
        alert.setHeaderText(titel);
        alert.setContentText(tekst);
        ButtonType annuleren = new ButtonType("Annuleren", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType bevestigen = new ButtonType("Verwijderen", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(annuleren, bevestigen);
        return alert.showAndWait().filter(bevestigen::equals).isPresent();
    }

    public AppSidebar getSidebar() { return sidebar; }
    public Label getSchooljaarLabel() { return schooljaarLabel; }
    public Button getNieuweKlasButton() { return nieuweKlasButton; }
    public TableView<Klas> getKlassenTable() { return klassenTable; }
    public TextField getNaamField() { return naamField; }
    public ComboBox<Integer> getLeerjaarComboBox() { return leerjaarComboBox; }
    public ComboBox<Doelgroep> getDoelgroepComboBox() { return doelgroepComboBox; }
    public Button getVerwijderenButton() { return verwijderenButton; }
    public Button getAnnulerenButton() { return annulerenButton; }
    public Button getOpslaanButton() { return opslaanButton; }
    public TextArea getPlakArea() { return plakArea; }
    public Button getPreviewButton() { return previewButton; }
    public Button getLeerlingenToevoegenButton() { return leerlingenToevoegenButton; }
}
