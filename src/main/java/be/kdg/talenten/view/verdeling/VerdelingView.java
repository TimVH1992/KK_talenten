package be.kdg.talenten.view.verdeling;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.overzicht.IngerichtTalentOverzicht;
import be.kdg.talenten.overzicht.LeerlingToewijzingOverzicht;
import be.kdg.talenten.overzicht.NietToegewezenLeerlingOverzicht;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.stream.Collectors;

public class VerdelingView extends BorderPane {
    private final Button terugButton = new Button("← Terug naar hoofdmenu");
    private final Button themeButton = new Button("☾");
    private final ComboBox<Schooljaar> schooljaarComboBox = new ComboBox<>();
    private final ComboBox<TalentenPeriode> periodeComboBox = new ComboBox<>();
    private final ComboBox<Doelgroep> doelgroepComboBox = new ComboBox<>();
    private final Button automatischButton = new Button("Automatische verdeling uitvoeren");
    private final Button handmatigButton = new Button("Handmatig toewijzen");
    private final TableView<IngerichtTalentOverzicht> talentTabel = new TableView<>();
    private final TableView<Toewijzing> leerlingTabel = new TableView<>();
    private final TableView<NietToegewezenLeerlingOverzicht> nietToegewezenTabel = new TableView<>();
    private final ComboBox<Klas> klasComboBox = new ComboBox<>();
    private final TableView<LeerlingToewijzingOverzicht> klasTabel = new TableView<>();
    private final Tab nietToegewezenTab = new Tab("Niet toegewezen (0)");
    private final Label leerlingenAantal = new Label("0");
    private final Label toegewezenAantal = new Label("0");
    private final Label nietToegewezenAantal = new Label("0");
    private final Label talentenAantal = new Label("0");
    private final Label detailTitel = new Label("Selecteer een talent");
    private final Label statusLabel = new Label();

    public VerdelingView() {
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
        Label titel = new Label("Verdeling opstellen");
        titel.getStyleClass().add("section-title");
        header.setCenter(titel);
        themeButton.getStyleClass().add("theme-toggle");
        themeButton.setTooltip(new Tooltip("Wissel tussen lichte en donkere modus"));
        header.setRight(themeButton);
        return header;
    }

    private Node maakInhoud() {
        VBox inhoud = new VBox(16);
        inhoud.setPadding(new Insets(24, 42, 32, 42));
        inhoud.setMaxWidth(1220);

        VBox intro = new VBox(4);
        Label titel = new Label("Verdeling opstellen");
        titel.getStyleClass().add("content-title");
        Label uitleg = new Label("Verdeel leerlingen automatisch en pas toewijzingen waar nodig handmatig aan.");
        uitleg.getStyleClass().add("content-subtitle");
        intro.getChildren().addAll(titel, uitleg);

        HBox keuzes = new HBox(12);
        keuzes.getChildren().addAll(maakKeuze("Schooljaar", schooljaarComboBox),
                maakKeuze("Talentenperiode", periodeComboBox), maakKeuze("Doelgroep", doelgroepComboBox));
        keuzes.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));

        HBox samenvatting = new HBox(12, maakTeller("Leerlingen", leerlingenAantal),
                maakTeller("Toegewezen", toegewezenAantal),
                maakTeller("Niet toegewezen", nietToegewezenAantal),
                maakTeller("Talenten", talentenAantal));
        samenvatting.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));

        HBox acties = new HBox(10);
        acties.setAlignment(Pos.CENTER_RIGHT);
        automatischButton.getStyleClass().add("primary-button");
        handmatigButton.getStyleClass().add("secondary-button");
        handmatigButton.setDisable(true);
        acties.getChildren().addAll(handmatigButton, automatischButton);

        HBox talentInhoud = new HBox(14);
        VBox talentKaart = maakKaart("Talenten en capaciteit", talentTabel);
        VBox detailKaart = maakKaart(detailTitel, leerlingTabel);
        talentKaart.setPrefWidth(720);
        detailKaart.setPrefWidth(440);
        HBox.setHgrow(talentKaart, Priority.ALWAYS);
        HBox.setHgrow(detailKaart, Priority.ALWAYS);
        talentInhoud.getChildren().addAll(talentKaart, detailKaart);

        klasComboBox.setEditable(false);
        klasComboBox.setPromptText("Selecteer een klas");
        klasComboBox.setMaxWidth(360);
        VBox klasInhoud = new VBox(10, klasComboBox, klasTabel);
        klasInhoud.setPadding(new Insets(12));

        Tab talentTab = new Tab("Talent", talentInhoud);
        Tab klasTab = new Tab("Klas", klasInhoud);
        nietToegewezenTab.setContent(nietToegewezenTabel);
        talentTab.setClosable(false);
        klasTab.setClosable(false);
        nietToegewezenTab.setClosable(false);
        TabPane tabPane = new TabPane(talentTab, klasTab, nietToegewezenTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox overzichtKaart = maakKaart("Overzicht", tabPane);

        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-message");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        inhoud.getChildren().addAll(intro, keuzes, acties, overzichtKaart, samenvatting, statusLabel);
        StackPane centreer = new StackPane(inhoud);
        centreer.setAlignment(Pos.TOP_CENTER);
        ScrollPane scroll = new ScrollPane(centreer);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("content-scroll");
        return scroll;
    }

    private <T> VBox maakKeuze(String labelTekst, ComboBox<T> comboBox) {
        Label label = new Label(labelTekst);
        label.getStyleClass().add("field-label");
        comboBox.setEditable(false);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        VBox box = new VBox(6, label, comboBox);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private VBox maakTeller(String titel, Label waarde) {
        Label label = new Label(titel);
        label.getStyleClass().add("content-subtitle");
        waarde.getStyleClass().add("card-section-title");
        VBox box = new VBox(3, label, waarde);
        box.setPadding(new Insets(12, 16, 12, 16));
        box.getStyleClass().add("content-card");
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private VBox maakKaart(String titel, Control tabel) {
        return maakKaart(new Label(titel), tabel);
    }

    private VBox maakKaart(Label titel, Control tabel) {
        titel.getStyleClass().add("card-section-title");
        VBox kaart = new VBox(10, titel, tabel);
        kaart.setPadding(new Insets(16));
        kaart.getStyleClass().add("content-card");
        return kaart;
    }

    private void configureerTabellen() {
        talentTabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        talentTabel.setPrefHeight(260);
        talentTabel.setPlaceholder(new Label("Geen actieve ingerichte talenten."));
        TableColumn<IngerichtTalentOverzicht, String> talent = new TableColumn<>("Talent");
        talent.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().ingerichtTalent().getNaam()));
        TableColumn<IngerichtTalentOverzicht, String> leerkrachten = new TableColumn<>("Leerkracht(en)");
        leerkrachten.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                d.getValue().ingerichtTalent().getLeerkrachten().isEmpty() ? "Geen"
                        : d.getValue().ingerichtTalent().getLeerkrachten().stream()
                        .map(l -> l.getVoornaam() + " " + l.getAchternaam()).collect(Collectors.joining(", "))));
        TableColumn<IngerichtTalentOverzicht, String> capaciteit = new TableColumn<>("Capaciteit");
        capaciteit.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                d.getValue().aantalToegewezen() + " / " + d.getValue().ingerichtTalent().getMaxCapaciteit()));
        TableColumn<IngerichtTalentOverzicht, String> vrij = new TableColumn<>("Vrij");
        vrij.setCellValueFactory(d -> new ReadOnlyStringWrapper(Integer.toString(d.getValue().aantalVrijePlaatsen())));
        talentTabel.getColumns().addAll(talent, leerkrachten, capaciteit, vrij);

        leerlingTabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        leerlingTabel.setPrefHeight(260);
        leerlingTabel.setPlaceholder(new Label("Selecteer een talent om leerlingen te bekijken."));
        TableColumn<Toewijzing, String> leerling = new TableColumn<>("Leerling");
        leerling.setCellValueFactory(d -> new ReadOnlyStringWrapper(naam(d.getValue().getLeerling())));
        TableColumn<Toewijzing, String> klas = new TableColumn<>("Klas");
        klas.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getLeerling().getKlas().getNaam()));
        TableColumn<Toewijzing, String> voorkeur = new TableColumn<>("Voorkeur");
        voorkeur.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                d.getValue().getVoorkeurNummer() == null ? "—" : d.getValue().getVoorkeurNummer().toString()));
        TableColumn<Toewijzing, String> type = new TableColumn<>("Type");
        type.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                d.getValue().getToewijzingsType() == ToewijzingsType.MANUEEL ? "Manueel" : "Automatisch"));
        leerlingTabel.getColumns().addAll(leerling, klas, voorkeur, type);

        nietToegewezenTabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        nietToegewezenTabel.setPrefHeight(180);
        nietToegewezenTabel.setPlaceholder(new Label("Alle leerlingen zijn toegewezen."));
        TableColumn<NietToegewezenLeerlingOverzicht, String> nietNaam = new TableColumn<>("Leerling");
        nietNaam.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().volledigeNaam()));
        TableColumn<NietToegewezenLeerlingOverzicht, String> nietKlas = new TableColumn<>("Klas");
        nietKlas.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().klasNaam()));
        nietToegewezenTabel.getColumns().addAll(nietNaam, nietKlas);

        klasTabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        klasTabel.setPrefHeight(300);
        klasTabel.setPlaceholder(new Label("Selecteer een klas om de verdeling te bekijken."));
        TableColumn<LeerlingToewijzingOverzicht, String> klasLeerling = new TableColumn<>("Leerling");
        klasLeerling.setCellValueFactory(d -> new ReadOnlyStringWrapper(naam(d.getValue().leerling())));
        TableColumn<LeerlingToewijzingOverzicht, String> klasTalent = new TableColumn<>("Ingericht talent");
        klasTalent.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().toewijzing() == null
                ? "Niet toegewezen" : d.getValue().toewijzing().getIngerichtTalent().getNaam()));
        TableColumn<LeerlingToewijzingOverzicht, String> klasVoorkeur = new TableColumn<>("Voorkeur");
        klasVoorkeur.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().toewijzing() == null
                || d.getValue().toewijzing().getVoorkeurNummer() == null ? "—"
                : d.getValue().toewijzing().getVoorkeurNummer().toString()));
        TableColumn<LeerlingToewijzingOverzicht, String> klasType = new TableColumn<>("Type");
        klasType.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().toewijzing() == null ? "—"
                : d.getValue().toewijzing().getToewijzingsType() == ToewijzingsType.MANUEEL
                ? "Manueel" : "Automatisch"));
        klasTabel.getColumns().addAll(klasLeerling, klasTalent, klasVoorkeur, klasType);
    }

    private static String naam(Leerling leerling) {
        return leerling.getVoornaam() + " " + leerling.getAchternaam();
    }

    public void toonAantallen(int leerlingen, int toegewezen, int nietToegewezen, int talenten) {
        leerlingenAantal.setText(Integer.toString(leerlingen));
        toegewezenAantal.setText(Integer.toString(toegewezen));
        nietToegewezenAantal.setText(Integer.toString(nietToegewezen));
        talentenAantal.setText(Integer.toString(talenten));
        nietToegewezenTab.setText("Niet toegewezen (" + nietToegewezen + ")");
    }
    public void toonStatus(String tekst, boolean fout) {
        statusLabel.setText(tekst);
        statusLabel.getStyleClass().removeAll("status-success", "status-error");
        statusLabel.getStyleClass().add(fout ? "status-error" : "status-success");
        statusLabel.setVisible(true); statusLabel.setManaged(true);
    }
    public void verbergStatus() { statusLabel.setVisible(false); statusLabel.setManaged(false); }
    public void updateThemeIcon(boolean dark) { themeButton.setText(dark ? "☀" : "☾"); }
    public Button getTerugButton() { return terugButton; }
    public Button getThemeButton() { return themeButton; }
    public ComboBox<Schooljaar> getSchooljaarComboBox() { return schooljaarComboBox; }
    public ComboBox<TalentenPeriode> getPeriodeComboBox() { return periodeComboBox; }
    public ComboBox<Doelgroep> getDoelgroepComboBox() { return doelgroepComboBox; }
    public Button getAutomatischButton() { return automatischButton; }
    public Button getHandmatigButton() { return handmatigButton; }
    public TableView<IngerichtTalentOverzicht> getTalentTabel() { return talentTabel; }
    public TableView<Toewijzing> getLeerlingTabel() { return leerlingTabel; }
    public TableView<NietToegewezenLeerlingOverzicht> getNietToegewezenTabel() { return nietToegewezenTabel; }
    public ComboBox<Klas> getKlasComboBox() { return klasComboBox; }
    public TableView<LeerlingToewijzingOverzicht> getKlasTabel() { return klasTabel; }
    public Label getDetailTitel() { return detailTitel; }
}
