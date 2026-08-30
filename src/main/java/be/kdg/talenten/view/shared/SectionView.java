package be.kdg.talenten.view.shared;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class SectionView extends BorderPane {
    private final Button terugButton = new Button("← Terug");
    private final Button themeButton = new Button("☾");

    public SectionView(String titel, String uitleg) {
        getStyleClass().add("app-root");
        setTop(maakHeader(titel));
        setCenter(maakInhoud(titel, uitleg));
    }

    private Pane maakHeader(String titel) {
        BorderPane header = new BorderPane();
        header.getStyleClass().add("section-header");
        header.setPadding(new Insets(18, 28, 18, 28));

        terugButton.getStyleClass().add("secondary-button");
        header.setLeft(terugButton);

        Label titelLabel = new Label(titel);
        titelLabel.getStyleClass().add("section-title");
        header.setCenter(titelLabel);

        themeButton.getStyleClass().add("theme-toggle");
        themeButton.setTooltip(new Tooltip("Wissel tussen lichte en donkere modus"));
        header.setRight(themeButton);
        return header;
    }

    private Pane maakInhoud(String titel, String uitleg) {
        VBox inhoud = new VBox(16);
        inhoud.setAlignment(Pos.CENTER);
        inhoud.setPadding(new Insets(60));

        Label icoon = new Label("◌");
        icoon.getStyleClass().add("placeholder-icon");
        Label titelLabel = new Label(titel);
        titelLabel.getStyleClass().add("placeholder-title");
        Label uitlegLabel = new Label(uitleg);
        uitlegLabel.getStyleClass().add("placeholder-text");
        uitlegLabel.setWrapText(true);
        uitlegLabel.setMaxWidth(650);
        uitlegLabel.setAlignment(Pos.CENTER);
        Label status = new Label("Deze pagina is klaar als navigatiepunt en kan nu aan de bestaande services gekoppeld worden.");
        status.getStyleClass().add("placeholder-status");
        status.setWrapText(true);
        status.setMaxWidth(650);
        status.setAlignment(Pos.CENTER);

        inhoud.getChildren().addAll(icoon, titelLabel, uitlegLabel, status);
        return inhoud;
    }

    public Button getTerugButton() { return terugButton; }
    public Button getThemeButton() { return themeButton; }

    public void updateThemeIcon(boolean darkMode) {
        themeButton.setText(darkMode ? "☀" : "☾");
    }
}
