package be.kdg.talenten.repository;

import be.kdg.talenten.domain.*;

import java.util.List;

public interface ToewijzingRepository {
    List<Toewijzing> zoekHistorischeToewijzingenVoorSchooljaar(Schooljaar schooljaar);
    void saveAll(List<Toewijzing> toewijzingen);
    void vervangAutomatischeToewijzingenVoorPeriode(TalentenPeriode periode, List<Toewijzing> nieuweToewijzingen);

    Toewijzing zoekToewijzingVoorLeerlingEnPeriode(
            Leerling leerling,
            TalentenPeriode talentenPeriode
    );

    int telToewijzingenVoorIngerichtTalent(IngerichtTalent ingerichtTalent);

    Toewijzing save(Toewijzing toewijzing);

    Toewijzing update(Toewijzing toewijzing);

    List<Toewijzing> zoekVoorPeriode(TalentenPeriode periode);
    List<Toewijzing> zoekHistorischeToewijzingenVoorLeerlingEnSchooljaar(Leerling leerling, Schooljaar schooljaar);
    void verwijderVoorIngerichtTalent(IngerichtTalent ingerichtTalent);
}
