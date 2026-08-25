package be.kdg.talenten.view.ingerichttalent;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Leerkracht;
import be.kdg.talenten.domain.Talent;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.service.beheer.IngerichtTalentService;
import be.kdg.talenten.service.beheer.LeerkrachtService;
import be.kdg.talenten.service.beheer.TalentService;
import be.kdg.talenten.service.beheer.TalentenPeriodeService;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.navigation.AppNavigator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class IngerichtTalentPresenter {

    private final IngerichtTalentView view;

    private final IngerichtTalentService ingerichtTalentService;
    private final TalentService talentService;
    private final LeerkrachtService leerkrachtService;
    private final TalentenPeriodeService talentenPeriodeService;

    private final AppNavigator navigator;

    private IngerichtTalent geselecteerdIngerichtTalent;

    private boolean periodesWordenGeladen;

    public IngerichtTalentPresenter(
            ApplicationConfig config,
            IngerichtTalentView view,
            SceneManager sceneManager
    ) {
        if (config == null
                || view == null
                || sceneManager == null) {

            throw new IllegalArgumentException(
                    "Config, view en sceneManager mogen niet null zijn"
            );
        }

        this.view =
                view;

        this.ingerichtTalentService =
                config.getIngerichtTalentService();

        this.talentService =
                config.getTalentService();

        this.leerkrachtService =
                config.getLeerkrachtService();

        this.talentenPeriodeService =
                config.getTalentenPeriodeService();

        this.navigator =
                new AppNavigator(
                        config,
                        sceneManager
                );


        navigator.koppelSidebar(
                view.getSidebar()
        );

        addEventHandlers();
        laadReferentiegegevens();
    }

    private void addEventHandlers() {
        view.getNieuwIngerichtTalentButton()
                .setOnAction(
                        event ->
                                startNieuwIngerichtTalent()
                );


        view.getAnnulerenButton()
                .setOnAction(
                        event ->
                                startNieuwIngerichtTalent()
                );


        view.getOpslaanButton()
                .setOnAction(
                        event ->
                                slaOp()
                );


        view.getLeerkrachtToevoegenButton()
                .setOnAction(
                        event -> {
                            Leerkracht leerkracht =
                                    view.getLeerkrachtComboBox()
                                            .getValue();

                            if (leerkracht == null) {
                                view.setStatus(
                                        "Kies eerst een leerkracht."
                                );

                                return;
                            }

                            view.voegLeerkrachtAanFormulierToe(
                                    leerkracht
                            );
                        }
                );


        view.getLeerkrachtVerwijderenButton()
                .setOnAction(
                        event ->
                                view.verwijderGeselecteerdeLeerkrachtUitFormulier()
                );


        view.getPeriodeFilterComboBox()
                .setOnAction(
                        event -> {
                            if (!periodesWordenGeladen) {
                                periodeFilterGewijzigd();
                            }
                        }
                );


        view.getIngerichteTalentenTable()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (
                                observable,
                                oud,
                                nieuw
                        ) -> {
                            if (nieuw != null) {
                                toonIngerichtTalent(
                                        nieuw
                                );
                            }
                        }
                );


        view.getActiefCheckBox()
                .selectedProperty()
                .addListener(
                        (
                                observable,
                                oud,
                                actief
                        ) -> {
                            if (geselecteerdIngerichtTalent != null) {
                                view.setLeerkrachtBeheerToegestaan(
                                        actief
                                );
                            }
                        }
                );
    }

    private void laadReferentiegegevens() {
        try {
            List<TalentenPeriode> periodes =
                    new ArrayList<>(
                            talentenPeriodeService.zoekAlle()
                    );

            periodes.sort(
                    Comparator.comparing(
                                    TalentenPeriode::getStartDatum
                            )
                            .reversed()
            );


            List<Talent> talenten =
                    new ArrayList<>(
                            talentService.geefAlleTalenten()
                    );

            talenten.sort(
                    Comparator.comparing(
                            Talent::getNaam,
                            String.CASE_INSENSITIVE_ORDER
                    )
            );


            List<Leerkracht> leerkrachten =
                    new ArrayList<>(
                            leerkrachtService.geefAlleLeerkrachten()
                    );

            leerkrachten.sort(
                    Comparator
                            .comparing(
                                    Leerkracht::getAchternaam,
                                    String.CASE_INSENSITIVE_ORDER
                            )
                            .thenComparing(
                                    Leerkracht::getVoornaam,
                                    String.CASE_INSENSITIVE_ORDER
                            )
            );


            periodesWordenGeladen =
                    true;

            view.setPeriodes(
                    periodes
            );

            view.setTalenten(
                    talenten
            );

            view.setLeerkrachten(
                    leerkrachten
            );


            if (periodes.isEmpty()) {
                view.setIngerichteTalenten(
                        List.of()
                );

                view.toonNieuwFormulier(
                        null
                );

                view.setStatus(
                        "Maak eerst een talentenperiode aan."
                );

                return;
            }


            TalentenPeriode standaardPeriode =
                    kiesStandaardPeriode(
                            periodes
                    );

            view.selecteerFilterPeriode(
                    standaardPeriode
            );

            periodesWordenGeladen =
                    false;


            laadIngerichteTalenten(
                    standaardPeriode
            );

            startNieuwIngerichtTalent();

        } catch (RuntimeException exception) {
            periodesWordenGeladen =
                    false;

            view.toonFout(
                    "De gegevens voor het beheerscherm konden niet geladen worden: "
                            + veiligBericht(
                            exception
                    )
            );
        }
    }

    private TalentenPeriode kiesStandaardPeriode(
            List<TalentenPeriode> periodes
    ) {
        return periodes
                .stream()
                .filter(
                        periode ->
                                periode
                                        .getSchooljaar()
                                        .isActief()
                )
                .min(
                        Comparator.comparing(
                                TalentenPeriode::getStartDatum
                        )
                )
                .orElse(
                        periodes.getFirst()
                );
    }

    private void periodeFilterGewijzigd() {
        TalentenPeriode periode =
                view.getPeriodeFilterComboBox()
                        .getValue();

        laadIngerichteTalenten(
                periode
        );

        startNieuwIngerichtTalent();
    }

    private void laadIngerichteTalenten(
            TalentenPeriode periode
    ) {
        if (periode == null) {
            view.setIngerichteTalenten(
                    List.of()
            );

            return;
        }

        try {
            view.setIngerichteTalenten(
                    ingerichtTalentService
                            .geefIngerichteTalentenVoorPeriode(
                                    periode
                            )
            );

        } catch (RuntimeException exception) {
            view.toonFout(
                    "De ingerichte talenten konden niet geladen worden: "
                            + veiligBericht(
                            exception
                    )
            );
        }
    }

    private void startNieuwIngerichtTalent() {
        geselecteerdIngerichtTalent =
                null;

        view.toonNieuwFormulier(
                view.getPeriodeFilterComboBox()
                        .getValue()
        );
    }

    private void toonIngerichtTalent(
            IngerichtTalent ingerichtTalent
    ) {
        geselecteerdIngerichtTalent =
                ingerichtTalent;

        view.toonWijzigFormulier(
                ingerichtTalent
        );
    }

    private void slaOp() {
        try {
            String naam =
                    view.getNaamField()
                            .getText();

            String omschrijving =
                    view.getOmschrijvingArea()
                            .getText();

            int maxCapaciteit =
                    view.getMaxCapaciteitSpinner()
                            .getValue();

            List<Leerkracht> gewensteLeerkrachten =
                    view.getFormulierLeerkrachten();


            if (geselecteerdIngerichtTalent == null) {
                maakNieuwIngerichtTalent(
                        naam,
                        omschrijving,
                        maxCapaciteit,
                        gewensteLeerkrachten
                );

            } else {
                wijzigBestaandIngerichtTalent(
                        naam,
                        omschrijving,
                        maxCapaciteit,
                        gewensteLeerkrachten
                );
            }

        } catch (RuntimeException exception) {
            view.toonFout(
                    veiligBericht(
                            exception
                    )
            );
        }
    }

    private void maakNieuwIngerichtTalent(
            String naam,
            String omschrijving,
            int maxCapaciteit,
            List<Leerkracht> leerkrachten
    ) {
        TalentenPeriode periode =
                view.getPeriodeComboBox()
                        .getValue();

        Talent talent =
                view.getTalentComboBox()
                        .getValue();

        Doelgroep doelgroep =
                view.getDoelgroepComboBox()
                        .getValue();


        if (periode == null) {
            throw new IllegalArgumentException(
                    "Selecteer een talentenperiode"
            );
        }

        if (talent == null) {
            throw new IllegalArgumentException(
                    "Selecteer een basistalent"
            );
        }

        if (doelgroep == null) {
            throw new IllegalArgumentException(
                    "Selecteer een doelgroep"
            );
        }


        IngerichtTalent nieuw =
                ingerichtTalentService
                        .maakIngerichtTalent(
                                talent,
                                periode,
                                naam,
                                omschrijving,
                                maxCapaciteit,
                                doelgroep,
                                leerkrachten
                        );


        view.selecteerFilterPeriode(
                periode
        );

        laadIngerichteTalenten(
                periode
        );

        startNieuwIngerichtTalent();


        view.toonSucces(
                "Ingericht talent "
                        + nieuw.getNaam()
                        + " is toegevoegd."
        );
    }

    private void wijzigBestaandIngerichtTalent(
            String naam,
            String omschrijving,
            int maxCapaciteit,
            List<Leerkracht> gewensteLeerkrachten
    ) {
        IngerichtTalent ingerichtTalent =
                geselecteerdIngerichtTalent;


        boolean wasActief =
                ingerichtTalent.isActief();

        boolean moetActiefZijn =
                view.getActiefCheckBox()
                        .isSelected();

        boolean wordtGedeactiveerd =
                wasActief
                        && !moetActiefZijn;


        /*
         * Deactivatie heeft belangrijke gevolgen:
         * - bestaande toewijzingen verdwijnen;
         * - leerlingen worden opnieuw niet toegewezen;
         * - gekoppelde leerkrachten worden verwijderd.
         *
         * Daarom bevestigen vóór we iets aanpassen.
         */
        if (wordtGedeactiveerd) {
            boolean bevestigd =
                    view.vraagBevestiging(
                            "Ingericht talent deactiveren",
                            "Wanneer je dit ingericht talent deactiveert, "
                                    + "worden alle leerlingen die eraan toegewezen zijn "
                                    + "opnieuw niet toegewezen.\n\n"
                                    + "Ook alle gekoppelde leerkrachten worden verwijderd.\n\n"
                                    + "De voorkeuren van de leerlingen blijven behouden.\n\n"
                                    + "Wil je doorgaan?"
                    );


            if (!bevestigd) {
                view.getActiefCheckBox()
                        .setSelected(
                                true
                        );

                view.setLeerkrachtBeheerToegestaan(
                        true
                );

                view.setStatus(
                        "Deactivatie geannuleerd."
                );

                return;
            }
        }


        /*
         * Naam, omschrijving en capaciteit kunnen onafhankelijk
         * van de actieve status gewijzigd worden.
         */
        ingerichtTalentService
                .wijzigIngerichtTalent(
                        ingerichtTalent,
                        naam,
                        omschrijving,
                        maxCapaciteit
                );


        /*
         * ACTIEF -> INACTIEF
         *
         * De service bevat de businessregel en verwijdert:
         * - toewijzingen;
         * - leerkrachtkoppelingen;
         * en zet daarna het talent op inactief.
         *
         * Daarom synchroniseren we hier GEEN leerkrachten meer.
         */
        if (wordtGedeactiveerd) {
            ingerichtTalentService
                    .deactiveer(
                            ingerichtTalent
                    );
        }

        /*
         * INACTIEF -> ACTIEF
         *
         * Eerst opnieuw activeren en daarna de leerkrachten
         * koppelen die de gebruiker in het formulier koos.
         */
        else if (!wasActief
                && moetActiefZijn) {

            ingerichtTalentService
                    .activeer(
                            ingerichtTalent
                    );

            synchroniseerLeerkrachten(
                    ingerichtTalent,
                    gewensteLeerkrachten
            );
        }

        /*
         * ACTIEF -> ACTIEF
         *
         * Alleen gewone wijzigingen en leerkrachten synchroniseren.
         */
        else if (wasActief
                && moetActiefZijn) {

            synchroniseerLeerkrachten(
                    ingerichtTalent,
                    gewensteLeerkrachten
            );
        }

        /*
         * INACTIEF -> INACTIEF:
         * geen leerkrachten synchroniseren.
         */


        TalentenPeriode periode =
                ingerichtTalent
                        .getTalentenPeriode();


        laadIngerichteTalenten(
                periode
        );

        startNieuwIngerichtTalent();


        if (wordtGedeactiveerd) {
            view.toonSucces(
                    "Ingericht talent "
                            + naam
                            + " is gedeactiveerd. "
                            + "De toegewezen leerlingen zijn opnieuw niet toegewezen "
                            + "en de gekoppelde leerkrachten zijn vrijgegeven."
            );

        } else if (!wasActief
                && moetActiefZijn) {

            view.toonSucces(
                    "Ingericht talent "
                            + naam
                            + " is opnieuw geactiveerd."
            );

        } else {
            view.toonSucces(
                    "Ingericht talent "
                            + naam
                            + " is aangepast."
            );
        }
    }

    private void synchroniseerLeerkrachten(
            IngerichtTalent ingerichtTalent,
            List<Leerkracht> gewensteLeerkrachten
    ) {
        List<Leerkracht> huidigeLeerkrachten =
                new ArrayList<>(
                        ingerichtTalent.getLeerkrachten()
                );


        for (Leerkracht huidigeLeerkracht :
                huidigeLeerkrachten) {

            if (!gewensteLeerkrachten.contains(
                    huidigeLeerkracht
            )) {

                ingerichtTalentService
                        .verwijderLeerkracht(
                                ingerichtTalent,
                                huidigeLeerkracht
                        );
            }
        }


        for (Leerkracht gewensteLeerkracht :
                gewensteLeerkrachten) {

            if (!ingerichtTalent
                    .getLeerkrachten()
                    .contains(
                            gewensteLeerkracht
                    )) {

                ingerichtTalentService
                        .voegLeerkrachtToe(
                                ingerichtTalent,
                                gewensteLeerkracht
                        );
            }
        }
    }

    private String veiligBericht(
            RuntimeException exception
    ) {
        if (exception.getMessage() == null
                || exception.getMessage().isBlank()) {

            return exception
                    .getClass()
                    .getSimpleName();
        }

        return exception.getMessage();
    }
}