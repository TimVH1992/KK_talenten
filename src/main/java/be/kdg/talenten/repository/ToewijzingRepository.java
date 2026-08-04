package be.kdg.talenten.repository;

import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;

import java.util.List;

public interface ToewijzingRepository {
    List<Toewijzing> zoekHistorischeToewijzingen();
    void saveAll(List<Toewijzing> toewijzingen);

    Toewijzing zoekToewijzingVoorLeerlingEnPeriode(
            Leerling leerling,
            TalentenPeriode talentenPeriode
    );

    int telToewijzingenVoorIngerichtTalent(IngerichtTalent ingerichtTalent);

    Toewijzing save(Toewijzing toewijzing);

    Toewijzing update(Toewijzing toewijzing);

    List<Toewijzing> zoekVoorPeriode(TalentenPeriode periode);
}
