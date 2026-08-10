package be.kdg.talenten.repository;

import be.kdg.talenten.domain.*;

import java.util.List;

public interface IngerichtTalentRepository {
    IngerichtTalent save (IngerichtTalent ingerichtTalent);
    List<IngerichtTalent> zoekVoorPeriode(TalentenPeriode periode);
    List<IngerichtTalent> zoekActieveVoorPeriodeEnDoelgroep(
            TalentenPeriode periode,
            Doelgroep doelgroep
    );
    IngerichtTalent zoekOpId(long id);
}
