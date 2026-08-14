package be.kdg.talenten.view.voorkeuren;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.VoorkeurImportProbleem;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VoorkeurenView extends BorderPane {
    private final DateTimeFormatter datumFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Button terugButton;
    private ComboBox<Schooljaar> schooljaarComboBox;
    private ComboBox<TalentenPeriode> periodeComboBox;
    private ComboBox<Doelgroep> doelgroepComboBox;
    private Button genereerButton;
    private Button importeerButton;
    private Label statusLabel;
    private TableView<VoorkeurImportProbleem> problemenTable;

    public VoorkeurenView() {
        initialiseNodes();
        layoutNodes();
    }

    private void initialiseNodes() {
        terugButton = new Button("⌂  Dashboard");
        terugButton.getStyleClass().add("sidebar-button");
        terugButton.setAlignment(Pos.CENTER_LEFT);
        terugButton.setMaxWidth(Double.MAX_VALUE);

        schooljaarComboBox = new ComboBox<>();
        schooljaarComboBox.setPromptText("Selecteer schooljaar");
        schooljaarComboBox.setMaxWidth(Double.MAX_VALUE);
        schooljaarComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Schooljaar schooljaar) {
                return schooljaar == null ? "" : schooljaar.getNaam();
            }

            @Override
            public Schooljaar fromString(String string) {
                return null;
            }
        });

        periodeComboBox = new ComboBox<>();
        periodeComboBox.setPromptText("Selecteer talentenperiode");
        periodeComboBox.setMaxWidth(Double.MAX_VALUE);
        periodeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TalentenPeriode periode) {
                if (periode == null) return "";
                return periode.getNaam() + " (" + datumFormatter.format(periode.getStartDatum()) + " - " + datumFormatter.format(periode.getEindDatum()) + ")";
            }

            @Override
            public TalentenPeriode fromString(String string) {
                return null;
            }
        });

        doelgroepComboBox = new ComboBox<>();
        doelgroepComboBox.setItems(FXCollections.observableArrayList(Doelgroep.values()));
        doelgroepComboBox.getSelectionModel().selectFirst();
        doelgroepComboBox.setMaxWidth(Double.MAX_VALUE);
        doelgroepComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Doelgroep doelgroep) {
                if (doelgroep == null) return "";
                return formatteerDoelgroep(doelgroep);
            }

            @Override
            public Doelgroep fromString(String string) {
                return null;
            }
        });

        genereerButton = new Button("Exceltemplate genereren");
        genereerButton.getStyleClass().add("primary-button");
        genereerButton.setMaxWidth(Double.MAX_VALUE);

        importeerButton = new Button("Ingevuld Excelbestand importeren");
        importeerButton.getStyleClass().add("primary-outline-button");
        importeerButton.setMaxWidth(Double.MAX_VALUE);

        statusLabel = new Label("Selecteer een schooljaar, periode en doelgroep.");
        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-label");

        problemenTable = maakProblemenTable();
    }

    private TableView<VoorkeurImportProbleem> maakProblemenTable() {
        TableView<VoorkeurImportProbleem> table = new TableView<>();
        table.setPlaceholder(new Label("Nog geen importproblemen gevonden."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<VoorkeurImportProbleem, String> leerlingKolom = new TableColumn<>("Leerling");
        leerlingKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getLeerling().toString()));

        TableColumn<VoorkeurImportProbleem, String> klasKolom = new TableColumn<>("Klas");
        klasKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getLeerling().getKlas().getNaam()));

        TableColumn<VoorkeurImportProbleem, String> redenKolom = new TableColumn<>("Probleem");
        redenKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getReden()));

        table.getColumns().addAll(leerlingKolom, klasKolom, redenKolom);
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
        Button talenten = maakPlaceholderButton("✦  Talenten");
        Button periodes = maakPlaceholderButton("◷  Talentenperiodes");
        Button ingerichte = maakPlaceholderButton("▤  Ingerichte talenten");

        Button voorkeuren = new Button("⇩  Voorkeuren importeren");
        voorkeuren.setMaxWidth(Double.MAX_VALUE);
        voorkeuren.setAlignment(Pos.CENTER_LEFT);
        voorkeuren.getStyleClass().addAll("sidebar-button", "sidebar-button-active");

        Button verdeling = maakPlaceholderButton("◎  Automatische verdeling");
        Button bekijken = maakPlaceholderButton("♙  Toewijzingen bekijken");
        Button manueel = maakPlaceholderButton("↔  Manuele toewijzingen");

        VBox menu = new VBox(4, terugButton, leerlingen, klassen, leerkrachten, talenten, periodes, ingerichte, maakMenuTussenruimte(), voorkeuren, verdeling, bekijken, manueel);
        menu.setPadding(new Insets(12));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label rol = new Label("Talentcoördinator");
        rol.getStyleClass().add("sidebar-user-name");
        Label sub = new Label("Voorkeuren beheren");
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
        Label titel = new Label("Voorkeuren importeren");
        titel.getStyleClass().add("page-title");
        Label ondertitel = new Label("Genereer een invulbestand per doelgroep en importeer het ingevulde Excelbestand opnieuw.");
        ondertitel.getStyleClass().add("page-subtitle");

        VBox header = new VBox(4, titel, ondertitel);

        Label selectieTitel = new Label("1. Selecteer de periode en doelgroep");
        selectieTitel.getStyleClass().add("section-heading");

        HBox selectie = new HBox(14,
                maakVeld("Schooljaar", schooljaarComboBox),
                maakVeld("Talentenperiode", periodeComboBox),
                maakVeld("Doelgroep", doelgroepComboBox)
        );
        HBox.setHgrow(selectie.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(selectie.getChildren().get(1), Priority.ALWAYS);
        HBox.setHgrow(selectie.getChildren().get(2), Priority.ALWAYS);

        VBox selectieCard = new VBox(16, selectieTitel, selectie);
        selectieCard.getStyleClass().add("content-card");
        selectieCard.setPadding(new Insets(22));

        Label genereerTitel = new Label("2. Excel invulbestand maken");
        genereerTitel.getStyleClass().add("section-heading");
        Label genereerTekst = new Label("De applicatie maakt per klas een tabblad met de leerlingen en drie keuzelijsten. Namen zijn vergrendeld; alleen de voorkeuren kunnen aangepast worden.");
        genereerTekst.setWrapText(true);
        genereerTekst.getStyleClass().add("muted-label");
        VBox genereerCard = new VBox(12, genereerTitel, genereerTekst, genereerButton);
        genereerCard.getStyleClass().add("content-card");
        genereerCard.setPadding(new Insets(22));

        Label importTitel = new Label("3. Ingevuld bestand importeren");
        importTitel.getStyleClass().add("section-heading");
        Label importTekst = new Label("Bij een herimport worden bestaande voorkeuren en oude importproblemen voor de leerlingen in het bestand vervangen. Geldige keuzes worden ook opgeslagen wanneer niet alle drie de keuzes geldig zijn.");
        importTekst.setWrapText(true);
        importTekst.getStyleClass().add("muted-label");
        VBox importCard = new VBox(12, importTitel, importTekst, importeerButton);
        importCard.getStyleClass().add("content-card");
        importCard.setPadding(new Insets(22));

        HBox acties = new HBox(16, genereerCard, importCard);
        HBox.setHgrow(genereerCard, Priority.ALWAYS);
        HBox.setHgrow(importCard, Priority.ALWAYS);
        genereerCard.setMaxWidth(Double.MAX_VALUE);
        importCard.setMaxWidth(Double.MAX_VALUE);

        Label problemenTitel = new Label("Importproblemen");
        problemenTitel.getStyleClass().add("section-heading");
        Label problemenUitleg = new Label("Leerlingen met minder dan drie geldige voorkeuren worden later niet automatisch verdeeld. De geldige voorkeuren blijven wel bewaard.");
        problemenUitleg.setWrapText(true);
        problemenUitleg.getStyleClass().add("muted-label");
        VBox problemenCard = new VBox(10, problemenTitel, problemenUitleg, problemenTable);
        problemenCard.getStyleClass().add("content-card");
        problemenCard.setPadding(new Insets(20));
        VBox.setVgrow(problemenTable, Priority.ALWAYS);

        VBox inhoud = new VBox(22, header, selectieCard, acties, problemenCard, statusLabel);
        inhoud.setPadding(new Insets(32));
        VBox.setVgrow(problemenCard, Priority.ALWAYS);
        return inhoud;
    }

    private VBox maakVeld(String labelTekst, ComboBox<?> comboBox) {
        Label label = new Label(labelTekst);
        label.getStyleClass().add("field-label");
        VBox box = new VBox(7, label, comboBox);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private String formatteerDoelgroep(Doelgroep doelgroep) {
        return switch (doelgroep) {
            case OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB -> "Observatie / opleidingsfase / 1e graad AB";
            case KWALIFICATIEFASE_TWEEDEGRAAD_AB -> "Kwalificatiefase / 2e graad AB";
        };
    }

    public void setSchooljaren(List<Schooljaar> schooljaren) {
        schooljaarComboBox.setItems(FXCollections.observableArrayList(schooljaren));
        schooljaarComboBox.setDisable(schooljaren.isEmpty());
    }

    public void setPeriodes(List<TalentenPeriode> periodes) {
        periodeComboBox.setItems(FXCollections.observableArrayList(periodes));
        periodeComboBox.setDisable(periodes.isEmpty());
    }

    public void setProblemen(List<VoorkeurImportProbleem> problemen) {
        problemenTable.setItems(FXCollections.observableArrayList(problemen));
    }

    public Path kiesOpslagBestand(String voorgesteldeNaam) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exceltemplate opslaan");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excelbestand (*.xlsx)", "*.xlsx"));
        chooser.setInitialFileName(voorgesteldeNaam);
        File bestand = chooser.showSaveDialog(getScene() == null ? null : getScene().getWindow());
        return bestand == null ? null : bestand.toPath();
    }

    public Path kiesImportBestand() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Ingevuld voorkeurenbestand kiezen");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excelbestand (*.xlsx)", "*.xlsx"));
        File bestand = chooser.showOpenDialog(getScene() == null ? null : getScene().getWindow());
        return bestand == null ? null : bestand.toPath();
    }

    public boolean vraagBevestiging(String titel, String boodschap) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titel);
        alert.setHeaderText(titel);
        alert.setResizable(true);

        if (getScene() != null && getScene().getWindow() != null) {
            alert.initOwner(getScene().getWindow());
        }

        Label boodschapLabel = new Label(boodschap);
        boodschapLabel.setWrapText(true);
        boodschapLabel.setPrefWidth(520);
        boodschapLabel.setMaxWidth(520);
        boodschapLabel.getStyleClass().add("dialog-message");

        ButtonType bevestigen = new ButtonType("Importeren", ButtonBar.ButtonData.OK_DONE);
        ButtonType annuleren = new ButtonType("Annuleren", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(annuleren, bevestigen);

        alert.getDialogPane().setContent(boodschapLabel);
        alert.getDialogPane().setPrefWidth(600);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().getStyleClass().add("app-dialog");

        URL stylesheet = getClass().getResource("/css/application.css");
        if (stylesheet != null) {
            alert.getDialogPane().getStylesheets().add(stylesheet.toExternalForm());
        }

        Button bevestigButton = (Button) alert.getDialogPane().lookupButton(bevestigen);
        bevestigButton.getStyleClass().add("primary-button");

        Button annuleerButton = (Button) alert.getDialogPane().lookupButton(annuleren);
        annuleerButton.getStyleClass().add("secondary-button");

        return alert.showAndWait().orElse(annuleren) == bevestigen;
    }

    public void toonMelding(String melding) {
        statusLabel.setText(melding);
        statusLabel.getStyleClass().removeAll("status-success", "status-error", "status-warning");
    }

    public void toonSucces(String melding) {
        statusLabel.setText(melding);
        statusLabel.getStyleClass().removeAll("status-error", "status-warning");
        if (!statusLabel.getStyleClass().contains("status-success")) statusLabel.getStyleClass().add("status-success");
    }

    public void toonWaarschuwing(String melding) {
        statusLabel.setText(melding);
        statusLabel.getStyleClass().removeAll("status-success", "status-error");
        if (!statusLabel.getStyleClass().contains("status-warning")) statusLabel.getStyleClass().add("status-warning");
    }

    public void toonFout(String melding) {
        statusLabel.setText(melding);
        statusLabel.getStyleClass().removeAll("status-success", "status-warning");
        if (!statusLabel.getStyleClass().contains("status-error")) statusLabel.getStyleClass().add("status-error");
    }

    public Button getTerugButton() {
        return terugButton;
    }

    public ComboBox<Schooljaar> getSchooljaarComboBox() {
        return schooljaarComboBox;
    }

    public ComboBox<TalentenPeriode> getPeriodeComboBox() {
        return periodeComboBox;
    }

    public ComboBox<Doelgroep> getDoelgroepComboBox() {
        return doelgroepComboBox;
    }

    public Button getGenereerButton() {
        return genereerButton;
    }

    public Button getImporteerButton() {
        return importeerButton;
    }
}
