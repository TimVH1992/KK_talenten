package be.kdg.talenten.view.beheer.schooljaar;

import be.kdg.talenten.domain.Schooljaar;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

public class SchooljaarView extends BorderPane {
    private static final DateTimeFormatter DATUM_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Button terugButton = new Button("← Terug");
    private final Button themeButton = new Button("☾");

    private final Label actiefSchooljaarLabel = new Label("—");
    private final Label statusLabel = new Label();

    private Schooljaar actiefSchooljaar;

    private final TableView<Schooljaar> tabel = new TableView<>();
    private final Button volgendSchooljaarButton = new Button("+ Volgend schooljaar toevoegen");
    private final Button maakActiefButton = new Button("Geselecteerd schooljaar actief maken");

    public SchooljaarView() {
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

        Label titel = new Label("Schooljaren");
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

        Label titel = new Label("Schooljaren beheren");
        titel.getStyleClass().add("content-title");

        Label uitleg = new Label(
                "Schooljaren worden automatisch opeenvolgend aangemaakt. U hoeft geen jaartallen of datums handmatig in te voeren."
        );
        uitleg.setWrapText(true);
        uitleg.getStyleClass().add("content-subtitle");

        intro.getChildren().addAll(titel, uitleg);

        HBox actiefKaart = new HBox(18);
        actiefKaart.setMaxWidth(980);
        actiefKaart.setAlignment(Pos.CENTER_LEFT);
        actiefKaart.setPadding(new Insets(18, 22, 18, 22));
        actiefKaart.getStyleClass().add("summary-card");

        Label icoon = new Label("✓");
        icoon.getStyleClass().add("summary-icon");

        VBox actiefTekst = new VBox(2);
        Label actiefTitel = new Label("Actief schooljaar");
        actiefTitel.getStyleClass().add("summary-label");
        actiefSchooljaarLabel.getStyleClass().add("summary-value");
        actiefTekst.getChildren().addAll(actiefTitel, actiefSchooljaarLabel);

        actiefKaart.getChildren().addAll(icoon, actiefTekst);

        VBox tabelKaart = new VBox(14);
        tabelKaart.setMaxWidth(980);
        tabelKaart.setPadding(new Insets(20));
        tabelKaart.getStyleClass().add("content-card");
        VBox.setVgrow(tabelKaart, Priority.ALWAYS);

        HBox tabelHeader = new HBox(12);
        tabelHeader.setAlignment(Pos.CENTER_LEFT);

        Label tabelTitel = new Label("Beschikbare schooljaren");
        tabelTitel.getStyleClass().add("card-section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        volgendSchooljaarButton.getStyleClass().add("primary-button");
        tabelHeader.getChildren().addAll(tabelTitel, spacer, volgendSchooljaarButton);

        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabel.setPlaceholder(new Label("Geen schooljaren gevonden."));
        VBox.setVgrow(tabel, Priority.ALWAYS);

        HBox acties = new HBox(12);
        acties.setAlignment(Pos.CENTER_RIGHT);
        maakActiefButton.getStyleClass().add("secondary-button");
        maakActiefButton.setDisable(true);
        acties.getChildren().add(maakActiefButton);

        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-message");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        tabelKaart.getChildren().addAll(tabelHeader, tabel, acties, statusLabel);

        inhoud.getChildren().addAll(intro, actiefKaart, tabelKaart);
        return inhoud;
    }

    private void configureerTabel() {
        TableColumn<Schooljaar, String> naamKolom = new TableColumn<>("Schooljaar");
        naamKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getNaam())
        );

        TableColumn<Schooljaar, String> startKolom = new TableColumn<>("Start");
        startKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getStartDatum().format(DATUM_FORMATTER))
        );

        TableColumn<Schooljaar, String> eindeKolom = new TableColumn<>("Einde");
        eindeKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getEindDatum().format(DATUM_FORMATTER))
        );

        TableColumn<Schooljaar, String> statusKolom = new TableColumn<>("Status");
        statusKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        isActiefSchooljaar(data.getValue())
                                ? "Actief"
                                : "Niet actief"
                )
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
                getStyleClass().add(item.equals("Actief") ? "status-active" : "status-inactive");
            }
        });

        naamKolom.setMinWidth(210);
        startKolom.setMinWidth(140);
        eindeKolom.setMinWidth(140);
        statusKolom.setMinWidth(130);

        tabel.getColumns().addAll(naamKolom, startKolom, eindeKolom, statusKolom);
    }

    public Button getTerugButton() {
        return terugButton;
    }

    public Button getThemeButton() {
        return themeButton;
    }

    public TableView<Schooljaar> getTabel() {
        return tabel;
    }

    public Button getVolgendSchooljaarButton() {
        return volgendSchooljaarButton;
    }

    public Button getMaakActiefButton() {
        return maakActiefButton;
    }

    public void toonActiefSchooljaar(Schooljaar schooljaar) {
        this.actiefSchooljaar = schooljaar;

        actiefSchooljaarLabel.setText(
                schooljaar == null
                        ? "Geen actief schooljaar"
                        : schooljaar.getNaam()
        );

        // De statuskolom gebruikt geen JavaFX-property uit het domeinobject.
        // Daarom forceren we hier onmiddellijk een hertekening zodra het
        // actieve schooljaar wijzigt.
        tabel.refresh();
    }

    public boolean isActiefSchooljaar(Schooljaar schooljaar) {
        if (schooljaar == null || actiefSchooljaar == null) {
            return false;
        }

        if (schooljaar.getId() != null && actiefSchooljaar.getId() != null) {
            return schooljaar.getId().equals(actiefSchooljaar.getId());
        }

        return schooljaar.equals(actiefSchooljaar);
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
