package be.kdg.talenten.repository;

import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.TalentenPeriode;

import java.util.List;

public interface IngerichtTalentRepository {
    IngerichtTalent save (IngerichtTalent ingerichtTalent);
    List<IngerichtTalent> zoekVoorPeriode(TalentenPeriode periode);
    IngerichtTalent zoekOpId(long id);
}
