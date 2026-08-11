package be.kdg.talenten.service.voorkeuren;

import be.kdg.talenten.domain.VoorkeurImportProbleem;

import java.util.List;

public class VoorkeurenImportResultaat {
    private final List<VoorkeurImportProbleem> problemen;

    public VoorkeurenImportResultaat(List<VoorkeurImportProbleem> problemen) {
        this.problemen = problemen;
    }

    public List<VoorkeurImportProbleem> getProblemen() {
        return problemen;
    }
}