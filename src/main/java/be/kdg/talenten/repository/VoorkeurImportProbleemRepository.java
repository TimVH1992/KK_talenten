package be.kdg.talenten.repository;

import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.VoorkeurImportProbleem;

import java.util.List;

public interface VoorkeurImportProbleemRepository {
    void save(VoorkeurImportProbleem probleem);

    List<VoorkeurImportProbleem> zoekVoorLeerlingEnPeriode(Leerling leerling, TalentenPeriode periode);

    List<VoorkeurImportProbleem> zoekVoorPeriode(TalentenPeriode periode);
    void verwijderVoorLeerlingEnPeriode(Leerling leerling, TalentenPeriode periode);
}