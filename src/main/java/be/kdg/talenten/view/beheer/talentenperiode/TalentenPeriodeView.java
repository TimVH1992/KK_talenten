package be.kdg.talenten.view.beheer.talentenperiode;

import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TalentenPeriodeView extends BorderPane {
    private static final DateTimeFormatter DATUM_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Button terugButton = new Button("← Terug naar beheer");
    private final Button themeButton = new Button("☾");
    private final ComboBox<Schooljaar> schooljaarComboBox = new ComboBox<>();
    private final TableView<TalentenPeriode> tabel = new TableView<>();
    private final Button toevoegenButton = new Button("+ Periode toevoegen");
    private final Button wijzigenButton = new Button("Wijzigen");
    private final Button verwijderenButton = new Button("Verwijderen");
    private final Label statusLabel = new Label();

    public TalentenPeriodeView() {
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

        Label titel = new Label("Talentenperiodes");
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
        intro.setMaxWidth(980);
        Label titel = new Label("Talentenperiodes beheren");
        titel.getStyleClass().add("content-title");
        Label uitleg = new Label("Beheer de periodes waarin talenten binnen een gekozen schooljaar worden georganiseerd.");
        uitleg.setWrapText(true);
        uitleg.getStyleClass().add("content-subtitle");
        intro.getChildren().addAll(titel, uitleg);

        VBox schooljaarKeuze = new VBox(7);
        schooljaarKeuze.setMaxWidth(980);
        Label schooljaarLabel = new Label("Schooljaar");
        schooljaarLabel.getStyleClass().add("field-label");
        schooljaarComboBox.setEditable(false);
        schooljaarComboBox.setMaxWidth(Double.MAX_VALUE);
        schooljaarComboBox.setPromptText("Selecteer een schooljaar");
        schooljaarKeuze.getChildren().addAll(schooljaarLabel, schooljaarComboBox);

        VBox tabelKaart = new VBox(14);
        tabelKaart.setMaxWidth(980);
        tabelKaart.setPadding(new Insets(20));
        tabelKaart.getStyleClass().add("content-card");
        VBox.setVgrow(tabelKaart, Priority.ALWAYS);

        HBox tabelHeader = new HBox(12);
        tabelHeader.setAlignment(Pos.CENTER_LEFT);
        Label tabelTitel = new Label("Periodes in het schooljaar");
        tabelTitel.getStyleClass().add("card-section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        toevoegenButton.getStyleClass().add("primary-button");
        tabelHeader.getChildren().addAll(tabelTitel, spacer, toevoegenButton);

        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabel.setPlaceholder(new Label("Geen talentenperiodes gevonden."));
        VBox.setVgrow(tabel, Priority.ALWAYS);

        HBox acties = new HBox(12);
        acties.setAlignment(Pos.CENTER_RIGHT);
        wijzigenButton.getStyleClass().add("secondary-button");
        verwijderenButton.getStyleClass().addAll("secondary-button", "danger-button");
        wijzigenButton.setDisable(true);
        verwijderenButton.setDisable(true);
        acties.getChildren().addAll(wijzigenButton, verwijderenButton);

        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-message");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        tabelKaart.getChildren().addAll(tabelHeader, tabel, acties, statusLabel);
        inhoud.getChildren().addAll(intro, schooljaarKeuze, tabelKaart);
        return inhoud;
    }

    private void configureerTabel() {
        TableColumn<TalentenPeriode, String> naamKolom = new TableColumn<>("Naam");
        naamKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNaam()));

        TableColumn<TalentenPeriode, String> startKolom = new TableColumn<>("Startdatum");
        startKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStartDatum().format(DATUM_FORMATTER)));

        TableColumn<TalentenPeriode, String> eindeKolom = new TableColumn<>("Einddatum");
        eindeKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEindDatum().format(DATUM_FORMATTER)));

        TableColumn<TalentenPeriode, String> statusKolom = new TableColumn<>("Status");
        statusKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(bepaalStatus(data.getValue(), LocalDate.now())));
        statusKolom.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("status-upcoming", "status-current", "status-past");
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item);
                getStyleClass().add(switch (item) {
                    case "Komend" -> "status-upcoming";
                    case "Lopend" -> "status-current";
                    default -> "status-past";
                });
            }
        });

        naamKolom.setMinWidth(220);
        startKolom.setMinWidth(150);
        eindeKolom.setMinWidth(150);
        statusKolom.setMinWidth(130);
        tabel.getColumns().addAll(naamKolom, startKolom, eindeKolom, statusKolom);
    }

    static String bepaalStatus(TalentenPeriode periode, LocalDate vandaag) {
        if (vandaag.isBefore(periode.getStartDatum())) return "Komend";
        if (vandaag.isAfter(periode.getEindDatum())) return "Afgelopen";
        return "Lopend";
    }

    public Button getTerugButton() { return terugButton; }
    public Button getThemeButton() { return themeButton; }
    public ComboBox<Schooljaar> getSchooljaarComboBox() { return schooljaarComboBox; }
    public TableView<TalentenPeriode> getTabel() { return tabel; }
    public Button getToevoegenButton() { return toevoegenButton; }
    public Button getWijzigenButton() { return wijzigenButton; }
    public Button getVerwijderenButton() { return verwijderenButton; }

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
