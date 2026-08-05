package be.kdg.talenten.view.main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class MainView extends BorderPane {
    private Button verdelingBekijkenButton;
    private Button afsluitenButton;

    public MainView() {
        initialiseNodes();
        layoutNodes();
    }

    private void initialiseNodes() {
        verdelingBekijkenButton = new Button("Verdeling bekijken");
        verdelingBekijkenButton.getStyleClass().add("primary-button");
        verdelingBekijkenButton.setPrefWidth(260);

        afsluitenButton = new Button("Applicatie afsluiten");
        afsluitenButton.getStyleClass().add("secondary-button");
        afsluitenButton.setPrefWidth(260);
    }

    private void layoutNodes() {
        Label titel = new Label("KK Talenten");
        titel.getStyleClass().add("app-title");

        Label ondertitel = new Label("Beheer en bekijk de talentenverdeling van leerlingen.");
        ondertitel.getStyleClass().add("app-subtitle");

        VBox inhoud = new VBox(18, titel, ondertitel, verdelingBekijkenButton, afsluitenButton);
        inhoud.setAlignment(Pos.CENTER);
        inhoud.setPadding(new Insets(40));
        inhoud.getStyleClass().add("dashboard-card");
        inhoud.setMaxWidth(560);

        setCenter(inhoud);
        BorderPane.setAlignment(inhoud, Pos.CENTER);
        setPadding(new Insets(40));
        getStyleClass().add("app-background");
    }

    public Button getVerdelingBekijkenButton() {
        return verdelingBekijkenButton;
    }

    public Button getAfsluitenButton() {
        return afsluitenButton;
    }
}
