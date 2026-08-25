package be.kdg.talenten.view.leerling;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.view.navigation.AppSidebar;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;

public class LeerlingView extends BorderPane {

    private final AppSidebar sidebar;

    private Button nieuweLeerlingButton;
    private ComboBox<Schooljaar> schooljaarComboBox;
    private TableView<Leerling> leerlingenTable;

    private TextField voornaamField;
    private TextField achternaamField;
    private ComboBox<Klas> klasComboBox;

    private Button opslaanButton;
    private Button annulerenButton;
    private Button actiefButton;

    private Label formulierTitelLabel;
    private Label statusLabel;

    public LeerlingView() {
        sidebar = new AppSidebar(
                AppSidebar.Sectie.LEERLINGEN,
                "Leerlingen beheren"
        );

        initialiseNodes();
        layoutNodes();
    }

    private void initialiseNodes() {
        nieuweLeerlingButton = new Button("+ Nieuwe leerling");
        nieuweLeerlingButton.getStyleClass().add("primary-button");

        schooljaarComboBox = new ComboBox<>();
        schooljaarComboBox.setPromptText("Selecteer schooljaar");
        schooljaarComboBox.setPrefWidth(210);

        schooljaarComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Schooljaar schooljaar) {
                return schooljaar == null
                        ? ""
                        : schooljaar.getNaam();
            }

            @Override
            public Schooljaar fromString(String string) {
                return null;
            }
        });

        leerlingenTable = maakLeerlingenTable();

        voornaamField = new TextField();
        voornaamField.setPromptText("Voornaam");
        voornaamField.setMaxWidth(Double.MAX_VALUE);

        achternaamField = new TextField();
        achternaamField.setPromptText("Achternaam");
        achternaamField.setMaxWidth(Double.MAX_VALUE);

        klasComboBox = new ComboBox<>();
        klasComboBox.setPromptText("Selecteer klas");
        klasComboBox.setMaxWidth(Double.MAX_VALUE);

        klasComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Klas klas) {
                if (klas == null) {
                    return "";
                }

                return klas.getNaam()
                        + " · leerjaar "
                        + klas.getLeerjaar();
            }

            @Override
            public Klas fromString(String string) {
                return null;
            }
        });

        opslaanButton = new Button("Opslaan");
        opslaanButton.getStyleClass().add("primary-button");

        annulerenButton = new Button("Annuleren");
        annulerenButton.getStyleClass().add("primary-outline-button");

        actiefButton = new Button("Op niet-actief zetten");
        actiefButton.getStyleClass().add("primary-outline-button");
        actiefButton.setVisible(false);
        actiefButton.setManaged(false);

        formulierTitelLabel = new Label("Nieuwe leerling");
        formulierTitelLabel.getStyleClass().add("section-heading");

        statusLabel = new Label(
                "Selecteer een schooljaar. Daarna kan je leerlingen toevoegen of wijzigen."
        );
        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-label");
    }

    private TableView<Leerling> maakLeerlingenTable() {
        TableView<Leerling> table = new TableView<>();

        table.setPlaceholder(
                new Label(
                        "Nog geen leerlingen gevonden voor dit schooljaar."
                )
        );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        TableColumn<Leerling, String> voornaamKolom =
                new TableColumn<>("Voornaam");

        voornaamKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getVoornaam()
                )
        );
        voornaamKolom.setPrefWidth(140);

        TableColumn<Leerling, String> achternaamKolom =
                new TableColumn<>("Achternaam");

        achternaamKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getAchternaam()
                )
        );
        achternaamKolom.setPrefWidth(175);

        TableColumn<Leerling, String> klasKolom =
                new TableColumn<>("Klas");

        klasKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getKlas().getNaam()
                )
        );
        klasKolom.setPrefWidth(90);

        TableColumn<Leerling, String> leerjaarKolom =
                new TableColumn<>("Leerjaar");

        leerjaarKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        String.valueOf(
                                data.getValue()
                                        .getKlas()
                                        .getLeerjaar()
                        )
                )
        );
        leerjaarKolom.setPrefWidth(80);

        TableColumn<Leerling, String> doelgroepKolom =
                new TableColumn<>("Doelgroep");

        doelgroepKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        formatteerDoelgroep(
                                data.getValue()
                                        .getKlas()
                                        .getDoelgroep()
                        )
                )
        );
        doelgroepKolom.setPrefWidth(250);

        TableColumn<Leerling, String> statusKolom =
                new TableColumn<>("Deelname");

        statusKolom.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().isActief()
                                ? "Neemt deel"
                                : "Neemt niet deel"
                )
        );
        statusKolom.setPrefWidth(100);

        table.getColumns().addAll(
                voornaamKolom,
                achternaamKolom,
                klasKolom,
                leerjaarKolom,
                doelgroepKolom,
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
        Label titel = new Label("Leerlingen beheren");
        titel.getStyleClass().add("page-title");

        Label ondertitel = new Label(
                "Voeg leerlingen toe, wijzig hun gegevens, verplaats ze naar een andere klas of wijzig hun status."
        );
        ondertitel.getStyleClass().add("page-subtitle");

        Label schooljaarLabel = new Label("Schooljaar");
        schooljaarLabel.getStyleClass().add("field-label");

        VBox schooljaarBox = new VBox(
                5,
                schooljaarLabel,
                schooljaarComboBox
        );

        Region headerSpacer = new Region();
        HBox.setHgrow(
                headerSpacer,
                Priority.ALWAYS
        );

        HBox header = new HBox(
                18,
                new VBox(
                        4,
                        titel,
                        ondertitel
                ),
                headerSpacer,
                schooljaarBox,
                nieuweLeerlingButton
        );

        header.setAlignment(
                Pos.BOTTOM_LEFT
        );

        Label overzichtTitel =
                new Label("Leerlingen");

        overzichtTitel
                .getStyleClass()
                .add("section-heading");

        Label overzichtUitleg =
                new Label(
                        "Selecteer een leerling in de tabel om de gegevens, klas of status te wijzigen."
                );

        overzichtUitleg.setWrapText(true);
        overzichtUitleg
                .getStyleClass()
                .add("muted-label");

        VBox overzichtCard = new VBox(
                10,
                overzichtTitel,
                overzichtUitleg,
                leerlingenTable
        );

        overzichtCard
                .getStyleClass()
                .add("content-card");

        overzichtCard.setPadding(
                new Insets(22)
        );

        overzichtCard.setMinWidth(600);

        HBox.setHgrow(
                overzichtCard,
                Priority.ALWAYS
        );

        VBox.setVgrow(
                leerlingenTable,
                Priority.ALWAYS
        );

        Label voornaamLabel =
                new Label("Voornaam");

        voornaamLabel
                .getStyleClass()
                .add("field-label");

        Label achternaamLabel =
                new Label("Achternaam");

        achternaamLabel
                .getStyleClass()
                .add("field-label");

        Label klasLabel =
                new Label("Klas");

        klasLabel
                .getStyleClass()
                .add("field-label");

        Region knoppenSpacer =
                new Region();

        HBox.setHgrow(
                knoppenSpacer,
                Priority.ALWAYS
        );

        HBox knoppen = new HBox(
                10,
                actiefButton,
                knoppenSpacer,
                annulerenButton,
                opslaanButton
        );

        knoppen.setAlignment(
                Pos.CENTER_RIGHT
        );

        Region formulierSpacer =
                new Region();

        VBox.setVgrow(
                formulierSpacer,
                Priority.ALWAYS
        );

        VBox formulierCard = new VBox(
                10,
                formulierTitelLabel,
                voornaamLabel,
                voornaamField,
                achternaamLabel,
                achternaamField,
                klasLabel,
                klasComboBox,
                formulierSpacer,
                knoppen
        );

        formulierCard
                .getStyleClass()
                .add("content-card");

        formulierCard.setPadding(
                new Insets(22)
        );

        formulierCard.setPrefWidth(420);
        formulierCard.setMinWidth(390);

        HBox beheer = new HBox(
                16,
                overzichtCard,
                formulierCard
        );

        HBox.setHgrow(
                overzichtCard,
                Priority.ALWAYS
        );

        VBox.setVgrow(
                beheer,
                Priority.ALWAYS
        );

        VBox inhoud = new VBox(
                22,
                header,
                beheer,
                statusLabel
        );

        inhoud.setPadding(
                new Insets(32)
        );

        VBox.setVgrow(
                beheer,
                Priority.ALWAYS
        );

        return inhoud;
    }

    public void setSchooljaren(List<Schooljaar> schooljaren) {
        schooljaarComboBox.setItems(
                FXCollections.observableArrayList(
                        schooljaren
                )
        );
    }

    public void setKlassen(List<Klas> klassen) {
        klasComboBox.setItems(
                FXCollections.observableArrayList(
                        klassen
                )
        );
    }

    public void setLeerlingen(List<Leerling> leerlingen) {
        leerlingenTable
                .getItems()
                .setAll(leerlingen);

        leerlingenTable.refresh();
    }

    public void toonNieuweLeerlingFormulier() {
        formulierTitelLabel.setText(
                "Nieuwe leerling"
        );

        voornaamField.clear();
        achternaamField.clear();

        klasComboBox
                .getSelectionModel()
                .clearSelection();

        leerlingenTable
                .getSelectionModel()
                .clearSelection();

        actiefButton.setVisible(false);
        actiefButton.setManaged(false);

        voornaamField.requestFocus();

        setStatus(
                "Vul de gegevens van de nieuwe leerling in en selecteer een klas."
        );
    }

    public void toonLeerlingFormulier(Leerling leerling) {
        formulierTitelLabel.setText(
                "Leerling wijzigen"
        );

        voornaamField.setText(
                leerling.getVoornaam()
        );

        achternaamField.setText(
                leerling.getAchternaam()
        );

        klasComboBox
                .getSelectionModel()
                .select(leerling.getKlas());

        actiefButton.setVisible(true);
        actiefButton.setManaged(true);

        if (leerling.isActief()) {
            actiefButton.setText(
                    "Niet meer laten deelnemen"
            );
        } else {
            actiefButton.setText(
                    "Opnieuw laten deelnemen"
            );
        }

        setStatus(
                "Je wijzigt "
                        + leerling.getVoornaam()
                        + " "
                        + leerling.getAchternaam()
                        + ". Status: "
                        + (
                        leerling.isActief()
                                ? "actief."
                                : "niet actief."
                )
        );
    }

    public void setStatus(String tekst) {
        statusLabel.setText(
                tekst == null
                        ? ""
                        : tekst
        );
    }

    public void toonFout(String bericht) {
        Alert alert =
                new Alert(
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

    private String formatteerDoelgroep(
            Doelgroep doelgroep
    ) {
        if (doelgroep == null) {
            return "";
        }

        return switch (doelgroep) {
            case OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB ->
                    "Observatie / opleidingsfase / 1e graad A-B";

            case KWALIFICATIEFASE_TWEEDEGRAAD_AB ->
                    "Kwalificatiefase / 2e graad A-B";
        };
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }

    public Button getNieuweLeerlingButton() {
        return nieuweLeerlingButton;
    }

    public ComboBox<Schooljaar> getSchooljaarComboBox() {
        return schooljaarComboBox;
    }

    public TableView<Leerling> getLeerlingenTable() {
        return leerlingenTable;
    }

    public TextField getVoornaamField() {
        return voornaamField;
    }

    public TextField getAchternaamField() {
        return achternaamField;
    }

    public ComboBox<Klas> getKlasComboBox() {
        return klasComboBox;
    }

    public Button getOpslaanButton() {
        return opslaanButton;
    }

    public Button getAnnulerenButton() {
        return annulerenButton;
    }

    public Button getActiefButton() {
        return actiefButton;
    }
}