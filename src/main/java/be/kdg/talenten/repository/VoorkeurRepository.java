package be.kdg.talenten.repository;

import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Voorkeur;

import java.util.List;

public interface VoorkeurRepository {
    Voorkeur save(Voorkeur voorkeur);
    List<Voorkeur> zoekVoorPeriode(TalentenPeriode periode);
    List<Voorkeur> zoekVoorLeerlingEnPeriode(Leerling leerling, TalentenPeriode periode);
    void verwijderVoorLeerlingEnPeriode(Leerling leerling, TalentenPeriode periode);
}
