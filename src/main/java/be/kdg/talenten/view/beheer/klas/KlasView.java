package be.kdg.talenten.view.beheer.klas;

import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Schooljaar;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class KlasView extends BorderPane {
    private final Button terugButton = new Button("← Terug naar beheer");
    private final Button themeButton = new Button("☾");

    private final ComboBox<Schooljaar> schooljaarComboBox = new ComboBox<>();
    private final TableView<Klas> tabel = new TableView<>();

    private final Button toevoegenButton = new Button("+ Klas toevoegen");
    private final Button wijzigenButton = new Button("Wijzigen");
    private final Button verwijderenButton = new Button("Verwijderen");
    private final Label statusLabel = new Label();

    public KlasView() {
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

        Label titel = new Label("Klassen");
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

        Label titel = new Label("Klassen beheren");
        titel.getStyleClass().add("content-title");

        Label uitleg = new Label(
                "Beheer de klassen, leerjaren en doelgroepen binnen een gekozen schooljaar."
        );
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

        Label tabelTitel = new Label("Klassen in het schooljaar");
        tabelTitel.getStyleClass().add("card-section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toevoegenButton.getStyleClass().add("primary-button");
        tabelHeader.getChildren().addAll(tabelTitel, spacer, toevoegenButton);

        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabel.setPlaceholder(new Label("Geen klassen gevonden."));
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
        TableColumn<Klas, String> naamKolom = new TableColumn<>("Klasnaam");
        naamKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getNaam())
        );

        TableColumn<Klas, String> leerjaarKolom = new TableColumn<>("Leerjaar");
        leerjaarKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(Integer.toString(data.getValue().getLeerjaar()))
        );

        TableColumn<Klas, String> doelgroepKolom = new TableColumn<>("Doelgroep");
        doelgroepKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(geefLeesbareDoelgroep(data.getValue()))
        );

        naamKolom.setMinWidth(220);
        leerjaarKolom.setMinWidth(130);
        doelgroepKolom.setMinWidth(420);
        tabel.getColumns().addAll(naamKolom, leerjaarKolom, doelgroepKolom);
    }

    private String geefLeesbareDoelgroep(Klas klas) {
        return switch (klas.getDoelgroep()) {
            case OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB ->
                    "Observatie-/opleidingsfase en eerste graad A/B";
            case KWALIFICATIEFASE_TWEEDEGRAAD_AB ->
                    "Kwalificatiefase en tweede graad A/B";
        };
    }

    public Button getTerugButton() {
        return terugButton;
    }

    public Button getThemeButton() {
        return themeButton;
    }

    public ComboBox<Schooljaar> getSchooljaarComboBox() {
        return schooljaarComboBox;
    }

    public TableView<Klas> getTabel() {
        return tabel;
    }

    public Button getToevoegenButton() {
        return toevoegenButton;
    }

    public Button getWijzigenButton() {
        return wijzigenButton;
    }

    public Button getVerwijderenButton() {
        return verwijderenButton;
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
