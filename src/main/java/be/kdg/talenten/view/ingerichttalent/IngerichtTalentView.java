package be.kdg.talenten.view.ingerichttalent;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Leerkracht;
import be.kdg.talenten.domain.Talent;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.view.navigation.AppSidebar;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IngerichtTalentView extends BorderPane {

    private final AppSidebar sidebar;

    private final ComboBox<TalentenPeriode> periodeFilterComboBox;
    private final Button nieuwIngerichtTalentButton;
    private final TableView<IngerichtTalent> ingerichteTalentenTable;

    private final Label formulierTitelLabel;
    private final ComboBox<TalentenPeriode> periodeComboBox;
    private final ComboBox<Talent> talentComboBox;
    private final ComboBox<Doelgroep> doelgroepComboBox;
    private final TextField naamField;
    private final TextArea omschrijvingArea;
    private final Spinner<Integer> maxCapaciteitSpinner;
    private final CheckBox actiefCheckBox;

    private final ListView<Leerkracht> gekoppeldeLeerkrachtenList;
    private final ComboBox<Leerkracht> leerkrachtComboBox;
    private final Button leerkrachtToevoegenButton;
    private final Button leerkrachtVerwijderenButton;

    private final List<Leerkracht> alleLeerkrachten =
            new ArrayList<>();

    private final Button opslaanButton;
    private final Button annulerenButton;
    private final Label statusLabel;

    public IngerichtTalentView() {
        sidebar =
                new AppSidebar(
                        AppSidebar.Sectie.INGERICHTE_TALENTEN,
                        "Ingerichte talenten beheren"
                );


        periodeFilterComboBox =
                new ComboBox<>();

        periodeFilterComboBox.setPrefWidth(
                260
        );

        periodeFilterComboBox.setConverter(
                periodeConverter()
        );


        nieuwIngerichtTalentButton =
                new Button(
                        "+ Nieuw ingericht talent"
                );

        nieuwIngerichtTalentButton
                .getStyleClass()
                .add(
                        "primary-button"
                );


        ingerichteTalentenTable =
                maakTabel();


        formulierTitelLabel =
                new Label(
                        "Nieuw ingericht talent"
                );

        formulierTitelLabel
                .getStyleClass()
                .add(
                        "section-heading"
                );


        periodeComboBox =
                new ComboBox<>();

        periodeComboBox.setMaxWidth(
                Double.MAX_VALUE
        );

        periodeComboBox.setConverter(
                periodeConverter()
        );


        talentComboBox =
                new ComboBox<>();

        talentComboBox.setMaxWidth(
                Double.MAX_VALUE
        );


        doelgroepComboBox =
                new ComboBox<>();

        doelgroepComboBox.setItems(
                FXCollections.observableArrayList(
                        Doelgroep.values()
                )
        );

        doelgroepComboBox.setMaxWidth(
                Double.MAX_VALUE
        );

        doelgroepComboBox.setConverter(
                doelgroepConverter()
        );


        naamField =
                new TextField();

        naamField.setPromptText(
                "Bijv. Schaken observatie"
        );


        omschrijvingArea =
                new TextArea();

        omschrijvingArea.setPromptText(
                "Periode-specifieke omschrijving"
        );

        omschrijvingArea.setWrapText(
                true
        );

        omschrijvingArea.setPrefRowCount(
                4
        );


        maxCapaciteitSpinner =
                new Spinner<>(
                        1,
                        200,
                        10
                );

        maxCapaciteitSpinner.setEditable(
                true
        );

        maxCapaciteitSpinner.setMaxWidth(
                Double.MAX_VALUE
        );


        actiefCheckBox =
                new CheckBox(
                        "Actief voor nieuwe voorkeuren en verdelingen"
                );

        actiefCheckBox.setSelected(
                true
        );


        gekoppeldeLeerkrachtenList =
                new ListView<>();

        gekoppeldeLeerkrachtenList.setPrefHeight(
                108
        );

        gekoppeldeLeerkrachtenList.setPlaceholder(
                new Label(
                        "Nog geen leerkracht gekoppeld."
                )
        );

        gekoppeldeLeerkrachtenList.setCellFactory(
                list ->
                        leerkrachtCell()
        );


        leerkrachtComboBox =
                new ComboBox<>();

        leerkrachtComboBox.setPromptText(
                "Kies een leerkracht"
        );

        leerkrachtComboBox.setMaxWidth(
                Double.MAX_VALUE
        );

        leerkrachtComboBox.setConverter(
                leerkrachtConverter()
        );


        leerkrachtToevoegenButton =
                new Button(
                        "Toevoegen"
                );

        leerkrachtToevoegenButton
                .getStyleClass()
                .add(
                        "primary-outline-button"
                );


        leerkrachtVerwijderenButton =
                new Button(
                        "Verwijderen"
                );

        leerkrachtVerwijderenButton
                .getStyleClass()
                .add(
                        "secondary-button"
                );


        opslaanButton =
                new Button(
                        "Opslaan"
                );

        opslaanButton
                .getStyleClass()
                .add(
                        "primary-button"
                );


        annulerenButton =
                new Button(
                        "Annuleren"
                );

        annulerenButton
                .getStyleClass()
                .add(
                        "primary-outline-button"
                );


        statusLabel =
                new Label(
                        "Selecteer een periode om ingerichte talenten te beheren."
                );

        statusLabel.setWrapText(
                true
        );

        statusLabel
                .getStyleClass()
                .add(
                        "status-label"
                );


        layoutNodes();
    }

    private TableView<IngerichtTalent> maakTabel() {
        TableView<IngerichtTalent> table =
                new TableView<>();

        table.setPlaceholder(
                new Label(
                        "Geen ingerichte talenten voor deze periode."
                )
        );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );


        TableColumn<IngerichtTalent, String> naamKolom =
                new TableColumn<>(
                        "Naam"
                );

        naamKolom.setCellValueFactory(
                data ->
                        new ReadOnlyStringWrapper(
                                data.getValue().getNaam()
                        )
        );

        naamKolom.setPrefWidth(
                180
        );


        TableColumn<IngerichtTalent, String> talentKolom =
                new TableColumn<>(
                        "Talent"
                );

        talentKolom.setCellValueFactory(
                data ->
                        new ReadOnlyStringWrapper(
                                data.getValue()
                                        .getTalent()
                                        .getNaam()
                        )
        );

        talentKolom.setPrefWidth(
                125
        );


        TableColumn<IngerichtTalent, String> doelgroepKolom =
                new TableColumn<>(
                        "Doelgroep"
                );

        doelgroepKolom.setCellValueFactory(
                data ->
                        new ReadOnlyStringWrapper(
                                formatteerDoelgroepKort(
                                        data.getValue()
                                                .getDoelgroep()
                                )
                        )
        );

        doelgroepKolom.setPrefWidth(
                150
        );


        TableColumn<IngerichtTalent, String> capaciteitKolom =
                new TableColumn<>(
                        "Cap."
                );

        capaciteitKolom.setCellValueFactory(
                data ->
                        new ReadOnlyStringWrapper(
                                String.valueOf(
                                        data.getValue()
                                                .getMaxCapaciteit()
                                )
                        )
        );

        capaciteitKolom.setPrefWidth(
                70
        );


        TableColumn<IngerichtTalent, String> leerkrachtenKolom =
                new TableColumn<>(
                        "Leerkrachten"
                );

        leerkrachtenKolom.setCellValueFactory(
                data ->
                        new ReadOnlyStringWrapper(
                                formatteerLeerkrachten(
                                        data.getValue()
                                                .getLeerkrachten()
                                )
                        )
        );

        leerkrachtenKolom.setPrefWidth(
                190
        );


        TableColumn<IngerichtTalent, String> statusKolom =
                new TableColumn<>(
                        "Status"
                );

        statusKolom.setCellValueFactory(
                data ->
                        new ReadOnlyStringWrapper(
                                data.getValue().isActief()
                                        ? "Actief"
                                        : "Inactief"
                        )
        );

        statusKolom.setPrefWidth(
                90
        );


        table.getColumns().addAll(
                naamKolom,
                talentKolom,
                doelgroepKolom,
                capaciteitKolom,
                leerkrachtenKolom,
                statusKolom
        );

        return table;
    }

    private void layoutNodes() {
        setLeft(
                sidebar
        );

        setCenter(
                maakInhoud()
        );

        getStyleClass()
                .add(
                        "app-background"
                );
    }

    private VBox maakInhoud() {
        Label titel =
                new Label(
                        "Ingerichte talenten beheren"
                );

        titel
                .getStyleClass()
                .add(
                        "page-title"
                );


        Label ondertitel =
                new Label(
                        "Configureer talenten per periode, doelgroep en capaciteit en koppel maximaal twee leerkrachten."
                );

        ondertitel
                .getStyleClass()
                .add(
                        "page-subtitle"
                );


        Label filterLabel =
                new Label(
                        "Periode"
                );

        filterLabel
                .getStyleClass()
                .add(
                        "field-label"
                );


        VBox filterBox =
                new VBox(
                        5,
                        filterLabel,
                        periodeFilterComboBox
                );


        Region headerSpacer =
                new Region();

        HBox.setHgrow(
                headerSpacer,
                Priority.ALWAYS
        );


        HBox header =
                new HBox(
                        18,
                        new VBox(
                                4,
                                titel,
                                ondertitel
                        ),
                        headerSpacer,
                        filterBox,
                        nieuwIngerichtTalentButton
                );

        header.setAlignment(
                Pos.BOTTOM_LEFT
        );


        Label overzichtTitel =
                new Label(
                        "Ingerichte talenten"
                );

        overzichtTitel
                .getStyleClass()
                .add(
                        "section-heading"
                );


        Label overzichtUitleg =
                new Label(
                        "Selecteer een ingericht talent om naam, omschrijving, capaciteit, status of leerkrachten te wijzigen."
                );

        overzichtUitleg.setWrapText(
                true
        );

        overzichtUitleg
                .getStyleClass()
                .add(
                        "muted-label"
                );


        VBox overzichtCard =
                new VBox(
                        10,
                        overzichtTitel,
                        overzichtUitleg,
                        ingerichteTalentenTable
                );

        overzichtCard
                .getStyleClass()
                .add(
                        "content-card"
                );

        overzichtCard.setPadding(
                new Insets(
                        22
                )
        );

        overzichtCard.setMinWidth(
                610
        );

        HBox.setHgrow(
                overzichtCard,
                Priority.ALWAYS
        );

        VBox.setVgrow(
                ingerichteTalentenTable,
                Priority.ALWAYS
        );


        VBox formulierCard =
                maakFormulierCard();


        HBox beheer =
                new HBox(
                        16,
                        overzichtCard,
                        formulierCard
                );

        HBox.setHgrow(
                overzichtCard,
                Priority.ALWAYS
        );

        VBox.setVgrow(
                beheer,
                Priority.ALWAYS
        );


        VBox inhoud =
                new VBox(
                        22,
                        header,
                        beheer,
                        statusLabel
                );

        inhoud.setPadding(
                new Insets(
                        32
                )
        );

        VBox.setVgrow(
                beheer,
                Priority.ALWAYS
        );

        return inhoud;
    }

    private VBox maakFormulierCard() {
        Label periodeLabel =
                maakFieldLabel(
                        "Talentenperiode"
                );

        Label talentLabel =
                maakFieldLabel(
                        "Basistalent"
                );

        Label doelgroepLabel =
                maakFieldLabel(
                        "Doelgroep"
                );

        Label naamLabel =
                maakFieldLabel(
                        "Naam"
                );

        Label omschrijvingLabel =
                maakFieldLabel(
                        "Omschrijving"
                );

        Label capaciteitLabel =
                maakFieldLabel(
                        "Maximumcapaciteit"
                );

        Label leerkrachtenLabel =
                maakFieldLabel(
                        "Leerkrachten (0–2)"
                );


        HBox leerkrachtToevoegenBox =
                new HBox(
                        8,
                        leerkrachtComboBox,
                        leerkrachtToevoegenButton
                );

        HBox.setHgrow(
                leerkrachtComboBox,
                Priority.ALWAYS
        );


        HBox leerkrachtActies =
                new HBox(
                        leerkrachtVerwijderenButton
                );

        leerkrachtActies.setAlignment(
                Pos.CENTER_RIGHT
        );


        HBox knoppen =
                new HBox(
                        10,
                        annulerenButton,
                        opslaanButton
                );

        knoppen.setAlignment(
                Pos.CENTER_RIGHT
        );


        VBox formulier =
                new VBox(
                        8,
                        formulierTitelLabel,
                        periodeLabel,
                        periodeComboBox,
                        talentLabel,
                        talentComboBox,
                        doelgroepLabel,
                        doelgroepComboBox,
                        naamLabel,
                        naamField,
                        omschrijvingLabel,
                        omschrijvingArea,
                        capaciteitLabel,
                        maxCapaciteitSpinner,
                        actiefCheckBox,
                        leerkrachtenLabel,
                        gekoppeldeLeerkrachtenList,
                        leerkrachtToevoegenBox,
                        leerkrachtActies,
                        knoppen
                );

        formulier
                .getStyleClass()
                .add(
                        "content-card"
                );

        formulier.setPadding(
                new Insets(
                        22
                )
        );

        formulier.setPrefWidth(
                420
        );

        formulier.setMinWidth(
                390
        );

        return formulier;
    }

    private Label maakFieldLabel(String tekst) {
        Label label =
                new Label(
                        tekst
                );

        label
                .getStyleClass()
                .add(
                        "field-label"
                );

        return label;
    }

    public void setPeriodes(List<TalentenPeriode> periodes) {
        periodeFilterComboBox.setItems(
                FXCollections.observableArrayList(
                        periodes
                )
        );

        periodeComboBox.setItems(
                FXCollections.observableArrayList(
                        periodes
                )
        );
    }

    public void setTalenten(List<Talent> talenten) {
        talentComboBox.setItems(
                FXCollections.observableArrayList(
                        talenten
                )
        );
    }

    public void setLeerkrachten(List<Leerkracht> leerkrachten) {
        alleLeerkrachten.clear();

        alleLeerkrachten.addAll(
                leerkrachten
        );

        vernieuwBeschikbareLeerkrachten();
    }

    public void setIngerichteTalenten(List<IngerichtTalent> ingerichteTalenten) {
        ingerichteTalentenTable
                .getItems()
                .setAll(
                        ingerichteTalenten
                );

        ingerichteTalentenTable.refresh();
    }

    public void selecteerFilterPeriode(TalentenPeriode periode) {
        periodeFilterComboBox
                .getSelectionModel()
                .select(
                        periode
                );
    }

    public void toonNieuwFormulier(TalentenPeriode periode) {
        formulierTitelLabel.setText(
                "Nieuw ingericht talent"
        );

        ingerichteTalentenTable
                .getSelectionModel()
                .clearSelection();


        periodeComboBox.setDisable(
                false
        );

        talentComboBox.setDisable(
                false
        );

        doelgroepComboBox.setDisable(
                false
        );

        actiefCheckBox.setDisable(
                true
        );


        periodeComboBox
                .getSelectionModel()
                .select(
                        periode
                );

        talentComboBox
                .getSelectionModel()
                .clearSelection();

        doelgroepComboBox
                .getSelectionModel()
                .clearSelection();

        naamField.clear();

        omschrijvingArea.clear();

        maxCapaciteitSpinner
                .getValueFactory()
                .setValue(
                        10
                );

        actiefCheckBox.setSelected(
                true
        );

        gekoppeldeLeerkrachtenList
                .getItems()
                .clear();

        leerkrachtComboBox
                .getSelectionModel()
                .clearSelection();


        setLeerkrachtBeheerToegestaan(
                true
        );

        vernieuwBeschikbareLeerkrachten();


        naamField.requestFocus();

        setStatus(
                "Vul de gegevens in. Een ingericht talent mag tijdelijk zonder leerkracht bestaan."
        );
    }

    public void toonWijzigFormulier(IngerichtTalent ingerichtTalent) {
        formulierTitelLabel.setText(
                "Ingericht talent wijzigen"
        );


        periodeComboBox
                .getSelectionModel()
                .select(
                        ingerichtTalent.getTalentenPeriode()
                );

        talentComboBox
                .getSelectionModel()
                .select(
                        ingerichtTalent.getTalent()
                );

        doelgroepComboBox
                .getSelectionModel()
                .select(
                        ingerichtTalent.getDoelgroep()
                );

        naamField.setText(
                ingerichtTalent.getNaam()
        );

        omschrijvingArea.setText(
                ingerichtTalent.getOmschrijving()
        );

        maxCapaciteitSpinner
                .getValueFactory()
                .setValue(
                        ingerichtTalent.getMaxCapaciteit()
                );

        actiefCheckBox.setSelected(
                ingerichtTalent.isActief()
        );


        gekoppeldeLeerkrachtenList
                .getItems()
                .setAll(
                        ingerichtTalent.getLeerkrachten()
                );

        leerkrachtComboBox
                .getSelectionModel()
                .clearSelection();


        periodeComboBox.setDisable(
                true
        );

        talentComboBox.setDisable(
                true
        );

        doelgroepComboBox.setDisable(
                true
        );

        actiefCheckBox.setDisable(
                false
        );


        setLeerkrachtBeheerToegestaan(
                ingerichtTalent.isActief()
        );

        vernieuwBeschikbareLeerkrachten();


        setStatus(
                ingerichtTalent.isActief()
                        ? "Basistalent, periode en doelgroep blijven bij een bestaand ingericht talent ongewijzigd."
                        : "Dit ingericht talent is inactief. Activeer het opnieuw om leerkrachten te koppelen."
        );
    }

    public boolean voegLeerkrachtAanFormulierToe(Leerkracht leerkracht) {
        if (leerkracht == null) {
            return false;
        }

        if (gekoppeldeLeerkrachtenList
                .getItems()
                .contains(
                        leerkracht
                )) {

            setStatus(
                    "Deze leerkracht is al gekoppeld."
            );

            return false;
        }

        if (gekoppeldeLeerkrachtenList
                .getItems()
                .size() >= 2) {

            toonFout(
                    "Een ingericht talent mag maximaal twee leerkrachten hebben."
            );

            return false;
        }

        gekoppeldeLeerkrachtenList
                .getItems()
                .add(
                        leerkracht
                );

        vernieuwBeschikbareLeerkrachten();

        return true;
    }

    public void verwijderGeselecteerdeLeerkrachtUitFormulier() {
        Leerkracht geselecteerd =
                gekoppeldeLeerkrachtenList
                        .getSelectionModel()
                        .getSelectedItem();

        if (geselecteerd == null) {
            setStatus(
                    "Selecteer eerst een gekoppelde leerkracht om te verwijderen."
            );

            return;
        }

        gekoppeldeLeerkrachtenList
                .getItems()
                .remove(
                        geselecteerd
                );

        vernieuwBeschikbareLeerkrachten();
    }

    public List<Leerkracht> getFormulierLeerkrachten() {
        return List.copyOf(
                gekoppeldeLeerkrachtenList.getItems()
        );
    }

    public void setLeerkrachtBeheerToegestaan(boolean toegestaan) {
        leerkrachtComboBox.setDisable(
                !toegestaan
        );

        leerkrachtToevoegenButton.setDisable(
                !toegestaan
        );

        leerkrachtVerwijderenButton.setDisable(
                !toegestaan
        );
    }

    public boolean vraagBevestiging(String titel, String boodschap) {
        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle(
                titel
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                boodschap
        );

        return alert
                .showAndWait()
                .filter(
                        buttonType ->
                                buttonType == ButtonType.OK
                )
                .isPresent();
    }

    public void setStatus(String tekst) {
        statusLabel.setText(
                tekst == null
                        ? ""
                        : tekst
        );
    }

    public void toonSucces(String tekst) {
        setStatus(
                tekst
        );
    }

    public void toonFout(String bericht) {
        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                "Fout"
        );

        alert.setHeaderText(
                "De actie kon niet uitgevoerd worden"
        );

        alert.setContentText(
                bericht
        );

        alert.showAndWait();
    }

    private String formatteerDoelgroepKort(Doelgroep doelgroep) {
        if (doelgroep == null) {
            return "";
        }

        return switch (doelgroep) {
            case OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB ->
                    "Observatie / opleiding";

            case KWALIFICATIEFASE_TWEEDEGRAAD_AB ->
                    "Kwalificatie";
        };
    }

    private String formatteerLeerkrachten(List<Leerkracht> leerkrachten) {
        if (leerkrachten == null
                || leerkrachten.isEmpty()) {

            return "Nog niet toegewezen";
        }

        return leerkrachten
                .stream()
                .map(
                        leerkracht ->
                                leerkracht.getVoornaam()
                                        + " "
                                        + leerkracht.getAchternaam()
                )
                .collect(
                        Collectors.joining(
                                ", "
                        )
                );
    }

    private StringConverter<TalentenPeriode> periodeConverter() {
        return new StringConverter<>() {

            @Override
            public String toString(TalentenPeriode periode) {
                if (periode == null) {
                    return "";
                }

                return periode
                        .getSchooljaar()
                        .getNaam()
                        + " • "
                        + periode.getNaam();
            }

            @Override
            public TalentenPeriode fromString(String string) {
                return null;
            }
        };
    }

    private StringConverter<Doelgroep> doelgroepConverter() {
        return new StringConverter<>() {

            @Override
            public String toString(Doelgroep doelgroep) {
                if (doelgroep == null) {
                    return "";
                }

                return switch (doelgroep) {
                    case OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB ->
                            "Observatie / opleidingsfase / 1e graad A-B";

                    case KWALIFICATIEFASE_TWEEDEGRAAD_AB ->
                            "Kwalificatiefase / 2e graad A-B";
                };
            }

            @Override
            public Doelgroep fromString(String string) {
                return null;
            }
        };
    }

    private StringConverter<Leerkracht> leerkrachtConverter() {
        return new StringConverter<>() {

            @Override
            public String toString(Leerkracht leerkracht) {
                if (leerkracht == null) {
                    return "";
                }

                return leerkracht.getVoornaam()
                        + " "
                        + leerkracht.getAchternaam();
            }

            @Override
            public Leerkracht fromString(String string) {
                return null;
            }
        };
    }

    private ListCell<Leerkracht> leerkrachtCell() {
        return new ListCell<>() {

            @Override
            protected void updateItem(
                    Leerkracht leerkracht,
                    boolean empty
            ) {
                super.updateItem(
                        leerkracht,
                        empty
                );

                setText(
                        empty || leerkracht == null
                                ? null
                                : leerkracht.getVoornaam()
                                + " "
                                + leerkracht.getAchternaam()
                );
            }
        };
    }

    private void vernieuwBeschikbareLeerkrachten() {
        List<Leerkracht> gekoppeldeLeerkrachten =
                gekoppeldeLeerkrachtenList.getItems();

        List<Leerkracht> beschikbareLeerkrachten =
                alleLeerkrachten
                        .stream()
                        .filter(
                                leerkracht ->
                                        !gekoppeldeLeerkrachten.contains(
                                                leerkracht
                                        )
                        )
                        .toList();

        leerkrachtComboBox.setItems(
                FXCollections.observableArrayList(
                        beschikbareLeerkrachten
                )
        );

        leerkrachtComboBox
                .getSelectionModel()
                .clearSelection();
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }

    public ComboBox<TalentenPeriode> getPeriodeFilterComboBox() {
        return periodeFilterComboBox;
    }

    public Button getNieuwIngerichtTalentButton() {
        return nieuwIngerichtTalentButton;
    }

    public TableView<IngerichtTalent> getIngerichteTalentenTable() {
        return ingerichteTalentenTable;
    }

    public ComboBox<TalentenPeriode> getPeriodeComboBox() {
        return periodeComboBox;
    }

    public ComboBox<Talent> getTalentComboBox() {
        return talentComboBox;
    }

    public ComboBox<Doelgroep> getDoelgroepComboBox() {
        return doelgroepComboBox;
    }

    public TextField getNaamField() {
        return naamField;
    }

    public TextArea getOmschrijvingArea() {
        return omschrijvingArea;
    }

    public Spinner<Integer> getMaxCapaciteitSpinner() {
        return maxCapaciteitSpinner;
    }

    public CheckBox getActiefCheckBox() {
        return actiefCheckBox;
    }

    public ComboBox<Leerkracht> getLeerkrachtComboBox() {
        return leerkrachtComboBox;
    }

    public Button getLeerkrachtToevoegenButton() {
        return leerkrachtToevoegenButton;
    }

    public Button getLeerkrachtVerwijderenButton() {
        return leerkrachtVerwijderenButton;
    }

    public Button getOpslaanButton() {
        return opslaanButton;
    }

    public Button getAnnulerenButton() {
        return annulerenButton;
    }
}