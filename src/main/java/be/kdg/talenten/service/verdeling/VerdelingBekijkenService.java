package be.kdg.talenten.service.verdeling;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.LeerlingKlasHistoriek;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.overzicht.IngerichtTalentOverzicht;
import be.kdg.talenten.overzicht.KlasOverzicht;
import be.kdg.talenten.overzicht.LeerlingToewijzingOverzicht;
import be.kdg.talenten.overzicht.NietToegewezenLeerlingOverzicht;
import be.kdg.talenten.repository.IngerichtTalentRepository;
import be.kdg.talenten.repository.LeerlingKlasHistoriekRepository;
import be.kdg.talenten.repository.LeerlingRepository;
import be.kdg.talenten.repository.ToewijzingRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VerdelingBekijkenService {

    private final IngerichtTalentRepository ingerichtTalentRepository;
    private final ToewijzingRepository toewijzingRepository;
    private final LeerlingRepository leerlingRepository;
    private final LeerlingKlasHistoriekRepository leerlingKlasHistoriekRepository;

    public VerdelingBekijkenService(
            IngerichtTalentRepository ingerichtTalentRepository,
            ToewijzingRepository toewijzingRepository,
            LeerlingRepository leerlingRepository,
            LeerlingKlasHistoriekRepository leerlingKlasHistoriekRepository
    ) {
        if (ingerichtTalentRepository == null) {
            throw new IllegalArgumentException(
                    "IngerichtTalentRepository mag niet null zijn"
            );
        }

        if (toewijzingRepository == null) {
            throw new IllegalArgumentException(
                    "ToewijzingRepository mag niet null zijn"
            );
        }

        if (leerlingRepository == null) {
            throw new IllegalArgumentException(
                    "LeerlingRepository mag niet null zijn"
            );
        }

        if (leerlingKlasHistoriekRepository == null) {
            throw new IllegalArgumentException(
                    "LeerlingKlasHistoriekRepository mag niet null zijn"
            );
        }

        this.ingerichtTalentRepository =
                ingerichtTalentRepository;

        this.toewijzingRepository =
                toewijzingRepository;

        this.leerlingRepository =
                leerlingRepository;

        this.leerlingKlasHistoriekRepository =
                leerlingKlasHistoriekRepository;
    }

    /*
     * Bestaande methode blijft bestaan.
     * null als doelgroep betekent: alle doelgroepen.
     */
    public List<IngerichtTalentOverzicht> bekijkPerIngerichtTalent(
            TalentenPeriode periode
    ) {
        return bekijkPerIngerichtTalent(
                periode,
                null
        );
    }

    public List<IngerichtTalentOverzicht> bekijkPerIngerichtTalent(
            TalentenPeriode periode,
            Doelgroep doelgroep
    ) {
        valideerPeriode(
                periode
        );

        Map<IngerichtTalent, List<Toewijzing>> toewijzingenPerTalent =
                new LinkedHashMap<>();

        List<IngerichtTalent> ingerichteTalenten =
                ingerichtTalentRepository
                        .zoekVoorPeriode(
                                periode
                        )
                        .stream()
                        .filter(
                                IngerichtTalent::isActief
                        )
                        .filter(
                                ingerichtTalent ->
                                        doelgroep == null
                                                || ingerichtTalent
                                                .getDoelgroep()
                                                == doelgroep
                        )
                        .toList();

        for (IngerichtTalent ingerichtTalent :
                ingerichteTalenten) {

            toewijzingenPerTalent.put(
                    ingerichtTalent,
                    new ArrayList<>()
            );
        }

        for (Toewijzing toewijzing :
                toewijzingRepository
                        .zoekVoorPeriode(
                                periode
                        )) {

            List<Toewijzing> toewijzingen =
                    toewijzingenPerTalent.get(
                            toewijzing.getIngerichtTalent()
                    );

            if (toewijzingen != null) {
                toewijzingen.add(
                        toewijzing
                );
            }
        }

        List<IngerichtTalentOverzicht> overzichten =
                new ArrayList<>();

        for (Map.Entry<IngerichtTalent, List<Toewijzing>> entry :
                toewijzingenPerTalent.entrySet()) {

            IngerichtTalent ingerichtTalent =
                    entry.getKey();

            List<Toewijzing> toewijzingen =
                    entry.getValue();

            int toegewezenAantal =
                    toewijzingen.size();

            int vrijePlaatsen =
                    ingerichtTalent.getMaxCapaciteit()
                            - toegewezenAantal;

            overzichten.add(
                    new IngerichtTalentOverzicht(
                            ingerichtTalent,
                            toegewezenAantal,
                            vrijePlaatsen,
                            toewijzingen
                    )
            );
        }

        return overzichten;
    }

    public KlasOverzicht bekijkVoorKlas(
            TalentenPeriode periode,
            Klas klas
    ) {
        valideerPeriode(
                periode
        );

        if (klas == null) {
            throw new IllegalArgumentException(
                    "Klas mag niet null zijn"
            );
        }

        List<LeerlingKlasHistoriek> klasHistoriek =
                leerlingKlasHistoriekRepository
                        .zoekVoorKlasOpDatum(
                                klas,
                                periode.getStartDatum()
                        );

        List<LeerlingToewijzingOverzicht> toewijzingenPerKlas =
                new ArrayList<>();

        for (LeerlingKlasHistoriek historiek :
                klasHistoriek) {

            Leerling leerling =
                    historiek.getLeerling();

            Toewijzing toewijzing =
                    toewijzingRepository
                            .zoekToewijzingVoorLeerlingEnPeriode(
                                    leerling,
                                    periode
                            );

            toewijzingenPerKlas.add(
                    new LeerlingToewijzingOverzicht(
                            leerling,
                            toewijzing
                    )
            );
        }

        return new KlasOverzicht(
                klas,
                periode,
                toewijzingenPerKlas
        );
    }

    /*
     * Bestaande methode blijft bestaan.
     * Zonder doelgroepfilter worden alle doelgroepen weergegeven.
     */
    public List<NietToegewezenLeerlingOverzicht>
    bekijkNietToegewezenLeerlingen(
            TalentenPeriode periode
    ) {
        return bekijkNietToegewezenLeerlingen(
                periode,
                null
        );
    }

    public List<NietToegewezenLeerlingOverzicht>
    bekijkNietToegewezenLeerlingen(
            TalentenPeriode periode,
            Doelgroep doelgroep
    ) {
        valideerPeriode(
                periode
        );

        List<LeerlingMetKlas> leerlingen =
                leerlingRepository
                        .zoekVoorSchooljaar(
                                periode.getSchooljaar()
                        )
                        .stream()
                        .filter(
                                Leerling::isActief
                        )
                        .map(
                                leerling ->
                                        new LeerlingMetKlas(
                                                leerling,
                                                bepaalKlasOpStartPeriode(
                                                        leerling,
                                                        periode
                                                )
                                        )
                        )
                        .filter(
                                leerlingMetKlas ->
                                        leerlingMetKlas.klas() != null
                        )
                        .filter(
                                leerlingMetKlas ->
                                        doelgroep == null
                                                || leerlingMetKlas
                                                .klas()
                                                .getDoelgroep()
                                                == doelgroep
                        )
                        .toList();

        Set<Leerling> toegewezenLeerlingen =
                toewijzingRepository
                        .zoekVoorPeriode(
                                periode
                        )
                        .stream()
                        .filter(
                                toewijzing ->
                                        toewijzing
                                                .getIngerichtTalent()
                                                .isActief()
                        )
                        .map(
                                Toewijzing::getLeerling
                        )
                        .collect(
                                Collectors.toSet()
                        );

        return leerlingen
                .stream()
                .filter(
                        leerlingMetKlas ->
                                !toegewezenLeerlingen.contains(
                                        leerlingMetKlas.leerling()
                                )
                )
                .sorted(
                        Comparator
                                .comparing(
                                        (LeerlingMetKlas leerlingMetKlas) ->
                                                leerlingMetKlas
                                                        .klas()
                                                        .getNaam()
                                )
                                .thenComparing(
                                        leerlingMetKlas ->
                                                leerlingMetKlas
                                                        .leerling()
                                                        .getAchternaam()
                                )
                                .thenComparing(
                                        leerlingMetKlas ->
                                                leerlingMetKlas
                                                        .leerling()
                                                        .getVoornaam()
                                )
                )
                .map(
                        leerlingMetKlas ->
                                new NietToegewezenLeerlingOverzicht(
                                        leerlingMetKlas.leerling(),
                                        leerlingMetKlas
                                                .leerling()
                                                .getVoornaam()
                                                + " "
                                                + leerlingMetKlas
                                                .leerling()
                                                .getAchternaam(),
                                        leerlingMetKlas
                                                .klas()
                                                .getNaam()
                                )
                )
                .toList();
    }

    private Klas bepaalKlasOpStartPeriode(
            Leerling leerling,
            TalentenPeriode periode
    ) {
        return leerlingKlasHistoriekRepository
                .zoekVoorLeerling(
                        leerling
                )
                .stream()
                .filter(
                        historiek ->
                                !historiek
                                        .getVanaf()
                                        .isAfter(
                                                periode.getStartDatum()
                                        )
                                        && (
                                        historiek.getTot() == null
                                                || historiek
                                                .getTot()
                                                .isAfter(
                                                        periode.getStartDatum()
                                                )
                                )
                )
                .map(
                        LeerlingKlasHistoriek::getKlas
                )
                .findFirst()
                .orElse(
                        leerling.getKlas()
                );
    }

    private void valideerPeriode(
            TalentenPeriode periode
    ) {
        if (periode == null) {
            throw new IllegalArgumentException(
                    "Talentenperiode mag niet null zijn"
            );
        }
    }

    private record LeerlingMetKlas(
            Leerling leerling,
            Klas klas
    ) {
    }
}