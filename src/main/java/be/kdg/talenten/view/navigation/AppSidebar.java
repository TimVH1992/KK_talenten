package be.kdg.talenten.view.navigation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AppSidebar extends VBox {

    public enum Sectie {
        DASHBOARD,
        LEERLINGEN,
        KLASSEN,
        LEERKRACHTEN,
        TALENTEN,
        TALENTENPERIODES,
        INGERICHTE_TALENTEN,
        VOORKEUREN,
        VERDELEN,
        // Behouden voor broncompatibiliteit met oudere views.
        AUTOMATISCHE_VERDELING,
        TOEWIJZINGEN_BEKIJKEN,
        MANUELE_TOEWIJZINGEN
    }

    private final Button dashboardButton;
    private final Button leerlingenButton;
    private final Button klassenButton;
    private final Button leerkrachtenButton;
    private final Button talentenButton;
    private final Button talentenperiodesButton;
    private final Button ingerichteTalentenButton;
    private final Button voorkeurenButton;
    private final Button verdeelButton;

    public AppSidebar(Sectie actieveSectie, String subtitel) {
        if (actieveSectie == null) {
            throw new IllegalArgumentException("Actieve sectie mag niet null zijn");
        }

        Sectie genormaliseerdeSectie = normaliseerVerdelingsSectie(actieveSectie);

        dashboardButton = maakNavigatieButton("⌂  Dashboard", Sectie.DASHBOARD, genormaliseerdeSectie);
        leerlingenButton = maakNavigatieButton("○  Leerlingen", Sectie.LEERLINGEN, genormaliseerdeSectie);
        klassenButton = maakNavigatieButton("▣  Klassen", Sectie.KLASSEN, genormaliseerdeSectie);
        leerkrachtenButton = maakNavigatieButton("○  Leerkrachten", Sectie.LEERKRACHTEN, genormaliseerdeSectie);
        talentenButton = maakNavigatieButton("✦  Talenten", Sectie.TALENTEN, genormaliseerdeSectie);
        talentenperiodesButton = maakNavigatieButton("◷  Talentenperiodes", Sectie.TALENTENPERIODES, genormaliseerdeSectie);
        ingerichteTalentenButton = maakNavigatieButton("▤  Ingerichte talenten", Sectie.INGERICHTE_TALENTEN, genormaliseerdeSectie);
        voorkeurenButton = maakNavigatieButton("⇩  Voorkeuren", Sectie.VOORKEUREN, genormaliseerdeSectie);
        verdeelButton = maakNavigatieButton("↔  Verdelen", Sectie.VERDELEN, genormaliseerdeSectie);

        Label logo = new Label("◉");
        logo.getStyleClass().add("brand-mark");

        Label brand = new Label("Talentontwikkeling");
        brand.getStyleClass().add("sidebar-brand");

        HBox brandBox = new HBox(10, logo, brand);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setPadding(new Insets(20, 18, 18, 18));

        Separator separator = new Separator();
        separator.getStyleClass().add("sidebar-separator");

        Region tussenruimte = new Region();
        tussenruimte.setPrefHeight(10);

        VBox menu = new VBox(
                4,
                dashboardButton,
                leerlingenButton,
                klassenButton,
                leerkrachtenButton,
                talentenButton,
                talentenperiodesButton,
                ingerichteTalentenButton,
                tussenruimte,
                voorkeurenButton,
                verdeelButton
        );
        menu.setPadding(new Insets(12));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label rol = new Label("Talentcoördinator");
        rol.getStyleClass().add("sidebar-user-name");

        Label sub = new Label(subtitel == null || subtitel.isBlank() ? "Lokale applicatie" : subtitel);
        sub.getStyleClass().add("sidebar-user-subtitle");

        VBox gebruiker = new VBox(3, rol, sub);
        gebruiker.setPadding(new Insets(16));
        gebruiker.getStyleClass().add("sidebar-user-box");

        getChildren().addAll(brandBox, separator, menu, spacer, gebruiker);
        setPrefWidth(245);
        setMinWidth(225);
        getStyleClass().add("sidebar");
    }

    private Sectie normaliseerVerdelingsSectie(Sectie sectie) {
        if (sectie == Sectie.AUTOMATISCHE_VERDELING
                || sectie == Sectie.TOEWIJZINGEN_BEKIJKEN
                || sectie == Sectie.MANUELE_TOEWIJZINGEN) {
            return Sectie.VERDELEN;
        }
        return sectie;
    }

    private Button maakNavigatieButton(String tekst, Sectie sectie, Sectie actieveSectie) {
        Button button = new Button(tekst);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.getStyleClass().add("sidebar-button");

        if (sectie == actieveSectie) {
            button.getStyleClass().add("sidebar-button-active");
            button.setDisable(true);
        }

        return button;
    }

    public Button getDashboardButton() { return dashboardButton; }
    public Button getLeerlingenButton() { return leerlingenButton; }
    public Button getKlassenButton() { return klassenButton; }
    public Button getLeerkrachtenButton() { return leerkrachtenButton; }
    public Button getTalentenButton() { return talentenButton; }
    public Button getTalentenperiodesButton() { return talentenperiodesButton; }
    public Button getIngerichteTalentenButton() { return ingerichteTalentenButton; }
    public Button getVoorkeurenButton() { return voorkeurenButton; }
    public Button getVerdeelButton() { return verdeelButton; }

    // Oudere views kunnen deze getters nog aanroepen; alle drie verwijzen nu naar dezelfde Verdeel-tab.
    public Button getAutomatischeVerdelingButton() { return verdeelButton; }
    public Button getVerdelingBekijkenButton() { return verdeelButton; }
    public Button getManueleToewijzingenButton() { return verdeelButton; }
}
