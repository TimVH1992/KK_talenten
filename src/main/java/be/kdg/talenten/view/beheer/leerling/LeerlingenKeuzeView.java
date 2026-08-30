package be.kdg.talenten.view.beheer.leerling;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LeerlingenKeuzeView extends BorderPane {
    private final Button terugButton = new Button("← Terug naar beheer");
    private final Button themeButton = new Button("☾");
    private final Button bulkButton = maakKnop("Leerlingen in bulk toevoegen",
            "Selecteer een klas en plak meerdere leerlingen rechtstreeks vanuit Excel");
    private final Button individueelButton = maakKnop("Individuele leerling wijzigen",
            "Zoek een leerling en wijzig naam, klas of actieve status");

    public LeerlingenKeuzeView() {
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
        Label titel = new Label("Leerlingen");
        titel.getStyleClass().add("section-title");
        header.setCenter(titel);
        themeButton.getStyleClass().add("theme-toggle");
        themeButton.setTooltip(new Tooltip("Wissel tussen lichte en donkere modus"));
        header.setRight(themeButton);
        return header;
    }

    private Pane maakInhoud() {
        VBox inhoud = new VBox(22);
        inhoud.setPadding(new Insets(36, 80, 40, 80));
        inhoud.setAlignment(Pos.TOP_CENTER);
        Label uitleg = new Label("Kies hoe u de leerlinggegevens wilt beheren.");
        uitleg.getStyleClass().add("welcome-subtitle");
        VBox keuzes = new VBox(14, bulkButton, individueelButton);
        keuzes.setMaxWidth(760);
        bulkButton.setMaxWidth(Double.MAX_VALUE);
        individueelButton.setMaxWidth(Double.MAX_VALUE);
        inhoud.getChildren().addAll(uitleg, keuzes);
        return inhoud;
    }

    private Button maakKnop(String titel, String subtitel) {
        Label titelLabel = new Label(titel);
        titelLabel.getStyleClass().add("manage-title");
        Label subtitelLabel = new Label(subtitel);
        subtitelLabel.getStyleClass().add("manage-subtitle");
        VBox teksten = new VBox(3, titelLabel, subtitelLabel);
        teksten.setAlignment(Pos.CENTER_LEFT);
        BorderPane graphic = new BorderPane();
        graphic.setCenter(teksten);
        Label pijl = new Label("›");
        pijl.getStyleClass().add("manage-arrow");
        graphic.setRight(pijl);
        Button knop = new Button();
        knop.setGraphic(graphic);
        knop.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        knop.getStyleClass().add("manage-card");
        return knop;
    }

    public Button getTerugButton() { return terugButton; }
    public Button getThemeButton() { return themeButton; }
    public Button getBulkButton() { return bulkButton; }
    public Button getIndividueelButton() { return individueelButton; }
    public void updateThemeIcon(boolean darkMode) { themeButton.setText(darkMode ? "☀" : "☾"); }
}
