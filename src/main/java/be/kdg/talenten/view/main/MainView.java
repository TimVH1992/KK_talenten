package be.kdg.talenten.view.main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class MainView extends BorderPane {
    private Button dashboardButton;
    private Button leerlingenButton;
    private Button klassenButton;
    private Button leerkrachtenButton;
    private Button talentenButton;
    private Button talentenperiodesButton;
    private Button ingerichteTalentenButton;
    private Button voorkeurenButton;
    private Button automatischeVerdelingButton;
    private Button verdelingBekijkenButton;
    private Button manueleToewijzingenButton;
    private Button afsluitenButton;

    private Button snelVoorkeurenButton;
    private Button snelVerdelingButton;
    private Button snelOverzichtButton;
    private Button leerlingenBeheerButton;
    private Button talentenBeheerButton;
    private Button leerkrachtenBeheerButton;
    private Button ingerichteTalentenBeheerButton;
    private Button klassenBeheerButton;

    private Label actiefSchooljaarLabel;

    public MainView() {
        initialiseNodes();
        layoutNodes();
    }

    private void initialiseNodes() {
        dashboardButton = maakNavigatieButton("⌂  Dashboard", true);
        leerlingenButton = maakNavigatieButton("○  Leerlingen", false);
        klassenButton = maakNavigatieButton("▣  Klassen", false);
        leerkrachtenButton = maakNavigatieButton("○  Leerkrachten", false);
        talentenButton = maakNavigatieButton("✦  Talenten", false);
        talentenperiodesButton = maakNavigatieButton("◷  Talentenperiodes", false);
        ingerichteTalentenButton = maakNavigatieButton("▤  Ingerichte talenten", false);
        voorkeurenButton = maakNavigatieButton("⇩  Voorkeuren importeren", false);
        automatischeVerdelingButton = maakNavigatieButton("↔  Verdelen", false);
        // Behouden voor broncompatibiliteit; deze twee knoppen worden niet meer afzonderlijk in de sidebar getoond.
        verdelingBekijkenButton = automatischeVerdelingButton;
        manueleToewijzingenButton = automatischeVerdelingButton;

        afsluitenButton = new Button("Applicatie afsluiten");
        afsluitenButton.getStyleClass().add("sidebar-exit-button");
        afsluitenButton.setMaxWidth(Double.MAX_VALUE);

        snelVoorkeurenButton = maakActieButton("Voorkeuren beheren", "Excel genereren en ingevulde bestanden importeren.", "Open voorkeuren");
        snelVerdelingButton = maakActieButton("Automatisch verdelen", "Verdeel leerlingen op basis van voorkeuren, capaciteit en historiek.", "Open verdeling");
        snelOverzichtButton = maakActieButton("Toewijzingen bekijken", "Bekijk per talent, per klas en welke leerlingen nog niet toegewezen zijn.", "Open overzicht");

        actiefSchooljaarLabel = new Label("Actief schooljaar wordt geladen...");
        actiefSchooljaarLabel.getStyleClass().add("context-pill");
    }

    private Button maakNavigatieButton(String tekst, boolean actief) {
        Button button = new Button(tekst);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.getStyleClass().add("sidebar-button");
        if (actief) {
            button.getStyleClass().add("sidebar-button-active");
        }
        return button;
    }

    private Button maakActieButton(String titel, String omschrijving, String actieTekst) {
        Button button = new Button(titel + "\n" + omschrijving + "\n\n" + actieTekst + "  →");
        button.setWrapText(true);
        button.setAlignment(Pos.TOP_LEFT);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(175);
        button.getStyleClass().add("action-card-button");
        return button;
    }

    private void layoutNodes() {
        setLeft(maakSidebar());
        setCenter(maakDashboard());
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

        VBox menu = new VBox(4,
                dashboardButton,
                leerlingenButton,
                klassenButton,
                leerkrachtenButton,
                talentenButton,
                talentenperiodesButton,
                ingerichteTalentenButton,
                maakMenuTussenruimte(),
                voorkeurenButton,
                automatischeVerdelingButton
        );
        menu.setPadding(new Insets(12));

        Label rolLabel = new Label("Talentcoördinator");
        rolLabel.getStyleClass().add("sidebar-user-name");
        Label rolSubLabel = new Label("Lokale applicatie");
        rolSubLabel.getStyleClass().add("sidebar-user-subtitle");
        VBox gebruiker = new VBox(2, rolLabel, rolSubLabel, afsluitenButton);
        gebruiker.setPadding(new Insets(14, 16, 18, 16));
        gebruiker.getStyleClass().add("sidebar-user-box");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox sidebar = new VBox(brandBox, separator, menu, spacer, gebruiker);
        sidebar.setPrefWidth(245);
        sidebar.setMinWidth(225);
        sidebar.getStyleClass().add("sidebar");
        return sidebar;
    }

    private Region maakMenuTussenruimte() {
        Region region = new Region();
        region.setPrefHeight(10);
        return region;
    }

    private VBox maakDashboard() {
        Label titel = new Label("Dashboard");
        titel.getStyleClass().add("page-title");

        Label ondertitel = new Label("Beheer de voorkeuren en talentenverdeling vanuit één overzicht.");
        ondertitel.getStyleClass().add("page-subtitle");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox header = new HBox(14, new VBox(4, titel, ondertitel), headerSpacer, actiefSchooljaarLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        Label actiesTitel = new Label("Snelle acties");
        actiesTitel.getStyleClass().add("section-heading");

        GridPane acties = new GridPane();
        acties.setHgap(16);
        acties.setVgap(16);
        acties.add(snelVoorkeurenButton, 0, 0);
        acties.add(snelVerdelingButton, 1, 0);
        acties.add(snelOverzichtButton, 2, 0);
        for (int i = 0; i < 3; i++) {
            javafx.scene.layout.ColumnConstraints kolom = new javafx.scene.layout.ColumnConstraints();
            kolom.setPercentWidth(33.333);
            kolom.setHgrow(Priority.ALWAYS);
            acties.getColumnConstraints().add(kolom);
        }

        Label beheerTitel = new Label("Beheerfuncties");
        beheerTitel.getStyleClass().add("section-heading");

        Label beheerUitleg = new Label("De kernflow voor voorkeuren en verdeling werkt. Leerlingen, leerkrachten, talenten en ingerichte talenten kunnen rechtstreeks beheerd worden.");
        beheerUitleg.setWrapText(true);
        beheerUitleg.getStyleClass().add("muted-label");

        GridPane beheerGrid = new GridPane();
        beheerGrid.setHgap(12);
        beheerGrid.setVgap(12);
        List<Button> beheerButtons = List.of(leerlingenButton, klassenButton, leerkrachtenButton, talentenButton, talentenperiodesButton, ingerichteTalentenButton);
        String[] titels = {"Leerlingen", "Klassen", "Leerkrachten", "Talenten", "Talentenperiodes", "Ingerichte talenten"};
        for (int i = 0; i < beheerButtons.size(); i++) {
            String onderdeel = titels[i];
            String status = onderdeel.equals("Leerlingen") || onderdeel.equals("Klassen") || onderdeel.equals("Talenten") || onderdeel.equals("Leerkrachten") || onderdeel.equals("Ingerichte talenten") ? "Beheren" : "Nog niet volledig geïmplementeerd";
            Button tegel = new Button(onderdeel + "\n" + status);
            tegel.setWrapText(true);
            tegel.setMaxWidth(Double.MAX_VALUE);
            tegel.setPrefHeight(86);
            tegel.getStyleClass().add("placeholder-card-button");

            if (onderdeel.equals("Leerlingen")) {
                leerlingenBeheerButton = tegel;
            } else if (onderdeel.equals("Klassen")) {
                klassenBeheerButton = tegel;
            } else if (onderdeel.equals("Talenten")) {
                talentenBeheerButton = tegel;
            } else if (onderdeel.equals("Leerkrachten")) {
                leerkrachtenBeheerButton = tegel;
            } else if (onderdeel.equals("Ingerichte talenten")) {
                ingerichteTalentenBeheerButton = tegel;
            } else {
                tegel.setOnAction(event -> toonNietBeschikbaar(onderdeel));
            }

            beheerGrid.add(tegel, i % 3, i / 3);
        }
        for (int i = 0; i < 3; i++) {
            javafx.scene.layout.ColumnConstraints kolom = new javafx.scene.layout.ColumnConstraints();
            kolom.setPercentWidth(33.333);
            kolom.setHgrow(Priority.ALWAYS);
            beheerGrid.getColumnConstraints().add(kolom);
        }

        VBox beheerCard = new VBox(12, beheerTitel, beheerUitleg, beheerGrid);
        beheerCard.getStyleClass().add("content-card");
        beheerCard.setPadding(new Insets(22));

        Label statusTitel = new Label("Projectstatus");
        statusTitel.getStyleClass().add("section-heading");
        Label statusTekst = new Label("✓ Exceltemplates genereren   ✓ Voorkeuren importeren   ✓ Verdelen en toewijzingen   ✓ Leerlingen beheren   ✓ Klassen beheren   ✓ Talenten beheren   ✓ Leerkrachten beheren   ✓ Ingerichte talenten beheren   ✓ PostgreSQL persistentie");
        statusTekst.setWrapText(true);
        statusTekst.getStyleClass().add("status-success");
        VBox statusCard = new VBox(10, statusTitel, statusTekst);
        statusCard.getStyleClass().add("content-card");
        statusCard.setPadding(new Insets(20));

        VBox inhoud = new VBox(24, header, actiesTitel, acties, beheerCard, statusCard);
        inhoud.setPadding(new Insets(34));
        VBox.setVgrow(beheerCard, Priority.ALWAYS);
        return inhoud;
    }

    public void toonNietBeschikbaar(String onderdeel) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Nog niet geïmplementeerd");
        alert.setHeaderText(onderdeel);
        alert.setContentText("Dit beheerscherm is nog niet uitgewerkt. De backend- en verdelingsfunctionaliteit kan ondertussen wel gebruikt worden.");
        alert.showAndWait();
    }

    public void setActiefSchooljaar(String tekst) {
        actiefSchooljaarLabel.setText(tekst == null || tekst.isBlank() ? "Geen actief schooljaar" : tekst);
    }

    public Button getDashboardButton() {
        return dashboardButton;
    }

    public Button getLeerlingenButton() {
        return leerlingenButton;
    }

    public Button getKlassenButton() {
        return klassenButton;
    }

    public Button getLeerkrachtenButton() {
        return leerkrachtenButton;
    }

    public Button getTalentenButton() {
        return talentenButton;
    }

    public Button getTalentenperiodesButton() {
        return talentenperiodesButton;
    }

    public Button getIngerichteTalentenButton() {
        return ingerichteTalentenButton;
    }

    public Button getVoorkeurenButton() {
        return voorkeurenButton;
    }

    public Button getAutomatischeVerdelingButton() {
        return automatischeVerdelingButton;
    }

    public Button getVerdelingBekijkenButton() {
        return verdelingBekijkenButton;
    }

    public Button getManueleToewijzingenButton() {
        return manueleToewijzingenButton;
    }

    public Button getAfsluitenButton() {
        return afsluitenButton;
    }

    public Button getSnelVoorkeurenButton() {
        return snelVoorkeurenButton;
    }

    public Button getSnelVerdelingButton() {
        return snelVerdelingButton;
    }

    public Button getSnelOverzichtButton() {
        return snelOverzichtButton;
    }

    public Button getTalentenBeheerButton() {
        return talentenBeheerButton;
    }

    public Button getLeerkrachtenBeheerButton() {
        return leerkrachtenBeheerButton;
    }

    public Button getLeerlingenBeheerButton() {
        return leerlingenBeheerButton;
    }

    public Button getIngerichteTalentenBeheerButton() {
        return ingerichteTalentenBeheerButton;
    }

    public Button getKlassenBeheerButton() {
        return klassenBeheerButton;
    }
}
