package be.kdg.talenten.service.voorkeuren;

import be.kdg.talenten.domain.VoorkeurImportProbleem;

import java.util.List;

public class VoorkeurenImportResultaat {
    private final List<VoorkeurImportProbleem> problemen;
    private final List<NietGekoppeldeLeerlingImportProbleem> nietGekoppeldeLeerlingen;

    public VoorkeurenImportResultaat(List<VoorkeurImportProbleem> problemen) {
        this(problemen, List.of());
    }

    public VoorkeurenImportResultaat(
            List<VoorkeurImportProbleem> problemen,
            List<NietGekoppeldeLeerlingImportProbleem> nietGekoppeldeLeerlingen
    ) {
        this.problemen = problemen;
        this.nietGekoppeldeLeerlingen = nietGekoppeldeLeerlingen;
    }

    public List<VoorkeurImportProbleem> getProblemen() {
        return problemen;
    }

    public List<NietGekoppeldeLeerlingImportProbleem> getNietGekoppeldeLeerlingen() {
        return nietGekoppeldeLeerlingen;
    }

    public int getTotaalAantalProblemen() {
        return problemen.size() + nietGekoppeldeLeerlingen.size();
    }
}
