package be.kdg.talenten.view.leerkracht;

import be.kdg.talenten.domain.Leerkracht;
import be.kdg.talenten.view.navigation.AppSidebar;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    private final Button nieuweLeerkrachtButton;
    private final TableView<Leerkracht> leerkrachtenTable;

    private final TextField voornaamField;
    private final TextField achternaamField;

    private final Button statusWijzigenButton;
    private final Button opslaanButton;
    private final Button annulerenButton;

    private final Label formulierTitelLabel;
    private final Label statusLabel;

    public LeerkrachtView() {
        sidebar = new AppSidebar(
                AppSidebar.Sectie.LEERKRACHTEN,
                "Leerkrachten beheren"
        );

        nieuweLeerkrachtButton = new Button("+ Nieuwe leerkracht");
        nieuweLeerkrachtButton.getStyleClass().add("primary-button");

        leerkrachtenTable = maakLeerkrachtenTable();

        voornaamField = new TextField();
        voornaamField.setPromptText("Voornaam");
        voornaamField.setMaxWidth(Double.MAX_VALUE);

        achternaamField = new TextField();
        achternaamField.setPromptText("Achternaam");
        achternaamField.setMaxWidth(Double.MAX_VALUE);

        statusWijzigenButton = new Button("Op niet-actief zetten");
        statusWijzigenButton.getStyleClass().add("secondary-button");
        statusWijzigenButton.setVisible(false);
        statusWijzigenButton.setManaged(false);

        opslaanButton = new Button("Opslaan");
        opslaanButton.getStyleClass().add("primary-button");

        annulerenButton = new Button("Annuleren");
        annulerenButton.getStyleClass().add("primary-outline-button");

        formulierTitelLabel = new Label("Nieuwe leerkracht");
        formulierTitelLabel.getStyleClass().add("section-heading");

        statusLabel = new Label(
                "Selecteer een leerkracht om de gegevens te wijzigen "
                        + "of maak een nieuwe leerkracht aan."
        );
        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-label");

        layoutNodes();
    }

    private TableView<Leerkracht> maakLeerkrachtenTable() {
        TableView<Leerkracht> table = new TableView<>();
        table.setPlaceholder(new Label("Nog geen leerkrachten gevonden."));
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        TableColumn<Leerkracht, String> voornaamKolom =
                new TableColumn<>("Voornaam");

        voornaamKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getVoornaam()
                )
        );
        voornaamKolom.setPrefWidth(210);

        TableColumn<Leerkracht, String> achternaamKolom =
                new TableColumn<>("Achternaam");

        achternaamKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getAchternaam()
                )
        );
        achternaamKolom.setPrefWidth(230);

        TableColumn<Leerkracht, String> statusKolom =
                new TableColumn<>("Status");

        statusKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().isActief()
                                ? "Actief"
                                : "Niet actief"
                )
        );
        statusKolom.setPrefWidth(110);

        table.getColumns().addAll(
                voornaamKolom,
                achternaamKolom,
                statusKolom
        );

        return table;
    }

    private void layoutNodes() {
        setLeft(sidebar);
        setCenter(maakInhoud());
        getStyleClass().add("app-background");
    }

    private VBox maakInhoud() {
        Label titel = new Label("Leerkrachten beheren");
        titel.getStyleClass().add("page-title");

        Label ondertitel = new Label(
                "Voeg leerkrachten toe, wijzig gegevens "
                        + "en beheer hun actieve status."
        );
        ondertitel.getStyleClass().add("page-subtitle");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox header = new HBox(
                14,
                new VBox(4, titel, ondertitel),
                headerSpacer,
                nieuweLeerkrachtButton
        );
        header.setAlignment(Pos.CENTER_LEFT);

        Label overzichtTitel =
                new Label("Bestaande leerkrachten");
        overzichtTitel.getStyleClass().add("section-heading");

        Label overzichtUitleg = new Label(
                "Selecteer een leerkracht om gegevens "
                        + "of de actieve status te wijzigen."
        );
        overzichtUitleg.setWrapText(true);
        overzichtUitleg.getStyleClass().add("muted-label");

        VBox overzichtCard = new VBox(
                10,
                overzichtTitel,
                overzichtUitleg,
                leerkrachtenTable
        );
        overzichtCard.getStyleClass().add("content-card");
        overzichtCard.setPadding(new Insets(22));
        overzichtCard.setMinWidth(520);

        HBox.setHgrow(overzichtCard, Priority.ALWAYS);
        VBox.setVgrow(leerkrachtenTable, Priority.ALWAYS);

        VBox formulierCard = maakFormulierCard();

        HBox beheer = new HBox(
                16,
                overzichtCard,
                formulierCard
        );

        HBox.setHgrow(overzichtCard, Priority.ALWAYS);
        VBox.setVgrow(beheer, Priority.ALWAYS);

        VBox inhoud = new VBox(
                22,
                header,
                beheer,
                statusLabel
        );

        inhoud.setPadding(new Insets(32));
        VBox.setVgrow(beheer, Priority.ALWAYS);

        return inhoud;
    }

    private VBox maakFormulierCard() {
        Label voornaamLabel = new Label("Voornaam");
        voornaamLabel.getStyleClass().add("field-label");

        Label achternaamLabel = new Label("Achternaam");
        achternaamLabel.getStyleClass().add("field-label");

        Region formulierSpacer = new Region();
        VBox.setVgrow(formulierSpacer, Priority.ALWAYS);

        Region knoppenSpacer = new Region();
        HBox.setHgrow(knoppenSpacer, Priority.ALWAYS);

        HBox knoppen = new HBox(
                10,
                statusWijzigenButton,
                knoppenSpacer,
                annulerenButton,
                opslaanButton
        );
        knoppen.setAlignment(Pos.CENTER_LEFT);

        VBox formulierCard = new VBox(
                10,
                formulierTitelLabel,
                voornaamLabel,
                voornaamField,
                achternaamLabel,
                achternaamField,
                formulierSpacer,
                knoppen
        );

        formulierCard.getStyleClass().add("content-card");
        formulierCard.setPadding(new Insets(22));
        formulierCard.setPrefWidth(410);
        formulierCard.setMinWidth(360);

        return formulierCard;
    }

    public void setLeerkrachten(List<Leerkracht> leerkrachten) {
        leerkrachtenTable.getItems().setAll(leerkrachten);
        leerkrachtenTable.refresh();
    }

    public void toonNieuweLeerkrachtFormulier() {
        formulierTitelLabel.setText("Nieuwe leerkracht");

        voornaamField.clear();
        achternaamField.clear();

        leerkrachtenTable
                .getSelectionModel()
                .clearSelection();

        statusWijzigenButton.setVisible(false);
        statusWijzigenButton.setManaged(false);

        voornaamField.requestFocus();

        setStatus(
                "Vul de gegevens van de nieuwe leerkracht in."
        );
    }

    public void toonLeerkrachtFormulier(Leerkracht leerkracht) {
        formulierTitelLabel.setText("Leerkracht wijzigen");

        voornaamField.setText(
                leerkracht.getVoornaam()
        );

        achternaamField.setText(
                leerkracht.getAchternaam()
        );

        statusWijzigenButton.setVisible(true);
        statusWijzigenButton.setManaged(true);

        if (leerkracht.isActief()) {
            statusWijzigenButton.setText(
                    "Op niet-actief zetten"
            );
        } else {
            statusWijzigenButton.setText(
                    "Op actief zetten"
            );
        }

        setStatus(
                "Je wijzigt "
                        + leerkracht.getVoornaam()
                        + " "
                        + leerkracht.getAchternaam()
                        + "."
        );
    }

    public void setStatus(String tekst) {
        statusLabel.setText(
                tekst == null ? "" : tekst
        );
    }

    public void toonFout(String bericht) {
        Alert alert = new Alert(
                Alert.AlertType.ERROR
        );

        alert.setTitle("Fout");
        alert.setHeaderText(
                "De actie kon niet uitgevoerd worden"
        );
        alert.setContentText(bericht);
        alert.showAndWait();
    }

    public void toonSucces(String bericht) {
        setStatus(bericht);
    }

    public AppSidebar getSidebar() {
        return sidebar;
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

    public Button getStatusWijzigenButton() {
        return statusWijzigenButton;
    }

    public Button getOpslaanButton() {
        return opslaanButton;
    }

    public Button getAnnulerenButton() {
        return annulerenButton;
    }
}