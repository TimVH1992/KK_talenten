package be.kdg.talenten.repository;

import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;

import java.util.List;

public interface TalentenPeriodeRepository {
    TalentenPeriode save(TalentenPeriode periode);
    List<TalentenPeriode> zoekAlle();
    List<TalentenPeriode> zoekVoorSchooljaar(Schooljaar schooljaar);
    TalentenPeriode zoekOpId(long id);
    void update(TalentenPeriode periode);
    void delete(TalentenPeriode periode);
}
