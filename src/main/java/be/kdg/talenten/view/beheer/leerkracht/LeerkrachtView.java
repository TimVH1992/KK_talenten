package be.kdg.talenten.view.beheer.leerkracht;

import be.kdg.talenten.domain.Leerkracht;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LeerkrachtView extends BorderPane {
    private final Button terugButton = new Button("← Terug naar beheer");
    private final Button themeButton = new Button("☾");
    private final TextField zoekVeld = new TextField();
    private final TableView<Leerkracht> tabel = new TableView<>();
    private final Button toevoegenButton = new Button("+ Leerkracht toevoegen");
    private final Button wijzigenButton = new Button("Wijzigen");
    private final Button actiefWijzigenButton = new Button("Deactiveren");
    private final Label zichtbaarheidLabel = new Label();
    private final Label statusLabel = new Label();

    public LeerkrachtView() {
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

        Label titel = new Label("Leerkrachten");
        titel.getStyleClass().add("section-title");
        header.setCenter(titel);

        themeButton.getStyleClass().add("theme-toggle");
        themeButton.setTooltip(new Tooltip("Wissel tussen lichte en donkere modus"));
        header.setRight(themeButton);
        return header;
    }

    private Pane maakInhoud() {
        VBox inhoud = new VBox(18);
        inhoud.setPadding(new Insets(28, 60, 34, 60));
        inhoud.setAlignment(Pos.TOP_CENTER);

        VBox intro = new VBox(5);
        intro.setMaxWidth(980);
        Label titel = new Label("Leerkrachten beheren");
        titel.getStyleClass().add("content-title");
        Label uitleg = new Label("Beheer leerkrachten en bepaal wie actief aan talenten kan deelnemen.");
        uitleg.setWrapText(true);
        uitleg.getStyleClass().add("content-subtitle");
        intro.getChildren().addAll(titel, uitleg);

        VBox zoeken = new VBox(7);
        zoeken.setMaxWidth(980);
        Label zoekLabel = new Label("Leerkracht zoeken");
        zoekLabel.getStyleClass().add("field-label");
        zoekVeld.setPromptText("Zoek op voor- of achternaam");
        zoeken.getChildren().addAll(zoekLabel, zoekVeld);

        VBox tabelKaart = new VBox(12);
        tabelKaart.setMaxWidth(980);
        tabelKaart.setPadding(new Insets(20));
        tabelKaart.getStyleClass().add("content-card");

        HBox tabelHeader = new HBox(10);
        tabelHeader.setAlignment(Pos.CENTER_LEFT);
        Label tabelTitel = new Label("Leerkrachten");
        tabelTitel.getStyleClass().add("card-section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        wijzigenButton.getStyleClass().add("secondary-button");
        actiefWijzigenButton.getStyleClass().add("secondary-button");
        toevoegenButton.getStyleClass().add("primary-button");
        wijzigSelectieActies(false, true);
        tabelHeader.getChildren().addAll(
                tabelTitel, spacer, wijzigenButton, actiefWijzigenButton, toevoegenButton
        );

        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabel.setPlaceholder(new Label("Geen leerkrachten gevonden."));
        tabel.setMinHeight(320);
        tabel.setPrefHeight(320);
        tabel.setMaxHeight(320);
        VBox.setVgrow(tabel, Priority.ALWAYS);

        zichtbaarheidLabel.setWrapText(true);
        zichtbaarheidLabel.getStyleClass().add("content-subtitle");
        toonAantalLeerkrachten(0);

        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-message");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        tabelKaart.getChildren().addAll(tabelHeader, tabel, zichtbaarheidLabel, statusLabel);
        inhoud.getChildren().addAll(intro, zoeken, tabelKaart);
        return inhoud;
    }

    private void configureerTabel() {
        TableColumn<Leerkracht, String> voornaamKolom = new TableColumn<>("Voornaam");
        voornaamKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getVoornaam())
        );

        TableColumn<Leerkracht, String> achternaamKolom = new TableColumn<>("Achternaam");
        achternaamKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getAchternaam())
        );

        TableColumn<Leerkracht, String> statusKolom = new TableColumn<>("Status");
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

        voornaamKolom.setMinWidth(260);
        achternaamKolom.setMinWidth(320);
        statusKolom.setMinWidth(150);
        tabel.getColumns().addAll(voornaamKolom, achternaamKolom, statusKolom);
    }

    public void wijzigSelectieActies(boolean geselecteerd, boolean actief) {
        wijzigenButton.setDisable(!geselecteerd);
        actiefWijzigenButton.setDisable(!geselecteerd);
        actiefWijzigenButton.setText(actief ? "Deactiveren" : "Activeren");
    }

    public void toonAantalLeerkrachten(int aantal) {
        if (aantal > 6) {
            zichtbaarheidLabel.setText(
                    "6 van " + aantal + " leerkrachten zichtbaar — scroll om de overige leerkrachten te bekijken."
            );
        } else if (aantal == 1) {
            zichtbaarheidLabel.setText("1 leerkracht zichtbaar.");
        } else {
            zichtbaarheidLabel.setText(aantal + " leerkrachten zichtbaar.");
        }
    }

    public Button getTerugButton() { return terugButton; }
    public Button getThemeButton() { return themeButton; }
    public TextField getZoekVeld() { return zoekVeld; }
    public TableView<Leerkracht> getTabel() { return tabel; }
    public Button getToevoegenButton() { return toevoegenButton; }
    public Button getWijzigenButton() { return wijzigenButton; }
    public Button getActiefWijzigenButton() { return actiefWijzigenButton; }

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
