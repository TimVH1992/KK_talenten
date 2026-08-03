package be.kdg.talenten.repository;

import be.kdg.talenten.domain.TalentenPeriode;

import java.util.List;

public interface TalentenPeriodeRepository {
    TalentenPeriode save (TalentenPeriode periode);
    List<TalentenPeriode> zoekAlle();
}
