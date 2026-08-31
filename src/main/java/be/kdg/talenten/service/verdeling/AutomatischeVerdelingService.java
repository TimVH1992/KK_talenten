package be.kdg.talenten.service.verdeling;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.domain.ToewijzingsType;
import be.kdg.talenten.domain.Voorkeur;
import be.kdg.talenten.domain.VoorkeurImportProbleem;
import be.kdg.talenten.repository.LeerlingRepository;
import be.kdg.talenten.repository.ToewijzingRepository;
import be.kdg.talenten.repository.VoorkeurImportProbleemRepository;
import be.kdg.talenten.repository.VoorkeurRepository;
import be.kdg.talenten.verdeling.AutomatischeVerdeler;
import be.kdg.talenten.verdeling.VerdelingsResultaat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AutomatischeVerdelingService {

    private final VoorkeurRepository voorkeurRepository;
    private final ToewijzingRepository toewijzingRepository;
    private final LeerlingRepository leerlingRepository;
    private final VoorkeurImportProbleemRepository voorkeurImportProbleemRepository;

    public AutomatischeVerdelingService(
            VoorkeurRepository voorkeurRepository,
            ToewijzingRepository toewijzingRepository,
            LeerlingRepository leerlingRepository,
            VoorkeurImportProbleemRepository voorkeurImportProbleemRepository
    ) {
        if (voorkeurRepository == null) {
            throw new IllegalArgumentException(
                    "De voorkeurRepository mag niet null zijn"
            );
        }

        if (toewijzingRepository == null) {
            throw new IllegalArgumentException(
                    "De toewijzingRepository mag niet null zijn"
            );
        }

        if (leerlingRepository == null) {
            throw new IllegalArgumentException(
                    "De leerlingRepository mag niet null zijn"
            );
        }

        if (voorkeurImportProbleemRepository == null) {
            throw new IllegalArgumentException(
                    "De voorkeurImportProbleemRepository mag niet null zijn"
            );
        }

        this.voorkeurRepository =
                voorkeurRepository;

        this.toewijzingRepository =
                toewijzingRepository;

        this.leerlingRepository =
                leerlingRepository;

        this.voorkeurImportProbleemRepository =
                voorkeurImportProbleemRepository;
    }

    public boolean heeftBestaandeToewijzingen(
            TalentenPeriode talentenPeriode,
            Doelgroep doelgroep
    ) {
        valideerContext(
                talentenPeriode,
                doelgroep
        );

        return toewijzingRepository
                .zoekVoorPeriode(
                        talentenPeriode
                )
                .stream()
                .anyMatch(
                        toewijzing ->
                                toewijzing
                                        .getIngerichtTalent()
                                        .getDoelgroep()
                                        == doelgroep
                );
    }

    public VoorkeurenDekking bepaalVoorkeurenDekking(
            TalentenPeriode talentenPeriode,
            Doelgroep doelgroep
    ) {
        valideerContext(talentenPeriode, doelgroep);

        List<Leerling> leerlingen = leerlingRepository
                .zoekVoorSchooljaar(talentenPeriode.getSchooljaar())
                .stream()
                .filter(Leerling::isActief)
                .filter(leerling -> leerling.getKlas().getDoelgroep() == doelgroep)
                .toList();

        List<Voorkeur> voorkeuren = voorkeurRepository
                .zoekVoorPeriode(talentenPeriode)
                .stream()
                .filter(voorkeur -> bevatLeerling(leerlingen, voorkeur.getLeerling()))
                .toList();

        int volledig = 0;
        for (Leerling leerling : leerlingen) {
            Set<Integer> nummers = voorkeuren.stream()
                    .filter(voorkeur -> zelfdeLeerling(voorkeur.getLeerling(), leerling))
                    .map(Voorkeur::getVoorkeurNummer)
                    .collect(Collectors.toSet());
            if (nummers.equals(Set.of(1, 2, 3))) volledig++;
        }

        return new VoorkeurenDekking(volledig, leerlingen.size());
    }

    public record VoorkeurenDekking(int leerlingenMetVolledigeVoorkeuren, int totaalLeerlingen) {
        public int leerlingenZonderVolledigeVoorkeuren() {
            return totaalLeerlingen - leerlingenMetVolledigeVoorkeuren;
        }
    }

    public VerdelingsResultaat voerAutomatischeVerdelingUit(
            TalentenPeriode talentenPeriode,
            Doelgroep doelgroep
    ) {
        valideerContext(
                talentenPeriode,
                doelgroep
        );

        if (talentenPeriode
                .getEindDatum()
                .isBefore(
                        LocalDate.now()
                )) {

            throw new IllegalStateException(
                    "Een afgelopen talentenperiode mag niet meer automatisch verdeeld worden."
            );
        }

        /*
         * Alleen actieve leerlingen uit de gekozen doelgroep
         * nemen deel aan deze verdeling.
         *
         * De doelgroep van een leerling volgt uit zijn klas.
         */
        List<Leerling> leerlingen =
                leerlingRepository
                        .zoekVoorSchooljaar(
                                talentenPeriode.getSchooljaar()
                        )
                        .stream()
                        .filter(
                                Leerling::isActief
                        )
                        .filter(
                                leerling ->
                                        leerling.getKlas()
                                                .getDoelgroep()
                                                == doelgroep
                        )
                        .toList();

        /*
         * Alleen voorkeuren van leerlingen uit deze verdelingsgroep
         * worden aan de verdeler doorgegeven.
         *
         * Een voorkeur naar een andere doelgroep is foutieve data
         * en wordt bewust niet stil genegeerd.
         */
        List<Voorkeur> voorkeuren =
                voorkeurRepository
                        .zoekVoorPeriode(
                                talentenPeriode
                        )
                        .stream()
                        .filter(
                                voorkeur ->
                                        bevatLeerling(
                                                leerlingen,
                                                voorkeur.getLeerling()
                                        )
                        )
                        .toList();

        valideerVoorkeurDoelgroepen(
                voorkeuren,
                doelgroep
        );

        /*
         * Historiek wordt alleen meegenomen voor leerlingen die
         * momenteel in deze verdeling zitten.
         *
         * We filteren historische ingerichte talenten niet op
         * doelgroep: als een leerling hetzelfde basistalent eerder
         * al gevolgd heeft, blijft dat relevante historiek.
         */
        List<Toewijzing> historischeToewijzingen =
                toewijzingRepository
                        .zoekHistorischeToewijzingenVoorSchooljaar(
                                talentenPeriode.getSchooljaar()
                        )
                        .stream()
                        .filter(
                                toewijzing ->
                                        bevatLeerling(
                                                leerlingen,
                                                toewijzing.getLeerling()
                                        )
                        )
                        .toList();

        /*
         * Alleen manuele toewijzingen uit deze doelgroep moeten
         * de capaciteit binnen deze verdeling beïnvloeden.
         */
        List<Toewijzing> manueleToewijzingen =
                toewijzingRepository
                        .zoekVoorPeriode(
                                talentenPeriode
                        )
                        .stream()
                        .filter(
                                toewijzing ->
                                        toewijzing
                                                .getToewijzingsType()
                                                == ToewijzingsType.MANUEEL
                        )
                        .filter(
                                toewijzing ->
                                        toewijzing
                                                .getIngerichtTalent()
                                                .getDoelgroep()
                                                == doelgroep
                        )
                        .toList();

        AutomatischeVerdeler verdeler =
                new AutomatischeVerdeler(
                        leerlingen,
                        voorkeuren,
                        historischeToewijzingen,
                        manueleToewijzingen
                );

        VerdelingsResultaat resultaat =
                verdeler.verdeel();

        /*
         * Importproblemen worden alleen toegevoegd voor leerlingen
         * die tot deze doelgroep behoren én uiteindelijk niet
         * toegewezen zijn.
         */
        for (VoorkeurImportProbleem probleem :
                voorkeurImportProbleemRepository
                        .zoekVoorPeriode(
                                talentenPeriode
                        )) {

            if (!bevatLeerling(
                    leerlingen,
                    probleem.getLeerling()
            )) {
                continue;
            }

            if (bevatLeerling(
                    resultaat.getNietToegewezenLeerlingen(),
                    probleem.getLeerling()
            )) {
                resultaat.voegImportProbleemToe(
                        probleem
                );
            }
        }

        /*
         * CRUCIAAL:
         *
         * Alleen automatische toewijzingen van DEZE doelgroep
         * worden vervangen.
         *
         * De andere doelgroep blijft volledig onaangeroerd.
         */
        toewijzingRepository
                .vervangAutomatischeToewijzingenVoorPeriodeEnDoelgroep(
                        talentenPeriode,
                        doelgroep,
                        resultaat.getToewijzingen()
                );

        return resultaat;
    }

    private void valideerVoorkeurDoelgroepen(
            List<Voorkeur> voorkeuren,
            Doelgroep doelgroep
    ) {
        for (Voorkeur voorkeur : voorkeuren) {
            if (voorkeur
                    .getIngerichtTalent()
                    .getDoelgroep()
                    != doelgroep) {

                throw new IllegalStateException(
                        "Leerling "
                                + voorkeur.getLeerling()
                                + " heeft een voorkeur voor een talent uit een andere doelgroep."
                );
            }
        }
    }

    private boolean bevatLeerling(
            List<Leerling> leerlingen,
            Leerling gezochteLeerling
    ) {
        for (Leerling leerling : leerlingen) {
            if (zelfdeLeerling(
                    leerling,
                    gezochteLeerling
            )) {
                return true;
            }
        }

        return false;
    }

    private boolean zelfdeLeerling(
            Leerling eerste,
            Leerling tweede
    ) {
        if (eerste == null || tweede == null) {
            return false;
        }

        if (eerste.getId() != null
                && tweede.getId() != null) {

            return eerste
                    .getId()
                    .equals(
                            tweede.getId()
                    );
        }

        return eerste == tweede;
    }

    private void valideerContext(
            TalentenPeriode talentenPeriode,
            Doelgroep doelgroep
    ) {
        if (talentenPeriode == null) {
            throw new IllegalArgumentException(
                    "Talentenperiode mag niet null zijn."
            );
        }

        if (doelgroep == null) {
            throw new IllegalArgumentException(
                    "Doelgroep mag niet null zijn."
            );
        }
    }
}
