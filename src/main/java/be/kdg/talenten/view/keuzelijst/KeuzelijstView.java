package be.kdg.talenten.view.keuzelijst;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.TalentenPeriode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class KeuzelijstView extends BorderPane {
    private final Button terugButton = new Button("← Terug naar hoofdmenu");
    private final Button themeButton = new Button("☾");
    private final Button legeKeuzelijstButton = new Button("Lege keuzelijst aanmaken");
    private final Button uploadKeuzelijstButton = new Button("Ingevulde keuzelijst uploaden");
    private final ComboBox<TalentenPeriode> periodeComboBox = new ComboBox<>();
    private final ComboBox<Doelgroep> doelgroepComboBox = new ComboBox<>();
    private final Button aanmakenButton = new Button("Keuzelijst als Excelbestand opslaan");
    private final Button uploadenButton = new Button("Excelbestand kiezen en uploaden");
    private final Label formulierTitel = new Label();
    private final Label formulierUitleg = new Label();
    private final Label statusLabel = new Label();
    private final VBox exportActie = new VBox();
    private final VBox importActie = new VBox();

    public KeuzelijstView() {
        getStyleClass().add("app-root");
        setTop(maakHeader());
        setCenter(maakInhoud());
        toonExportFormulier();
    }

    private Pane maakHeader() {
        BorderPane header = new BorderPane();
        header.getStyleClass().add("section-header");
        header.setPadding(new Insets(18, 28, 18, 28));
        terugButton.getStyleClass().add("secondary-button");
        header.setLeft(terugButton);
        Label titel = new Label("Keuzelijsten");
        titel.getStyleClass().add("section-title");
        header.setCenter(titel);
        themeButton.getStyleClass().add("theme-toggle");
        themeButton.setTooltip(new Tooltip("Wissel tussen lichte en donkere modus"));
        header.setRight(themeButton);
        return header;
    }

    private Node maakInhoud() {
        VBox inhoud = new VBox(18);
        inhoud.setPadding(new Insets(30, 60, 36, 60));
        inhoud.setAlignment(Pos.TOP_CENTER);
        VBox intro = new VBox(5);
        intro.setMaxWidth(980);
        Label titel = new Label("Keuzelijsten");
        titel.getStyleClass().add("content-title");
        Label uitleg = new Label("Maak een lege keuzelijst of verwerk een ingevulde keuzelijst.");
        uitleg.setWrapText(true);
        uitleg.getStyleClass().add("content-subtitle");
        intro.getChildren().addAll(titel, uitleg);

        HBox acties = new HBox(14, legeKeuzelijstButton, uploadKeuzelijstButton);
        acties.setMaxWidth(980);
        legeKeuzelijstButton.getStyleClass().add("primary-button");
        uploadKeuzelijstButton.getStyleClass().add("secondary-button");
        for (Button button : new Button[]{legeKeuzelijstButton, uploadKeuzelijstButton}) {
            button.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(button, Priority.ALWAYS);
        }

        VBox kaart = new VBox(16);
        kaart.setPadding(new Insets(22));
        kaart.setMaxWidth(980);
        kaart.getStyleClass().add("content-card");
        formulierTitel.getStyleClass().add("card-section-title");
        formulierUitleg.getStyleClass().add("content-subtitle");
        formulierUitleg.setWrapText(true);
        aanmakenButton.getStyleClass().add("primary-button");
        aanmakenButton.setMaxWidth(Double.MAX_VALUE);
        exportActie.getChildren().add(aanmakenButton);
        uploadenButton.getStyleClass().add("primary-button");
        uploadenButton.setMaxWidth(Double.MAX_VALUE);
        importActie.getChildren().add(uploadenButton);
        kaart.getChildren().addAll(formulierTitel, formulierUitleg,
                maakKeuze("Talentenperiode", periodeComboBox, "Selecteer een talentenperiode"),
                maakKeuze("Doelgroep", doelgroepComboBox, "Selecteer een doelgroep"),
                exportActie, importActie);

        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(980);
        statusLabel.getStyleClass().add("status-message");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        inhoud.getChildren().addAll(intro, acties, kaart, statusLabel);
        StackPane centreer = new StackPane(inhoud);
        centreer.setAlignment(Pos.TOP_CENTER);
        ScrollPane scroll = new ScrollPane(centreer);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("content-scroll");
        return scroll;
    }

    private <T> VBox maakKeuze(String tekst, ComboBox<T> combo, String prompt) {
        Label label = new Label(tekst);
        label.getStyleClass().add("field-label");
        combo.setEditable(false);
        combo.setPromptText(prompt);
        combo.setMaxWidth(Double.MAX_VALUE);
        return new VBox(6, label, combo);
    }

    public void toonExportFormulier() {
        formulierTitel.setText("Lege keuzelijst aanmaken");
        formulierUitleg.setText("Maak een beveiligde Excel-keuzelijst voor één talentenperiode en doelgroep.");
        wijzigActie(exportActie, importActie, legeKeuzelijstButton, uploadKeuzelijstButton);
    }

    public void toonImportFormulier() {
        formulierTitel.setText("Ingevulde keuzelijst uploaden");
        formulierUitleg.setText("Selecteer de bijbehorende talentenperiode en doelgroep en kies daarna het ingevulde Excelbestand.");
        wijzigActie(importActie, exportActie, uploadKeuzelijstButton, legeKeuzelijstButton);
    }

    private void wijzigActie(VBox zichtbaar, VBox verborgen, Button actief, Button inactief) {
        verbergStatus();
        zichtbaar.setVisible(true);
        zichtbaar.setManaged(true);
        verborgen.setVisible(false);
        verborgen.setManaged(false);
        actief.getStyleClass().remove("secondary-button");
        if (!actief.getStyleClass().contains("primary-button")) actief.getStyleClass().add("primary-button");
        inactief.getStyleClass().remove("primary-button");
        if (!inactief.getStyleClass().contains("secondary-button")) inactief.getStyleClass().add("secondary-button");
    }

    public Button getTerugButton() { return terugButton; }
    public Button getThemeButton() { return themeButton; }
    public Button getLegeKeuzelijstButton() { return legeKeuzelijstButton; }
    public Button getUploadKeuzelijstButton() { return uploadKeuzelijstButton; }
    public ComboBox<TalentenPeriode> getPeriodeComboBox() { return periodeComboBox; }
    public ComboBox<Doelgroep> getDoelgroepComboBox() { return doelgroepComboBox; }
    public Button getAanmakenButton() { return aanmakenButton; }
    public Button getUploadenButton() { return uploadenButton; }

    public void toonStatus(String tekst, boolean fout) {
        statusLabel.setText(tekst);
        statusLabel.getStyleClass().removeAll("status-success", "status-error");
        statusLabel.getStyleClass().add(fout ? "status-error" : "status-success");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    public void verbergStatus() {
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }

    public void updateThemeIcon(boolean dark) { themeButton.setText(dark ? "☀" : "☾"); }
}
