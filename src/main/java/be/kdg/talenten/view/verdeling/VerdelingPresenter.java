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
import be.kdg.talenten.overzicht.NietToegewezenLeerlingOverzicht;
import be.kdg.talenten.service.verdeling.AutomatischeVerdelingService;
import be.kdg.talenten.service.beheer.KlasService;
import be.kdg.talenten.service.leerling.LeerlingDetailsService;
import be.kdg.talenten.service.verdeling.ManueleToewijzingService;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.service.beheer.TalentenPeriodeService;
import be.kdg.talenten.service.verdeling.VerdelingBekijkenService;
import be.kdg.talenten.verdeling.VerdelingsResultaat;
import be.kdg.talenten.view.SceneManager;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class VerdelingPresenter {
    private final VerdelingView view;
    private final SceneManager sceneManager;
    private final Runnable terugNaarHoofdmenu;

    private final SchooljaarService schooljaarService;
    private final TalentenPeriodeService talentenPeriodeService;
    private final KlasService klasService;
    private final VerdelingBekijkenService verdelingBekijkenService;
    private final AutomatischeVerdelingService automatischeVerdelingService;
    private final ManueleToewijzingService manueleToewijzingService;
    private final LeerlingDetailsService leerlingDetailsService;

    private Leerling geselecteerdeLeerling;

    private boolean schooljarenWordenGeladen;
    private boolean periodesWordenGeladen;
    private boolean klassenWordenGeladen;

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

        this.view = view;
        this.sceneManager = sceneManager;
        this.terugNaarHoofdmenu = terugNaarHoofdmenu;

        this.schooljaarService = config.getSchooljaarService();
        this.talentenPeriodeService = config.getTalentenPeriodeService();
        this.klasService = config.getKlasService();
        this.verdelingBekijkenService = config.getVerdelingBekijkenService();
        this.automatischeVerdelingService = config.getAutomatischeVerdelingService();
        this.manueleToewijzingService = config.getManueleToewijzingService();
        this.leerlingDetailsService = config.getLeerlingDetailsService();

        addEventHandlers();
        laadSchooljaren();
    }

    private void addEventHandlers() {
        view.getTerugButton().setOnAction(
                event -> terugNaarHoofdmenu.run()
        );

        view.getSchooljaarComboBox().setOnAction(event -> {
            if (!schooljarenWordenGeladen) {
                schooljaarGewijzigd();
            }
        });

        view.getPeriodeComboBox().setOnAction(event -> {
            if (!periodesWordenGeladen) {
                laadOverzicht();
            }
        });

        view.getKlasComboBox().setOnAction(event -> {
            if (!klassenWordenGeladen) {
                laadKlasOverzicht();
            }
        });

        view.getOverzichtLadenButton().setOnAction(
                event -> laadOverzicht()
        );

        view.getTalentenTable()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oud, nieuw) ->
                                toonToewijzingen(nieuw)
                );

        view.getLeerlingenTable()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oud, nieuw) ->
                                selecteerToewijzing(nieuw)
                );

        view.getKlasLeerlingenTable()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oud, nieuw) ->
                                selecteerLeerlingUitKlasOverzicht(nieuw)
                );

        view.getNietToegewezenLeerlingenTable()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oud, nieuw) ->
                                selecteerNietToegewezenLeerling(nieuw)
                );

        view.getAutomatischeVerdelingButton().setOnAction(
                event -> voerAutomatischeVerdelingUit()
        );

        view.getVerplaatsLeerlingButton().setOnAction(
                event -> verplaatsLeerling()
        );
    }

    private void laadSchooljaren() {
        try {
            schooljarenWordenGeladen = true;

            List<Schooljaar> schooljaren =
                    schooljaarService.zoekSelecteerbareSchooljaren();

            view.setSchooljaren(schooljaren);

            if (schooljaren.isEmpty()) {
                maakOverzichtenLeeg();
                view.setPeriodes(List.of());
                view.setKlassen(List.of());
                view.toonMelding(
                        "Er is geen selecteerbaar schooljaar opgeslagen."
                );
                return;
            }

            Schooljaar actiefSchooljaar =
                    schooljaarService.zoekActiefSchooljaar()
                            .filter(schooljaren::contains)
                            .orElse(schooljaren.getFirst());

            view.getSchooljaarComboBox()
                    .getSelectionModel()
                    .select(actiefSchooljaar);

            laadGegevensVoorSchooljaar(actiefSchooljaar);
        } catch (RuntimeException exception) {
            view.toonFout(
                    "De schooljaren konden niet geladen worden: "
                            + veiligBericht(exception)
            );
        } finally {
            schooljarenWordenGeladen = false;
        }
    }

    private void schooljaarGewijzigd() {
        Schooljaar schooljaar =
                view.getSchooljaarComboBox().getValue();

        if (schooljaar == null) {
            view.setPeriodes(List.of());
            view.setKlassen(List.of());
            maakOverzichtenLeeg();
            view.toonMelding("Selecteer eerst een schooljaar.");
            return;
        }

        try {
            schooljaarService.maakActief(schooljaar);
            laadGegevensVoorSchooljaar(schooljaar);

            view.toonMelding(
                    "Schooljaar "
                            + schooljaar.getNaam()
                            + " is geselecteerd en wordt bij de volgende "
                            + "opstart opnieuw gebruikt."
            );
        } catch (RuntimeException exception) {
            view.toonFout(
                    "Het schooljaar kon niet geselecteerd worden: "
                            + veiligBericht(exception)
            );
        }
    }

    private void laadGegevensVoorSchooljaar(Schooljaar schooljaar) {
        laadKlassen(schooljaar);
        laadPeriodes(schooljaar);
    }

    private void laadPeriodes(Schooljaar schooljaar) {
        try {
            periodesWordenGeladen = true;

            List<TalentenPeriode> periodes =
                    talentenPeriodeService.zoekVoorSchooljaar(schooljaar);

            view.setPeriodes(periodes);

            if (periodes.isEmpty()) {
                maakOverzichtenLeeg();
                view.toonMelding(
                        "Voor schooljaar "
                                + schooljaar.getNaam()
                                + " zijn nog geen talentenperiodes opgeslagen."
                );
                return;
            }

            TalentenPeriode standaardPeriode =
                    bepaalStandaardPeriode(periodes);

            view.getPeriodeComboBox()
                    .getSelectionModel()
                    .select(standaardPeriode);
        } finally {
            periodesWordenGeladen = false;
        }

        laadOverzicht();
    }

    private TalentenPeriode bepaalStandaardPeriode(
            List<TalentenPeriode> periodes
    ) {
        LocalDate vandaag = LocalDate.now();

        return periodes.stream()
                .filter(periode ->
                        !vandaag.isBefore(periode.getStartDatum())
                                && !vandaag.isAfter(periode.getEindDatum())
                )
                .findFirst()
                .orElseGet(() ->
                        periodes.stream()
                                .filter(periode ->
                                        !periode.getStartDatum()
                                                .isBefore(vandaag)
                                )
                                .min(Comparator.comparing(
                                        TalentenPeriode::getStartDatum
                                ))
                                .orElse(periodes.getLast())
                );
    }

    private void laadKlassen(Schooljaar schooljaar) {
        try {
            klassenWordenGeladen = true;

            List<Klas> klassen = klasService.zoekAlle().stream()
                    .filter(klas ->
                            klas.getSchooljaar()
                                    .equals(schooljaar.getNaam())
                    )
                    .toList();

            view.setKlassen(klassen);

            if (klassen.isEmpty()) {
                view.setKlasOverzicht(null);
                return;
            }

            view.getKlasComboBox()
                    .getSelectionModel()
                    .selectFirst();
        } finally {
            klassenWordenGeladen = false;
        }
    }

    private void laadOverzicht() {
        TalentenPeriode periode =
                view.getPeriodeComboBox().getValue();

        pasWijzigMogelijkhedenAan(periode);
        wisGeselecteerdeLeerling();

        if (periode == null) {
            maakOverzichtenLeeg();
            view.toonMelding(
                    "Selecteer eerst een talentenperiode."
            );
            return;
        }

        try {
            List<IngerichtTalentOverzicht> overzichten =
                    verdelingBekijkenService
                            .bekijkPerIngerichtTalent(periode);

            List<NietToegewezenLeerlingOverzicht>
                    nietToegewezenLeerlingen =
                    verdelingBekijkenService
                            .bekijkNietToegewezenLeerlingen(periode);

            view.setOverzichten(overzichten);
            view.setNietToegewezenLeerlingen(
                    nietToegewezenLeerlingen
            );
            view.setToewijzingen(null, List.of());

            laadKlasOverzicht();

            if (overzichten.isEmpty()) {
                view.toonMelding(
                        "Voor deze periode zijn nog geen talenten ingericht. "
                                + nietToegewezenLeerlingen.size()
                                + " leerlingen zijn niet toegewezen."
                );
                return;
            }

            view.getTalentenTable()
                    .getSelectionModel()
                    .selectFirst();

            int totaalToegewezen = overzichten.stream()
                    .mapToInt(
                            IngerichtTalentOverzicht::aantalToegewezen
                    )
                    .sum();

            String melding =
                    "Overzicht geladen voor "
                            + periode.getNaam()
                            + ". "
                            + totaalToegewezen
                            + " leerlingen toegewezen in totaal. "
                            + "Niet toegewezen: "
                            + nietToegewezenLeerlingen.size()
                            + ".";

            if (isAfgelopenPeriode(periode)) {
                melding +=
                        " Deze periode is afgelopen en kan alleen bekeken worden.";
            }

            view.toonMelding(melding);
        } catch (RuntimeException exception) {
            view.toonFout(
                    "Het overzicht kon niet geladen worden: "
                            + veiligBericht(exception)
            );
        }
    }

    private void laadKlasOverzicht() {
        TalentenPeriode periode =
                view.getPeriodeComboBox().getValue();

        Klas klas =
                view.getKlasComboBox().getValue();

        if (periode == null || klas == null) {
            view.setKlasOverzicht(null);
            return;
        }

        try {
            view.setKlasOverzicht(
                    verdelingBekijkenService
                            .bekijkVoorKlas(periode, klas)
            );
        } catch (RuntimeException exception) {
            view.setKlasOverzicht(null);

            view.toonFout(
                    "Het klasoverzicht kon niet geladen worden: "
                            + veiligBericht(exception)
            );
        }
    }

    private void toonToewijzingen(
            IngerichtTalentOverzicht overzicht
    ) {
        wisGeselecteerdeLeerling();

        if (overzicht == null) {
            view.setToewijzingen(null, List.of());
            return;
        }

        view.setToewijzingen(
                overzicht.ingerichtTalent()
                        .getTalent()
                        .getNaam(),
                overzicht.toewijzingen()
        );
    }

    private void selecteerToewijzing(Toewijzing toewijzing) {
        if (toewijzing == null) return;

        view.getKlasLeerlingenTable()
                .getSelectionModel()
                .clearSelection();

        view.getNietToegewezenLeerlingenTable()
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
        if (overzicht == null) return;

        view.getLeerlingenTable()
                .getSelectionModel()
                .clearSelection();

        view.getNietToegewezenLeerlingenTable()
                .getSelectionModel()
                .clearSelection();

        selecteerLeerling(
                overzicht.leerling(),
                overzicht.toewijzing()
        );
    }

    private void selecteerNietToegewezenLeerling(
            NietToegewezenLeerlingOverzicht overzicht
    ) {
        if (overzicht == null) return;

        view.getLeerlingenTable()
                .getSelectionModel()
                .clearSelection();

        view.getKlasLeerlingenTable()
                .getSelectionModel()
                .clearSelection();

        selecteerLeerling(
                overzicht.leerling(),
                null
        );
    }

    private void selecteerLeerling(
            Leerling leerling,
            Toewijzing huidigeToewijzing
    ) {
        TalentenPeriode periode =
                view.getPeriodeComboBox().getValue();

        if (leerling == null || periode == null) {
            wisGeselecteerdeLeerling();
            return;
        }

        try {
            LeerlingDetailsOverzicht details =
                    leerlingDetailsService.bekijk(
                            leerling,
                            periode
                    );

            geselecteerdeLeerling = leerling;

            view.setLeerlingDetails(
                    details,
                    huidigeToewijzing
            );
        } catch (RuntimeException exception) {
            wisGeselecteerdeLeerling();

            view.toonFout(
                    "De leerlinginformatie kon niet geladen worden: "
                            + veiligBericht(exception)
            );
        }
    }

    private void wisGeselecteerdeLeerling() {
        geselecteerdeLeerling = null;
        view.setLeerlingDetails(null, null);
    }

    private void voerAutomatischeVerdelingUit() {
        TalentenPeriode periode =
                view.getPeriodeComboBox().getValue();

        if (periode == null) {
            view.toonFout(
                    "Selecteer eerst een talentenperiode."
            );
            return;
        }

        try {
            boolean bestaandeToewijzingen =
                    automatischeVerdelingService
                            .heeftBestaandeToewijzingen(periode);

            String boodschap;

            if (bestaandeToewijzingen) {
                boodschap =
                        "De bestaande automatische toewijzingen voor "
                                + periode.getNaam()
                                + " worden opnieuw berekend. "
                                + "Manuele toewijzingen blijven behouden. "
                                + "Doorgaan?";
            } else {
                boodschap =
                        "De automatische verdeling voor "
                                + periode.getNaam()
                                + " wordt uitgevoerd en opgeslagen. "
                                + "Doorgaan?";
            }

            if (!view.vraagBevestiging(
                    "Automatische verdeling",
                    boodschap
            )) {
                return;
            }

            VerdelingsResultaat resultaat =
                    automatischeVerdelingService
                            .voerAutomatischeVerdelingUit(periode);

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

            view.toonSucces(melding);
        } catch (RuntimeException exception) {
            view.toonFout(
                    "De automatische verdeling is niet uitgevoerd: "
                            + veiligBericht(exception)
            );
        }
    }

    private void verplaatsLeerling() {
        TalentenPeriode periode =
                view.getPeriodeComboBox().getValue();

        IngerichtTalent doelTalent =
                view.getDoelTalentComboBox().getValue();

        if (periode == null) {
            view.toonFout(
                    "Selecteer eerst een talentenperiode."
            );
            return;
        }

        if (geselecteerdeLeerling == null) {
            view.toonFout(
                    "Selecteer eerst een leerling in het overzicht "
                            + "per talent, per klas of niet toegewezen."
            );
            return;
        }

        if (doelTalent == null) {
            view.toonFout(
                    "Kies het talent waaraan de leerling toegewezen moet worden."
            );
            return;
        }

        try {
            String leerlingNaam =
                    geselecteerdeLeerling.toString();

            manueleToewijzingService.wijzigToewijzing(
                    periode,
                    geselecteerdeLeerling,
                    doelTalent
            );

            laadOverzicht();

            view.toonSucces(
                    leerlingNaam
                            + " werd toegewezen aan "
                            + doelTalent.getTalent().getNaam()
                            + "."
            );
        } catch (RuntimeException exception) {
            view.toonFout(
                    "De leerling kon niet toegewezen worden: "
                            + veiligBericht(exception)
            );
        }
    }

    private void maakOverzichtenLeeg() {
        view.setOverzichten(List.of());
        view.setToewijzingen(null, List.of());
        view.setKlasOverzicht(null);
        view.setNietToegewezenLeerlingen(List.of());
        wisGeselecteerdeLeerling();
    }

    private String veiligBericht(RuntimeException exception) {
        if (exception.getMessage() == null
                || exception.getMessage().isBlank()) {
            return exception.getClass().getSimpleName();
        }

        return exception.getMessage();
    }

    private void pasWijzigMogelijkhedenAan(
            TalentenPeriode periode
    ) {
        boolean wijzigingenToegestaan =
                periode != null
                        && !isAfgelopenPeriode(periode);

        view.setWijzigingenToegestaan(
                wijzigingenToegestaan
        );
    }

    private boolean isAfgelopenPeriode(
            TalentenPeriode periode
    ) {
        return periode.getEindDatum()
                .isBefore(LocalDate.now());
    }
}