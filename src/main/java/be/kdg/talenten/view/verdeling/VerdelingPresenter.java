package be.kdg.talenten.view.verdeling;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.overzicht.IngerichtTalentOverzicht;
import be.kdg.talenten.overzicht.LeerlingDetailsOverzicht;
import be.kdg.talenten.overzicht.LeerlingToewijzingOverzicht;
import be.kdg.talenten.service.beheer.KlasService;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.service.beheer.TalentenPeriodeService;
import be.kdg.talenten.service.leerling.LeerlingDetailsService;
import be.kdg.talenten.service.verdeling.AutomatischeVerdelingService;
import be.kdg.talenten.service.verdeling.ManueleToewijzingService;
import be.kdg.talenten.service.verdeling.VerdelingBekijkenService;
import be.kdg.talenten.service.verdeling.VerdelingExcelService;
import be.kdg.talenten.verdeling.VerdelingsResultaat;
import be.kdg.talenten.view.SceneManager;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class VerdelingPresenter {

    private final VerdelingView view;
    private final Runnable terugNaarHoofdmenu;

    private final SchooljaarService schooljaarService;
    private final TalentenPeriodeService talentenPeriodeService;
    private final KlasService klasService;

    private final VerdelingBekijkenService verdelingBekijkenService;
    private final AutomatischeVerdelingService automatischeVerdelingService;
    private final ManueleToewijzingService manueleToewijzingService;
    private final LeerlingDetailsService leerlingDetailsService;
    private final VerdelingExcelService verdelingExcelService;

    private Leerling geselecteerdeLeerling;

    private boolean schooljarenWordenGeladen;

    public VerdelingPresenter(
            ApplicationConfig config,
            VerdelingView view,
            SceneManager sceneManager,
            Runnable terugNaarHoofdmenu
    ) {
        if (config == null
                || view == null
                || sceneManager == null
                || terugNaarHoofdmenu == null) {

            throw new IllegalArgumentException(
                    "Config, view, sceneManager en terugactie mogen niet null zijn"
            );
        }

        this.view =
                view;

        this.terugNaarHoofdmenu =
                terugNaarHoofdmenu;

        this.schooljaarService =
                config.getSchooljaarService();

        this.talentenPeriodeService =
                config.getTalentenPeriodeService();

        this.klasService =
                config.getKlasService();

        this.verdelingBekijkenService =
                config.getVerdelingBekijkenService();

        this.automatischeVerdelingService =
                config.getAutomatischeVerdelingService();

        this.manueleToewijzingService =
                config.getManueleToewijzingService();

        this.leerlingDetailsService =
                config.getLeerlingDetailsService();

        this.verdelingExcelService =
                config.getVerdelingExcelService();

        addEventHandlers();
        laadSchooljaren();
    }

    private void addEventHandlers() {
        view.getTerugButton()
                .setOnAction(
                        event ->
                                terugNaarHoofdmenu.run()
                );

        view.getSchooljaarComboBox()
                .setOnAction(
                        event ->
                                schooljaarGewijzigd()
                );

        view.getOverzichtLadenButton()
                .setOnAction(
                        event ->
                                laadOverzicht()
                );

        view.getPeriodeComboBox()
                .setOnAction(
                        event ->
                                laadOverzicht()
                );

        view.getKlasComboBox()
                .setOnAction(
                        event ->
                                laadKlasOverzicht()
                );

        view.getTalentenTable()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (
                                observable,
                                oud,
                                nieuw
                        ) ->
                                toonToewijzingen(
                                        nieuw
                                )
                );

        view.getLeerlingenTable()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (
                                observable,
                                oud,
                                nieuw
                        ) ->
                                selecteerToewijzing(
                                        nieuw
                                )
                );

        view.getKlasLeerlingenTable()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (
                                observable,
                                oud,
                                nieuw
                        ) ->
                                selecteerLeerlingUitKlasOverzicht(
                                        nieuw
                                )
                );

        view.getAutomatischeVerdelingButton()
                .setOnAction(
                        event ->
                                voerAutomatischeVerdelingUit()
                );

        view.getVerplaatsLeerlingButton()
                .setOnAction(
                        event ->
                                verplaatsLeerling()
                );

        view.getExportPerTalentButton()
                .setOnAction(
                        event ->
                                exporteerPerTalent()
                );

        view.getExportPerKlasButton()
                .setOnAction(
                        event ->
                                exporteerPerKlas()
                );
    }

    private void laadSchooljaren() {
        try {
            schooljarenWordenGeladen =
                    true;

            List<Schooljaar> schooljaren =
                    schooljaarService
                            .zoekSelecteerbareSchooljaren();

            view.setSchooljaren(
                    schooljaren
            );

            if (schooljaren.isEmpty()) {
                view.setPeriodes(
                        List.of()
                );

                view.setKlassen(
                        List.of()
                );

                view.setExportToegestaan(
                        false
                );

                view.toonMelding(
                        "Er is geen selecteerbaar schooljaar opgeslagen."
                );

                return;
            }

            Schooljaar actiefSchooljaar =
                    schooljaarService
                            .zoekActiefSchooljaar()
                            .filter(
                                    schooljaren::contains
                            )
                            .orElse(
                                    schooljaren.getFirst()
                            );

            view
                    .getSchooljaarComboBox()
                    .getSelectionModel()
                    .select(
                            actiefSchooljaar
                    );

            laadGegevensVoorSchooljaar(
                    actiefSchooljaar
            );

        } catch (RuntimeException exception) {
            view.toonFout(
                    "De schooljaren konden niet geladen worden: "
                            + veiligBericht(
                            exception
                    )
            );

        } finally {
            schooljarenWordenGeladen =
                    false;
        }
    }

    private void schooljaarGewijzigd() {
        if (schooljarenWordenGeladen) {
            return;
        }

        Schooljaar schooljaar =
                view
                        .getSchooljaarComboBox()
                        .getValue();

        if (schooljaar == null) {
            view.setPeriodes(
                    List.of()
            );

            view.setKlassen(
                    List.of()
            );

            view.setExportToegestaan(
                    false
            );

            view.toonMelding(
                    "Selecteer eerst een schooljaar."
            );

            return;
        }

        try {
            schooljaarService.maakActief(
                    schooljaar
            );

            laadGegevensVoorSchooljaar(
                    schooljaar
            );

            view.toonMelding(
                    "Schooljaar "
                            + schooljaar.getNaam()
                            + " is geselecteerd."
            );

        } catch (RuntimeException exception) {
            view.toonFout(
                    "Het schooljaar kon niet geselecteerd worden: "
                            + veiligBericht(
                            exception
                    )
            );
        }
    }

    private void laadGegevensVoorSchooljaar(
            Schooljaar schooljaar
    ) {
        laadKlassen(
                schooljaar
        );

        laadPeriodes(
                schooljaar
        );
    }

    private void laadPeriodes(
            Schooljaar schooljaar
    ) {
        List<TalentenPeriode> periodes =
                talentenPeriodeService
                        .geefPeriodesVoorSchooljaar(
                                schooljaar
                        );

        view.setPeriodes(
                periodes
        );

        if (periodes.isEmpty()) {
            view.setOverzichten(
                    List.of()
            );

            view.setToewijzingen(
                    null,
                    List.of()
            );

            view.setKlasOverzicht(
                    null
            );

            view.setExportToegestaan(
                    false
            );

            view.toonMelding(
                    "Voor schooljaar "
                            + schooljaar.getNaam()
                            + " zijn nog geen talentenperiodes opgeslagen."
            );

            return;
        }

        view
                .getPeriodeComboBox()
                .getSelectionModel()
                .select(
                        bepaalStandaardPeriode(
                                periodes
                        )
                );

        laadOverzicht();
    }

    private TalentenPeriode bepaalStandaardPeriode(
            List<TalentenPeriode> periodes
    ) {
        LocalDate vandaag =
                LocalDate.now();

        return periodes
                .stream()
                .filter(
                        periode ->
                                !vandaag.isBefore(
                                        periode.getStartDatum()
                                )
                                        && !vandaag.isAfter(
                                        periode.getEindDatum()
                                )
                )
                .findFirst()
                .orElseGet(
                        () ->
                                periodes
                                        .stream()
                                        .filter(
                                                periode ->
                                                        !periode
                                                                .getStartDatum()
                                                                .isBefore(
                                                                        vandaag
                                                                )
                                        )
                                        .min(
                                                Comparator.comparing(
                                                        TalentenPeriode::getStartDatum
                                                )
                                        )
                                        .orElse(
                                                periodes.getLast()
                                        )
                );
    }

    private void laadKlassen(
            Schooljaar schooljaar
    ) {
        view
                .getKlasComboBox()
                .getSelectionModel()
                .clearSelection();

        List<Klas> klassen =
                klasService
                        .geefAlleKlassen()
                        .stream()
                        .filter(
                                klas ->
                                        klas
                                                .getSchooljaar()
                                                .equals(
                                                        schooljaar
                                                )
                        )
                        .sorted(
                                Comparator.comparing(
                                        Klas::getNaam
                                )
                        )
                        .toList();

        view.setKlassen(
                klassen
        );

        if (klassen.isEmpty()) {
            view.setKlasOverzicht(
                    null
            );

            return;
        }

        view
                .getKlasComboBox()
                .getSelectionModel()
                .selectFirst();
    }

    private void laadOverzicht() {
        TalentenPeriode periode =
                view
                        .getPeriodeComboBox()
                        .getValue();

        pasWijzigMogelijkhedenAan(
                periode
        );

        view.setExportToegestaan(
                periode != null
        );

        wisGeselecteerdeLeerling();

        if (periode == null) {
            view.setOverzichten(
                    List.of()
            );

            view.setToewijzingen(
                    null,
                    List.of()
            );

            view.setKlasOverzicht(
                    null
            );

            view.toonMelding(
                    "Selecteer eerst een talentenperiode."
            );

            return;
        }

        try {
            List<IngerichtTalentOverzicht> overzichten =
                    verdelingBekijkenService
                            .bekijkPerIngerichtTalent(
                                    periode
                            );

            view.setOverzichten(
                    overzichten
            );

            view.setToewijzingen(
                    null,
                    List.of()
            );

            laadKlasOverzicht();

            if (overzichten.isEmpty()) {
                view.toonMelding(
                        "Voor deze periode zijn nog geen talenten ingericht."
                );

                return;
            }

            view
                    .getTalentenTable()
                    .getSelectionModel()
                    .selectFirst();

            int totaalToegewezen =
                    overzichten
                            .stream()
                            .mapToInt(
                                    IngerichtTalentOverzicht::aantalToegewezen
                            )
                            .sum();

            String melding =
                    "Overzicht geladen voor "
                            + periode.getNaam()
                            + ". "
                            + totaalToegewezen
                            + " leerlingen toegewezen in totaal.";

            if (isAfgelopenPeriode(
                    periode
            )) {
                melding +=
                        " Deze periode is afgelopen en kan alleen bekeken of geëxporteerd worden.";
            }

            view.toonMelding(
                    melding
            );

        } catch (RuntimeException exception) {
            view.toonFout(
                    "Het overzicht kon niet geladen worden: "
                            + veiligBericht(
                            exception
                    )
            );
        }
    }

    private void laadKlasOverzicht() {
        TalentenPeriode periode =
                view
                        .getPeriodeComboBox()
                        .getValue();

        Klas klas =
                view
                        .getKlasComboBox()
                        .getValue();

        if (periode == null
                || klas == null) {

            view.setKlasOverzicht(
                    null
            );

            return;
        }

        try {
            view.setKlasOverzicht(
                    verdelingBekijkenService
                            .bekijkVoorKlas(
                                    periode,
                                    klas
                            )
            );

        } catch (RuntimeException exception) {
            view.setKlasOverzicht(
                    null
            );

            view.toonFout(
                    "Het klasoverzicht kon niet geladen worden: "
                            + veiligBericht(
                            exception
                    )
            );
        }
    }

    private void toonToewijzingen(
            IngerichtTalentOverzicht overzicht
    ) {
        wisGeselecteerdeLeerling();

        if (overzicht == null) {
            view.setToewijzingen(
                    null,
                    List.of()
            );

            return;
        }

        view.setToewijzingen(
                overzicht
                        .ingerichtTalent()
                        .getNaam(),
                overzicht.toewijzingen()
        );
    }

    private void selecteerToewijzing(
            Toewijzing toewijzing
    ) {
        if (toewijzing == null) {
            return;
        }

        view
                .getKlasLeerlingenTable()
                .getSelectionModel()
                .clearSelection();

        selecteerLeerling(
                toewijzing.getLeerling(),
                toewijzing
        );
    }

    private void selecteerLeerlingUitKlasOverzicht(
            LeerlingToewijzingOverzicht overzicht
    ) {
        if (overzicht == null) {
            return;
        }

        view
                .getLeerlingenTable()
                .getSelectionModel()
                .clearSelection();

        selecteerLeerling(
                overzicht.leerling(),
                overzicht.toewijzing()
        );
    }

    private void selecteerLeerling(
            Leerling leerling,
            Toewijzing huidigeToewijzing
    ) {
        TalentenPeriode periode =
                view
                        .getPeriodeComboBox()
                        .getValue();

        if (leerling == null
                || periode == null) {

            wisGeselecteerdeLeerling();
            return;
        }

        try {
            LeerlingDetailsOverzicht details =
                    leerlingDetailsService
                            .bekijk(
                                    leerling,
                                    periode
                            );

            geselecteerdeLeerling =
                    leerling;

            view.setLeerlingDetails(
                    details,
                    huidigeToewijzing
            );

        } catch (RuntimeException exception) {
            wisGeselecteerdeLeerling();

            view.toonFout(
                    "De leerlinginformatie kon niet geladen worden: "
                            + veiligBericht(
                            exception
                    )
            );
        }
    }

    private void wisGeselecteerdeLeerling() {
        geselecteerdeLeerling =
                null;

        view.setLeerlingDetails(
                null,
                null
        );
    }

    private void voerAutomatischeVerdelingUit() {
        TalentenPeriode periode =
                view
                        .getPeriodeComboBox()
                        .getValue();

        if (periode == null) {
            view.toonFout(
                    "Selecteer eerst een talentenperiode."
            );

            return;
        }

        if (isAfgelopenPeriode(
                periode
        )) {
            view.toonFout(
                    "Een afgelopen talentenperiode kan niet opnieuw verdeeld worden."
            );

            return;
        }

        try {
            boolean bestaandeToewijzingen =
                    automatischeVerdelingService
                            .heeftBestaandeToewijzingen(
                                    periode
                            );

            String boodschap =
                    bestaandeToewijzingen
                            ? "De bestaande automatische toewijzingen voor "
                            + periode.getNaam()
                            + " worden opnieuw berekend. "
                            + "Manuele toewijzingen blijven behouden. Doorgaan?"
                            : "De automatische verdeling voor "
                            + periode.getNaam()
                            + " wordt uitgevoerd en opgeslagen. Doorgaan?";

            if (!view.vraagBevestiging(
                    "Automatische verdeling",
                    boodschap
            )) {
                return;
            }

            VerdelingsResultaat resultaat =
                    automatischeVerdelingService
                            .voerAutomatischeVerdelingUit(
                                    periode
                            );

            laadOverzicht();

            String melding =
                    resultaat.getAantalToewijzingen()
                            + " leerlingen automatisch toegewezen. "
                            + "Niet toegewezen: "
                            + resultaat
                            .getNietToegewezenLeerlingen()
                            .size()
                            + ".";

            if (bestaandeToewijzingen) {
                melding +=
                        " De manuele toewijzingen zijn behouden.";
            }

            view.toonSucces(
                    melding
            );

        } catch (RuntimeException exception) {
            view.toonFout(
                    "De automatische verdeling is niet uitgevoerd: "
                            + veiligBericht(
                            exception
                    )
            );
        }
    }

    private void verplaatsLeerling() {
        TalentenPeriode periode =
                view
                        .getPeriodeComboBox()
                        .getValue();

        IngerichtTalent doelTalent =
                view
                        .getDoelTalentComboBox()
                        .getValue();

        if (periode == null) {
            view.toonFout(
                    "Selecteer eerst een talentenperiode."
            );

            return;
        }

        if (isAfgelopenPeriode(
                periode
        )) {
            view.toonFout(
                    "Een toewijzing uit een afgelopen periode kan niet gewijzigd worden."
            );

            return;
        }

        if (geselecteerdeLeerling == null) {
            view.toonFout(
                    "Selecteer eerst een leerling in het overzicht per talent of per klas."
            );

            return;
        }

        if (doelTalent == null) {
            view.toonFout(
                    "Kies het talent waarnaar de leerling verplaatst moet worden."
            );

            return;
        }

        try {
            String leerlingNaam =
                    geselecteerdeLeerling.toString();

            manueleToewijzingService
                    .wijzigToewijzing(
                            periode,
                            geselecteerdeLeerling,
                            doelTalent
                    );

            laadOverzicht();

            view.toonSucces(
                    leerlingNaam
                            + " werd verplaatst naar "
                            + doelTalent.getNaam()
                            + "."
            );

        } catch (RuntimeException exception) {
            view.toonFout(
                    "De leerling kon niet verplaatst worden: "
                            + veiligBericht(
                            exception
                    )
            );
        }
    }

    private void exporteerPerTalent() {
        TalentenPeriode periode =
                view
                        .getPeriodeComboBox()
                        .getValue();

        if (periode == null) {
            view.toonFout(
                    "Selecteer eerst een talentenperiode."
            );

            return;
        }

        Path bestand =
                kiesExportBestand(
                        "verdeling-per-talent-"
                                + maakVeiligeBestandsnaam(
                                periode.getNaam()
                        )
                                + ".xlsx"
                );

        if (bestand == null) {
            return;
        }

        try {
            verdelingExcelService
                    .exporteerPerIngerichtTalent(
                            periode,
                            bestand
                    );

            view.toonSucces(
                    "De verdeling per talent werd succesvol geëxporteerd."
            );

        } catch (RuntimeException exception) {
            view.toonFout(
                    "De export kon niet aangemaakt worden: "
                            + veiligBericht(
                            exception
                    )
            );
        }
    }

    private void exporteerPerKlas() {
        TalentenPeriode periode =
                view
                        .getPeriodeComboBox()
                        .getValue();

        if (periode == null) {
            view.toonFout(
                    "Selecteer eerst een talentenperiode."
            );

            return;
        }

        Path bestand =
                kiesExportBestand(
                        "verdeling-per-klas-"
                                + maakVeiligeBestandsnaam(
                                periode.getNaam()
                        )
                                + ".xlsx"
                );

        if (bestand == null) {
            return;
        }

        try {
            verdelingExcelService
                    .exporteerPerKlas(
                            periode,
                            bestand
                    );

            view.toonSucces(
                    "De verdeling per klas werd succesvol geëxporteerd."
            );

        } catch (RuntimeException exception) {
            view.toonFout(
                    "De export kon niet aangemaakt worden: "
                            + veiligBericht(
                            exception
                    )
            );
        }
    }

    private Path kiesExportBestand(
            String standaardBestandsnaam
    ) {
        FileChooser fileChooser =
                new FileChooser();

        fileChooser.setTitle(
                "Verdeling exporteren"
        );

        fileChooser.setInitialFileName(
                standaardBestandsnaam
        );

        fileChooser
                .getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                "Excel-bestand (*.xlsx)",
                                "*.xlsx"
                        )
                );

        File bestand =
                fileChooser.showSaveDialog(
                        view
                                .getScene()
                                .getWindow()
                );

        if (bestand == null) {
            return null;
        }

        String pad =
                bestand.getAbsolutePath();

        if (!pad
                .toLowerCase()
                .endsWith(
                        ".xlsx"
                )) {

            bestand =
                    new File(
                            pad + ".xlsx"
                    );
        }

        return bestand.toPath();
    }

    private String maakVeiligeBestandsnaam(
            String waarde
    ) {
        if (waarde == null
                || waarde.isBlank()) {

            return "verdeling";
        }

        String resultaat =
                waarde
                        .toLowerCase()
                        .trim()
                        .replaceAll(
                                "[^a-z0-9]+",
                                "-"
                        )
                        .replaceAll(
                                "^-|-$",
                                ""
                        );

        return resultaat.isBlank()
                ? "verdeling"
                : resultaat;
    }

    private void pasWijzigMogelijkhedenAan(
            TalentenPeriode periode
    ) {
        view.setWijzigingenToegestaan(
                periode != null
                        && !isAfgelopenPeriode(
                        periode
                )
        );
    }

    private boolean isAfgelopenPeriode(
            TalentenPeriode periode
    ) {
        return periode
                .getEindDatum()
                .isBefore(
                        LocalDate.now()
                );
    }

    private String veiligBericht(
            RuntimeException exception
    ) {
        return exception.getMessage() == null
                || exception
                .getMessage()
                .isBlank()
                ? exception
                .getClass()
                .getSimpleName()
                : exception.getMessage();
    }
}