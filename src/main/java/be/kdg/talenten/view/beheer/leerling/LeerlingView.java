package be.kdg.talenten.view.beheer.leerling;

import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Schooljaar;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LeerlingView extends BorderPane {
    private final Button terugButton = new Button("← Terug naar beheer");
    private final Button themeButton = new Button("☾");
    private final ComboBox<Schooljaar> schooljaarComboBox = new ComboBox<>();
    private final ComboBox<Klas> klasComboBox = new ComboBox<>();
    private final TextField zoekVeld = new TextField();
    private final TableView<Leerling> tabel = new TableView<>();
    private final Button toevoegenButton = new Button("+ Leerling toevoegen");
    private final TextArea plakVeld = new TextArea();
    private final Button validerenButton = new Button("Valideren");
    private final Button bulkToevoegenButton = new Button("Leerlingen toevoegen");
    private final Label validatieLabel = new Label();
    private final Button wijzigenButton = new Button("Wijzigen");
    private final Button klasWijzigenButton = new Button("Klas wijzigen");
    private final Button actiefWijzigenButton = new Button("Deactiveren");
    private final Label zichtbaarheidLabel = new Label();
    private final Label statusLabel = new Label();

    public LeerlingView() {
        getStyleClass().add("app-root");
        setTop(maakHeader());
        setCenter(maakInhoud());
        configureerTabel();
    }

    private Pane maakHeader() {
        BorderPane header = new BorderPane();
        header.getStyleClass().add("section-header");
        header.setPadding(new Insets(18, 28, 18, 28));

        terugButton.getStyleClass().add("secondary-button");
        header.setLeft(terugButton);

        Label titel = new Label("Individuele leerling wijzigen");
        titel.getStyleClass().add("section-title");
        header.setCenter(titel);

        themeButton.getStyleClass().add("theme-toggle");
        themeButton.setTooltip(new Tooltip("Wissel tussen lichte en donkere modus"));
        header.setRight(themeButton);
        return header;
    }

    private Pane maakInhoud() {
        VBox inhoud = new VBox(22);
        inhoud.setPadding(new Insets(34, 60, 42, 60));
        inhoud.setAlignment(Pos.TOP_CENTER);

        VBox intro = new VBox(5);
        intro.setMaxWidth(1080);
        Label titel = new Label("Individuele leerling wijzigen");
        titel.getStyleClass().add("content-title");
        Label uitleg = new Label(
                "Beheer leerlingen, hun actieve status en klaswissels binnen een gekozen schooljaar."
        );
        uitleg.setWrapText(true);
        uitleg.getStyleClass().add("content-subtitle");
        intro.getChildren().addAll(titel, uitleg);

        HBox filters = new HBox(14);
        filters.setMaxWidth(1080);
        filters.setAlignment(Pos.BOTTOM_LEFT);

        VBox schooljaarKeuze = new VBox(7);
        Label schooljaarLabel = new Label("Schooljaar");
        schooljaarLabel.getStyleClass().add("field-label");
        schooljaarComboBox.setEditable(false);
        schooljaarComboBox.setMaxWidth(Double.MAX_VALUE);
        schooljaarComboBox.setPromptText("Selecteer een schooljaar");
        schooljaarKeuze.getChildren().addAll(schooljaarLabel, schooljaarComboBox);

        VBox klasKeuze = new VBox(7);
        Label klasLabel = new Label("Klas");
        klasLabel.getStyleClass().add("field-label");
        klasComboBox.setEditable(false);
        klasComboBox.setMaxWidth(Double.MAX_VALUE);
        klasComboBox.setPromptText("Selecteer een klas");
        klasKeuze.getChildren().addAll(klasLabel, klasComboBox);

        VBox zoeken = new VBox(7);
        Label zoekLabel = new Label("Leerling zoeken");
        zoekLabel.getStyleClass().add("field-label");
        zoekVeld.setPromptText("Zoek op voor- of achternaam");
        zoeken.getChildren().addAll(zoekLabel, zoekVeld);

        HBox.setHgrow(schooljaarKeuze, Priority.ALWAYS);
        HBox.setHgrow(klasKeuze, Priority.ALWAYS);
        HBox.setHgrow(zoeken, Priority.ALWAYS);
        schooljaarKeuze.setMaxWidth(Double.MAX_VALUE);
        klasKeuze.setMaxWidth(Double.MAX_VALUE);
        zoeken.setMaxWidth(Double.MAX_VALUE);
        filters.getChildren().addAll(schooljaarKeuze, klasKeuze, zoeken);

        VBox tabelKaart = new VBox(14);
        tabelKaart.setMaxWidth(1080);
        tabelKaart.setPadding(new Insets(20));
        tabelKaart.getStyleClass().add("content-card");
        VBox.setVgrow(tabelKaart, Priority.ALWAYS);

        HBox tabelHeader = new HBox(12);
        tabelHeader.setAlignment(Pos.CENTER_LEFT);
        Label tabelTitel = new Label("Leerlingen in het schooljaar");
        tabelTitel.getStyleClass().add("card-section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        wijzigenButton.getStyleClass().add("secondary-button");
        klasWijzigenButton.getStyleClass().add("secondary-button");
        actiefWijzigenButton.getStyleClass().add("secondary-button");
        wijzigSelectieActies(false, true);
        toevoegenButton.getStyleClass().add("primary-button");
        tabelHeader.getChildren().addAll(
                tabelTitel,
                spacer,
                wijzigenButton,
                klasWijzigenButton,
                actiefWijzigenButton,
                toevoegenButton
        );

        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabel.setPlaceholder(new Label("Geen leerlingen gevonden."));
        tabel.setMinHeight(265);
        tabel.setPrefHeight(265);
        tabel.setMaxHeight(265);
        VBox.setVgrow(tabel, Priority.ALWAYS);

        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-message");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        zichtbaarheidLabel.setWrapText(true);
        zichtbaarheidLabel.getStyleClass().add("content-subtitle");

        tabelKaart.getChildren().addAll(tabelHeader, tabel, zichtbaarheidLabel, statusLabel);
        inhoud.getChildren().addAll(intro, filters, tabelKaart);
        return inhoud;
    }

    private VBox maakPlakKaart() {
        VBox kaart = new VBox(10);
        kaart.setMaxWidth(1080);
        kaart.setPadding(new Insets(16, 20, 16, 20));
        kaart.getStyleClass().add("content-card");

        Label titel = new Label("Leerlingen plakken vanuit Excel");
        titel.getStyleClass().add("card-section-title");
        Label uitleg = new Label(
                "Selecteer eerst een klas en plak daarna twee Excel-kolommen: Voornaam en Achternaam."
        );
        uitleg.setWrapText(true);
        uitleg.getStyleClass().add("content-subtitle");

        plakVeld.setPromptText("Plak hier de Excelkolommen Voornaam | Achternaam");
        plakVeld.setPrefRowCount(3);
        plakVeld.setWrapText(false);

        HBox acties = new HBox(10);
        acties.setAlignment(Pos.CENTER_RIGHT);
        validatieLabel.setWrapText(true);
        validatieLabel.getStyleClass().add("content-subtitle");
        HBox.setHgrow(validatieLabel, Priority.ALWAYS);
        validerenButton.getStyleClass().add("secondary-button");
        bulkToevoegenButton.getStyleClass().add("primary-button");
        bulkToevoegenButton.setDisable(true);
        acties.getChildren().addAll(validatieLabel, validerenButton, bulkToevoegenButton);

        kaart.getChildren().addAll(titel, uitleg, plakVeld, acties);
        return kaart;
    }

    private void configureerTabel() {
        TableColumn<Leerling, String> voornaamKolom = new TableColumn<>("Voornaam");
        voornaamKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getVoornaam())
        );

        TableColumn<Leerling, String> achternaamKolom = new TableColumn<>("Achternaam");
        achternaamKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getAchternaam())
        );

        TableColumn<Leerling, String> klasKolom = new TableColumn<>("Klas");
        klasKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getKlas().getNaam())
        );

        TableColumn<Leerling, String> leerjaarKolom = new TableColumn<>("Leerjaar");
        leerjaarKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(Integer.toString(data.getValue().getKlas().getLeerjaar()))
        );

        TableColumn<Leerling, String> statusKolom = new TableColumn<>("Status");
        statusKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().isActief() ? "Neemt deel" : "Inactief")
        );
        statusKolom.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("status-active", "status-inactive");
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item);
                getStyleClass().add(item.equals("Neemt deel") ? "status-active" : "status-inactive");
            }
        });

        voornaamKolom.setMinWidth(180);
        achternaamKolom.setMinWidth(210);
        klasKolom.setMinWidth(130);
        leerjaarKolom.setMinWidth(110);
        statusKolom.setMinWidth(120);
        tabel.getColumns().addAll(
                voornaamKolom, achternaamKolom, klasKolom, leerjaarKolom, statusKolom
        );
    }

    public void wijzigSelectieActies(boolean geselecteerd, boolean actief) {
        wijzigenButton.setDisable(!geselecteerd);
        klasWijzigenButton.setDisable(!geselecteerd);
        actiefWijzigenButton.setDisable(!geselecteerd);
        actiefWijzigenButton.setText(actief ? "Deactiveren" : "Activeren");
    }

    public Button getTerugButton() { return terugButton; }
    public Button getThemeButton() { return themeButton; }
    public ComboBox<Schooljaar> getSchooljaarComboBox() { return schooljaarComboBox; }
    public ComboBox<Klas> getKlasComboBox() { return klasComboBox; }
    public TextField getZoekVeld() { return zoekVeld; }
    public TableView<Leerling> getTabel() { return tabel; }
    public Button getToevoegenButton() { return toevoegenButton; }
    public TextArea getPlakVeld() { return plakVeld; }
    public Button getValiderenButton() { return validerenButton; }
    public Button getBulkToevoegenButton() { return bulkToevoegenButton; }
    public Button getWijzigenButton() { return wijzigenButton; }
    public Button getKlasWijzigenButton() { return klasWijzigenButton; }
    public Button getActiefWijzigenButton() { return actiefWijzigenButton; }

    public void toonAantalLeerlingen(int aantal) {
        if (aantal > 5) {
            zichtbaarheidLabel.setText(
                    "5 van " + aantal + " leerlingen zichtbaar — scroll om de overige leerlingen te bekijken."
            );
        } else if (aantal == 1) {
            zichtbaarheidLabel.setText("1 leerling zichtbaar.");
        } else {
            zichtbaarheidLabel.setText(aantal + " leerlingen zichtbaar.");
        }
    }

    public void toonPlakValidatie(String boodschap, boolean fout, boolean kanOpslaan) {
        validatieLabel.setText(boodschap);
        validatieLabel.getStyleClass().removeAll("status-success", "status-error");
        validatieLabel.getStyleClass().add(fout ? "status-error" : "status-success");
        bulkToevoegenButton.setDisable(!kanOpslaan);
    }

    public void toonStatus(String boodschap, boolean fout) {
        statusLabel.setText(boodschap);
        statusLabel.getStyleClass().removeAll("status-success", "status-error");
        statusLabel.getStyleClass().add(fout ? "status-error" : "status-success");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    public void verbergStatus() {
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }

    public void updateThemeIcon(boolean darkMode) {
        themeButton.setText(darkMode ? "☀" : "☾");
    }
}
