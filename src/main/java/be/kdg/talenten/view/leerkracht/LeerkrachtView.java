package be.kdg.talenten.view.leerkracht;

import be.kdg.talenten.view.navigation.AppSidebar;

import be.kdg.talenten.domain.Leerkracht;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class LeerkrachtView extends BorderPane {
    private final AppSidebar sidebar;
    private Button terugButton;
    private Button nieuweLeerkrachtButton;
    private TableView<Leerkracht> leerkrachtenTable;
    private TextField voornaamField;
    private TextField achternaamField;
    private Button opslaanButton;
    private Button annulerenButton;
    private Label formulierTitelLabel;
    private Label statusLabel;

    public LeerkrachtView() {
        sidebar = new AppSidebar(AppSidebar.Sectie.LEERKRACHTEN, "Leerkrachten beheren");
        initialiseNodes();
        layoutNodes();
    }

    private void initialiseNodes() {
        terugButton = new Button("⌂  Dashboard");
        terugButton.getStyleClass().add("sidebar-button");
        terugButton.setAlignment(Pos.CENTER_LEFT);
        terugButton.setMaxWidth(Double.MAX_VALUE);

        nieuweLeerkrachtButton = new Button("+ Nieuwe leerkracht");
        nieuweLeerkrachtButton.getStyleClass().add("primary-button");

        leerkrachtenTable = maakLeerkrachtenTable();

        voornaamField = new TextField();
        voornaamField.setPromptText("Voornaam");
        voornaamField.setMaxWidth(Double.MAX_VALUE);

        achternaamField = new TextField();
        achternaamField.setPromptText("Achternaam");
        achternaamField.setMaxWidth(Double.MAX_VALUE);

        opslaanButton = new Button("Opslaan");
        opslaanButton.getStyleClass().add("primary-button");

        annulerenButton = new Button("Annuleren");
        annulerenButton.getStyleClass().add("primary-outline-button");

        formulierTitelLabel = new Label("Nieuwe leerkracht");
        formulierTitelLabel.getStyleClass().add("section-heading");

        statusLabel = new Label("Selecteer een leerkracht om de gegevens te wijzigen of maak een nieuwe leerkracht aan.");
        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-label");
    }

    private TableView<Leerkracht> maakLeerkrachtenTable() {
        TableView<Leerkracht> table = new TableView<>();
        table.setPlaceholder(new Label("Nog geen leerkrachten gevonden."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Leerkracht, String> voornaamKolom = new TableColumn<>("Voornaam");
        voornaamKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getVoornaam()));
        voornaamKolom.setPrefWidth(230);

        TableColumn<Leerkracht, String> achternaamKolom = new TableColumn<>("Achternaam");
        achternaamKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAchternaam()));

        table.getColumns().addAll(voornaamKolom, achternaamKolom);
        return table;
    }

    private void layoutNodes() {
        setLeft(sidebar);
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

        Button leerkrachten = new Button("○  Leerkrachten");
        leerkrachten.setMaxWidth(Double.MAX_VALUE);
        leerkrachten.setAlignment(Pos.CENTER_LEFT);
        leerkrachten.getStyleClass().addAll("sidebar-button", "sidebar-button-active");
        leerkrachten.setDisable(true);

        Button talenten = maakPlaceholderButton("✦  Talenten");
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
        Label sub = new Label("Leerkrachten beheren");
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
        Label titel = new Label("Leerkrachten beheren");
        titel.getStyleClass().add("page-title");

        Label ondertitel = new Label("Voeg leerkrachten toe en pas bestaande namen aan.");
        ondertitel.getStyleClass().add("page-subtitle");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox header = new HBox(14, new VBox(4, titel, ondertitel), headerSpacer, nieuweLeerkrachtButton);
        header.setAlignment(Pos.CENTER_LEFT);

        Label overzichtTitel = new Label("Bestaande leerkrachten");
        overzichtTitel.getStyleClass().add("section-heading");

        Label overzichtUitleg = new Label("Selecteer een leerkracht in de tabel om de voornaam of achternaam te wijzigen.");
        overzichtUitleg.setWrapText(true);
        overzichtUitleg.getStyleClass().add("muted-label");

        VBox overzichtCard = new VBox(10, overzichtTitel, overzichtUitleg, leerkrachtenTable);
        overzichtCard.getStyleClass().add("content-card");
        overzichtCard.setPadding(new Insets(22));
        overzichtCard.setMinWidth(520);
        HBox.setHgrow(overzichtCard, Priority.ALWAYS);
        VBox.setVgrow(leerkrachtenTable, Priority.ALWAYS);

        Label voornaamLabel = new Label("Voornaam");
        voornaamLabel.getStyleClass().add("field-label");

        Label achternaamLabel = new Label("Achternaam");
        achternaamLabel.getStyleClass().add("field-label");

        HBox knoppen = new HBox(10, annulerenButton, opslaanButton);
        knoppen.setAlignment(Pos.CENTER_RIGHT);

        Region formulierSpacer = new Region();
        VBox.setVgrow(formulierSpacer, Priority.ALWAYS);

        VBox formulierCard = new VBox(10, formulierTitelLabel, voornaamLabel, voornaamField, achternaamLabel, achternaamField, formulierSpacer, knoppen);
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

    public void setLeerkrachten(List<Leerkracht> leerkrachten) {
        leerkrachtenTable.getItems().setAll(leerkrachten);
        leerkrachtenTable.refresh();
    }

    public void toonNieuweLeerkrachtFormulier() {
        formulierTitelLabel.setText("Nieuwe leerkracht");
        voornaamField.clear();
        achternaamField.clear();
        leerkrachtenTable.getSelectionModel().clearSelection();
        voornaamField.requestFocus();
        setStatus("Vul de gegevens van de nieuwe leerkracht in.");
    }

    public void toonLeerkrachtFormulier(Leerkracht leerkracht) {
        formulierTitelLabel.setText("Leerkracht wijzigen");
        voornaamField.setText(leerkracht.getVoornaam());
        achternaamField.setText(leerkracht.getAchternaam());
        setStatus("Je wijzigt " + leerkracht.getVoornaam() + " " + leerkracht.getAchternaam() + ".");
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

    public Button getNieuweLeerkrachtButton() {
        return nieuweLeerkrachtButton;
    }

    public TableView<Leerkracht> getLeerkrachtenTable() {
        return leerkrachtenTable;
    }

    public TextField getVoornaamField() {
        return voornaamField;
    }

    public TextField getAchternaamField() {
        return achternaamField;
    }

    public Button getOpslaanButton() {
        return opslaanButton;
    }

    public Button getAnnulerenButton() {
        return annulerenButton;
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }
}
