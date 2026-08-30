package be.kdg.talenten.view.beheer.basistalent;

import be.kdg.talenten.domain.Talent;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class BasistalentView extends BorderPane {
    private final Button terugButton = new Button("← Terug naar beheer");
    private final Button themeButton = new Button("☾");
    private final TextField zoekVeld = new TextField();
    private final TableView<Talent> tabel = new TableView<>();
    private final Button toevoegenButton = new Button("+ Basistalent toevoegen");
    private final Button wijzigenButton = new Button("Wijzigen");
    private final Label aantalLabel = new Label();
    private final Label statusLabel = new Label();

    public BasistalentView() {
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
        Label titel = new Label("Basistalenten");
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
        Label titel = new Label("Basistalenten beheren");
        titel.getStyleClass().add("content-title");
        Label uitleg = new Label("Beheer de talentencatalogus die als basis dient voor het ingerichte aanbod.");
        uitleg.setWrapText(true);
        uitleg.getStyleClass().add("content-subtitle");
        intro.getChildren().addAll(titel, uitleg);

        VBox zoeken = new VBox(7);
        zoeken.setMaxWidth(980);
        Label zoekLabel = new Label("Basistalent zoeken");
        zoekLabel.getStyleClass().add("field-label");
        zoekVeld.setPromptText("Zoek op naam of beschrijving");
        zoeken.getChildren().addAll(zoekLabel, zoekVeld);

        VBox tabelKaart = new VBox(12);
        tabelKaart.setMaxWidth(980);
        tabelKaart.setPadding(new Insets(20));
        tabelKaart.getStyleClass().add("content-card");

        HBox tabelHeader = new HBox(10);
        tabelHeader.setAlignment(Pos.CENTER_LEFT);
        Label tabelTitel = new Label("Basistalenten");
        tabelTitel.getStyleClass().add("card-section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        wijzigenButton.getStyleClass().add("secondary-button");
        wijzigenButton.setDisable(true);
        toevoegenButton.getStyleClass().add("primary-button");
        tabelHeader.getChildren().addAll(tabelTitel, spacer, wijzigenButton, toevoegenButton);

        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabel.setPlaceholder(new Label("Geen basistalenten gevonden."));
        tabel.setMinHeight(330);
        tabel.setPrefHeight(330);
        tabel.setMaxHeight(330);

        aantalLabel.getStyleClass().add("content-subtitle");
        toonAantal(0);
        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-message");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        tabelKaart.getChildren().addAll(tabelHeader, tabel, aantalLabel, statusLabel);
        inhoud.getChildren().addAll(intro, zoeken, tabelKaart);
        return inhoud;
    }

    private void configureerTabel() {
        TableColumn<Talent, String> naamKolom = new TableColumn<>("Naam");
        naamKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNaam()));
        naamKolom.setMinWidth(260);

        TableColumn<Talent, String> beschrijvingKolom = new TableColumn<>("Beschrijving");
        beschrijvingKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getBeschrijving()));
        beschrijvingKolom.setMinWidth(560);
        tabel.getColumns().addAll(naamKolom, beschrijvingKolom);
    }

    public void toonAantal(int aantal) {
        aantalLabel.setText(aantal == 1 ? "1 basistalent zichtbaar." : aantal + " basistalenten zichtbaar.");
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

    public Button getTerugButton() { return terugButton; }
    public Button getThemeButton() { return themeButton; }
    public TextField getZoekVeld() { return zoekVeld; }
    public TableView<Talent> getTabel() { return tabel; }
    public Button getToevoegenButton() { return toevoegenButton; }
    public Button getWijzigenButton() { return wijzigenButton; }
}
