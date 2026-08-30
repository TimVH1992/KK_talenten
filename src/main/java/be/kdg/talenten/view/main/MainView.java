package be.kdg.talenten.view.main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;

public class MainView extends BorderPane {
    private final Button verdelingButton = maakKaart("☑", "Verdeling opstellen", "Verdelen, bekijken en manueel aanpassen");
    private final Button beheerButton = maakKaart("⚙", "Beheer", "Schooljaren, periodes, klassen, leerlingen en leerkrachten");
    private final Button keuzelijstButton = maakKaart("☷", "Keuzelijst aanmaken", "Excelbestand maken voor de leerlingkeuzes");
    private final Button exportButton = maakKaart("⇩", "Lijsten exporteren", "Per klas / per ingericht talent");
    private final Button themeButton = new Button("☾");

    public MainView() {
        getStyleClass().add("app-root");
        setTop(maakHeader());
        setCenter(maakInhoud());
    }

    private Pane maakHeader() {
        BorderPane header = new BorderPane();
        header.getStyleClass().add("top-banner");
        header.setPadding(new Insets(22, 28, 22, 28));

        Label titel = new Label("Talenten KK");
        titel.getStyleClass().add("app-title");
        BorderPane.setAlignment(titel, Pos.CENTER);
        header.setCenter(titel);

        themeButton.getStyleClass().add("theme-toggle");
        themeButton.setTooltip(new Tooltip("Wissel tussen lichte en donkere modus"));
        header.setRight(themeButton);
        BorderPane.setAlignment(themeButton, Pos.CENTER_RIGHT);

        Region links = new Region();
        links.setMinWidth(44);
        header.setLeft(links);
        return header;
    }

    private Pane maakInhoud() {
        VBox inhoud = new VBox(26);
        inhoud.setAlignment(Pos.TOP_CENTER);
        inhoud.setPadding(new Insets(28, 70, 30, 70));

        VBox intro = new VBox(5);
        intro.setAlignment(Pos.CENTER);
        Label welkom = new Label("Welkom! Kies wat u wilt doen.");
        welkom.getStyleClass().add("welcome-title");
        Label uitleg = new Label("De dagelijkse werking staat vooraan; jaarlijkse instellingen zitten onder Beheer.");
        uitleg.getStyleClass().add("welcome-subtitle");
        intro.getChildren().addAll(welkom, uitleg);

        GridPane grid = new GridPane();
        grid.setHgap(28);
        grid.setVgap(28);
        grid.setAlignment(Pos.CENTER);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        RowConstraints r1 = new RowConstraints();
        r1.setVgrow(Priority.ALWAYS);
        RowConstraints r2 = new RowConstraints();
        r2.setVgrow(Priority.ALWAYS);
        grid.getRowConstraints().addAll(r1, r2);

        grid.add(verdelingButton, 0, 0);
        grid.add(beheerButton, 1, 0);
        grid.add(keuzelijstButton, 0, 1);
        grid.add(exportButton, 1, 1);

        for (Button button : new Button[]{verdelingButton, beheerButton, keuzelijstButton, exportButton}) {
            button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            GridPane.setHgrow(button, Priority.ALWAYS);
            GridPane.setVgrow(button, Priority.ALWAYS);
        }

        HBox info = new HBox(12);
        info.getStyleClass().add("info-bar");
        info.setAlignment(Pos.CENTER_LEFT);
        info.setPadding(new Insets(14, 18, 14, 18));
        Label icoon = new Label("ⓘ");
        icoon.getStyleClass().add("info-icon");
        Label tekst = new Label("Schooljaar, periodes en klassen worden doorgaans maar één keer per schooljaar ingericht.");
        tekst.getStyleClass().add("info-text");
        info.getChildren().addAll(icoon, tekst);

        VBox.setVgrow(grid, Priority.ALWAYS);
        inhoud.getChildren().addAll(intro, grid, info);
        return inhoud;
    }

    private Button maakKaart(String icoon, String titel, String subtitel) {
        Label iconLabel = new Label(icoon);
        iconLabel.getStyleClass().add("card-icon");

        Label titleLabel = new Label(titel);
        titleLabel.getStyleClass().add("card-title");

        Label subtitleLabel = new Label(subtitel);
        subtitleLabel.getStyleClass().add("card-subtitle");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(390);
        subtitleLabel.setAlignment(Pos.CENTER);

        VBox graphic = new VBox(10, iconLabel, titleLabel, subtitleLabel);
        graphic.setAlignment(Pos.CENTER);

        Button button = new Button();
        button.setGraphic(graphic);
        button.getStyleClass().add("dashboard-card");
        button.setMinHeight(210);
        return button;
    }

    public Button getVerdelingButton() { return verdelingButton; }
    public Button getBeheerButton() { return beheerButton; }
    public Button getKeuzelijstButton() { return keuzelijstButton; }
    public Button getExportButton() { return exportButton; }
    public Button getThemeButton() { return themeButton; }

    public void updateThemeIcon(boolean darkMode) {
        themeButton.setText(darkMode ? "☀" : "☾");
    }
}
