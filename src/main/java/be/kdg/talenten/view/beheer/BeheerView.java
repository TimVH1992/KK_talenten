package be.kdg.talenten.view.beheer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;

public class BeheerView extends BorderPane {
    private final Button terugButton = new Button("← Terug");
    private final Button themeButton = new Button("☾");
    private final Button schooljarenButton = maakBeheerKnop("Schooljaren", "Eenmalig het volgende schooljaar klaarzetten");
    private final Button periodesButton = maakBeheerKnop("Talentenperiodes", "Periodes binnen het schooljaar beheren");
    private final Button klassenButton = maakBeheerKnop("Klassen", "Klassen en doelgroepen beheren");
    private final Button leerlingenButton = maakBeheerKnop("Leerlingen", "Leerlingen en klaswissels beheren");
    private final Button leerkrachtenButton = maakBeheerKnop("Leerkrachten", "Leerkrachten activeren en beheren");
    private final Button ingerichteTalentenButton = maakBeheerKnop(
            "Ingerichte talenten",
            "Talentaanbod per periode, doelgroep en leerkracht beheren"
    );
    private final Button basisTalentenButton = maakBeheerKnop(
            "Basistalenten",
            "De stabiele talentencatalogus beheren"
    );

    public BeheerView() {
        getStyleClass().add("app-root");
        ingerichteTalentenButton.getStyleClass().add("manage-card-featured");
        setTop(maakHeader());
        setCenter(maakInhoud());
    }

    private Pane maakHeader() {
        BorderPane header = new BorderPane();
        header.getStyleClass().add("section-header");
        header.setPadding(new Insets(18, 28, 18, 28));

        terugButton.getStyleClass().add("secondary-button");
        header.setLeft(terugButton);

        Label titel = new Label("Beheer");
        titel.getStyleClass().add("section-title");
        header.setCenter(titel);

        themeButton.getStyleClass().add("theme-toggle");
        themeButton.setTooltip(new Tooltip("Wissel tussen lichte en donkere modus"));
        header.setRight(themeButton);
        return header;
    }

    private Pane maakInhoud() {
        VBox inhoud = new VBox(22);
        inhoud.setAlignment(Pos.TOP_CENTER);
        inhoud.setPadding(new Insets(26, 80, 30, 80));

        Label uitleg = new Label("Deze onderdelen worden vooral bij de start van een schooljaar gebruikt.");
        uitleg.getStyleClass().add("welcome-subtitle");

        VBox lijst = new VBox(11,
                schooljarenButton,
                periodesButton,
                klassenButton,
                leerlingenButton,
                leerkrachtenButton,
                ingerichteTalentenButton,
                basisTalentenButton
        );
        lijst.setMaxWidth(760);
        for (Button button : new Button[]{schooljarenButton, periodesButton, klassenButton,
                leerlingenButton, leerkrachtenButton, ingerichteTalentenButton, basisTalentenButton}) {
            button.setMaxWidth(Double.MAX_VALUE);
        }

        inhoud.getChildren().addAll(uitleg, lijst);
        return inhoud;
    }

    private Button maakBeheerKnop(String titel, String subtitel) {
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

        Button button = new Button();
        button.setGraphic(graphic);
        button.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        button.getStyleClass().add("manage-card");
        return button;
    }

    public Button getTerugButton() { return terugButton; }
    public Button getThemeButton() { return themeButton; }
    public Button getSchooljarenButton() { return schooljarenButton; }
    public Button getPeriodesButton() { return periodesButton; }
    public Button getKlassenButton() { return klassenButton; }
    public Button getLeerlingenButton() { return leerlingenButton; }
    public Button getLeerkrachtenButton() { return leerkrachtenButton; }
    public Button getIngerichteTalentenButton() { return ingerichteTalentenButton; }
    public Button getBasisTalentenButton() { return basisTalentenButton; }

    public void updateThemeIcon(boolean darkMode) {
        themeButton.setText(darkMode ? "☀" : "☾");
    }
}
