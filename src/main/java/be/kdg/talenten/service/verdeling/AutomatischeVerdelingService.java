package be.kdg.talenten.service.verdeling;

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
            TalentenPeriode talentenPeriode
    ) {
        valideerTalentenPeriode(
                talentenPeriode
        );

        return !toewijzingRepository
                .zoekVoorPeriode(
                        talentenPeriode
                )
                .isEmpty();
    }

    public VerdelingsResultaat voerAutomatischeVerdelingUit(
            TalentenPeriode talentenPeriode
    ) {
        valideerTalentenPeriode(
                talentenPeriode
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
         * Alleen leerlingen die deelnemen aan de talentenwerking
         * mogen automatisch verdeeld worden.
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
                        .toList();


        /*
         * De voorkeuren blijven volledig behouden.
         *
         * We filteren hier alleen voorkeuren van leerlingen
         * die niet deelnemen.
         *
         * BELANGRIJK:
         * Voorkeuren naar een inactief IngerichtTalent worden
         * hier NIET verwijderd.
         *
         * AutomatischeVerdeler slaat zo'n voorkeur tijdens
         * het verdelen over.
         *
         * Daardoor kan een leerling bijvoorbeeld nog:
         *
         * 1. Digitale Media  -> inactief
         * 2. Voetbal         -> actief
         * 3. Koken           -> actief
         *
         * hebben en alsnog op voorkeur 2 terechtkomen.
         */
        List<Voorkeur> voorkeuren =
                voorkeurRepository
                        .zoekVoorPeriode(
                                talentenPeriode
                        )
                        .stream()
                        .filter(
                                voorkeur ->
                                        voorkeur
                                                .getLeerling()
                                                .isActief()
                        )
                        .toList();


        /*
         * Historische toewijzingen uit hetzelfde schooljaar
         * worden meegegeven aan de verdeler zodat een leerling
         * indien mogelijk niet opnieuw hetzelfde basistalent volgt.
         */
        List<Toewijzing> historischeToewijzingen =
                toewijzingRepository
                        .zoekHistorischeToewijzingenVoorSchooljaar(
                                talentenPeriode.getSchooljaar()
                        )
                        .stream()
                        .filter(
                                toewijzing ->
                                        toewijzing
                                                .getIngerichtTalent()
                                                .getTalentenPeriode()
                                                .getSchooljaar()
                                                .equals(
                                                        talentenPeriode
                                                                .getSchooljaar()
                                                )
                        )
                        .toList();


        /*
         * Manuele toewijzingen worden behouden.
         * De automatische verdeler moet hier rekening mee houden.
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
         * Eventuele problemen uit de voorkeurimport worden
         * alleen toegevoegd wanneer de betreffende leerling
         * uiteindelijk niet toegewezen kon worden.
         */
        for (VoorkeurImportProbleem voorkeurImportProbleem :
                voorkeurImportProbleemRepository
                        .zoekVoorPeriode(
                                talentenPeriode
                        )) {

            if (resultaat
                    .getNietToegewezenLeerlingen()
                    .contains(
                            voorkeurImportProbleem.getLeerling()
                    )) {

                resultaat.voegImportProbleemToe(
                        voorkeurImportProbleem
                );
            }
        }


        /*
         * Alleen de automatische toewijzingen voor deze periode
         * worden vervangen.
         *
         * Manuele toewijzingen blijven in de databank staan.
         */
        toewijzingRepository
                .vervangAutomatischeToewijzingenVoorPeriode(
                        talentenPeriode,
                        resultaat.getToewijzingen()
                );


        return resultaat;
    }

    private void valideerTalentenPeriode(
            TalentenPeriode talentenPeriode
    ) {
        if (talentenPeriode == null) {
            throw new IllegalArgumentException(
                    "Talentenperiode mag niet null zijn."
            );
        }
    }
}