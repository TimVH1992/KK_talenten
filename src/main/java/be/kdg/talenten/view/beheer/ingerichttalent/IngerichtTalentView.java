package be.kdg.talenten.view.beheer.ingerichttalent;

import be.kdg.talenten.domain.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.stream.Collectors;

public class IngerichtTalentView extends BorderPane {
    private final Button terugButton = new Button("← Terug naar beheer");
    private final Button themeButton = new Button("☾");
    private final ComboBox<Schooljaar> schooljaarComboBox = new ComboBox<>();
    private final ComboBox<TalentenPeriode> periodeComboBox = new ComboBox<>();
    private final ComboBox<Doelgroep> doelgroepComboBox = new ComboBox<>();
    private final TableView<IngerichtTalent> tabel = new TableView<>();
    private final Button toevoegenButton = new Button("+ Talent inrichten");
    private final Button wijzigenButton = new Button("Wijzigen");
    private final Button actiefWijzigenButton = new Button("Niet actief maken");
    private final Label aantalLabel = new Label();
    private final Label statusLabel = new Label();

    public IngerichtTalentView() {
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
        Label titel = new Label("Ingerichte talenten");
        titel.getStyleClass().add("section-title");
        header.setCenter(titel);
        themeButton.getStyleClass().add("theme-toggle");
        themeButton.setTooltip(new Tooltip("Wissel tussen lichte en donkere modus"));
        header.setRight(themeButton);
        return header;
    }

    private Pane maakInhoud() {
        VBox inhoud = new VBox(16);
        inhoud.setPadding(new Insets(24, 48, 30, 48));
        inhoud.setAlignment(Pos.TOP_CENTER);

        VBox intro = new VBox(4);
        intro.setMaxWidth(1100);
        Label titel = new Label("Ingerichte talenten");
        titel.getStyleClass().add("content-title");
        Label uitleg = new Label("Beheer welke talenten in een talentenperiode aangeboden worden.");
        uitleg.getStyleClass().add("content-subtitle");
        intro.getChildren().addAll(titel, uitleg);

        HBox keuzes = new HBox(12);
        keuzes.setMaxWidth(1100);
        VBox schooljaar = maakKeuze("Schooljaar", schooljaarComboBox, "Selecteer een schooljaar");
        VBox periode = maakKeuze("Talentenperiode", periodeComboBox, "Selecteer een talentenperiode");
        VBox doelgroep = maakKeuze("Doelgroep", doelgroepComboBox, "Selecteer een doelgroep");
        HBox.setHgrow(schooljaar, Priority.ALWAYS);
        HBox.setHgrow(periode, Priority.ALWAYS);
        HBox.setHgrow(doelgroep, Priority.ALWAYS);
        keuzes.getChildren().addAll(schooljaar, periode, doelgroep);

        VBox kaart = new VBox(12);
        kaart.setMaxWidth(1100);
        kaart.setPadding(new Insets(18));
        kaart.getStyleClass().add("content-card");

        HBox tabelHeader = new HBox(10);
        tabelHeader.setAlignment(Pos.CENTER_LEFT);
        Label tabelTitel = new Label("Aanbod in de geselecteerde periode");
        tabelTitel.getStyleClass().add("card-section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        wijzigenButton.getStyleClass().add("secondary-button");
        actiefWijzigenButton.getStyleClass().add("secondary-button");
        toevoegenButton.getStyleClass().add("primary-button");
        wijzigSelectieActies(null);
        tabelHeader.getChildren().addAll(
                tabelTitel, spacer, wijzigenButton, actiefWijzigenButton, toevoegenButton
        );

        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabel.setPlaceholder(new Label("Geen ingerichte talenten gevonden."));
        tabel.setMinHeight(310);
        tabel.setPrefHeight(310);
        tabel.setMaxHeight(310);

        aantalLabel.getStyleClass().add("content-subtitle");
        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-message");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        kaart.getChildren().addAll(tabelHeader, tabel, aantalLabel, statusLabel);
        inhoud.getChildren().addAll(intro, keuzes, kaart);
        return inhoud;
    }

    private <T> VBox maakKeuze(String tekst, ComboBox<T> comboBox, String prompt) {
        Label label = new Label(tekst);
        label.getStyleClass().add("field-label");
        comboBox.setEditable(false);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setPromptText(prompt);
        VBox box = new VBox(6, label, comboBox);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private void configureerTabel() {
        TableColumn<IngerichtTalent, String> naam = new TableColumn<>("Naam");
        naam.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNaam()));
        TableColumn<IngerichtTalent, String> basis = new TableColumn<>("Basistalent");
        basis.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTalent().getNaam()));
        TableColumn<IngerichtTalent, String> capaciteit = new TableColumn<>("Capaciteit");
        capaciteit.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                Integer.toString(data.getValue().getMaxCapaciteit())));
        TableColumn<IngerichtTalent, String> leerkrachten = new TableColumn<>("Leerkracht(en)");
        leerkrachten.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getLeerkrachten().isEmpty() ? "Geen"
                        : data.getValue().getLeerkrachten().stream()
                        .map(leerkracht -> leerkracht.getVoornaam() + " " + leerkracht.getAchternaam())
                        .collect(Collectors.joining(", "))));
        TableColumn<IngerichtTalent, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().isActief() ? "Actief" : "Niet actief"));
        status.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("status-active", "status-inactive");
                if (empty || item == null) { setText(null); return; }
                setText(item);
                getStyleClass().add(item.equals("Actief") ? "status-active" : "status-inactive");
            }
        });
        naam.setMinWidth(190);
        basis.setMinWidth(160);
        capaciteit.setMinWidth(100);
        leerkrachten.setMinWidth(270);
        status.setMinWidth(120);
        tabel.getColumns().addAll(naam, basis, capaciteit, leerkrachten, status);
    }

    public void wijzigSelectieActies(IngerichtTalent talent) {
        wijzigenButton.setDisable(talent == null);
        actiefWijzigenButton.setDisable(talent == null);
        actiefWijzigenButton.setText(talent != null && !talent.isActief()
                ? "Actief maken" : "Niet actief maken");
    }

    public void toonAantal(int aantal) {
        aantalLabel.setText(aantal == 1 ? "1 ingericht talent zichtbaar."
                : aantal + " ingerichte talenten zichtbaar.");
    }

    public Button getTerugButton() { return terugButton; }
    public Button getThemeButton() { return themeButton; }
    public ComboBox<Schooljaar> getSchooljaarComboBox() { return schooljaarComboBox; }
    public ComboBox<TalentenPeriode> getPeriodeComboBox() { return periodeComboBox; }
    public ComboBox<Doelgroep> getDoelgroepComboBox() { return doelgroepComboBox; }
    public TableView<IngerichtTalent> getTabel() { return tabel; }
    public Button getToevoegenButton() { return toevoegenButton; }
    public Button getWijzigenButton() { return wijzigenButton; }
    public Button getActiefWijzigenButton() { return actiefWijzigenButton; }
    public void toonStatus(String boodschap, boolean fout) {
        statusLabel.setText(boodschap);
        statusLabel.getStyleClass().removeAll("status-success", "status-error");
        statusLabel.getStyleClass().add(fout ? "status-error" : "status-success");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }
    public void verbergStatus() { statusLabel.setVisible(false); statusLabel.setManaged(false); }
    public void updateThemeIcon(boolean darkMode) { themeButton.setText(darkMode ? "☀" : "☾"); }
}
