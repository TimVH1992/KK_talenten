package be.kdg.talenten.view.keuzelijst;

import be.kdg.talenten.domain.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class KeuzelijstView extends BorderPane {
    private final Button terugButton = new Button("← Terug naar hoofdmenu");
    private final Button themeButton = new Button("☾");
    private final ComboBox<TalentenPeriode> periodeComboBox = new ComboBox<>();
    private final ComboBox<Doelgroep> doelgroepComboBox = new ComboBox<>();
    private final Button aanmakenButton = new Button("Keuzelijst als Excelbestand opslaan");
    private final Label statusLabel = new Label();

    public KeuzelijstView() {
        getStyleClass().add("app-root"); setTop(maakHeader()); setCenter(maakInhoud());
    }
    private Pane maakHeader() {
        BorderPane header = new BorderPane(); header.getStyleClass().add("section-header");
        header.setPadding(new Insets(18, 28, 18, 28)); terugButton.getStyleClass().add("secondary-button");
        header.setLeft(terugButton); Label titel = new Label("Keuzelijst aanmaken");
        titel.getStyleClass().add("section-title"); header.setCenter(titel);
        themeButton.getStyleClass().add("theme-toggle"); header.setRight(themeButton); return header;
    }
    private Node maakInhoud() {
        VBox inhoud = new VBox(18); inhoud.setPadding(new Insets(30, 60, 36, 60)); inhoud.setAlignment(Pos.TOP_CENTER);
        VBox intro = new VBox(5); Label titel = new Label("Keuzelijst aanmaken"); titel.getStyleClass().add("content-title");
        Label uitleg = new Label("Maak een beveiligde Excel-keuzelijst voor één talentenperiode en doelgroep.");
        uitleg.setWrapText(true); uitleg.getStyleClass().add("content-subtitle"); intro.getChildren().addAll(titel, uitleg);
        VBox kaart = new VBox(16); kaart.setPadding(new Insets(22)); kaart.setMaxWidth(980); kaart.getStyleClass().add("content-card");
        kaart.getChildren().addAll(maakKeuze("Talentenperiode", periodeComboBox, "Selecteer een talentenperiode"),
                maakKeuze("Doelgroep", doelgroepComboBox, "Selecteer een doelgroep"));
        aanmakenButton.getStyleClass().add("primary-button"); aanmakenButton.setMaxWidth(Double.MAX_VALUE); kaart.getChildren().add(aanmakenButton);
        statusLabel.setWrapText(true); statusLabel.getStyleClass().add("status-message"); statusLabel.setVisible(false); statusLabel.setManaged(false);
        inhoud.getChildren().addAll(intro, kaart, statusLabel); StackPane centreer = new StackPane(inhoud); centreer.setAlignment(Pos.TOP_CENTER);
        ScrollPane scroll = new ScrollPane(centreer); scroll.setFitToWidth(true); scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); scroll.getStyleClass().add("content-scroll"); return scroll;
    }
    private <T> VBox maakKeuze(String tekst, ComboBox<T> combo, String prompt) {
        Label label = new Label(tekst); label.getStyleClass().add("field-label"); combo.setEditable(false); combo.setPromptText(prompt); combo.setMaxWidth(Double.MAX_VALUE); return new VBox(6, label, combo);
    }
    public Button getTerugButton() { return terugButton; } public Button getThemeButton() { return themeButton; }
    public ComboBox<TalentenPeriode> getPeriodeComboBox() { return periodeComboBox; }
    public ComboBox<Doelgroep> getDoelgroepComboBox() { return doelgroepComboBox; } public Button getAanmakenButton() { return aanmakenButton; }
    public void toonStatus(String tekst, boolean fout) { statusLabel.setText(tekst); statusLabel.getStyleClass().removeAll("status-success", "status-error"); statusLabel.getStyleClass().add(fout ? "status-error" : "status-success"); statusLabel.setVisible(true); statusLabel.setManaged(true); }
    public void updateThemeIcon(boolean dark) { themeButton.setText(dark ? "☀" : "☾"); }
}
