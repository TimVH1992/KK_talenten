package be.kdg.talenten.view.verdeling;

import be.kdg.talenten.view.navigation.AppSidebar;

import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.domain.Voorkeur;
import be.kdg.talenten.overzicht.IngerichtTalentOverzicht;
import be.kdg.talenten.overzicht.KlasOverzicht;
import be.kdg.talenten.overzicht.LeerlingDetailsOverzicht;
import be.kdg.talenten.overzicht.LeerlingToewijzingOverzicht;
import be.kdg.talenten.overzicht.NietToegewezenLeerlingOverzicht;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class VerdelingView extends BorderPane {
    private final AppSidebar sidebar;
    private final DateTimeFormatter datumFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Button terugButton;
    private ComboBox<Schooljaar> schooljaarComboBox;
    private ComboBox<TalentenPeriode> periodeComboBox;
    private Button overzichtLadenButton;
    private Button automatischeVerdelingButton;

    private TableView<IngerichtTalentOverzicht> talentenTable;
    private TableView<Toewijzing> leerlingenTable;
    private Label leerlingenTitelLabel;

    private ComboBox<Klas> klasComboBox;
    private TableView<LeerlingToewijzingOverzicht> klasLeerlingenTable;

    private TableView<NietToegewezenLeerlingOverzicht> nietToegewezenLeerlingenTable;
    private Tab nietToegewezenTab;

    private Label geselecteerdeLeerlingLabel;
    private Label voorkeurenTitelLabel;
    private TableView<Voorkeur> voorkeurenTable;
    private TableView<Toewijzing> historiekTable;

    private ComboBox<IngerichtTalent> doelTalentComboBox;
    private Button verplaatsLeerlingButton;
    private Label statusLabel;

    public VerdelingView() {
        sidebar = new AppSidebar(AppSidebar.Sectie.VERDELEN, "Verdelen en toewijzingen");
        initialiseNodes();
        layoutNodes();
        setWijzigingenToegestaan(false);
    }

    private void initialiseNodes() {
        terugButton = new Button("⌂  Dashboard");
        terugButton.getStyleClass().add("sidebar-button");
        terugButton.setAlignment(Pos.CENTER_LEFT);
        terugButton.setMaxWidth(Double.MAX_VALUE);

        schooljaarComboBox = new ComboBox<>();
        schooljaarComboBox.setPromptText("Selecteer een schooljaar");
        schooljaarComboBox.setPrefWidth(170);
        schooljaarComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Schooljaar schooljaar) {
                return schooljaar == null ? "" : schooljaar.getNaam();
            }

            @Override
            public Schooljaar fromString(String string) {
                return null;
            }
        });

        periodeComboBox = new ComboBox<>();
        periodeComboBox.setPromptText("Selecteer een talentenperiode");
        periodeComboBox.setPrefWidth(330);
        periodeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TalentenPeriode periode) {
                if (periode == null) return "";

                return periode.getNaam()
                        + " — "
                        + datumFormatter.format(periode.getStartDatum())
                        + " t.e.m. "
                        + datumFormatter.format(periode.getEindDatum());
            }

            @Override
            public TalentenPeriode fromString(String string) {
                return null;
            }
        });

        overzichtLadenButton = new Button("Overzicht laden");
        overzichtLadenButton.getStyleClass().add("primary-button");

        automatischeVerdelingButton = new Button("Automatische verdeling uitvoeren");
        automatischeVerdelingButton.getStyleClass().add("warning-button");

        talentenTable = maakTalentenTable();

        leerlingenTable = maakLeerlingenTable();
        leerlingenTitelLabel = new Label("Toegewezen leerlingen");
        leerlingenTitelLabel.getStyleClass().add("section-title");

        klasComboBox = new ComboBox<>();
        klasComboBox.setPromptText("Selecteer een klas");
        klasComboBox.setPrefWidth(260);
        klasComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Klas klas) {
                if (klas == null) return "";
                return klas.getNaam() + " — " + klas.getSchooljaar();
            }

            @Override
            public Klas fromString(String string) {
                return null;
            }
        });

        klasLeerlingenTable = maakKlasLeerlingenTable();
        nietToegewezenLeerlingenTable = maakNietToegewezenLeerlingenTable();

        geselecteerdeLeerlingLabel = new Label(
                "Selecteer een leerling om diens voorkeuren en historiek te bekijken."
        );
        geselecteerdeLeerlingLabel.getStyleClass().add("selected-student-label");

        voorkeurenTitelLabel = new Label("Voorkeuren voor geselecteerde periode");
        voorkeurenTitelLabel.getStyleClass().add("section-title");

        voorkeurenTable = maakVoorkeurenTable();
        historiekTable = maakHistoriekTable();

        doelTalentComboBox = new ComboBox<>();
        doelTalentComboBox.setPromptText("Kies het nieuwe talent");
        doelTalentComboBox.setPrefWidth(300);
        doelTalentComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(IngerichtTalent ingerichtTalent) {
                if (ingerichtTalent == null) return "";

                return ingerichtTalent.getTalent().getNaam()
                        + " — capaciteit "
                        + ingerichtTalent.getMaxCapaciteit();
            }

            @Override
            public IngerichtTalent fromString(String string) {
                return null;
            }
        });

        verplaatsLeerlingButton = new Button("Leerling toewijzen of verplaatsen");
        verplaatsLeerlingButton.getStyleClass().add("primary-button");

        statusLabel = new Label("Selecteer een periode om de verdeling te bekijken.");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setWrapText(true);
    }

    private TableView<IngerichtTalentOverzicht> maakTalentenTable() {
        TableView<IngerichtTalentOverzicht> table = new TableView<>();
        table.setPlaceholder(new Label("Geen ingerichte talenten gevonden."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<IngerichtTalentOverzicht, String> talentKolom = new TableColumn<>("Talent");
        talentKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().ingerichtTalent().getTalent().getNaam()
        ));

        TableColumn<IngerichtTalentOverzicht, String> doelgroepKolom = new TableColumn<>("Doelgroep");
        doelgroepKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                formatteerDoelgroep(data.getValue().ingerichtTalent())
        ));

        TableColumn<IngerichtTalentOverzicht, Number> toegewezenKolom = new TableColumn<>("Toegewezen");
        toegewezenKolom.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(
                data.getValue().aantalToegewezen()
        ));

        TableColumn<IngerichtTalentOverzicht, Number> capaciteitKolom = new TableColumn<>("Capaciteit");
        capaciteitKolom.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(
                data.getValue().ingerichtTalent().getMaxCapaciteit()
        ));

        TableColumn<IngerichtTalentOverzicht, Number> vrijKolom = new TableColumn<>("Vrij");
        vrijKolom.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(
                data.getValue().aantalVrijePlaatsen()
        ));

        table.getColumns().addAll(
                talentKolom,
                doelgroepKolom,
                toegewezenKolom,
                capaciteitKolom,
                vrijKolom
        );

        return table;
    }

    private TableView<Toewijzing> maakLeerlingenTable() {
        TableView<Toewijzing> table = new TableView<>();
        table.setPlaceholder(new Label("Selecteer links een talent."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Toewijzing, String> leerlingKolom = new TableColumn<>("Leerling");
        leerlingKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getLeerling().toString()
        ));

        TableColumn<Toewijzing, String> klasKolom = new TableColumn<>("Klas");
        klasKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getLeerling().getKlas().getNaam()
        ));

        TableColumn<Toewijzing, String> typeKolom = new TableColumn<>("Type");
        typeKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getToewijzingsType().name()
        ));

        TableColumn<Toewijzing, String> voorkeurKolom = new TableColumn<>("Voorkeur");
        voorkeurKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getVoorkeurNummer() == null
                        ? "—"
                        : data.getValue().getVoorkeurNummer().toString()
        ));

        table.getColumns().addAll(
                leerlingKolom,
                klasKolom,
                typeKolom,
                voorkeurKolom
        );

        return table;
    }

    private TableView<LeerlingToewijzingOverzicht> maakKlasLeerlingenTable() {
        TableView<LeerlingToewijzingOverzicht> table = new TableView<>();
        table.setPlaceholder(new Label("Selecteer een klas om alle leerlingen te bekijken."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<LeerlingToewijzingOverzicht, String> leerlingKolom =
                new TableColumn<>("Leerling");

        leerlingKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().leerling().toString()
        ));

        TableColumn<LeerlingToewijzingOverzicht, String> talentKolom =
                new TableColumn<>("Toegewezen talent");

        talentKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                naamVanToegewezenTalent(data.getValue())
        ));

        TableColumn<LeerlingToewijzingOverzicht, String> typeKolom =
                new TableColumn<>("Type");

        typeKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                typeVanToewijzing(data.getValue())
        ));

        TableColumn<LeerlingToewijzingOverzicht, String> voorkeurKolom =
                new TableColumn<>("Voorkeur");

        voorkeurKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                voorkeurVanToewijzing(data.getValue())
        ));

        table.getColumns().addAll(
                leerlingKolom,
                talentKolom,
                typeKolom,
                voorkeurKolom
        );

        return table;
    }

    private TableView<NietToegewezenLeerlingOverzicht> maakNietToegewezenLeerlingenTable() {
        TableView<NietToegewezenLeerlingOverzicht> table = new TableView<>();
        table.setPlaceholder(new Label("Alle leerlingen zijn toegewezen."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<NietToegewezenLeerlingOverzicht, String> naamKolom =
                new TableColumn<>("Leerling");

        naamKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().volledigeNaam()
        ));

        TableColumn<NietToegewezenLeerlingOverzicht, String> klasKolom =
                new TableColumn<>("Klas");

        klasKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().klasNaam()
        ));

        table.getColumns().addAll(naamKolom, klasKolom);

        return table;
    }

    private TableView<Voorkeur> maakVoorkeurenTable() {
        TableView<Voorkeur> table = new TableView<>();
        table.setPlaceholder(new Label("Geen voorkeuren gevonden voor deze periode."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(150);

        TableColumn<Voorkeur, Number> nummerKolom = new TableColumn<>("Nr.");
        nummerKolom.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(
                data.getValue().getVoorkeurNummer()
        ));
        nummerKolom.setMaxWidth(55);

        TableColumn<Voorkeur, String> talentKolom = new TableColumn<>("Talent");
        talentKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getIngerichtTalent().getTalent().getNaam()
        ));

        table.getColumns().addAll(nummerKolom, talentKolom);

        return table;
    }

    private TableView<Toewijzing> maakHistoriekTable() {
        TableView<Toewijzing> table = new TableView<>();
        table.setPlaceholder(new Label("Nog geen eerder gevolgde talenten gevonden."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(150);

        TableColumn<Toewijzing, String> periodeKolom = new TableColumn<>("Periode");
        periodeKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue()
                        .getIngerichtTalent()
                        .getTalentenPeriode()
                        .getNaam()
        ));

        TableColumn<Toewijzing, String> talentKolom = new TableColumn<>("Talent");
        talentKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue()
                        .getIngerichtTalent()
                        .getTalent()
                        .getNaam()
        ));

        TableColumn<Toewijzing, String> typeKolom = new TableColumn<>("Type");
        typeKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getToewijzingsType().name()
        ));

        TableColumn<Toewijzing, String> voorkeurKolom = new TableColumn<>("Voorkeur");
        voorkeurKolom.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getVoorkeurNummer() == null
                        ? "—"
                        : data.getValue().getVoorkeurNummer().toString()
        ));

        table.getColumns().addAll(
                periodeKolom,
                talentKolom,
                typeKolom,
                voorkeurKolom
        );

        return table;
    }

    private void layoutNodes() {
        setLeft(sidebar);

        Label titel = new Label("Verdelen");
        titel.getStyleClass().add("page-title");

        Label ondertitel = new Label("Voer de automatische verdeling uit, bekijk toewijzingen en pas ze waar nodig manueel aan.");
        ondertitel.getStyleClass().add("page-subtitle");

        VBox titelBox = new VBox(4, titel, ondertitel);

        HBox selectieBalk = new HBox(
                12,
                maakVeldLabel("Schooljaar", schooljaarComboBox),
                maakVeldLabel("Periode", periodeComboBox),
                overzichtLadenButton,
                automatischeVerdelingButton
        );
        selectieBalk.setAlignment(Pos.BOTTOM_LEFT);
        selectieBalk.getStyleClass().add("toolbar");
        selectieBalk.setPadding(new Insets(16));

        VBox bovenkant = new VBox(18, titelBox, selectieBalk);
        bovenkant.setPadding(new Insets(28, 28, 12, 28));
        setTop(bovenkant);

        SplitPane perTalentSplitPane = new SplitPane(
                maakPaneel("Ingerichte talenten", talentenTable),
                maakPaneel(leerlingenTitelLabel, leerlingenTable)
        );
        perTalentSplitPane.setDividerPositions(0.48);
        perTalentSplitPane.setPadding(new Insets(8, 12, 12, 12));

        HBox klasSelectieBalk = new HBox(12, new Label("Klas:"), klasComboBox);
        klasSelectieBalk.setAlignment(Pos.CENTER_LEFT);

        Label klasUitleg = new Label(
                "Dit overzicht toont alle leerlingen van de geselecteerde klas, ook wanneer ze nog niet zijn toegewezen."
        );
        klasUitleg.getStyleClass().add("muted-label");
        klasUitleg.setWrapText(true);

        VBox klasPaneel = new VBox(12, klasSelectieBalk, klasUitleg, klasLeerlingenTable);
        VBox.setVgrow(klasLeerlingenTable, Priority.ALWAYS);
        klasPaneel.setPadding(new Insets(16));
        klasPaneel.getStyleClass().add("content-card");

        Label nietToegewezenUitleg = new Label(
                "Deze leerlingen hebben voor de geselecteerde periode nog geen toewijzing. Selecteer een leerling om diens voorkeuren te bekijken en de leerling manueel toe te wijzen."
        );
        nietToegewezenUitleg.getStyleClass().add("muted-label");
        nietToegewezenUitleg.setWrapText(true);

        VBox nietToegewezenPaneel = new VBox(12, nietToegewezenUitleg, nietToegewezenLeerlingenTable);
        VBox.setVgrow(nietToegewezenLeerlingenTable, Priority.ALWAYS);
        nietToegewezenPaneel.setPadding(new Insets(16));
        nietToegewezenPaneel.getStyleClass().add("content-card");

        Tab perTalentTab = new Tab("Per talent", perTalentSplitPane);
        Tab perKlasTab = new Tab("Per klas", klasPaneel);
        nietToegewezenTab = new Tab("Niet toegewezen (0)", nietToegewezenPaneel);

        perTalentTab.setClosable(false);
        perKlasTab.setClosable(false);
        nietToegewezenTab.setClosable(false);

        TabPane overzichtTabPane = new TabPane(perTalentTab, perKlasTab, nietToegewezenTab);
        overzichtTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        overzichtTabPane.setPadding(new Insets(0, 28, 12, 28));
        setCenter(overzichtTabPane);

        Label historiekTitel = new Label("Eerder gevolgde talenten");
        historiekTitel.getStyleClass().add("section-title");

        Label handmatigeTitel = new Label("Manuele toewijzing");
        handmatigeTitel.getStyleClass().add("section-title");

        Label uitleg = new Label("Selecteer een leerling in een van de tabellen en kies daarna een ingericht talent.");
        uitleg.getStyleClass().add("muted-label");
        uitleg.setWrapText(true);

        VBox voorkeurenPaneel = maakDetailPaneel(voorkeurenTitelLabel, voorkeurenTable);
        VBox historiekPaneel = maakDetailPaneel(historiekTitel, historiekTable);

        VBox wijzigingPaneel = new VBox(10, handmatigeTitel, uitleg, doelTalentComboBox, verplaatsLeerlingButton);
        wijzigingPaneel.setPadding(new Insets(12));
        wijzigingPaneel.getStyleClass().add("detail-section");

        SplitPane detailSplitPane = new SplitPane(voorkeurenPaneel, historiekPaneel, wijzigingPaneel);
        detailSplitPane.setDividerPositions(0.30, 0.72);
        detailSplitPane.setPrefHeight(205);

        VBox onderkant = new VBox(8, geselecteerdeLeerlingLabel, detailSplitPane, statusLabel);
        onderkant.setPadding(new Insets(12, 28, 20, 28));
        onderkant.getStyleClass().add("bottom-panel");
        BorderPane.setMargin(onderkant, new Insets(0, 18, 18, 18));
        setBottom(onderkant);

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

        Button leerlingen = maakSidebarPlaceholder("○  Leerlingen");
        Button klassen = maakSidebarPlaceholder("▣  Klassen");
        Button leerkrachten = maakSidebarPlaceholder("○  Leerkrachten");
        Button talenten = maakSidebarPlaceholder("✦  Talenten");
        Button periodes = maakSidebarPlaceholder("◷  Talentenperiodes");
        Button ingerichte = maakSidebarPlaceholder("▤  Ingerichte talenten");
        Button voorkeuren = maakSidebarPlaceholder("⇩  Voorkeuren importeren");
        Button automatisch = maakSidebarPlaceholder("◎  Automatische verdeling");

        Button actief = new Button("♙  Toewijzingen bekijken");
        actief.setMaxWidth(Double.MAX_VALUE);
        actief.setAlignment(Pos.CENTER_LEFT);
        actief.getStyleClass().addAll("sidebar-button", "sidebar-button-active");

        Button manueel = maakSidebarPlaceholder("↔  Manuele toewijzingen");

        Region tussenruimte = new Region();
        tussenruimte.setPrefHeight(10);
        VBox menu = new VBox(4, terugButton, leerlingen, klassen, leerkrachten, talenten, periodes, ingerichte, tussenruimte, voorkeuren, automatisch, actief, manueel);
        menu.setPadding(new Insets(12));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label rol = new Label("Talentcoördinator");
        rol.getStyleClass().add("sidebar-user-name");
        Label sub = new Label("Toewijzingen en verdeling");
        sub.getStyleClass().add("sidebar-user-subtitle");
        VBox gebruiker = new VBox(3, rol, sub);
        gebruiker.setPadding(new Insets(16));
        gebruiker.getStyleClass().add("sidebar-user-box");

        VBox sidebar = new VBox(brandBox, separator, menu, spacer, gebruiker);
        sidebar.setPrefWidth(245);
        sidebar.setMinWidth(225);
        sidebar.getStyleClass().add("sidebar");
        return sidebar;
    }

    private Button maakSidebarPlaceholder(String tekst) {
        Button button = new Button(tekst);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.getStyleClass().add("sidebar-button");
        button.setDisable(true);
        return button;
    }

    private VBox maakVeldLabel(String labelTekst, ComboBox<?> comboBox) {
        Label label = new Label(labelTekst);
        label.getStyleClass().add("field-label");
        VBox box = new VBox(6, label, comboBox);
        return box;
    }

    private <T> VBox maakPaneel(String titelTekst, TableView<T> table) {
        Label titel = new Label(titelTekst);
        titel.getStyleClass().add("section-title");

        return maakPaneel(titel, table);
    }

    private <T> VBox maakPaneel(Label titel, TableView<T> table) {
        VBox paneel = new VBox(10, titel, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        paneel.setPadding(new Insets(16));
        paneel.getStyleClass().add("content-card");

        return paneel;
    }

    private <T> VBox maakDetailPaneel(Label titel, TableView<T> table) {
        VBox paneel = new VBox(8, titel, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        paneel.setPadding(new Insets(12));
        paneel.getStyleClass().add("detail-section");

        return paneel;
    }

    private String formatteerDoelgroep(IngerichtTalent ingerichtTalent) {
        return switch (ingerichtTalent.getDoelgroep()) {
            case OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB ->
                    "Observatie / opleidingsfase";
            case KWALIFICATIEFASE_TWEEDEGRAAD_AB ->
                    "Kwalificatiefase";
        };
    }

    private String naamVanToegewezenTalent(LeerlingToewijzingOverzicht overzicht) {
        return overzicht.toewijzing() == null
                ? "Niet toegewezen"
                : overzicht.toewijzing()
                .getIngerichtTalent()
                .getTalent()
                .getNaam();
    }

    private String typeVanToewijzing(LeerlingToewijzingOverzicht overzicht) {
        return overzicht.toewijzing() == null
                ? "—"
                : overzicht.toewijzing().getToewijzingsType().name();
    }

    private String voorkeurVanToewijzing(LeerlingToewijzingOverzicht overzicht) {
        if (overzicht.toewijzing() == null
                || overzicht.toewijzing().getVoorkeurNummer() == null) {
            return "—";
        }

        return overzicht.toewijzing().getVoorkeurNummer().toString();
    }

    public void setSchooljaren(List<Schooljaar> schooljaren) {
        schooljaarComboBox.setItems(
                FXCollections.observableArrayList(schooljaren)
        );

        schooljaarComboBox.setDisable(schooljaren.isEmpty());
    }

    public void setPeriodes(List<TalentenPeriode> periodes) {
        periodeComboBox.setItems(
                FXCollections.observableArrayList(periodes)
        );

        periodeComboBox.setDisable(periodes.isEmpty());
    }

    public void setKlassen(List<Klas> klassen) {
        klasComboBox.setItems(
                FXCollections.observableArrayList(klassen)
        );

        klasComboBox.setDisable(klassen.isEmpty());
    }

    public void setOverzichten(List<IngerichtTalentOverzicht> overzichten) {
        talentenTable.setItems(
                FXCollections.observableArrayList(overzichten)
        );

        List<IngerichtTalent> ingerichteTalenten = overzichten.stream()
                .map(IngerichtTalentOverzicht::ingerichtTalent)
                .toList();

        doelTalentComboBox.setItems(
                FXCollections.observableArrayList(ingerichteTalenten)
        );
    }

    public void setToewijzingen(String talentNaam, List<Toewijzing> toewijzingen) {
        if (talentNaam == null || talentNaam.isBlank()) {
            leerlingenTitelLabel.setText("Toegewezen leerlingen");
        } else {
            leerlingenTitelLabel.setText(
                    "Toegewezen leerlingen voor " + talentNaam
            );
        }

        leerlingenTable.setItems(
                FXCollections.observableArrayList(toewijzingen)
        );
    }

    public void setKlasOverzicht(KlasOverzicht overzicht) {
        List<LeerlingToewijzingOverzicht> leerlingen =
                overzicht == null
                        ? List.of()
                        : overzicht.leerlingen();

        klasLeerlingenTable.setItems(
                FXCollections.observableArrayList(leerlingen)
        );
    }

    public void setNietToegewezenLeerlingen(
            List<NietToegewezenLeerlingOverzicht> leerlingen
    ) {
        nietToegewezenLeerlingenTable.setItems(
                FXCollections.observableArrayList(leerlingen)
        );

        nietToegewezenTab.setText(
                "Niet toegewezen (" + leerlingen.size() + ")"
        );
    }

    public void setLeerlingDetails(
            LeerlingDetailsOverzicht details,
            Toewijzing huidigeToewijzing
    ) {
        doelTalentComboBox.getSelectionModel().clearSelection();

        if (details == null) {
            geselecteerdeLeerlingLabel.setText(
                    "Selecteer een leerling om diens voorkeuren en historiek te bekijken."
            );

            voorkeurenTitelLabel.setText(
                    "Voorkeuren voor geselecteerde periode"
            );

            voorkeurenTable.setItems(
                    FXCollections.observableArrayList()
            );

            historiekTable.setItems(
                    FXCollections.observableArrayList()
            );

            return;
        }

        String huidigTalent = huidigeToewijzing == null
                ? "Niet toegewezen"
                : huidigeToewijzing
                .getIngerichtTalent()
                .getTalent()
                .getNaam();

        geselecteerdeLeerlingLabel.setText(
                details.leerling()
                        + " — klas "
                        + details.leerling().getKlas().getNaam()
                        + " — huidige toewijzing: "
                        + huidigTalent
        );

        voorkeurenTitelLabel.setText(
                "Voorkeuren voor " + details.periode().getNaam()
        );

        voorkeurenTable.setItems(
                FXCollections.observableArrayList(details.voorkeuren())
        );

        historiekTable.setItems(
                FXCollections.observableArrayList(
                        details.historischeToewijzingen()
                )
        );
    }

    public void toonMelding(String melding) {
        statusLabel.setText(melding);
        statusLabel.getStyleClass().removeAll(
                "status-error",
                "status-success"
        );
    }

    public void toonSucces(String melding) {
        statusLabel.setText(melding);
        statusLabel.getStyleClass().remove("status-error");

        if (!statusLabel.getStyleClass().contains("status-success")) {
            statusLabel.getStyleClass().add("status-success");
        }
    }

    public void toonFout(String melding) {
        statusLabel.setText(melding);
        statusLabel.getStyleClass().remove("status-success");

        if (!statusLabel.getStyleClass().contains("status-error")) {
            statusLabel.getStyleClass().add("status-error");
        }
    }

    public boolean vraagBevestiging(String titel, String boodschap) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titel);
        alert.setHeaderText(titel);
        alert.setContentText(boodschap);

        return alert.showAndWait()
                .filter(ButtonType.OK::equals)
                .isPresent();
    }

    public void setWijzigingenToegestaan(boolean toegestaan) {
        automatischeVerdelingButton.setDisable(!toegestaan);
        doelTalentComboBox.setDisable(!toegestaan);
        verplaatsLeerlingButton.setDisable(!toegestaan);
    }

    public Button getTerugButton() {
        return terugButton;
    }

    public ComboBox<Schooljaar> getSchooljaarComboBox() {
        return schooljaarComboBox;
    }

    public ComboBox<TalentenPeriode> getPeriodeComboBox() {
        return periodeComboBox;
    }

    public Button getOverzichtLadenButton() {
        return overzichtLadenButton;
    }

    public Button getAutomatischeVerdelingButton() {
        return automatischeVerdelingButton;
    }

    public TableView<IngerichtTalentOverzicht> getTalentenTable() {
        return talentenTable;
    }

    public TableView<Toewijzing> getLeerlingenTable() {
        return leerlingenTable;
    }

    public ComboBox<Klas> getKlasComboBox() {
        return klasComboBox;
    }

    public TableView<LeerlingToewijzingOverzicht> getKlasLeerlingenTable() {
        return klasLeerlingenTable;
    }

    public TableView<NietToegewezenLeerlingOverzicht> getNietToegewezenLeerlingenTable() {
        return nietToegewezenLeerlingenTable;
    }

    public ComboBox<IngerichtTalent> getDoelTalentComboBox() {
        return doelTalentComboBox;
    }

    public Button getVerplaatsLeerlingButton() {
        return verplaatsLeerlingButton;
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }
}