package be.kdg.talenten.repository.inmemory;

import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.VoorkeurImportProbleem;
import be.kdg.talenten.repository.VoorkeurImportProbleemRepository;

import java.util.ArrayList;
import java.util.List;

public class InMemoryVoorkeurImportProbleemRepository implements VoorkeurImportProbleemRepository {
    private final List<VoorkeurImportProbleem> opgeslagenProblemen;

    public InMemoryVoorkeurImportProbleemRepository(List<VoorkeurImportProbleem> opgeslagenProblemen) {
        if (opgeslagenProblemen == null) {
            throw new IllegalArgumentException("De lijst met opgeslagen importproblemen mag niet null zijn");
        }

        this.opgeslagenProblemen = opgeslagenProblemen;
    }

    @Override
    public void save(VoorkeurImportProbleem probleem) {
        if (probleem == null) {
            throw new IllegalArgumentException("Het importprobleem mag niet null zijn");
        }

        opgeslagenProblemen.add(probleem);
    }

    @Override
    public List<VoorkeurImportProbleem> zoekVoorLeerlingEnPeriode(Leerling leerling, TalentenPeriode periode) {
        if (leerling == null) {
            throw new IllegalArgumentException("De leerling mag niet null zijn");
        }
        if (periode == null) {
            throw new IllegalArgumentException("De periode mag niet null zijn");
        }

        List<VoorkeurImportProbleem> gevondenProblemen = new ArrayList<>();

        for (VoorkeurImportProbleem probleem : opgeslagenProblemen) {
            if (probleem.getLeerling().equals(leerling) && probleem.getPeriode().equals(periode)) {
                gevondenProblemen.add(probleem);
            }
        }

        return gevondenProblemen;
    }

    @Override
    public List<VoorkeurImportProbleem> zoekVoorPeriode(TalentenPeriode periode) {
        if (periode == null) {
            throw new IllegalArgumentException("De periode mag niet null zijn");
        }

        List<VoorkeurImportProbleem> gevondenProblemen = new ArrayList<>();

        for (VoorkeurImportProbleem probleem : opgeslagenProblemen) {
            if (probleem.getPeriode().equals(periode)) {
                gevondenProblemen.add(probleem);
            }
        }

        return gevondenProblemen;
    }
}