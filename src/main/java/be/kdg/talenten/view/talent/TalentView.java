package be.kdg.talenten.view.talent;

import be.kdg.talenten.domain.Talent;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class TalentView extends BorderPane {
    private Button terugButton;
    private Button nieuwTalentButton;
    private TableView<Talent> talentenTable;
    private TextField naamField;
    private TextArea beschrijvingArea;
    private Button opslaanButton;
    private Button annulerenButton;
    private Label formulierTitelLabel;
    private Label statusLabel;

    public TalentView() {
        initialiseNodes();
        layoutNodes();
    }

    private void initialiseNodes() {
        terugButton = new Button("⌂  Dashboard");
        terugButton.getStyleClass().add("sidebar-button");
        terugButton.setAlignment(Pos.CENTER_LEFT);
        terugButton.setMaxWidth(Double.MAX_VALUE);

        nieuwTalentButton = new Button("+ Nieuw talent");
        nieuwTalentButton.getStyleClass().add("primary-button");

        talentenTable = maakTalentenTable();

        naamField = new TextField();
        naamField.setPromptText("Naam van het talent");
        naamField.setMaxWidth(Double.MAX_VALUE);

        beschrijvingArea = new TextArea();
        beschrijvingArea.setPromptText("Beschrijving van het talent");
        beschrijvingArea.setWrapText(true);
        beschrijvingArea.setPrefRowCount(7);

        opslaanButton = new Button("Opslaan");
        opslaanButton.getStyleClass().add("primary-button");

        annulerenButton = new Button("Annuleren");
        annulerenButton.getStyleClass().add("primary-outline-button");

        formulierTitelLabel = new Label("Nieuw talent");
        formulierTitelLabel.getStyleClass().add("section-heading");

        statusLabel = new Label("Selecteer een talent om het te wijzigen of maak een nieuw talent aan.");
        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-label");
    }

    private TableView<Talent> maakTalentenTable() {
        TableView<Talent> table = new TableView<>();
        table.setPlaceholder(new Label("Nog geen talenten gevonden."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Talent, String> naamKolom = new TableColumn<>("Naam");
        naamKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNaam()));
        naamKolom.setPrefWidth(230);

        TableColumn<Talent, String> beschrijvingKolom = new TableColumn<>("Beschrijving");
        beschrijvingKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getBeschrijving()));

        table.getColumns().addAll(naamKolom, beschrijvingKolom);
        return table;
    }

    private void layoutNodes() {
        setLeft(maakSidebar());
        setCenter(maakInhoud());
        getStyleClass().add("app-background");
    }

    private VBox maakSidebar() {
        Label logo = new Label("◉");
        logo.getStyleClass().add("brand-mark");

        Label brand = new Label("Talentontwikkeling");
        brand.getStyleClass().add("sidebar-brand");

        HBox brandBox = new HBox(10, logo, brand);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setPadding(new Insets(20, 18, 18, 18));

        Separator separator = new Separator();
        separator.getStyleClass().add("sidebar-separator");

        Button leerlingen = maakPlaceholderButton("○  Leerlingen");
        Button klassen = maakPlaceholderButton("▣  Klassen");
        Button leerkrachten = maakPlaceholderButton("○  Leerkrachten");

        Button talenten = new Button("✦  Talenten");
        talenten.setMaxWidth(Double.MAX_VALUE);
        talenten.setAlignment(Pos.CENTER_LEFT);
        talenten.getStyleClass().addAll("sidebar-button", "sidebar-button-active");
        talenten.setDisable(true);

        Button periodes = maakPlaceholderButton("◷  Talentenperiodes");
        Button ingerichte = maakPlaceholderButton("▤  Ingerichte talenten");
        Button voorkeuren = maakPlaceholderButton("⇩  Voorkeuren importeren");
        Button verdeling = maakPlaceholderButton("◎  Automatische verdeling");
        Button bekijken = maakPlaceholderButton("♙  Toewijzingen bekijken");
        Button manueel = maakPlaceholderButton("↔  Manuele toewijzingen");

        VBox menu = new VBox(4, terugButton, leerlingen, klassen, leerkrachten, talenten, periodes, ingerichte, maakMenuTussenruimte(), voorkeuren, verdeling, bekijken, manueel);
        menu.setPadding(new Insets(12));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label rol = new Label("Talentcoördinator");
        rol.getStyleClass().add("sidebar-user-name");
        Label sub = new Label("Talenten beheren");
        sub.getStyleClass().add("sidebar-user-subtitle");
        VBox gebruiker = new VBox(3, rol, sub);
        gebruiker.setPadding(new Insets(16));
        gebruiker.getStyleClass().add("sidebar-user-box");

        VBox sidebar = new VBox(brandBox, separator, menu, spacer, gebruiker);
        sidebar.setPrefWidth(245);
        sidebar.setMinWidth(225);
        sidebar.getStyleClass().add("sidebar");
        return sidebar;
    }

    private Button maakPlaceholderButton(String tekst) {
        Button button = new Button(tekst);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.getStyleClass().add("sidebar-button");
        button.setDisable(true);
        return button;
    }

    private Region maakMenuTussenruimte() {
        Region region = new Region();
        region.setPrefHeight(10);
        return region;
    }

    private VBox maakInhoud() {
        Label titel = new Label("Talenten beheren");
        titel.getStyleClass().add("page-title");

        Label ondertitel = new Label("Maak basistalenten aan en pas bestaande talenten aan.");
        ondertitel.getStyleClass().add("page-subtitle");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox header = new HBox(14, new VBox(4, titel, ondertitel), headerSpacer, nieuwTalentButton);
        header.setAlignment(Pos.CENTER_LEFT);

        Label overzichtTitel = new Label("Bestaande talenten");
        overzichtTitel.getStyleClass().add("section-heading");

        Label overzichtUitleg = new Label("Selecteer een talent in de tabel om de naam of beschrijving te wijzigen.");
        overzichtUitleg.setWrapText(true);
        overzichtUitleg.getStyleClass().add("muted-label");

        VBox overzichtCard = new VBox(10, overzichtTitel, overzichtUitleg, talentenTable);
        overzichtCard.getStyleClass().add("content-card");
        overzichtCard.setPadding(new Insets(22));
        overzichtCard.setMinWidth(520);
        HBox.setHgrow(overzichtCard, Priority.ALWAYS);
        VBox.setVgrow(talentenTable, Priority.ALWAYS);

        Label naamLabel = new Label("Naam");
        naamLabel.getStyleClass().add("field-label");

        Label beschrijvingLabel = new Label("Beschrijving");
        beschrijvingLabel.getStyleClass().add("field-label");

        HBox knoppen = new HBox(10, annulerenButton, opslaanButton);
        knoppen.setAlignment(Pos.CENTER_RIGHT);

        Region formulierSpacer = new Region();
        VBox.setVgrow(formulierSpacer, Priority.ALWAYS);

        VBox formulierCard = new VBox(10, formulierTitelLabel, naamLabel, naamField, beschrijvingLabel, beschrijvingArea, formulierSpacer, knoppen);
        formulierCard.getStyleClass().add("content-card");
        formulierCard.setPadding(new Insets(22));
        formulierCard.setPrefWidth(390);
        formulierCard.setMinWidth(340);

        HBox beheer = new HBox(16, overzichtCard, formulierCard);
        HBox.setHgrow(overzichtCard, Priority.ALWAYS);
        VBox.setVgrow(beheer, Priority.ALWAYS);

        VBox inhoud = new VBox(22, header, beheer, statusLabel);
        inhoud.setPadding(new Insets(32));
        VBox.setVgrow(beheer, Priority.ALWAYS);
        return inhoud;
    }

    public void setTalenten(List<Talent> talenten) {
        talentenTable.getItems().setAll(talenten);
        talentenTable.refresh();
    }

    public void toonNieuwTalentFormulier() {
        formulierTitelLabel.setText("Nieuw talent");
        naamField.clear();
        beschrijvingArea.clear();
        talentenTable.getSelectionModel().clearSelection();
        naamField.requestFocus();
        setStatus("Vul de gegevens van het nieuwe talent in.");
    }

    public void toonTalentFormulier(Talent talent) {
        formulierTitelLabel.setText("Talent wijzigen");
        naamField.setText(talent.getNaam());
        beschrijvingArea.setText(talent.getBeschrijving());
        setStatus("Je wijzigt " + talent.getNaam() + ".");
    }

    public void setStatus(String tekst) {
        statusLabel.setText(tekst == null ? "" : tekst);
    }

    public void toonFout(String bericht) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fout");
        alert.setHeaderText("De actie kon niet uitgevoerd worden");
        alert.setContentText(bericht);
        alert.showAndWait();
    }

    public void toonSucces(String bericht) {
        setStatus(bericht);
    }

    public Button getTerugButton() {
        return terugButton;
    }

    public Button getNieuwTalentButton() {
        return nieuwTalentButton;
    }

    public TableView<Talent> getTalentenTable() {
        return talentenTable;
    }

    public TextField getNaamField() {
        return naamField;
    }

    public TextArea getBeschrijvingArea() {
        return beschrijvingArea;
    }

    public Button getOpslaanButton() {
        return opslaanButton;
    }

    public Button getAnnulerenButton() {
        return annulerenButton;
    }
}
