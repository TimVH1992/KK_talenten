package be.kdg.talenten.view.main;

import be.kdg.talenten.view.navigation.AppSidebar;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class MainView extends BorderPane {
    private final AppSidebar sidebar;

    private Label actiefSchooljaarLabel;
    private Label leerlingenAantalLabel;
    private Label ingerichteTalentenAantalLabel;
    private Label voorkeurenStatusLabel;
    private Label toewijzingenStatusLabel;

    private Label huidigePeriodeNaamLabel;
    private Label huidigePeriodeDatumsLabel;
    private Label huidigePeriodeStatusLabel;

    private VBox aandachtspuntenBox;
    private Button volgendeActieButton;

    public MainView() {
        sidebar = new AppSidebar(AppSidebar.Sectie.DASHBOARD, "Overzicht");
        initialiseNodes();
        layoutNodes();
    }

    private void initialiseNodes() {
        actiefSchooljaarLabel = new Label("Schooljaar wordt geladen...");
        actiefSchooljaarLabel.getStyleClass().add("context-pill");

        leerlingenAantalLabel = maakWaardeLabel();
        ingerichteTalentenAantalLabel = maakWaardeLabel();
        voorkeurenStatusLabel = maakWaardeLabel();
        toewijzingenStatusLabel = maakWaardeLabel();

        huidigePeriodeNaamLabel = new Label("Geen actieve periode");
        huidigePeriodeNaamLabel.getStyleClass().add("section-heading");

        huidigePeriodeDatumsLabel = new Label("—");
        huidigePeriodeDatumsLabel.getStyleClass().add("muted-label");

        huidigePeriodeStatusLabel = new Label("Er is nog geen periode-informatie geladen.");
        huidigePeriodeStatusLabel.setWrapText(true);
        huidigePeriodeStatusLabel.getStyleClass().add("status-label");

        aandachtspuntenBox = new VBox(10);
        toonGeenAandachtspunten();

        volgendeActieButton = new Button();
        volgendeActieButton.getStyleClass().add("primary-button");
        volgendeActieButton.setVisible(false);
        volgendeActieButton.setManaged(false);
    }

    private Label maakWaardeLabel() {
        Label label = new Label("—");
        label.getStyleClass().add("page-title");
        return label;
    }

    private void layoutNodes() {
        setLeft(sidebar);
        setCenter(maakInhoud());
        getStyleClass().add("app-background");
    }

    private ScrollPane maakInhoud() {
        Label titel = new Label("Overzicht");
        titel.getStyleClass().add("page-title");

        Label ondertitel = new Label("Bekijk in één oogopslag de status van de huidige talentenwerking.");
        ondertitel.getStyleClass().add("page-subtitle");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox header = new HBox(
                14,
                new VBox(4, titel, ondertitel),
                headerSpacer,
                actiefSchooljaarLabel
        );
        header.setAlignment(Pos.CENTER_LEFT);

        GridPane kerncijfers = maakKerncijfers();

        VBox periodeCard = maakPeriodeCard();
        VBox aandachtCard = maakAandachtCard();

        HBox detailRij = new HBox(16, periodeCard, aandachtCard);
        HBox.setHgrow(periodeCard, Priority.ALWAYS);
        HBox.setHgrow(aandachtCard, Priority.ALWAYS);

        VBox inhoud = new VBox(24, header, kerncijfers, detailRij);
        inhoud.setPadding(new Insets(32));

        ScrollPane scrollPane = new ScrollPane(inhoud);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("app-background");
        return scrollPane;
    }

    private GridPane maakKerncijfers() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        grid.add(maakKerncijferCard(
                "Leerlingen",
                leerlingenAantalLabel,
                "Aantal leerlingen in het actieve schooljaar."
        ), 0, 0);

        grid.add(maakKerncijferCard(
                "Ingerichte talenten",
                ingerichteTalentenAantalLabel,
                "Talenten ingericht voor de huidige periode."
        ), 1, 0);

        grid.add(maakKerncijferCard(
                "Voorkeuren",
                voorkeurenStatusLabel,
                "Leerlingen met volledige voorkeuren."
        ), 2, 0);

        grid.add(maakKerncijferCard(
                "Toewijzingen",
                toewijzingenStatusLabel,
                "Leerlingen die aan een talent zijn toegewezen."
        ), 3, 0);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints kolom = new ColumnConstraints();
            kolom.setPercentWidth(25);
            kolom.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(kolom);
        }

        return grid;
    }

    private VBox maakKerncijferCard(String titel, Label waardeLabel, String uitleg) {
        Label titelLabel = new Label(titel);
        titelLabel.getStyleClass().add("section-heading");

        Label uitlegLabel = new Label(uitleg);
        uitlegLabel.setWrapText(true);
        uitlegLabel.getStyleClass().add("muted-label");

        VBox card = new VBox(8, titelLabel, waardeLabel, uitlegLabel);
        card.getStyleClass().add("content-card");
        card.setPadding(new Insets(20));
        card.setMinHeight(150);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private VBox maakPeriodeCard() {
        Label titel = new Label("Talentenperiode");
        titel.getStyleClass().add("section-heading");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox card = new VBox(
                10,
                titel,
                huidigePeriodeNaamLabel,
                huidigePeriodeDatumsLabel,
                huidigePeriodeStatusLabel,
                spacer,
                volgendeActieButton
        );
        card.getStyleClass().add("content-card");
        card.setPadding(new Insets(22));
        card.setMinHeight(240);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private VBox maakAandachtCard() {
        Label titel = new Label("Aandacht nodig");
        titel.getStyleClass().add("section-heading");

        Label uitleg = new Label("Hier verschijnen zaken die nog actie vragen binnen het actieve schooljaar.");
        uitleg.setWrapText(true);
        uitleg.getStyleClass().add("muted-label");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox card = new VBox(12, titel, uitleg, aandachtspuntenBox, spacer);
        card.getStyleClass().add("content-card");
        card.setPadding(new Insets(22));
        card.setMinHeight(240);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private void toonGeenAandachtspunten() {
        aandachtspuntenBox.getChildren().clear();

        Label label = new Label("✓ Geen aandachtspunten.");
        label.setWrapText(true);
        label.getStyleClass().add("status-success");

        aandachtspuntenBox.getChildren().add(label);
    }

    public void setActiefSchooljaar(String schooljaar) {
        actiefSchooljaarLabel.setText(
                schooljaar == null || schooljaar.isBlank()
                        ? "Geen actief schooljaar"
                        : "Schooljaar " + schooljaar
        );
    }

    public void setAantalLeerlingen(int aantal) {
        leerlingenAantalLabel.setText(String.valueOf(aantal));
    }

    public void setAantalIngerichteTalenten(int aantal) {
        ingerichteTalentenAantalLabel.setText(String.valueOf(aantal));
    }

    public void setVoorkeurenStatus(int volledig, int totaal) {
        voorkeurenStatusLabel.setText(volledig + " / " + totaal);
    }

    public void setToewijzingenStatus(int toegewezen, int totaal) {
        toewijzingenStatusLabel.setText(toegewezen + " / " + totaal);
    }

    public void setHuidigePeriode(String naam, String datums, String status) {
        huidigePeriodeNaamLabel.setText(
                naam == null || naam.isBlank() ? "Geen actieve periode" : naam
        );
        huidigePeriodeDatumsLabel.setText(
                datums == null || datums.isBlank() ? "—" : datums
        );
        huidigePeriodeStatusLabel.setText(
                status == null || status.isBlank() ? "" : status
        );
    }

    public void setAandachtspunten(List<String> aandachtspunten) {
        if (aandachtspunten == null || aandachtspunten.isEmpty()) {
            toonGeenAandachtspunten();
            return;
        }

        aandachtspuntenBox.getChildren().clear();

        for (String aandachtspunt : aandachtspunten) {
            if (aandachtspunt == null || aandachtspunt.isBlank()) {
                continue;
            }

            Label label = new Label("⚠ " + aandachtspunt);
            label.setWrapText(true);
            label.getStyleClass().add("status-warning");
            aandachtspuntenBox.getChildren().add(label);
        }

        if (aandachtspuntenBox.getChildren().isEmpty()) {
            toonGeenAandachtspunten();
        }
    }

    public void toonVolgendeActie(String tekst) {
        if (tekst == null || tekst.isBlank()) {
            verbergVolgendeActie();
            return;
        }

        volgendeActieButton.setText(tekst);
        volgendeActieButton.setVisible(true);
        volgendeActieButton.setManaged(true);
    }

    public void verbergVolgendeActie() {
        volgendeActieButton.setVisible(false);
        volgendeActieButton.setManaged(false);
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }

    public Button getVolgendeActieButton() {
        return volgendeActieButton;
    }
}
