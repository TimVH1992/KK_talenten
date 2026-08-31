package be.kdg.talenten.view.beheer.leerling.historiek;

import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.domain.ToewijzingsType;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

public class LeerlingHistoriekView extends BorderPane {
    private final Button terugButton = new Button("← Terug naar leerlingen");
    private final Button themeButton = new Button("☾");
    private final TextField zoekVeld = new TextField();
    private final TableView<Leerling> leerlingTabel = new TableView<>();
    private final TableView<Toewijzing> historiekTabel = new TableView<>();
    private final Label historiekTitel = new Label("Selecteer een leerling");
    private final Label statusLabel = new Label();
    private static final DateTimeFormatter DATUM = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public LeerlingHistoriekView() {
        getStyleClass().add("app-root");
        setTop(maakHeader());
        setCenter(maakInhoud());
        configureerTabellen();
    }

    private Pane maakHeader() {
        BorderPane header = new BorderPane();
        header.getStyleClass().add("section-header");
        header.setPadding(new Insets(18, 28, 18, 28));
        terugButton.getStyleClass().add("secondary-button");
        header.setLeft(terugButton);
        Label titel = new Label("Leerlinghistoriek");
        titel.getStyleClass().add("section-title");
        header.setCenter(titel);
        themeButton.getStyleClass().add("theme-toggle");
        themeButton.setTooltip(new Tooltip("Wissel tussen lichte en donkere modus"));
        header.setRight(themeButton);
        return header;
    }

    private Pane maakInhoud() {
        VBox inhoud = new VBox(16);
        inhoud.setPadding(new Insets(28, 50, 36, 50));
        inhoud.setAlignment(Pos.TOP_CENTER);
        inhoud.setMaxWidth(1180);
        Label uitleg = new Label("Zoek een leerling op naam en bekijk alle opgeslagen toewijzingen over de schooljaren heen.");
        uitleg.getStyleClass().add("content-subtitle");
        zoekVeld.setPromptText("Zoek op voornaam, achternaam of volledige naam");
        zoekVeld.setMaxWidth(Double.MAX_VALUE);

        VBox leerlingKaart = new VBox(10, new Label("Leerlingen"), leerlingTabel);
        leerlingKaart.getStyleClass().add("content-card");
        leerlingKaart.setPadding(new Insets(18));
        leerlingTabel.setPrefHeight(210);
        historiekTitel.getStyleClass().add("card-section-title");
        VBox historiekKaart = new VBox(10, historiekTitel, historiekTabel);
        historiekKaart.getStyleClass().add("content-card");
        historiekKaart.setPadding(new Insets(18));
        historiekTabel.setPrefHeight(300);
        VBox.setVgrow(historiekTabel, Priority.ALWAYS);
        statusLabel.getStyleClass().add("status-message");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        inhoud.getChildren().addAll(uitleg, zoekVeld, leerlingKaart, historiekKaart, statusLabel);
        return inhoud;
    }

    private void configureerTabellen() {
        leerlingTabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        leerlingTabel.setPlaceholder(new Label("Geen leerlingen gevonden."));
        TableColumn<Leerling, String> naam = new TableColumn<>("Leerling");
        naam.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getVoornaam() + " " + d.getValue().getAchternaam()));
        TableColumn<Leerling, String> klas = new TableColumn<>("Klas");
        klas.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getKlas().getNaam()));
        TableColumn<Leerling, String> schooljaar = new TableColumn<>("Schooljaar");
        schooljaar.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getKlas().getSchooljaar().getNaam()));
        leerlingTabel.getColumns().addAll(naam, klas, schooljaar);

        historiekTabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        historiekTabel.setPlaceholder(new Label("Geen opgeslagen toewijzingen voor deze leerling."));
        TableColumn<Toewijzing, String> jaar = new TableColumn<>("Schooljaar");
        jaar.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getIngerichtTalent().getTalentenPeriode().getSchooljaar().getNaam()));
        TableColumn<Toewijzing, String> periode = new TableColumn<>("Periode");
        periode.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getIngerichtTalent().getTalentenPeriode().getNaam()));
        TableColumn<Toewijzing, String> talent = new TableColumn<>("Ingericht talent");
        talent.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getIngerichtTalent().getNaam()));
        TableColumn<Toewijzing, String> type = new TableColumn<>("Type");
        type.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getToewijzingsType() == ToewijzingsType.MANUEEL ? "Manueel" : "Automatisch"));
        TableColumn<Toewijzing, String> voorkeur = new TableColumn<>("Voorkeur");
        voorkeur.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getVoorkeurNummer() == null ? "—" : d.getValue().getVoorkeurNummer().toString()));
        TableColumn<Toewijzing, String> datum = new TableColumn<>("Datum");
        datum.setCellValueFactory(d -> new ReadOnlyStringWrapper((d.getValue().getGewijzigdOp() == null
                ? d.getValue().getToegewezenOp() : d.getValue().getGewijzigdOp()).format(DATUM)));
        historiekTabel.getColumns().addAll(jaar, periode, talent, type, voorkeur, datum);
    }

    public Button getTerugButton() { return terugButton; }
    public Button getThemeButton() { return themeButton; }
    public TextField getZoekVeld() { return zoekVeld; }
    public TableView<Leerling> getLeerlingTabel() { return leerlingTabel; }
    public TableView<Toewijzing> getHistoriekTabel() { return historiekTabel; }
    public Label getHistoriekTitel() { return historiekTitel; }
    public void toonStatus(String tekst) { statusLabel.setText(tekst); statusLabel.setVisible(true); statusLabel.setManaged(true); }
    public void updateThemeIcon(boolean dark) { themeButton.setText(dark ? "☀" : "☾"); }
}
