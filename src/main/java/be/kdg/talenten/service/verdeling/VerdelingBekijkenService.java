package be.kdg.talenten.service.verdeling;

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

        this.ingerichtTalentRepository = ingerichtTalentRepository;
        this.toewijzingRepository = toewijzingRepository;
        this.leerlingRepository = leerlingRepository;
        this.leerlingKlasHistoriekRepository =
                leerlingKlasHistoriekRepository;
    }

    public List<IngerichtTalentOverzicht> bekijkPerIngerichtTalent(
            TalentenPeriode periode
    ) {
        valideerPeriode(periode);

        Map<IngerichtTalent, List<Toewijzing>> toewijzingenPerTalent =
                new LinkedHashMap<>();

        for (IngerichtTalent ingerichtTalent :
                ingerichtTalentRepository.zoekVoorPeriode(periode)) {

            toewijzingenPerTalent.put(
                    ingerichtTalent,
                    new ArrayList<>()
            );
        }

        for (Toewijzing toewijzing :
                toewijzingRepository.zoekVoorPeriode(periode)) {

            List<Toewijzing> toewijzingen =
                    toewijzingenPerTalent.get(
                            toewijzing.getIngerichtTalent()
                    );

            if (toewijzingen != null) {
                toewijzingen.add(toewijzing);
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
        valideerPeriode(periode);

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

        for (LeerlingKlasHistoriek historiek : klasHistoriek) {
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

    public List<NietToegewezenLeerlingOverzicht>
    bekijkNietToegewezenLeerlingen(
            TalentenPeriode periode
    ) {
        valideerPeriode(periode);

        List<Leerling> leerlingen =
                leerlingRepository
                        .zoekVoorSchooljaar(
                                periode.getSchooljaar()
                        )
                        .stream()
                        .filter(Leerling::isActief)
                        .toList();

        Set<Leerling> toegewezenLeerlingen =
                toewijzingRepository
                        .zoekVoorPeriode(periode)
                        .stream()
                        .map(Toewijzing::getLeerling)
                        .collect(Collectors.toSet());

        return leerlingen.stream()
                .filter(leerling ->
                        !toegewezenLeerlingen.contains(leerling)
                )
                .sorted(
                        Comparator
                                .comparing(
                                        (Leerling leerling) ->
                                                leerling
                                                        .getKlas()
                                                        .getNaam()
                                )
                                .thenComparing(
                                        Leerling::getAchternaam
                                )
                                .thenComparing(
                                        Leerling::getVoornaam
                                )
                )
                .map(leerling ->
                        new NietToegewezenLeerlingOverzicht(
                                leerling,
                                leerling.getVoornaam()
                                        + " "
                                        + leerling.getAchternaam(),
                                leerling.getKlas().getNaam()
                        )
                )
                .toList();
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
}