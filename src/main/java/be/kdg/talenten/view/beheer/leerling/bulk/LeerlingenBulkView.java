package be.kdg.talenten.view.beheer.leerling.bulk;

import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Schooljaar;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LeerlingenBulkView extends BorderPane {
    private final Button terugButton = new Button("← Terug naar leerlingen");
    private final Button themeButton = new Button("☾");
    private final ComboBox<Schooljaar> schooljaarComboBox = new ComboBox<>();
    private final ComboBox<Klas> klasComboBox = new ComboBox<>();
    private final TextArea plakVeld = new TextArea();
    private final Button validerenButton = new Button("Valideren");
    private final Button toevoegenButton = new Button("Leerlingen toevoegen");
    private final Label validatieLabel = new Label();
    private final Label statusLabel = new Label();

    public LeerlingenBulkView() {
        getStyleClass().add("app-root");
        setTop(maakHeader());
        setCenter(maakInhoud());
    }

    private Pane maakHeader() {
        BorderPane header = new BorderPane();
        header.getStyleClass().add("section-header");
        header.setPadding(new Insets(18, 28, 18, 28));
        terugButton.getStyleClass().add("secondary-button");
        header.setLeft(terugButton);
        Label titel = new Label("Leerlingen in bulk toevoegen");
        titel.getStyleClass().add("section-title");
        header.setCenter(titel);
        themeButton.getStyleClass().add("theme-toggle");
        header.setRight(themeButton);
        return header;
    }

    private Pane maakInhoud() {
        VBox inhoud = new VBox(22);
        inhoud.setPadding(new Insets(34, 60, 42, 60));
        inhoud.setAlignment(Pos.TOP_CENTER);
        VBox intro = new VBox(5);
        intro.setMaxWidth(980);
        Label titel = new Label("Leerlingen plakken vanuit Excel");
        titel.getStyleClass().add("content-title");
        Label uitleg = new Label("Selecteer een klas en plak twee Excel-kolommen: Voornaam en Achternaam.");
        uitleg.getStyleClass().add("content-subtitle");
        intro.getChildren().addAll(titel, uitleg);

        HBox keuzes = new HBox(14);
        keuzes.setMaxWidth(980);
        VBox schooljaarBox = maakKeuze("Schooljaar", schooljaarComboBox, "Selecteer een schooljaar");
        VBox klasBox = maakKeuze("Klas", klasComboBox, "Selecteer een klas");
        HBox.setHgrow(schooljaarBox, Priority.ALWAYS);
        HBox.setHgrow(klasBox, Priority.ALWAYS);
        keuzes.getChildren().addAll(schooljaarBox, klasBox);

        VBox kaart = new VBox(12);
        kaart.setMaxWidth(980);
        kaart.setPadding(new Insets(20));
        kaart.getStyleClass().add("content-card");
        plakVeld.setPromptText("Plak hier de Excelkolommen Voornaam | Achternaam");
        plakVeld.setPrefRowCount(10);
        plakVeld.setMinHeight(250);
        plakVeld.setWrapText(false);
        validatieLabel.setWrapText(true);
        validatieLabel.getStyleClass().add("content-subtitle");
        HBox acties = new HBox(10, validatieLabel, validerenButton, toevoegenButton);
        acties.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(validatieLabel, Priority.ALWAYS);
        validerenButton.getStyleClass().add("secondary-button");
        toevoegenButton.getStyleClass().add("primary-button");
        toevoegenButton.setDisable(true);
        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-message");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        kaart.getChildren().addAll(plakVeld, acties, statusLabel);
        inhoud.getChildren().addAll(intro, keuzes, kaart);
        return inhoud;
    }

    private <T> VBox maakKeuze(String tekst, ComboBox<T> combo, String prompt) {
        Label label = new Label(tekst);
        label.getStyleClass().add("field-label");
        combo.setEditable(false);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setPromptText(prompt);
        VBox box = new VBox(7, label, combo);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    public Button getTerugButton() { return terugButton; }
    public Button getThemeButton() { return themeButton; }
    public ComboBox<Schooljaar> getSchooljaarComboBox() { return schooljaarComboBox; }
    public ComboBox<Klas> getKlasComboBox() { return klasComboBox; }
    public TextArea getPlakVeld() { return plakVeld; }
    public Button getValiderenButton() { return validerenButton; }
    public Button getToevoegenButton() { return toevoegenButton; }
    public void toonValidatie(String tekst, boolean fout, boolean opslaan) {
        validatieLabel.setText(tekst);
        validatieLabel.getStyleClass().removeAll("status-success", "status-error");
        validatieLabel.getStyleClass().add(fout ? "status-error" : "status-success");
        toevoegenButton.setDisable(!opslaan);
    }
    public void toonStatus(String tekst, boolean fout) {
        statusLabel.setText(tekst);
        statusLabel.getStyleClass().removeAll("status-success", "status-error");
        statusLabel.getStyleClass().add(fout ? "status-error" : "status-success");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }
    public void updateThemeIcon(boolean darkMode) { themeButton.setText(darkMode ? "☀" : "☾"); }
}
